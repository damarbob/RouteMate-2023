package id.my.dsm.routemate.data.accessobject.user;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import id.my.dsm.routemate.BuildConfig;
import id.my.dsm.routemate.data.model.user.DSMUser;

@Singleton
public class DSMUserDAO {
    private static final String TAG = DSMUserDAO.class.getSimpleName();

    private final DatabaseReference databaseReference;

    private static final String DB_NAME = "DSMUser";

    @Inject
    public DSMUserDAO() {
        // Point the database reference to 'DSMUser' node in Firebase
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference().child(DB_NAME);
    }

    /**
     * Insert a record into {@link DSMUser} node.
     *
     * @param user a {@link DSMUser} instance
     * @param uid a user id generated from FirebaseAuth
     * @return a {@link Task} instance
     */
    public Task<Void> insert(DSMUser user, String uid) {
        return databaseReference.child(uid).push().setValue(user);
    }

    public Task<Void> update(DatabaseReference databaseReference, HashMap<String, Object> HashMap) {
        return databaseReference.updateChildren(HashMap);
    }

    public Task<DataSnapshot> filterByUid(String uid) {
        return databaseReference.child(uid).limitToFirst(1).get();
    }

}
