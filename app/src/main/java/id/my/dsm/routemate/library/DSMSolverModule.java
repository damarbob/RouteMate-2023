package id.my.dsm.routemate.library;


import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import id.my.dsm.routemate.library.dsmlib.DSMSolver;
import okhttp3.OkHttpClient;

@Module
@InstallIn(SingletonComponent.class)
public class DSMSolverModule {

    @Singleton
    @Provides
    DSMSolver getDSMSolver() {
        return new DSMSolver();
    }

}
