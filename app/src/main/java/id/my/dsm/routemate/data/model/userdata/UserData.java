package id.my.dsm.routemate.data.model.userdata;

import com.google.firebase.database.Exclude;

import java.util.List;

import id.my.dsm.routemate.data.model.maps.MapboxDirectionsRoute;
import id.my.dsm.routemate.data.place.Place;
import id.my.dsm.routemate.library.dsmlib.enums.OptimizationMethod;
import id.my.dsm.routemate.library.dsmlib.model.MatrixElement;
import id.my.dsm.routemate.library.dsmlib.model.Solution;
import id.my.dsm.routemate.library.dsmlib.model.Vehicle;

/**
 * UserData model that is used for saving the user's data remotely
 */
public class UserData {

    private String uid;
    private String name; // Data Name
    private List<Place> places;
    private List<Vehicle> vehicles;
    private List<MatrixElement> matrix;
    private List<Solution> solutions;
    private List<MapboxDirectionsRoute> mapboxDirectionsRoutes;
    private OptimizationMethod optimizationMethod;
    private Boolean usingAdvancedAlgorithm;

    // Used for deserialization
    public UserData() {
    }

    private UserData(Builder builder) {
        this.uid = builder.uid;
        this.name = builder.name;
        this.places = builder.places;
        this.vehicles = builder.vehicles;
        this.matrix = builder.matrix;
        this.solutions = builder.solutions;
        this.mapboxDirectionsRoutes = builder.mapboxDirectionsRoutes;
        this.optimizationMethod = builder.optimizationMethod;
        this.usingAdvancedAlgorithm = builder.advancedAlgorithm;
    }

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public List<Place> getPlaces() {
        return places;
    }

    public List<Vehicle> getVehicles() {
        return vehicles;
    }

    public List<MatrixElement> getMatrix() {
        return matrix;
    }

    public List<Solution> getSolutions() {
        return solutions;
    }

    public List<MapboxDirectionsRoute> getMapboxDirectionsRoutes() {
        return mapboxDirectionsRoutes;
    }

    public OptimizationMethod getOptimizationMethod() {
        return optimizationMethod;
    }

    public Boolean getUsingAdvancedAlgorithm() {
        return usingAdvancedAlgorithm;
    }

    public static class Builder {

        private final String uid;
        private String name;
        private List<Place> places;
        private List<Vehicle> vehicles;
        private List<MatrixElement> matrix;
        private List<Solution> solutions;
        private List<MapboxDirectionsRoute> mapboxDirectionsRoutes;
        private OptimizationMethod optimizationMethod;
        private Boolean advancedAlgorithm;

        public Builder(String uid) {
            this.uid = uid;
        }

        @Exclude
        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        @Exclude
        public Builder withPlaces(List<Place> places) {
            this.places = places;
            return this;
        }

        @Exclude
        public Builder withVehicles(List<Vehicle> vehicles) {
            this.vehicles = vehicles;
            return this;
        }

        @Exclude
        public Builder withMatrix(List<MatrixElement> matrix) {
            this.matrix = matrix;
            return this;
        }

        @Exclude
        public Builder withSolutions(List<Solution> solutions) {
            this.solutions = solutions;
            return this;
        }

        @Exclude
        public Builder withMapboxDirectionsRoutes(List<MapboxDirectionsRoute> mapboxDirectionsRoutes) {
            this.mapboxDirectionsRoutes = mapboxDirectionsRoutes;
            return this;
        }

        @Exclude
        public Builder withOptimizationMethod(OptimizationMethod optimizationMethod) {
            this.optimizationMethod = optimizationMethod;
            return this;
        }

        @Exclude
        public Builder withAdvancedAlgorithm(Boolean advancedAlgorithm) {
            this.advancedAlgorithm = advancedAlgorithm;
            return this;
        }

        @Exclude
        public UserData build() {
            return new UserData(this);
        }

    }

}
