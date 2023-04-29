package id.my.dsm.routemate.data.model.maps;

import androidx.room.Ignore;

import com.mapbox.api.directions.v5.models.DirectionsRoute;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapboxDirectionsRoute {

    private List<String> vehicleIds = new ArrayList<>();

    private List<Integer> tripIndexes = new ArrayList<>();

    private String id;

    private String geometry;
    private double distance;
    private double duration;
    private double durationTypical;

    private String styleSourceId;
    private String lineLayerId;

    public MapboxDirectionsRoute() {
    }

    public List<String> getVehicleIds() {
        return vehicleIds;
    }

    public void setVehicleIds(List<String> vehicleIds) {
        this.vehicleIds = vehicleIds;
    }

    public List<Integer> getTripIndexes() {
        return tripIndexes;
    }

    public void setTripIndexes(List<Integer> tripIndexes) {
        this.tripIndexes = tripIndexes;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGeometry() {
        return geometry;
    }

    public void setGeometry(String geometry) {
        this.geometry = geometry;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public double getDurationTypical() {
        return durationTypical;
    }

    public void setDurationTypical(double durationTypical) {
        this.durationTypical = durationTypical;
    }

    public String getStyleSourceId() {
        return styleSourceId;
    }

    public void setStyleSourceId(String styleSourceId) {
        this.styleSourceId = styleSourceId;
    }

    public String getLineLayerId() {
        return lineLayerId;
    }

    public void setLineLayerId(String lineLayerId) {
        this.lineLayerId = lineLayerId;
    }

    @Ignore
    public MapboxDirectionsRoute(String geometry, double distance, double duration, double durationTypical) {
        this.geometry = geometry;
        this.distance = distance;
        this.duration = duration;
        this.durationTypical = durationTypical;
    }

    public static MapboxDirectionsRoute fromDirectionsRoute(DirectionsRoute directionsRoute) {
        return new MapboxDirectionsRoute(
                directionsRoute.geometry(),
                directionsRoute.distance(),
                directionsRoute.duration(),
                directionsRoute.durationTypical() == null ? 0 : directionsRoute.durationTypical()
        );
    }
}
