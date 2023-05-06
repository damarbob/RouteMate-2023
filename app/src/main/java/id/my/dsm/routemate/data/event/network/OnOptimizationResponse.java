package id.my.dsm.routemate.data.event.network;

import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;

import id.my.dsm.routemate.R;
import id.my.dsm.vrpsolver.model.Solution;

public class OnOptimizationResponse {

    public enum Status {
        SUCCESS,
        FAILED
    }
    public enum Error {
        MapboxNoRoute,
        MapboxNoTrips,
        MapboxNotImplemented,
        MapboxNoSegment,
        MapboxForbidden,
        MapboxProfileNotFound,
        MapboxInvalidInput;

        public String toMessage(Resources resources) {

            // Import string array to adapt localization changes
            String[] messages = resources.getStringArray(R.array.optimization_response_error_message);

            switch (this) {
                case MapboxNoRoute:
                    return messages[1];
                case MapboxNoTrips:
                    return messages[2];
                case MapboxNotImplemented:
                    return messages[3];
                case MapboxNoSegment:
                    return messages[4];
                case MapboxForbidden:
                    return messages[5];
                case MapboxProfileNotFound:
                    return messages[6];
                case MapboxInvalidInput:
                    return messages[7];
                default:
                    throw new IllegalStateException("Unexpected value: " + this);
            }
        }

        public static Error mapboxErrorFrom(String errorCode) {
            switch (errorCode) {
                case "NoRoute":
                    return MapboxNoRoute;
                case "NoTrips":
                    return MapboxNoTrips;
                case "NotImplemented":
                    return MapboxNotImplemented;
                case "NoSegment":
                    return MapboxNoSegment;
                case "Forbidden":
                    return MapboxForbidden;
                case "ProfileNotFound":
                    return MapboxProfileNotFound;
                case "InvalidInput":
                    return MapboxInvalidInput;
                default:
                    throw new IllegalStateException("Unexpected value: " + errorCode);
            }
        }
    }
    private final Status status;
    private List<Solution> solutions;
    private Error error;

    public OnOptimizationResponse(Status status, List<Solution> solutions) {
        this.status = status;
        this.solutions = solutions;
    }

    public OnOptimizationResponse(Status status, Error error) {
        this.status = status;
        this.error = error;
    }

    @Nullable
    public List<Solution> getSolutions() {
        return solutions;
    }

    @NonNull
    public Status getStatus() {
        return status;
    }

    @Nullable
    public Error getError() {
        return error;
    }
}
