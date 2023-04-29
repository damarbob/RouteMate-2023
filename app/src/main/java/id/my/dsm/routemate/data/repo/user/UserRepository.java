package id.my.dsm.routemate.data.repo.user;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Task;
import com.google.common.collect.Iterables;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import id.my.dsm.routemate.data.accessobject.data.DataDAO;
import id.my.dsm.routemate.data.accessobject.user.DSMUserDAO;
import id.my.dsm.routemate.data.event.model.OnRetrieveDSMUser;
import id.my.dsm.routemate.data.event.model.OnRetrieveUserData;
import id.my.dsm.routemate.data.event.model.OnUpdateDSMUser;
import id.my.dsm.routemate.data.event.model.OnUpdateUserData;
import id.my.dsm.routemate.data.event.model.OnUserStatusChangedEvent;
import id.my.dsm.routemate.data.model.user.DSMUser;
import id.my.dsm.routemate.data.model.userdata.UserData;
import id.my.dsm.routemate.data.source.user.ConnectionStateDataSource;
import id.my.dsm.routemate.data.source.user.UserDataSource;

/**
 * Handles all transactions with {@link UserDataSource}
 */
@Singleton
public class UserRepository {

    private static final String TAG = UserRepository.class.getSimpleName();

    // Database keys
    private static final String PLACES_DB_KEY = "places";
    private static final String VEHICLES_DB_KEY = "vehicles";
    private static final String MATRIX_DB_KEY = "matrix";
    private static final String SOLUTIONS_DB_KEY = "solutions";
    private static final String MAPBOX_DIRECTION_ROUTES_DB_KEY = "mapboxDirectionsRoutes";
    private static final String OPTIMIZATION_METHOD_DB_KEY = "optimizationMethod";
    private static final String USING_ADVANCED_ALGORITHM_DB_KEY = "usingAdvancedAlgorithm";

    private final UserDataSource userDataSource;
    private final ConnectionStateDataSource connectionStateDataSource;
    private final DSMUserDAO dsmUserDAO;
    private final DataDAO dataDAO;

    // Records/Live records
    private DSMUser dsmUser;
    private final MutableLiveData<FirebaseUser> user = new MutableLiveData<>(null);

    // States
    private final MutableLiveData<UserStatus> userStatusMutableLiveData = new MutableLiveData<>(UserStatus.UNAUTHORIZED);

    @Inject
    public UserRepository(UserDataSource userDataSource, ConnectionStateDataSource connectionStateDataSource, DSMUserDAO dsmUserDAO, DataDAO dataDAO) {
        this.userDataSource = userDataSource;
        this.connectionStateDataSource = connectionStateDataSource;
        this.dsmUserDAO = dsmUserDAO;
        this.dataDAO = dataDAO;

        validateUser(this);
    }

    /**
     * Listen to connection state changes. Invoke {@link UserStatus} update whenever connection state changes.
     */
    public void listenConnectionState() {
        connectionStateDataSource.listenConnectionState(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        boolean connected = Boolean.TRUE.equals(snapshot.getValue(Boolean.class));
                        UserStatus userStatus = connected ? UserStatus.ONLINE : UserStatus.OFFLINE;

                        EventBus.getDefault().post(
                                new OnUserStatusChangedEvent.Builder(userStatus).build()
                        );

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.w(TAG, "listenConnectionState: Listener was cancelled");
                    }
                }
        );
    }

    public void updateDSMUser(String uid, DSMUser dsmUser) {
        dsmUserDAO.filterByUid(uid).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                Iterable<DataSnapshot> dataSnapshots = task.getResult().getChildren();

                // If userData not found
                if (Iterables.size(dataSnapshots) == 0) {
                    Log.w(TAG, "updateDSMUser: filterByUid: No data found. ");
                }

                // Iterable must be iterated
                for (DataSnapshot data : task.getResult().getChildren()) {

                    // Deserialize before doing anything
                    DSMUser dsmUser1 = data.getValue(DSMUser.class);

                    if (dsmUser1 == null) {
                        Log.e(TAG, "updateDSMUser: Unable to deserialize DSMUser");
                        return;
                    }

                    /*
                    Double checking the uid. May remove this checking if the uid can be updated.
                    For now, Firebase Auth seems to deny uid change.
                     */
                    if (dsmUser1.getUid().equals(uid)) {

                        Log.d(TAG, "updateDSMUser: Found DSMUser (" + dsmUser1.getUid() + ")");

                        HashMap<String, Object> hashMap = new HashMap<>();

                        if (dsmUser.getEmail() != null)
                            hashMap.put("email", dsmUser.getEmail());
                        if (dsmUser.getFullName() != null)
                            hashMap.put("fullName", dsmUser.getFullName());
                        if (dsmUser.getPlan() != null)
                            hashMap.put("plan", dsmUser.getPlan());

                        // Update user data (must use data.getRef(). Otherwise, bug.
                        dsmUserDAO.update(data.getRef(), hashMap).addOnCompleteListener(task1 -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "updateDSMUser: DSMUser data synced successfully");

                                EventBus.getDefault().post(new OnUpdateDSMUser(
                                        OnUpdateDSMUser.Status.SUCCESS,
                                        dsmUser,
                                        "DSMUser data synced successfully"
                                ));
                            }
                            else {
                                Log.d(TAG, "updateDSMUser: Update failed");

                                EventBus.getDefault().post(new OnUpdateDSMUser(
                                        OnUpdateDSMUser.Status.FAILED,
                                        "Update failed"
                                ));

                            }
                        });
                    }
                }

            }
            else {
                Log.w(TAG, "updateDSMUser: Error getting DSMUser: ", task.getException());


                EventBus.getDefault().post(new OnUpdateDSMUser(
                        OnUpdateDSMUser.Status.FAILED,
                        "Error getting DSMUser: " + Objects.requireNonNull(task.getException()).getMessage()
                ));
            }
        });
    }

    /**
     * Update user data on the server. Will bypass null fields. Use as necessary.
     * @param userData {@link UserData} instance
     */
    public void updateUserData(UserData userData) {

        if (
                userData.getPlaces() == null &&
                        userData.getVehicles() == null &&
                        userData.getMatrix() == null &&
                        userData.getSolutions() == null &&
                        userData.getMapboxDirectionsRoutes() == null
        ) {
            Log.e(TAG, "updateUserData: UserData contains no data to be inserted into database");

            EventBus.getDefault().post(new OnUpdateUserData(
                    OnUpdateUserData.Status.FAILED,
                    "updateUserData: UserData contains no data to be inserted into database"
            ));
            return;
        }

        dataDAO.filterByUid(userData.getUid()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "updateUserData: filterByUid: Request successful");

                Iterable<DataSnapshot> dataSnapshots = task.getResult().getChildren();

                // If userData not found
                if (Iterables.size(dataSnapshots) == 0) {
                    Log.w(TAG, "updateUserData: filterByUid: No data found. Inserting ...");
                    dataDAO.insert(userData).addOnCompleteListener(task1 -> {
                        if (task1.isSuccessful()) {
                            Log.d(TAG, "updateUserData: filterByUid: insert: Inserted successfully");

                            EventBus.getDefault().post(new OnUpdateUserData(
                                    OnUpdateUserData.Status.SUCCESS,
                                    userData,
                                    "User data synced successfully"
                            ));
                        }
                        else {
                            Log.e(TAG, "updateUserData: filterByUid: insert: Failed to insert");

                            EventBus.getDefault().post(new OnUpdateUserData(
                                    OnUpdateUserData.Status.FAILED,
                                    "updateUserData: filterByUid: insert: Failed to insert"
                            ));
                        }
                    });

                    return;
                }

                for (DataSnapshot data : task.getResult().getChildren()) {

                    // Deserialization
                    UserData userData1 = data.getValue(UserData.class);

                    if (userData1 == null) {
                        Log.w(TAG, "updateUserData: filterByUid: Class is empty!");

                        EventBus.getDefault().post(new OnUpdateUserData(
                                OnUpdateUserData.Status.FAILED,
                                "updateUserData: filterByUid: Class is empty!"
                        ));
                        return;
                    }

                    if (userData1.getName().equals(DSMUser.MY_ROUTE + userData.getUid())) {
                        Log.d(TAG, "Found MyRoute-" + userData.getUid());

                        HashMap<String, Object> newDataHashMap = new HashMap<>();

                        if (userData.getPlaces() != null)
                            newDataHashMap.put(PLACES_DB_KEY, userData.getPlaces());
                        if (userData.getVehicles() != null)
                            newDataHashMap.put(VEHICLES_DB_KEY, userData.getVehicles());
                        if (userData.getMatrix() != null)
                            newDataHashMap.put(MATRIX_DB_KEY, userData.getMatrix());
                        if (userData.getSolutions() != null)
                            newDataHashMap.put(SOLUTIONS_DB_KEY, userData.getSolutions());
                        if (userData.getMapboxDirectionsRoutes() != null)
                            newDataHashMap.put(MAPBOX_DIRECTION_ROUTES_DB_KEY, userData.getMapboxDirectionsRoutes());
                        if (userData.getOptimizationMethod() != null)
                            newDataHashMap.put(OPTIMIZATION_METHOD_DB_KEY, userData.getOptimizationMethod());
                        if (userData.getUsingAdvancedAlgorithm() != null)
                            newDataHashMap.put(USING_ADVANCED_ALGORITHM_DB_KEY, userData.getUsingAdvancedAlgorithm());

                        // Update user data (must use data.getRef(). Otherwise, bug.
                        dataDAO.update(data.getRef(), newDataHashMap).addOnCompleteListener(task1 -> {
                            if (task.isSuccessful()) {
                                Log.d(TAG, "User data synced successfully");

                                EventBus.getDefault().post(new OnUpdateUserData(
                                        OnUpdateUserData.Status.SUCCESS,
                                        userData,
                                        "User data synced successfully"
                                ));
                            }
                            else {
                                Log.e(TAG, "Update failed");

                                EventBus.getDefault().post(new OnUpdateUserData(
                                        OnUpdateUserData.Status.FAILED,
                                        "Update failed"
                                ));

                            }
                        });

                    }
                }
            } else {
                Log.w(TAG, "Error getting UserData: ", task.getException());

                EventBus.getDefault().post(new OnUpdateUserData(
                        OnUpdateUserData.Status.FAILED,
                        "Error getting UserData: " + Objects.requireNonNull(task.getException()).getMessage()
                ));
            }
        });

    }

    public void retrieveDSMUser(String uid) {

        // Filter data by uid
        dsmUserDAO.filterByUid(uid).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                Log.d(TAG, "retrieveDSMUser: Request successful");

                Iterable<DataSnapshot> dataSnapshots = task.getResult().getChildren();

                // If dsmUser not found
                if (Iterables.size(dataSnapshots) == 0) {
                    Log.w(TAG, "retrieveDSMUser: DSMUser not found");
                    EventBus.getDefault().post(new OnRetrieveDSMUser(OnRetrieveDSMUser.Status.FAILED));
                    return;
                }

                Log.d(TAG, "retrieveDSMUser: Importing DSMUser ...");

                DSMUser dsmUser = null;

                for (DataSnapshot data : task.getResult().getChildren()) {
                    dsmUser = data.getValue(DSMUser.class);
//                    Log.d(TAG, "" + dsmUser.getSolution().size());
                }

                // If somehow data has no contents
                if (dsmUser == null) {
                    Log.e(TAG, "retrieveDSMUser: DSMUser has no contents or is null");
                    return;
                }

                // Prevent developer's fault
                if (!dsmUser.getUid().equals(uid)) {
                    Log.e(TAG, "retrieveDSMUser: Retrieved DSMUser ID doesn't match with the user ID");
                    return;
                }

                // Load data
                EventBus.getDefault().post(new OnRetrieveDSMUser(OnRetrieveDSMUser.Status.SUCCESS, dsmUser));

            }
            else {
                Log.e(TAG, "retrieveDSMUser: Failed to retrieve DSMUser (" + uid + ")");
                EventBus.getDefault().post(new OnRetrieveDSMUser(OnRetrieveDSMUser.Status.FAILED));
            }
        });
    }

    /**
     * Load user data from server by uid. On complete will trigger {@link OnRetrieveUserData.Status}.
     * @param uid uid string
     */
    public void retrieveUserData(String uid) {

        // Filter data by uid
        dataDAO.filterByUid(uid).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                Log.d(TAG, "retrieveUserData: Request successful");

                Iterable<DataSnapshot> dataSnapshots = task.getResult().getChildren();

                // If userData not found
                if (Iterables.size(dataSnapshots) == 0) {
                    Log.w(TAG, "retrieveUserData: No synced data found in server");
                    EventBus.getDefault().post(new OnRetrieveUserData(OnRetrieveUserData.Status.FAILED));
                    return;
                }

                Log.d(TAG, "retrieveUserData: Importing UserData ...");

                UserData userData = null;

                for (DataSnapshot data : task.getResult().getChildren()) {
                    userData = data.getValue(UserData.class);
                }

                // If somehow data has no contents
                if (userData == null) {
                    Log.e(TAG, "retrieveUserData: UserData has no contents or is null");
                    return;
                }

                // Prevent developer's fault
                if (!userData.getName().equals(DSMUser.MY_ROUTE + uid)) {
                    Log.e(TAG, "retrieveUserData: Retrieved UserData ID doesn't match with the user ID");
                    return;
                }

                // Load data
                EventBus.getDefault().post(new OnRetrieveUserData(OnRetrieveUserData.Status.SUCCESS, userData));

            }
            else {
                Log.e(TAG, "retrieveUserData: Failed to retrieve " + DSMUser.MY_ROUTE + uid);
                EventBus.getDefault().post(new OnRetrieveUserData(OnRetrieveUserData.Status.FAILED));
            }
        });
    }

    private <T> void postUserStatusChangedEventByTask(@NonNull Task<T> task, @NonNull UserStatus userStatus, @Nullable Exception exception) {

        // Whether the authorization successful or fail
        if (task.isSuccessful()) {
            // Include SUCCESS statuses
            EventBus.getDefault().post(new OnUserStatusChangedEvent.Builder(userStatus).build());
        }
        else {
            // Include FAILURE statuses
            OnUserStatusChangedEvent onUserStatusChangedEvent = new OnUserStatusChangedEvent.Builder(userStatus)
                    .build();

            if (exception != null)
                onUserStatusChangedEvent = new OnUserStatusChangedEvent.Builder(userStatus)
                        .withException(exception)
                        .build();

            EventBus.getDefault().post(onUserStatusChangedEvent);
        }

    }

    /**
     * Validates current {@link FirebaseUser} and update userStatus.
     */
    public void validateUser(Object invoker) {

        user.setValue(getUser()); // Update user LiveData

        // Create a new OnUserStatusChangedEvent
        OnUserStatusChangedEvent onUserStatusChangedEvent;

        // Check current FirebaseUser
        if (getUser() == null) {
            setUserStatus(UserStatus.UNAUTHORIZED);

            onUserStatusChangedEvent = new OnUserStatusChangedEvent.Builder(getUserStatus())
                    .withException(new Exception("User unauthorized"))
                    .build();
        }
        else {
            // TODO: Re-authenticate user
            setUserStatus(UserStatus.SIGN_IN_SUCCESS);
            onUserStatusChangedEvent = new OnUserStatusChangedEvent.Builder(getUserStatus())
                    .build();
        }

        // Post new OnUserStatusChangedEvent
        EventBus.getDefault().post(onUserStatusChangedEvent);

        Log.d(TAG, "validateUser() invoked by " + invoker.getClass().getSimpleName() + ": " + onUserStatusChangedEvent.getUserStatus());

    }

    /**
     * Sign new user up to system.
     * @param email email string
     * @param password password string
     */
    public void signUp(String email, String password) {

        // Sign user up into FirebaseAuth
        userDataSource.signUp(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                // From here, getUser() is accessible
                user.setValue(getUser());

                // Upon signup success
                setUserStatus(UserStatus.SIGNUP_SUCCESS);

                Log.e(TAG, "Signed up successfully as " + Objects.requireNonNull(user.getValue()).getDisplayName());

            }
            else {

                // Upon signup failure
                setUserStatus(UserStatus.SIGNUP_FAILURE);

//                Log.e(TAG, "Sign up failed: " + task.getException().getMessage());

            }

            // Post user repository event
            postUserStatusChangedEventByTask(task, getUserStatus(), task.getException());

        });

    }

    /**
     * Sign user in to system.
     * @param email email string
     * @param password password string
     */
    public void signIn(String email, String password) {

        // Sign user into FirebaseAuth
        userDataSource.signIn(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                // From here, getUser() is accessible
                user.setValue(getUser());

                // Upon sign in success
                setUserStatus(UserStatus.SIGN_IN_SUCCESS);

            }
            else {

                // Upon sign in failure
                setUserStatus(UserStatus.SIGN_IN_FAILURE);

            }

            // Post user repository event
            postUserStatusChangedEventByTask(task, getUserStatus(), task.getException());

        });

    }

    /**
     * Updates user's Display Name.
     * @param user {@link FirebaseUser} instance
     * @param dsmUser {@link DSMUser} instance
     */
    public void updateDisplayName(FirebaseUser user, DSMUser dsmUser) {

        // Update the Display Name after signed up successfully
        userDataSource.updateDisplayName(user, dsmUser.getFullName()).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                // Upon update mapboxProfile success
                setUserStatus(UserStatus.UPDATE_USER_SUCCESS);

            }
            else {

                // Upon update mapboxProfile failure
                setUserStatus(UserStatus.UPDATE_USER_FAILURE);

            }

            // Post user repository event
            postUserStatusChangedEventByTask(task, getUserStatus(), task.getException());

        });

    }

    /**
     * Insert new user data record to system.
     * @param user {@link FirebaseUser} instance
     * @param newDSMUser {@link DSMUser} instance
     */
    public void insert(FirebaseUser user, DSMUser newDSMUser) {
        // Insert into DSMUser node in different database
        dsmUserDAO.insert(newDSMUser, user.getUid()).addOnCompleteListener(task -> {

            if (task.isSuccessful()) {

                // Upon insert user record success
                setUserStatus(UserStatus.INSERT_RECORD_SUCCESS);

            }
            else {

                // Upon insert user record failure
                setUserStatus(UserStatus.INSERT_RECORD_FAILURE);
            }

            // Post user repository event
            postUserStatusChangedEventByTask(task, getUserStatus(), task.getException());

        });

    }

    //

    public void setDsmUser(@NonNull DSMUser dsmUser) {
        this.dsmUser = dsmUser;
    }

    @Nullable
    public DSMUser getDsmUser() {
        return dsmUser;
    }

    /**
     * Get current {@link FirebaseUser}.
     * @return {@link FirebaseUser} instance
     */
    public FirebaseUser getUser() {
        return userDataSource.getUser();
    }

    /**
     * Get current {@link UserStatus}.
     * @return {@link UserStatus} enum value
     */
    private UserStatus getUserStatus() {
        return userStatusMutableLiveData.getValue();
    }

    /**
     * Set new {@link UserStatus}.
     */
    private void setUserStatus(UserStatus status) {
        userStatusMutableLiveData.setValue(status);
    }

    // Exposer

    /**
     * Get current {@link FirebaseUser} as observable.
     * @return {@link FirebaseUser} {@link LiveData} instance
     */
    public LiveData<FirebaseUser> getLiveUser() {
        return user;
    }

    /**
     * Get current {@link UserStatus} as observable.
     * @return {@link UserStatus} {@link LiveData} instance
     */
    public LiveData<UserStatus> getLiveUserStatus() {
        return userStatusMutableLiveData;
    }

}
