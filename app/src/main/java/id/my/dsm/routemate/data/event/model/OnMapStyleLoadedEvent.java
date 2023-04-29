package id.my.dsm.routemate.data.event.model;

import androidx.annotation.NonNull;

import com.mapbox.mapboxsdk.maps.Style;

public final class OnMapStyleLoadedEvent {

    private final Style style;

    public OnMapStyleLoadedEvent(@NonNull Style style) {
        this.style = style;
    }

    @NonNull
    public Style getStyle() {
        return style;
    }

}
