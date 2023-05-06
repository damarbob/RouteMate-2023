package id.my.dsm.routemate.usecase.place;

import android.util.Log;

import androidx.annotation.NonNull;

import com.mapbox.api.geocoding.v5.models.CarmenContext;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import id.my.dsm.routemate.data.event.network.OnMapboxGeocodingResponse;
import id.my.dsm.routemate.data.model.place.Place;
import id.my.dsm.routemate.data.source.mapbox.MapboxDataSource;
import id.my.dsm.vrpsolver.model.LatLngAlt;
import id.my.dsm.vrpsolver.model.Location;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestMapboxGeocodingUseCase {

    private static final String TAG = RequestMapboxGeocodingUseCase.class.getSimpleName();

    @Inject
    public RequestMapboxGeocodingUseCase() {
    }

    public void invoke(String query) {

        MapboxDataSource.getGeocoding(query, 2, new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<GeocodingResponse> call, @NonNull Response<GeocodingResponse> response) {

                List<CarmenFeature> results = response.body().features();

                if (results.size() > 0) {

                    List<Place> resultPlaces = new ArrayList<>();

                    for (CarmenFeature c : results) {

                        Point point = c.center();
                        LatLng latLng = new LatLng(point.latitude(), point.longitude());

                        StringBuilder context = new StringBuilder();
                        List<CarmenContext> carmenContexts = c.context();

                        /*
                            Some places don't have carmenContexts like countries.
                            Tested for Indonesia.
                         */
                        if (carmenContexts != null)
                            for (int i = 0; i < carmenContexts.size(); i++) {
                                CarmenContext cc = carmenContexts.get(i);
                                context.append(cc.text());

                                if (i < carmenContexts.size())
                                    context.append(", ");
                            }

                        Location location = new Location(
                                new LatLngAlt(latLng.getLatitude(), latLng.getLongitude()),
                                Location.Profile.DESTINATION
                        );
                        Place place = new Place.Builder(location)
                                .withName(c.text())
                                .withDescription(c.placeName())
                                .withNote(context.toString())
                                .build();

                        resultPlaces.add(place);

                        Log.d(TAG, "mapboxGeocoding: onResponse: \ntext: " + c.text()
                                + " | \nmatchingText: " + c.matchingText()
                                + " | \nplaceName: " + c.placeName()
                                + " | \naddress: " + c.address()
                                + " | \nid: " + c.id()
                                + " | \nlanguage: " + c.language()
                                + " | \nrelevance: " + c.relevance()
                                + " | \nproperties-landmark: " + c.properties().get("landmark")
                                + " | \nproperties-category: " + c.properties().get("category")
                                + " | \ncontext: " + context.toString()
                        );

                    }

                    EventBus.getDefault().post(
                            new OnMapboxGeocodingResponse(
                                    OnMapboxGeocodingResponse.Status.SUCCESS,
                                    OnMapboxGeocodingResponse.Type.FORWARD,
                                    resultPlaces
                            )
                    );

                } else {

                    EventBus.getDefault().post(
                            new OnMapboxGeocodingResponse(
                                    OnMapboxGeocodingResponse.Status.SUCCESS,
                                    OnMapboxGeocodingResponse.Type.FORWARD,
                                    "No result found"
                            )
                    );

                    // No result for your request were found.
                    Log.d(TAG, "mapboxGeocoding: onResponse: No result found");

                }
            }

            @Override
            public void onFailure(@NonNull Call<GeocodingResponse> call, @NonNull Throwable throwable) {

                EventBus.getDefault().post(
                        new OnMapboxGeocodingResponse(
                                OnMapboxGeocodingResponse.Status.FAILED,
                                OnMapboxGeocodingResponse.Type.FORWARD,
                                throwable.getMessage()
                        )
                );

                throwable.printStackTrace();
            }
        });

    }

}
