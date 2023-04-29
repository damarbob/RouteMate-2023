package id.my.dsm.routemate.library.dsmlib.model;

import java.util.ArrayList;
import java.util.List;


public class MatrixElement extends Distance {

    public static final String TAG = MatrixElement.class.getSimpleName();

    // For matrix
    private double savingDistance = 0; // For matrix

    // Used for deserialization
    public MatrixElement() {
    }

    public MatrixElement(Location origin, Location destination, double distance) {
        super(origin, destination, distance);
    }

    public double getSavingDistance() {
        return savingDistance;
    }

    public void setSavingDistance(double savingDistance) {
        this.savingDistance = savingDistance;
    }

    public static class Toolbox {

        /**
         * Filter List of solution MatrixElement by origin Place
         * @param matrixElements List of MatrixElement
         * @param origin an origin Place
         * @return List of MatrixElement filtered
         */
        public static List<MatrixElement> filterByOrigin(List<MatrixElement> matrixElements, Location origin) {

            ArrayList<MatrixElement> filteredMatrixElements = new ArrayList<>();

            for (MatrixElement d : matrixElements) {
                if (d.getOrigin().equals(origin))
                    filteredMatrixElements.add(d);
            }

            return filteredMatrixElements;

        }

    }

}
