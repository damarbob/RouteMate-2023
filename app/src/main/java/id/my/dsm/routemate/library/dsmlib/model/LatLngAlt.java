package id.my.dsm.routemate.library.dsmlib.model;

public class LatLngAlt {

    private double latitude;
    private double longitude;
    private double altitude = 0.0;

    public LatLngAlt() {
        this.latitude = 0.0;
        this.longitude = 0.0;
    }

    public LatLngAlt(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LatLngAlt(double latitude, double longitude, double altitude) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.altitude = altitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

}
