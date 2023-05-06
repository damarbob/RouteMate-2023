package id.my.dsm.routemate.data.model.place;

import android.content.res.Resources;

import androidx.annotation.Nullable;

import com.google.firebase.database.Exclude;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;

import java.util.ArrayList;
import java.util.List;

import id.my.dsm.routemate.R;
import id.my.dsm.vrpsolver.model.LatLngAlt;
import id.my.dsm.vrpsolver.model.Location;

public class Place {

    private String id;
    private String name; // Place name
    private String description; // Place description
    private String note; // Additional note from user about the destination

    private Location location;

    private Symbol symbol; // Holds the maps symbol for the destination

    private boolean bypassDemands = true; // Whether the demands constraint should be bypassed
    private double bypassedDemands; // To store the demands value while it is bypassed

    public Place() {
        if (location != null)
            this.id = location.getId();
    }

    public Place(Builder builder) {
        this.name = builder.name;
        this.description = builder.description;
        this.note = builder.note;
        this.location = builder.location;
        this.symbol = builder.symbol;

        this.id = location.getId();
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
        if (location != null)
            this.id = location.getId();
    }

    @Exclude
    public Symbol getSymbol() {
        return symbol;
    }

    @Exclude
    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }

    public LatLng getMapboxLatLng() {
        return new LatLng(
                location.getLatLngAlt().getLatitude(),
                location.getLatLngAlt().getLongitude()
        );
    }

    public com.google.maps.model.LatLng getGoogleLatLng() {
        return new com.google.maps.model.LatLng(
                location.getLatLngAlt().getLatitude(),
                location.getLatLngAlt().getLongitude()
        );
    }

    public String getLocalizedProfile(Resources resources) {

        // Import string array to adapt localization changes
        String[] profiles = resources.getStringArray(R.array.location_profile);

        switch (this.getLocation().getProfile()) {
            case SOURCE:
                return profiles[0];
            case DESTINATION:
                return profiles[1];
        }

        return null;
    }

    public boolean isBypassDemands() {
        return bypassDemands;
    }

    public void setBypassDemands(boolean bypassDemands) {
        this.bypassDemands = bypassDemands;
    }

    /**
     * Special function to set demands and unbypass demands constraint. Bypassed demands will reset to 0.
     *
     * @param demands a double value of demands
     */
    public void setUnBypassDemands(double demands) {
        this.location.setDemands(demands);
        bypassDemands = false; // Unbypass demands constraint
    }

    /**
     * Bypass demands constraint. Will set demands to 0 and store the last
     * demands value to bypassedDemands variable. Invoke getBypassedDemands()
     * to getInstance the bypassedDemands' value.
     */
    public void bypassDemands() {
        bypassDemands = true; // Bypass demands constraint
        bypassedDemands = this.location.getDemands() > 0 ? this.location.getDemands() : bypassedDemands;
        this.location.setDemands(0.0);
    }

    public double getBypassedDemands() {
        return bypassedDemands;
    }

    public void setBypassedDemands(double bypassedDemands) {
        this.bypassedDemands = bypassedDemands;
    }

    public static class Builder {

        private String name;
        private String description;
        private String note;
        private Symbol symbol;

        private final Location location;

        public Builder(Location location) {
            this.location = location;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withNote(String note) {
            this.note = note;
            return this;
        }

        public Builder withSymbol(Symbol symbol) {
            this.symbol = symbol;
            return this;
        }

        public Place build() {
            return new Place(this);
        }

    }

    public static class Toolbox {

        public static List<Location> getDSMPlaces(List<Place> places) {
            List<Location> locations = new ArrayList<>();
            for (Place o : places) {
                locations.add(o.location);
            }
            return locations;
        }

        /**
         * Get {@link Place} instances that has <i>Source</i> mapboxProfile.
         * @param places List of Places
         * @return an {@link List} of {@link Place} instances
         */
        public static List<Place> getSources(List<Place> places) {
            List<Place> sources = new ArrayList<>();

            for (int i = 0; i < places.size(); i++) {
                if (places.get(i).location.getProfile() == Location.Profile.SOURCE) {
                    sources.add(places.get(i));
                }
            }

            return sources;
        }

        /**
         * Get {@link Place} instances that has <i>Destination</i> mapboxProfile.
         * @param places List of Places
         * @return an {@link ArrayList} of {@link Place} instances
         */
        public static List<Place> getDestinations(List<Place> places) {
            ArrayList<Place> destinations = new ArrayList<>();

            for (int i = 0; i < places.size(); i++) {
                if (places.get(i).location.getProfile() == Location.Profile.DESTINATION) {
                    destinations.add(places.get(i));
                }
            }

            return destinations;
        }

        /**
         * Get LatLng of all places
         * @param places List of Places
         * @return List of google-accepted LatLngs
         */
        public static List<LatLngAlt> getLatLngs(List<Place> places) {

            ArrayList<LatLngAlt> latLngs = new ArrayList<>();

            for (Place o : places) {

                double lat = o.getLocation().getLatLngAlt().getLatitude();
                double lng = o.getLocation().getLatLngAlt().getLongitude();

                latLngs.add(
                        new LatLngAlt(lat, lng));
            }

            return latLngs;

        }

        /**
         * Get LatLng of all places
         * @param places List of Places
         * @return List of google-accepted LatLngs
         */
        public static List<com.google.maps.model.LatLng> getGoogleLatLngs(List<Place> places) {

            ArrayList<com.google.maps.model.LatLng> latLngs = new ArrayList<>();

            for (Place o : places)
                latLngs.add(o.getGoogleLatLng());

            return latLngs;

        }

        /**
         * Filter objectives by the placeProfile
         * @param places ArrayList of {@link Place} instances
         * @param placeProfile int of Place Profile
         * @return filtered ArrayList of {@link Place} instances
         */
        public static List<Place> getByProfile(List<Place> places, Location.Profile placeProfile) {
            List<Place> profileFilteredVehicles = new ArrayList<>();

            for (int i = 0; i < places.size(); i++) {
                Place place = places.get(i);

                if (place.location.getProfile() == placeProfile) {
                    profileFilteredVehicles.add(place);
                }
            }

            return profileFilteredVehicles;
        }

        /**
         * Create a Place from LatLng
         * @param latLng a LatLng instance
         * @param profile int Place profile
         * @return a new Place instance
         */
        public static Place createObjectiveFromMapboxLatLng(com.mapbox.mapboxsdk.geometry.LatLng latLng, Location.Profile profile) {

            Location location = new Location(
                    new LatLngAlt(latLng.getLatitude(), latLng.getLongitude()),
                    profile
            );

            return new Place.Builder(location)
                    .withName("Place")
                    .build();
        }

        @Nullable
        public static Place getById(List<Place> places, String id) {
            for (int i = 0; i < places.size(); i++) {
                Place place = places.get(i);

                if (place.getId().equals(id)) {
                    return place;
                }
            }
            return null;
        }

    }

}
