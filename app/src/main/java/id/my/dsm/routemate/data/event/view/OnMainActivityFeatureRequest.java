package id.my.dsm.routemate.data.event.view;

public class OnMainActivityFeatureRequest {

    public enum Event {
        OPEN_DRAWER,
        HIDE_SEARCH_PANEL,
        PULL_BOTTOM_PANEL,
        HIDE_BOTTOM_PANEL,
    }

    private Event event;

    public OnMainActivityFeatureRequest(Event event) {
        this.event = event;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }
}
