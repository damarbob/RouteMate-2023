package id.my.dsm.routemate.usecase.userdata;

import android.util.Log;

import javax.inject.Inject;

import id.my.dsm.routemate.data.repo.place.PlaceRepositoryN;
import id.my.dsm.routemate.data.repo.user.UserRepository;

public class RetrieveUserDataUseCase {

    private static final String TAG = "RetrieveUserDataUseCase";
    private final PlaceRepositoryN placeRepository;
    private final UserRepository userRepository;

    @Inject
    public RetrieveUserDataUseCase(UserRepository userRepository, PlaceRepositoryN placeRepository) {
        this.userRepository = userRepository;
        this.placeRepository = placeRepository;
    }

    /**
     * Fetch user data from server
     */
    public void invoke() {
        // TODO: Make an alternative option to load offline data
        if (userRepository.getLiveUser().getValue() != null)
            userRepository.retrieveUserData(userRepository.getLiveUser().getValue().getUid());
        else
            Log.d(TAG, "show: User unauthorized, cannot retrieve cloud data!");
    }

}
