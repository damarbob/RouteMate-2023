package id.my.dsm.routemate.usecase.event;

import androidx.annotation.NonNull;

import org.greenrobot.eventbus.EventBus;

import id.my.dsm.routemate.data.event.view.OnViewStateChange;

public class DispatchViewStateEventUseCase {

    /**
     * Dispatch a {@link OnViewStateChange.Event} event globally
     * @param viewTag view reference in form of {@link String}
     * @param event {@link OnViewStateChange.Event} enum
     */
    public static void invoke(@NonNull String viewTag, @NonNull OnViewStateChange.Event event) {
        EventBus.getDefault().post(
                new OnViewStateChange(
                        viewTag,
                        event
                )
        );
    }

}
