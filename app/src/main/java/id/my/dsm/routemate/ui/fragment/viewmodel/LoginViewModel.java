package id.my.dsm.routemate.ui.fragment.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import id.my.dsm.routemate.data.repo.user.UserRepository;
import id.my.dsm.routemate.data.repo.user.UserStatus;

@HiltViewModel
public class LoginViewModel extends ViewModel {

    // Dependencies
    @Inject
    UserRepository userRepository;

    @Inject
    public LoginViewModel() {

    }

    public FirebaseUser getUser() {
        return userRepository.getLiveUser().getValue();
    }

    public LiveData<UserStatus> getLiveUserStatus() {
        return userRepository.getLiveUserStatus();
    }

}