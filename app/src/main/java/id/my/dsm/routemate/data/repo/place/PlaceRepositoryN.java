package id.my.dsm.routemate.data.repo.place;

import androidx.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import id.my.dsm.routemate.data.event.repo.OnPlaceRepositoryUpdate;
import id.my.dsm.routemate.data.event.repo.OnRepositoryUpdate;
import id.my.dsm.routemate.data.place.Place;
import id.my.dsm.routemate.data.repo.Repository;
import id.my.dsm.routemate.library.dsmlib.model.Location;

@Module
@InstallIn(SingletonComponent.class)
public class PlaceRepositoryN extends Repository<Place> {

//    private static final String TAG = PlaceRepositoryN.class.getName();

    public PlaceRepositoryN() {
        super();
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onDestroy() {
    }

    @Singleton
    @Provides
    public PlaceRepositoryN getInstance() {
        return new PlaceRepositoryN();
    }

    @Override
    public void onRecordAdded(Place record) {
        EventBus.getDefault().post(
                new OnPlaceRepositoryUpdate(
                        OnRepositoryUpdate.Event.RECORD_ADDED,
                        record
                )
        );
    }

    @Override
    public void onRecordDeleted(Place record) {
        EventBus.getDefault().post(
                new OnPlaceRepositoryUpdate(
                        OnRepositoryUpdate.Event.RECORD_DELETED,
                        record
                )
        );
    }

    @Override
    public void onRecordCleared() {
        EventBus.getDefault().post(
                new OnPlaceRepositoryUpdate(
                        OnRepositoryUpdate.Event.RECORDS_CLEARED
                )
        );
    }

    /**
     * Create a place with complete ready-to-use predefined template data before adding to repository.
     * Hence, prevents duplicate indexes & ids. These data includes index, id, name, description, and note.
     * @param record {@link Place} instance
     */
    @Override
    public void createRecord(@NonNull Place record) {

//        Timestamp timestamp = new Timestamp(System.currentTimeMillis()); // Uses timestamp as id (might be improved in future)

//        record.setIndex(getLastRecordIndex());
//        record.setId("PLACE_" + timestamp.getTime());

        // In case of geocoded places, description and note are not empty. So do not overwrite.
        if (record.getDescription() == null)
            record.setDescription(record.getName() + " is a place where goods distributed");
        if (record.getNote() == null)
            record.setNote("No note");

        addRecord(record);
        record.setName(record.getName() + " (" + getLastRecordIndex() + ")");

    }

    /**
     * Count the destinations
     * @return int
     */
    public int getDestinationsCount() {
        return getDestinations().size();
    }

    /**
     * Count the sources
     * @return int
     */
    public int getSourcesCount() {
        return getSources().size();
    }

    /**
     * Get automatic mapboxProfile. Follows the following rule: SOURCE for 0 place found, DESTINATION for > 0 places found.
     * @return int Place mapboxProfile
     */
    public Location.Profile getAutoProfile() {
        return getRecordsCount() == 0 ? Location.Profile.SOURCE : Location.Profile.DESTINATION; // Set mapboxProfile based on the placeRepository
    }

    /**
     * Get {@link Place} instances that has <i>Source</i> mapboxProfile.
     * @return an {@link List} of {@link Place} instances
     */
    public List<Place> getSources() {
        return Place.Toolbox.getSources(getRecords());
    }

    /**
     * Get {@link Place} instances that has <i>Destination</i> mapboxProfile.
     * @return an {@link List} of {@link Place} instances
     */
    public List<Place> getDestinations() {
        return Place.Toolbox.getDestinations(getRecords());
    }

    /**
     * Get a place only with defined id
     * @param placeId Place id
     * @return List of Places
     */
    public Place getById(String placeId) {
        return Place.Toolbox.getById(getRecords(), placeId);
    }

    /**
     * Get places only with selected profile
     * @param placeProfile int of Place profile
     * @return List of Places
     */
    public List<Place> getByProfile(Location.Profile placeProfile) {
        return Place.Toolbox.getByProfile(getRecords(), placeProfile);
    }

    /**
     * Get LatLng of all places
     * @return List of google-accepted LatLngs
     */
    public List<com.google.maps.model.LatLng> getGoogleLatLngs() {
        return Place.Toolbox.getGoogleLatLngs(getRecords());
    }

    public List<Location> getDSMPlaces() {
        List<Location> locations = new ArrayList<>();

        for (Place o : getRecords())
            locations.add(o.getDsmPlace());

        return locations;
    }

}
