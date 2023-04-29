package id.my.dsm.routemate.ui.fragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.my.dsm.routemate.R;
import id.my.dsm.routemate.data.event.view.OnBottomSheetStateChanged;
import id.my.dsm.routemate.data.event.repo.OnVehicleRepositoryUpdate;
import id.my.dsm.routemate.data.model.user.DSMPlan;
import id.my.dsm.routemate.data.repo.distance.SolutionRepositoryN;
import id.my.dsm.routemate.data.repo.user.UserRepository;
import id.my.dsm.routemate.data.repo.vehicle.VehicleRepositoryN;
import id.my.dsm.routemate.databinding.FragmentVehiclesBinding;
import id.my.dsm.routemate.data.enums.MapsAPI;
import id.my.dsm.routemate.library.dsmlib.model.Vehicle;
import id.my.dsm.routemate.ui.fragment.viewmodel.VehiclesViewModel;
import id.my.dsm.routemate.ui.model.IntroShowCase;
import id.my.dsm.routemate.ui.model.OptionsMenu;
import id.my.dsm.routemate.ui.model.RouteMateNavigation;
import id.my.dsm.routemate.ui.model.RouteMatePref;
import id.my.dsm.routemate.ui.recyclerview.VehicleRecViewAdapter;
import id.my.dsm.routemate.usecase.repository.AlterRepositoryUseCase;

@AndroidEntryPoint
public class VehiclesFragment extends Fragment {

    private static final String TAG = VehiclesFragment.class.getSimpleName();
    private FragmentVehiclesBinding binding;

    // Dependencies
    @Inject
    UserRepository userRepository;
    @Inject
    VehicleRepositoryN vehicleRepository;
    @Inject
    SolutionRepositoryN solutionRepositoryN;
    private VehiclesViewModel mViewModel;
    private RecyclerView rvVehiclesList;
    private VehicleRecViewAdapter adapter;

    // Use case
    @Inject
    AlterRepositoryUseCase alterRepositoryUseCase;

    // State
    private boolean isFeatureReady;
    private boolean isGetStartedDismiss;
    private boolean isFeatureFirstTime;

    public static VehiclesFragment newInstance() {
        return new VehiclesFragment();
    }

    @Override
    public void onStart() {
        super.onStart();

        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();

        EventBus.getDefault().unregister(this);
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentVehiclesBinding.inflate(inflater, container, false);

        mViewModel = new ViewModelProvider(this).get(VehiclesViewModel.class);

        // Setup layout transition
        binding.layoutVehicles.getLayoutTransition().setAnimateParentHierarchy(false);
        binding.cardVehiclesOverviewLayout.getLayoutTransition().setAnimateParentHierarchy(false);

        // Recycler
        rvVehiclesList = binding.rvVehiclesList;
        rvVehiclesList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        DSMPlan plan = userRepository.getDsmUser() != null ? userRepository.getDsmUser().getPlan() : DSMPlan.FREE;
        MapsAPI mapsAPI = RouteMatePref.readMapsAPI(requireActivity()); // Read mapsAPI preference
        adapter = new VehicleRecViewAdapter(vehicleRepository.getRecords(), alterRepositoryUseCase, solutionRepositoryN, NavHostFragment.findNavController(this), mapsAPI, plan);

        rvVehiclesList.setAdapter(adapter);

        // Observe state
        vehicleRepository.getRecordsCountObservable().observe(getViewLifecycleOwner(), count -> {

            // Card Overview
            binding.textVehiclesNumber.setText(count + "");

            this.isFeatureReady = vehicleRepository.getRecordsCount() > 1;
            setFeatureStatus(isFeatureReady);

        });

        // Setup view listeners
        binding.buttonVehiclesOptions.setOnClickListener(v -> {

            // Show places menu options
            OptionsMenu optionsMenu = new OptionsMenu(getContext(), v, R.menu.menu_popup_vehicles);

            optionsMenu.getPopupMenu().setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.vehicles_clear:
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Clear Vehicles")
                                .setMessage("This will clear vehicles except the default and the online.")
                                .setPositiveButton("Continue", (dialogInterface, i) -> {
                                    mViewModel.clearVehicles();
                                })
                                .setNeutralButton("Cancel", (dialogInterface, i) -> {
                                    dialogInterface.cancel();
                                })
                                .show();

                        break;
                    case R.id.vehicles_show_hint:
                        forceShowIntroShowCase();
                        break;
                }
                return true;
            });

            optionsMenu.show(); // Show options menu

        });

        // Set up listeners
        binding.buttonVehiclesAdd.setOnClickListener(v -> {
            // TODO: Customizable add vehicle
            mViewModel.createVehicle(new Vehicle("Vehicle", Vehicle.MapboxProfile.DRIVING_TRAFFIC, 1));
            RouteMateNavigation.navigateToVehiclesEdit(
                    NavHostFragment.findNavController(this),
                    vehicleRepository.getRecordsCount() - 1
            );
        });
        binding.buttonVehiclesHint.setOnClickListener(v -> forceShowIntroShowCase());
        binding.buttonVehiclesDismiss.setOnClickListener(v -> {
            binding.cardVehiclesGetStarted.setVisibility(GONE);
            RouteMatePref.saveBoolean(requireActivity(), RouteMatePref.VEHICLES_GET_STARTED_DISMISS_KEY, true);
        });
//        RouteMateNavigation.navigateToVehiclesEdit(
//                NavHostFragment.findNavController(this),
//                vehicleRepository.getRecordsCount() - 1,
//                true
//        );

        // Checks whether its first time
        isGetStartedDismiss = RouteMatePref.readBoolean(requireActivity(), RouteMatePref.VEHICLES_GET_STARTED_DISMISS_KEY, false);
        isFeatureFirstTime = Boolean.parseBoolean(
                RouteMatePref.readString(requireActivity(), RouteMatePref.VEHICLES_FEATURE_IS_FIRST_TIME_KEY, "true")
        );

        // Update UI based on preferences
        if (isGetStartedDismiss)
            binding.cardVehiclesGetStarted.setVisibility(GONE);

        // Show intro tutorial
        showIntroShowCase();

        return binding.getRoot();
    }

    // Status is true if vehicle count > 1
    private void setFeatureStatus(boolean status) {

        if (!isGetStartedDismiss)
            binding.cardVehiclesGetStarted.setVisibility(status ? GONE : VISIBLE);

    }

    private void forceShowIntroShowCase() {
        isFeatureFirstTime = true;
        showIntroShowCase();
    }

    private void showIntroShowCase() {

        if (isFeatureFirstTime && binding.cardVehiclesOverview.getVisibility() == VISIBLE) {
            // Save preference
            RouteMatePref.saveString(requireActivity(), RouteMatePref.VEHICLES_FEATURE_IS_FIRST_TIME_KEY, "false");

            // Display showcase
            IntroShowCase.show(
                    requireActivity(),
                    "Fleet Overview",
                    "General information about your Fleet",
                    binding.cardVehiclesOverview,
                    v -> {

                        if (!isAdded())
                            return;

                        IntroShowCase.show(
                                requireActivity(),
                                "Vehicle",
                                "Tap to edit vehicle, long tap to display options",
                                binding.rvVehiclesList.getChildAt(0).findViewById(R.id.cardVehiclesList),

                                v2 -> {

                                    if (!isAdded())
                                        return;

                                    IntroShowCase.show(
                                            requireActivity(),
                                            "Add Vehicle",
                                            "Add a new vehicle and adjust the parameter manually",
                                            binding.buttonVehiclesAdd,

                                                v3 -> {

                                                    if (!isAdded())
                                                        return;

                                                    IntroShowCase.show(
                                                            requireActivity(),
                                                            "Vehicles Options",
                                                            "Show more vehicle options",
                                                            binding.buttonVehiclesOptions
                                                    );

                                                }

                                            );
                                }

                        );
                    }
            );
        }

    }

    // Subscribe to OnVehicleRepositoryUpdate event
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void _105705072022(OnVehicleRepositoryUpdate event) {

        switch (event.getStatus()) {

            case RECORD_ADDED:
            case RECORD_DELETED:
            case RECORDS_CLEARED:
                // Notify that the entire item has removed
                adapter.notifyItemRangeRemoved(0, adapter.getItemCount());
                break;
        }
    }

    // Subscribe to OnBottomSheetStateChanged event
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void _073205062022(OnBottomSheetStateChanged event) {

        switch (event.getState()) {
            case BottomSheetBehavior.STATE_COLLAPSED:
            case BottomSheetBehavior.STATE_HALF_EXPANDED:
                if (isFeatureReady) {
                    binding.cardVehiclesOverview.setVisibility(VISIBLE);
                }
                break;
            case BottomSheetBehavior.STATE_EXPANDED:
                if (isFeatureReady) {
                    binding.cardVehiclesOverview.setVisibility(GONE);
                }
                break;
        }

    }

}