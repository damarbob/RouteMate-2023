package id.my.dsm.routemate;

import android.app.Application;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.color.MaterialColors;
import com.google.firebase.database.FirebaseDatabase;
import com.instacart.library.truetime.TrueTime;

import java.io.IOException;

import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
//        DynamicColors.applyToActivitiesIfAvailable(this);

    }
}
