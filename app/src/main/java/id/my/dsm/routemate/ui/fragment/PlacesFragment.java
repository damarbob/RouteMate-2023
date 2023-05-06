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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.NumberFormat;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.my.dsm.routemate.R;
import id.my.dsm.routemate.data.event.repo.OnPlaceRepositoryUpdate;
import id.my.dsm.routemate.data.event.view.OnBottomSheetStateChanged;
import id.my.dsm.routemate.data.event.view.OnMainActivityShowCaseRequest;
import id.my.dsm.routemate.data.model.place.Place;
import id.my.dsm.routemate.data.repo.place.PlaceRepositoryN;
import id.my.dsm.routemate.databinding.FragmentPlacesBinding;
import id.my.dsm.routemate.ui.fragment.dialog.QuickHelpFragment;
import id.my.dsm.routemate.ui.fragment.viewmodel.MapsViewModel;
import id.my.dsm.routemate.ui.fragment.viewmodel.PlacesViewModel;
import id.my.dsm.routemate.ui.model.IntroShowCase;
import id.my.dsm.routemate.ui.model.OptionsMenu;
import id.my.dsm.routemate.ui.model.RouteMatePref;
import id.my.dsm.routemate.ui.recyclerview.PlaceRecViewAdapter;
import id.my.dsm.routemate.usecase.repository.AlterRepositoryUseCase;
import id.my.dsm.vrpsolver.model.Location;

@AndroidEntryPoint
public class
PlacesFragment extends Fragment {

    private static final String TAG = "PlacesFragment";
    private FragmentPlacesBinding binding;

    // Dependencies
    @Inject
    PlaceRepositoryN placeRepository;
    private PlacesViewModel mViewModel;
    private MapsViewModel mapsViewModel;
    private RecyclerView rvPlacesList;
    private PlaceRecViewAdapter adapter;

    // Use case
    @Inject
    AlterRepositoryUseCase alterRepositoryUseCase;

    // State
    private boolean isFeatureReady;
    private boolean isGetStartedFirstTime;
    private boolean isFeatureFirstTime;

    public static PlacesFragment newInstance() {
        return new PlacesFragment();
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
        binding = FragmentPlacesBinding.inflate(inflater, container, false);

        mapsViewModel = new ViewModelProvider(requireActivity()).get(MapsViewModel.class);
        mViewModel = new ViewModelProvider(requireActivity()).get(PlacesViewModel.class);
        Log.d(TAG, "" + mViewModel);

        // Setup layout transition
        binding.layoutPlaces.getLayoutTransition().setAnimateParentHierarchy(false);
        binding.cardPlacesOverviewLayout.getLayoutTransition().setAnimateParentHierarchy(false);

        // Recycler
        rvPlacesList = binding.rvPlacesList;
        rvPlacesList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));

        adapter = new PlaceRecViewAdapter(placeRepository.getRecords(), alterRepositoryUseCase, NavHostFragment.findNavController(this));

        rvPlacesList.setAdapter(adapter);

        // Checks whether its first time
        getIntroShowcaseStatus();

        // Observe state
        placeRepository.getRecordsCountObservable().observe(getViewLifecycleOwner(), count -> {

            this.isFeatureReady = count > 0;

            setFeatureStatus(isFeatureReady);

            binding.buttonPlacesFilterAllPlaces.setText(NumberFormat.getInstance().format(count));
            binding.buttonPlacesFilterSource.setText(NumberFormat.getInstance().format(placeRepository.getSourcesCount()));
            binding.buttonPlacesFilterDestination.setText(NumberFormat.getInstance().format(placeRepository.getDestinationsCount()));

        });

        // Setup view listeners
        binding.buttonPlacesOptions.setOnClickListener(this::showOptionsMenu);
        binding.buttonPlacesMyLocation.setOnClickListener(v -> mapsViewModel.addMyLocationPlace());
        binding.buttonPlacesPickLocation.setOnClickListener(v -> EventBus.getDefault().post(
                new OnMainActivityShowCaseRequest(OnMainActivityShowCaseRequest.Event.MAP)
        ));

        binding.buttonPlacesReminderMissingSourceHelp.setOnClickListener(v -> {
            QuickHelpFragment quickHelpFragment = new QuickHelpFragment();
            Bundle bundle = new Bundle();
            bundle.putString("index", "adding_source");
            quickHelpFragment.setArguments(bundle);
            quickHelpFragment.show(getParentFragmentManager(), null);
        });
        binding.buttonPlacesReminderMissingDestinationHelp.setOnClickListener(v -> {
            QuickHelpFragment quickHelpFragment = new QuickHelpFragment();
            Bundle bundle = new Bundle();
            bundle.putString("index", "adding_places");
            quickHelpFragment.setArguments(bundle);
            quickHelpFragment.show(getParentFragmentManager(), null);
        });

        binding.buttonPlacesFilterAllPlaces.setChecked(true);
        binding.buttonTogglePlacesFilter.addOnButtonCheckedListener((group, checkedId, isChecked) -> {

            if (!isChecked)
                return;

            if (checkedId == R.id.buttonPlacesFilterAllPlaces) {
                adapter.setObjects(placeRepository.getRecords());
                adapter.notifyDataSetChanged();
            }
            else if (checkedId == R.id.buttonPlacesFilterSource) {
                adapter.setObjects(placeRepository.getSources());
                adapter.notifyDataSetChanged();
            }
            else if (checkedId == R.id.buttonPlacesFilterDestination) {
                adapter.setObjects(placeRepository.getDestinations());
                adapter.notifyDataSetChanged();
            }

        });
        binding.buttonPlacesFilterAllPlaces.setOnClickListener(v -> {
        });
        binding.buttonPlacesFilterSource.setOnClickListener(v -> {
        });
        binding.buttonPlacesFilterDestination.setOnClickListener(v -> {

        });

        return binding.getRoot();
    }

    private void getIntroShowcaseStatus() {
        isGetStartedFirstTime = Boolean.parseBoolean(
                RouteMatePref.readString(requireActivity(), RouteMatePref.PLACES_GET_STARTED_IS_FIRST_TIME_KEY, "true")
        );
        isFeatureFirstTime = Boolean.parseBoolean(
                RouteMatePref.readString(requireActivity(), RouteMatePref.PLACES_FEATURE_IS_FIRST_TIME_KEY, "true")
        );
    }

    private void setFeatureStatus(boolean status) {

        binding.cardPlacesOverview.setVisibility(status ? VISIBLE : GONE);
        binding.cardPlacesGetStarted.setVisibility(status ? GONE : VISIBLE);

        // Card Overview
        binding.textPlacesNumber.setText(status ? placeRepository.getRecordsCount() + " places" : "No places added");

        // Card Overview Event
        boolean isSourceAvailable = (placeRepository.getSourcesCount() > 0);
        boolean isDestinationAvailable = (placeRepository.getDestinationsCount() > 0);

        binding.cardPlacesReminderMissingSource.setVisibility(!isSourceAvailable && isDestinationAvailable ? VISIBLE : GONE);
        binding.cardPlacesReminderMissingDestination.setVisibility(!isDestinationAvailable && isSourceAvailable ? VISIBLE : GONE);

        // Place Filters
        binding.layoutPlacesFilter.setVisibility(status ? VISIBLE : GONE);

        showIntroShowCase();

    }

    private void forceShowIntroShowCase() {
        isFeatureFirstTime = true;
        showIntroShowCase();
    }

    private void showIntroShowCase() {

        if (isGetStartedFirstTime && binding.cardPlacesGetStarted.getVisibility() == VISIBLE) {
            isGetStartedFirstTime = false;

            // Save preference
            RouteMatePref.saveString(requireActivity(), RouteMatePref.PLACES_GET_STARTED_IS_FIRST_TIME_KEY, "false");

            // Display showcase
            IntroShowCase.show(
                    requireActivity(),
                    "My Location",
                    "Mark your live location as a Start Location",
                    binding.buttonPlacesMyLocation,
                    v -> {

                        if (!isAdded())
                            return;

                        IntroShowCase.show(
                                requireActivity(),
                                "Pick Location",
                                "Turn on Drawing Mode and pick a Start Location manually",
                                binding.buttonPlacesPickLocation
                        );
                    }
            );
        }

        if (isFeatureFirstTime && binding.cardPlacesOverview.getVisibility() == VISIBLE) {
            isFeatureFirstTime = false;

            // Save preference
            RouteMatePref.saveString(requireActivity(), RouteMatePref.PLACES_FEATURE_IS_FIRST_TIME_KEY, "false");

            // Display showcase
            IntroShowCase.show(
                    requireActivity(),
                    "Places Overview",
                    "General information about your Places",
                    binding.cardPlacesOverview,
                    v -> {

                        if (!isAdded())
                            return;

                        IntroShowCase.show(
                                requireActivity(),
                                "Place",
                                "Tap to edit the place, long tap to display options",
                                binding.rvPlacesList.getChildAt(0).findViewById(R.id.cardPlacesList),

                                v2 -> {

                                    if (!isAdded())
                                        return;

                                    IntroShowCase.show(
                                            requireActivity(),
                                            "Options",
                                            "Show more place options",
                                            binding.buttonPlacesOptions
                                    );

                                }

                        );
                    }
            );
        }

    }

    private void showOptionsMenu(View anchorView) {

        StringBuilder stringBuilder = new StringBuilder("");

        for (Place p : placeRepository.getRecords()) {

            Location location = p.getLocation();

            stringBuilder.append(location.getLatLngAlt().getLatitude());
            stringBuilder.append(",");
            stringBuilder.append(location.getLatLngAlt().getLongitude());

            if (placeRepository.getRecords().indexOf(p) < placeRepository.getRecordsCount() - 1)
                stringBuilder.append("|");
        }

        Log.e(TAG, stringBuilder.toString());

        // Show places menu options
        OptionsMenu optionsMenu = new OptionsMenu(getContext(), anchorView, R.menu.menu_popup_places);

        optionsMenu.getPopupMenu().setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.places_clear:
                    new MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Clear Places")
                            .setMessage("This will clear places on the map and clear the list.")
                            .setPositiveButton("Continue", (dialogInterface, i) -> {
                                mViewModel.clearPlaces();
                            })
                            .setNeutralButton("Cancel", (dialogInterface, i) -> {
                                dialogInterface.cancel();
                            })
                            .show();

                    break;
                case R.id.places_show_hint:
                    forceShowIntroShowCase();
                    break;
            }
            return true;
        });

        optionsMenu.show(); // Show options menu
    }

    // Subscribe to OnPlaceRepositoryUpdate event
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void _105705072022(OnPlaceRepositoryUpdate event) {

        switch (event.getStatus()) {

            case RECORD_ADDED:
            case RECORD_DELETED:
            case RECORDS_CLEARED:
                adapter.notifyDataSetChanged();
                break;
        }
    }

    // Subscribe to OnBottomSheetStateChanged event
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void _073205062022(OnBottomSheetStateChanged event) {

        switch (event.getState()) {
            case BottomSheetBehavior.STATE_COLLAPSED:
            case BottomSheetBehavior.STATE_HALF_EXPANDED:
                if (isFeatureReady)
                    binding.cardPlacesOverview.setVisibility(VISIBLE);
                break;
            case BottomSheetBehavior.STATE_DRAGGING:
                break;
            case BottomSheetBehavior.STATE_EXPANDED:
                if (isFeatureReady)
                    binding.cardPlacesOverview.setVisibility(GONE);
                break;
            case BottomSheetBehavior.STATE_HIDDEN:
                break;
            case BottomSheetBehavior.STATE_SETTLING:
                break;
        }

    }

}