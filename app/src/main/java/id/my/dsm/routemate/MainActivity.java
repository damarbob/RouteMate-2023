package id.my.dsm.routemate;

import static android.view.View.VISIBLE;
import static id.my.dsm.routemate.data.repo.user.UserStatus.INSERT_RECORD_FAILURE;
import static id.my.dsm.routemate.data.repo.user.UserStatus.SIGNUP_FAILURE;
import static id.my.dsm.routemate.data.repo.user.UserStatus.SIGN_IN_FAILURE;
import static id.my.dsm.routemate.data.repo.user.UserStatus.SIGN_IN_SUCCESS;
import static id.my.dsm.routemate.data.repo.user.UserStatus.UNAUTHORIZED;
import static id.my.dsm.routemate.data.repo.user.UserStatus.UPDATE_USER_FAILURE;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.FragmentContainerView;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ServerValue;
import com.instacart.library.truetime.TrueTime;
import com.mapbox.mapboxsdk.Mapbox;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.IOException;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.my.dsm.routemate.data.event.model.OnMapStyleLoadedEvent;
import id.my.dsm.routemate.data.event.model.OnMapSymbolClickedEvent;
import id.my.dsm.routemate.data.event.model.OnOptimizationUpdate;
import id.my.dsm.routemate.data.event.model.OnRetrieveDSMUser;
import id.my.dsm.routemate.data.event.model.OnUpdateDSMUser;
import id.my.dsm.routemate.data.event.model.OnUpdateUserSession;
import id.my.dsm.routemate.data.event.model.OnUserStatusChangedEvent;
import id.my.dsm.routemate.data.event.view.OnBottomSheetStateChanged;
import id.my.dsm.routemate.data.event.view.OnMainActivityFeatureRequest;
import id.my.dsm.routemate.data.event.view.OnMainActivityShowCaseRequest;
import id.my.dsm.routemate.data.event.view.OnProgressIndicatorUpdate;
import id.my.dsm.routemate.data.event.view.OnViewStateChange;
import id.my.dsm.routemate.data.model.behavior.FabToBottomSheetBehavior;
import id.my.dsm.routemate.data.model.behavior.MapViewBehavior;
import id.my.dsm.routemate.data.model.user.DSMPlan;
import id.my.dsm.routemate.data.model.user.DSMUser;
import id.my.dsm.routemate.data.repo.distance.DistanceRepositoryN;
import id.my.dsm.routemate.data.repo.distance.SolutionRepositoryN;
import id.my.dsm.routemate.data.repo.fleet.FleetRepository;
import id.my.dsm.routemate.data.repo.mapbox.MapboxDirectionsRouteRepository;
import id.my.dsm.routemate.data.repo.place.PlaceRepositoryN;
import id.my.dsm.routemate.data.repo.user.SessionRepository;
import id.my.dsm.routemate.data.repo.user.UserRepository;
import id.my.dsm.routemate.data.repo.user.UserStatus;
import id.my.dsm.routemate.databinding.ActivityMainBinding;
import id.my.dsm.routemate.ui.fragment.dialog.ApplicationSettingsFragment;
import id.my.dsm.routemate.ui.fragment.viewmodel.DistancesViewModel;
import id.my.dsm.routemate.ui.fragment.viewmodel.MainViewModel;
import id.my.dsm.routemate.ui.fragment.viewmodel.MapsViewModel;
import id.my.dsm.routemate.ui.fragment.viewmodel.OptimizationViewModel;
import id.my.dsm.routemate.ui.fragment.viewmodel.mapsviewmodel.MapboxDirectionsRouteManager;
import id.my.dsm.routemate.ui.model.IntroShowCase;
import id.my.dsm.routemate.ui.model.MaterialDialogTemplate;
import id.my.dsm.routemate.ui.model.PlaceSearch;
import id.my.dsm.routemate.ui.model.RouteMatePref;
import id.my.dsm.routemate.ui.recyclerview.PlaceSearchRecViewAdapter;
import id.my.dsm.routemate.usecase.event.DispatchViewStateEventUseCase;
import id.my.dsm.routemate.usecase.place.RequestMapboxGeocodingUseCase;
import id.my.dsm.routemate.usecase.repository.AlterRepositoryUseCase;
import id.my.dsm.routemate.usecase.user.GetDisplayNameUseCase;
import id.my.dsm.routemate.usecase.user.ListenConnectionStateUseCase;
import id.my.dsm.routemate.usecase.userdata.RetrieveDSMUserUseCase;
import id.my.dsm.routemate.usecase.userdata.RetrieveUserDataUseCase;
import id.my.dsm.routemate.usecase.userdata.UploadUserDataUseCase;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity
{

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private ActivityMainBinding binding;

    // Constants
    private static final String TAG = "MainActivity";
    public static final String FAB_MARKER_INFO = "FAB_MARKER_INFO";
    public static final String FAB_MARKER_LOCK = "FAB_MARKER_LOCK";
    public static final String FAB_DRAW = "FAB_DRAW";
    public static final String FAB_START_NAVIGATION_VIEW = "FAB_START_NAVIGATION_VIEW";
    public static final String FAB_TRAFFIC_STYLE = "FAB_TRAFFIC_STYLE";

    // Dependencies
    @Inject
    UserRepository userRepository;
    @Inject
    SessionRepository sessionRepository;
    @Inject
    PlaceRepositoryN placeRepository;
    @Inject
    FleetRepository vehicleRepository;
    @Inject
    DistanceRepositoryN distanceRepository;
    @Inject
    SolutionRepositoryN solutionRepository;
    @Inject
    MapboxDirectionsRouteRepository mapboxDirectionsRouteRepository;
    private MapsViewModel mapsViewModel;
    private MainViewModel mainViewModel;
    @Inject
    MapboxDirectionsRouteManager mapboxDirectionsRouteManager;

    // Use cases
    @Inject
    RetrieveUserDataUseCase retrieveUserDataUseCase;
    @Inject
    RetrieveDSMUserUseCase retrieveDSMUserUseCase;
    @Inject
    GetDisplayNameUseCase getDisplayNameUseCase;
    @Inject
    ListenConnectionStateUseCase listenConnectionStateUseCase;
    @Inject
    UploadUserDataUseCase uploadUserDataUseCase;
    @Inject
    RequestMapboxGeocodingUseCase requestMapboxGeocodingUseCase;
    @Inject
    AlterRepositoryUseCase alterRepositoryUseCase;

    // Early ViewModel initiation in MainActivity
    private DistancesViewModel distancesViewModel; // DON'T DELETE
    private OptimizationViewModel optimizationViewModel; // DON'T DELETE

    // Model
    private PlaceSearch placeSearch;

    // View
    private LinearProgressIndicator progressIndicator;
    private LinearProgressIndicator progressIndicatorSearch;
    private RecyclerView rvMainPlaceSearch;
    private PlaceSearchRecViewAdapter placeSearchRecViewAdapter;

    // Navigation view
    private View startNavigationViewHeader;

    // State
    private boolean isMarkerInfoEnabled = true;
    private boolean isMarkerLockEnabled = true;
    private boolean isTrafficStyle = true;
    private boolean isDrawingMode = false;

    // Bottom sheet
    private BottomSheetBehavior bottomSheetBehavior; // For BottomSheetCallback and setting the bottom sheet's properties and behaviour


    /**
     * Initialize TrueTime TODO Move to other class fool!
     * @param context context
     */
    public static void initTrueTime(final Context context) {
        (new Thread(() -> {
            try {
                TrueTime.build().withNtpHost("time.google.com").withLoggingEnabled(false).withSharedPreferencesCache(context).withConnectionTimeout(31428).initialize();
            } catch (IOException var2) {
                var2.printStackTrace();
            }

        })).start();
    }

    @Override
    public void onBackPressed() {

        // Override action on back pressed
        if (placeSearch.getSearchShown()) // If search view is shown
            placeSearch.getSearchView().hide(); // Hide search view
        else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) // If bottom sheet is hidden
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED); // Display bottom sheet
        else
            super.onBackPressed(); // Do the default action
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        applyThemeFromPreferences();

        Mapbox.getInstance(getApplicationContext(), BuildConfig.MAPBOX_ACCESS_TOKEN); //Must be initiated before inflating view

        initTrueTime(this); // Initialize TrueTime

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EventBus.getDefault().register(this); // Register to listen global events
        placeRepository.onStart();
        vehicleRepository.onStart();
        distanceRepository.onStart();
        solutionRepository.onStart();
        mapboxDirectionsRouteRepository.onStart();

        mapsViewModel = new ViewModelProvider(this).get(MapsViewModel.class); // Provide ViewModel
        mapsViewModel.provideContext(this);
        mapsViewModel.provideMapboxDirectionsRouteManager(mapboxDirectionsRouteManager);

        mainViewModel = new ViewModelProvider(this).get(MainViewModel.class); // Provide ViewModel
        mainViewModel.provideContext(this);
        mainViewModel.provideMapsViewModel(mapsViewModel);
        mainViewModel.provideMapboxDirectionsRouteManager(mapboxDirectionsRouteManager);

        // Initiate viewModel in MainActivity to be able to use early and to make it ActivityScoped
        distancesViewModel = new ViewModelProvider(this).get(DistancesViewModel.class); // Provide ViewModel
        distancesViewModel.provideContext(this);
        optimizationViewModel = new ViewModelProvider(this).get(OptimizationViewModel.class);
        optimizationViewModel.provideContext(this);

        // View
        progressIndicator = binding.incBottomSheetMain.progressIndicator;

        // Navigation View
        startNavigationViewHeader = binding.startNavigationView.inflateHeaderView(R.layout.header_drawer_navigation_start);

        startNavigationViewHeader.findViewById(R.id.cardUserInfo).setOnClickListener(v -> {
            if (userRepository.getUser() != null) {
                return;
            }
            binding.parentMainActivity.closeDrawer(GravityCompat.START);
            Navigation.findNavController(binding.incBottomSheetMain.bottomSheetMainFragmentContainer).navigate(R.id.action_global_auth_navigation);
        });

        binding.startNavigationView.setNavigationItemSelectedListener(item -> {

            binding.parentMainActivity.closeDrawer(GravityCompat.START); // Close drawer

            switch (item.getItemId()) {
                case R.id.settings_item:
                    // Go to settings fragment
                    new ApplicationSettingsFragment().show(getSupportFragmentManager(), "");
                    break;
                case R.id.policy_item:
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.app_tc_url)));
                    startActivity(browserIntent);
                    break;
                case R.id.about_item:
                    new MaterialAlertDialogBuilder(this)
                            .setTitle("About")
                            .setMessage(getString(R.string.about_dialog_message))
                            .setPositiveButton("Version Info", (dialogInterface, i) -> {
                                MaterialDialogTemplate.showVersionInfo(this, null);
                            })
                            .setNeutralButton("Close", null)
                            .show();
                    break;
            }
            return true;
        });

        // Place search
        placeSearch = new PlaceSearch(this, binding.searchBarPlaces, binding.searchViewPlaces, binding.progressIndicatorSearch, binding.rvMainSearch);
        placeSearch.onCreate();

        // Bottom sheet attributes
        bottomSheetBehavior = BottomSheetBehavior.from(binding.incBottomSheetMain.bottomSheetMain);
        bottomSheetBehavior.setFitToContents(false);
        bottomSheetBehavior.setHalfExpandedRatio(0.6f);
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        binding.appBarLayout.setExpanded(true);
                        break;
                    case BottomSheetBehavior.STATE_HALF_EXPANDED:
                        binding.appBarLayout.setExpanded(true);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        binding.appBarLayout.setExpanded(false);
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        binding.appBarLayout.setExpanded(false);
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        // Observe state
        mapsViewModel.isMarkerInfoEnabled().observe(this, isMarkerInfoEnabled -> {
            this.isMarkerInfoEnabled = isMarkerInfoEnabled;

            buttonToggleModeByCondition(binding.fabMainMarkerInfo, isMarkerInfoEnabled, "Marker info");
        });
        mapsViewModel.isMarkerLockEnabled().observe(this, isMarkerLockEnabled -> {
            this.isMarkerLockEnabled = isMarkerLockEnabled;

            buttonToggleModeByCondition(binding.fabMainMarkerLock, isMarkerLockEnabled, "Marker info");
        });
        mapsViewModel.isDrawingMode().observe(this, drawingMode -> {
            this.isDrawingMode = drawingMode;

            buttonToggleModeByCondition(binding.fabMainDraw, drawingMode, "Draw");
        });
        mapsViewModel.isTrafficStyle().observe(this, trafficStyle -> {
            this.isTrafficStyle = trafficStyle;

            buttonToggleModeByCondition(binding.fabMainTrafficStyle, trafficStyle, "Marker info");
        });

        // Set listeners
        binding.incBottomSheetMain.bottomSheetMain.setOnTouchListener((v, event) -> true);
        binding.fabMainStartNavigationView.setOnClickListener(v -> binding.parentMainActivity.openDrawer(GravityCompat.START));
        binding.fabMainDraw.setOnClickListener(v -> {
            DispatchViewStateEventUseCase.invoke(FAB_DRAW, !isDrawingMode ? OnViewStateChange.Event.STATE_ENABLED : OnViewStateChange.Event.STATE_DISABLED);
        });
        binding.fabMainTrafficStyle.setOnClickListener(v -> {
            DispatchViewStateEventUseCase.invoke(FAB_TRAFFIC_STYLE, !isTrafficStyle ? OnViewStateChange.Event.STATE_ENABLED : OnViewStateChange.Event.STATE_DISABLED);;
        });
        binding.fabMainMarkerInfo.setOnClickListener(v -> {
            DispatchViewStateEventUseCase.invoke(FAB_MARKER_INFO, !isMarkerInfoEnabled ? OnViewStateChange.Event.STATE_ENABLED : OnViewStateChange.Event.STATE_DISABLED);
        });
        binding.fabMainMarkerLock.setOnClickListener(v -> {
            DispatchViewStateEventUseCase.invoke(FAB_MARKER_LOCK, !isMarkerLockEnabled ? OnViewStateChange.Event.STATE_ENABLED : OnViewStateChange.Event.STATE_DISABLED);
        });

        // Use case
        listenConnectionStateUseCase.invoke();

    }

    private void applyThemeFromPreferences() {

        String theme = RouteMatePref.readString(this, RouteMatePref.APPLICATION_THEME_KEY, RouteMatePref.APPLICATION_THEME_VALUE_AUTO);
        switch (theme) {
            case RouteMatePref.APPLICATION_THEME_VALUE_LIGHT:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
            case RouteMatePref.APPLICATION_THEME_VALUE_DARK:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
        }

    }

    @Override
    protected void onStart() {

        super.onStart();

        mapsViewModel.provideNavController(Navigation.findNavController(this, R.id.mapsFragmentContainer));

    }

    @Override
    protected void onStop() {

        EventBus.getDefault().unregister(this);

        super.onStop();

    }


    /**
     * Toggle FAB (switch enabled/disabled state) by given condition.
     * @param button {@link FloatingActionButton} instance
     * @param condition boolean condition
     * @param context FAB identifier context in {@link String}
     */
    private void buttonToggleModeByCondition(@NonNull MaterialButton button, boolean condition, String context) {
        button.setChecked(condition);
        if (condition) {

            Snackbar snackbar = Snackbar.make(this, button, context + " mode activated", Snackbar.LENGTH_SHORT);
            snackbar
                    .setAction("Dismiss", v -> {
                        snackbar.dismiss();
                    })
                    .setAnimationMode(BaseTransientBottomBar.ANIMATION_MODE_SLIDE);

            snackbar.show();

        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        /*
            All static variables should be set null here if needed to prevent huge memory leaks
         */
        placeRepository.onDestroy();
        vehicleRepository.onDestroy();
        distanceRepository.onDestroy();
        solutionRepository.onDestroy();
        mapboxDirectionsRouteRepository.onDestroy();

        placeSearch.onDestroy();
    }

    /**
     * Setup all {@link CoordinatorLayout.Behavior} used in {@link MainActivity}
     */
    private void setupCoordinatorBehavior() {
        // Set up coordinator behavior
        setupCoordinatorBehavior(binding.mapsFragmentContainer);
//        setupFabCoordinatorBehavior(binding.fabMainTrafficStyle, FabToBottomSheetBehavior.BOTTOM_LEVEL_2);
//        setupFabCoordinatorBehavior(binding.fabMainDraw, FabToBottomSheetBehavior.BOTTOM_LEVEL_2);
        setupFabCoordinatorBehavior(binding.fabMainStartNavigationView, FabToBottomSheetBehavior.TOP_LEVEL_1);
    }

    /**
     * Setup {@link CoordinatorLayout.Behavior} to a specific {@link View}
     * @param view view instance to be attached
     * @param <T> view class type
     */
    private <T extends View> void setupCoordinatorBehavior(View view) {
        // Set up coordinator behavior
        CoordinatorLayout.LayoutParams mapViewParams = (CoordinatorLayout.LayoutParams) view.getLayoutParams();
        CoordinatorLayout.Behavior<T> behavior = null;

        if (view instanceof FragmentContainerView) {
            behavior = (CoordinatorLayout.Behavior<T>) new MapViewBehavior(
                    this,
                    null
            );
        }

        mapViewParams.setBehavior(behavior);

    }

    /**
     * Setup a {@link CoordinatorLayout.Behavior} for {@link FloatingActionButton}
     * @param fab {@link FloatingActionButton} instance
     * @param fabLevel fab level relative to BottomSheet
     */
    private void setupFabCoordinatorBehavior(FloatingActionButton fab, int fabLevel) {
        CoordinatorLayout.LayoutParams fabParams = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
        fabParams.setBehavior(
                new FabToBottomSheetBehavior(
                        this,
                        fab,
                        fabLevel,
                        null
                )
        );
    }

    // Event listeners

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void _OnUserStateChanged(OnUserStatusChangedEvent event) {

        UserStatus status = event.getUserStatus();

        boolean isUnauthorized =
                (status == UNAUTHORIZED) || (status == SIGN_IN_FAILURE) || (status == SIGNUP_FAILURE) || (status == UPDATE_USER_FAILURE) || (status == INSERT_RECORD_FAILURE);

        if (isUnauthorized)
            return;

        TextView textUserDisplayName = startNavigationViewHeader.findViewById(R.id.textUserDisplayName);
        textUserDisplayName.setText(getDisplayNameUseCase.invoke());

        TextView textUserEmail = startNavigationViewHeader.findViewById(R.id.textUserEmail);
        textUserEmail.setText(userRepository.getUser().getEmail());

        if (status == SIGN_IN_SUCCESS) {
            retrieveDSMUserUseCase.invoke(); // Related to user's plan
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void _OnMapStyleLoadedEvent(OnMapStyleLoadedEvent event) {

        /*
            Wait until maps finished loaded its style,
            then setup UI behavior for buttons
         */
        setupCoordinatorBehavior(); // Set up view behaviors

        /*
            Retrieve user data from server if the userData has not yet
            loaded to repositories.

            UserData must be loaded after Map's Style Loaded because
            retrieveUserDataUseCase involves loadIntoRepositories
            and then triggers a sequence of map UI events like placing
            symbols, drawing routes, etc. If the userData retrieved before
            the map ready, the app crashes.
         */
        boolean isUserDataLoaded = placeRepository.getRecordsCount() != 0 ||
                vehicleRepository.getRecordsCount() > 1 ||
                solutionRepository.getRecordsCount() != 0 ||
                distanceRepository.getRecordsCount() != 0;

        if (!isUserDataLoaded)
            retrieveUserDataUseCase.invoke();


    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void _OnRetrieveDSMUser(OnRetrieveDSMUser event) {

        if (event.getDsmUser() == null) {
            Log.e(TAG, "OnRetrieveDSMUser: DSMUser is empty");
            return;
        }

        String uid = event.getDsmUser().getUid();
        String plan;

        // For user that has no plan yet (also prevents plan attribute-related error)
        if (event.getDsmUser().getPlan() == null) {

            // Create a new free user plan
            DSMUser freeUser = new DSMUser();
            freeUser.setUid(uid);
            freeUser.setPlan(DSMPlan.FREE);

            userRepository.updateDSMUser(uid, freeUser);

            plan = DSMPlan.FREE.toString();

        }
        else {
            plan = event.getDsmUser().getPlan().toString();
        }

        // Update DSMUser on userRepository
        userRepository.setDsmUser(event.getDsmUser());

        // Retrieve session
        sessionRepository.retrieve(uid, OnUpdateUserSession.Action.NONE);

        Log.d(TAG, "OnRetrieveDSMUser: ServerValue.TIMESTAMP " + ServerValue.TIMESTAMP);
        Log.d(TAG, "OnRetrieveDSMUser: DSMUser plan " + plan);

    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void _OnUpdateDSMUser(OnUpdateDSMUser event) {

        if (event.getDsmUser() == null || event.getStatus() == OnUpdateDSMUser.Status.FAILED) {
            Log.w(TAG, "OnUpdateDSMUser: DSMUser is null or update failed: " + event.getMessage());
            return;
        }

        // Update DSMUser on userRepository
        userRepository.setDsmUser(event.getDsmUser());

        Log.d(TAG, "OnUpdateDSMUser: " + event.getStatus());

    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void _031205062022(@NonNull OnOptimizationUpdate event) {

        /*
            Happens when all optimization process is done including distance matrix calculation
            and directions request.

            Sync all data to the server.
         */

        // Sync data to cloud
        uploadUserDataUseCase.invoke(true, true, true, true, true);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void _035905122022(OnProgressIndicatorUpdate event) {

        // Show progressIndicator whenever a "progress" is happening
        progressIndicator.setVisibility(VISIBLE);

        // Update progress indicator upon request
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            progressIndicator.setProgress(event.getValue(), true);
        }
        else {
            progressIndicator.setProgress(event.getValue());
        }

        if (progressIndicator.getProgress() == 100 || progressIndicator.getProgress() == 0) {

            // Hide progressIndicator in 2 seconds
            progressIndicator.postDelayed(() -> {
                progressIndicator.setVisibility(View.INVISIBLE);

                if (progressIndicator.getProgress() == 100) {
                    EventBus.getDefault().postSticky(
                            new OnProgressIndicatorUpdate(0)
                    );
                }

            }, 2000);

            if (progressIndicator.getProgress() == 100 && event.getEvent() != null) {

                switch (event.getEvent()) {

                    case DIRECTIONS_OBTAINED:
                        break;
                    case OPTIMIZATION_FINISHED:
                        EventBus.getDefault().post(
                                new OnOptimizationUpdate(OnOptimizationUpdate.Event.FINISHED)
                        );
                        break;
                }


            }

        }

    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void _OnMapSymbolClickedEvent(OnMapSymbolClickedEvent event) {
        Navigation.findNavController(binding.incBottomSheetMain.bottomSheetMainFragmentContainer).navigate(R.id.action_global_locationFragment);
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void _053803052023(OnMainActivityFeatureRequest event) {
        switch (event.getEvent()) {
            case OPEN_DRAWER:
                binding.parentMainActivity.openDrawer(GravityCompat.START);
                break;
            case HIDE_SEARCH_PANEL:
                binding.searchViewPlaces.hide();
                break;
            case PULL_BOTTOM_PANEL:
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                break;
            case HIDE_BOTTOM_PANEL:
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.POSTING)
    public void _121502262023(OnMainActivityShowCaseRequest event) {
        switch (event.getEvent()) {
            case MAP:
                IntroShowCase.show(
                        this,
                        "Map",
                        "Find your places here",
                        binding.mapCrossHair,
                        event.getOnDismissListener()
                );
                break;
            case FAB_DRAW:
                IntroShowCase.show(
                        this,
                        "Draw Mode",
                        "This mode allows you to mark places on the map",
                        binding.fabMainDraw,
                        event.getOnDismissListener()
                );
                break;
            case FAB_TRAFFIC_STYLE:
                IntroShowCase.show(
                        this,
                        "Map Mode",
                        "Switch between traffic or normal map mode",
                        binding.fabMainTrafficStyle,
                        event.getOnDismissListener()
                );
                break;
            case FAB_START_NAVIGATION_VIEW:
                IntroShowCase.show(
                        this,
                        "Open Sidebar",
                        "Swipe to the right to open the sidebar",
                        binding.fabMainStartNavigationView,
                        event.getOnDismissListener()
                );
                break;
            case FAB_MARKER_INFO:
                IntroShowCase.show(
                        this,
                        "Marker Info",
                        "Show or hide place's info bubble on the map",
                        binding.fabMainMarkerInfo,
                        event.getOnDismissListener()
                );
                break;
            case FAB_MARKER_LOCK:
                IntroShowCase.show(
                        this,
                        "Marker Lock",
                        "Lock or unlock place's marker on the map",
                        binding.fabMainMarkerLock,
                        event.getOnDismissListener()
                );
                break;
        }
    }

    public boolean checkLocationPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new MaterialAlertDialogBuilder(this)
                        .setTitle("My Location")
                        .setMessage("My location is a feature that lets you locate your position in realtime. It mostly useful for pointing a place right at your position or doing navigation. However, this feature needs a Location Permission in order to work properly. In the next dialog, you will be prompted to allow Location Permission. Please tap allow to enable Location Permission.")
                        .setPositiveButton("Next", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                requestLocationPermission();
                            }
                        })
                        .create()
                        .show();

            } else {
                // No explanation needed, we can request the permission.
                requestLocationPermission();
            }
            return false;
        } else {
            return true;
        }

    }

    private void requestLocationPermission() {
        new MaterialAlertDialogBuilder(this)
                .setTitle("Permission Needed")
                .setMessage("Please allow Location Permission in the next dialog to enable live location feature.")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //Prompt the user once explanation has been shown
                        ActivityCompat.requestPermissions(
                                MainActivity.this,
                                new String[]{
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                },
                                MY_PERMISSIONS_REQUEST_LOCATION
                        );

                    }
                })
                .create()
                .show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        //Request location updates:
                        mapsViewModel.addMyLocationPlace();
                    }

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(this, "Location Permission denied", Toast.LENGTH_SHORT).show();

                    new MaterialAlertDialogBuilder(this)
                            .setTitle("Permission Denied")
                            .setMessage("You had chosen to deny Location Permission. Unable to use Live Location feature.")
                            .setPositiveButton("Hide", null)
                            .create()
                            .show();

                }
                return;
            }

        }
    }
}