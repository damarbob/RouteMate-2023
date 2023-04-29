package id.my.dsm.routemate.library.dsmlib.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Location {

    public enum Profile {
        SOURCE,
        DESTINATION
    }

    // Declare destination attributes
    private String id;
    private LatLngAlt latLngAlt; // Place location in latitude longitude
    private Profile profile;
    private double demands; // Number of demands in capacitated vrp

    public Location() {
    }

    public Location(LatLngAlt latLngAlt, Profile profile) {
        this.id = UUID.randomUUID().toString();
        this.latLngAlt = latLngAlt;
        this.profile = profile;
    }

    public Location(LatLngAlt latLngAlt, Profile profile, double demands) {
        this.id = UUID.randomUUID().toString();
        this.latLngAlt = latLngAlt;
        this.profile = profile;
        this.demands = demands;
    }

    public String getId() {
        return id;
    }

    public LatLngAlt getLatLngAlt() {
        return latLngAlt;
    }

    public void setLatLngAlt(LatLngAlt latLngAlt) {
        this.latLngAlt = latLngAlt;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public double getDemands() {
        return demands;
    }

    public void setDemands(double demands) {
        this.demands = demands;
    }

    public static class Toolbox {

        /**
         * Get {@link Location} instances that has <i>Source</i> mapboxProfile.
         * @param locations List of {@link Location}
         * @return an {@link List} of {@link Location} instances
         */
        public static List<Location> getSources(List<Location> locations) {
            List<Location> sources = new ArrayList<>();

            for (int i = 0; i < locations.size(); i++) {
                if (locations.get(i).getProfile() == Profile.SOURCE) {
                    sources.add(locations.get(i));
                }
            }

            return sources;
        }

        /**
         * Get {@link Location} instances that has <i>Destination</i> mapboxProfile.
         * @param locations List of {@link Location}
         * @return an {@link ArrayList} of {@link Location} instances
         */
        public static List<Location> getDestinations(List<Location> locations) {
            ArrayList<Location> destinations = new ArrayList<>();

            for (int i = 0; i < locations.size(); i++) {
                if (locations.get(i).getProfile() == Profile.DESTINATION) {
                    destinations.add(locations.get(i));
                }
            }

            return destinations;
        }

        /**
         * Filter locations by the placeProfile
         * @param locations ArrayList of {@link Location} instances
         * @param placeProfile {@link Location.Profile} of a {@link Location}
         * @return filtered ArrayList of {@link Location} instances
         */
        public static List<Location> getByProfile(List<Location> locations, Profile placeProfile) {
            ArrayList<Location> profileFilteredVehicles = new ArrayList<>();

            for (int i = 0; i < locations.size(); i++) {
                Location location = locations.get(i);

                if (location.getProfile() == placeProfile) {
                    profileFilteredVehicles.add(location);
                }
            }

            return profileFilteredVehicles;
        }

        /**
         * Sort the locations to be the source first then the destination
         * @param locations ArrayList of {@link Location} instances
         * @param isSingleSource whether to add all or the first source only
         * @return sorted List of {@link Location} instances
         */
        public static List<Location> getSortedPlaces(List<Location> locations, boolean isSingleSource) {

            ArrayList<Location> sortedPlaces = new ArrayList<>();

            if (isSingleSource)
                sortedPlaces.add(getByProfile(locations, Profile.SOURCE).get(0));
            else
                sortedPlaces.addAll(getByProfile(locations, Profile.SOURCE));

            sortedPlaces.addAll(getByProfile(locations, Profile.DESTINATION));

            return sortedPlaces;

        }

    }

}
