package id.my.dsm.routemate.data.event.view;

public final class OnBottomSheetStateChanged {

    int state;

    public OnBottomSheetStateChanged(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }
    
}
