package id.my.dsm.routemate.data.enums;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public enum MapsAPI {
    GOOGLE,
    MAPBOX;

    @NonNull
    @Override
    public String toString() {
        switch (this) {
            case GOOGLE:
                return "Google";
            case MAPBOX:
                return "Mapbox";
            default:
                throw new IllegalStateException("Unexpected MapsAPI value: " + this);
        }
    }

    @Nullable
    public static MapsAPI fromString(String string) {
        switch (string) {
            case "Google":
                return MapsAPI.GOOGLE;
            case "Mapbox":
                return MapsAPI.MAPBOX;
            default:
                return null;
        }
    }

}
