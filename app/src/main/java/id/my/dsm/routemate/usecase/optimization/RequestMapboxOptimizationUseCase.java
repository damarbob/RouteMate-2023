package id.my.dsm.routemate.usecase.optimization;

import android.util.Log;

import androidx.annotation.NonNull;

import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.directions.v5.models.RouteLeg;
import com.mapbox.api.optimization.v1.MapboxOptimization;
import com.mapbox.api.optimization.v1.models.OptimizationResponse;
import com.mapbox.api.optimization.v1.models.OptimizationWaypoint;
import com.mapbox.geojson.Point;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import id.my.dsm.routemate.BuildConfig;
import id.my.dsm.routemate.data.enums.MapboxProfile;
import id.my.dsm.routemate.data.event.network.OnMapboxDirectionsRouteResponse;
import id.my.dsm.routemate.data.event.network.OnOptimizationResponse;
import id.my.dsm.routemate.data.event.repo.OnRepositoryUpdate;
import id.my.dsm.routemate.data.model.fleet.Fleet;
import id.my.dsm.routemate.data.model.maps.MapboxDirectionsRoute;
import id.my.dsm.routemate.data.model.place.Place;
import id.my.dsm.routemate.data.repo.fleet.FleetRepository;
import id.my.dsm.routemate.usecase.repository.AlterRepositoryUseCase;
import id.my.dsm.vrpsolver.model.Location;
import id.my.dsm.vrpsolver.model.Solution;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestMapboxOptimizationUseCase {

    private static final String TAG = RequestMapboxOptimizationUseCase.class.getSimpleName();

    @Inject
    FleetRepository fleetRepository;
    @Inject
    AlterRepositoryUseCase alterRepositoryUseCase;

    @Inject
    public RequestMapboxOptimizationUseCase(FleetRepository fleetRepository) {
        this.fleetRepository = fleetRepository;
    }

    public void invoke(@NonNull List<Place> places, boolean isRoundTrip) {

        /*
            The following line of code is for sorting the places to be the source first.
            VERY IMPORTANT, as Mapbox Optimization API will only accepts "first" source or "any".
            However, in this case we will use "first".
            Henceforth, the places should be sorted to be the source first.
         */
        List<Location> sortedPlaces = Location.Toolbox.getSortedPlaces(Place.Toolbox.getDSMPlaces(places), true);

        ArrayList<Point> points = new ArrayList<>();

        for (Location p : sortedPlaces) {
            points.add(
                    Point.fromLngLat(
                            p.getLatLngAlt().getLongitude(),
                            p.getLatLngAlt().getLatitude()
                    )
            );
        }

        /*
            Get the default vehicle from the vehicles list.
            Then, get the driving mapboxProfile in an acceptable Mapbox mapboxProfile format (DirectionsCriteria).

            mapboxProfile: driving, walking, cycling, driving-traffic
         */
        Fleet defaultVehicle = fleetRepository.getDefaultVehicle();
        String mapboxProfile = defaultVehicle == null ? MapboxProfile.DRIVING.toDirectionsCriteria() : defaultVehicle.getMapboxProfile().toDirectionsCriteria();

        /*
            Adapt sorted places into acceptable Mapbox coordinates format.
            A semicolon-separated list of {longitude},{latitude} coordinates.
            There must be between two and 12 coordinates.
            The first coordinate is the start and end point of the trip by default (source=first&roundtrip=true).

            coordinates: 110.3526,-7.7849125;110.3844,-7.79163;110.3754,-7.7912;110.3526,-7.7754
         */
        StringBuilder placeLatLngs = new StringBuilder();
        for (int i = 0, placesSize = sortedPlaces.size(); i < placesSize; i++) {

            Location p = sortedPlaces.get(i);

            placeLatLngs.append(p.getLatLngAlt().getLongitude());
            placeLatLngs.append(",");
            placeLatLngs.append(p.getLatLngAlt().getLatitude());

            if (sortedPlaces.indexOf(p) < sortedPlaces.size() - 1)
                placeLatLngs.append(";");

        }

        /*
            Create request to Mapbox Optimization API
            Source must be first
            Destination must be any
            Geometries must be polyline6 (RouteMate only supports polyline6)
         */
        MapboxOptimization mapboxOptimization = MapboxOptimization.builder()
                .profile(mapboxProfile)
                .accessToken(BuildConfig.MAPBOX_ACCESS_TOKEN)
                .coordinates(points)
                .source(DirectionsCriteria.SOURCE_FIRST)
                .destination(DirectionsCriteria.DESTINATION_ANY)
                .roundTrip(isRoundTrip)
                .geometries(DirectionsCriteria.GEOMETRY_POLYLINE6)
                .build();

        mapboxOptimization.enqueueCall(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<OptimizationResponse> call, @NonNull Response<OptimizationResponse> response) {

                Log.d(TAG, "computeDefault: response: " + response); // Log full response

                OptimizationResponse optimizationResponse = response.body();
                assert optimizationResponse != null; // Response error if null

                Log.d(TAG, "computeDefault: optimizationResponse: " + optimizationResponse); // Log response body

                String code = optimizationResponse.code();

                switch (Objects.requireNonNull(code)) {
                    case "NoRoute":
                    case "NoTrips":
                    case "NotImplemented":
                    case "NoSegment":
                    case "Forbidden":
                    case "ProfileNotFound":
                    case "InvalidInput":
                        EventBus.getDefault().post(
                                new OnOptimizationResponse(
                                        OnOptimizationResponse.Status.FAILED,
                                        OnOptimizationResponse.Error.mapboxErrorFrom(code)
                                )
                        );
                        return;
                }

                ArrayList<Location> solutionPlaces = new ArrayList<>(); // If no assertion error, prepare solution places list

                List<OptimizationWaypoint> waypoints = optimizationResponse.waypoints(); // Get waypoints
                assert waypoints != null; // Waypoints error if null

                Log.d(TAG, "computeDefault: waypoints: " + waypoints.size()); // Log waypoints size

                for (OptimizationWaypoint ow : waypoints) {

                    int waypoint_index = ow.waypointIndex();
                    Location p = sortedPlaces.get(waypoint_index); // Get place based on the index found in the waypointIndexes
                    solutionPlaces.add(p);

                }
                Log.d(TAG, "computeDefault: solutionPlaces: " + solutionPlaces.size());

                // Trips array is always either 0 or 1 in size
                List<DirectionsRoute> trips = optimizationResponse.trips();
                assert trips != null; // Trips error if null

                Log.d(TAG, "computeDefault: trips: " + trips.size());

                DirectionsRoute trip = trips.get(0); // First and only trip (if exists)
                assert trip != null; // Trip error if null

                // Declare new mapboxDirectionsRoute from trip
                MapboxDirectionsRoute mapboxDirectionsRoute = MapboxDirectionsRoute.fromDirectionsRoute(trip);

                List<RouteLeg> legs = trip.legs();
                assert legs != null; // Trip's legs error if null
                assert legs.size() > 0; // Error if the route legs length is 0

                // Declare new solutions from place sequence of solutionPlaces
                ArrayList<Solution> solutions = getDistanceFromPlaceSequence(solutionPlaces, legs);
                assert solutions.size() > 0; // Solution distances error if empty

                assert defaultVehicle != null; // Default vehicle error if null
//                Objects.requireNonNull(mapboxDirectionsRoute.getLinkedIdsMap().get("vehicleIds")).add(defaultVehicle.getId());
                mapboxDirectionsRoute.getVehicleIds().add(defaultVehicle.getId()); // Assign default vehicle to the directionsRoute

                // Assign vehicleId to all solution
                for (Solution s : solutions)
                    s.setVehicleId(defaultVehicle.getId());

                Log.d(TAG, "computeDefault: solutions: " + solutions.size());

                alterRepositoryUseCase.invoke(OnRepositoryUpdate.Event.ACTION_CLEAR, new MapboxDirectionsRoute(), false);

                // Post solution distances response
                EventBus.getDefault().post(
                        new OnOptimizationResponse(
                                OnOptimizationResponse.Status.SUCCESS,
                                solutions
                        )
                );

                // To draw the line in mapsViewModel also acts as onFinished callback
                EventBus.getDefault().post(
                        new OnMapboxDirectionsRouteResponse(mapboxDirectionsRoute)
                );

            }

            @Override
            public void onFailure(@NonNull Call<OptimizationResponse> call, @NonNull Throwable t) {
                call.cancel();
                Log.d(TAG, "computeDefault: FAILED");
            }
        });

    }

    // TODO: Add comments
    public static ArrayList<Solution> getDistanceFromPlaceSequence(@NonNull ArrayList<Location> places, @NonNull List<RouteLeg> routeLegs) {

        Log.d(TAG, "getDistanceFromPlaceSequence: places: " + places.size() + " | directionRoutes: " + routeLegs.size());

        ArrayList<Solution> solutionArrayList = new ArrayList<>(); // To keep the result in memory

        boolean isRoundTrip;

        // If the route length is equal to the number of places, it's assumed a roundtrip
        if (places.size() == routeLegs.size())
            isRoundTrip = true;
            // If the route length is equal to the number of places minus 1, it's definitely not a roundtrip
        else if (places.size() == routeLegs.size() - 1)
            isRoundTrip = false;
            // If not both, cannot determine
        else {
            Log.d(TAG, "getDistanceFromPlaceSequence: Cannot determine whether roundtrip or not" );
            return solutionArrayList;
        }

        // Loop place till the previous place before the end
        for (int i = 0; i < places.size() - 1; i++) {

            Location p = places.get(i); // Get current iterated place
            Location p2 = places.get(i + 1); // Get the next iterated place

            RouteLeg leg = routeLegs.get(i);

            Solution solution = new Solution(p, p2, leg.distance() == null ? 0 : leg.distance());
            solution.setDuration(leg.duration() == null ? 0 : leg.duration());

            solutionArrayList.add(solution);

        }

        if (isRoundTrip) {

            RouteLeg lastLeg = routeLegs.get(routeLegs.size() - 1);

            // Assume the depot is the first place
            Solution solution = new Solution(places.get(places.size() - 1), places.get(0), lastLeg.distance() == null ? 0 : lastLeg.distance());
            solution.setDuration(lastLeg.duration() == null ? 0 : lastLeg.duration());

            solutionArrayList.add(solution);

        }

        return solutionArrayList;

    }

}
