package id.my.dsm.routemate.data.event.model;

import androidx.annotation.NonNull;

import id.my.dsm.routemate.data.model.place.Place;

public final class OnSelectedPlaceChangedEvent {

    private Place place;

    public OnSelectedPlaceChangedEvent(@NonNull Place place) {
        this.place = place;
    }

    @NonNull
    public Place getObjective() {
        return place;
    }

}
