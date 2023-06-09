package id.my.dsm.routemate.data.repo.mapbox;

import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import id.my.dsm.routemate.data.event.repo.OnMapboxDirectionsRouteRepositoryUpdate;
import id.my.dsm.routemate.data.event.repo.OnRepositoryUpdate;
import id.my.dsm.routemate.data.event.viewmodel.OnMapsViewModelRequest;
import id.my.dsm.routemate.data.model.maps.MapboxDirectionsRoute;
import id.my.dsm.routemate.data.repo.Repository;

@Module
@InstallIn(SingletonComponent.class)
public class MapboxDirectionsRouteRepository extends Repository<MapboxDirectionsRoute> {

    private static final String TAG = MapboxDirectionsRouteRepository.class.getSimpleName();

    @Singleton
    @Provides
    public MapboxDirectionsRouteRepository getInstance() {
        return new MapboxDirectionsRouteRepository();
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void createRecord(MapboxDirectionsRoute record) {

        String uUid = UUID.randomUUID().toString(); // Create unique ID
        record.setId(uUid); // Set unique ID

        addRecord(record);

        Log.d(TAG, "createRecord: a new MapboxDirectionsRoute instance created with ID: " + record.getId());

    }

    @Override
    public void onRecordAdded(MapboxDirectionsRoute record) {
        EventBus.getDefault().post(
                new OnMapboxDirectionsRouteRepositoryUpdate(
                        OnRepositoryUpdate.Event.RECORD_ADDED,
                        record
                )
        );
    }

    @Override
    public void onRecordDeleted(MapboxDirectionsRoute record) {
        EventBus.getDefault().post(
                new OnMapboxDirectionsRouteRepositoryUpdate(
                        OnRepositoryUpdate.Event.RECORD_DELETED,
                        record
                )
        );
    }

    @Override
    public void onRecordCleared() {
        EventBus.getDefault().post(
                new OnMapboxDirectionsRouteRepositoryUpdate(
                        OnRepositoryUpdate.Event.RECORDS_CLEARED
                )
        );
    }

    @Override
    public void clearRecord() {
        // Clear the route line first before clearing the repository
        Log.d(TAG, "clearRecord in: sending OnMapsViewModelRequest ACTION_CLEAR_ROUTE_LINE for " + getRecords().size() + " records");

        /*
            Duplicate records to save temporarily for use to clear route line in MapViewModel
            because right after the event dispatched, records get cleared.

            So, by copying the records we give MapsViewModel access to the record's layer & source IDs
            as deleting the DirectionsRoute on the map requires its layer and source ID.
         */
        List<MapboxDirectionsRoute> mapboxDirectionsRoutes = new ArrayList<>(getRecords());

        EventBus.getDefault().post(
                new OnMapsViewModelRequest
                        .Builder(OnMapsViewModelRequest.Event.ACTION_CLEAR_ROUTE_LINE)
                        .withMapboxDirectionsRoutes(mapboxDirectionsRoutes)
                        .build()
        );

        super.clearRecord();
    }

    public List<MapboxDirectionsRoute> filterByTripIndex(int tripIndex) {

        List<MapboxDirectionsRoute> filteredMapboxDirectionsRoute = new ArrayList<>();

        for (MapboxDirectionsRoute m : getRecords())
            if (m.getTripIndexes().contains(tripIndex))
                filteredMapboxDirectionsRoute.add(m);

        return filteredMapboxDirectionsRoute;

    }

}
