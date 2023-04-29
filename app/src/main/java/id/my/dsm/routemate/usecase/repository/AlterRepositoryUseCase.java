package id.my.dsm.routemate.usecase.repository;

import javax.inject.Inject;

import id.my.dsm.routemate.data.event.repo.OnRepositoryUpdate;
import id.my.dsm.routemate.data.model.maps.MapboxDirectionsRoute;
import id.my.dsm.routemate.data.place.Place;
import id.my.dsm.routemate.data.repo.distance.DistanceRepositoryN;
import id.my.dsm.routemate.data.repo.distance.SolutionRepositoryN;
import id.my.dsm.routemate.data.repo.mapbox.MapboxDirectionsRouteRepository;
import id.my.dsm.routemate.data.repo.place.PlaceRepositoryN;
import id.my.dsm.routemate.data.repo.user.UserRepository;
import id.my.dsm.routemate.data.repo.vehicle.VehicleRepositoryN;
import id.my.dsm.routemate.library.dsmlib.model.MatrixElement;
import id.my.dsm.routemate.library.dsmlib.model.Vehicle;
import id.my.dsm.routemate.usecase.userdata.UploadUserDataUseCase;

public class AlterRepositoryUseCase {

    private static final String TAG = AlterRepositoryUseCase.class.getName();
    private final UserRepository userRepository;
    private final PlaceRepositoryN placeRepository;
    private final VehicleRepositoryN vehicleRepository;
    private final DistanceRepositoryN distanceRepository;
    private final SolutionRepositoryN solutionRepository;
    private final MapboxDirectionsRouteRepository mapboxDirectionsRouteRepository;
    private final UploadUserDataUseCase uploadUserDataUseCase;

    @Inject
    public AlterRepositoryUseCase(UserRepository userRepository, PlaceRepositoryN placeRepository, VehicleRepositoryN vehicleRepository, DistanceRepositoryN distanceRepository, SolutionRepositoryN solutionRepository, MapboxDirectionsRouteRepository mapboxDirectionsRouteRepository, UploadUserDataUseCase uploadUserDataUseCase) {
        this.userRepository = userRepository;
        this.placeRepository = placeRepository;
        this.vehicleRepository = vehicleRepository;
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
                else if (record instanceof Vehicle)
                    vehicleRepository.createRecord((Vehicle) record);
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
                else if (record instanceof Vehicle) {
                    vehicleRepository.deleteRecord((Vehicle) record);
                }

                break;
            case ACTION_CLEAR:

                // Clear record
                if (record instanceof Place) {
                    placeRepository.clearRecord();
                }
                else if (record instanceof Vehicle) {
                    vehicleRepository.clearRecord(solutionRepository);
                }
                else if (record instanceof MapboxDirectionsRoute)
                    mapboxDirectionsRouteRepository.clearRecord();

                break;

            case ACTION_NONE:
                break;
            case ACTION_SET_DEFAULT:

                // Set default (currently used in Vehicle instance)
                if (record instanceof Vehicle)
                    vehicleRepository.setDefaultVehicle((Vehicle) record);

                break;
            case ACTION_CLEAR_DEFAULT:

                // Clear default (currently used in Vehicle instance)
                if (record instanceof Vehicle)
                    vehicleRepository.clearDefaultVehicle((Vehicle) record);

                break;
        }

        // Sync place
        if (sync)
            uploadUserDataUseCase.invoke(record instanceof Place, record instanceof Vehicle, record instanceof MatrixElement, record instanceof MatrixElement, record instanceof MapboxDirectionsRoute);

    }

}
