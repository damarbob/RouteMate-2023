package id.my.dsm.routemate.data.event.network;

import java.util.List;

import id.my.dsm.vrpsolver.model.MatrixElement;

public final class OnDistanceMatrixResponse {

    private final List<MatrixElement> matrix;
    private final boolean continueOptimization;

    public OnDistanceMatrixResponse(List<MatrixElement> matrix, boolean continueOptimization) {
        this.matrix = matrix;
        this.continueOptimization = continueOptimization;
    }

    public List<MatrixElement> getMatrix() {
        return matrix;
    }

    public boolean isContinueOptimization() {
        return continueOptimization;
    }
}