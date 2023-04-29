package id.my.dsm.routemate.data.event.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import id.my.dsm.routemate.data.repo.user.UserRepository;
import id.my.dsm.routemate.data.repo.user.UserStatus;

/**
 * Event instance thrown by {@link UserRepository}
 */
public final class OnUserStatusChangedEvent {

    private final UserStatus userStatus;
    private final Exception exception;

    private OnUserStatusChangedEvent(@NonNull Builder builder) {
        this.userStatus = builder.userStatus;
        this.exception = builder.exception;
    }

    @NonNull
    public UserStatus getUserStatus() {
        return userStatus;
    }

    @Nullable
    public Exception getException() {
        return exception;
    }

    public static class Builder {

        private final UserStatus userStatus;
        private Exception exception;

        public Builder(@NonNull UserStatus userStatus) {
            this.userStatus = userStatus;
        }

        public Builder withException(@NonNull Exception exception) {
            this.exception = exception;
            return this;
        }

        public OnUserStatusChangedEvent build() {
            return new OnUserStatusChangedEvent(this);
        }

        private void validateEvent(OnUserStatusChangedEvent event) {
            //
        }
    }

}
