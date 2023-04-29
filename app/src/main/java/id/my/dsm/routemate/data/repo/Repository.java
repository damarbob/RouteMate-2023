package id.my.dsm.routemate.data.repo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository base template.
 * @param <T> repository type
 */
public abstract class Repository<T> {

    private static final String TAG = Repository.class.getSimpleName();
    private final ArrayList<T> records = new ArrayList<>();
    private final MutableLiveData<Integer> recordsCount = new MutableLiveData<>(0);
    private int lastRecordIndex = 0;

    public Repository() {
    }

    /**
     * Start repository, default implementation is registering to {@link EventBus}.
     */
    abstract public void onStart();

    /**
     * End repository, default implementation is unregistering the {@link EventBus}
     */
    abstract public void onDestroy();

    /**
     * Obtain the records from repository in form of {@link ArrayList}
     * @return {@link List} of {@link T}
     */
    public List<T> getRecords() {
        return records;
    }

    /**
     * Add a record to repository
     * @param record {@link T} instance
     */
    public void addRecord(T record) {
        lastRecordIndex ++;
        records.add(record);
        updateRecordsCount();

        onRecordAdded(record);

    }

    abstract public void createRecord(T record);

    /**
     * Delete a record to repository
     * @param record {@link T} instance
     */
    public void deleteRecord(T record) {
        records.remove(record);
        updateRecordsCount();

        onRecordDeleted(record);

    }

    /**
     * Clear all records from repository
     */
    public void clearRecord() {
        records.clear();
        updateRecordsCount();

        onRecordCleared();

    }

    /**
     * Clear all records and replace a new one
     * @param records {@link ArrayList} of new records
     * @param invokeCallback whether to show callback using the new addRecord() method or using original {@link ArrayList} addAll() method instead
     */
    public void setRecords(@NonNull List<T> records, @Nullable Boolean invokeCallback) {
        getRecords().clear();

        if (invokeCallback == null || !invokeCallback) {
            getRecords().addAll(records); // Will NOT trigger any events
            return;
        }

        // New method will use addRecord instead
        for (T record : records) {
            addRecord(record); // Will also trigger onRecordAdded
        }

    }

    abstract public void onRecordAdded(T record);
    abstract public void onRecordDeleted(T record);
    abstract public void onRecordCleared();

    public T getRecordByIndex(int i) {
        return records.get(i);
    }

    /**
     * Get the total records count in the repository
     * @return int of total records count
     */
    public int getRecordsCount() {
        return records.size();
    }

    /**
     * Updates the count observable value to match current records count
     */
    private void updateRecordsCount() {
        recordsCount.postValue(getRecordsCount());
    }

    /**
     * Set the last inserted record index
     * @param index int index
     */
    public void setLastRecordIndex(int index) {
        this.lastRecordIndex = index;
    }

    /**
     * Get index of the last record position from the repository
     * @return int of last record position index
     */
    public int getLastRecordIndex() {
        return lastRecordIndex;
    }

    /**
     * Get the observable of records count
     * @return {@link LiveData} observable
     */
    public LiveData<Integer> getRecordsCountObservable() {
        return recordsCount;
    }

}
