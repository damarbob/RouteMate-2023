package id.my.dsm.routemate.ui.fragment.maps;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import javax.inject.Inject;

import id.my.dsm.routemate.data.repo.mapbox.MapboxDirectionsRouteRepository;
import id.my.dsm.routemate.databinding.FragmentBasicMapsBinding;
import id.my.dsm.routemate.ui.fragment.viewmodel.MapsViewModel;

public class BasicMapsFragment extends Fragment {

    private static final String TAG = BasicMapsFragment.class.getSimpleName();
    private FragmentBasicMapsBinding binding;
    private MapView mapView;

    // Dependencies
    private MapsViewModel mapsViewModel;

    @Inject
    MapboxDirectionsRouteRepository mapboxDirectionsRouteRepository;

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

        assert getArguments() != null;
        boolean redrawMapboxDirectionsRouteLines = getArguments().getBoolean("redrawMapboxDirectionsRouteLines");

        // Check if directionsRoute needs to be redrawn by request from reloadMap()
//        if (redrawMapboxDirectionsRouteLines)
            mapView.addOnDidFinishLoadingStyleListener(() -> mapsViewModel.drawCachedDirectionsRoute());

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