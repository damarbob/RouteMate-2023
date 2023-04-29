package id.my.dsm.routemate.ui.fragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseUser;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.my.dsm.routemate.R;
import id.my.dsm.routemate.data.event.model.OnUpdateUserSession;
import id.my.dsm.routemate.data.event.repo.OnSolutionRepositoryUpdate;
import id.my.dsm.routemate.data.event.view.OnBottomSheetStateChanged;
import id.my.dsm.routemate.data.event.view.OnProgressIndicatorUpdate;
import id.my.dsm.routemate.data.event.viewmodel.OnMapsViewModelRequest;
import id.my.dsm.routemate.data.model.user.DSMPlan;
import id.my.dsm.routemate.data.repo.distance.DistanceRepositoryN;
import id.my.dsm.routemate.data.repo.distance.SolutionRepositoryN;
import id.my.dsm.routemate.data.repo.mapbox.MapboxDirectionsRouteRepository;
import id.my.dsm.routemate.data.repo.place.PlaceRepositoryN;
import id.my.dsm.routemate.data.repo.user.SessionRepository;
import id.my.dsm.routemate.data.repo.user.UserRepository;
import id.my.dsm.routemate.data.repo.vehicle.VehicleRepositoryN;
import id.my.dsm.routemate.databinding.FragmentOptimizationBinding;
import id.my.dsm.routemate.library.dsmlib.enums.OptimizationMethod;
import id.my.dsm.routemate.library.dsmlib.model.Solution;
import id.my.dsm.routemate.library.dsmlib.model.Vehicle;
import id.my.dsm.routemate.ui.fragment.dialog.OptimizationFilterFragment;
import id.my.dsm.routemate.ui.fragment.viewmodel.MapsViewModel;
import id.my.dsm.routemate.ui.fragment.viewmodel.OptimizationViewModel;
import id.my.dsm.routemate.ui.model.IntroShowCase;
import id.my.dsm.routemate.ui.model.MeasurementConversion;
import id.my.dsm.routemate.ui.model.DistanceMeasurementUnit;
import id.my.dsm.routemate.ui.model.OptionsMenu;
import id.my.dsm.routemate.ui.model.RouteMatePref;
import id.my.dsm.routemate.ui.recyclerview.SolutionRecViewAdapter;

@AndroidEntryPoint
public class OptimizationFragment extends Fragment implements OptimizationFilterFragment.OptimizationFilterListener {

    public static final int PLACES_IS_READY = 1;
    public static final int PLACES_IS_NOT_READY = 0;
    public static final int PLACES_IS_MISSING_DESTINATIONS = -1;
    public static final int PLACES_IS_MISSING_SOURCES = -2;
    public static final int VEHICLES_IS_READY = 1;
    public static final int VEHICLES_IS_NOT_READY = 0;
    private static final String TAG = OptimizationFragment.class.getSimpleName();

    private FragmentOptimizationBinding binding;

    // Dependencies
    @Inject
    UserRepository userRepository;
    @Inject
    SessionRepository sessionRepository;
    @Inject
    PlaceRepositoryN placeRepository;
    @Inject
    DistanceRepositoryN distanceRepository;
    @Inject
    VehicleRepositoryN vehicleRepository;
    @Inject
    SolutionRepositoryN solutionRepository;
    @Inject
    MapboxDirectionsRouteRepository mapboxDirectionsRouteRepository;
    private RecyclerView rvSolution;
    private MapsViewModel mapsViewModel;
    private OptimizationViewModel mViewModel;
    private SolutionRecViewAdapter adapter;

    // States
    private DistanceMeasurementUnit distanceMeasurementUnit = DistanceMeasurementUnit.KILOMETER;
    private boolean isFeatureFirstTime;
    private boolean withMatrixCalculation = true;
    private int placesStatus = 0;
    private int transportStatus = 0;

    public static OptimizationFragment newInstance() {
        return new OptimizationFragment();
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentOptimizationBinding.inflate(inflater, container, false);

        mapsViewModel = new ViewModelProvider(requireActivity()).get(MapsViewModel.class);
        mViewModel = new ViewModelProvider(requireActivity()).get(OptimizationViewModel.class);

        // Setup layout transition
        binding.layoutOptimization.getLayoutTransition().setAnimateParentHierarchy(false);
        binding.cardOptimizationOverviewLayout.getLayoutTransition().setAnimateParentHierarchy(false);
        binding.layoutOptimizationFilter.getLayoutTransition().setAnimateParentHierarchy(false);

        // Adjust layout based on plan
        binding.buttonOptimizationSettings.setVisibility(
                userRepository.getDsmUser() == null ?
                        GONE :
                        userRepository.getDsmUser().getPlan() == DSMPlan.FREE ?
                                GONE :
                                VISIBLE
        );

        // Recycler
        rvSolution = binding.rvOptimizationList;
        rvSolution.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        adapter = new SolutionRecViewAdapter(solutionRepository.getRecords(), placeRepository, vehicleRepository, requireActivity(), distanceMeasurementUnit);
        rvSolution.setAdapter(adapter);

        // Observe state
        placeRepository.getRecordsCountObservable().observe(getViewLifecycleOwner(), count -> {
            refreshFeatureStatus();
        });
        vehicleRepository.getRecordsCountObservable().observe(getViewLifecycleOwner(), count -> {
            refreshFeatureStatus();
        });

        solutionRepository.getRecordsCountObservable().observe(getViewLifecycleOwner(), count -> {
            refreshFeatureStatus();
        });

        // Set up listeners
        binding.buttonOptimizationOptimize.setOnClickListener(v -> {
            mViewModel.beginOptimization();
        });
        binding.buttonOptimizationSettings.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.action_optimizationFragment_to_optimizationSettingsFragment);
        });
        binding.buttonOptimizationOptions.setOnClickListener(v -> {
            // Show optimization menu options
            OptionsMenu optionsMenu = new OptionsMenu(v.getContext(), v, R.menu.menu_popup_optimization);

            // Set listeners
            optionsMenu.getPopupMenu().setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.optimization_clear_result:
                        // Clear solution directions route line
                        new MaterialAlertDialogBuilder(requireContext())
                                .setTitle("Clear Route")
                                .setMessage("This will clear the route and directions. You will have to do optimization again to obtain a new route.")
                                .setPositiveButton("Proceed", (dialogInterface, i) -> {

                                    // Clear route
                                    mViewModel.clearSolutionsAndDirections();

                                    refreshFeatureStatus(); // Refresh features

                                })
                                .setNeutralButton("Cancel", null)
                                .show();

                        break;
                    case R.id.optimization_show_hint:
                        forceShowIntroShowCase();
                        break;
                }
                return true;
            });
            optionsMenu.show();
        });
        binding.buttonOptimizationHint.setOnClickListener(v -> forceShowIntroShowCase());
        binding.buttonOptimizationAddSource.setOnClickListener(v ->
            NavHostFragment.findNavController(this).navigate(R.id.action_optimizationFragment_to_placesFragment)
        );
        binding.buttonOptimizationAddDestination.setOnClickListener(v ->
            NavHostFragment.findNavController(this).navigate(R.id.action_optimizationFragment_to_placesFragment)
        );
        binding.buttonOptimizationFilter.setOnClickListener(view -> {
            OptimizationFilterFragment optimizationFilterFragment = new OptimizationFilterFragment();

            ArrayList<String> tripNames = new ArrayList<>();

            for (Solution d : solutionRepository.getRecords()) {
                String tripName = "Trip " + d.getTripIndex();

                if (!tripNames.contains(tripName))
                    tripNames.add(tripName);
            }

            ///

            ArrayList<String> vehicleNames = new ArrayList<>();

            for (Vehicle v : vehicleRepository.getRecords()) {
                vehicleNames.add(v.getName());
            }

            Bundle bundle = new Bundle();
            bundle.putStringArrayList("tripNames", tripNames);
            bundle.putStringArrayList("vehicleNames", vehicleNames);

            optimizationFilterFragment.setArguments(bundle);
            optimizationFilterFragment.setCaller(this);

            optimizationFilterFragment.show(requireActivity().getSupportFragmentManager(), "");

        });
        binding.buttonOptimizationFilterVehicle.setOnClickListener(v -> {
            binding.buttonOptimizationFilterVehicle.setVisibility(GONE);
            clearFilter();
        });
        binding.buttonOptimizationFilterTrip.setOnClickListener(v -> {
            binding.buttonOptimizationFilterTrip.setVisibility(GONE);
            clearFilter();
        });

        // Check preferences
        distanceMeasurementUnit = RouteMatePref.readMeasurementDistanceUnit(requireActivity());
        isFeatureFirstTime = Boolean.parseBoolean(
                RouteMatePref.readString(requireActivity(), RouteMatePref.OPTIMIZATION_FEATURE_IS_FIRST_TIME_KEY, "true")
        );

        // Update quota
        FirebaseUser user = userRepository.getUser();
        if (user != null) {

            sessionRepository.retrieve(user.getUid(), OnUpdateUserSession.Action.UPDATE);

            showIntroShowCase();

        }
        else
            new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Please Sign In")
                    .setMessage("Please sign in to enable optimization feature")
                    .setPositiveButton("Sign In", (dialogInterface, i) -> {
                        NavHostFragment.findNavController(this).navigate(R.id.auth_navigation);
                    })
                    .setNeutralButton("Cancel", ((dialogInterface, i) ->
                            NavHostFragment.findNavController(this).popBackStack()))
                    .setCancelable(false)
                    .show();

        binding.buttonOptimizationOptimize.setVisibility(user != null ? VISIBLE : GONE);

        return binding.getRoot();
    }

    private void refreshFeatureStatus() {

        // Check Places status
        if (placeRepository.getSourcesCount() > 0)
            setPlaceStatus(placeRepository.getDestinationsCount() > 0 ? PLACES_IS_READY : PLACES_IS_MISSING_DESTINATIONS);
        else
            setPlaceStatus(placeRepository.getDestinationsCount() > 0 ? PLACES_IS_MISSING_SOURCES : PLACES_IS_NOT_READY);

        // Check Transport status
        setTransportStatus(vehicleRepository.getRecordsCount() > 0 ? VEHICLES_IS_READY : VEHICLES_IS_NOT_READY);

        // Set feature status based on Place and Transport status
        setFeatureStatus(placesStatus == PLACES_IS_READY && transportStatus == VEHICLES_IS_READY);

        // Show travel distance if solutions exist
        binding.layoutTextOptimizationTravelDistance.setVisibility(solutionRepository.getRecordsCount() > 0 ? VISIBLE : GONE);

        // Distance count
        double travelDistance = 0;
        for (Solution d : solutionRepository.getRecords())
            travelDistance += d.getDistance();
        String formattedTravelDistance = "";
        int unitStringRes = R.string.measurement_kilometer;
        switch (distanceMeasurementUnit) {
            case METER:
                formattedTravelDistance = NumberFormat.getInstance().format(travelDistance);
                unitStringRes = R.string.measurement_meter;
                break;
            case KILOMETER:
                formattedTravelDistance = NumberFormat.getInstance().format(MeasurementConversion.Companion.metersToKilometers(travelDistance));
                unitStringRes = R.string.measurement_kilometer;
                break;
            case MILE:
                formattedTravelDistance = NumberFormat.getInstance().format(MeasurementConversion.Companion.metersToMiles(travelDistance));
                unitStringRes = R.string.measurement_mile;
                break;
        }
        binding.textOptimizationTravelDistanceValue.setText(getString(unitStringRes, formattedTravelDistance));

        // Show reminder for missing sources and destinations
        binding.cardOptimizationReminder.setVisibility(placesStatus == PLACES_IS_MISSING_SOURCES || placesStatus == PLACES_IS_MISSING_DESTINATIONS ? VISIBLE : GONE);
        binding.buttonOptimizationAddSource.setVisibility(placesStatus == PLACES_IS_MISSING_SOURCES ? VISIBLE : GONE);
        binding.buttonOptimizationAddDestination.setVisibility(placesStatus == PLACES_IS_MISSING_DESTINATIONS ? VISIBLE : GONE);

        // Enable optimize button if places is ready
        binding.buttonOptimizationOptimize.setEnabled(placesStatus == PLACES_IS_READY);

        // Get Started card
        if (solutionRepository.getRecordsCount() == 0) {

            if (placesStatus == PLACES_IS_READY && transportStatus == VEHICLES_IS_READY) {
                binding.cardOptimizationGetStarted.setVisibility(VISIBLE);
            }
            else {
                binding.cardOptimizationGetStarted.setVisibility(GONE);
            }

        }
        else {
            binding.cardOptimizationGetStarted.setVisibility(GONE);
        }

        // Hide filter button for DEFAULT OptimizationMethod
        binding.buttonOptimizationFilter.setVisibility(
                solutionRepository.isUsingAdvancedAlgorithm() == null || !solutionRepository.isUsingAdvancedAlgorithm() ||
                        solutionRepository.getRecordsCount() == 0 ?
                        GONE : VISIBLE
        );

    }

    private void setFeatureStatus(boolean status) {
        if (status) {
            binding.textOptimizationStatus.setText(solutionRepository.getRecordsCount() == 0 ? R.string.status_ready : R.string.status_done);
            binding.imageOptimizationStatusDone.setVisibility(VISIBLE);
            binding.imageOptimizationStatusError.setVisibility(GONE);
            binding.layoutOptimizationStatus.setVisibility(solutionRepository.getRecordsCount() > 0 ? GONE : VISIBLE);
        }
        else {
            binding.textOptimizationStatus.setText(R.string.status_need_action);
            binding.imageOptimizationStatusDone.setVisibility(GONE);
            binding.imageOptimizationStatusError.setVisibility(VISIBLE);
        }

    }

    private void forceShowIntroShowCase() {
        isFeatureFirstTime = true;
        showIntroShowCase();
    }

    private void showIntroShowCase() {

        if (isFeatureFirstTime && binding.cardOptimizationOverview.getVisibility() == VISIBLE) {

            // Save preference
            RouteMatePref.saveString(requireActivity(), RouteMatePref.OPTIMIZATION_FEATURE_IS_FIRST_TIME_KEY, "false");

            // Display showcase
            IntroShowCase.show(
                    requireActivity(),
                    "Optimization Overview",
                    "Contains your optimization summary",
                    binding.cardOptimizationOverview,
                    v -> {

                        if (!isAdded())
                            return;

                        IntroShowCase.show(
                                requireActivity(),
                                "Optimize",
                                "Obtain optimized route here",
                                binding.buttonOptimizationOptimize,

                                v2 -> {

                                    if (!isAdded())
                                        return;

                                    IntroShowCase.show(
                                            requireActivity(),
                                            "Optimization Options",
                                            "Show more optimization options",
                                            binding.buttonOptimizationOptions
                                            );

                                }

                        );

                    }
            );
        }

    }

    private void setPlaceStatus(int status) {
        this.placesStatus = status;

        switch (status) {
            case PLACES_IS_READY:
                // TODO Fill with something
                break;
            case PLACES_IS_NOT_READY:
                // TODO Fill with something
                break;
            case PLACES_IS_MISSING_DESTINATIONS:
                // TODO Fill with something
                break;
            case PLACES_IS_MISSING_SOURCES:
                // TODO Fill with something
                break;
        }
    }

    private void setTransportStatus(int status) {
        this.transportStatus = status;

        switch (status) {
            case VEHICLES_IS_READY:
                // TODO Fill with something
                break;
            case VEHICLES_IS_NOT_READY:
                // TODO Fill with something
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void _105705072022(OnSolutionRepositoryUpdate event) {

        switch (event.getStatus()) {

            case RECORD_ADDED:
            case RECORD_DELETED:
            case RECORDS_CLEARED:
                adapter.notifyDataSetChanged();
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void _052203022023(@NonNull OnUpdateUserSession event) {

        if (
                event.getStatus() != OnUpdateUserSession.Status.SUCCESS ||
                        (event.getEvent() != OnUpdateUserSession.Event.RETRIEVE && event.getEvent() != OnUpdateUserSession.Event.UPDATE)
        )
            return;

        binding.textOptimizationQuota.setText(event.getUserSession().getLastRemainingOptimizationQuota().toString());

    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void _034405122022(OnProgressIndicatorUpdate event) {
        binding.buttonOptimizationOptimize.setEnabled(event.getValue() <= 0);
    }

    // Subscribe to OnBottomSheetStateChanged event
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void _073205062022(OnBottomSheetStateChanged event) {

        switch (event.getState()) {
            case BottomSheetBehavior.STATE_COLLAPSED:
            case BottomSheetBehavior.STATE_HALF_EXPANDED:
                binding.cardOptimizationOverview.setVisibility(VISIBLE);
                break;
            case BottomSheetBehavior.STATE_EXPANDED:
                binding.cardOptimizationOverview.setVisibility(GONE);
                break;
        }

    }

    @Override
    public void onApplyOptimizationFilter(@NonNull Map<String, Integer> filterMap) {

        int vehicleIndex = filterMap.get("vehicleIndex");
        int tripIndex = filterMap.get("tripIndex");

        // TODO Future: Mixable filter
        if (vehicleIndex >= 0) {
            filterSolutionByVehicleIndex(vehicleIndex);
        }
        if (tripIndex >= 0) {
            filterSolutionByTripIndex(tripIndex);
        }
    }

    private void filterSolutionByTripIndex(int tripIndex) {

        // Clear the route lines first, then draw the route lines based on the selected vehicle
        EventBus.getDefault().post(
                new OnMapsViewModelRequest.Builder(OnMapsViewModelRequest.Event.ACTION_CLEAR_ROUTE_LINE)
                        .withMapboxDirectionsRoutes(mapboxDirectionsRouteRepository.getRecords())
                        .build()
        );
        EventBus.getDefault().post(
                new OnMapsViewModelRequest.Builder(OnMapsViewModelRequest.Event.ACTION_DRAW_ROUTE_LINE)
                        .withMapboxDirectionsRoutes(mapboxDirectionsRouteRepository.filterByTripIndex(tripIndex))
                        .build()
        );

        List<Solution> solutions = solutionRepository.filterByTripIndex(tripIndex);
        refreshRecyclerView(solutions);

        updateTravelDistanceFromAdapter();

        binding.buttonOptimizationFilterTrip.setVisibility(VISIBLE);
        binding.buttonOptimizationFilterVehicle.setVisibility(GONE);

    }

    private void filterSolutionByVehicleIndex(int vehicleIndex) {

        Vehicle selectedVehicle = vehicleRepository.getRecords().get(vehicleIndex);

        // Clear the route lines first, then draw the route lines based on the selected vehicle
        EventBus.getDefault().post(
                new OnMapsViewModelRequest.Builder(OnMapsViewModelRequest.Event.ACTION_CLEAR_ROUTE_LINE)
                        .withMapboxDirectionsRoutes(mapboxDirectionsRouteRepository.getRecords())
                        .build()
        );
        EventBus.getDefault().post(
                new OnMapsViewModelRequest.Builder(OnMapsViewModelRequest.Event.ACTION_DRAW_VEHICLE_ROUTE_LINE)
                        .withMapboxDirectionsRoutes(mapboxDirectionsRouteRepository.getRecords())
                        .withVehicle(selectedVehicle)
                        .build()
        );

        // Filter solution based on the selected vehicle (hopefully match the route lines)
        List<Solution> filteredSolution = solutionRepository.filterByVehicleId(selectedVehicle.getId());
        refreshRecyclerView(filteredSolution);

        updateTravelDistanceFromAdapter();

        binding.buttonOptimizationFilterVehicle.setVisibility(VISIBLE);
        binding.buttonOptimizationFilterTrip.setVisibility(GONE);

    }

    private void updateTravelDistanceFromAdapter() {
        double travelDistance = 0;

        for (Solution d : adapter.getObjects())
            travelDistance += d.getDistance();

        binding.textOptimizationTravelDistanceValue.setText(Math.round(travelDistance * 100.0) / 100.0 + " m");
    }

    private void clearFilter() {
        restoreRouteLines();
        restoreAdapter();
    }

    private void restoreAdapter() {

        List<Solution> solutions = solutionRepository.getRecords();
        refreshRecyclerView(solutions);

        updateTravelDistanceFromAdapter();

    }

    private void refreshRecyclerView(List<Solution> solutions) {

        adapter = new SolutionRecViewAdapter(solutions, placeRepository, vehicleRepository, requireActivity(), distanceMeasurementUnit);
        rvSolution.setAdapter(adapter);

    }

    private void restoreRouteLines() {
        // Clear the route lines first, then draw route lines from all available mapboxDirectionsRoute
        EventBus.getDefault().post(
                new OnMapsViewModelRequest.Builder(OnMapsViewModelRequest.Event.ACTION_CLEAR_ROUTE_LINE)
                        .withMapboxDirectionsRoutes(mapboxDirectionsRouteRepository.getRecords())
                        .build()
        );
        EventBus.getDefault().post(
                new OnMapsViewModelRequest.Builder(OnMapsViewModelRequest.Event.ACTION_DRAW_ROUTE_LINE)
                        .withMapboxDirectionsRoutes(mapboxDirectionsRouteRepository.getRecords())
                        .build()
        );
    }

}