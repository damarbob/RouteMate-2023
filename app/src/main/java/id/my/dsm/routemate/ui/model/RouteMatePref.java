package id.my.dsm.routemate.ui.model;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import id.my.dsm.routemate.data.enums.MapsAPI;
import id.my.dsm.routemate.library.dsmlib.enums.DistancesMethod;
import id.my.dsm.routemate.library.dsmlib.enums.OptimizationMethod;

public class RouteMatePref {

    public static final String APPLICATION_THEME_KEY = "ApplicationTheme";
    public static final String APPLICATION_THEME_VALUE_AUTO = "auto";
    public static final String APPLICATION_THEME_VALUE_LIGHT = "light";
    public static final String APPLICATION_THEME_VALUE_DARK = "dark";
    public static final String MAPS_API_KEY = MapsAPI.class.getName();
    public static final String DISTANCES_METHOD_KEY = DistancesMethod.class.getName();
    public static final String OPTIMIZATION_METHOD_KEY = OptimizationMethod.class.getName();
    public static final String OPTIMIZATION_IS_ADVANCED_ALGORITHM_KEY = "OptimizationIsAdvancedAlgorithm";
    public static final String HOME_GET_STARTED_IS_FIRST_TIME_KEY = "HomeGetStartedIsFirstTime";
    public static final String HOME_MENU_LAYOUT_GRID_KEY = "HomeMenuLayoutGrid";
    public static final String PLACES_FEATURE_IS_FIRST_TIME_KEY = "MainMenuItemPlacesFeatureIsFirstTime";
    public static final String PLACES_GET_STARTED_IS_FIRST_TIME_KEY = "MainMenuItemPlacesGetStartedIsFirstTime";
    public static final String VEHICLES_FEATURE_IS_FIRST_TIME_KEY = "MainMenuItemVehiclesFeatureIsFirstTime";
    public static final String VEHICLES_GET_STARTED_DISMISS_KEY = "MainMenuItemVehiclesGetStartedDismiss";
    public static final String OPTIMIZATION_FEATURE_IS_FIRST_TIME_KEY = "MainMenuItemOptimizationFeatureIsFirstTime";
    public static final String OPTIMIZATION_IS_ROUND_TRIP = "OptimizationIsRoundTrip";
    public static final String MEASUREMENT_DISTANCE_UNIT = DistanceMeasurementUnit.class.getSimpleName();

    public static String readString(Activity activity, String key) {
        return activity.getPreferences(Context.MODE_PRIVATE).getString(key, null);
    }

    public static String readString(Activity activity, String key, String defaultValue) {
        return activity.getPreferences(Context.MODE_PRIVATE).getString(key, defaultValue);
    }

    public static boolean readBoolean(Activity activity, String key, boolean defaultValue) {
        return activity.getPreferences(Context.MODE_PRIVATE).getBoolean(key, defaultValue);
    }

    public static DistanceMeasurementUnit readMeasurementDistanceUnit(Activity activity) {
        String distanceMeasurementUnit = readString(activity, MEASUREMENT_DISTANCE_UNIT, DistanceMeasurementUnit.KILOMETER.name());
        return DistanceMeasurementUnit.valueOf(distanceMeasurementUnit);
    }

    public static MapsAPI readMapsAPI(Activity activity) {
        String mapsAPI = readString(activity, MAPS_API_KEY, MapsAPI.MAPBOX.name());
        return MapsAPI.valueOf(mapsAPI);
    }

    public static OptimizationMethod readOptimizationMethod(Activity activity) {
        String optimizationMethod = readString(activity, OPTIMIZATION_METHOD_KEY, OptimizationMethod.NEAREST_NEIGHBOR.name());
        return OptimizationMethod.valueOf(optimizationMethod);
    }

    public static DistancesMethod readDistancesMethod(Activity activity) {
        String distancesMethod = readString(activity, DISTANCES_METHOD_KEY, DistancesMethod.TRAVEL.name());
        return DistancesMethod.valueOf(distancesMethod);
    }

    public static Boolean readOptimizationIsAdvancedAlgorithm(Activity activity) {
        return readBoolean(activity, OPTIMIZATION_IS_ADVANCED_ALGORITHM_KEY, true);
    }

    public static void saveMethods(Activity activity, MapsAPI mapsAPI, DistancesMethod distancesMethod, OptimizationMethod optimizationMethod) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(MAPS_API_KEY, mapsAPI.name());
        editor.putString(DISTANCES_METHOD_KEY, distancesMethod.name());
        editor.putString(OPTIMIZATION_METHOD_KEY, optimizationMethod.name());
        editor.apply();
    }

    public static void saveString(Activity activity, String key, String value) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void saveBoolean(Activity activity, String key, boolean value) {
        SharedPreferences sharedPref = activity.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

}
