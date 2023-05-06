package id.my.dsm.routemate.usecase.repository;

import javax.inject.Inject;

import id.my.dsm.routemate.data.event.repo.OnRepositoryUpdate;
import id.my.dsm.routemate.data.model.fleet.Fleet;
import id.my.dsm.routemate.data.model.maps.MapboxDirectionsRoute;
import id.my.dsm.routemate.data.model.place.Place;
import id.my.dsm.routemate.data.repo.distance.DistanceRepositoryN;
import id.my.dsm.routemate.data.repo.distance.SolutionRepositoryN;
import id.my.dsm.routemate.data.repo.fleet.FleetRepository;
import id.my.dsm.routemate.data.repo.mapbox.MapboxDirectionsRouteRepository;
import id.my.dsm.routemate.data.repo.place.PlaceRepositoryN;
import id.my.dsm.routemate.data.repo.user.UserRepository;
import id.my.dsm.routemate.usecase.userdata.UploadUserDataUseCase;
import id.my.dsm.vrpsolver.model.MatrixElement;

public class AlterRepositoryUseCase {

    private static final String TAG = AlterRepositoryUseCase.class.getName();
    private final UserRepository userRepository;
    private final PlaceRepositoryN placeRepository;
    private final FleetRepository fleetRepository;
    private final DistanceRepositoryN distanceRepository;
    private final SolutionRepositoryN solutionRepository;
    private final MapboxDirectionsRouteRepository mapboxDirectionsRouteRepository;
    private final UploadUserDataUseCase uploadUserDataUseCase;

    @Inject
    public AlterRepositoryUseCase(UserRepository userRepository, PlaceRepositoryN placeRepository, FleetRepository fleetRepository, DistanceRepositoryN distanceRepository, SolutionRepositoryN solutionRepository, MapboxDirectionsRouteRepository mapboxDirectionsRouteRepository, UploadUserDataUseCase uploadUserDataUseCase) {
        this.userRepository = userRepository;
        this.placeRepository = placeRepository;
        this.fleetRepository = fleetRepository;
        this.distanceRepository = distanceRepository;
        this.solutionRepository = solutionRepository;
        this.mapboxDirectionsRouteRepository = mapboxDirectionsRouteRepository;
        this.uploadUserDataUseCase = uploadUserDataUseCase;
    }

    public <T> void invoke(OnRepositoryUpdate.Event event, T record, boolean sync) {

        switch (event) {
            case ACTION_CREATE:

                // Create record using predefined
                if (record instanceof Place)
                    placeRepository.createRecord((Place) record);
                else if (record instanceof Fleet)
                    fleetRepository.createRecord((Fleet) record);
                else if (record instanceof MapboxDirectionsRoute)
                    mapboxDirectionsRouteRepository.createRecord((MapboxDirectionsRoute) record);

                break;
            case ACTION_ADD:

                break;
            case ACTION_DELETE:

                // Delete record
                if (record instanceof Place) {
                    placeRepository.deleteRecord((Place) record);
                }
                else if (record instanceof Fleet) {
                    fleetRepository.deleteRecord((Fleet) record);
                }

                break;
            case ACTION_CLEAR:

                // Clear record
                if (record instanceof Place) {
                    placeRepository.clearRecord();
                }
                else if (record instanceof Fleet) {
                    fleetRepository.clearRecord(solutionRepository);
                }
                else if (record instanceof MapboxDirectionsRoute)
                    mapboxDirectionsRouteRepository.clearRecord();

                break;

            case ACTION_NONE:
                break;
            case ACTION_SET_DEFAULT:

                // Set default (currently used in Vehicle instance)
                if (record instanceof Fleet)
                    fleetRepository.switchDefaultVehicle((Fleet) record);

                break;
            case ACTION_CLEAR_DEFAULT:
                break;
        }

        // Sync place
        if (sync)
            uploadUserDataUseCase.invoke(record instanceof Place, record instanceof Fleet, record instanceof MatrixElement, record instanceof MatrixElement, record instanceof MapboxDirectionsRoute);

    }

}
