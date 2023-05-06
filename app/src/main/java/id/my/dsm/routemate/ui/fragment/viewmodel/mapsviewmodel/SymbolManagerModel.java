package id.my.dsm.routemate.ui.fragment.viewmodel.mapsviewmodel;

import android.util.Log;

import androidx.annotation.NonNull;

import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import id.my.dsm.routemate.data.model.place.Place;
import id.my.dsm.routemate.data.repo.place.PlaceRepositoryN;

public class SymbolManagerModel {


    private static final String TAG = SymbolManagerModel.class.getSimpleName();

    private PlaceRepositoryN placeRepository;
    private SymbolManager symbolManager;

    @Inject
    public SymbolManagerModel(PlaceRepositoryN placeRepository) {
        this.placeRepository = placeRepository;
    }

    // PROVIDER

    public void provideSymbolManager(SymbolManager symbolManager) {
        this.symbolManager = symbolManager;
    }


    // FUNCTIONALITY

    /**
     * Draw symbol for the given {@link Place} instance and assign to it.
     *
     * @param place {@link Place} instance
     */
    public void createPlaceSymbol(Place place, boolean draggable) {

        // Declare a new Symbol object
        Symbol symbol = symbolManager.create(new SymbolOptions()
                .withLatLng(place.getMapboxLatLng())
                .withIconImage("icon-location-on")
                .withIconSize(1.0f)
                .withDraggable(draggable) // Former value: !isMarkerLockEnabledValue()
        );

        place.setSymbol(symbol);

    }

    public void deleteSymbol(Place place) {
        symbolManager.delete(place.getSymbol());
    }

    /**
     * Recreate place symbols to match latest {@link SymbolManager}
     * Former reloadPlaceSymbols
     */
    public void recreatePlaceSymbols() {

        List<Place> records = placeRepository.getRecords(); // Symbols are taken from placeRepository

        if (!checkPlaceSymbolsUniformDraggability(records)) {
            Log.e(TAG, "recreatePlaceSymbols: Place symbol draggability states are not uniform!");
            return;
        }

        /*
            Takes the depot or the first place for draggability reference
         */
        Place referencePlace = records.get(0);

        Symbol referenceSymbol = referencePlace.getSymbol();
        if (referenceSymbol == null) {
            Log.e(TAG, "reloadPlaceSymbols: Referenced place symbol is null!");
            return;
        }

        recreatePlaceSymbols(records, referenceSymbol.isDraggable());

    }

    /**
     * Recreate place symbols based on the provided Place instances and draggability state
     *
     * @param places ArrayList of Place instances
     * @param draggable whether the icons are draggable
     */
    public void recreatePlaceSymbols(List<Place> places, boolean draggable) {
        clearPlaceSymbols();
        createPlaceSymbols(places, draggable);
    }

    /**
     * Used for error checking purposes. Place symbols are supposed to be uniform in draggable state.
     *
     * @param records ArrayList of Place instances
     * @return
     */
    private boolean checkPlaceSymbolsUniformDraggability(@NonNull List<Place> records) {

        ArrayList<Boolean> draggable = new ArrayList<>();

        Boolean firstDraggable = null; // For comparison reference

        // Populate draggable states
        for (Place o : records) {

            Symbol symbol = o.getSymbol();

            if (symbol != null) {
                draggable.add(symbol.isDraggable());

                if (firstDraggable == null)
                    firstDraggable = symbol.isDraggable();

            }

        }

        if (firstDraggable == null) {
            Log.e(TAG, "checkPlaceSymbolsUniformDraggability: Places do not contain symbols!");
            return false;
        }

        if (draggable.contains(!firstDraggable)) {
            Log.e(TAG, "checkPlaceSymbolsUniformDraggability: Place symbols draggability is not uniform!");
            return false;
        }

        return true;

    }

    // TODO: Move to MapsVM
    /**
     * Clears all {@link Place} instances and its {@link Symbol} available on the map
     */
    public void clearPlacesAndSymbols() {
        clearPlaceSymbols();
    }

    /**
     * Clears all {@link Symbol} instances available on the map
     */
    public void clearPlaceSymbols() {
        if (symbolManager != null)
            symbolManager.deleteAll();
    }

    // SYMBOL

    /**
     * Create place symbols based on the provided Place instances
     * Former createPlacesSymbol
     *
     * @param places ArrayList of Place instances
     * @param draggable whether the icons are draggable
     */
    public void createPlaceSymbols(@NonNull List<Place> places, boolean draggable) {

        for (Place place : places) {

            // Declare a new Symbol object
            Symbol symbol = symbolManager.create(new SymbolOptions()
                    .withLatLng(place.getMapboxLatLng())
                    .withIconImage("icon-location-on")
                    .withIconSize(1.0f)
                    .withDraggable(draggable)
            );

            // Replace symbol to a new one to adapt with the new symbolManager
            place.setSymbol(symbol);

            // TODO: Post specific event

            // Update last index for Places
            // If user is logged, lastLocationIndex is updated in mapPlacesSymbol()
//            Log.d(TAG, "addPlacesSymbol(): LastLocationIndex: " + place.getIndex());

        }

    }

    /**
     * Map symbols to the newly retrieved places from database to adapt new {@link SymbolManager} instance.
     * Former mapPlacesSymbol
     * @param newPlaces {@link ArrayList} of {@link Place}
     */
    public void mapPlaceSymbols(@NonNull ArrayList<Place> newPlaces, boolean draggable) {

        List<Place> records = placeRepository.getRecords();
        records.addAll(newPlaces); // Store the new places into DSMSolver places list

        createPlaceSymbols(records, draggable);

    }

    /**
     * Set the {@link Place}(s) {@link Symbol} whether should be draggable without changing its state.
     * <p>
     *     Refresh the {@link Symbol} instances placed on the map by re-adding the entire symbol to the map.
     * </p>
     *
     * @param draggable whether should be draggable or not
     */
    public void setPlaceSymbolsFakeDraggable(boolean draggable) {

        clearPlaceSymbols(); // Delete all places symbol

        List<Place> records = placeRepository.getRecords();
        createPlaceSymbols(records, draggable);

    }

    /**
     * Whether the {@link Place}(s) {@link Symbol} is fake draggable.
     * TODO: Fix last line
     * @return boolean
     */
    public boolean isPlaceSymbolsFakeDraggable() {
        if (placeRepository.getRecords().size() == 0) {
            Log.e(TAG, "isPlaceSymbolsFakeDraggable: No places!");
            return false;
        }
        return placeRepository.getRecords().get(0).getSymbol().isDraggable();
    }

}