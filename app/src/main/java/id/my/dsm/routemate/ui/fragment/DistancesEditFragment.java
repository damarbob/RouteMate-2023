package id.my.dsm.routemate.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import id.my.dsm.routemate.data.repo.distance.DistanceRepositoryN;
import id.my.dsm.routemate.databinding.FragmentDistancesEditBinding;
import id.my.dsm.routemate.library.dsmlib.model.MatrixElement;
import id.my.dsm.routemate.usecase.userdata.UploadUserDataUseCase;

@AndroidEntryPoint
public class DistancesEditFragment extends Fragment {

    private FragmentDistancesEditBinding binding;

    // Dependencies
    @Inject
    DistanceRepositoryN distanceRepository;
    private MatrixElement matrixElement;

    // Use case
    @Inject
    UploadUserDataUseCase uploadUserDataUseCase;

    // State
    private boolean isCreateMode = false;

    public DistancesEditFragment newInstance() {
        return new DistancesEditFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentDistancesEditBinding.inflate(inflater, container, false);

        int distanceIndex = DistancesEditFragmentArgs.fromBundle(getArguments()).getDistanceIndex();
        String[] placesName = DistancesEditFragmentArgs.fromBundle(getArguments()).getPlacesName();

        binding.textDistanceInfo.setText("Editing matrixElement for " + placesName[0] + " to " + placesName[1]);
        binding.checkBoxDistanceSymmetric.setText("Apply symmetric matrixElement for " + placesName[1] + " to " + placesName[0]);

        matrixElement = distanceRepository.getRecordByIndex(distanceIndex);

        // Set listeners
        binding.buttonVehiclesSave.setOnLongClickListener(view -> {

            // For testing purposes TODO: remove when not needed
//            distanceRepository.obtainMapboxDirectionsRoute(distanceIndex, DirectionsCriteria.PROFILE_DRIVING);

            return true;
        });
        binding.buttonVehiclesSave.setOnClickListener(view -> {

            // Save new data
            matrixElement.setDistance(Double.parseDouble(String.valueOf(binding.editTextDistanceValue.getText())));

            // Error counter
            if (String.valueOf(binding.editTextDistanceSavingValue.getText()).equals(""))
                binding.editTextDistanceSavingValue.setText("0");

            matrixElement.setSavingDistance(Double.parseDouble(String.valueOf(binding.editTextDistanceSavingValue.getText())));

            if (binding.checkBoxDistanceSymmetric.isChecked()) {

                for (MatrixElement d : distanceRepository.getRecords()) {

                    if (d.getOrigin().getId().equals(matrixElement.getDestination().getId()) &&
                            d.getDestination().getId().equals(matrixElement.getOrigin().getId())) {

                        d.setDistance(matrixElement.getDistance());
                        d.setSavingDistance(matrixElement.getSavingDistance());

                    }

                };

            }

            // After saving successfully
            Toast.makeText(getActivity(), "MatrixElement saved successfully!", Toast.LENGTH_SHORT).show();

            NavHostFragment.findNavController(this).popBackStack(); // Return to backstack

            // Sync changes
            uploadUserDataUseCase.invoke(false, false, true, false, false);

        });
        binding.buttonVehiclesBack.setOnClickListener(view1 -> {

            NavHostFragment.findNavController(this).popBackStack(); // Return to backstack

        });

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        // Refresh contents
        binding.editTextDistanceValue.setText(String.valueOf(matrixElement.getDistance()));
        binding.editTextDistanceSavingValue.setText(String.valueOf(matrixElement.getSavingDistance()));

    }

}