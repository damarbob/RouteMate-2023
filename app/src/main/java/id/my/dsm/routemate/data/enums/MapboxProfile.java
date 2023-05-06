package id.my.dsm.routemate.data.enums;

import android.content.res.Resources;

import androidx.annotation.NonNull;

import com.mapbox.api.directions.v5.DirectionsCriteria;

import id.my.dsm.routemate.R;

public enum MapboxProfile {
    DRIVING,
    DRIVING_TRAFFIC,
    WALKING,
    CYCLING;

    /**
     * Convert to acceptable profile for Mapbox
     * @return Mapbox DirectionsCriteria string
     */
    @NonNull
    public String toDirectionsCriteria() {
        switch(this) {
            case DRIVING:
                return DirectionsCriteria.PROFILE_DRIVING;
            case DRIVING_TRAFFIC:
                return DirectionsCriteria.PROFILE_DRIVING_TRAFFIC;
            case WALKING:
                return DirectionsCriteria.PROFILE_WALKING;
            case CYCLING:
                return DirectionsCriteria.PROFILE_CYCLING;
            default:
                throw new IllegalStateException("MapboxProfile: toDirectionsCriteria: Unexpected value: " + this);
        }
    }

    /**
     * Convert to localized string for display purposes taken from string array resource.
     * @param resources application's resource
     * @return profile pretty string
     */
    @NonNull
    public String toPrettyString(Resources resources) {

        // Import string array to adapt localization changes
        String[] profiles = resources.getStringArray(R.array.vehicle_mapbox_profile);

        switch(this) {
            case DRIVING:
                return profiles[0];
            case DRIVING_TRAFFIC:
                return profiles[1];
            case WALKING:
                return profiles[2];
            case CYCLING:
                return profiles[3];
            default:
                throw new IllegalStateException("MapboxProfile: toPrettyString: Unexpected value: " + this);
        }
    }

}