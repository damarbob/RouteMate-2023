package id.my.dsm.routemate.data.repo.distance;

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
import id.my.dsm.routemate.data.event.repo.OnSolutionRepositoryUpdate;
import id.my.dsm.routemate.data.repo.Repository;
import id.my.dsm.routemate.library.dsmlib.enums.OptimizationMethod;
import id.my.dsm.routemate.library.dsmlib.model.Solution;

@Module
@InstallIn(SingletonComponent.class)
public class SolutionRepositoryN extends Repository<Solution> {

    private static final String TAG = "SolutionRepository";

    /*

        IMPORTANT! Will be saved into UserData within 'optimizationMethod' key and uploaded to server.

        This variable is also known as Solution's Optimization Method
        which is the OptimizationMethod used in the optimization TO OBTAIN the LAST solutions.

        So, we can track the OptimizationMethod used for the solutions.

        Alternatively, use this variable to enable/disable a feature
        based on the current solution's OptimizationMethod.

     */
    private OptimizationMethod optimizationMethod;
    private Boolean usingAdvancedAlgorithm;

    public SolutionRepositoryN() {
        super();
    }

    @Nullable
    public OptimizationMethod getOptimizationMethod() {
        return optimizationMethod;
    }

    public void setOptimizationMethod(OptimizationMethod optimizationMethod) {
        this.optimizationMethod = optimizationMethod;
    }

    public Boolean isUsingAdvancedAlgorithm() {
        return usingAdvancedAlgorithm;
    }

    public void setUsingAdvancedAlgorithm(Boolean usingAdvancedAlgorithm) {
        this.usingAdvancedAlgorithm = usingAdvancedAlgorithm;
    }


    @Override
    public void onStart() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void createRecord(Solution record) {

    }

    @Singleton
    @Provides
    public SolutionRepositoryN getInstance() {
        return new SolutionRepositoryN();
    }

    @Override
    public void onRecordAdded(Solution record) {

        /*
            ID assignment happens in onRecordAdded because solutions are never created using createRecords
            but directly set into repository as a List
         */

//        Place origin = record.getOrigin();
//        Place destination = record.getDestination();

        record.setIndex(getLastRecordIndex());
//        String uUid = "SOLUTION_" + origin.getIndex() + destination.getIndex() + "_" + UUID.randomUUID().toString();  // Create unique ID
        String uUid = "SOLUTION_" + UUID.randomUUID().toString();  // Create unique ID
        record.setId(uUid);

        EventBus.getDefault().post(
                new OnSolutionRepositoryUpdate(
                        OnRepositoryUpdate.Event.RECORD_ADDED,
                        record
                )
        );
    }

    @Override
    public void onRecordDeleted(Solution record) {
        EventBus.getDefault().post(
                new OnSolutionRepositoryUpdate(
                        OnRepositoryUpdate.Event.RECORD_DELETED,
                        record
                )
        );
    }

    @Override
    public void onRecordCleared() {
        EventBus.getDefault().post(
                new OnSolutionRepositoryUpdate(
                        OnRepositoryUpdate.Event.RECORDS_CLEARED
                )
        );
    }

    /**
     * Filter List of solution MatrixElement by vehicleId
     * @param vehicleId String of vehicleId
     * @return List of solution Distances filtered
     */
    public List<Solution> filterByVehicleId(String vehicleId) {
        return Solution.Toolbox.filterByVehicleId(getRecords(), vehicleId);
    }

    public List<String> getOnlineVehiclesId() {
        List<String> onlineVehiclesId = new ArrayList<>();

        for (Solution s : getRecords())
            if (!onlineVehiclesId.contains(s.getVehicleId()))
                onlineVehiclesId.add(s.getVehicleId());

        return onlineVehiclesId;
    }

    /**
     * Filter List of solution MatrixElement by tripIndex
     * @param tripIndex int of tripIndex
     * @return List of solution MatrixElement filtered
     */
    public List<Solution> filterByTripIndex(int tripIndex) {
        return Solution.Toolbox.filterByTripIndex(getRecords(), tripIndex);
    }

    /**
     * Auto-assign route index to a List of solution MatrixElement
     */
    public void assignRouteIndex() {
        Solution.Toolbox.assignTripIndex(getRecords());
    }

}
