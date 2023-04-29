package id.my.dsm.routemate.data.event.model;

import androidx.annotation.NonNull;

import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;

public final class OnMapSymbolClickedEvent {

    private final Symbol symbol;

    public OnMapSymbolClickedEvent(@NonNull Symbol symbol) {
        this.symbol = symbol;
    }

    @NonNull
    public Symbol getSymbol() {
        return symbol;
    }

}
