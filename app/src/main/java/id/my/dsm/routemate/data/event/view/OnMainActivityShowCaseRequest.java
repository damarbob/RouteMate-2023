package id.my.dsm.routemate.data.event.view;

import smartdevelop.ir.eram.showcaseviewlib.listener.GuideListener;

public class OnMainActivityShowCaseRequest {

    public enum Event {
        MAP,
        FAB_DRAW,
        FAB_TRAFFIC_STYLE,
        FAB_START_NAVIGATION_VIEW,
        FAB_MARKER_INFO,
        FAB_MARKER_LOCK
    }

    private final Event event;
    private GuideListener onDismissListener;

    public OnMainActivityShowCaseRequest(Event event) {
        this.event = event;
    }

    public OnMainActivityShowCaseRequest(Event event, GuideListener onDismissListener) {
        this.event = event;
        this.onDismissListener = onDismissListener;
    }

    public Event getEvent() {
        return event;
    }

    public GuideListener getOnDismissListener() {
        return onDismissListener;
    }
}
