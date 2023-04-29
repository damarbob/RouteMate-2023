package id.my.dsm.routemate.ui.fragment.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import id.my.dsm.routemate.data.repo.user.UserRepository;
import id.my.dsm.routemate.data.repo.user.UserStatus;

@HiltViewModel
public class HomeViewModel extends ViewModel {

    // Dependencies

    // State
    private boolean isSignInAsked = false;
    private boolean isGreeted = false;

    @Inject
    public HomeViewModel() {
    }

    public boolean isSignInAsked() {
        return isSignInAsked;
    }

    public void setSignInAsked(boolean signInAsked) {
        isSignInAsked = signInAsked;
    }

    public boolean isGreeted() {
        return isGreeted;
    }

    public void setGreeted(boolean greeted) {
        isGreeted = greeted;
    }
}