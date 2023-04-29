package id.my.dsm.routemate.data.event.model;

public final class OnOptimizationUpdate {

    public enum Event {
        STARTED, // Indicates that optimization process has started
        FINISHED, // Indicates that optimization process has finished
    }

    private final Event event;

    public OnOptimizationUpdate(Event event) {
        this.event = event;
    }

    public Event getEvent() {
        return event;
    }
}
