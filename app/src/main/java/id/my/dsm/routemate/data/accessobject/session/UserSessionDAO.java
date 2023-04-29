package id.my.dsm.routemate.data.accessobject.session;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import javax.inject.Inject;

import id.my.dsm.routemate.BuildConfig;
import id.my.dsm.routemate.data.model.session.UserSession;
import id.my.dsm.routemate.data.model.userdata.UserData;

public class UserSessionDAO {

    private static final String TAG = UserSessionDAO.class.getSimpleName();

    private final DatabaseReference databaseReference;
    private static final String DB_NAME = "UserSession";

    @Inject
    public UserSessionDAO() {
        // Point the database reference to 'UserData' node in Firebase
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference().child(DB_NAME);
        databaseReference.keepSynced(true);
    }

    public Task<Void> insert(@NonNull UserSession userSession) {
        return databaseReference.child(userSession.getUid()).push().setValue(userSession);
    }

    public Task<Void> update(@NonNull DatabaseReference databaseReference, HashMap<String, Object> HashMap) {
        return databaseReference.updateChildren(HashMap);
    }

    public Task<Void> delete(String uid) {
        return databaseReference.child(uid).removeValue();
    }

    public Task<DataSnapshot> filterByUid(String uid) {
        return databaseReference.child(uid).limitToFirst(1).get();
    }

    public Task<DataSnapshot> filterById(DatabaseReference databaseReference, String id) {
        return databaseReference.child(id).limitToFirst(1).get();
    }

}
