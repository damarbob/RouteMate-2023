package id.my.dsm.routemate.data.event.repo;

import androidx.annotation.NonNull;

import id.my.dsm.routemate.data.model.place.Place;

public final class OnPlaceRepositoryUpdate extends OnRepositoryUpdate<Place> {

    private Place place;

    public OnPlaceRepositoryUpdate(@NonNull Event event) {
        super(event);
    }

    public OnPlaceRepositoryUpdate(@NonNull Event event, Place place) {
        super(event);
        this.place = place;
    }

    @NonNull
    @Override
    public Event getStatus() {
        return super.getStatus();
    }

    public Place getObjective() {
        return place;
    }
}
