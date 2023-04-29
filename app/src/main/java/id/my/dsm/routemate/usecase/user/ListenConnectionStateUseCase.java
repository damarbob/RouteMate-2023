package id.my.dsm.routemate.usecase.user;

import javax.inject.Inject;

import id.my.dsm.routemate.data.repo.user.UserRepository;

/**
 * Listen connection state changes
 */
public class ListenConnectionStateUseCase {

    private final UserRepository userRepository;

    @Inject
    public ListenConnectionStateUseCase(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void invoke() {
        userRepository.listenConnectionState();
    }

}
