package id.my.dsm.routemate.usecase.distance;

import android.util.Log;

import androidx.annotation.NonNull;

import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.matrix.v1.models.MatrixResponse;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import javax.inject.Inject;

import id.my.dsm.routemate.data.event.network.OnDistanceMatrixResponse;
import id.my.dsm.routemate.data.event.view.OnProgressIndicatorUpdate;
import id.my.dsm.routemate.data.place.Place;
import id.my.dsm.routemate.data.repo.distance.DistanceRepositoryN;
import id.my.dsm.routemate.data.repo.place.PlaceRepositoryN;
import id.my.dsm.routemate.library.dsmlib.model.MatrixElement;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestMapboxDistanceMatrixUseCase {

    private static final String TAG = RequestMapboxDistanceMatrixUseCase.class.getName();

    private final RequestMapboxMatrixUseCase requestMapboxMatrixUseCase;
    private final PlaceRepositoryN placeRepository;
    private final DistanceRepositoryN distanceRepository;

    @Inject
    public RequestMapboxDistanceMatrixUseCase(RequestMapboxMatrixUseCase requestMapboxMatrixUseCase, PlaceRepositoryN placeRepository, DistanceRepositoryN distanceRepository) {
        this.requestMapboxMatrixUseCase = requestMapboxMatrixUseCase;
        this.placeRepository = placeRepository;
        this.distanceRepository = distanceRepository;
    }

    public void invoke(@NonNull String mapboxProfile, boolean continueOptimization) {

        List<Place> places = placeRepository.getRecords(); // Get places object from repository
        List<MatrixElement> matrixElements = distanceRepository.getRecords(); // Get matrixElements object from repository

        // Requirements
        if (mapboxProfile.equals(DirectionsCriteria.PROFILE_DRIVING_TRAFFIC) && places.size() > 10) {
            Log.e(TAG, "obtainDistanceMatrix: Current profile does not support more than 10 places!");
            return;
        }

        EventBus.getDefault().postSticky(new OnProgressIndicatorUpdate(25)); // To use the progressIndicator in activity

        requestMapboxMatrixUseCase.invoke(places, mapboxProfile, new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<MatrixResponse> call, @NonNull Response<MatrixResponse> response) {

                MatrixResponse matrixResponse = response.body(); // Assign to a new variable

                Log.d(TAG, "obtainMapboxDistanceMatrix: Response: " + matrixResponse);

                if (matrixResponse == null) {
                    Log.e(TAG, "obtainMapboxDistanceMatrix: Response empty! Unable to proceed.");
                    return;
                }

                distanceRepository.clearRecord(); // On success response, clears the previous record first

                List<Double[]> distanceValues = matrixResponse.distances(); // Assign matrixElements

                // Iterate through places as origin
                for (int originIndex = 0; originIndex < places.size(); originIndex++) {

                    Place origin = places.get(originIndex);

                    // Iterate through places as destination
                    for (int destinationIndex = 0; destinationIndex < places.size(); destinationIndex++) {

                        Place destination = places.get(destinationIndex);

                        if (!(originIndex == destinationIndex)) { // Skipping zero matrixElements

                            if (distanceValues != null) {

                                // Insert new matrixElement into record list
                                MatrixElement matrixElement = new MatrixElement(origin.getDsmPlace(), destination.getDsmPlace(), distanceValues.get(originIndex)[destinationIndex]);
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

            @Override
            public void onFailure(@NonNull Call<MatrixResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to request matrixElements matrix: " + t.getMessage());
            }
        });
    }

}
