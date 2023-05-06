package id.my.dsm.routemate.data.enums;

import android.content.res.Resources;

import androidx.annotation.NonNull;

import com.google.maps.model.TravelMode;

import id.my.dsm.routemate.R;

public enum GoogleProfile {
    DRIVING,
    WALKING,
    CYCLING,
    TRANSIT;

    /**
     * Convert to localized string for display purposes taken from string array resource.
     * @param resources application's resource
     * @return profile pretty string
     */
    @NonNull
    public String toPrettyString(Resources resources) {

        // Import string array to adapt localization changes
        String[] profiles = resources.getStringArray(R.array.vehicle_google_profile);

        switch(this) {
            case DRIVING:
                return profiles[0];
            case WALKING:
                return profiles[1];
            case CYCLING:
                return profiles[2];
            case TRANSIT:
                return profiles[3];
            default:
                throw new IllegalStateException("GoogleProfile: toPrettyString: Unexpected value: " + this);
        }
    }

    /**
     * Convert to acceptable routing profile for Google
     * @return Google TravelMode enum
     */
    @NonNull
    public TravelMode toTravelMode() {
        switch (this) {
            case DRIVING:
                return TravelMode.DRIVING;
            case WALKING:
                return TravelMode.WALKING;
            case CYCLING:
                return TravelMode.BICYCLING;
            case TRANSIT:
                return TravelMode.TRANSIT;
            default:
                throw new IllegalStateException("GoogleProfile: toTravelMode: Unexpected value: " + this);
        }
    }

}