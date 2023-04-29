package id.my.dsm.routemate.usecase.userdata;

import android.util.Log;

import javax.inject.Inject;

import id.my.dsm.routemate.data.repo.place.PlaceRepositoryN;
import id.my.dsm.routemate.data.repo.user.UserRepository;

public class RetrieveDSMUserUseCase {

    private static final String TAG = "RetrieveDSMUserUseCase";
    private final UserRepository userRepository;

    @Inject
    public RetrieveDSMUserUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Fetch DSMUser from server
     */
    public void invoke() {
        // TODO: Make an alternative option to load offline data
        if (userRepository.getLiveUser().getValue() != null)
            userRepository.retrieveDSMUser(userRepository.getLiveUser().getValue().getUid());
        else
            Log.d(TAG, "invoke: User unauthorized, cannot retrieve cloud data!");
    }

}
