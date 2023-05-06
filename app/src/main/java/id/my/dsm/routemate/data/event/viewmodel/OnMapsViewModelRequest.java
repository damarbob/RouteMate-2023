package id.my.dsm.routemate.data.event.viewmodel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import id.my.dsm.routemate.data.model.fleet.Fleet;
import id.my.dsm.routemate.data.model.maps.MapboxDirectionsRoute;
import id.my.dsm.routemate.data.model.place.Place;
import id.my.dsm.vrpsolver.model.MatrixElement;

/**
 * Bridge event dispatched into {@link id.my.dsm.routemate.ui.fragment.viewmodel.MapsViewModel} as request.
 */
public final class OnMapsViewModelRequest {

    public enum Event {
        ACTION_CENTER_CAMERA,
        ACTION_CLEAR_ROUTE_LINE, // done
        ACTION_RELOAD_MAP,
        ACTION_DRAW_ROUTE_LINE, // done
        ACTION_DRAW_VEHICLE_ROUTE_LINE, // done
        ACTION_RELOAD_PLACES_SYMBOL,
        ACTION_RELOAD_MARKER_VIEWS,
    }

    private final Event event;
    private final Place place;
    private final List<MatrixElement> matrix;
    private final Fleet fleet;
    private final List<MapboxDirectionsRoute> mapboxDirectionsRoutes;

    private OnMapsViewModelRequest(@NonNull Builder builder) {
        this.event = builder.event;
        this.place = builder.place;
        this.matrix = builder.matrix;
        this.fleet = builder.fleet;
        this.mapboxDirectionsRoutes = builder.mapboxDirectionsRoutes;
    }

    @NonNull
    public Event getEvent() {
        return event;
    }

    @Nullable
    public Place getObjective() {
        return place;
    }

    @Nullable
    public List<MatrixElement> getMatrix() {
        return matrix;
    }

    @Nullable
    public Fleet getFleet() {
        return fleet;
    }

    @Nullable
    public List<MapboxDirectionsRoute> getMapboxDirectionsRoutes() {
        return mapboxDirectionsRoutes;
    }

    public static class Builder {
        private final Event event;
        private Place place;
        private List<MatrixElement> matrix;
        private Fleet fleet;
        private List<MapboxDirectionsRoute> mapboxDirectionsRoutes;

        public Builder(@NonNull Event event) {
            this.event = event;
        }

        public Builder withObjective(@NonNull Place place) {
            this.place = place;
            return this;
        }

        public Builder withMatrix(@NonNull List<MatrixElement> matrix) {
            this.matrix = matrix;
            return this;
        }

        public Builder withVehicle(@NonNull Fleet fleet) {
            this.fleet = fleet;
            return this;
        }

        public Builder withMapboxDirectionsRoutes(@NonNull List<MapboxDirectionsRoute> mapboxDirectionsRoutes) {
            this.mapboxDirectionsRoutes = mapboxDirectionsRoutes;
            return this;
        }

        public OnMapsViewModelRequest build() {
            return new OnMapsViewModelRequest(this);
        }

    }
}
