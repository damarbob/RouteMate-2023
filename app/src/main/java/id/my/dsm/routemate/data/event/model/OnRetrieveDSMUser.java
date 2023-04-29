package id.my.dsm.routemate.data.event.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import id.my.dsm.routemate.data.model.user.DSMUser;

/**
 * Triggered by {@link id.my.dsm.routemate.data.repo.user.UserRepository}'s retrieveUserData()
 */
public final class OnRetrieveDSMUser {

    public enum Status {
        FAILED,
        SUCCESS,
    }

    private final Status status;
    private DSMUser dsmUser;
    private String message;

    public OnRetrieveDSMUser(@NonNull Status status) {
        this.status = status;
    }

    public OnRetrieveDSMUser(Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public OnRetrieveDSMUser(@NonNull Status status, @NonNull DSMUser dsmUser) {
        this.status = status;
        this.dsmUser = dsmUser;
    }

    @NonNull
    public Status getStatus() {
        return status;
    }

    @Nullable
    public DSMUser getDsmUser() {
        return dsmUser;
    }

    @NonNull
    public String getMessage() {
        return message != null ? message : "No message";
    }
}
