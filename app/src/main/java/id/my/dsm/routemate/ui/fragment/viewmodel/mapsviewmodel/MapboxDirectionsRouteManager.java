package id.my.dsm.routemate.ui.fragment.viewmodel.mapsviewmodel;

import static com.mapbox.core.constants.Constants.PRECISION_6;

import android.graphics.Color;
import android.util.Log;

import androidx.annotation.NonNull;

import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import id.my.dsm.routemate.data.event.repo.OnRepositoryUpdate;
import id.my.dsm.routemate.data.model.maps.MapboxDirectionsRoute;
import id.my.dsm.routemate.data.repo.vehicle.VehicleRepositoryN;
import id.my.dsm.routemate.library.dsmlib.model.MatrixElement;
import id.my.dsm.routemate.library.dsmlib.model.Vehicle;
import id.my.dsm.routemate.ui.fragment.viewmodel.MapsViewModel;
import id.my.dsm.routemate.usecase.repository.AlterRepositoryUseCase;

public class MapboxDirectionsRouteManager {

    private static final String LAYER_BELOW_ID_TRAFFIC = "road-label-street";
    private static final String LAYER_BELOW_ID = "road-label";

    private static final String TAG = "MVMRouteManager";

    MapsViewModel model;

    Style mapboxMapStyle;

    @Inject
    VehicleRepositoryN vehicleRepository;

    @Inject
    AlterRepositoryUseCase alterRepositoryUseCase;

    @Inject
    public MapboxDirectionsRouteManager() {
//        this.alterRepositoryUseCase = alterRepositoryUseCase;
    }

    public void provideViewModel(MapsViewModel model) {
        this.model = model;
    }

    public void provideMapboxMapStyle(Style mapboxMapStyle) {
        this.mapboxMapStyle = mapboxMapStyle;
    }

    public void createMapboxDirectionsRoute(MapboxDirectionsRoute mapboxDirectionsRoute) {
        alterRepositoryUseCase.invoke(OnRepositoryUpdate.Event.ACTION_CREATE, mapboxDirectionsRoute, false);
    }

    /**
     * Draw directionsRouteLine from a given {@link MatrixElement} mapboxDirectionsRoute
     * @param mapboxDirectionsRoute a {@link MatrixElement} instance of a mapboxDirectionsRoute
     */
    public void drawMapboxDirectionsRouteLine(@NonNull MapboxDirectionsRoute mapboxDirectionsRoute) {

        List<String> vehicleIds = mapboxDirectionsRoute.getVehicleIds();
        /*
            Set color to default if no vehicleIds found in the mapboxDirectionsRoute instance.
            Otherwise, use the last vehicle color in the mapboxDirectionsRoute.
         */
        assert vehicleIds != null;
        Vehicle vehicle = vehicleRepository.getVehicleById(vehicleIds.get(vehicleIds.size() - 1));
        assert vehicle != null; // Vehicle error if null

        int vehicleColor = vehicleIds.size() == 0 ? Vehicle.COLOR_DEFAULT : vehicle.getColor();

        drawDirectionsRouteLine(mapboxDirectionsRoute, mapboxDirectionsRoute.getId(), vehicleColor);

    }

    // TODO: Add doc comments
    // For multiple directionsRouteLines
    public void drawMapboxDirectionsRouteLines(@NonNull List<MapboxDirectionsRoute> mapboxDirectionsRoutes) {

        if (mapboxDirectionsRoutes.size() == 0)
            Log.e(TAG, "drawMapboxDirectionsRouteLines: No directions route!");

        for (MapboxDirectionsRoute m : mapboxDirectionsRoutes)
            drawMapboxDirectionsRouteLine(m);
    }

    // Draw directionsRouteLines from selected vehicle only
    public void drawMapboxDirectionsRouteLines(@NonNull List<MapboxDirectionsRoute> mapboxDirectionsRoutes, Vehicle vehicle) {
        for (MapboxDirectionsRoute m : mapboxDirectionsRoutes)
            if (m.getVehicleIds().contains(vehicle.getId()))
                drawDirectionsRouteLine(m, m.getId(), vehicle.getColor());
    }

    public void clearMapboxDirectionsRouteLines(@NonNull List<MapboxDirectionsRoute> mapboxDirectionsRoutes) {
        for (MapboxDirectionsRoute m : mapboxDirectionsRoutes) {
            mapboxMapStyle.removeLayer(m.getLineLayerId());
            mapboxMapStyle.removeSource(m.getStyleSourceId());
        }
    }

    /**
     * Draw directionsRouteLine from the given {@link MapboxDirectionsRoute} instance
     * Only support geometry with precision of 6 decimal places
     *
     * @param mapboxDirectionsRoute The route to be drawn in the map's LineLayer that was set up above.
     */
    public void drawDirectionsRouteLine(@NonNull MapboxDirectionsRoute mapboxDirectionsRoute, String id, int color) {

        String layerId = "DIRECTIONS_LAYER_" + id;
        String sourceId = "SOURCE_ID_" + id;

        Log.d(TAG, "drawDirectionsRouteLine: LayerID:" + layerId);
        Log.d(TAG, "drawDirectionsRouteLine: SourceID:" + sourceId);

        mapboxMapStyle.addSource(new GeoJsonSource(sourceId)); // Add source

        // Set up line properties
        LineLayer lineLayer = new LineLayer(
                layerId, sourceId).withProperties(
                PropertyFactory.lineWidth(4.5f),
                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                PropertyFactory.lineColor(Color.GRAY)
        );

        mapboxMapStyle.addLayerBelow(lineLayer, model.isTrafficStyleValue() ? LAYER_BELOW_ID_TRAFFIC : LAYER_BELOW_ID); // Add line layer below road label

        // TODO
        lineLayer = (LineLayer) mapboxMapStyle.getLayer("DIRECTIONS_LAYER_" + id);
        lineLayer.setProperties(PropertyFactory.lineColor(color));

        LineString lineString = LineString.fromPolyline(mapboxDirectionsRoute.getGeometry(), PRECISION_6);
        List<Point> coordinates = lineString.coordinates();

        List<Feature> directionsRouteFeatureList = new ArrayList<>();
        for (int i = 0; i < coordinates.size(); i++) {
            directionsRouteFeatureList.add(Feature.fromGeometry(LineString.fromLngLats(coordinates)));
        }

        FeatureCollection lineDirectionsFeatureCollection = FeatureCollection.fromFeatures(directionsRouteFeatureList);
        GeoJsonSource source = mapboxMapStyle.getSourceAs(sourceId);

        if (source != null) {
            source.setGeoJson(lineDirectionsFeatureCollection);
        }

        // Update layer id & source id into mapboxDirectionsRoute to remember
        mapboxDirectionsRoute.setLineLayerId(layerId);
        mapboxDirectionsRoute.setStyleSourceId(sourceId);

    }

}
