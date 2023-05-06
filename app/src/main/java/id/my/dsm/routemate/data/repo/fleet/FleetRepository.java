package id.my.dsm.routemate.data.repo.fleet;

import androidx.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import id.my.dsm.routemate.data.event.repo.OnRepositoryUpdate;
import id.my.dsm.routemate.data.event.repo.OnVehicleRepositoryUpdate;
import id.my.dsm.routemate.data.model.fleet.Fleet;
import id.my.dsm.routemate.data.repo.Repository;
import id.my.dsm.routemate.data.repo.distance.SolutionRepositoryN;
import id.my.dsm.routemate.ui.model.MaterialManager;
import id.my.dsm.vrpsolver.model.Vehicle;

@Module
@InstallIn(SingletonComponent.class)
public class FleetRepository extends Repository<Fleet> {

    private final int[] COLOR = MaterialManager.COLOR;

    public FleetRepository() {
        super();
        createRecord(
                new Fleet(
                        "Vehicle",
                        new Vehicle.Builder()
                                .build()
                )
        );
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
    public void createRecord(Fleet record) {
//        String uUid = UUID.randomUUID().toString();  // Create unique ID
//        record.setId(uUid); // Assign uid

        record.getVehicle().setDefault(getRecordsCount() == 0); // Set default for first vehicle added

        record.setName(record.getName() + " (" + getLastRecordIndex() + ")");
        record.setColor(COLOR[getRecordsCount() % COLOR.length]);

        addRecord(record);
    }

    @Singleton
    @Provides
    public FleetRepository getInstance() {
        return new FleetRepository();
    }

    @Override
    public void onRecordAdded(Fleet record) {
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

        List<Fleet> onlineVehicles = new ArrayList<>(); // Keep online vehicles in memory

        for (Fleet v : getRecords())
            if (onlineVehicleIds.contains(v.getId()))
                onlineVehicles.add(v);

        Fleet defaultVehicle = getDefaultVehicle(); // Keep default vehicle in memory

        clearRecord();

        if (!onlineVehicles.contains(defaultVehicle)) // If default vehicle is NOT in online vehicles
            addRecord(defaultVehicle);

        for (Fleet v : onlineVehicles)
            addRecord(v);

    }

    @Override
    public void onRecordDeleted(Fleet record) {
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

    public List<Vehicle> getVehicles() {
        List<Vehicle> vehicles = new ArrayList<>();
        for (Fleet f : getRecords())
            vehicles.add(f.getVehicle());
        return vehicles;
    }

    /**
     * Get a vehicle by its vehicle index
     * @param vehicleId int of vehicle index
     * @return a {@link Vehicle} instance
     */
    @Nullable
    public Fleet getFleetById(@NonNull String vehicleId) {
        return Fleet.Toolbox.getFleetById(getRecords(), vehicleId);
    }

    /**
     * Get the default vehicle
     * @return a default Vehicle
     */
    @Nullable
    public Fleet getDefaultVehicle() {
        Vehicle defaultVehicle = Vehicle.Toolbox.getDefaultVehicle(getVehicles());
        if (defaultVehicle == null)
            return null;
        return getFleetById(defaultVehicle.getId());
    }

    /**
     * Set the default vehicle and clear the old
     * @param record Vehicle to be default
     */
    public void switchDefaultVehicle(Fleet record) {
        Vehicle.Toolbox.switchDefaultVehicle(getVehicles(), record.getVehicle());
    }

}
