package id.my.dsm.routemate.data.accessobject.data;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.common.collect.Iterables;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import javax.inject.Inject;

import id.my.dsm.routemate.BuildConfig;
import id.my.dsm.routemate.data.model.user.DSMUser;
import id.my.dsm.routemate.data.model.userdata.UserData;

/**
 * Handles communication with UserData node in Firebase Realtime Database
 */
public class DataDAO {

    private static final String TAG = DataDAO.class.getSimpleName();

    private final DatabaseReference databaseReference;
    private static final String DB_NAME = "UserData";

    @Inject
    public DataDAO() {
        // Point the database reference to 'UserData' node in Firebase
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        databaseReference = db.getReference().child(DB_NAME);
        databaseReference.keepSynced(true);
    }

    public Task<Void> insert(@NonNull UserData userData) {
        return databaseReference.child(userData.getUid()).push().setValue(userData);
    }

    public Task<Void> update(@NonNull DatabaseReference databaseReference, HashMap<String, Object> HashMap) {
        return databaseReference.updateChildren(HashMap);
    }

    public Task<Void> delete(String uid) {
        return databaseReference.child(uid).removeValue();
    }

    public Task<Void> deletePlaces(String uid) {
        return databaseReference.child(uid).child("places").removeValue();
    }

    public Task<Void> deleteVehicles(String uid) {
        return databaseReference.child(uid).child("vehicles").removeValue();
    }

    public Task<Void> deleteMatrix(String uid) {
        return databaseReference.child(uid).child("matrix").removeValue();
    }

    public Task<Void> deleteSolution(String uid) {
        return databaseReference.child(uid).child("solution").removeValue();
    }

    public Task<DataSnapshot> filterByUid(String uid) {
        return databaseReference.child(uid).limitToFirst(1).get();
    }

}
