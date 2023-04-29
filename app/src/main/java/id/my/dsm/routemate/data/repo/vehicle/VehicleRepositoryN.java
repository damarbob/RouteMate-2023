package id.my.dsm.routemate.data.repo.vehicle;

import androidx.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import id.my.dsm.routemate.data.event.repo.OnRepositoryUpdate;
import id.my.dsm.routemate.data.event.repo.OnVehicleRepositoryUpdate;
import id.my.dsm.routemate.data.repo.Repository;
import id.my.dsm.routemate.data.repo.distance.SolutionRepositoryN;
import id.my.dsm.routemate.library.dsmlib.model.Vehicle;
import id.my.dsm.routemate.ui.model.MaterialManager;

@Module
@InstallIn(SingletonComponent.class)
public class VehicleRepositoryN extends Repository<Vehicle> {

    private final int[] COLOR = MaterialManager.COLOR;

    public VehicleRepositoryN() {
        super();
        createRecord(new Vehicle("Vehicle", Vehicle.MapboxProfile.DRIVING_TRAFFIC, 1));
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onDestroy() {
    }

    /**
     * Create a vehicle with complete ready-to-use predefined template data before adding to repository.
     * Hence, prevents duplicate indexes & ids.
     * @param record {@link Vehicle} instance
     */
    @Override
    public void createRecord(Vehicle record) {
        String uUid = UUID.randomUUID().toString();  // Create unique ID
        record.setId(uUid); // Assign uid

        record.setDefault(getRecordsCount() == 0); // Set default for first vehicle added

        record.setName(record.getName() + " (" + getLastRecordIndex() + ")");
        record.setColor(COLOR[getRecordsCount() % COLOR.length]);

        addRecord(record);
    }

    @Singleton
    @Provides
    public VehicleRepositoryN getInstance() {
        return new VehicleRepositoryN();
    }

    @Override
    public void onRecordAdded(Vehicle record) {
        EventBus.getDefault().post(
                new OnVehicleRepositoryUpdate(
                        OnRepositoryUpdate.Event.RECORD_ADDED,
                        record
                )
        );
    }

    // Custom clearRecord, ALWAYS USE THIS
    public void clearRecord(SolutionRepositoryN solutionRepository) {

        // Maintain online & default vehicles and clear others
        List<String> onlineVehicleIds = solutionRepository.getOnlineVehiclesId();

        List<Vehicle> onlineVehicles = new ArrayList<>(); // Keep online vehicles in memory

        for (Vehicle v : getRecords())
            if (onlineVehicleIds.contains(v.getId()))
                onlineVehicles.add(v);

        Vehicle defaultVehicle = getDefaultVehicle(); // Keep default vehicle in memory

        clearRecord();

        if (!onlineVehicles.contains(defaultVehicle)) // If default vehicle is NOT in online vehicles
            addRecord(defaultVehicle);

        for (Vehicle v : onlineVehicles)
            addRecord(v);

    }

    @Override
    public void onRecordDeleted(Vehicle record) {
        EventBus.getDefault().post(
                new OnVehicleRepositoryUpdate(
                        OnRepositoryUpdate.Event.RECORD_DELETED,
                        record
                )
        );
    }

    @Override
    public void onRecordCleared() {
        EventBus.getDefault().post(
                new OnVehicleRepositoryUpdate(
                        OnRepositoryUpdate.Event.RECORDS_CLEARED
                )
        );
    }

    /**
     * Get a vehicle by its vehicle index
     * @param vehicleId int of vehicle index
     * @return a {@link Vehicle} instance
     */
    @Nullable
    public Vehicle getVehicleById(@NonNull String vehicleId) {
        return Vehicle.Toolbox.getVehicleById(getRecords(), vehicleId);
    }

    /**
     * Get the default vehicle
     * @return a default Vehicle
     */
    @Nullable
    public Vehicle getDefaultVehicle() {
        return Vehicle.Toolbox.getDefaultVehicle(getRecords());
    }

    /**
     * Set the default vehicle and clear the old
     * @param record Vehicle to be default
     */
    public void setDefaultVehicle(Vehicle record) {
        Vehicle.Toolbox.setDefaultVehicle(getRecords(), record);
    }

    /**
     * Clear the default TODO Future: Deprecated, remove
     * @param record default Vehicle
     */
    public void clearDefaultVehicle(Vehicle record) {
        Vehicle.Toolbox.clearDefaultVehicle(record);
    }

}
