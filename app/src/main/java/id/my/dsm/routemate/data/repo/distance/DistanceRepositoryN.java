package id.my.dsm.routemate.data.repo.distance;

import androidx.annotation.NonNull;

import com.mapbox.mapboxsdk.geometry.LatLng;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import id.my.dsm.routemate.data.event.repo.OnDistanceRepositoryUpdate;
import id.my.dsm.routemate.data.event.repo.OnRepositoryUpdate;
import id.my.dsm.routemate.data.model.place.Place;
import id.my.dsm.routemate.data.repo.Repository;
import id.my.dsm.vrpsolver.model.Location;
import id.my.dsm.vrpsolver.model.MatrixElement;

@Module
@InstallIn(SingletonComponent.class)
public class DistanceRepositoryN extends Repository<MatrixElement> {

    private static final String TAG = "DistanceRepository";

    public DistanceRepositoryN() {
        super();
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void createRecord(MatrixElement record) {

//        Objective origin = record.getOrigin();
//        Objective destination = record.getDestination();

        String uUid = UUID.randomUUID().toString();  // Create unique ID

        // Assign index and id (origin's index + destination's index)
        record.setIndex(getLastRecordIndex());
//        record.setId(String.valueOf(origin.getIndex()) + destination.getIndex());
        record.setId(uUid);

        addRecord(record);

    }

    public void createRecords(ArrayList<MatrixElement> records) {

        clearRecord(); // Clear repository

        for (MatrixElement d : records)
            createRecord(d);

    }

    @Singleton
    @Provides
    public DistanceRepositoryN getInstance() {
        return new DistanceRepositoryN();
    }

    @Override
    public void onRecordAdded(MatrixElement record) {
        EventBus.getDefault().post(
                new OnDistanceRepositoryUpdate(
                        OnRepositoryUpdate.Event.RECORD_ADDED,
                        record
                )
        );
    }

    @Override
    public void onRecordDeleted(MatrixElement record) {
        EventBus.getDefault().post(
                new OnDistanceRepositoryUpdate(
                        OnRepositoryUpdate.Event.RECORD_DELETED,
                        record
                )
        );
    }

    @Override
    public void onRecordCleared() {
        EventBus.getDefault().post(
                new OnDistanceRepositoryUpdate(
                        OnRepositoryUpdate.Event.RECORDS_CLEARED
                )
        );
    }

    /**
     * Filter List of solution MatrixElement by origin Place
     * @param origin an origin Place
     * @return List of MatrixElement filtered
     */
    public List<MatrixElement> filterByOrigin(Location origin) {
        return MatrixElement.Toolbox.filterByOrigin(getRecords(), origin);
    }

    /**
     * Calculate and populate all possible distances from a given arraylist of place into distances array.
     * WARNING: This might takes A LOT of resources to work. Using async task is very recommended.
     *
     * @param places Route arraylist
     */
    public void calculateAirDistances(@NonNull List<Place> places, boolean effectiveMode) {

        // (REMOVED) Trigger distance calculation started callback

        ArrayList<MatrixElement> distancesArray = new ArrayList<>();

        int a = 0;
        for (Place objFrom : places) {
            LatLng origin = objFrom.getMapboxLatLng();
            int originIndex = places.indexOf(objFrom);

            int b = 0;
            for (Place objTo : places) {
                b++;

                LatLng destination = objTo.getMapboxLatLng();
                int destinationIndex = places.indexOf(objTo);

                if (effectiveMode) {
                    if (b <= a) {
                        // In effective mode, this helps fulfill the arraylist
                        for (int i = 0; i < distancesArray.size() - 1; i++) {
                            if (distancesArray.get(i).getOrigin().equals(objTo.getLocation()) &&
                                    distancesArray.get(i).getDestination().equals(objFrom.getLocation())) {

                                MatrixElement matrixElement = new MatrixElement(objFrom.getLocation(), objTo.getLocation(), distancesArray.get(i).getDistance());

                                distancesArray.add(
                                        matrixElement
                                );
                            }
                        }

                        continue;
                    }
                }

                if (!(originIndex == destinationIndex)) {
                    MatrixElement matrixElement = new MatrixElement(objFrom.getLocation(), objTo.getLocation(), calculateAirDistance(origin, destination));
                    distancesArray.add(matrixElement);
                }

            }

            a++;
        }

//        calculateDistanceSavingValue(objectives, distancesArray);

        // (REMOVED) Trigger on distance calculation finished callback to listeners

        createRecords(distancesArray);
    }

    /**
     * Calculate a straight distance between two places.
     *
     * @param origin      a {@link LatLng} of origin {@link Place} instance
     * @param destination a {@link LatLng} of destination {@link Place} instance
     * @return a double value of distance in meters
     */
    public static double calculateAirDistance(@NonNull LatLng origin, @NonNull LatLng destination) {
        return origin.distanceTo(destination);
    }

}
