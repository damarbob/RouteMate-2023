package id.my.dsm.routemate.data.source.user;

import android.app.Activity;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.FirebaseDatabase;

import javax.inject.Inject;

import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

/**
 * Handles communication to authorization system
 */
public class UserDataSource {
    private final FirebaseAuth auth;

    @Inject
    public UserDataSource() {
        auth = FirebaseAuth.getInstance();
    }

    /**
     * Sign user up to {@link FirebaseAuth}.
     * @param email email string
     * @param password password string
     * @return {@link Task} instance of {@link AuthResult}
     */
    public Task<AuthResult> signUp(String email, String password) {
        return auth.createUserWithEmailAndPassword(email, password);
    }

    /**
     * Sign user in to {@link FirebaseAuth}.
     * @param email email string
     * @param password password string
     * @return {@link Task} instance of {@link AuthResult}
     */
    public Task<AuthResult> signIn(String email, String password) {
        return auth.signInWithEmailAndPassword(email, password);
    }

    /**
     * Updates {@link FirebaseUser} display name.
     * @param user {@link FirebaseUser} instance
     * @param displayName display name string
     * @return {@link Task} instance of {@link Void}
     */
    public Task<Void> updateDisplayName(FirebaseUser user, String displayName) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(displayName)
                .build();

        return user.updateProfile(profileUpdates);
    }

    /**
     * Get current {@link FirebaseUser} instance.
     * @return {@link FirebaseUser} instance
     */
    public FirebaseUser getUser() {
        return auth.getCurrentUser();
    }

}
