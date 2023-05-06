package id.my.dsm.routemate.usecase.solution;

import android.util.Log;

import androidx.annotation.NonNull;

import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import id.my.dsm.routemate.data.event.network.OnMapboxDirectionsRouteResponse;
import id.my.dsm.routemate.data.event.view.OnProgressIndicatorUpdate;
import id.my.dsm.routemate.data.model.maps.MapboxDirectionsRoute;
import id.my.dsm.routemate.data.model.place.Place;
import id.my.dsm.routemate.data.repo.place.PlaceRepositoryN;
import id.my.dsm.routemate.data.source.mapbox.MapboxDataSource;
import id.my.dsm.vrpsolver.model.Solution;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestMapboxDirectionsRouteUseCase {

    private static final String TAG = RequestMapboxDirectionsRouteUseCase.class.getSimpleName();

    // Dependencies
    private PlaceRepositoryN placeRepository;

    private static int taskQueueTotal = 0;
    private static int taskFinished = 0;

    @Inject
    public RequestMapboxDirectionsRouteUseCase(PlaceRepositoryN placeRepository) {
        this.placeRepository = placeRepository;
    }

    /**
     * Clears previous directions route line and fetch new directions from server
     * @param solutions ArrayList of {@link Solution}
     * @param mapboxProfile a {@link DirectionsCriteria} mapboxProfile
     */
    public void invoke(List<Solution> solutions, String mapboxProfile){

        taskFinished = 0; // Reset the taskFinishedCount to 0
        taskQueueTotal = solutions.size();

        ArrayList<Thread> threads = new ArrayList<>();

        for (Solution solution : solutions) {

            // Skip request for incomplete solution
            if (solution.getOrigin() == null)
                continue;

            Place origin = Place.Toolbox.getById(placeRepository.getRecords(), solution.getOrigin().getId());
            Place destination = Place.Toolbox.getById(placeRepository.getRecords(), solution.getDestination().getId());

            threads.add(new Thread(() -> MapboxDataSource.getRoute(
                    origin.getMapboxLatLng(),
                    destination.getMapboxLatLng(),
                    mapboxProfile, // TODO: Make adaptive to each vehicle
                    new Callback<DirectionsResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<DirectionsResponse> call, @NonNull Response<DirectionsResponse> response) {

                            taskFinished ++; // On success, add into taskFinished

                            int progress = (taskFinished * 100) / taskQueueTotal;

                            // To use the progressIndicator in activity
                            EventBus.getDefault().postSticky(
                                    progress == 100 ?
                                            new OnProgressIndicatorUpdate(progress, OnProgressIndicatorUpdate.Event.OPTIMIZATION_FINISHED) :
                                            new OnProgressIndicatorUpdate(progress)
                            );

                            // Bind directionsRoute to solution
                            DirectionsRoute directionsRoute = Objects.requireNonNull(response.body()).routes().get(0);
                            MapboxDirectionsRoute mapboxDirectionsRoute = MapboxDirectionsRoute.fromDirectionsRoute(directionsRoute);
                            if (solution.getVehicleId() != null) {
                                mapboxDirectionsRoute.getVehicleIds().add(solution.getVehicleId());
                                mapboxDirectionsRoute.getTripIndexes().add(solution.getTripIndex());
                            }

                            // To draw the line in mapsViewModel also acts as onFinished callback
                            EventBus.getDefault().post(
                                    new OnMapboxDirectionsRouteResponse(mapboxDirectionsRoute, solution)
                            );

                        }

                        @Override
                        public void onFailure(Call<DirectionsResponse> call, Throwable t) {
                            call.cancel();
                            Log.e(TAG, "invoke: Failed to get Mapbox directions route!");
                        }
                    }
            )));
        }

        for (int i = 0; i < threads.size(); i++) {
            Thread thread = threads.get(i);

            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

}
