package id.my.dsm.routemate.ui.fragment.viewmodel;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;

import androidx.lifecycle.ViewModel;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.maps.GeoApiContext;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import id.my.dsm.routemate.data.event.viewmodel.OnDistancesViewModelRequest;
import id.my.dsm.routemate.data.repo.distance.DistanceRepositoryN;
import id.my.dsm.routemate.data.repo.place.PlaceRepositoryN;
import id.my.dsm.routemate.data.repo.vehicle.VehicleRepositoryN;
import id.my.dsm.routemate.library.dsmlib.DSMSolver;
import id.my.dsm.routemate.library.dsmlib.enums.DistancesMethod;
import id.my.dsm.routemate.data.enums.MapsAPI;
import id.my.dsm.routemate.ui.model.RouteMatePref;
import id.my.dsm.routemate.usecase.distance.RequestDistanceMatrixUseCase;

@HiltViewModel
public class DistancesViewModel extends ViewModel {

    private static final String TAG = "DistancesViewModel";

    // Dependencies
    @Inject
    GeoApiContext geoApiContext;
    @Inject
    DSMSolver dsmSolver;
    @Inject
    DistanceRepositoryN distanceRepository;
    @Inject
    PlaceRepositoryN placeRepository;
    @Inject
    VehicleRepositoryN vehicleRepository;
    @Inject
    RequestDistanceMatrixUseCase requestDistanceMatrixUseCase;
    @SuppressLint("StaticFieldLeak")
    private Context context;

    @Inject
    public DistancesViewModel() {
        EventBus.getDefault().register(this); //
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        EventBus.getDefault().unregister(this); //
    }

    // Dependency provider

    public void provideContext(Context context) {
        this.context = context;
    }

    // Subscribe to OnDistancesViewModelRequest event
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void _031205062022(OnDistancesViewModelRequest event) {

        switch (event.getEvent()) {
            case ACTION_RESOLVE_MATRIX:
                // Resolve matrix, currently used by Optimization UI
                resolveMatrix(event.isContinueOptimization());
                break;
        }

    }

    // Resolve distance matrix based on the given places and method
    private void resolveMatrix(Boolean continueOptimization) {

        // Read optimization method from preference
        DistancesMethod distancesMethod = RouteMatePref.readDistancesMethod((Activity) context);
        MapsAPI mapsAPI = RouteMatePref.readMapsAPI((Activity) context);

        // Error prevention for Google Matrix API
        if (distancesMethod == DistancesMethod.DIRECTIONS && mapsAPI == MapsAPI.GOOGLE) {
            new MaterialAlertDialogBuilder(context)
                    .setTitle("Unable To Calculate Matrix")
                    .setMessage("The Google Matrix API does not support DIRECTIONS distance calculation method. Please switch to TRAVEL in order to continue using Google Matrix API. Otherwise, switch the provider to Mapbox.")
                    .setNeutralButton("Close", null)
                    .show();
            return;
        }

        requestDistanceMatrixUseCase.invoke(distancesMethod, mapsAPI, geoApiContext, continueOptimization);
    }

}