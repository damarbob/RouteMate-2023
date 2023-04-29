package id.my.dsm.routemate.data.event.viewmodel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import id.my.dsm.routemate.library.dsmlib.model.MatrixElement;

public final class OnDistancesViewModelRequest {

    public enum Event {
        ACTION_RESOLVE_MATRIX,
    }

    private final Event event;
    private final ArrayList<MatrixElement> matrixElements; // NOT YET USED
    private final boolean continueOptimization;

    private OnDistancesViewModelRequest(@NonNull Builder builder) {
        this.event = builder.event;
        this.matrixElements = builder.matrixElements;
        this.continueOptimization = builder.continueOptimization;
    }

    @NonNull
    public Event getEvent() {
        return event;
    }

    @Nullable
    public ArrayList<MatrixElement> getDistances() {
        return matrixElements;
    }

    public boolean isContinueOptimization() {
        return continueOptimization;
    }

    public static class Builder {
        private final Event event;
        private ArrayList<MatrixElement> matrixElements;
        private boolean continueOptimization;

        public Builder(@NonNull Event event) {
            this.event = event;
        }

        public Builder withPlace(@NonNull ArrayList<MatrixElement> matrixElements) {
            this.matrixElements = matrixElements;
            return this;
        }

        public Builder withContinueOptimization(boolean continueOptimization) {
            this.continueOptimization = continueOptimization;
            return this;
        }

        public OnDistancesViewModelRequest build() {
            return new OnDistancesViewModelRequest(this);
        }

    }
    
}
