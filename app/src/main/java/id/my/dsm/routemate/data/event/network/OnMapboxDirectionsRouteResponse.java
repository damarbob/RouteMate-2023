package id.my.dsm.routemate.data.event.network;

import androidx.annotation.NonNull;

import id.my.dsm.routemate.data.model.maps.MapboxDirectionsRoute;
import id.my.dsm.vrpsolver.model.Solution;

public final class OnMapboxDirectionsRouteResponse extends OnResponse<MapboxDirectionsRoute> {

    private Solution solution;

    public OnMapboxDirectionsRouteResponse(@NonNull MapboxDirectionsRoute response, @NonNull Solution solution) {
        super(response);
        this.solution = solution;
    }

    public OnMapboxDirectionsRouteResponse(@NonNull MapboxDirectionsRoute response) {
        super(response);
    }

    public Solution getSolution() {
        return solution;
    }

}
