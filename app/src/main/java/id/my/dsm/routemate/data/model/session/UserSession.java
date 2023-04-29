package id.my.dsm.routemate.data.model.session;

import com.google.firebase.database.Exclude;
import com.mapbox.mapboxsdk.geometry.LatLng;

import id.my.dsm.routemate.data.model.DSMTimestamp;

public class UserSession {

    private String id;
    private String uid;
    private LatLng lastKnownLocation;
    private LatLng lastViewedLocation;
    private DSMTimestamp lastLoginActivity;
    private DSMTimestamp lastOptimizationActivity;
    private String lastPurchasedItemId;
    private DSMTimestamp lastPurchaseActivity;
    private DSMTimestamp lastOptimizationQuotaRefresh;
    private Integer lastRemainingOptimizationQuota;
    private String message;

    // For deserialization
    public UserSession() {
    }

    public UserSession(Builder builder) {
        this.id = builder.id;
        this.uid = builder.uid;
        this.lastKnownLocation = builder.lastKnownLocation;
        this.lastViewedLocation = builder.lastViewedLocation;
        this.lastLoginActivity = builder.lastLoginActivity;
        this.lastOptimizationActivity = builder.lastOptimizationActivity;
        this.lastPurchasedItemId = builder.lastPurchasedItemId;
        this.lastPurchaseActivity = builder.lastPurchaseActivity;
        this.lastOptimizationQuotaRefresh = builder.lastOptimizationQuotaRefresh;
        this.lastRemainingOptimizationQuota = builder.lastRemainingOptimizationQuota;
        this.message = builder.message;
    }

    public String getId() {
        return id;
    }

    public String getUid() {
        return uid;
    }

    public LatLng getLastKnownLocation() {
        return lastKnownLocation;
    }

    public LatLng getLastViewedLocation() {
        return lastViewedLocation;
    }

    public DSMTimestamp getLastLoginActivity() {
        return lastLoginActivity;
    }

    public DSMTimestamp getLastOptimizationActivity() {
        return lastOptimizationActivity;
    }

    public String getLastPurchasedItemId() {
        return lastPurchasedItemId;
    }

    public DSMTimestamp getLastPurchaseActivity() {
        return lastPurchaseActivity;
    }

    public DSMTimestamp getLastOptimizationQuotaRefresh() {
        return lastOptimizationQuotaRefresh;
    }

    public Integer getLastRemainingOptimizationQuota() {
        return lastRemainingOptimizationQuota;
    }

    public String getMessage() {
        return message;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public void setLastKnownLocation(LatLng lastKnownLocation) {
        this.lastKnownLocation = lastKnownLocation;
    }

    public void setLastViewedLocation(LatLng lastViewedLocation) {
        this.lastViewedLocation = lastViewedLocation;
    }

    public void setLastLoginActivity(DSMTimestamp lastLoginActivity) {
        this.lastLoginActivity = lastLoginActivity;
    }

    public void setLastOptimizationActivity(DSMTimestamp lastOptimizationActivity) {
        this.lastOptimizationActivity = lastOptimizationActivity;
    }

    public void setLastPurchasedItemId(String lastPurchasedItemId) {
        this.lastPurchasedItemId = lastPurchasedItemId;
    }

    public void setLastPurchaseActivity(DSMTimestamp lastPurchaseActivity) {
        this.lastPurchaseActivity = lastPurchaseActivity;
    }

    public void setLastOptimizationQuotaRefresh(DSMTimestamp lastOptimizationQuotaRefresh) {
        this.lastOptimizationQuotaRefresh = lastOptimizationQuotaRefresh;
    }

    public void setLastRemainingOptimizationQuota(Integer lastRemainingOptimizationQuota) {
        this.lastRemainingOptimizationQuota = lastRemainingOptimizationQuota;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UserSession copy() {
        return new UserSession.Builder(this.getId(), this.getUid())
                .withMessage(this.message)
                .withLastViewedLocation(this.lastViewedLocation)
                .withLastRemainingOptimizationQuota(this.lastRemainingOptimizationQuota)
                .withLastPurchasedItemId(this.lastPurchasedItemId)
                .withLastPurchaseActivity(this.lastPurchaseActivity)
                .withLastOptimizationActivity(this.lastOptimizationActivity)
                .withLastLoginActivity(this.lastLoginActivity)
                .withLastKnownLocation(this.lastKnownLocation)
                .withLastOptimizationQuotaRefresh(this.lastOptimizationQuotaRefresh)
                .build();
    }

    public static class Builder {

        private final String id;
        private final String uid;
        private LatLng lastKnownLocation;
        private LatLng lastViewedLocation;
        private DSMTimestamp lastLoginActivity;
        private DSMTimestamp lastOptimizationActivity;
        private String lastPurchasedItemId;
        private DSMTimestamp lastPurchaseActivity;
        private DSMTimestamp lastOptimizationQuotaRefresh;
        private Integer lastRemainingOptimizationQuota;
        private String message;

        public Builder(String id, String uid) {
            this.id = id;
            this.uid = uid;
        }

        @Exclude
        public Builder withLastKnownLocation(LatLng lastKnownLocation) {
            this.lastKnownLocation = lastKnownLocation;
            return this;
        }

        @Exclude
        public Builder withLastViewedLocation(LatLng lastViewedLocation) {
            this.lastViewedLocation = lastViewedLocation;
            return this;
        }

        @Exclude
        public Builder withLastLoginActivity(DSMTimestamp lastLoginActivity) {
            this.lastLoginActivity = lastLoginActivity;
            return this;
        }

        @Exclude
        public Builder withLastOptimizationActivity(DSMTimestamp lastOptimizationActivity) {
            this.lastOptimizationActivity = lastOptimizationActivity;
            return this;
        }

        @Exclude
        public Builder withLastPurchasedItemId(String lastPurchasedItemId) {
            this.lastPurchasedItemId = lastPurchasedItemId;
            return this;
        }

        @Exclude
        public Builder withLastPurchaseActivity(DSMTimestamp lastPurchaseActivity) {
            this.lastPurchaseActivity = lastPurchaseActivity;
            return this;
        }

        @Exclude
        public Builder withLastOptimizationQuotaRefresh(DSMTimestamp lastOptimizationQuotaRefresh) {
            this.lastOptimizationQuotaRefresh = lastOptimizationQuotaRefresh;
            return this;
        }

        @Exclude
        public Builder withLastRemainingOptimizationQuota(Integer lastRemainingOptimizationQuota) {
            this.lastRemainingOptimizationQuota = lastRemainingOptimizationQuota;
            return this;
        }

        @Exclude
        public Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        @Exclude
        public UserSession build() {
            return new UserSession(this);
        }

    }

}
