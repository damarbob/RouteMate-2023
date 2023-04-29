package id.my.dsm.routemate.data.event.repo;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import id.my.dsm.routemate.data.model.maps.MapboxDirectionsRoute;

public class OnMapboxDirectionsRouteRepositoryUpdate extends OnRepositoryUpdate<MapboxDirectionsRoute> {

    private MapboxDirectionsRoute mapboxDirectionsRoute;
    private ArrayList<MapboxDirectionsRoute> mapboxDirectionsRoutes;

    public OnMapboxDirectionsRouteRepositoryUpdate(@NonNull Event event) {
        super(event);
    }

    public OnMapboxDirectionsRouteRepositoryUpdate(@NonNull Event event, MapboxDirectionsRoute mapboxDirectionsRoute) {
        super(event);
        this.mapboxDirectionsRoute = mapboxDirectionsRoute;
    }

    public OnMapboxDirectionsRouteRepositoryUpdate(@NonNull Event event, ArrayList<MapboxDirectionsRoute> mapboxDirectionsRoutes) {
        super(event);
        this.mapboxDirectionsRoutes = mapboxDirectionsRoutes;
    }

    public MapboxDirectionsRoute getMapboxDirectionsRoute() {
        return mapboxDirectionsRoute;
    }

    public void setMapboxDirectionsRoute(MapboxDirectionsRoute mapboxDirectionsRoute) {
        this.mapboxDirectionsRoute = mapboxDirectionsRoute;
    }

    public ArrayList<MapboxDirectionsRoute> getMapboxDirectionsRoutes() {
        return mapboxDirectionsRoutes;
    }

    public void setMapboxDirectionsRoutes(ArrayList<MapboxDirectionsRoute> mapboxDirectionsRoutes) {
        this.mapboxDirectionsRoutes = mapboxDirectionsRoutes;
    }

}
