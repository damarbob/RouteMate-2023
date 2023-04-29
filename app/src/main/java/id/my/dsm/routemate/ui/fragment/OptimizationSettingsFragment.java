package id.my.dsm.routemate.ui.fragment;

import static android.view.View.GONE;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.my.dsm.routemate.R;
import id.my.dsm.routemate.data.enums.MapsAPI;
import id.my.dsm.routemate.data.model.user.DSMPlan;
import id.my.dsm.routemate.data.model.user.DSMUser;
import id.my.dsm.routemate.data.repo.user.UserRepository;
import id.my.dsm.routemate.databinding.FragmentOptimizationSettingsBinding;
import id.my.dsm.routemate.library.dsmlib.DSMSolver;
import id.my.dsm.routemate.library.dsmlib.enums.DistancesMethod;
import id.my.dsm.routemate.library.dsmlib.enums.OptimizationMethod;
import id.my.dsm.routemate.ui.model.RouteMatePref;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link OptimizationSettingsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
public class OptimizationSettingsFragment extends Fragment {

    private FragmentOptimizationSettingsBinding binding;

    // Dependencies
    @Inject
    UserRepository userRepository;
    @Inject
    DSMSolver dsmSolver;

    public static OptimizationSettingsFragment newInstance() {
        return new OptimizationSettingsFragment();
    }

    @Override
    public void onResume() {
        super.onResume();

        DSMUser user = userRepository.getDsmUser();

        // Disable advanced feature for free user
        boolean isFree = user == null || user.getPlan() == DSMPlan.FREE;
        if (isFree) {
            binding.autocompleteOptimizationMapsAPI.setVisibility(GONE);
            binding.autocompleteOptimizationDistancesMethod.setVisibility(GONE);
            binding.autocompleteOptimizationMethod.setVisibility(GONE);
        }

        // AutoCompleteTextView adapter must be declared inside onResume otherwise it will not show up correctly
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.distances_method, R.layout.list_item_basic);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.autocompleteOptimizationDistancesMethod.setAdapter(adapter);

        ArrayAdapter<CharSequence> adapter3 = ArrayAdapter.createFromResource(getContext(),
                R.array.maps_api, R.layout.list_item_basic);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.autocompleteOptimizationMapsAPI.setAdapter(adapter3);

        ArrayAdapter<CharSequence> adapter2 = ArrayAdapter.createFromResource(getContext(),
                R.array.dsmsolver_optimization_method, R.layout.list_item_basic);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.autocompleteOptimizationMethod.setAdapter(adapter2);

        // Read from preference
        DistancesMethod distancesMethod = RouteMatePref.readDistancesMethod(requireActivity());
        MapsAPI mapsAPI = RouteMatePref.readMapsAPI(requireActivity());
        OptimizationMethod optimizationMethod = RouteMatePref.readOptimizationMethod(requireActivity());
        boolean isRoundTrip = RouteMatePref.readBoolean(requireActivity(), RouteMatePref.OPTIMIZATION_IS_ROUND_TRIP, true);
        boolean isAdvancedAlgorithm = RouteMatePref.readOptimizationIsAdvancedAlgorithm(requireActivity());

        binding.autocompleteOptimizationDistancesMethod.setText(distancesMethod.toString(), false);
        binding.autocompleteOptimizationMapsAPI.setText(mapsAPI.toString(), false);
        binding.autocompleteOptimizationMethod.setText(optimizationMethod.toString(), false);
        binding.autocompleteOptimizationMapsAPI.setEnabled(isAdvancedAlgorithm);
        binding.autocompleteOptimizationDistancesMethod.setEnabled(isAdvancedAlgorithm);
        binding.autocompleteOptimizationMethod.setEnabled(isAdvancedAlgorithm);
        binding.checkBoxOptimizationAllowRoundtrip.setChecked(isRoundTrip);
        binding.checkBoxOptimizationUseAdvancedAlgorithm.setChecked(isAdvancedAlgorithm);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        binding = FragmentOptimizationSettingsBinding.inflate(inflater, container, false);

        // Set up listeners
        binding.buttonOptimizationBack.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).popBackStack(); // Return to backstack
        });
        binding.checkBoxOptimizationUseAdvancedAlgorithm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                binding.autocompleteOptimizationMapsAPI.setEnabled(b);
                binding.autocompleteOptimizationDistancesMethod.setEnabled(b);
                binding.autocompleteOptimizationMethod.setEnabled(b);
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        saveSettings();
    }

    private void saveSettings() {

        // Save settings to preference
        RouteMatePref.saveMethods(
                requireActivity(),
                MapsAPI.fromString(binding.autocompleteOptimizationMapsAPI.getText().toString()),
                DistancesMethod.fromString(binding.autocompleteOptimizationDistancesMethod.getText().toString()),
                OptimizationMethod.fromString(binding.autocompleteOptimizationMethod.getText().toString())
        );
        RouteMatePref.saveBoolean(requireActivity(), RouteMatePref.OPTIMIZATION_IS_ROUND_TRIP, binding.checkBoxOptimizationAllowRoundtrip.isChecked());
        RouteMatePref.saveBoolean(requireActivity(), RouteMatePref.OPTIMIZATION_IS_ADVANCED_ALGORITHM_KEY, binding.checkBoxOptimizationUseAdvancedAlgorithm.isChecked());

    }

}