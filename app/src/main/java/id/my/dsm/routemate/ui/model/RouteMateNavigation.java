package id.my.dsm.routemate.ui.model;

import android.os.Bundle;

import androidx.navigation.NavController;

import id.my.dsm.routemate.R;

public class RouteMateNavigation {

    public static void navigateToVehiclesEdit(NavController navController, int vehicleIndex, boolean isCreateMode) {
        // Set up bundle containing index and navigate to VehiclesEditFragment
        Bundle bundle = new Bundle();
        bundle.putInt("vehicleIndex", vehicleIndex);
        bundle.putBoolean("isCreateMode", isCreateMode);
        navController.navigate(R.id.action_global_vehiclesEditFragment, bundle);
    }

    public static void navigateToVehiclesEdit(NavController navController, int vehicleIndex) {
        // Set up bundle containing index and navigate to VehiclesEditFragment
        Bundle bundle = new Bundle();
        bundle.putInt("vehicleIndex", vehicleIndex);
        navController.navigate(R.id.action_global_vehiclesEditFragment, bundle);
    }

}
