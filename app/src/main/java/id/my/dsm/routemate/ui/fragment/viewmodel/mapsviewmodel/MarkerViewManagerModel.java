package id.my.dsm.routemate.ui.fragment.viewmodel.mapsviewmodel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.markerview.MarkerView;
import com.mapbox.mapboxsdk.plugins.markerview.MarkerViewManager;

import java.util.ArrayList;

import javax.inject.Inject;

import id.my.dsm.routemate.R;
import id.my.dsm.routemate.data.place.Place;
import id.my.dsm.routemate.data.repo.place.PlaceRepositoryN;

public class MarkerViewManagerModel {

    private static final String TAG = MarkerViewManagerModel.class.getName();
    private Context context;

    private PlaceRepositoryN placeRepository;

    final ArrayList<MarkerView> markerViews = new ArrayList<>(); // MarkerView container
    final MutableLiveData<Boolean> isMarkerInfoEnabled = new MutableLiveData<>(true);
    final MutableLiveData<Boolean> isMarkerLockEnabled = new MutableLiveData<>(true);

    private MarkerViewManager markerViewManager;

    @Inject
    public MarkerViewManagerModel(PlaceRepositoryN placeRepository) {
        this.placeRepository = placeRepository;
    }

    // PROVIDER

    public void provideContext(Context context) {
        this.context = context;
    }

    public void provideMarkerViewManager(MarkerViewManager markerViewManager) {
        this.markerViewManager = markerViewManager;
    }

    // STATE

    public ArrayList<MarkerView> getMarkerViews() {
        return markerViews;
    }

    public LiveData<Boolean> isMarkerInfoEnabled() {
        return isMarkerInfoEnabled;
    }

    @NonNull
    public Boolean isMarkerInfoEnabledValue() {
        return isMarkerInfoEnabled.getValue() != null ? isMarkerInfoEnabled.getValue() : false;
    }

    public LiveData<Boolean> isMarkerLockEnabled() {
        return isMarkerLockEnabled;
    }

    /**
     * Set the {@link Place}(s) {@link Symbol} whether should be draggable.
     * <p>
     *     Done by setting the fake draggable state and apply to the original state.
     * </p>
     *
     * @param enabled whether should be draggable or not
     */
    public void setMarkerLockEnabled(boolean enabled) {
        isMarkerLockEnabled.setValue(enabled);
    }

    /**
     * Whether the {@link Place}(s) {@link Symbol} is draggable.
     * @return boolean
     */
    @NonNull
    public Boolean isMarkerLockEnabledValue() {
        return isMarkerLockEnabled.getValue() != null ? isMarkerLockEnabled.getValue() : false;
    }

    // MARKER VIEW

    /**
     * Displays marker views on {@link Place} instance
     */
    public void drawMarkerViews() {
        setMarkerViewInfoEnabled(true);
    }

    /**
     * Clears all marker views on {@link Place} instance
     */
    public void clearMarkerViews() {
        setMarkerViewInfoEnabled(false);
    }

    /**
     * Set all {@link Place}(s) {@link MarkerView} visibility.
     * <p>
     *     Done by removing all {@link MarkerView}(s) exist in the map by iterating through markerViews ArrayList.
     *     Then, adding {@link MarkerView}(s) to the map again.
     * </p>
     *
     * @param enable boolean value whether MarkerViews should be visible or not
     */
    public void setMarkerViewInfoEnabled(boolean enable) {
        isMarkerInfoEnabled.setValue(enable);

        if (!enable) {
            // Remove all MarkerViews exist in the map by iterating through markerViews ArrayList
            for (int i = 0; i < markerViews.size(); i++) {
                markerViewManager.removeMarker(markerViews.get(i));
            }

            markerViews.clear();// Clear the markerViews ArrayList
        }
        else {
            // Add MarkerViews to the map again
            for (int i = 0; i < placeRepository.getRecords().size(); i++) {
                drawMarkerView(placeRepository.getRecords().get(i));
            }
        }
    }

    /**
     * Display a objective-specific marker view at objective's lat & long using custom view.
     * TODO: Move to MapsVM
     *
     * @param place {@link Place} instance reference
     */
    public void drawMarkerView(@NonNull Place place) {

        if (!isMarkerInfoEnabledValue()) {
            Log.w(TAG, "drawMarkerView: Unable to draw marker, Marker info is disabled!");
            return;
        }

        // Use an XML layout to create a View object
        @SuppressLint("InflateParams") View customView = LayoutInflater.from(context).inflate(
                R.layout.bubble_view_marker, null);
        customView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Inject objective's variables into text views
        TextView textName = customView.findViewById(R.id.textName);
        TextView textDescription = customView.findViewById(R.id.textDescription);

        textDescription.setText(place.getDescription());
        textName.setText(place.getName());

        // Finally add marker into marker view manager
        MarkerView markerView = new MarkerView(place.getMapboxLatLng(), customView);
        markerViewManager.addMarker(markerView);

        markerViews.add(markerView);

    }

    /**
     * Reload all {@link MarkerView}(s) in the map.
     * Former reloadMarkerViews
     */
    public void recreateMarkerViews() {
        if (isMarkerInfoEnabledValue()) {
            clearMarkerViews();
            drawMarkerViews();
        }
    }

}
