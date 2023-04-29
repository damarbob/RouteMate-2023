package id.my.dsm.routemate.data.model;

import com.google.firebase.database.Exclude;

import java.sql.Timestamp;
import java.util.Date;

public class DSMTimestamp {

    private String dsmTimestamp;

    public DSMTimestamp() {
    }

    public DSMTimestamp(Date date) {
        this.dsmTimestamp = new Timestamp(date.getTime()).toString();
    }

    public String getDsmTimestamp() {
        return dsmTimestamp;
    }

    public void setDsmTimestamp(String dsmTimestamp) {
        this.dsmTimestamp = dsmTimestamp;
    }

    @Exclude
    public Date getDate() {
        return new Date(getTime());
    }

    @Exclude
    public Timestamp getTimestamp() {
        return Timestamp.valueOf(dsmTimestamp);
    }

    @Exclude
    public Long getTime() {
        return getTimestamp().getTime();
    }

}
