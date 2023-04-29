package id.my.dsm.routemate.ui.fragment;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;

import org.greenrobot.eventbus.EventBus;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.my.dsm.routemate.R;
import id.my.dsm.routemate.data.event.repo.OnRepositoryUpdate;
import id.my.dsm.routemate.data.event.viewmodel.OnMapsViewModelRequest;
import id.my.dsm.routemate.data.model.user.DSMPlan;
import id.my.dsm.routemate.data.repo.user.UserRepository;
import id.my.dsm.routemate.data.repo.vehicle.VehicleRepositoryN;
import id.my.dsm.routemate.databinding.FragmentVehiclesEditBinding;
import id.my.dsm.routemate.data.enums.MapsAPI;
import id.my.dsm.routemate.library.dsmlib.model.Vehicle;
import id.my.dsm.routemate.ui.model.MaterialManager;
import id.my.dsm.routemate.ui.model.RouteMatePref;
import id.my.dsm.routemate.usecase.repository.AlterRepositoryUseCase;
import id.my.dsm.routemate.usecase.userdata.UploadUserDataUseCase;

@AndroidEntryPoint
public class VehiclesEditFragment extends Fragment {

    private FragmentVehiclesEditBinding binding;

    // Dependencies
    @Inject
    UserRepository userRepository;
    @Inject
    VehicleRepositoryN vehicleRepository;
    private Vehicle vehicle;
    private MapsAPI mapsAPI;

    // UI State
    private int vehicleProfileIndex = -1;

    // Use case
    @Inject
    UploadUserDataUseCase uploadUserDataUseCase;
    @Inject
    AlterRepositoryUseCase alterRepositoryUseCase;

    // State
    private boolean isCreateMode = false;

    public VehiclesEditFragment newInstance() {
        return new VehiclesEditFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentVehiclesEditBinding.inflate(inflater, container, false);

        // Read preference
        mapsAPI = RouteMatePref.readMapsAPI(requireActivity());

        // Import values from bundle
        int vehicleIndex = VehiclesEditFragmentArgs.fromBundle(getArguments()).getVehicleIndex();
        vehicle = vehicleRepository.getRecordByIndex(vehicleIndex);

        isCreateMode = VehiclesEditFragmentArgs.fromBundle(getArguments()).getIsCreateMode(); // Check if it's create mode (not editing)

        // Set vehicleProfileIndex depending on which API is currently used
        vehicleProfileIndex = mapsAPI == MapsAPI.MAPBOX ?
                Vehicle.Toolbox.convertMapboxProfileToProfileIndex(vehicle.getMapboxProfile()) :
                Vehicle.Toolbox.convertGoogleProfileToProfileIndex(vehicle.getGoogleProfile());

        // Adjust layout based on plan
        binding.textInputVehiclesCapacity.setVisibility(
                userRepository.getDsmUser() == null ?
                        GONE :
                        userRepository.getDsmUser().getPlan() == DSMPlan.FREE ?
                                GONE :
                                VISIBLE
        );
        binding.layoutVehiclesAdditionalProFeature.setVisibility(
                userRepository.getDsmUser() == null ?
                        GONE :
                        userRepository.getDsmUser().getPlan() == DSMPlan.FREE ?
                                GONE :
                                VISIBLE
        );

        // Set listeners
        binding.buttonVehiclesColor.setOnClickListener(v -> {
            ColorPickerDialogBuilder
                    .with(requireContext())
                    .setTitle("Choose Vehicle Route Color")
                    .initialColor(vehicle.getColor())
                    .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                    .density(12)
                    .setOnColorSelectedListener(selectedColor -> {

                    })
                    .setPositiveButton("Apply", new ColorPickerClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int selectedColor, Integer[] allColors) {
                            MaterialManager.setButtonColor(binding.buttonVehiclesColor, selectedColor);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .build()
                    .show();
        });
        binding.autocompleteVehiclesProfile.setOnItemClickListener((adapterView, view, i, l) -> {
            vehicleProfileIndex = i;
        });
        binding.buttonVehiclesSave.setOnClickListener(view -> {

            // Previous state
            int previousVehicleColor = vehicle.getColor();

            // Save new data to current Vehicle object
            vehicle.setName(String.valueOf(binding.editTextVehiclesName.getText()));
            vehicle.setColor(binding.buttonVehiclesColor.getBackgroundTintList().getDefaultColor());
            vehicle.setCapacity(Double.parseDouble(String.valueOf(binding.editTextVehiclesCapacity.getText())));

            // Apply changes on the map if any
            if (vehicle.getColor() != previousVehicleColor)
                EventBus.getDefault().post(
                        new OnMapsViewModelRequest
                                .Builder(OnMapsViewModelRequest.Event.ACTION_RELOAD_MAP)
                                .build()
                );

            // Translate mapboxProfile to readable string
            String profileText = String.valueOf(binding.autocompleteVehiclesProfile.getText());
            switch (mapsAPI) {
                case GOOGLE:
                    vehicle.setGoogleProfile(Vehicle.Toolbox.convertProfileIndexToGoogleProfile(vehicleProfileIndex));
                    break;
                case MAPBOX:
                    vehicle.setMapboxProfile(Vehicle.Toolbox.convertProfileIndexToMapboxProfile(vehicleProfileIndex));
                    break;
            }

            if (binding.checkBoxVehicleDefault.isChecked()) // Post repository event
                alterRepositoryUseCase.invoke(OnRepositoryUpdate.Event.ACTION_SET_DEFAULT, vehicle, false);

            else
                alterRepositoryUseCase.invoke(OnRepositoryUpdate.Event.ACTION_CLEAR_DEFAULT, vehicle, false);

            // Constraints
            vehicle.setDispatchLimit(Integer.parseInt(String.valueOf(binding.editTextVehiclesDispatchLimit.getText())));

            isCreateMode = false; // Set create mode to false

            // After saving successfully
            Toast.makeText(getActivity(), "Vehicle saved successfully!", Toast.LENGTH_SHORT).show();

            NavHostFragment.findNavController(this).popBackStack(); // Return to backstack

            // Sync vehicle repository
            uploadUserDataUseCase.invoke(false, true, false, false, false);

        });
        binding.buttonVehiclesBack.setOnClickListener(view1 -> {

            NavHostFragment.findNavController(this).popBackStack(); // Return to backstack
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // Delete vehicle if not edited
        if (isCreateMode) {
            vehicleRepository.deleteRecord(vehicle);
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        // AutoCompleteTextView adapter must be declared inside onResume otherwise it will not show up correctly
        ArrayAdapter<CharSequence> mapboxProfileAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.vehicle_mapbox_profile, R.layout.list_item_basic);
        ArrayAdapter<CharSequence> googleProfileAdapter = ArrayAdapter.createFromResource(requireContext(), R.array.vehicle_google_profile, R.layout.list_item_basic);
//        ArrayAdapter<String> mapboxProfileAdapter = new ArrayAdapter<>(requireContext(), R.layout.list_item_basic, Vehicle.MAPBOX_PROFILES);
//        ArrayAdapter<String> googleProfileAdapter = new ArrayAdapter<>(requireContext(), R.layout.list_item_basic, Vehicle.GOOGLE_PROFILES);

        // Adapter decision
        ArrayAdapter<CharSequence> arrayAdapter = mapsAPI == MapsAPI.MAPBOX ? mapboxProfileAdapter : googleProfileAdapter;

        // Set adapter
        binding.autocompleteVehiclesProfile.setAdapter(arrayAdapter);

        // Display the profile depending on the selected MapsAPI
        CharSequence vehicleProfile = mapsAPI == MapsAPI.MAPBOX ? vehicle.getMapboxProfile().toPrettyString(getResources()) : vehicle.getGoogleProfile().toPrettyString(getResources());

        // Refresh contents
        MaterialManager.setButtonColor(binding.buttonVehiclesColor, vehicle.getColor());
        binding.editTextVehiclesName.setText(vehicle.getName());
        binding.editTextVehiclesCapacity.setText(String.valueOf(vehicle.getCapacity()));
        binding.autocompleteVehiclesProfile.setText(vehicleProfile, false);
        binding.checkBoxVehicleDefault.setChecked(vehicleRepository.getRecordsCount() <= 1 ? true : vehicle.isDefault());
        binding.checkBoxVehicleDefault.setEnabled(!(vehicle.isDefault()));
        // Hide checkbox for default vehicle
        binding.checkBoxVehicleDefault.setVisibility(vehicle.isDefault() ? View.GONE : View.VISIBLE);

        binding.editTextVehiclesDispatchLimit.setText(String.valueOf(vehicle.getDispatchLimit()));

    }
}