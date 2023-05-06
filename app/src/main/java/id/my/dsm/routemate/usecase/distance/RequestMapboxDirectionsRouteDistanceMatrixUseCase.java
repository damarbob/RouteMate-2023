package id.my.dsm.routemate.usecase.distance;

import androidx.annotation.NonNull;

import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import javax.inject.Inject;

import id.my.dsm.routemate.data.event.network.OnDistanceMatrixResponse;
import id.my.dsm.routemate.data.event.view.OnProgressIndicatorUpdate;
import id.my.dsm.routemate.data.model.maps.MapboxDirectionsRoute;
import id.my.dsm.routemate.data.model.place.Place;
import id.my.dsm.routemate.data.repo.distance.DistanceRepositoryN;
import id.my.dsm.routemate.data.repo.place.PlaceRepositoryN;
import id.my.dsm.routemate.data.source.mapbox.MapboxDataSource;
import id.my.dsm.vrpsolver.model.MatrixElement;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestMapboxDirectionsRouteDistanceMatrixUseCase {

    private static final String TAG = RequestMapboxDirectionsRouteDistanceMatrixUseCase.class.getName();

    private final PlaceRepositoryN placeRepository;
    private final DistanceRepositoryN distanceRepository;

    private static int taskQueueTotal = 0;
    private static int taskFinished = 0;

    @Inject
    public RequestMapboxDirectionsRouteDistanceMatrixUseCase(PlaceRepositoryN placeRepository, DistanceRepositoryN distanceRepository) {
        this.placeRepository = placeRepository;
        this.distanceRepository = distanceRepository;

        // SHOULD NOT BE HERE! NON-STANDARD USE CASE IMPLEMENTATION
        distanceRepository.calculateAirDistances(placeRepository.getRecords(), true);
    }

    /**
     * Retrieve directions from Mapbox server
     *
     * @param mapboxProfile a {@link DirectionsCriteria} mapboxProfile
     * @param continueOptimization whether to continue to optimization
     */
    public void invoke(@NonNull String mapboxProfile, boolean continueOptimization) {

        taskQueueTotal = distanceRepository.getRecordsCount();

        List<Place> places = placeRepository.getRecords(); // Get places object from repository
        List<MatrixElement> matrixElements = distanceRepository.getRecords(); // Get matrixElements object from repository

        MatrixElement matrixElement = distanceRepository.getRecordByIndex(taskFinished);

        // TODO: Make LatLng adapter
        LatLng originLatLng = new LatLng(
                matrixElement.getOrigin().getLatLngAlt().getLatitude(),
                matrixElement.getOrigin().getLatLngAlt().getLongitude()
        );
        LatLng destLatLng = new LatLng(
                matrixElement.getDestination().getLatLngAlt().getLatitude(),
                matrixElement.getDestination().getLatLngAlt().getLongitude()
        );

        MapboxDataSource.getRoute(
                originLatLng,
                destLatLng,
                mapboxProfile, // TODO: Make adaptive to each vehicle
                new Callback<DirectionsResponse>() {
                    @Override
                    public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {

                        taskFinished++; // On success, add into taskFinished

                        int progress = (taskFinished * (continueOptimization ? 99 : 100)) / taskQueueTotal;

                        // To use the progressIndicator in activity
                        EventBus.getDefault().postSticky(
                                new OnProgressIndicatorUpdate(progress)
                        );

                        // Bind directionsRoute to matrixElement
                        DirectionsRoute directionsRoute = response.body().routes().get(0);
                        MapboxDirectionsRoute mapboxDirectionsRoute = MapboxDirectionsRoute.fromDirectionsRoute(directionsRoute);

                        matrixElement.setDistance(mapboxDirectionsRoute.getDistance());

                        // OnFinished callback if condition met
                        if (progress == (continueOptimization ? 99 : 100)) {

                            taskFinished = 0; // Reset the taskFinishedCount to 0

                            // OnFinished callback
                            EventBus.getDefault().post(
                                    new OnDistanceMatrixResponse(matrixElements, continueOptimization)
                            );

                        } else {
                            invoke(DirectionsCriteria.PROFILE_DRIVING, continueOptimization);
                        }

                    }

                    @Override
                    public void onFailure(Call<DirectionsResponse> call, Throwable t) {

                    }
                }
        );

    }

}
