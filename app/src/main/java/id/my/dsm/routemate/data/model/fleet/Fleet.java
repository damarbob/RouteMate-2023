package id.my.dsm.routemate.data.model.fleet;

import android.graphics.Color;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import id.my.dsm.routemate.data.enums.GoogleProfile;
import id.my.dsm.routemate.data.enums.MapboxProfile;
import id.my.dsm.vrpsolver.model.Vehicle;

public class Fleet {

    public static final int COLOR_DEFAULT = Color.parseColor("#9e9e9e");

    private String id;
    private String name;
    private MapboxProfile mapboxProfile = MapboxProfile.DRIVING;
    private GoogleProfile googleProfile = GoogleProfile.DRIVING;
    private Vehicle vehicle;
    private int color = COLOR_DEFAULT;

    public Fleet() {
    }

    public Fleet(String name, Vehicle vehicle) {
        this.name = name;
        this.vehicle = vehicle;

        this.id = vehicle.getId();
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

    public GoogleProfile getGoogleProfile() {
        return googleProfile;
    }

    public void setGoogleProfile(GoogleProfile googleProfile) {
        this.googleProfile = googleProfile;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
        if (vehicle != null)
            this.id = vehicle.getId();
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public static class Toolbox {
        /**
         * Get a vehicle by its vehicle index
         * @param fleets List of Vehicle
         * @param vehicleId int of vehicle index
         * @return a {@link Vehicle} instance
         */
        @Nullable
        public static Fleet getFleetById(List<Fleet> fleets, @NonNull String vehicleId) {

            Fleet fleet = null; // In case the fleet not found

            for (Fleet v : fleets)
                if (v.getId() != null && v.getId().equals(vehicleId))
                    fleet = v;

            return fleet;

        }

        /**
         * Filter fleets by MapboxProfile
         * @param fleets List of Vehicle
         * @param mapboxProfile enum of MapboxProfile
         * @return filtered List of Vehicle
         */
        public List<Fleet> getFleetsByMapboxProfile(List<Fleet> fleets, MapboxProfile mapboxProfile) {
            List<Fleet> profileFilteredVehicles = new ArrayList<>();

            for (int i = 0; i < fleets.size(); i++) {
                Fleet vehicle = fleets.get(i);

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
