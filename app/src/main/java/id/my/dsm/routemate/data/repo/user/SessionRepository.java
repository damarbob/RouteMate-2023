package id.my.dsm.routemate.data.repo.user;

import android.util.Log;

import androidx.annotation.Nullable;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.firebase.database.DataSnapshot;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;

import id.my.dsm.routemate.data.accessobject.session.UserSessionDAO;
import id.my.dsm.routemate.data.event.model.OnUpdateUserSession;
import id.my.dsm.routemate.data.model.session.UserSession;

@Singleton
public class SessionRepository {

    private static final String TAG = SessionRepository.class.getSimpleName();

    private final UserSessionDAO dao;

    // Records
    private final List<UserSession> userSessions = new ArrayList<>();

    @Inject
    public SessionRepository(UserSessionDAO dao) {
        this.dao = dao;
    }

    /**
     * Update remote UserSession. The session id must match with one in the remote repository.
     * @param userSession updated UserSession instance
     * @param actionUponSuccess enum OnUpdateUserSession.Action
     */
    public void update(UserSession userSession, OnUpdateUserSession.Action actionUponSuccess) {

        dao.filterByUid(userSession.getUid()).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "updateSession: filterByUid: Request successful for UID " + userSession.getUid(), task.getException());

                Iterable<DataSnapshot> dataSnapshots = task.getResult().getChildren();

                // If session not found (it may means the user is new or the data had gone)
                // insert a new UserSession data
                if (Iterables.size(dataSnapshots) == 0) {
                    Log.w(TAG, "updateSession: filterByUid: No session UID found. Inserting new session with ID " + userSession.getId());

                    insert(userSession, OnUpdateUserSession.Action.NONE);

                    return;
                }

                // Each user might has more than one session. Iterate and get the matching id with the submitted UserSession's id
                for (DataSnapshot data : task.getResult().getChildren()) {

                    UserSession userSession1 = data.getValue(UserSession.class); // Deserialize
                    assert userSession1 != null; // Error if UserSession deserialization is failed

                    if (userSession1.getId().equals(userSession.getId())) {

                        Log.d(TAG, "updateSession: filterByUid: filterById: Found session for ID " + userSession.getId() + " in UID " + userSession.getUid());

                        // Only update data that have a value and skip null data
                        HashMap<String, Object> newDataHashMap = new HashMap<>();
                        if (userSession.getLastKnownLocation() != null)
                            newDataHashMap.put("lastKnownLocation", userSession.getLastKnownLocation());
                        if (userSession.getLastViewedLocation() != null)
                            newDataHashMap.put("lastViewedLocation", userSession.getLastViewedLocation());
                        if (userSession.getLastLoginActivity() != null)
                            newDataHashMap.put("lastLoginActivity", userSession.getLastLoginActivity());
                        if (userSession.getLastOptimizationActivity() != null)
                            newDataHashMap.put("lastOptimizationActivity", userSession.getLastOptimizationActivity());
                        if (userSession.getLastPurchasedItemId() != null)
                            newDataHashMap.put("lastPurchasedItemId", userSession.getLastPurchasedItemId());
                        if (userSession.getLastPurchaseActivity() != null)
                            newDataHashMap.put("lastPurchaseActivity", userSession.getLastPurchaseActivity());
                        if (userSession.getLastOptimizationQuotaRefresh() != null)
                            newDataHashMap.put("lastOptimizationQuotaRefresh", userSession.getLastOptimizationQuotaRefresh());
                        if (userSession.getLastRemainingOptimizationQuota() != null)
                            newDataHashMap.put("lastRemainingOptimizationQuota", userSession.getLastRemainingOptimizationQuota());

                        // Apply changes to remote data
                        dao.update(data.getRef(), newDataHashMap).addOnCompleteListener(task1 -> {
                            if (task1.isSuccessful()) {
                                Log.d(TAG, "User session synced successfully");

                                EventBus.getDefault().post(
                                        new OnUpdateUserSession(OnUpdateUserSession.Status.SUCCESS, OnUpdateUserSession.Event.UPDATE, userSession, actionUponSuccess)
                                );
                            }
                            else {
                                Log.e(TAG, "updateSession: Update failed");

                                EventBus.getDefault().post(
                                        new OnUpdateUserSession(OnUpdateUserSession.Status.FAILED, OnUpdateUserSession.Event.UPDATE, "Error updating userSession: " + Objects.requireNonNull(task1.getException()).getLocalizedMessage())
                                );
                            }
                        });

                        updateRecord(userSession); // Update the existing session in the records to match the requested session

                    }

                }

            }
            else {
                Log.w(TAG, "updateSession: update: ", task.getException());

                EventBus.getDefault().post(
                        new OnUpdateUserSession(OnUpdateUserSession.Status.FAILED, OnUpdateUserSession.Event.UPDATE, "Error updating userSession: " + Objects.requireNonNull(task.getException()).getLocalizedMessage())
                );

            }
        });

    }

    /**
     * Update local UserSession. The session id must match with the one found in the local repository.
     * @param userSession UserSession instance
     */
    private void updateRecord(UserSession userSession) {

        UserSession record = get(userSession.getId());
        assert record != null;

        record.setLastRemainingOptimizationQuota(userSession.getLastRemainingOptimizationQuota());
        record.setLastPurchaseActivity(userSession.getLastPurchaseActivity());
        record.setLastPurchasedItemId(userSession.getLastPurchasedItemId());
        record.setLastOptimizationActivity(userSession.getLastOptimizationActivity());
        record.setLastLoginActivity(userSession.getLastLoginActivity());
        record.setLastViewedLocation(userSession.getLastViewedLocation());
        record.setLastKnownLocation(userSession.getLastKnownLocation());

    }

    /**
     * Update multiple UserSession with matching id from the given data. Overwrites the data from local repository using the given data.
     * @param userSessions List of UserSession
     */
    private void updateRecords(List<UserSession> userSessions) {
        for (UserSession us : userSessions)
            updateRecord(us);
    }

    /**
     * Insert UserSession record to remote repository
     * @param userSession new UserSession instance
     * @param actionUponSuccess enum OnUpdateUserSession.Action
     */
    public void insert(UserSession userSession, OnUpdateUserSession.Action actionUponSuccess) {
        dao.insert(userSession).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "insert: Inserted successfully");

                EventBus.getDefault().post(
                        new OnUpdateUserSession(OnUpdateUserSession.Status.SUCCESS, OnUpdateUserSession.Event.INSERT, userSession, actionUponSuccess)
                );
            }
            else {
                Log.e(TAG, "insert: Insert failed: " + task.getException());

                EventBus.getDefault().post(
                        new OnUpdateUserSession(OnUpdateUserSession.Status.FAILED, OnUpdateUserSession.Event.INSERT, "Error inserting userSession: " + Objects.requireNonNull(task.getException()).getLocalizedMessage())
                );
            }
        });
    }

    /**
     * Retrieve UserSession from remote repository and store to local repository using the given uid.
     * @param uid String UID
     * @param actionUponSuccess enum OnUpdateUserSession.Action
     */
    public void retrieve(String uid, OnUpdateUserSession.Action actionUponSuccess) {

        List<UserSession> retrievedUserSessions = new ArrayList<>();

        dao.filterByUid(uid).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "retrieve: filterByUid: Request successful");

                Iterable<DataSnapshot> dataSnapshots = task.getResult().getChildren();

                // If session not found
                if (Iterables.size(dataSnapshots) == 0) {
                    Log.w(TAG, "retrieve: filterByUid: No session with specified UID found.");
                    EventBus.getDefault().post(
                            new OnUpdateUserSession(
                                    OnUpdateUserSession.Status.UID_NOT_FOUND,
                                    OnUpdateUserSession.Event.RETRIEVE,
                                    "No session with specified UID found"
                            )
                    );
                    return;
                }

                // Deserialize all data retrieved
                for (DataSnapshot data : task.getResult().getChildren()) {

                    UserSession userSession = data.getValue(UserSession.class);

                    if (userSession == null)
                        continue;

                    retrievedUserSessions.add(userSession); // Populate successfully deserialized UserSessions into List

                    Log.d(TAG, "retrieve: filterByUid: Session with ID " + userSession.getId() + " added to repository");

                }

                // Last step. Update the repository if sessions already exist, otherwise add all into the repository
                if (getUserSessions().size() > 0)
                    updateRecords(retrievedUserSessions);
                else
                    userSessions.addAll(retrievedUserSessions);

                // Invoke event for each UserSession retrieved
                for (UserSession us : userSessions)
                    EventBus.getDefault().post(
                            new OnUpdateUserSession(
                                    OnUpdateUserSession.Status.SUCCESS,
                                    OnUpdateUserSession.Event.RETRIEVE,
                                    us,
                                    actionUponSuccess
                            )
                    );

            }
            else {
                Log.e(TAG, "retrieve: filterByUid: Request failed: " + task.getException());

                EventBus.getDefault().post(
                        new OnUpdateUserSession(OnUpdateUserSession.Status.FAILED, OnUpdateUserSession.Event.RETRIEVE, "Error retrieving userSession: " + Objects.requireNonNull(task.getException()).getLocalizedMessage())
                );
            }
        });
    }

    /**
     * Get all UserSession instances from the local repository.
     * @return ImmutableList of UserSession.
     */
    public ImmutableList<UserSession> getUserSessions() {
        return ImmutableList.copyOf(userSessions);
    }

    /**
     * Get only UserSession with the matching id.
     * @param id String of session id.
     * @return UserSession instances with matching id.
     */
    public ImmutableList<UserSession> filterById(String id) {

        List<UserSession> userSessions1 = new ArrayList<>();

        for (UserSession us : getUserSessions()) {
            if (us.getId().equals(id))
                userSessions1.add(us);
        }

        return ImmutableList.copyOf(userSessions1);

    }

    /**
     * Get a UserSession from local repository.
     * @param id String of session id
     * @return UserSession with matching id. Null if not found.
     */
    @Nullable
    public UserSession get(String id) {

        List<UserSession> filteredUserSessions = filterById(id);

        if (filteredUserSessions.size() == 0) {
            Log.e(TAG, "get: Not found");
            return null;
        }
        else if (filteredUserSessions.size() > 1) {
            Log.e(TAG, "get: Error getting userSession. Found duplicate ID");
            return null;
        }

        return filteredUserSessions.get(0);

    }

}
