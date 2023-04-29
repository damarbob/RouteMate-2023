package id.my.dsm.routemate.data.event.network;

import androidx.annotation.NonNull;

import id.my.dsm.routemate.data.model.maps.MapboxDirectionsRoute;

public abstract class OnResponse<T> {

    private final T response;

    public OnResponse(@NonNull T response) {
        this.response = response;
    }

    @NonNull
    public T getResponse() {
        return response;
    }

}
