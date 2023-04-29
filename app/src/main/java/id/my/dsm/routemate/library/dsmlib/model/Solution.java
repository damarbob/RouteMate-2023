package id.my.dsm.routemate.library.dsmlib.model;

import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Solution extends Distance {

    private static final String TAG = Solution.class.getSimpleName();

    // For solution
    private double carry = 0;
    private double demand = 0;
    private int tripIndex;
    private String vehicleId;

    // Used for deserialization
    public Solution() {
    }

    public Solution(Location origin, Location destination, double distance) {
        super(origin, destination, distance);
    }

    public double getDemand() {
        return demand;
    }

    public void setDemand(double demand) {
        this.demand = demand;
    }

    public double getCarry() {
        return carry;
    }

    public void setCarry(double carry) {
        this.carry = carry;
    }

    public int getTripIndex() {
        return tripIndex;
    }

    public void setTripIndex(int tripIndex) {
        this.tripIndex = tripIndex;
    }

    @Nullable
    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public static Solution fromMatrixElement(MatrixElement matrixElement) {
        return new Solution(matrixElement.getOrigin(), matrixElement.getDestination(), matrixElement.getDistance());
    }

    public static class Toolbox {

        /**
         * Filter List of solution MatrixElement by vehicleId
         * @param solutionDistances List of solution Distances
         * @param vehicleId String of vehicleId
         * @return List of solution Distances filtered
         */
        public static List<Solution> filterByVehicleId(List<Solution> solutionDistances, String vehicleId) {

            ArrayList<Solution> solutions = new ArrayList<>();

            for (Solution d : solutionDistances) {
                assert d.getVehicleId() != null; // Error if vehicleId is null
                if (d.getVehicleId().equals(vehicleId))
                    solutions.add(d);
            }

            return solutions;

        }

        /**
         * Filter List of solution MatrixElement by tripIndex
         * @param solutionDistances List of solution MatrixElement
         * @param tripIndex int of tripIndex
         * @return List of solution MatrixElement filtered
         */
        public static List<Solution> filterByTripIndex(List<Solution> solutionDistances, int tripIndex) {

            ArrayList<Solution> filteredSolution = new ArrayList<>();

            for (Solution d : solutionDistances)
                if (d.getTripIndex() == tripIndex)
                    filteredSolution.add(d);

            return filteredSolution;

        }

        /**
         * Auto-assign route index to a List of solution MatrixElement
         * @param solutionDistances List of solution Distances
         */
        public static void assignTripIndex(List<Solution> solutionDistances) {

            int routeIndex = 0;
            Log.e(TAG, "assignTripIndex: First trip index: " + routeIndex);

            Location origin = solutionDistances.get(0).getOrigin();
//            Log.e(TAG, "assignTripIndex: First origin: " + origin.getName());

            // Uses numbered loop because the index is needed
            for (int i = 0; i < solutionDistances.size(); i++) {

                Solution d = solutionDistances.get(i);

                d.setTripIndex(routeIndex);

                if (d.getDestination().equals(origin)) {

                    routeIndex++;
                    Log.e(TAG, "runAutoRouteIndexAssign: Next trip index: " + routeIndex);

                    if (i + 1 <= solutionDistances.size() - 1) {

                        origin = solutionDistances.get(i + 1).getOrigin();

//                        Log.e(TAG, "runAutoRouteIndexAssign: Next origin: " + origin.getName());

                    }

                }

            }

        }

    }

}
