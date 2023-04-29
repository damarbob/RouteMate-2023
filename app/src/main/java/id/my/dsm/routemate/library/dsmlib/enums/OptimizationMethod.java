package id.my.dsm.routemate.library.dsmlib.enums;

import androidx.annotation.NonNull;

public enum OptimizationMethod {
    NEAREST_NEIGHBOR,
    SAVING_MATRIX;

    @NonNull
    @Override
    public String toString() {
        switch (this) {
            case NEAREST_NEIGHBOR:
                return "Nearest Neighbor";
            case SAVING_MATRIX:
                return "Saving Matrix";
            default:
                throw new IllegalStateException("Unexpected OptimizationMethod value: " + this);
        }
    }

    public static OptimizationMethod fromString(String string) {
        switch (string) {
            case "Nearest Neighbor":
                return OptimizationMethod.NEAREST_NEIGHBOR;
            case "Saving Matrix":
                return OptimizationMethod.SAVING_MATRIX;
            default:
                return null;
        }
    }

}
