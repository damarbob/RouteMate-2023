package id.my.dsm.routemate.data.event.repo;

import androidx.annotation.NonNull;

import id.my.dsm.vrpsolver.model.MatrixElement;

public final class OnDistanceRepositoryUpdate extends OnRepositoryUpdate<MatrixElement> {

    private MatrixElement matrixElement;

    public OnDistanceRepositoryUpdate(@NonNull Event event) {
        super(event);
    }

    public OnDistanceRepositoryUpdate(@NonNull Event event, MatrixElement matrixElement) {
        super(event);

        this.matrixElement = matrixElement;
    }

    @NonNull
    @Override
    public Event getStatus() {
        return super.getStatus();
    }

    public MatrixElement getDistance() {
        return matrixElement;
    }

}
