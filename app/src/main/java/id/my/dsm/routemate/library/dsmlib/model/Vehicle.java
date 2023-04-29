package id.my.dsm.routemate.library.dsmlib.model;

import android.content.res.Resources;
import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.maps.model.TravelMode;
import com.mapbox.api.directions.v5.DirectionsCriteria;

import java.util.ArrayList;
import java.util.List;

import id.my.dsm.routemate.R;

public class Vehicle {

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

    public static final int COLOR_DEFAULT = Color.parseColor("#9e9e9e");

    // Basic attributes
    private String id;
    private String name;
    private MapboxProfile mapboxProfile = MapboxProfile.DRIVING;
    private GoogleProfile googleProfile = GoogleProfile.DRIVING;
    private int color = COLOR_DEFAULT;
    private boolean isDefault;

    // Constraints
    private double capacity;
    private int dispatchLimit = 1000;

    // Used for deserialization
    public Vehicle() {
    }

    public Vehicle(String name, MapboxProfile mapboxProfile, double capacity) {
        this.name = name;
        this.mapboxProfile = mapboxProfile;
        this.capacity = capacity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MapboxProfile getMapboxProfile() {
        return mapboxProfile;
    }

    public void setMapboxProfile(MapboxProfile mapboxProfile) {
        this.mapboxProfile = mapboxProfile;
    }

    public void setGoogleProfile(GoogleProfile googleProfile) {
        this.googleProfile = googleProfile;
    }

    public GoogleProfile getGoogleProfile() {
        return googleProfile;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public double getCapacity() {
        return capacity;
    }

    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public int getDispatchLimit() {
        return dispatchLimit;
    }

    public void setDispatchLimit(int dispatchLimit) {
        this.dispatchLimit = dispatchLimit;
    }

    public static class Toolbox {

        /**
         * Get the default vehicle from a List of Vehicle
         * @param vehicles List of Vehicle
         * @return a default Vehicle
         */
        @Nullable
        public static Vehicle getDefaultVehicle(List<Vehicle> vehicles) {

            for (Vehicle v : vehicles) {
                if (v.isDefault())
                    return v; // Return the default vehicle
            }

            // Return the first vehicle REMOVED

            return null; // Return empty
        }

        /**
         * Set the default vehicle and clear the old
         * @param vehicles List of Vehicle
         * @param record Vehicle to be default
         */
        public static void setDefaultVehicle(List<Vehicle> vehicles, Vehicle record) {

            Vehicle defaultVehicle = Vehicle.Toolbox.getDefaultVehicle(vehicles);

            if (defaultVehicle != null)
                defaultVehicle.setDefault(false);

            record.setDefault(true);

        }

        /**
         * Clear the default
         * @param record default Vehicle
         */
        public static void clearDefaultVehicle(Vehicle record) {
            record.setDefault(false);
        }

        /**
         * Get a vehicle by its vehicle index
         * @param vehicles List of Vehicle
         * @param vehicleId int of vehicle index
         * @return a {@link Vehicle} instance
         */
        @Nullable
        public static Vehicle getVehicleById(List<Vehicle> vehicles, @NonNull String vehicleId) {

            Vehicle vehicle = null; // In case the vehicle not found

            for (Vehicle v : vehicles)
                if (v.getId() != null && v.getId().equals(vehicleId))
                    vehicle = v;

            return vehicle;

        }

        /**
         * Filter vehicles by MapboxProfile
         * @param vehicles List of Vehicle
         * @param mapboxProfile enum of MapboxProfile
         * @return filtered List of Vehicle
         */
        public List<Vehicle> getVehiclesByMapboxProfile(List<Vehicle> vehicles, MapboxProfile mapboxProfile) {
            List<Vehicle> profileFilteredVehicles = new ArrayList<>();

            for (int i = 0; i < vehicles.size(); i++) {
                Vehicle vehicle = vehicles.get(i);

                if (vehicle.getMapboxProfile() == mapboxProfile) {
                    profileFilteredVehicles.add(vehicle);
                }
            }

            return profileFilteredVehicles;
        }

        /**
         * Convert profile index to MapboxProfile
         * @param profileIndex int of profile index
         * @return enum of MapboxProfile
         */
        public static MapboxProfile convertProfileIndexToMapboxProfile(int profileIndex) {
            switch (profileIndex) {
                case 0:
                    return MapboxProfile.DRIVING;
                case 1:
                    return MapboxProfile.DRIVING_TRAFFIC;
                case 2:
                    return MapboxProfile.WALKING;
                case 3:
                    return MapboxProfile.CYCLING;
                default:
                    throw new IllegalStateException("convertProfileIndexToMapboxProfile: Unexpected value: " + profileIndex);
            }
        }

        /**
         * Convert profile index to GoogleProfile
         * @param profileIndex int of profile index
         * @return enum of GoogleProfile
         */
        public static GoogleProfile convertProfileIndexToGoogleProfile(int profileIndex) {
            switch (profileIndex) {
                case 0:
                    return GoogleProfile.DRIVING;
                case 1:
                    return GoogleProfile.WALKING;
                case 2:
                    return GoogleProfile.CYCLING;
                case 3:
                    return GoogleProfile.TRANSIT;
                default:
                    throw new IllegalStateException("convertProfileIndexToGoogleProfile: Unexpected value: " + profileIndex);
            }
        }

        /**
         * Convert MapboxProfile to profile index
         * @param mapboxProfile enum of MapboxProfile
         * @return int of profile index
         */
        public static int convertMapboxProfileToProfileIndex(MapboxProfile mapboxProfile) {
            switch (mapboxProfile) {
                case DRIVING:
                    return 0;
                case DRIVING_TRAFFIC:
                    return 1;
                case WALKING:
                    return 2;
                case CYCLING:
                    return 3;
                default:
                    throw new IllegalStateException("convertMapboxProfileToProfileIndex: Unexpected value: " + mapboxProfile);
            }
        }

        /**
         * Convert GoogleProfile to profile index
         * @param googleProfile enum of GoogleProfile
         * @return int of profile index
         */
        public static int convertGoogleProfileToProfileIndex(GoogleProfile googleProfile) {
            switch (googleProfile) {
                case DRIVING:
                    return 0;
                case WALKING:
                    return 1;
                case CYCLING:
                    return 2;
                case TRANSIT:
                    return 3;
                default:
                    throw new IllegalStateException("convertGoogleProfileToProfileIndex: Unexpected value: " + googleProfile);
            }
        }

    }

}
