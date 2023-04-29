package id.my.dsm.routemate.usecase.user;

import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;

import id.my.dsm.routemate.data.repo.user.UserRepository;

/**
 * Get FirebaseUser display name if any, return "Guest" or "User" if doesn't.
 */
public class GetDisplayNameUseCase {

    private final UserRepository userRepository;

    @Inject
    public GetDisplayNameUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String invoke() {

        FirebaseUser user = userRepository.getLiveUser().getValue();

        if (user == null)
            return "Guest";

        if (user.getDisplayName() == null)
            return "User";

        return user.getDisplayName();

    }

}
