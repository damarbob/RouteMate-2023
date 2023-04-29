package id.my.dsm.routemate.data.event.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import id.my.dsm.routemate.data.model.user.DSMUser;

/**
 * Triggered by {@link id.my.dsm.routemate.data.repo.user.UserRepository}'s updateUserData()
 */
public final class OnUpdateDSMUser {

    public enum Status {
        FAILED,
        SUCCESS,
    }

    private final Status status;
    private DSMUser dsmUser;
    private String message;

    public OnUpdateDSMUser(@NonNull Status status) {
        this.status = status;
    }

    public OnUpdateDSMUser(Status status, @NonNull String message) {
        this.status = status;
        this.message = message;
    }

    public OnUpdateDSMUser(@NonNull Status status, @NonNull DSMUser dsmUser) {
        this.status = status;
        this.dsmUser = dsmUser;
    }

    public OnUpdateDSMUser(@NonNull Status status, @NonNull DSMUser dsmUser, @NonNull String message) {
        this.status = status;
        this.dsmUser = dsmUser;
        this.message = message;
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
