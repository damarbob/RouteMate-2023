package id.my.dsm.routemate.data.event.model;

import androidx.annotation.NonNull;

import id.my.dsm.routemate.data.model.session.UserSession;

public class OnUpdateUserSession {

    public enum Status {
        FAILED,
        SUCCESS,
        UID_NOT_FOUND,
    }

    public enum Event {
        INSERT,
        UPDATE,
        RETRIEVE,
        DELETE
    }

    // Action taken upon successful update (Status.SUCCESS)
    public enum Action {
        NONE,
        UPDATE,
        OPTIMIZATION
    }

    private final Status status;
    private final Event event;
    private UserSession userSession;
    private Action action;
    private String message;

    public OnUpdateUserSession(Status status, Event event, UserSession userSession, Action action) {
        this.status = status;
        this.event = event;
        this.userSession = userSession;
        this.action = action;
    }

    public OnUpdateUserSession(Status status, Event event, String message) {
        this.status = status;
        this.event = event;
        this.message = message;
    }

    @NonNull
    public Status getStatus() {
        return status;
    }

    @NonNull
    public Event getEvent() {
        return event;
    }

    public UserSession getUserSession() {
        return userSession;
    }

    @NonNull
    public Action getAction() {
        return action != null ? action : Action.NONE;
    }

    @NonNull
    public String getMessage() {
        return message != null ? message : "No message";
    }

}
