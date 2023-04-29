package id.my.dsm.routemate.data.source.user;

import androidx.annotation.NonNull;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import javax.inject.Inject;

/**
 * Data source for connection state (connected/disconnected)
 */
public class ConnectionStateDataSource {

    private static final String TAG = ConnectionStateDataSource.class.getName();

    DatabaseReference connectedRef = FirebaseDatabase.getInstance("https://dsmsolver-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference(".info/connected");

    @Inject
    public ConnectionStateDataSource() {
    }

    public void listenConnectionState(@NonNull ValueEventListener valueEventListener) {
        connectedRef.addValueEventListener(valueEventListener);
    }
}
