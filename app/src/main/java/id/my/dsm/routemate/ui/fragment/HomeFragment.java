package id.my.dsm.routemate.ui.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import dev.shreyaspatil.MaterialDialog.MaterialDialog;
import id.my.dsm.routemate.R;
import id.my.dsm.routemate.data.event.model.OnRetrieveDSMUser;
import id.my.dsm.routemate.data.event.model.OnUserStatusChangedEvent;
import id.my.dsm.routemate.data.event.view.OnMainActivityFeatureRequest;
import id.my.dsm.routemate.data.event.view.OnMainActivityShowCaseRequest;
import id.my.dsm.routemate.data.model.user.DSMPlan;
import id.my.dsm.routemate.data.model.user.DSMUser;
import id.my.dsm.routemate.data.repo.distance.DistanceRepositoryN;
import id.my.dsm.routemate.data.repo.fleet.FleetRepository;
import id.my.dsm.routemate.data.repo.place.PlaceRepositoryN;
import id.my.dsm.routemate.data.repo.user.UserRepository;
import id.my.dsm.routemate.data.repo.user.UserStatus;
import id.my.dsm.routemate.databinding.FragmentHomeBinding;
import id.my.dsm.routemate.ui.fragment.viewmodel.HomeViewModel;
import id.my.dsm.routemate.ui.model.MaterialDialogTemplate;
import id.my.dsm.routemate.ui.model.OptionsMenu;
import id.my.dsm.routemate.ui.model.RouteMatePref;
import id.my.dsm.routemate.ui.recyclerview.MainMenuItem;
import id.my.dsm.routemate.ui.recyclerview.MainMenuItemRecViewAdapter;
import id.my.dsm.routemate.usecase.user.GetDisplayNameUseCase;

@AndroidEntryPoint
public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;

    // Dependencies
    @Inject
    GetDisplayNameUseCase getDisplayNameUseCase;
    @Inject
    UserRepository userRepository;
    @Inject
    PlaceRepositoryN placeRepository;
    @Inject
    FleetRepository vehicleRepository;
    @Inject
    DistanceRepositoryN distanceRepository;
    private HomeViewModel mViewModel;
    private RecyclerView rvMainMenu;
    private MainMenuItemRecViewAdapter adapter;

    private boolean isGetStartedFirstTime;
    private boolean isMenuGridLayout;
    private GridLayoutManager gridLayoutManager;
    private LinearLayoutManager linearLayoutManager;

    // Main Menu Items
    private final MainMenuItem mainMenuItemPlaces = new MainMenuItem.Builder(MainMenuItem.Title.Places, "Marked locations", R.drawable.ic_app_location_on)
            .withActionId(R.id.action_homeFragment_to_placesFragment)
            .withStyle(MainMenuItem.Style.DEFAULT_COUNT)
            .build();

    private final MainMenuItem mainMenuItemLocation = new MainMenuItem.Builder(MainMenuItem.Title.Location, "Show location details", R.drawable.ic_app_location_searching)
            .withActionId(R.id.action_homeFragment_to_locationFragment)
            .withStyle(MainMenuItem.Style.DEFAULT)
            .build();

    private final MainMenuItem mainMenuItemFleet = new MainMenuItem.Builder(MainMenuItem.Title.Fleet, "Set up transport", R.drawable.ic_app_local_shipping)
            .withActionId(R.id.action_homeFragment_to_vehiclesFragment)
            .withStyle(MainMenuItem.Style.DEFAULT_COUNT)
            .build();

    private final MainMenuItem mainMenuItemMatrix = new MainMenuItem.Builder(MainMenuItem.Title.Matrix, "Show distance matrix", R.drawable.ic_app_device_hub)
            .withActionId(R.id.action_homeFragment_to_distancesFragment)
            .withLabel("Pro")
            .build();

    private final MainMenuItem mainMenuItemOptimize = new MainMenuItem.Builder(MainMenuItem.Title.Optimize, "Obtain optimized route", R.drawable.ic_app_route)
            .withActionId(R.id.action_homeFragment_to_optimizationFragment)
            .withStyle(MainMenuItem.Style.OPAQUE)
            .build();

    private final ArrayList<MainMenuItem> mainMenuItems = new ArrayList<>(
            Arrays.asList(
                    mainMenuItemPlaces,
                    mainMenuItemFleet,
                    mainMenuItemOptimize,
                    mainMenuItemMatrix
            )
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);

        mViewModel = new ViewModelProvider(this).get(HomeViewModel.class); // Provide ViewModel
        Log.d(TAG, "" + mViewModel);

        // Set up view
        binding.textHomeGreet.setText("Hello, " + getDisplayNameUseCase.invoke());

        // Recycler
        gridLayoutManager = new GridLayoutManager(getContext(), 2, LinearLayoutManager.VERTICAL, false);
        linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        isMenuGridLayout = RouteMatePref.readBoolean(requireActivity(), RouteMatePref.HOME_MENU_LAYOUT_GRID_KEY, true);
        rvMainMenu = binding.rvHomeMainMenu;
        if (isMenuGridLayout)
            rvMainMenu.setLayoutManager(gridLayoutManager);
        else
            rvMainMenu.setLayoutManager(linearLayoutManager);

        adapter = new MainMenuItemRecViewAdapter(
                mainMenuItems,
                NavHostFragment.findNavController(this),
                userRepository.getDsmUser() == null ? DSMPlan.FREE : userRepository.getDsmUser().getPlan()
        );

        rvMainMenu.setAdapter(adapter);

        // Observers
        observeRepositoryRecordsCount(distanceRepository.getRecordsCountObservable(), mainMenuItemMatrix);
        observeRepositoryRecordsCount(placeRepository.getRecordsCountObservable(), mainMenuItemPlaces);
        observeRepositoryRecordsCount(vehicleRepository.getRecordsCountObservable(), mainMenuItemFleet);

        // Listeners
        binding.buttonHomeOptions.setOnClickListener(v -> showOptionsMenu(binding.buttonHomeOptions));
        binding.buttonHomeMenu.setOnClickListener(v -> EventBus.getDefault().post(
                new OnMainActivityFeatureRequest(OnMainActivityFeatureRequest.Event.OPEN_DRAWER)
        ));

        // Show App Intro & ShowCase
        isGetStartedFirstTime = RouteMatePref.readBoolean(requireActivity(), RouteMatePref.HOME_GET_STARTED_IS_FIRST_TIME_KEY, true);

        return binding.getRoot();

    }

    private void observeRepositoryRecordsCount(LiveData<Integer> recordsCountObservable, MainMenuItem mainMenuItem) {
        recordsCountObservable.observe(getViewLifecycleOwner(), count -> {
            mainMenuItem.setCountable(count);

            switch (mainMenuItem.getTitle()) {
                case Matrix:

                    if (count == 0)
                        mainMenuItems.remove(mainMenuItemMatrix);
                    else if (!mainMenuItems.contains(mainMenuItemMatrix))
                        mainMenuItems.add(mainMenuItemMatrix);
                    break;
                case Fleet:
                case Places:
                    if (placeRepository.getRecordsCountObservable().getValue() == 0 ||
                            vehicleRepository.getRecordsCountObservable().getValue() == 0)
                        mainMenuItems.remove(mainMenuItemOptimize);
                    else if (!mainMenuItems.contains(mainMenuItemOptimize))
                        mainMenuItems.add(mainMenuItemOptimize);
            }

            adapter.notifyDataSetChanged();
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this); // Register to event

        userRepository.validateUser(this); // Validate current user

        // Greet user if hasn't
        if (!mViewModel.isGreeted()) {
            Toast.makeText(requireContext(), "Hello, " + getDisplayNameUseCase.invoke(), Toast.LENGTH_SHORT).show();
            mViewModel.setGreeted(true);
        }

        // TODO: Add main menu tutorial
        if (isGetStartedFirstTime) {

            RouteMatePref.saveBoolean(requireActivity(), RouteMatePref.HOME_GET_STARTED_IS_FIRST_TIME_KEY, false);

            MaterialDialogTemplate.showVersionInfo(requireContext(), dialogInterface -> showAppIntro());

        }

    }

    private void showAppIntro() {
        new MaterialDialog.Builder(requireActivity())
                .setTitle("Welcome to RouteMate")
                .setMessage("Your optimal route only in a few clicks away!\nWe assure the rest of your life will be easier using RouteMate.\n\nClick start to take a quick intro tour.")
                .setCancelable(false)
                .setPositiveButton("Start", R.drawable.ic_app_arrow_forward_ios, (dialogInterface, which) -> {

                    EventBus.getDefault().post(
                            new OnMainActivityShowCaseRequest(OnMainActivityShowCaseRequest.Event.FAB_DRAW, v -> {

                                new MaterialDialog.Builder(requireActivity())
                                        .setTitle("Add Places")
                                        .setMessage("By activating Draw Mode, you can place a marker on your desired places anywhere in the map")
                                        .setCancelable(false)
                                        .setAnimation(R.raw.lottie_intro_adding_location)
                                        .setPositiveButton("Continue", R.drawable.ic_app_arrow_forward_ios, (dialogInterface2, which2) -> {

                                            dialogInterface2.dismiss();

                                            EventBus.getDefault().post(
                                                    new OnMainActivityShowCaseRequest(OnMainActivityShowCaseRequest.Event.MAP, v2 -> EventBus.getDefault().post(
                                                            new OnMainActivityShowCaseRequest(OnMainActivityShowCaseRequest.Event.FAB_TRAFFIC_STYLE, v3 -> EventBus.getDefault().post(
                                                                    new OnMainActivityShowCaseRequest(OnMainActivityShowCaseRequest.Event.FAB_MARKER_INFO, v4 -> EventBus.getDefault().post(
                                                                            new OnMainActivityShowCaseRequest(OnMainActivityShowCaseRequest.Event.FAB_MARKER_LOCK, v5 -> EventBus.getDefault().post(
                                                                                    new OnMainActivityShowCaseRequest(OnMainActivityShowCaseRequest.Event.FAB_START_NAVIGATION_VIEW)
                                                                            ))
                                                                    ))
                                                            ))
                                                    ))
                                            );
                                        })
                                        .build()
                                        .show();

                            })
                    );

                    dialogInterface.dismiss();
                })
                .setAnimation(R.raw.lottie_intro_navigation)
                .build()
                .show();
    }

    private void showOptionsMenu(View anchorView) {

        // Show places menu options
        OptionsMenu optionsMenu = new OptionsMenu(getContext(), anchorView, R.menu.menu_popup_home);

        optionsMenu.getPopupMenu().setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.home_switch_menu_layout:

                    // Switch layout and update value
                    isMenuGridLayout = !isMenuGridLayout;
                    RouteMatePref.saveBoolean(requireActivity(), RouteMatePref.HOME_MENU_LAYOUT_GRID_KEY, isMenuGridLayout);

                    rvMainMenu.setLayoutManager(isMenuGridLayout ? gridLayoutManager : linearLayoutManager);

                    break;
                case R.id.home_show_intro:
                    showAppIntro();
                    break;
            }
            return true;
        });

        optionsMenu.show(); // Show options menu
    }

    @Override
    public void onStop() {
        super.onStop();

        EventBus.getDefault().unregister(this); // Unregister to event
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // Subscribe to OnUserStatusChangedEvent event
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void _023905072022(OnUserStatusChangedEvent event) {

        // If current user is unauthorized and not yet asked to sign in
        if (event.getUserStatus() == UserStatus.UNAUTHORIZED && !mViewModel.isSignInAsked()) {
            NavHostFragment.findNavController(this).navigate(R.id.action_homeFragment_to_auth_navigation); // Ask user to sign in
            mViewModel.setSignInAsked(true); // Sign in has been asked to user
        }

    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void _070103052023(OnRetrieveDSMUser event) {

        DSMUser dsmUser = event.getDsmUser();

        if (dsmUser == null || event.getStatus() == OnRetrieveDSMUser.Status.FAILED)
            return;

        DSMPlan plan = dsmUser.getPlan();

        adapter.setPlan(plan); // This meant to refresh the adapter with the new plan

        Log.d(TAG, "OnRetrieveDSMUser: " + event.getStatus() + " | " + dsmUser.getFullName() + ": " + plan);

    }

}