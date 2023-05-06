package id.my.dsm.routemate.usecase.distance;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.matrix.v1.MapboxMatrix;
import com.mapbox.api.matrix.v1.models.MatrixResponse;
import com.mapbox.geojson.Point;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import id.my.dsm.routemate.BuildConfig;
import id.my.dsm.routemate.data.model.place.Place;
import retrofit2.Callback;

public class RequestMapboxMatrixUseCase {

    private static final String TAG = RequestMapboxMatrixUseCase.class.getName();

    @Inject
    public RequestMapboxMatrixUseCase() {
    }

    /**
     * Request distances to Mapbox API from a given arraylist of places into distances array.
     * WARNING: This might take A LOT of resources (like data and cpu) to work. Using async task is very recommended.
     *
     * @param places an {@link ArrayList} object {@link Place}
     * @param mapboxProfile String of mapbox profile
     */
    public void invoke(@NonNull List<Place> places, @NonNull String mapboxProfile, @Nullable Callback<MatrixResponse> matrixResponseCallback) {

        // Validate mapboxProfile
        if (mapboxProfile.equals("undefined")) {
            Log.e(TAG, "populateMapboxDistancesArray: Incorrect mapboxProfile! (mapboxProfile: undefined)");
            return;
        }

        // Populate points
        ArrayList<Point> points = new ArrayList<>();

        for (Place place : places) {
            points.add(Point.fromLngLat(place.getMapboxLatLng().getLongitude(), place.getMapboxLatLng().getLatitude()));
        }

        // Build Mapbox Matrix API parameters
        MapboxMatrix directionsMatrixClient = MapboxMatrix.builder()
                .accessToken(BuildConfig.MAPBOX_ACCESS_TOKEN)
                .profile(mapboxProfile)
                // Request both distances and durations using annotations
                .addAnnotations(DirectionsCriteria.ANNOTATION_DISTANCE, DirectionsCriteria.ANNOTATION_DURATION)
                .coordinates(points)
                .build();

        // Handle the API response
        directionsMatrixClient.enqueueCall(matrixResponseCallback);

    }

}
