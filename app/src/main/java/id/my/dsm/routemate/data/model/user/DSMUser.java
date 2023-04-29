package id.my.dsm.routemate.data.model.user;

import com.google.firebase.database.Exclude;

/**
 * General instance for DSMApp users
 */
public class DSMUser {

    public static final String MY_ROUTE = "MyRoute-";
    private static final String SESSION_OPTIMIZATION = "Optimization-";
    private String uid;
    private String fullName;
    private String email;
    private DSMPlan plan;

    public DSMUser() {
        // Default constructor required for calls to DataSnapshot.getValue(DSMUser.class)
    }

    public DSMUser(String uid, String fullName, String email) {
        this.uid = uid;
        this.fullName = fullName;
        this.email = email;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public DSMPlan getPlan() {
        return plan;
    }

    public void setPlan(DSMPlan plan) {
        this.plan = plan;
    }

    @Exclude
    public int getOptimizationQuota() {
        switch (plan) {
            case FREE:
                return 10;
            case PRO:
                return 20;
            case EXPERT:
                return 30;
            case OWNER:
                return 99;
            default:
                throw new IllegalStateException("Unexpected value: " + plan);
        }
    }

    @Exclude
    public static String getOptimizationSession(String uid) {
        return SESSION_OPTIMIZATION + uid;
    }

}
