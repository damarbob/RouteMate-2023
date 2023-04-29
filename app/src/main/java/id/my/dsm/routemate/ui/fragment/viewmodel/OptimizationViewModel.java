package id.my.dsm.routemate.ui.fragment.viewmodel;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.instacart.library.truetime.TrueTime;
import com.mapbox.api.directions.v5.DirectionsCriteria;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import id.my.dsm.routemate.R;
import id.my.dsm.routemate.data.event.model.OnUpdateUserSession;
import id.my.dsm.routemate.data.event.network.OnDistanceMatrixResponse;
import id.my.dsm.routemate.data.event.repo.OnRepositoryUpdate;
import id.my.dsm.routemate.data.event.view.OnProgressIndicatorUpdate;
import id.my.dsm.routemate.data.event.viewmodel.OnDistancesViewModelRequest;
import id.my.dsm.routemate.data.model.DSMTimestamp;
import id.my.dsm.routemate.data.model.maps.MapboxDirectionsRoute;
import id.my.dsm.routemate.data.model.session.UserSession;
import id.my.dsm.routemate.data.model.user.DSMUser;
import id.my.dsm.routemate.data.repo.distance.DistanceRepositoryN;
import id.my.dsm.routemate.data.repo.distance.SolutionRepositoryN;
import id.my.dsm.routemate.data.repo.mapbox.MapboxDirectionsRouteRepository;
import id.my.dsm.routemate.data.repo.place.PlaceRepositoryN;
import id.my.dsm.routemate.data.repo.user.SessionRepository;
import id.my.dsm.routemate.data.repo.user.UserRepository;
import id.my.dsm.routemate.data.repo.vehicle.VehicleRepositoryN;
import id.my.dsm.routemate.library.dsmlib.DSMSolver;
import id.my.dsm.routemate.library.dsmlib.enums.OptimizationMethod;
import id.my.dsm.routemate.data.event.network.OnOptimizationResponse;
import id.my.dsm.routemate.library.dsmlib.event.OptimizationResponseError;
import id.my.dsm.routemate.library.dsmlib.event.OptimizationResponseListener;
import id.my.dsm.routemate.library.dsmlib.model.Solution;
import id.my.dsm.routemate.library.dsmlib.model.Vehicle;
import id.my.dsm.routemate.ui.model.RouteMatePref;
import id.my.dsm.routemate.usecase.optimization.RequestMapboxOptimizationUseCase;
import id.my.dsm.routemate.usecase.repository.AlterRepositoryUseCase;
import id.my.dsm.routemate.usecase.solution.RequestMapboxDirectionsRouteUseCase;
import id.my.dsm.routemate.usecase.userdata.UploadUserDataUseCase;

@HiltViewModel
public class OptimizationViewModel extends ViewModel implements OptimizationResponseListener {

    private enum OptimizationRequirementCheckResult {
        WithMatrixCalculation,
        WithoutMatrixCalculation,
        ConfirmMatrixRecalculation
    }

    private static final String TAG = "OptimizationViewModel";

    public final static long MILLIS_PER_DAY = 24 * 60 * 60 * 1000L;

    // Dependencies
    @Inject
    DSMSolver dsmSolver;
    @Inject
    UserRepository userRepository;
    @Inject
    PlaceRepositoryN placeRepository;
    @Inject
    VehicleRepositoryN vehicleRepository;
    @Inject
    DistanceRepositoryN distanceRepository;
    @Inject
    SolutionRepositoryN solutionRepository;
    @Inject
    MapboxDirectionsRouteRepository mapboxDirectionsRouteRepository;
    @Inject
    SessionRepository sessionRepository;
    @SuppressLint("StaticFieldLeak")
    private Context context;

    // Use case
    @Inject
    AlterRepositoryUseCase alterRepositoryUseCase;
    @Inject
    UploadUserDataUseCase uploadUserDataUseCase;
    @Inject
    RequestMapboxOptimizationUseCase requestMapboxOptimizationUseCase;
    @Inject
    RequestMapboxDirectionsRouteUseCase requestMapboxDirectionsRouteUseCase;

    @Inject
    public OptimizationViewModel() {
        EventBus.getDefault().register(this);
        DSMSolver.setOnOptimizationResponseListener(this);
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        EventBus.getDefault().unregister(this);
    }

    // Dependency provider

    public void provideContext(Context context) {
        this.context = context;
    }

    // Refresh data

    /**
     * Update optimization quota to server if it's already 24 hours
     */
    public void updateQuota() {

        String uid = userRepository.getUser().getUid(); // Get the current logged user's uid
        UserSession userSession = sessionRepository.get(DSMUser.getOptimizationSession(uid)); // Get the user's optimization session
        if (userSession != null) {

            DSMUser dsmUser = userRepository.getDsmUser(); // Get the DSMUser
            assert dsmUser != null;

            Date lastSession = userSession.getLastOptimizationQuotaRefresh().getDate(); // Get last refresh date
            Date now = TrueTime.now(); // Real-time date

            // Next day will be the previous date if current month is later than last session's month
            // or will be the next date if current month is the same or earlier than last session's month
            boolean isNextDay = now.getMonth() > lastSession.getMonth() ? now.getDate() < lastSession.getDate() : now.getDate() > lastSession.getDate();
            Log.d(TAG, "updateQuota: currentDate: " + now.getDate() + " | lastDate: " + lastSession.getDate());
            Log.d(TAG, "updateQuota: isNextDay: " + isNextDay + " (now: " + now + " | last: " + lastSession + ")");

            // Do quota refresh if it's the next day and last quota is lower than default user's quota
            if (isNextDay && userSession.getLastRemainingOptimizationQuota() < dsmUser.getOptimizationQuota()) {

                userSession.setLastRemainingOptimizationQuota(dsmUser.getOptimizationQuota()); // Update the user's quota
                userSession.setLastOptimizationQuotaRefresh(new DSMTimestamp(now)); // Update the last date of quota refresh to NOW

                sessionRepository.update(userSession, OnUpdateUserSession.Action.NONE); // Update to repository (ONLINE)
                Log.d(TAG, "updateQuota: Quota refreshed!");

            }

        }

    }

    // Optimization

    /*
        Optimization lifecycle
        prepareOptimization → requestOptimization → startOptimization → requestComputation → DSMSolver
     */

    /**
     * Prepare optimization by collecting important data from server.
     */
    public void beginOptimization() {
        sessionRepository.retrieve(userRepository.getUser().getUid(), OnUpdateUserSession.Action.OPTIMIZATION);
    }

    /**
     * Do optimization requirement checking. Start optimization if requirements are fulfilled.
     */
    public void requestOptimization() {

        Boolean useAdvancedAlgorithm = RouteMatePref.readOptimizationIsAdvancedAlgorithm((Activity) context);
        OptimizationMethod optimizationMethod = RouteMatePref.readOptimizationMethod((Activity) context); // Read optimization method

        // Start of general optimization requirements checking
        // Any NON method-related requirements for optimization are checked here
        // Check the number of places. If under 2, cancel optimization
        if (placeRepository.getRecords().size() < 2) {
            new MaterialAlertDialogBuilder(context)
                    .setTitle("Unable to Optimize")
                    .setMessage("To optimize, you need at least 2 places with a source and a destination.")
                    .setPositiveButton("Ok", null)
                    .setOnDismissListener(dialogInterface -> {
                        // Cancel will finish the process and set the progressIndicator to 100
                        EventBus.getDefault().post(new OnProgressIndicatorUpdate(100));
                    })
                    .show();
            return;
        }
        else if (
                placeRepository.getRecordsCount() >= 12
                        && !useAdvancedAlgorithm
        ) {
            new MaterialAlertDialogBuilder(context)
                    .setTitle("Unable to Optimize")
                    .setMessage("Default optimization setting supports only up to 12 places.")
                    .setPositiveButton("Ok", null)
                    .setOnDismissListener(dialogInterface -> {
                        // Cancel will finish the process and set the progressIndicator to 100
                        EventBus.getDefault().post(new OnProgressIndicatorUpdate(100));
                    })
                    .show();
            return;
        }

        // End of general optimization requirements checking

        if (!useAdvancedAlgorithm) {
            startOptimization(false); // Directly proceed to optimization if optimization method is default
            return;
        }
        if (distanceRepository.getRecordsCount() == 0)
            startOptimization(true); // If no distance records found, start optimization immediately with matrix calculation
        else
            // Make sure whether recalculate the distance matrix again
            new MaterialAlertDialogBuilder(context)
                    .setTitle("Recalculate Distances")
                    .setMessage("Start over distance matrix calculation? All changes to the matrix will be lost.")
                    .setPositiveButton("Start Over", (dialogInterface, i) -> {
                        startOptimization(true); // Start optimization with matrix recalculation
                    })
                    .setNegativeButton("Keep", (dialogInterface, i) -> {
                        startOptimization(false); // Start optimization with current available matrix
                    })
                    .setNeutralButton("Cancel", null)
                    .show();

    }

    /**
     * Start optimization process.
     * @param isWithMatrixCalculation whether to also calculate the distance matrix
     */
    public void startOptimization(boolean isWithMatrixCalculation) {

        Boolean useAdvancedAlgorithm = RouteMatePref.readOptimizationIsAdvancedAlgorithm((Activity) context);
        OptimizationMethod optimizationMethod = RouteMatePref.readOptimizationMethod((Activity) context);

        solutionRepository.setUsingAdvancedAlgorithm(useAdvancedAlgorithm); // Set the solution's optimization method
        solutionRepository.setOptimizationMethod(optimizationMethod); // Set the solution's optimization method

        if (isWithMatrixCalculation) {
            // Request matrix calculation to DistancesViewModel
            EventBus.getDefault().post(
                    new OnDistancesViewModelRequest.Builder(OnDistancesViewModelRequest.Event.ACTION_RESOLVE_MATRIX)
                            .withContinueOptimization(true)
                            .build()
            );
        }
        else {
            requestComputation(); // Directly proceed to request computation
        }
    }

    /**
     * Directly resolve optimization using latest records of matrix, places, and vehicles. Better to use startOptimization() instead.
     */
    private void requestComputation() {

        // Read optimization method from preference
        Boolean useAdvancedAlgorithm = RouteMatePref.readOptimizationIsAdvancedAlgorithm((Activity) context);
        OptimizationMethod optimizationMethod = RouteMatePref.readOptimizationMethod((Activity) context);
        boolean isRoundTrip = RouteMatePref.readBoolean((Activity) context, RouteMatePref.OPTIMIZATION_IS_ROUND_TRIP, true);

        Vehicle defaultVehicle = vehicleRepository.getDefaultVehicle(); // Get default vehicle

        if (!useAdvancedAlgorithm) {
            requestMapboxOptimizationUseCase.invoke(placeRepository.getRecords(), vehicleRepository.getRecords(), isRoundTrip);
            return;
        }

        Log.e(TAG, "Distances: " + distanceRepository.getRecords().size() + " | Places: " + placeRepository.getRecords().size() + " | Default Vehicle: " + defaultVehicle);
        new DSMSolver.OptimizationBuilder(distanceRepository.getRecords(), placeRepository.getDSMPlaces(), vehicleRepository.getRecords())
                .withMethod(optimizationMethod)
                .withRoundTrip(isRoundTrip)
                .optimize();

    }

    /**
     * Clear Solution repository and MapboxDirectionsRoute repository
     */
    public void clearSolutionsAndDirections() {
        alterRepositoryUseCase.invoke(OnRepositoryUpdate.Event.ACTION_CLEAR, new MapboxDirectionsRoute(), false);
        alterRepositoryUseCase.invoke(OnRepositoryUpdate.Event.ACTION_CLEAR, new Solution(), false);
    }

    // Event listeners

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void _040703012023(@NonNull OnUpdateUserSession event) {

        // Do not proceed if operation is either status failed, not a retrieve event,
        // or not action optimization and not action update
        if (
                event.getStatus() != OnUpdateUserSession.Status.SUCCESS ||
                event.getEvent() != OnUpdateUserSession.Event.RETRIEVE ||
                (event.getAction() != OnUpdateUserSession.Action.OPTIMIZATION && event.getAction() != OnUpdateUserSession.Action.UPDATE)
        )
            return;

        // Only update quota if the action is UPDATE
        if (event.getAction() == OnUpdateUserSession.Action.UPDATE) {
            updateQuota();
            return;
        }

        if (event.getUserSession().getLastRemainingOptimizationQuota() <= 0) {
            new MaterialAlertDialogBuilder(context)
                    .setTitle("Out of chance")
                    .setMessage("Oops... You are running out of optimization chances. But please do not worry, it will be refreshed in the next day.")
                    .setPositiveButton("Ok", null)
                    .show();
            return;
        }

        requestOptimization(); // Request optimization

    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void _031205062022(@NonNull OnDistanceMatrixResponse event) {

        DSMSolver.calculateDistanceSavingValue(
                placeRepository.getDSMPlaces(),
                event.getMatrix()
        ); // Immediately calculate the saving distance to enable saving matrix computation

        // After distanceMatrix done calculated, continue to optimization if true
        if (event.isContinueOptimization()) {
            requestComputation();
        }

    }

    @Override
    public void onOptimizationSuccess(List<Solution> solutions) {

        syncOptimizationSession(); // Sync session

        // Re/set new solution to repository
        solutionRepository.setRecords(solutions, true);
        solutionRepository.assignRouteIndex();

        // Clear mapbox directions routes and its lines
        alterRepositoryUseCase.invoke(OnRepositoryUpdate.Event.ACTION_CLEAR, new MapboxDirectionsRoute(), false);

        new MaterialAlertDialogBuilder(context)
                .setTitle("Find Directions")
                .setMessage("Proceed to find directions? (This action might consumes data)")
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    // Fetch directions from Mapbox
                    requestMapboxDirectionsRouteUseCase.invoke(solutionRepository.getRecords(), DirectionsCriteria.PROFILE_DRIVING);
                })
                .setNeutralButton("Cancel", null)
                .setOnDismissListener(dialogInterface -> {
                    // Cancel will finish the process and set the progressIndicator to 100
                    EventBus.getDefault().post(new OnProgressIndicatorUpdate(100));
                })
                .show();

        Log.d(TAG, "onOptimizationSuccess: Optimization finished.");

    }

    @Override
    public void onOptimizationFailed(OptimizationResponseError error) {

        // Finish the process and set the progressIndicator to 100
        EventBus.getDefault().post(new OnProgressIndicatorUpdate(100));

        // Import localized error messages
        String[] errorMessages = context.getResources().getStringArray(R.array.dsmsolver_optimization_response_error_message);
        String errorMessage = errorMessages[0];

        switch (error) {
            case METHOD_NOT_IMPLEMENTED:
                errorMessage = errorMessages[0];
                break;
        }

        new MaterialAlertDialogBuilder(context)
                .setTitle("Failed to Optimize")
                .setMessage(errorMessage)
                .setPositiveButton("Ok", null)
                .show();

    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void _031205062022(@NonNull OnOptimizationResponse event) {

        if (event.getStatus() == OnOptimizationResponse.Status.FAILED) {
            // Finish the process and set the progressIndicator to 100
            EventBus.getDefault().post(new OnProgressIndicatorUpdate(100));

            assert event.getError() != null;
            new MaterialAlertDialogBuilder(context)
                    .setTitle("Failed to Optimize")
                    .setMessage(event.getError().toMessage(context.getResources()))
                    .setPositiveButton("Ok", null)
                    .show();

            return;
        }


        // If returned result is null, it means that the method does not yet implemented
        if (event.getSolutions() == null) {

            // Finish the process and set the progressIndicator to 100
            EventBus.getDefault().post(new OnProgressIndicatorUpdate(100));

            new MaterialAlertDialogBuilder(context)
                    .setTitle("Failed to Optimize")
                    .setMessage("Selected method has not yet implemented in RouteMate. Please wait patiently for the next update patch to come.")
                    .setPositiveButton("Ok", null)
                    .show();

            return;

        }

        syncOptimizationSession(); // Sync session

        // Re/set new solution to repository
        solutionRepository.setRecords(event.getSolutions(), true);
        solutionRepository.assignRouteIndex();

        // Clear mapbox directions routes and its lines
        alterRepositoryUseCase.invoke(OnRepositoryUpdate.Event.ACTION_CLEAR, new MapboxDirectionsRoute(), false);

        // If not using advanced algorithms, skip find directions
        Boolean useAdvancedAlgorithm = RouteMatePref.readOptimizationIsAdvancedAlgorithm((Activity) context);

        if (!useAdvancedAlgorithm) {
            EventBus.getDefault().post(new OnProgressIndicatorUpdate(100, OnProgressIndicatorUpdate.Event.OPTIMIZATION_FINISHED));
            Log.d(TAG, "OnDSMSolverOptimizationResponse: Optimization finished with default method.");
            return;
        }

        new MaterialAlertDialogBuilder(context)
                .setTitle("Find Directions")
                .setMessage("Proceed to find directions? (This action might consumes data)")
                .setPositiveButton("Yes", (dialogInterface, i) -> {
                    // Fetch directions from Mapbox
                    requestMapboxDirectionsRouteUseCase.invoke(solutionRepository.getRecords(), DirectionsCriteria.PROFILE_DRIVING);
                })
                .setNeutralButton("Cancel", null)
                .setOnDismissListener(dialogInterface -> {
                    // Cancel will finish the process and set the progressIndicator to 100
                    EventBus.getDefault().post(new OnProgressIndicatorUpdate(100));
                })
                .show();

        Log.d(TAG, "OnDSMSolverOptimizationResponse: Optimization finished.");

    }

    // TODO: Move
    private void syncOptimizationSession() {

        String uid = userRepository.getUser().getUid();
        String sessionId = DSMUser.getOptimizationSession(uid);
        Date now = TrueTime.now();
        DSMTimestamp dsmTimestamp = new DSMTimestamp(now);

        DSMUser dsmUser = userRepository.getDsmUser();
        assert dsmUser != null;

        // Get lastOptimizationQuota if any, if not take the plan default quota from dsmUSer.getOptimizationQuota()
        UserSession optimizationSession = sessionRepository.get(sessionId);
        int optimizationQuota = dsmUser.getOptimizationQuota();
        if (optimizationSession != null)
            optimizationQuota = optimizationSession.getLastRemainingOptimizationQuota();

        int lastOptimizationQuota = optimizationQuota - 1;

        UserSession.Builder userSession = new UserSession.Builder(sessionId, uid)
                .withLastOptimizationActivity(dsmTimestamp)
                .withLastRemainingOptimizationQuota(lastOptimizationQuota);

        sessionRepository.update(userSession.build(), OnUpdateUserSession.Action.NONE);

    }

}