package id.my.dsm.routemate.data.event.network;

import androidx.annotation.Nullable;

import java.util.List;

import id.my.dsm.routemate.data.place.Place;

public class OnMapboxGeocodingResponse {

    public enum Status {
        FAILED,
        SUCCESS
    }

    public enum Type {
        FORWARD,
        REVERSE
    }

    private Status status;
    private Type type;
    private List<Place> result;
    private String message; // Mostly error message

    public OnMapboxGeocodingResponse(Status status, Type type, String message) {
        this.status = status;
        this.type = type;
        this.message = message;
    }

    public OnMapboxGeocodingResponse(Status status, Type type, List<Place> result) {
        this.status = status;
        this.type = type;
        this.result = result;
    }

    public Status getStatus() {
        return status;
    }

    public Type getType() {
        return type;
    }

    @Nullable
    public List<Place> getResult() {
        return result;
    }

    public String getMessage() {
        return message;
    }
}
