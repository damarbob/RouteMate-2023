package id.my.dsm.routemate.usecase.distance;

import android.util.Log;

import com.google.maps.GeoApiContext;
import com.google.maps.model.TravelMode;

import java.util.List;

import javax.inject.Inject;

import id.my.dsm.routemate.data.enums.GoogleProfile;
import id.my.dsm.routemate.data.enums.MapboxProfile;
import id.my.dsm.routemate.data.enums.MapsAPI;
import id.my.dsm.routemate.data.model.fleet.Fleet;
import id.my.dsm.routemate.data.model.place.Place;
import id.my.dsm.routemate.data.repo.distance.DistanceRepositoryN;
import id.my.dsm.routemate.data.repo.fleet.FleetRepository;
import id.my.dsm.routemate.data.repo.place.PlaceRepositoryN;
import id.my.dsm.vrpsolver.enums.DistancesMethod;
import id.my.dsm.vrpsolver.model.MatrixElement;

public class RequestDistanceMatrixUseCase {

    private static final String TAG = RequestDistanceMatrixUseCase.class.getName();

    private final PlaceRepositoryN placeRepository;
    private final FleetRepository vehicleRepository;
    private final DistanceRepositoryN distanceRepository;
    private final List<Place> places;
    private final List<Fleet> fleets;
    private final List<MatrixElement> matrixElements;
    private final FillAirDistanceMatrixUseCase fillAirDistanceMatrixUseCase;
    private final RequestGoogleDistanceMatrixUseCase requestGoogleDistanceMatrixUseCase;
    private final RequestMapboxDistanceMatrixUseCase requestMapboxDistanceMatrixUseCase;
    private final RequestMapboxDirectionsRouteDistanceMatrixUseCase requestMapboxDirectionsRouteDistanceMatrixUseCase;

    @Inject
    public RequestDistanceMatrixUseCase(
            PlaceRepositoryN placeRepository,
            FleetRepository vehicleRepository,
            DistanceRepositoryN distanceRepository,
            FillAirDistanceMatrixUseCase fillAirDistanceMatrixUseCase,
            RequestGoogleDistanceMatrixUseCase requestGoogleDistanceMatrixUseCase,
            RequestMapboxDistanceMatrixUseCase requestMapboxDistanceMatrixUseCase,
            RequestMapboxDirectionsRouteDistanceMatrixUseCase requestMapboxDirectionsRouteDistanceMatrixUseCase
            ) {
        this.placeRepository = placeRepository;
        places = placeRepository.getRecords();
        this.vehicleRepository = vehicleRepository;
        fleets = vehicleRepository.getRecords();
        this.distanceRepository = distanceRepository;
        matrixElements = distanceRepository.getRecords();

        this.fillAirDistanceMatrixUseCase = fillAirDistanceMatrixUseCase;
        this.requestGoogleDistanceMatrixUseCase = requestGoogleDistanceMatrixUseCase;
        this.requestMapboxDistanceMatrixUseCase = requestMapboxDistanceMatrixUseCase;
        this.requestMapboxDirectionsRouteDistanceMatrixUseCase = requestMapboxDirectionsRouteDistanceMatrixUseCase;
    }

    public void invoke(DistancesMethod distancesMethod, MapsAPI mapsAPI, GeoApiContext geoApiContext, boolean continueOptimization) {

        Log.d(TAG, "invoke: placeRepository: " + placeRepository.toString() +
                " | vehicleRepository: " + vehicleRepository.toString() +
                " | distancesMethod: " + distancesMethod.name() +
                " | mapsAPI: " + mapsAPI.name());

        Fleet defaultVehicle = vehicleRepository.getDefaultVehicle();

        // TODO: FIX LOGIC
        /*
            Converts the vehicle profile to corresponding API profile, because
            - Mapbox requires String as the profile
            - Google requires TravelMode as the profile
            for use in the distance matrix API request
         */
        String mapboxProfile = defaultVehicle == null ? MapboxProfile.DRIVING.toDirectionsCriteria() : defaultVehicle.getMapboxProfile().toDirectionsCriteria();
        TravelMode googleProfile = defaultVehicle == null ? GoogleProfile.DRIVING.toTravelMode() : defaultVehicle.getGoogleProfile().toTravelMode();

        switch (distancesMethod) {
            case TRAVEL:
                switch (mapsAPI) {
                    case GOOGLE:
                        requestGoogleDistanceMatrixUseCase.invoke(geoApiContext, googleProfile, continueOptimization);
                        break;
                    case MAPBOX:
                        requestMapboxDistanceMatrixUseCase.invoke(mapboxProfile, continueOptimization);
                        break;
                }
                break;
            case AIR:
                fillAirDistanceMatrixUseCase.invoke(continueOptimization);
                break;
            case DIRECTIONS:
                switch (mapsAPI) {
                    case MAPBOX:
                        requestMapboxDirectionsRouteDistanceMatrixUseCase.invoke(mapboxProfile, continueOptimization);
                }
                break;
        }

    }



}
