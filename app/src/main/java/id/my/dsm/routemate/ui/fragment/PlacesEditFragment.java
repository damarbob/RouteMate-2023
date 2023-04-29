package id.my.dsm.routemate.ui.fragment;

import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.my.dsm.routemate.R;
import id.my.dsm.routemate.data.event.viewmodel.OnMapsViewModelRequest;
import id.my.dsm.routemate.data.model.user.DSMPlan;
import id.my.dsm.routemate.data.model.user.DSMUser;
import id.my.dsm.routemate.data.place.Place;
import id.my.dsm.routemate.data.repo.place.PlaceRepositoryN;
import id.my.dsm.routemate.data.repo.user.UserRepository;
import id.my.dsm.routemate.databinding.FragmentPlacesEditBinding;
import id.my.dsm.routemate.library.dsmlib.DSMSolver;
import id.my.dsm.routemate.library.dsmlib.enums.OptimizationMethod;
import id.my.dsm.routemate.library.dsmlib.model.Location;
import id.my.dsm.routemate.library.dsmlib.model.LatLngAlt;
import id.my.dsm.routemate.ui.model.RouteMatePref;
import id.my.dsm.routemate.usecase.userdata.UploadUserDataUseCase;

@AndroidEntryPoint
public class
PlacesEditFragment extends Fragment {

    private static final String TAG = PlacesEditFragment.class.getSimpleName();
    private FragmentPlacesEditBinding binding;

    // Dependencies
    @Inject
    DSMSolver dsmSolver;
    @Inject
    UserRepository userRepository;
    @Inject
    PlaceRepositoryN placeRepository;
    private Place place;
    private Location location;

    // Use case
    @Inject
    UploadUserDataUseCase uploadUserDataUseCase;

    // State
    private boolean isSource = false;
    private boolean enablePlacesDemandConstraint;

    public static PlacesEditFragment newInstance() {
        return new PlacesEditFragment();
    }

    @Override
    public void onResume() {
        super.onResume();

        // AutoCompleteTextView adapter must be declared inside onResume otherwise it will not show up correctly
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getContext(),
                R.array.location_profile,
                R.layout.list_item_basic
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.autocompletePlacesProfile.setAdapter(adapter);

        // Refresh contents
        Location location = place.getDsmPlace();
        boolean isBypassDemands = place.isBypassDemands();

        binding.editTextLatitude.setText(String.valueOf(location.getLatLngAlt().getLatitude()));
        binding.editTextLongitude.setText(String.valueOf(location.getLatLngAlt().getLongitude()));
        binding.editTextPlacesName.setText(place.getName());
        binding.editTextPlacesDescription.setText(place.getDescription());
        binding.editTextPlacesNote.setText(place.getNote());
        binding.checkBoxPlacesDemandConstraint.setChecked(!isBypassDemands);
        binding.autocompletePlacesProfile.setText(place.getLocalizedProfile(requireActivity().getResources()), false);
        if (isBypassDemands) {
            binding.editTextPlacesDemands.setText(String.valueOf(place.getBypassedDemands()));
        }
        else {
            binding.editTextPlacesDemands.setText(String.valueOf(location.getDemands()));
        }
        binding.textInputLayoutDemands.setEnabled(enablePlacesDemandConstraint);

        // Checks if the edited place is source
        if (convertProfileToEnum(String.valueOf(binding.autocompletePlacesProfile.getText())) == Location.Profile.SOURCE) {
            isSource = true;
        }

    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentPlacesEditBinding.inflate(inflater, container, false);

        String placeId = PlacesEditFragmentArgs.fromBundle(getArguments()).getPlaceId();
        place = placeRepository.getById(placeId);
        location = place.getDsmPlace();

        // Adjust layout based on user's plan
        DSMUser dsmUser = userRepository.getDsmUser();
        if (dsmUser != null) {

            DSMPlan plan = dsmUser.getPlan();
            if (plan != DSMPlan.FREE)
                binding.layoutPlacesAdditionalProFeature.setVisibility(VISIBLE);

        }

        // Setup view listeners
        binding.checkBoxPlacesDemandConstraint.setOnCheckedChangeListener((compoundButton, b) -> {
            enablePlacesDemandConstraint = binding.checkBoxPlacesDemandConstraint.isChecked();
            binding.textInputLayoutDemands.setEnabled(enablePlacesDemandConstraint);
        });
        binding.buttonPlacesSave.setOnClickListener(v -> {

            boolean isAdvancedAlgorithm = RouteMatePref.readOptimizationIsAdvancedAlgorithm(requireActivity());
            OptimizationMethod optimizationMethod = RouteMatePref.readOptimizationMethod(requireActivity());

            if (
                    // Add or logic after this line for each new method that don't allow multiple sources
                    (
                            !isAdvancedAlgorithm ||
                            optimizationMethod == OptimizationMethod.NEAREST_NEIGHBOR)
                        &&
                    (placeRepository.getSources().size() > 0 && convertProfileToEnum(String.valueOf(binding.autocompletePlacesProfile.getText())) == Location.Profile.SOURCE)
            ) {
                if (!isSource) {
                    Toast.makeText(requireContext(), "The optimization method does not allow multiple sources", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            // Save new data to current Place object
            place.setName(String.valueOf(binding.editTextPlacesName.getText()));
            place.setDescription(String.valueOf(binding.editTextPlacesDescription.getText()));
            place.setNote(String.valueOf(binding.editTextPlacesNote.getText()));
            location.setLatLngAlt(
                    new LatLngAlt(
                            Double.parseDouble(String.valueOf(binding.editTextLatitude.getText())),
                            Double.parseDouble(String.valueOf(binding.editTextLongitude.getText()))
                    )
            );
            location.setProfile(convertProfileToEnum(String.valueOf(binding.autocompletePlacesProfile.getText())));

            // Whether demand constraint is enabled from the checkbox
            if (enablePlacesDemandConstraint) {
                place.setUnBypassDemands(Double.parseDouble(String.valueOf(binding.editTextPlacesDemands.getText())));
            }
            else {
                place.bypassDemands();
            }

            // After successfully saving data
            Toast.makeText(getActivity(), "Saved successfully!", Toast.LENGTH_SHORT).show();

            EventBus.getDefault().post(new OnMapsViewModelRequest.Builder(OnMapsViewModelRequest.Event.ACTION_RELOAD_PLACES_SYMBOL).build());
            EventBus.getDefault().post(new OnMapsViewModelRequest.Builder(OnMapsViewModelRequest.Event.ACTION_RELOAD_MARKER_VIEWS).build());

            NavHostFragment.findNavController(this).popBackStack(); // Return to backstack

            // Sync changes
            uploadUserDataUseCase.invoke(true, false, false, false, false);

        });
        binding.buttonPlacesBack.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).popBackStack(); // Return to backstack
        });

        return binding.getRoot();
    }

    /**
     * Converts text mapboxProfile to int mapboxProfile for use in {@link Place} instance.
     *
     * @param profileText string place mapboxProfile
     * @return mapboxProfile int
     */
    public Location.Profile convertProfileToEnum(String profileText) {
        Location.Profile profile = Location.Profile.DESTINATION;
        switch(profileText) {
            case "Source":
                profile = Location.Profile.SOURCE;
                break;
            case "Destination":
                profile = Location.Profile.DESTINATION;
                break;
        }

        return profile;
    }
}