package id.my.dsm.routemate.data.event.repo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class OnRepositoryUpdate<T> {

    public enum Event {
        RECORD_ADDED,
        RECORD_DELETED,
        RECORDS_CLEARED,
        ACTION_CREATE,
        ACTION_ADD,
        ACTION_DELETE,
        ACTION_CLEAR,

        ACTION_NONE,
        ACTION_SET_DEFAULT,
        ACTION_CLEAR_DEFAULT, // Deprecated TODO Future: Remove
    }

    private final Event event;

    public OnRepositoryUpdate(@NonNull Event event) {
        this.event = event;
    }

    @NonNull
    public Event getStatus() {
        return event;
    }

}
