package id.my.dsm.routemate.ui.fragment.viewmodel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.navigation.NavController;

import com.mapbox.android.gestures.MoveGestureDetector;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import id.my.dsm.routemate.MainActivity;
import id.my.dsm.routemate.R;
import id.my.dsm.routemate.data.event.model.OnMapStyleLoadedEvent;
import id.my.dsm.routemate.data.event.model.OnMapSymbolClickedEvent;
import id.my.dsm.routemate.data.event.model.OnSelectedPlaceChangedEvent;
import id.my.dsm.routemate.data.event.network.OnMapboxDirectionsRouteResponse;
import id.my.dsm.routemate.data.event.repo.OnMapboxDirectionsRouteRepositoryUpdate;
import id.my.dsm.routemate.data.event.repo.OnPlaceRepositoryUpdate;
import id.my.dsm.routemate.data.event.repo.OnRepositoryUpdate;
import id.my.dsm.routemate.data.event.viewmodel.OnMapsViewModelRequest;
import id.my.dsm.routemate.data.model.maps.MapboxDirectionsRoute;
import id.my.dsm.routemate.data.model.place.Place;
import id.my.dsm.routemate.data.repo.distance.DistanceRepositoryN;
import id.my.dsm.routemate.data.repo.distance.SolutionRepositoryN;
import id.my.dsm.routemate.data.repo.fleet.FleetRepository;
import id.my.dsm.routemate.data.repo.mapbox.MapboxDirectionsRouteRepository;
import id.my.dsm.routemate.data.repo.place.PlaceRepositoryN;
import id.my.dsm.routemate.data.repo.user.UserRepository;
import id.my.dsm.routemate.ui.fragment.viewmodel.mapsviewmodel.MapboxDirectionsRouteManager;
import id.my.dsm.routemate.ui.fragment.viewmodel.mapsviewmodel.MarkerViewManagerModel;
import id.my.dsm.routemate.ui.fragment.viewmodel.mapsviewmodel.SymbolManagerModel;
import id.my.dsm.routemate.usecase.repository.AlterRepositoryUseCase;
import id.my.dsm.vrpsolver.model.Location;

// TODO: Hide directions route line
// TODO: Global error message display
@HiltViewModel
public class MapsViewModel extends ViewModel implements
        OnMapReadyCallback,
        MapboxMap.OnMoveListener,
        Style.OnStyleLoaded {

    private static final String TAG = "MapsViewModel";

    // Repositories
    @Inject
    UserRepository userRepository;
    @Inject
    PlaceRepositoryN placeRepository;
    @Inject
    FleetRepository vehicleRepository;
    @Inject
    DistanceRepositoryN distanceRepository;
    @Inject
    SolutionRepositoryN solutionRepository;
    @Inject
    MapboxDirectionsRouteRepository mapboxDirectionsRouteRepository;

    // Dependencies
    @SuppressLint("StaticFieldLeak")
    private Context context;
    private NavController navController;
    @SuppressLint("StaticFieldLeak")
    private MapView mapView;
    private String dayStyle;
    private String nightStyle;
    private MapboxMap mapboxMap;
    private Style mapboxMapStyle;
    private SymbolManager symbolManager;
    private MarkerViewManager markerViewManager;

    // Extension classes
    private MapboxDirectionsRouteManager mapboxDirectionsRouteManager;

    // Use cases
    @Inject
    AlterRepositoryUseCase alterRepositoryUseCase;

    // Session
    private CameraPosition cameraPosition = new CameraPosition.Builder()
            .target(new LatLng(-7.810930, 110.378523))
            .zoom(13)
            .build();

    // State
    private final MutableLiveData<Boolean> isDrawingMode = new MutableLiveData<>(false); // Drawing mode state
    private final MutableLiveData<Boolean> isTrafficStyle = new MutableLiveData<>(true);

    private final MutableLiveData<Place> selectedPlace = new MutableLiveData<>(null);

    // Manager Model
    @Inject
    SymbolManagerModel symbolManagerModel;
    @Inject
    MarkerViewManagerModel markerViewManagerModel;

    @Inject
    public MapsViewModel() {

        EventBus.getDefault().register(this); //

        provideAdaptedStyle(); // Adapt style to state changes

    }

    @Override
    protected void onCleared() {
        super.onCleared();

        EventBus.getDefault().unregister(this); //
    }


    // Dependency providers

    public void provideContext(Context context) {
        this.context = context;

        // Manager Model
        markerViewManagerModel.provideContext(context);
    }

    public void provideNavController(NavController navController) {
        this.navController = navController;
    }

    public void provideMapView(@NonNull MapView mapView) {
        this.mapView = mapView;
    }

    private void provideStyle(@NonNull String dayStyle, @NonNull String nightStyle) {
        this.dayStyle = dayStyle;
        this.nightStyle = nightStyle;
    }

    public void provideMapboxDirectionsRouteManager(MapboxDirectionsRouteManager mapboxDirectionsRouteManager) {
        this.mapboxDirectionsRouteManager = mapboxDirectionsRouteManager;

        // Provide manager model
        mapboxDirectionsRouteManager.provideViewModel(this);
    }

    private void applyProvidedStyle() {

        String style = "";

        int nightModeFlags =
                context.getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                style = nightStyle;
                break;
            case Configuration.UI_MODE_NIGHT_NO:
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                style = dayStyle;
                break;
        }

        mapboxMap.setStyle(style, this);

    }

    public void provideAdaptedStyle() {
        String styleDay;
        String styleNight;

        if (isTrafficStyleValue()) {
            styleDay = Style.TRAFFIC_DAY;
            styleNight = Style.TRAFFIC_NIGHT;
        } else {
            styleDay = Style.MAPBOX_STREETS;
            styleNight = Style.DARK;
        }

        provideStyle(styleDay, styleNight);
    }

    // State

    public LiveData<Boolean> isDrawingMode() {
        return isDrawingMode;
    }

    // Session

    private void setCameraPosition(CameraPosition cameraPosition) {
        this.cameraPosition = cameraPosition;
    }

    private void moveCameraToPlace(Place place) {
        this.cameraPosition = new CameraPosition.Builder()
                .target(place.getMapboxLatLng())
                .build();

        mapboxMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition), 1000);
    }

    /**
     * Save last camera position
     */
    public void cacheCameraPosition() {
        if (mapboxMap != null)
            setCameraPosition(mapboxMap.getCameraPosition());
    }

    public void loadCachedCameraPosition(boolean animate) {
        if (!animate) {
            mapboxMap.setCameraPosition(cameraPosition);
            return;
        }

        mapboxMap.animateCamera(CameraUpdateFactory
                .newCameraPosition(cameraPosition), 1000);
    }

    /**
     * Load cached directionsRoute from {@link MapboxDirectionsRouteRepository} if any.
     */
    public void drawCachedDirectionsRoute() {

        Log.d(TAG, "drawCachedDirectionsRoute: Drawing cached directionsRouteLines for " + mapboxDirectionsRouteRepository.getRecordsCount());
        mapboxDirectionsRouteManager.drawMapboxDirectionsRouteLines(mapboxDirectionsRouteRepository.getRecords());

    }

    // TOGGLES

    public void toggleDrawingMode() {
        isDrawingMode.setValue(!isDrawingMode.getValue());
    }

    public void toggleTrafficStyle() {
        isTrafficStyle.setValue(!isTrafficStyleValue());
        provideAdaptedStyle(); // Prepare to adapt style to state changes

        reloadMap(); // Reload the map
    }

    public void toggleMarkerViewInfo() {
        markerViewManagerModel.setMarkerViewInfoEnabled(!isMarkerInfoEnabledValue());
    }

    public void toggleMarkerLock() {

        boolean lockState = !markerViewManagerModel.isMarkerLockEnabledValue(); // Toggle lock state

        markerViewManagerModel.setMarkerLockEnabled(lockState); // Save lock state
        symbolManagerModel.setPlaceSymbolsFakeDraggable(lockState); // Apply lock state to place symbols

    }

    // CAMERA

    /**
     * Update the projected {@link LatLng} of center view of {@link MapView} object.
     */
    @NonNull
    private LatLng getCenterScreenLatLng() {
        return getLatLngFromScreenLocation(new PointF(
                (float) mapView.getWidth() / 2,
                (float) mapView.getHeight() / 2
        ));
    }

    @NonNull
    private LatLng getLatLngFromScreenLocation(@NonNull PointF point) {
        return mapboxMap.getProjection().fromScreenLocation(point);
    }

    // STATE

    /**
     * Observable isMarkerInfoEnabled
     *
     * @return observable boolean
     */
    @NonNull
    public LiveData<Boolean> isMarkerInfoEnabled() {
        return markerViewManagerModel.isMarkerInfoEnabled();
    }

    @NonNull
    public Boolean isMarkerInfoEnabledValue() {
        return markerViewManagerModel.isMarkerInfoEnabledValue();
    }

    /**
     * Observable isMarkerLockEnabled
     *
     * @return observable boolean
     */
    @NonNull
    public LiveData<Boolean> isMarkerLockEnabled() {
        return markerViewManagerModel.isMarkerLockEnabled();
    }

    /**
     * Observable isTrafficStyle
     *
     * @return observable boolean
     */
    @NonNull
    public LiveData<Boolean> isTrafficStyle() {
        return isTrafficStyle;
    }

    @NonNull
    public Boolean isTrafficStyleValue() {
        return isTrafficStyle.getValue() != null ? isTrafficStyle.getValue() : false;
    }

    /**
     * Sets current place by finding the matching place from given symbol
     *
     * @param symbol {@link Symbol}
     */
    public void setCurrentPlaceFromSymbol(@NonNull Symbol symbol) {

        for (Place place : placeRepository.getRecords()) {

            assert place.getSymbol() != null;
            if (place.getSymbol().equals(symbol)) {
                selectedPlace.setValue(place);
                Log.d(TAG, "setCurrentPlaceFromSymbol: Found symbol in " + place);
            }

        }

        Log.d(TAG, "setCurrentPlaceFromSymbol: End of symbol search ");

    }

    @NonNull
    public LiveData<Place> getSelectedPlace() {
        return selectedPlace;
    }

    // MAP

    /**
     * Cache camera position and reload the map. Basically reloading the fragment itself.
     */
    public void reloadMap() {

        cacheCameraPosition(); // Save last camera position

        Bundle bundle = new Bundle();
        bundle.putBoolean("redrawMapboxDirectionsRouteLines", true);

        navController.navigate(R.id.action_basicMapsFragment_self, bundle);

        Toast.makeText(context, "reloadMap executed on Thread: " + Thread.currentThread(), Toast.LENGTH_SHORT).show();

    }

    // Implemented interface

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        this.mapboxMap = mapboxMap;

        applyProvidedStyle(); // Apply the provided Style

        // Re/assign listener
        mapboxMap.addOnMapClickListener(point -> {

            // TODO: DELETE addOnMapClickListener! Has been replaced by addOnMapLongClickListener
            // Adds marker if drawing mode enabled
            if (isDrawingMode.getValue() != null && isDrawingMode.getValue()) {

                Location.Profile mapboxProfile = placeRepository.getAutoProfile();
                Place place = Place.Toolbox.createObjectiveFromMapboxLatLng(point, mapboxProfile);

                alterRepositoryUseCase.invoke(OnRepositoryUpdate.Event.ACTION_CREATE, place, true);

                return true;

            }

            return false;

        });
        mapboxMap.addOnMapLongClickListener(point -> {
//            Location.Profile mapboxProfile = placeRepository.getAutoProfile();
//            Place place = Place.Toolbox.createObjectiveFromMapboxLatLng(point, mapboxProfile);
//
//            alterRepositoryUseCase.invoke(OnRepositoryUpdate.Event.ACTION_CREATE, place, true);

//            mapboxDirectionsRouteManager.clearMapboxDirectionsRouteLines(mapboxDirectionsRouteRepository.getRecords());


            return true;
        });

        mapboxMap.addOnMoveListener(this);
    }

    @Override
    public void onStyleLoaded(@NonNull Style style) {

        symbolManagerModel.clearPlaceSymbols(); // Clear previous symbol in case of Style change

        // Obtain custom resources for map
        initCustomSymbolIcon(style, context);

        symbolManager = new SymbolManager(mapView, mapboxMap, style);
        symbolManager.setIconAllowOverlap(true);

        markerViewManager = new MarkerViewManager(mapView, mapboxMap);

        // Provide markerViewManager and symbolManager to Manager Model
        symbolManagerModel.provideSymbolManager(symbolManager);
        markerViewManagerModel.provideMarkerViewManager(markerViewManager);

        // Provide style to manager model
        mapboxDirectionsRouteManager.provideMapboxMapStyle(style);

        // Load cached data
        loadCachedCameraPosition(false);

        // Listeners
        symbolManager.addClickListener(symbol -> {
            setCurrentPlaceFromSymbol(symbol);

            // Post OnMapSymbolClickedEvent
            EventBus.getDefault().post(
                    new OnMapSymbolClickedEvent(symbol)
            );

            return true;
        });

        // Post OnMapStyleLoadedEvent
        EventBus.getDefault().post(
                new OnMapStyleLoadedEvent(style)
        );

//        logStyleLayers(style);

        // Reload symbols (if any) that might have lost during config changes
        if (placeRepository.getRecordsCount() == 0) return;

        symbolManagerModel.recreatePlaceSymbols();

        // Draw marker views if enabled
        if (isMarkerInfoEnabledValue()) {
            markerViewManagerModel.drawMarkerViews();
        }

    }

    @Override
    public void onMoveBegin(@NonNull MoveGestureDetector detector) {

        /*
            Set a fake symbol position lock state without changing the actual state.
            In other words, disable dragging temporary using a fake state.
         */
        if (!markerViewManagerModel.isMarkerLockEnabledValue()) {
            symbolManagerModel.setPlaceSymbolsFakeDraggable(false);
        }

    }

    @Override
    public void onMove(@NonNull MoveGestureDetector detector) {

    }

    @Override
    public void onMoveEnd(@NonNull MoveGestureDetector detector) {

        /*
            Enable dragging again after the map move is ended.
            Hence, the actual draggable state of place symbols are never altered.
         */
        if (!(symbolManager == null) && !markerViewManagerModel.isMarkerLockEnabledValue()) {
            symbolManagerModel.setPlaceSymbolsFakeDraggable(true);
        }

    }

    // Resources

    /**
     * Initiate custom symbol icon resources for the map
     *
     * @param style   loaded style from mapbox
     * @param context {@link MainActivity} instance
     */
    private void initCustomSymbolIcon(@NonNull Style style, @NonNull Context context) {

        if (style.getImage("icon-location-on") != null || (style.getLayer("layer-location-on") != null)) {
            Log.e(TAG, "initCustomSymbolIcon: Icon resource already exists!");
            return;
        }

        @SuppressLint("UseCompatLoadingForDrawables") Bitmap iconLocationOn = BitmapUtils.getBitmapFromDrawable(context.getResources().getDrawable(R.drawable.ic_app_map_pointer_x));

        if (iconLocationOn != null) {
            style.addImage("icon-location-on", iconLocationOn);
        }

        SymbolLayer symbolLayer = new SymbolLayer("layer-location-on", "source-location-on");
        symbolLayer.setProperties(PropertyFactory.iconImage("icon-location-on"));
        symbolLayer.setProperties(PropertyFactory.iconAllowOverlap(true));

        style.addLayer(symbolLayer);

    }

    // Location

    public void addMyLocationPlace() {
        MainActivity mainActivity = (MainActivity) context;

        if (!mainActivity.checkLocationPermission())
            return;

        Style loadedMapStyle = mapboxMap.getStyle();

        // Get an instance of the component
        LocationComponent locationComponent = mapboxMap.getLocationComponent();

        // Activate with options
        locationComponent.activateLocationComponent(
                LocationComponentActivationOptions.builder(context, loadedMapStyle).build()
        );

        // Enable to make component visible
        locationComponent.setLocationComponentEnabled(true);
        // Set the component's camera mode
        locationComponent.setCameraMode(CameraMode.TRACKING_GPS);
        // Set the component's render mode
        locationComponent.setRenderMode(RenderMode.COMPASS);

        // My Location
        android.location.Location lastLocation = mapboxMap.getLocationComponent().getLastKnownLocation();
        if (lastLocation == null) {
            Toast.makeText(context, "Location service is not enabled!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create my location Place instance
        LatLng myLocation = new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude());
        Location.Profile myPlaceProfile = placeRepository.getAutoProfile();
        Place myPlace = Place.Toolbox.createObjectiveFromMapboxLatLng(myLocation, myPlaceProfile);

        alterRepositoryUseCase.invoke(OnRepositoryUpdate.Event.ACTION_CREATE, myPlace, true); // Create a place and sync

    }

    // Event listeners

    // Subscribe to OnSelectedPlaceChangedEvent event
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void _021405062022(OnSelectedPlaceChangedEvent event) {
        selectedPlace.postValue(event.getObjective());
    }

    // Subscribe to OnPlaceRepositoryUpdate event
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void _105705072022(@NonNull OnPlaceRepositoryUpdate event) {

        Place place = event.getObjective();

        switch (event.getStatus()) {

            case RECORD_ADDED:
                symbolManagerModel.createPlaceSymbol(place, !markerViewManagerModel.isMarkerLockEnabledValue());
                markerViewManagerModel.drawMarkerView(place); // Add marker view to the map
                break;
            case RECORD_DELETED:
                symbolManagerModel.deleteSymbol(place);
                markerViewManagerModel.recreateMarkerViews(); // Refresh marker views for any changes
                break;
            case RECORDS_CLEARED:
                symbolManagerModel.clearPlacesAndSymbols();
                markerViewManagerModel.clearMarkerViews(); // Clear marker views
                break;

        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void _105705072022(@NonNull OnMapboxDirectionsRouteRepositoryUpdate event) {

        MapboxDirectionsRoute mapboxDirectionsRoute = event.getMapboxDirectionsRoute();

        switch (event.getStatus()) {

            case RECORD_ADDED:
                Log.d(TAG, "OnMapboxDirectionsRouteRepositoryUpdate: Drawing newly added MapboxDirectionsRouteLine");
                mapboxDirectionsRouteManager.drawMapboxDirectionsRouteLine(mapboxDirectionsRoute);
                break;
            case RECORD_DELETED:
                // TODO
                break;
            case RECORDS_CLEARED:
//                Toast.makeText(context, "YES. DirectionsRoutes: " + mapboxDirectionsRouteRepository.getRecordsCount() + ". " + Thread.currentThread(), Toast.LENGTH_SHORT).show();
//                reloadMap();
//                mapboxDirectionsRouteManager.clearMapboxDirectionsRouteLines(mapboxDirectionsRouteRepository.getRecords());
                break;

        }

    }

    // Subscribe to OnMapboxDirectionsRouteResponse event
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void _105705072022(@NonNull OnMapboxDirectionsRouteResponse event) {

        MapboxDirectionsRoute mapboxDirectionsRoute = event.getResponse();

        Log.d(TAG, "OnMapboxDirectionsRouteResponse: mapboxDirectionsRoute received!");

        mapboxDirectionsRouteManager.createMapboxDirectionsRoute(mapboxDirectionsRoute);

        // From here after, MapboxDirectionsRoute ID is available due to createMapboxDirectionsRoute above

    }

    // Subscribe to OnMapsViewModelRequest event
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void _031205062022(@NonNull OnMapsViewModelRequest event) {

        switch (event.getEvent()) {
            case ACTION_CENTER_CAMERA:
                // Used by PlaceRecViewAdapter
                moveCameraToPlace(event.getObjective());
                break;
            case ACTION_CLEAR_ROUTE_LINE:
                // Used by OptimizationViewModel
                Toast.makeText(context, "NO", Toast.LENGTH_SHORT).show();
                mapboxDirectionsRouteManager.clearMapboxDirectionsRouteLines(event.getMapboxDirectionsRoutes());
                break;
            case ACTION_RELOAD_MAP:
                // Used everytime the route color changed
                reloadMap();
                break;
            case ACTION_DRAW_ROUTE_LINE:
                mapboxDirectionsRouteManager.drawMapboxDirectionsRouteLines(event.getMapboxDirectionsRoutes());
                break;
            case ACTION_DRAW_VEHICLE_ROUTE_LINE:
                mapboxDirectionsRouteManager.drawMapboxDirectionsRouteLines(event.getMapboxDirectionsRoutes(), event.getFleet());
                break;
            case ACTION_RELOAD_PLACES_SYMBOL:
                symbolManagerModel.recreatePlaceSymbols();
                break;
            case ACTION_RELOAD_MARKER_VIEWS:
                markerViewManagerModel.recreateMarkerViews();
                break;
        }

    }

}

