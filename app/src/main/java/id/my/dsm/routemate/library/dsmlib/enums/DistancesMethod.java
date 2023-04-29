package id.my.dsm.routemate.library.dsmlib.enums;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.Contract;

public enum DistancesMethod {
    AIR,
    TRAVEL,
    DIRECTIONS;

    // TODO Future: Localization
    public static DistancesMethod fromString(String string) {
        switch (string) {
            case "Air Distance":
                return AIR;
            case "Travel Distance":
                return TRAVEL;
            case "Directions Distance":
                return DIRECTIONS;
            default:
                return null;
        }
    }

    // TODO Future: Localization
    @NonNull
    @Override
    public String toString() {
        switch (this) {
            case AIR:
                return "Air Distance";
            case TRAVEL:
                return "Travel Distance";
            case DIRECTIONS:
                return "Directions Distance";
            default:
                throw new IllegalStateException("Unexpected DistancesMethod value: " + this);
        }
    }
}