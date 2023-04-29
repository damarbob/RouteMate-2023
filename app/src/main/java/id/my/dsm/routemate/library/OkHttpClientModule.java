package id.my.dsm.routemate.library;

import com.google.maps.GeoApiContext;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import id.my.dsm.routemate.BuildConfig;
import okhttp3.OkHttpClient;

@Module
@InstallIn(SingletonComponent.class)
public class OkHttpClientModule {

    @Singleton
    @Provides
    OkHttpClient getOkHttpClient() {
        return new OkHttpClient(); // Prepare okhttp client for http request
    }

}
