package id.my.dsm.routemate.data.event.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import id.my.dsm.routemate.data.model.userdata.UserData;

/**
 * Triggered by {@link id.my.dsm.routemate.data.repo.user.UserRepository}'s updateUserData()
 */
public final class OnUpdateUserData {

    public enum Status {
        FAILED,
        SUCCESS,
    }

    private final Status status;
    private UserData userData;
    private String message;

    public OnUpdateUserData(@NonNull Status status) {
        this.status = status;
    }

    public OnUpdateUserData(Status status, @NonNull String message) {
        this.status = status;
        this.message = message;
    }

    public OnUpdateUserData(@NonNull Status status, @NonNull UserData userData) {
        this.status = status;
        this.userData = userData;
    }

    public OnUpdateUserData(@NonNull Status status, @NonNull UserData userData, @NonNull String message) {
        this.status = status;
        this.userData = userData;
        this.message = message;
    }

    @NonNull
    public Status getStatus() {
        return status;
    }

    @Nullable
    public UserData getUserData() {
        return userData;
    }

    @NonNull
    public String getMessage() {
        return message != null ? message : "No message";
    }
}
