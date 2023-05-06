package id.my.dsm.routemate.usecase.distance;

import android.util.Log;

import com.mapbox.api.matrix.v1.models.MatrixResponse;
import com.mapbox.turf.TurfConversion;

import org.greenrobot.eventbus.EventBus;

import java.text.DecimalFormat;
import java.util.List;

import javax.inject.Inject;

import id.my.dsm.routemate.data.event.network.OnDistanceMatrixResponse;
import id.my.dsm.routemate.data.event.view.OnProgressIndicatorUpdate;
import id.my.dsm.routemate.data.model.place.Place;
import id.my.dsm.routemate.data.repo.distance.DistanceRepositoryN;
import id.my.dsm.routemate.data.repo.place.PlaceRepositoryN;
import id.my.dsm.vrpsolver.model.MatrixElement;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestMapboxDurationMatrixUseCase {

    private static final String TAG = RequestMapboxDurationMatrixUseCase.class.getName();

    private final RequestMapboxMatrixUseCase requestMapboxMatrixUseCase;
    private final PlaceRepositoryN placeRepository;
    private final DistanceRepositoryN distanceRepository;

    @Inject
    public RequestMapboxDurationMatrixUseCase(RequestMapboxMatrixUseCase requestMapboxMatrixUseCase, PlaceRepositoryN placeRepository, DistanceRepositoryN distanceRepository) {
        this.requestMapboxMatrixUseCase = requestMapboxMatrixUseCase;
        this.placeRepository = placeRepository;
        this.distanceRepository = distanceRepository;
    }

    public void invoke(String mapboxProfile, boolean continueOptimization) {

        List<Place> places = placeRepository.getRecords(); // Get places object from repository
        List<MatrixElement> matrixElements = distanceRepository.getRecords(); // Get matrixElements object from repository

        EventBus.getDefault().postSticky(new OnProgressIndicatorUpdate(25)); // To use the progressIndicator in activity

        requestMapboxMatrixUseCase.invoke(places, mapboxProfile, new Callback<>() {
            @Override
            public void onResponse(Call<MatrixResponse> call, Response<MatrixResponse> response) {
                if (response.body() != null) {

                    distanceRepository.clearRecord(); // On success response, clears the previous record first

                    // TODO: Use matrixElements instead
                    List<Double[]> durationsToAllOfTheLocationsFromTheOrigin = response.body().durations();

                    // TODO: Unfinished work
                    for (int originIndex = 0; originIndex < places.size(); originIndex++) {
                        Place placeFrom = places.get(originIndex);

                        for (int destinationIndex = 0; destinationIndex < places.size(); destinationIndex++) {
                            Place placeTo = places.get(destinationIndex);

                            if (!(originIndex == destinationIndex)) {
                                String finalConvertedFormattedDistance = null;
                                if (durationsToAllOfTheLocationsFromTheOrigin != null) {

                                    // Convert matrixElement value
                                    String distanceValue = new DecimalFormat("#.##")
                                            .format(TurfConversion.convertLength(
                                                    durationsToAllOfTheLocationsFromTheOrigin.get(originIndex)[destinationIndex],
                                                    "meters", "meters"));

                                    finalConvertedFormattedDistance = distanceValue;

                                    // Insert new matrixElement into record list
                                    MatrixElement matrixElement = new MatrixElement(placeFrom.getLocation(), placeTo.getLocation(), durationsToAllOfTheLocationsFromTheOrigin.get(originIndex)[destinationIndex]);
                                    distanceRepository.createRecord(matrixElement);

                                }
                            }
                        }
                    }

                    // Update progressIndicator to 99 percent if continueOptimization (because 100 will start countdown to hide progressIndicator)
                    EventBus.getDefault().postSticky(
                            new OnProgressIndicatorUpdate(
                                    continueOptimization ? 99 : 100
                            )
                    ); // To use the progressIndicator in activity

                    // OnFinished callback
                    EventBus.getDefault().post(
                            new OnDistanceMatrixResponse(matrixElements, continueOptimization)
                    );
                }
            }

            @Override
            public void onFailure(Call<MatrixResponse> call, Throwable t) {
                Log.e(TAG, "Failed to request matrixElements matrix: " + t.getMessage());
            }
        });
    }

}
