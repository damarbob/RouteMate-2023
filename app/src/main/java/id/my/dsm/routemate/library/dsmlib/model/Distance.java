package id.my.dsm.routemate.library.dsmlib.model;

public class Distance {

    private int index;
    private String id; // Consists of origin's index and destination's index. ID assignment can be found in the repository
    private Location origin;
    private Location destination;
    private double distance;
    private double duration;

    // Used for deserialization
    public Distance() {
    }

    public Distance(Location origin, Location destination, double distance) {
        this.origin = origin;
        this.destination = destination;
        this.distance = distance;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Location getOrigin() {
        return origin;
    }

    public void setOrigin(Location origin) {
        this.origin = origin;
    }

    public Location getDestination() {
        return destination;
    }

    public void setDestination(Location location) {
        this.destination = location;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

}
