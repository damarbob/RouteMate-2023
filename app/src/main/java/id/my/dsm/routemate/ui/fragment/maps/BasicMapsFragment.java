package id.my.dsm.routemate.ui.fragment.maps;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapbox.mapboxsdk.maps.MapView;

import id.my.dsm.routemate.databinding.FragmentBasicMapsBinding;
import id.my.dsm.routemate.ui.fragment.viewmodel.MapsViewModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BasicMapsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BasicMapsFragment extends Fragment {

    private FragmentBasicMapsBinding binding;
    private MapView mapView;

    // Dependencies
    private MapsViewModel mapsViewModel;

    public static BasicMapsFragment newInstance(String param1, String param2) {
        return new BasicMapsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentBasicMapsBinding.inflate(inflater, container, false);

        mapsViewModel = new ViewModelProvider(requireActivity()).get(MapsViewModel.class); // Provide ViewModel
        mapsViewModel.provideContext(requireContext());

        // Maps
        mapView = binding.basicMapView;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapsViewModel);
        mapsViewModel.provideMapView(mapView); // Inject dependency to viewModel ASAP

        return binding.getRoot();

    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // Do caching before maps destroyed
        mapsViewModel.cacheCameraPosition();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}