package id.my.dsm.routemate.ui.fragment.viewmodel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.instacart.library.truetime.TrueTime;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Date;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import id.my.dsm.routemate.MainActivity;
import id.my.dsm.routemate.data.event.model.OnRetrieveUserData;
import id.my.dsm.routemate.data.event.model.OnUpdateUserData;
import id.my.dsm.routemate.data.event.model.OnUpdateUserSession;
import id.my.dsm.routemate.data.event.view.OnProgressIndicatorUpdate;
import id.my.dsm.routemate.data.event.view.OnViewStateChange;
import id.my.dsm.routemate.data.model.DSMTimestamp;
import id.my.dsm.routemate.data.model.session.UserSession;
import id.my.dsm.routemate.data.model.user.DSMUser;
import id.my.dsm.routemate.data.model.userdata.UserData;
import id.my.dsm.routemate.data.repo.distance.DistanceRepositoryN;
import id.my.dsm.routemate.data.repo.distance.SolutionRepositoryN;
import id.my.dsm.routemate.data.repo.mapbox.MapboxDirectionsRouteRepository;
import id.my.dsm.routemate.data.repo.place.PlaceRepositoryN;
import id.my.dsm.routemate.data.repo.user.SessionRepository;
import id.my.dsm.routemate.data.repo.user.UserRepository;
import id.my.dsm.routemate.data.repo.vehicle.VehicleRepositoryN;
import id.my.dsm.routemate.ui.fragment.viewmodel.mapsviewmodel.MapboxDirectionsRouteManager;
import id.my.dsm.routemate.usecase.userdata.LoadUserDataIntoRepositoriesUseCase;

@HiltViewModel
public class MainViewModel extends ViewModel {

    private static final String TAG = MainViewModel.class.getSimpleName();

    @SuppressLint("StaticFieldLeak")
    private Context context;

    private MapsViewModel mapsViewModel;

    // Repositories
    @Inject
    UserRepository userRepository;
    @Inject
    SessionRepository sessionRepository;
    @Inject
    PlaceRepositoryN placeRepository;
    @Inject
    VehicleRepositoryN vehicleRepository;
    @Inject
    SolutionRepositoryN solutionRepository;
    @Inject
    DistanceRepositoryN distanceRepository;

    @Inject
    MapboxDirectionsRouteRepository mapboxDirectionsRouteRepository;

    // Extension classes
    private MapboxDirectionsRouteManager mapboxDirectionsRouteManager;

    // Use cases
    @Inject
    LoadUserDataIntoRepositoriesUseCase loadUserDataIntoRepositoriesUseCase;

    @Inject
    public MainViewModel() {
        EventBus.getDefault().register(this); //
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        EventBus.getDefault().unregister(this); //
    }

    // Dependency provider

    public void provideContext(Context context) {
        this.context = context;
    }

    public void provideMapsViewModel(MapsViewModel mapsViewModel) {
        this.mapsViewModel = mapsViewModel;
    }

    public void provideMapboxDirectionsRouteManager(MapboxDirectionsRouteManager mapboxDirectionsRouteManager) {
        this.mapboxDirectionsRouteManager = mapboxDirectionsRouteManager;
    }

    // Subscribe to OnViewStateChange event
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void _021305062022(OnViewStateChange event) {

        OnViewStateChange.Event state = event.getEvent();

        switch (event.getViewTag()) {
            case MainActivity.FAB_DRAW:
                mapsViewModel.toggleDrawingMode();
                break;
            case MainActivity.FAB_TRAFFIC_STYLE:
                mapsViewModel.toggleTrafficStyle();
                break;
            case MainActivity.FAB_MARKER_INFO:
                mapsViewModel.toggleMarkerViewInfo();
                break;
            case MainActivity.FAB_MARKER_LOCK:
                mapsViewModel.toggleMarkerLock();
                break;

        }

    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void _064803042023(@NonNull OnUpdateUserSession event) {

        DSMUser user = userRepository.getDsmUser();

        if (event.getStatus() != OnUpdateUserSession.Status.UID_NOT_FOUND ||
                event.getEvent() != OnUpdateUserSession.Event.RETRIEVE ||
                user == null
        )
            return;

        // Insert new session (usually first-timer)
        Date now = TrueTime.now();
        String optimizationSessionId = DSMUser.getOptimizationSession(user.getUid());

        UserSession newUserSession = new UserSession.Builder(optimizationSessionId, user.getUid())
                .withLastRemainingOptimizationQuota(user.getOptimizationQuota())
                .withLastOptimizationQuotaRefresh(new DSMTimestamp(now))
                .build();

        sessionRepository.insert(newUserSession, OnUpdateUserSession.Action.NONE);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void _105705072022(@NonNull OnRetrieveUserData event) {
        switch (event.getStatus()) {

            case FAILED:
                // TODO: Add something
                break;
            case SUCCESS:

                UserData userData = event.getUserData(); // UserData is always NonNull if succeed
                assert userData != null;

                int placeRepositoryRecordsCount = placeRepository.getRecordsCount();
                int vehicleRepositoryRecordsCount = vehicleRepository.getRecordsCount();
                int solutionRepositoryRecordsCount = solutionRepository.getRecordsCount();
                int distanceRepositoryRecordsCount = distanceRepository.getRecordsCount();

                if (placeRepositoryRecordsCount != 0 || vehicleRepositoryRecordsCount > 1 || solutionRepositoryRecordsCount != 0 || distanceRepositoryRecordsCount != 0) {
                    Log.e(TAG, "OnRetrieveUserData: Cannot load UserData because user is currently working on the new data");
                    return;
                }

                // Fill newly fetched user data into repositories
                loadUserDataIntoRepositoriesUseCase.invoke(
                        userData.getPlaces(),
                        userData.getVehicles(),
                        userData.getMatrix(),
                        userData.getSolutions(),
                        userData.getMapboxDirectionsRoutes(),
                        userData.getOptimizationMethod(),
                        userData.getUsingAdvancedAlgorithm());

                Toast.makeText(context, "Session restored", Toast.LENGTH_SHORT).show();
                
                Log.d(TAG, "OnRetrieveUserData: UserData loaded successfully");

                break;

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void _105705072022(@NonNull OnUpdateUserData event) {

        switch (event.getStatus()) {
            case FAILED:
                new MaterialAlertDialogBuilder(context)
                        .setTitle("Sync Failed")
                        .setMessage(event.getMessage())
                        .setNeutralButton("Cancel", null)
                        .show();
                break;
            case SUCCESS:
                EventBus.getDefault().post(new OnProgressIndicatorUpdate(100));
                Log.d(TAG, "OnUpdateUserData: " + event.getMessage());
                break;
        }
    }

}
