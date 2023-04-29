package id.my.dsm.routemate.data.source.mapbox;

import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.geocoding.v5.MapboxGeocoding;
import com.mapbox.api.geocoding.v5.models.GeocodingResponse;
import com.mapbox.api.optimization.v1.MapboxOptimization;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;

import javax.inject.Inject;

import id.my.dsm.routemate.BuildConfig;
import id.my.dsm.routemate.R;
import retrofit2.Callback;

public class MapboxDataSource {

    private static final String ACCESS_TOKEN = BuildConfig.MAPBOX_ACCESS_TOKEN;

    @Inject
    public MapboxDataSource() {
    }

    /**
     * Make a request to the Mapbox Directions API. Once successful, pass the route to the
     * route layer.
     *
     * @param origin the starting point of the route
     * @param destination the desired finish point of the route
     * @param mapboxProfile
     */
    public static void getRoute(final LatLng origin, final LatLng destination, String mapboxProfile, Callback<DirectionsResponse> listener) {
        MapboxDirections client = MapboxDirections.builder()
                .origin(Point.fromLngLat(origin.getLongitude(), origin.getLatitude()))
                .destination(Point.fromLngLat(destination.getLongitude(), destination.getLatitude()))
                .overview(DirectionsCriteria.OVERVIEW_SIMPLIFIED)
                .profile(mapboxProfile)
                .accessToken(ACCESS_TOKEN)
                .build();

        client.enqueueCall(listener);
    }

    public static void getGeocoding(String query, int limit, Callback<GeocodingResponse> listener) {
        MapboxGeocoding mapboxGeocoding = MapboxGeocoding.builder()
                .accessToken(ACCESS_TOKEN)
                .limit(limit)
                .query(query)
                .build();

        mapboxGeocoding.enqueueCall(listener);
    }

}
