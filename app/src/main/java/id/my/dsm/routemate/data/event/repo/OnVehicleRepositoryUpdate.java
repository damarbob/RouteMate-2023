package id.my.dsm.routemate.data.event.repo;

import androidx.annotation.NonNull;

import id.my.dsm.routemate.data.model.fleet.Fleet;

public final class OnVehicleRepositoryUpdate extends OnRepositoryUpdate<Fleet> {

    public enum VehicleEvent {
        ACTION_SET_DEFAULT,
        ACTION_CLEAR_DEFAULT
    }

    private Fleet vehicle;
    private VehicleEvent vehicleEvent;

    public OnVehicleRepositoryUpdate(@NonNull Event event) {
        super(event);
    }

    public OnVehicleRepositoryUpdate(@NonNull Event event, Fleet vehicle) {
        super(event);

        this.vehicle = vehicle;
    }

    public OnVehicleRepositoryUpdate(@NonNull Event event, Fleet vehicle, VehicleEvent vehicleEvent) {
        super(event);

        this.vehicle = vehicle;
        this.vehicleEvent = vehicleEvent;
    }

    @NonNull
    @Override
    public Event getStatus() {
        return super.getStatus();
    }

    public Fleet getVehicle() {
        return vehicle;
    }

    public VehicleEvent getVehicleEvent() {
        return vehicleEvent;
    }
}
