package id.my.dsm.routemate.usecase.userdata;

import android.app.Activity;
import android.util.Log;

import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;

import id.my.dsm.routemate.data.model.user.DSMUser;
import id.my.dsm.routemate.data.model.userdata.UserData;
import id.my.dsm.routemate.data.repo.distance.DistanceRepositoryN;
import id.my.dsm.routemate.data.repo.distance.SolutionRepositoryN;
import id.my.dsm.routemate.data.repo.mapbox.MapboxDirectionsRouteRepository;
import id.my.dsm.routemate.data.repo.place.PlaceRepositoryN;
import id.my.dsm.routemate.data.repo.user.UserRepository;
import id.my.dsm.routemate.data.repo.vehicle.VehicleRepositoryN;
import id.my.dsm.routemate.library.dsmlib.enums.OptimizationMethod;
import id.my.dsm.routemate.ui.model.RouteMatePref;

public class UploadUserDataUseCase {

    private static final String TAG = UploadUserDataUseCase.class.getSimpleName();

    private final UserRepository userRepository;
    private final PlaceRepositoryN placeRepository;
    private final VehicleRepositoryN vehicleRepository;
    private final DistanceRepositoryN distanceRepository;
    private final SolutionRepositoryN solutionRepository;
    private final MapboxDirectionsRouteRepository mapboxDirectionsRouteRepository;

    @Inject
    public UploadUserDataUseCase(UserRepository userRepository, PlaceRepositoryN placeRepository, VehicleRepositoryN vehicleRepository, DistanceRepositoryN distanceRepository, SolutionRepositoryN solutionRepository, MapboxDirectionsRouteRepository mapboxDirectionsRouteRepository) {
        this.userRepository = userRepository;
        this.placeRepository = placeRepository;
        this.vehicleRepository = vehicleRepository;
        this.distanceRepository = distanceRepository;
        this.solutionRepository = solutionRepository;
        this.mapboxDirectionsRouteRepository = mapboxDirectionsRouteRepository;
    }

    /**
     * Upload user data to the cloud. Utilizes a {@link UserData} instance populated with latest records across all repositories.
     */
    public void invoke(boolean includePlaces, boolean includeVehicles, boolean includeMatrix, boolean includeSolution, boolean includeMapboxDirectionsRoutes) {
        FirebaseUser user = userRepository.getLiveUser().getValue();

        // Check for authorization
        if (user == null) {
            Log.e(TAG, "show: User unauthorized, sync cancelled");
//            new MaterialAlertDialogBuilder(context)
//                    .setTitle("Sync Failed")
//                    .setMessage("Please sign in to use sync feature!")
//                    .setPositiveButton("Ok", null)
//                    .setNeutralButton("Cancel", null)
//                    .show();
            return;
        }

        // Populate user data with latest records available
        UserData.Builder userData = new UserData.Builder(user.getUid());
        userData.withName(DSMUser.MY_ROUTE + user.getUid());

        if (includePlaces)
            userData.withPlaces(placeRepository.getRecords());
        if (includeVehicles)
            userData.withVehicles(vehicleRepository.getRecords());
        if (includeMatrix)
            userData.withMatrix(distanceRepository.getRecords());
        if (includeSolution)
            userData.withSolutions(solutionRepository.getRecords());
        if (includeMapboxDirectionsRoutes)
            userData.withMapboxDirectionsRoutes(mapboxDirectionsRouteRepository.getRecords());

        if (solutionRepository.getOptimizationMethod() != null)
            userData.withOptimizationMethod(solutionRepository.getOptimizationMethod()); // The optimization method used in the solution
        if (solutionRepository.isUsingAdvancedAlgorithm() != null)
            userData.withAdvancedAlgorithm(solutionRepository.isUsingAdvancedAlgorithm()); // Whether user chooses to use advanced algorithm

        userRepository.updateUserData(userData.build()); // Request user data update using above userData

    }



}
