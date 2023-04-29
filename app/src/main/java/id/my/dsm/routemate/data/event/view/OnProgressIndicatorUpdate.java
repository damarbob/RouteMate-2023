package id.my.dsm.routemate.data.event.view;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Subscribed by Activity to update the {@link com.google.android.material.progressindicator.LinearProgressIndicator} progress value
 */
public class OnProgressIndicatorUpdate {

    public enum Event {
        DIRECTIONS_OBTAINED,
        OPTIMIZATION_FINISHED,
    }

    private final double value;
    private Event event;

    public OnProgressIndicatorUpdate(int value) {
        this.value = value;
    }

    public OnProgressIndicatorUpdate(int value, @NonNull Event event) {
        this.value = value;
        this.event = event;
    }

    public int getValue() {
        return (int) Math.ceil(value);
    }

    @Nullable
    public Event getEvent() {
        return event;
    }
}
