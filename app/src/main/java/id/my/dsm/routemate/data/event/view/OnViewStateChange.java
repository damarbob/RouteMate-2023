package id.my.dsm.routemate.data.event.view;

import androidx.annotation.NonNull;

/**
 * Dispatch event from a {@link android.view.View} across the project scope using {@link org.greenrobot.eventbus.EventBus}
 */
public final class OnViewStateChange {

    public enum Event {
        STATE_ENABLED,
        STATE_DISABLED,
        STATE_CLICKED,
    }

    private final String viewTag;
    private final Event event;

    public OnViewStateChange(String viewTag, Event event) {
        this.viewTag = viewTag;
        this.event = event;
    }

    /**
     * Get viewTag to identify which {@link android.view.View} that dispatch the event
     * @return {@link String} viewTag
     */
    @NonNull
    public String getViewTag() {
        return viewTag;
    }

    /**
     * Get event type dispatched from the {@link android.view.View}
     * @return {@link Event} enum instance
     */
    @NonNull
    public Event getEvent() {
        return event;
    }
}
