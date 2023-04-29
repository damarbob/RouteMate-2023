package id.my.dsm.routemate.library.dsmlib;

import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import id.my.dsm.routemate.library.dsmlib.enums.OptimizationMethod;
import id.my.dsm.routemate.library.dsmlib.event.OptimizationResponseError;
import id.my.dsm.routemate.library.dsmlib.event.OptimizationResponseListener;
import id.my.dsm.routemate.library.dsmlib.model.Location;
import id.my.dsm.routemate.library.dsmlib.model.MatrixElement;
import id.my.dsm.routemate.library.dsmlib.model.Solution;
import id.my.dsm.routemate.library.dsmlib.model.Vehicle;

public class DSMSolver {

    // Constants
    private static final String TAG = DSMSolver.class.getSimpleName();

    // Internal dependencies
    private static OptimizationMethod optimizationMethod = OptimizationMethod.NEAREST_NEIGHBOR;

    public DSMSolver() {
    }

    /**
     * Optimization builder for DSMSolver, recommended to be invoked under background thread
     */
    public static class OptimizationBuilder {

        private final List<MatrixElement> matrix;
        private final List<Location> places;
        private final List<Vehicle> vehicles;
        private OptimizationMethod optimizationMethod;
        private boolean isRoundTrip = true;

        public OptimizationBuilder(
                @NonNull List<MatrixElement> matrix,
                @NonNull List<Location> places,
                @NonNull List<Vehicle> vehicles
        ) {
            this.matrix = matrix;
            this.places = places;
            this.vehicles = vehicles;
        }

        public OptimizationBuilder withMethod(@NonNull OptimizationMethod optimizationMethod) {
            this.optimizationMethod = optimizationMethod;
            return this;
        }

        public OptimizationBuilder withRoundTrip(boolean isRoundTrip) {
            this.isRoundTrip = isRoundTrip;
            return this;
        }

        public void optimize() {

            if (this.optimizationMethod == null)
                this.optimizationMethod = DSMSolver.optimizationMethod;
            else
                DSMSolver.optimizationMethod = this.optimizationMethod;

            // Pick the first source because NN & SM only accepts one source
            Location source = Location.Toolbox.getByProfile(places, Location.Profile.SOURCE).get(0);
            int depotPlaceIndex = places.indexOf(source);

            switch (optimizationMethod) {
                case NEAREST_NEIGHBOR:

                    // Post optimization response event (might take a while to process depends on the algorithm)
                    sendOptimizationSuccessResponse(
                            computeCapacitatedNearestNeighborResult(
                                    matrix,
                                    places,
                                    vehicles,
                                    depotPlaceIndex,
                                    isRoundTrip
                            )
                    );
//                    EventBus.getDefault().post(
//                            new OnDSMSolverOptimizationResponse(
//                                    OnDSMSolverOptimizationResponse.Status.SUCCESS,
//                                    computeCapacitatedNearestNeighborResult(
//                                            matrix,
//                                            places,
//                                            vehicles,
//                                            depotPlaceIndex,
//                                            isRoundTrip
//                                    )
//                            ));

                    break;
                case SAVING_MATRIX:

                    // Post optimization response event
                    sendOptimizationSuccessResponse(
                            computeCapacitatedSavingMatrixResult(
                                    matrix,
                                    places,
                                    vehicles,
                                    depotPlaceIndex,
                                    isRoundTrip
                            )
                    );
//                    EventBus.getDefault().post(
//                            new OnDSMSolverOptimizationResponse(
//                                    OnDSMSolverOptimizationResponse.Status.SUCCESS,
//                                    computeCapacitatedSavingMatrixResult(
//                                            matrix,
//                                            places,
//                                            vehicles,
//                                            depotPlaceIndex,
//                                            isRoundTrip
//                                    )
//                            ));
                    break;

                default:
                    Log.e(TAG, "optimize: Method not yet implemented");

                    // Post optimization response event
                    sendOptimizationFailedResponse(OptimizationResponseError.METHOD_NOT_IMPLEMENTED);
//                    EventBus.getDefault().post(
//                            new OnDSMSolverOptimizationResponse(
//                                    OnDSMSolverOptimizationResponse.Status.FAILED,
//                                    OnDSMSolverOptimizationResponse.Error.MethodNotImplemented
//                            )
//                    );
                    break;
            }

            //

        }

    }

    private static final List<OptimizationResponseListener> optimizationResponseListeners = new ArrayList<>();
    public static void setOnOptimizationResponseListener(OptimizationResponseListener listener) {
        optimizationResponseListeners.add(listener);
    }
    private static void sendOptimizationSuccessResponse(List<Solution> solutions) {
        for (OptimizationResponseListener listener :  optimizationResponseListeners) {
            if (listener != null)
                listener.onOptimizationSuccess(solutions);
            else
                optimizationResponseListeners.remove(listener);
        }
    }
    private static void sendOptimizationFailedResponse(OptimizationResponseError error) {
        for (OptimizationResponseListener listener :  optimizationResponseListeners) {
            if (listener != null)
                listener.onOptimizationFailed(error);
            else
                optimizationResponseListeners.remove(listener);
        }
    }


    public static ArrayList<MatrixElement> getDistanceFromPlaceSequence(ArrayList<Location> places, ArrayList<Double> distanceValues, ArrayList<Double> durationValues) {

        ArrayList<MatrixElement> matrixElements = new ArrayList<>(); // To keep the result in memory

        boolean isRoundTrip;

        if (places.size() == distanceValues.size() || places.size() == durationValues.size())
            isRoundTrip = true;
        else if (places.size() == distanceValues.size() - 1 || places.size() == durationValues.size() - 1)
            isRoundTrip = false;
        else
            return matrixElements;

        for (int i = 0; i < places.size() - 1; i++) {

            Location p = places.get(i);
            Location p2 = places.get(i + 1);

            MatrixElement d = new MatrixElement(p, p2, distanceValues.get(i));

            if (durationValues != null && durationValues.size() > 0)
                d.setDuration(durationValues.get(i));

            matrixElements.add(d);

        }

        if (isRoundTrip) {
            // Assume the depot is the first place
            MatrixElement d = new MatrixElement(places.get(places.size() - 1), places.get(0), distanceValues.get(distanceValues.size() - 1));

            if (durationValues != null && durationValues.size() > 0)
                d.setDuration(durationValues.get(durationValues.size() - 1));

            matrixElements.add(d);

        }

        return matrixElements;
    }

    // DEPENDENCIES FUNCTIONALITY

    // TODO: Finish thoroughly
    public static void calculateDistanceSavingValue(@NonNull List<Location> places, List<MatrixElement> distancesArray) {

        // Pick the first source because Saving Matrix method only accepts one source
        Location source = Location.Toolbox.getByProfile(places, Location.Profile.SOURCE).get(0);
        int depotId = places.indexOf(source);

        // Saving MatrixElement (Saving matrix equivalent) TODO: Extract depotId as a parameter
        for (MatrixElement matrixElement : distancesArray) {

            int oriId = places.indexOf(matrixElement.getOrigin());
            int destId = places.indexOf(matrixElement.getDestination());

            if (oriId == depotId || destId == depotId) {
                continue;
            }

            double doi = 0;
            double doj = 0;
            double dij = 0;

            for (MatrixElement matrixElement2 : distancesArray) {
                if (places.indexOf(matrixElement2.getOrigin()) == depotId && places.indexOf(matrixElement2.getDestination()) == oriId) {
                    doi = matrixElement2.getDistance();
                } else if (places.indexOf(matrixElement2.getOrigin()) == depotId && places.indexOf(matrixElement2.getDestination()) == destId) {
                    doj = matrixElement2.getDistance();
                } else if (places.indexOf(matrixElement2.getOrigin()) == oriId && places.indexOf(matrixElement2.getDestination()) == destId) {
                    dij = matrixElement2.getDistance();
                }
            }

            double sij = doi + doj - dij;

            Log.d(TAG, "MatrixElement saving: " + doi + " + " + doj +  " - " + dij + " = " + sij);

//            if (sij > 0)
                matrixElement.setSavingDistance(sij);

        }

    }

    // DSMSolver

    /***
     * Filter a list of distances by a place. Must be called after populateEstimatedDistancesArray.
     *
     * @param distancesArray Arraylist of distances
     * @param place Place object by which the distances filtered
     * @return An arraylist of distance
     */
    public static List<MatrixElement> filterDistancesArrayByPlace(@NonNull List<MatrixElement> distancesArray, Location place, boolean isOrigin) {
        ArrayList<MatrixElement> filteredDistancesArray = new ArrayList<>();

        for (MatrixElement matrixElement : distancesArray) {
            if (isOrigin && matrixElement.getOrigin().equals(place)) {
                filteredDistancesArray.add(matrixElement);
            }
            if (!isOrigin && matrixElement.getDestination().equals(place)) {
                filteredDistancesArray.add(matrixElement);
            }
        }

        return filteredDistancesArray;
    }

    /***
     * Filter the distances by an arraylist of "used places". Any distances object that contains one of "used places" array will not be returned. Therefore, reduces redundancy.
     *
     * @param distancesArray An arraylist of distances
     * @param places An arraylist of places
     * @return Arraylist of distances filtered by places
     */
    public static List<MatrixElement> filterDistancesArrayByPlaces(List<MatrixElement> distancesArray, List<Location> places, boolean excludeLastPlace, boolean filterIn) {
        ArrayList<MatrixElement> filteredDistancesArrayByPlaces = new ArrayList<>();
        ArrayList<Location> usedPlacesModified = new ArrayList<>(places);

        if (excludeLastPlace) {
            usedPlacesModified.remove(usedPlacesModified.size() - 1);
        }

        for (MatrixElement matrixElement : distancesArray) {
            boolean acceptedDistance = !filterIn ? !usedPlacesModified.contains(matrixElement.getOrigin()) && !usedPlacesModified.contains(matrixElement.getDestination()) : usedPlacesModified.contains(matrixElement.getOrigin()) || usedPlacesModified.contains(matrixElement.getDestination());

            if (acceptedDistance) {
                filteredDistancesArrayByPlaces.add(matrixElement);
            }
        }

        return filteredDistancesArrayByPlaces;
    }

    public static List<MatrixElement> filterDistancesArrayByPlacesStrict(List<MatrixElement> distancesArray, List<Location> places) {
        ArrayList<MatrixElement> filteredDistancesArrayByPlaces = new ArrayList<>();

        for (MatrixElement matrixElement : distancesArray) {
            boolean acceptedDistance = places.contains(matrixElement.getOrigin()) && places.contains(matrixElement.getDestination());

            if (acceptedDistance) {
                filteredDistancesArrayByPlaces.add(matrixElement);
            }
        }

        return filteredDistancesArrayByPlaces;

    }

    public static List<MatrixElement> filterDistancesArrayBySufficientCapacity(@NonNull List<MatrixElement> distancesArray, double vehicleCapacity) {
        ArrayList<MatrixElement> sufficientCapacityMatrixElements = new ArrayList<>();

        for (MatrixElement matrixElement : distancesArray) {

            if ((vehicleCapacity - matrixElement.getDestination().getDemands()) >= 0) {
                sufficientCapacityMatrixElements.add(matrixElement);
            }

        }

        return sufficientCapacityMatrixElements;
    }

    /***
     * Find the best (minimum) matrixElements among a list of matrixElements. Best in combination with filterDistance.
     *
     * @param matrixElements An arraylist of matrixElements
     * @return An arraylist of matrixElements object that contain the minimum distance value
     */
    public static List<MatrixElement> findBestDistances(@NonNull List<MatrixElement> matrixElements) {
        ArrayList<MatrixElement> bestMatrixElements = new ArrayList<>();
        ArrayList<Double> distanceValue = new ArrayList<>();

        // Assign distance value into distanceValue
        for (MatrixElement matrixElement : matrixElements) {
            distanceValue.add(matrixElement.getDistance());
        }

        // Get the best distance value
        int bestId = distanceValue.indexOf(Collections.min(distanceValue));
        double bestDistanceValue = distanceValue.get(bestId);

        // Check each value of matrixElements for the min and store it into bestMatrixElements array
        for (MatrixElement matrixElement : matrixElements) {

            // Find matrixElements with the most minimum value
            if (matrixElement.getDistance() == bestDistanceValue) {
                bestMatrixElements.add(matrixElement);
            }
        }

        return bestMatrixElements;
    }

    // Optimization

    /**
     * Capacitated TSP with Nearest Neighbor method. Returns a sequence of distances. MatrixElement traveled is not included, must be calculated manually.
     * Written by Damar Syah Maulana
     *
     * @param distancesArray  Arraylist of distances object
     * @param places          Arraylist of Destinations
     * @param depotPlaceIndex Index of Place defined as a Depot or initial point
     * @return Arraylist of Distances in Nearest Neighbor
     */
    public static List<Solution> computeCapacitatedNearestNeighborResult(List<MatrixElement> distancesArray, @NonNull List<Location> places, @NonNull List<Vehicle> vehicles, int depotPlaceIndex, boolean isRoundTrip) {

        Log.d(TAG, "computeCapacitatedNearestNeighborResult: Start of NN method");
        Log.d(TAG, "computeCapacitatedNearestNeighborResult: Distances: " + distancesArray.size() + " | Places: " + places.size() + " | Vehicles: " + Vehicle.Toolbox.getDefaultVehicle(vehicles));

        ArrayList<Solution> solutions = new ArrayList<>(); // Store the Nearest Neighbor produced distance or the final result

        // Get the default vehicle
        Vehicle vehicle = Vehicle.Toolbox.getDefaultVehicle(vehicles);
        vehicle = vehicle != null ? vehicle : vehicles.get(0);

        // Used places needs to be stored inside arraylist to check whether the distance that is about to calculate is already used or not
        ArrayList<Location> usedPlaces = new ArrayList<>();
        Location depotPlace = places.get(depotPlaceIndex); // Set the depot place by place index  TODO: Change to placeID
        usedPlaces.add(depotPlace);

        // Sort vehicles, move default vehicle to the first
        ArrayList<Vehicle> sortedVehicles = new ArrayList<>(vehicles);
        sortedVehicles.remove(vehicle);
        sortedVehicles.add(0, vehicle);

        // Clone vehicles for each dispatch limit
        ArrayList<Vehicle> fleet = new ArrayList<>();
        for (Vehicle v : sortedVehicles)
            if (v.getDispatchLimit() > 0)
                for (int i = 0; i < v.getDispatchLimit(); i ++) {
                    fleet.add(v);
                }

        boolean startNewRoute = false;

        for (Vehicle v : fleet) {

            // Skip current vehicle if solutions
            if (usedPlaces.containsAll(places))
                continue;

            ////

            // Vehicle remaining capacity should be visible in the entire scope for use multiple times
            double vehicleRemainingCapacity = v.getCapacity();

            // Compute solution for single trip
            ArrayList<Solution> trip = new ArrayList<>(); // Store the Nearest Neighbor produced distance or the final result

            for (int j = 0; vehicleRemainingCapacity >= 0; j++) {

                if (startNewRoute)
                    usedPlaces.add(depotPlace);

                // Filter the distancesArray by the last place used so that it will show the distance only FROM that place to ANY place because isOrigin is true.
                List<MatrixElement> filteredMatrixElements = filterDistancesArrayByPlace(distancesArray, usedPlaces.get(usedPlaces.size() - 1), true);
                if (startNewRoute) {
                    usedPlaces.remove(depotPlace);
                    startNewRoute = false;
                }

                List<MatrixElement> processedMatrixElements = filterDistancesArrayByPlaces(filteredMatrixElements, usedPlaces, true, false);

                List<MatrixElement> sufficientCapacityMatrixElements = filterDistancesArrayBySufficientCapacity(processedMatrixElements, vehicleRemainingCapacity);

                if (sufficientCapacityMatrixElements.size() == 0) {

                    if (!isRoundTrip)
                        break;

                    // Find a distance that go straight to depot and add to solutions (ROUNDTRIP)
                    for (MatrixElement matrixElement : filteredMatrixElements) {

                        if (matrixElement.getDestination().equals(depotPlace) && trip.size() > 0 && matrixElement.getOrigin().equals(trip.get(trip.size() - 1).getDestination())) {
                            Solution solution = Solution.fromMatrixElement(matrixElement);
                            solution.setCarry(vehicleRemainingCapacity);
                            solution.setVehicleId(v.getId()); // Assign vehicle id to solutionDistance
                            trip.add(solution);
                            startNewRoute = true;
//                            Log.d(TAG, "computeCapacitatedNearestNeighborResult: Last used places: " + usedPlaces.get(usedPlaces.size()-1).getName());
//                            Log.d(TAG, "computeCapacitatedNearestNeighborResult: (ROUNDTRIP) Added matrixElement " + solution.getOrigin().getName() + " to " + solution.getDestination().getName() + ": " + solution.getDistance());

                        }
                    }

                    break;

                }

                List<MatrixElement> bestMatrixElements = findBestDistances(sufficientCapacityMatrixElements);
                Solution bestDistance = Solution.fromMatrixElement(bestMatrixElements.get(0));

                double demands = bestDistance.getDestination().getDemands();
                vehicleRemainingCapacity -= demands;

                bestDistance.setDemand(demands);
                bestDistance.setCarry(vehicleRemainingCapacity);
                bestDistance.setVehicleId(v.getId()); // Assign vehicle id to solutionDistance

                usedPlaces.add(bestDistance.getDestination());
                trip.add(bestDistance);
//                Log.e(TAG, "(NN): Added distance " + bestDistance.getOrigin().getName() + " to " + bestDistance.getDestination().getName() + ": " + bestDistance.getDistance());

            }

            solutions.addAll(trip);


        }

        return solutions;
    }

    public static List<Solution> computeCapacitatedSavingMatrixResult(List<MatrixElement> distancesArray, @NonNull List<Location> places, @NonNull List<Vehicle> vehicles, int depotPlaceIndex, boolean isRoundTrip) {

        // Result distances
        ArrayList<Solution> solutions = new ArrayList<>();

        // Filter distances to only those that has a savingDistance
        Location depot = Location.Toolbox.getByProfile(places, Location.Profile.SOURCE).get(0);
        ArrayList<MatrixElement> savingMatrixElements = new ArrayList<>();

        for (MatrixElement matrixElement : distancesArray) {

            if (matrixElement.getSavingDistance() == 0 && (matrixElement.getOrigin().equals(depot) || matrixElement.getDestination().equals(depot)))
                continue;

            // Validate whether the matrixElement is already exists
            boolean isExists = false;

            for (MatrixElement d : savingMatrixElements) {

                // Looking for the reversed matrixElement
                if (matrixElement.getOrigin().equals(d.getDestination()) && matrixElement.getDestination().equals(d.getOrigin())) {
                    isExists = true;
                    break; // Break loop if matrixElement is exists
                }

            }

            if (!isExists)
                savingMatrixElements.add(matrixElement);

        }

        // Log saving distances
//        for (MatrixElement d : savingMatrixElements) {
//            Log.e(TAG + "(SM)", "MatrixElement with saving: MatrixElement " + d.getOrigin().getName() + " to " + d.getDestination().getName());
//        }

        // Begin computation
        ArrayList<MatrixElement> maxMatrixElements = new ArrayList<>();
        int totalDest = Location.Toolbox.getByProfile(places, Location.Profile.DESTINATION).size();
        int iteration = ((totalDest * totalDest) - totalDest) / 2; // Number of iteration self defined formula

        Log.e(TAG + "(SM)", "Iteration: " + iteration + " | Saving distances: " + savingMatrixElements.size());

        // Sort saving distances descending
        for (int i = 0; i < iteration; i++) {

            MatrixElement maxMatrixElement = savingMatrixElements.get(0);

            // Find the largest saving value
            for (int j = 0; j < savingMatrixElements.size(); j++) {

                MatrixElement matrixElement = savingMatrixElements.get(j);

                // If matrixElement is larger than current maxMatrixElement
                if (matrixElement.getSavingDistance() > maxMatrixElement.getSavingDistance()) {
                    maxMatrixElement = matrixElement;
                }

            }

            savingMatrixElements.remove(maxMatrixElement);

            maxMatrixElements.add(maxMatrixElement); // Add the largest distance into array

            // Log largest distance
//            Log.e(TAG + "(SM)", "Descending sorted distance: " + maxMatrixElement.getSavingDistance() + " | Origin: " + maxMatrixElement.getOrigin().getName() + " | Destination: " + maxMatrixElement.getDestination().getName());

        }

        // Main iteration
        ArrayList<ArrayList<Location>> placeGroups = new ArrayList<>();
        ArrayList<Location> usedPlaces = new ArrayList<>();

        Vehicle vehicle = Vehicle.Toolbox.getDefaultVehicle(vehicles);
        if (vehicle == null)
            vehicle = vehicles.get(0);

        // Sort vehicles, move default vehicle to the first
        ArrayList<Vehicle> sortedVehicles = new ArrayList<>(vehicles);
        sortedVehicles.remove(vehicle);
        sortedVehicles.add(0, vehicle);

        // Clone vehicles for each dispatch limit
        Log.e(TAG + "(SM)", "Start populating vehicles...");
        ArrayList<Vehicle> fleet = new ArrayList<>();
        for (Vehicle v : sortedVehicles)
            if (v.getDispatchLimit() > 0)
                for (int i = 0; i < v.getDispatchLimit(); i ++) {
                    fleet.add(v);
                }

        // Log fleet content
        for (Vehicle v : fleet)
            Log.e(TAG, "Vehicle added:" + v.getName());

        boolean forceStopComputation = false;

        for (MatrixElement matrixElement : maxMatrixElements) {

            double vehicleCapacity;

            Location origin = matrixElement.getOrigin();
            Location destination = matrixElement.getDestination();

            boolean isOriginExists = usedPlaces.contains(origin);
            boolean isDestinationExists = usedPlaces.contains(destination);

//            Log.e(TAG + "(SM)", "Inspecting MatrixElement " + matrixElement.getSavingDistance() + " | Origin: " + matrixElement.getOrigin().getName() + " | Destination: " + matrixElement.getDestination().getName());

            if (isOriginExists && isDestinationExists) {
                Log.e(TAG + "(SM)", "MatrixElement " + matrixElement.getSavingDistance() + ": Skipped due to both of its contents has been used");
                continue;
            }

            // Initialize container to list places that has grouped and existed in a placeGroup in the next loop
            ArrayList<Location> groupedPlaces = new ArrayList<>();

            /*
                Loops through places group if any.
                Useful if the iterated matrixElement's places whether origin or destination
                is a part of an existing placeGroup.
                If no placeGroup existed, skip immediately by the nature of foreach loop
                itself.
             */
            for (ArrayList<Location> placeGroup : placeGroups) {

                int i = placeGroups.indexOf(placeGroup);

                if (i > fleet.size() - 1) {
                    forceStopComputation = true;
                    continue;
                }

                vehicleCapacity = fleet.get(i).getCapacity();  // Get vehicle capacity from the matching index

                Log.e(TAG + "(SM)", "Inspecting placeGroup " + i + " from MatrixElement " + matrixElement.getSavingDistance() + " | Using vehicle: " + fleet.get(i).getName());

                groupedPlaces.addAll(placeGroup); // List places inside placeGroup into groupedPlaces for use outside of the loop

                // Check to see if whether the origin or destination existed in the current placeGroup
                boolean isOriginExistsInPlaceGroup = placeGroup.contains(origin);
                boolean isDestinationExistsInPlaceGroup = placeGroup.contains(destination);

//                if (isOriginExistsInPlaceGroup)
//                    Log.e(TAG + "(SM)", "Origin exists in placeGroup " + i);
//
//                if (isDestinationExistsInPlaceGroup)
//                    Log.e(TAG + "(SM)", "Destination exists in placeGroup " + i);

                // Immediately skip if both origin and destination exist in the current placeGroup
                if (isOriginExistsInPlaceGroup && isDestinationExistsInPlaceGroup) {
                    Log.e(TAG + "(SM)", "Both origin and destination exist in the placeGroup " + i);
                    break;
                }

                // Sum all demands of the place group
                double totalDemand = 0;
                for (Location p : placeGroup)
                    totalDemand += p.getDemands();

                // Check whether the demand of origin or destination and current placeGroup fits the vehicleCapacity
                boolean isOriginCapacityFitsInPlaceGroup = totalDemand + origin.getDemands() <= vehicleCapacity;
                boolean isDestinationCapacityFitsInPlaceGroup = totalDemand + destination.getDemands() <= vehicleCapacity;

//                if (isOriginCapacityFitsInPlaceGroup)
//                    Log.e(TAG + "(SM)", "Origin capacity fits in placeGroup " + i);
//
//                if (isDestinationCapacityFitsInPlaceGroup)
//                    Log.e(TAG + "(SM)", "Destination capacity fits in placeGroup " + i);

                Log.e(TAG + "(SM)", "Total demands of placeGroup " + i + ": " + totalDemand);

                if (isOriginExistsInPlaceGroup && isDestinationCapacityFitsInPlaceGroup) {
                    placeGroup.add(destination);
//                    Log.e(TAG + "(SM)", "Added " + destination.getName() + " (destination) into placeGroup(" + placeGroups.indexOf(placeGroup) + ") from MatrixElement " + matrixElement.getSavingDistance());
                    usedPlaces.add(destination);
                } else if (isDestinationExistsInPlaceGroup && isOriginCapacityFitsInPlaceGroup) {
                    placeGroup.add(origin);
//                    Log.e(TAG + "(SM)", "Added " + origin.getName() + " (origin) into placeGroup(" + placeGroups.indexOf(placeGroup) + ") from MatrixElement " + matrixElement.getSavingDistance());
                    usedPlaces.add(origin);
                }

            }

            // If the placeGroups size is equal or more than fleet size, prevent creation of a new placeGroup
            if (placeGroups.size() - 1 >= fleet.size() - 1) {
                forceStopComputation = true; // By enabling the force stop computation, prevents unused places from being added to the solution
                continue; // Must use continue to finish the inspection to all distances
            }

            // Checks if both origin & destination capacity fits
            vehicleCapacity = fleet.get(placeGroups.size() == 0 ? 0 : placeGroups.size() - 1).getCapacity(); // Get vehicle capacity from the matching index
            boolean isDistanceCapacityFits = origin.getDemands() + destination.getDemands() <= vehicleCapacity;

            // Checks origin and destination presence in the existing groupedPlaces
            boolean isOriginExistsInGroupedPlaces = groupedPlaces.contains(origin);
            boolean isDestinationExistsInGroupedPlaces = groupedPlaces.contains(destination);

            // Insert both origin and destination for the first loop or if the origin & destination don't exist in the groupedPlaces, AND if the capacity fits
            ArrayList<Location> ap = new ArrayList<>();
            if (
                    (placeGroups.size() == 0 || (!isOriginExistsInGroupedPlaces && !isDestinationExistsInGroupedPlaces))
                            && isDistanceCapacityFits
            ) {

                ap.add(origin);
                ap.add(destination);

                placeGroups.add(ap);
                usedPlaces.addAll(ap);

//                Log.e(TAG + "(SM)", "Added " + origin.getName() + " and " + destination.getName() + " into a NEW placeGroup(" + placeGroups.indexOf(ap) + ") from MatrixElement " + matrixElement.getSavingDistance());
            }

        }

        Log.e(TAG + "(SM)", "Begin populating used places");

//        for (DSMPlace p : usedPlaces)
//            Log.e(TAG + "(SM)", "Used: " + p.getName());

        // Populate unused places into one array if computation doesn't force stopped
        if (!forceStopComputation) {
            ArrayList<Location> unusedPlaces = new ArrayList<>();
            for (Location p : places) {
                if (!usedPlaces.contains(p) && !p.equals(depot)) {
                    unusedPlaces.add(p);
//                    Log.e(TAG + "(SM)", "Unused: " + p.getName());
                }
            }

            // Add unusedPlaces into placesGroup
            if (unusedPlaces.size() > 0) {
                /*
                    Possibilities on unusedPlaces:
                    - Filled with leftover places that have don't have pair due to large amount of demands
                 */
                for (Location p : unusedPlaces)
                    placeGroups.add(new ArrayList<>(Collections.singletonList(p)));

                // Warn if the number of unusedPlaces is more than 1 because there might be an error in the algorithm
                if (unusedPlaces.size() > 2)
                    Log.e(TAG + "(SM)", "Unused place should be a maximum of one. The solution produce more than one unused places. There might be an error during optimization!");
            }
        }

        Log.e(TAG + "(SM)", "Start of placeGroups log session");

        for (int i = 0; i < placeGroups.size(); i++) {
            ArrayList<Location> ap = placeGroups.get(i);

            ap.add(0, depot); // Adds depot every placeGroup's end

            Log.e(TAG + "(SM)", "Start of placeGroup " + i + " contents");

//            for (DSMPlace p : ap) {
//                Log.e(TAG + "(SM)", "placeGroup " + i + " content: " + p.getName());
//            }

            /*
                Filter all distances from distancesArray which its origin and destination are members of
                the current placeGroup. Required before continuing to optimization.
             */
            List<MatrixElement> filteredMatrixElements = filterDistancesArrayByPlacesStrict(distancesArray, ap);

            ArrayList<Vehicle> assignedVehicle = new ArrayList<>(Collections.singletonList(fleet.get(i)));
            List<Solution> optimizedDistances = computeCapacitatedNearestNeighborResult(filteredMatrixElements, ap, assignedVehicle, depotPlaceIndex, isRoundTrip);
            solutions.addAll(optimizedDistances);

            Log.e(TAG + "(SM)", "Start of placeGroup " + i + " optimized distances");
//            for (Solution s : optimizedDistances) {
//                Log.e(TAG + "(SM)", "placeGroup " + i + ": Place:" + s.getOrigin().getName() + " | Place: " + s.getDestination().getName());
//            }

            Log.e(TAG + "(SM)", "Start of placeGroup " + i + " demands");

            double totalDemands = 0;
//            for (DSMPlace p : ap) {
//                totalDemands += p.getDemands();
//                Log.e(TAG + "(SM)", "placeGroup " + i + ": " + p.getName() + ", demands: " + totalDemands);
//            }

            Log.e(TAG + "(SM)", "Total demands: " + totalDemands);
            Log.e(TAG + "(SM)", "End of placeGroup " + i);

        }

        return solutions;

    }

    // Toolbox

    @NonNull
    public static List<MatrixElement> symmetrizeMatrix(@NonNull List<MatrixElement> matrix) {

        ArrayList<MatrixElement> resultMatrix = new ArrayList<>(matrix);

        for (MatrixElement d : resultMatrix) {

            Location origin = d.getOrigin();
            Location destination = d.getDestination();
            double distance = d.getDistance();

            // Iterate through matrix again and find the opposite distance and set the value to match the previous distance
            for (MatrixElement d2 : resultMatrix)
                if (d2.getOrigin().equals(destination) && d2.getDestination().equals(origin)) // If it's opposite distance
                    d2.setDistance(distance);

        }

        return resultMatrix;

    }

    @NonNull
    private static ArrayList<MatrixElement> adaptPlacesToDistances(ArrayList<MatrixElement> distancesArray, ArrayList<Location> places) {

        ArrayList<MatrixElement> matrixElements = new ArrayList<>(); // To keep the result in memory

        for (int i = 0; i < places.size() - 1; i++) {
            Location p = places.get(i);
            Location p2 = places.get(i + 1);

            for (MatrixElement d : distancesArray) {

                if (d.getOrigin().equals(p) && d.getDestination().equals(p2))
                    matrixElements.add(d);

            }

        }

        return matrixElements;
    }


}
