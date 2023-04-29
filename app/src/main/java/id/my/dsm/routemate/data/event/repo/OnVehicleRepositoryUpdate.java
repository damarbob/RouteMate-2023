package id.my.dsm.routemate.data.event.repo;

import androidx.annotation.NonNull;

import id.my.dsm.routemate.library.dsmlib.model.Vehicle;

public final class OnVehicleRepositoryUpdate extends OnRepositoryUpdate<Vehicle> {

    public enum VehicleEvent {
        ACTION_SET_DEFAULT,
        ACTION_CLEAR_DEFAULT
    }

    private Vehicle vehicle;
    private VehicleEvent vehicleEvent;

    public OnVehicleRepositoryUpdate(@NonNull Event event) {
        super(event);
    }

    public OnVehicleRepositoryUpdate(@NonNull Event event, Vehicle vehicle) {
        super(event);

        this.vehicle = vehicle;
    }

    public OnVehicleRepositoryUpdate(@NonNull Event event, Vehicle vehicle, VehicleEvent vehicleEvent) {
        super(event);

        this.vehicle = vehicle;
        this.vehicleEvent = vehicleEvent;
    }

    @NonNull
    @Override
    public Event getStatus() {
        return super.getStatus();
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public VehicleEvent getVehicleEvent() {
        return vehicleEvent;
    }
}
