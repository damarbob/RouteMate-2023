package id.my.dsm.routemate.usecase.distance;

import android.util.Log;

import com.google.maps.DistanceMatrixApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.model.DistanceMatrix;
import com.google.maps.model.DistanceMatrixElement;
import com.google.maps.model.DistanceMatrixRow;
import com.google.maps.model.TravelMode;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import id.my.dsm.routemate.data.event.network.OnDistanceMatrixResponse;
import id.my.dsm.routemate.data.event.view.OnProgressIndicatorUpdate;
import id.my.dsm.routemate.data.place.Place;
import id.my.dsm.routemate.data.repo.distance.DistanceRepositoryN;
import id.my.dsm.routemate.data.repo.place.PlaceRepositoryN;
import id.my.dsm.routemate.library.dsmlib.model.MatrixElement;

public class RequestGoogleDistanceMatrixUseCase {

    private static final String TAG = RequestGoogleDistanceMatrixUseCase.class.getSimpleName();

    private final PlaceRepositoryN placeRepository;
    private final DistanceRepositoryN distanceRepository;

    @Inject
    public RequestGoogleDistanceMatrixUseCase(PlaceRepositoryN placeRepository, DistanceRepositoryN distanceRepository) {
        this.placeRepository = placeRepository;
        this.distanceRepository = distanceRepository;
    }

    public void invoke(GeoApiContext geoApiContext, TravelMode travelMode, boolean continueOptimization) {

        /*
            Google MatrixElement
            1. Populate the estimated distance array
            2. Request the distance matrix to Google MatrixElement Matrix API
         */

        List<Place> places = placeRepository.getRecords(); // Get places object from repository
        List<MatrixElement> matrixElements = distanceRepository.getRecords(); // Get matrixElements object from repository

        distanceRepository.calculateAirDistances(places, true); // Populate the estimated distance array

        // Begin request

        int taskQueueTotal = places.size();
        final int[] taskFinished = {0};

        for (int i = 0, placesSize = places.size(); i < placesSize; i++) {

            Place p = places.get(i);

            com.google.maps.model.LatLng originLatLng = p.getGoogleLatLng(); // Get Google LatLng of the origin place

            // Build distance matrix request
            DistanceMatrixApiRequest distanceMatrixApiRequest = new DistanceMatrixApiRequest(geoApiContext)
                    .origins(originLatLng)
                    .destinations(placeRepository.getGoogleLatLngs().toArray(new com.google.maps.model.LatLng[0]))
                    .mode(travelMode);

            distanceMatrixApiRequest.setCallback(new PendingResult.Callback<>() {
                @Override
                public void onResult(DistanceMatrix result) {

                    Log.d(TAG, "invoke: Result: " + result.toString());

                    taskFinished[0]++; // On success, add into taskFinished

                    int progress = (taskFinished[0] * (continueOptimization ? 99 : 100)) / taskQueueTotal;

                    // To use the progressIndicator in activity
                    EventBus.getDefault().postSticky(
                            new OnProgressIndicatorUpdate(progress)
                    );

                    /*
                        Information
                        Rows size is the number of origin,
                        elements size is equal to the number of destinations
                        requested to the API
                     */
                    for (DistanceMatrixRow r : result.rows) {

                        Log.d(TAG, "invoke: rowElements: " + r.elements.length);

                        List<Double> distanceValues = new ArrayList<>();

                        /*
                            Information about the following loop (r.elements) and the next loop (distancesWithOrigin)
                            CANNOT be combined, the 2nd loop will always have the size of
                            first loop MINUS 1. It won't match and will produce weird behavior.
                         */
                        for (DistanceMatrixElement e : r.elements) {

                            Log.d(TAG, "invoke: Element: status: " + e.status.name());

                            Log.e(TAG, " invoke: Element: duration: " + e.duration);

                            Log.e(TAG, " invoke: Element: distance: " + e.distance);

                            Log.e(TAG, " invoke: Element: fare: " + e.fare);

                            // Get distance value as double
                            double distance = e.distance.inMeters;

                            if (distance > 0) // Skip zero matrixElements
                                distanceValues.add(distance);

                        }

                        Log.d(TAG, "invoke: distanceValues: " + distanceValues);

                        // Assign listed response matrixElements into existing distance objects
                        List<MatrixElement> distancesWithOrigin = distanceRepository.filterByOrigin(p.getDsmPlace());

                        for (int j = 0; j < distancesWithOrigin.size(); j++) {
                            MatrixElement d = distancesWithOrigin.get(j);

                            d.setDistance(distanceValues.get(j));
                        }

                        Log.d(TAG, "invoke: distancesWithOrigin: " + distancesWithOrigin.size());

                    }

                    //
                    // OnFinished callback if condition met
                    if (progress == (continueOptimization ? 99 : 100)) {

                        Log.d(TAG, "invoke: Request finished");

                        taskFinished[0] = 0; // Reset the taskFinishedCount to 0

                        // OnFinished callback
                        EventBus.getDefault().post(
                                new OnDistanceMatrixResponse(matrixElements, continueOptimization)
                        );

                    }

                }

                @Override
                public void onFailure(Throwable e) {
                    Log.e(TAG, "Failed: " + e.getLocalizedMessage());
                }
            });
        }

    }

}
