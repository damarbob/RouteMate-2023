package id.my.dsm.routemate.ui.fragment;

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

import org.greenrobot.eventbus.EventBus;

import id.my.dsm.routemate.R;
import id.my.dsm.routemate.data.event.viewmodel.OnMapsViewModelRequest;
import id.my.dsm.routemate.data.model.place.Place;
import id.my.dsm.routemate.databinding.FragmentLocationBinding;
import id.my.dsm.routemate.ui.fragment.viewmodel.LocationViewModel;
import id.my.dsm.routemate.ui.fragment.viewmodel.MapsViewModel;
import id.my.dsm.vrpsolver.model.Location;

public class LocationFragment extends Fragment {

    private static final String TAG = "LocationFragment";
    private FragmentLocationBinding binding;

    // Dependencies
    private MapsViewModel mapsViewModel;
    private LocationViewModel mViewModel;

    public static LocationFragment newInstance() {
        return new LocationFragment();
    }


    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentLocationBinding.inflate(inflater, container, false);

        mapsViewModel = new ViewModelProvider(requireActivity()).get(MapsViewModel.class);
        mViewModel = new ViewModelProvider(this).get(LocationViewModel.class);

        Place place = mapsViewModel.getSelectedPlace().getValue();

        if (place != null) {
            updateFragmentInfo(place);
        }

        // Set listeners
        binding.buttonEditLocation.setOnClickListener(v -> {
            if (place == null)
                return;

            // Set up bundle containing index and navigate to PlacesEditFragment
            Bundle bundle = new Bundle();
            bundle.putString("placeId", place.getId());
            NavHostFragment.findNavController(this).navigate(R.id.action_global_placesEditFragment, bundle);
        });
        binding.buttonLocationCenterTo.setOnClickListener(v -> {
            if (place == null)
                return;

            EventBus.getDefault().post(
                    new OnMapsViewModelRequest.Builder(OnMapsViewModelRequest.Event.ACTION_CENTER_CAMERA)
                            .withObjective(place)
                            .build()
            );
        });

        return binding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    private void updateFragmentInfo(Place place) {

        binding.textLocationName.setText(place.getName());
        binding.textLocationProfile.setText(place.getLocalizedProfile(getResources()));

        Location location = place.getLocation();

        binding.textLocationLatValue.setText("" + location.getLatLngAlt().getAltitude());
        binding.textLocationLongValue.setText("" + location.getLatLngAlt().getLongitude());
        binding.textLocationDemandValue.setText("" + location.getDemands());

        binding.textLocationAboutContent.setText("" + place.getDescription());
        binding.textLocationNoteContent.setText("" + place.getNote());

    }

}