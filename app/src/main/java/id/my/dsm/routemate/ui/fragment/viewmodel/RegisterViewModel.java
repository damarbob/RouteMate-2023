package id.my.dsm.routemate.ui.fragment.viewmodel;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import id.my.dsm.routemate.data.model.user.DSMUser;
import id.my.dsm.routemate.data.event.model.OnUserStatusChangedEvent;
import id.my.dsm.routemate.data.repo.user.UserRepository;

@HiltViewModel
public class RegisterViewModel extends ViewModel {

    // Dependencies
    @Inject
    UserRepository userRepository;
    private String newUserFullName;

    @Inject
    public RegisterViewModel() {

        EventBus.getDefault().register(this); // Register to event
    }

    @Override
    protected void onCleared() {
        super.onCleared();

        EventBus.getDefault().unregister(this); // Unregister to event

    }

    public void signUp(String fullName, String email, String password) {
        newUserFullName = fullName;
        userRepository.signUp(email, password);
    }

    /**
     * Get current FirebaseUser
     */
    @Nullable
    public FirebaseUser getUser() {
        return userRepository.getLiveUser().getValue();
    }

    // Subscribe to OnUserStatusChangedEvent event
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void _onUserRepositoryEvent(OnUserStatusChangedEvent event) {

        if (getUser() == null)
            return;

        // From here on, user has logged into FirebaseAuth
        DSMUser newDSMUser = new DSMUser(
                getUser().getUid(),
                newUserFullName,
                getUser().getEmail()
        );

        // Handles user sign in status
        switch(event.getUserStatus()) {
            // From this status on, user has logged into FirebaseAuth and is not null anymore (although its nullable)
            case SIGNUP_SUCCESS:

                // Update FirebaseUser display name
                userRepository.updateDisplayName(userRepository.getLiveUser().getValue(), newDSMUser);

                break;

            case UPDATE_USER_SUCCESS:

                // Insert into DSMUser node in database
                userRepository.insert(getUser(), newDSMUser);

                break;

            case UPDATE_USER_FAILURE:

                // TODO: The update display name has stated as failed but user has signed in, just proceed to home and do update later.

                break;

            case INSERT_RECORD_SUCCESS:

                break;

            case INSERT_RECORD_FAILURE:

                // TODO: The insert record has stated as failed but user has signed in, just proceed to home and do insert record later.

                break;

        }

    }

}