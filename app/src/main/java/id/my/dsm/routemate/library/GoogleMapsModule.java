package id.my.dsm.routemate.library;

import com.google.maps.GeoApiContext;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import id.my.dsm.routemate.BuildConfig;

@Module
@InstallIn(SingletonComponent.class)
public class GoogleMapsModule {

    @Singleton
    @Provides
    GeoApiContext getGeoApiContext() {
        return new GeoApiContext.Builder()
                .apiKey(BuildConfig.GOOGLE_API_KEY)
                .build();
    }

}
