package id.my.dsm.routemate.usecase.userdata;

import java.util.List;

import javax.inject.Inject;

import id.my.dsm.routemate.data.model.maps.MapboxDirectionsRoute;
import id.my.dsm.routemate.data.place.Place;
import id.my.dsm.routemate.data.repo.distance.DistanceRepositoryN;
import id.my.dsm.routemate.data.repo.distance.SolutionRepositoryN;
import id.my.dsm.routemate.data.repo.mapbox.MapboxDirectionsRouteRepository;
import id.my.dsm.routemate.data.repo.place.PlaceRepositoryN;
import id.my.dsm.routemate.data.repo.vehicle.VehicleRepositoryN;
import id.my.dsm.routemate.library.dsmlib.enums.OptimizationMethod;
import id.my.dsm.routemate.library.dsmlib.model.MatrixElement;
import id.my.dsm.routemate.library.dsmlib.model.Solution;
import id.my.dsm.routemate.library.dsmlib.model.Vehicle;

public class LoadUserDataIntoRepositoriesUseCase {

    private static final String TAG = LoadUserDataIntoRepositoriesUseCase.class.getSimpleName();
    private final PlaceRepositoryN placeRepository;
    private final VehicleRepositoryN vehicleRepository;
    private final DistanceRepositoryN distanceRepository;
    private final SolutionRepositoryN solutionRepository;
    private final MapboxDirectionsRouteRepository mapboxDirectionsRouteRepository;

    @Inject
    public LoadUserDataIntoRepositoriesUseCase(PlaceRepositoryN placeRepository, VehicleRepositoryN vehicleRepository, DistanceRepositoryN distanceRepository, SolutionRepositoryN solutionRepository, MapboxDirectionsRouteRepository mapboxDirectionsRouteRepository) {
        this.placeRepository = placeRepository;
        this.vehicleRepository = vehicleRepository;
        this.distanceRepository = distanceRepository;
        this.solutionRepository = solutionRepository;
        this.mapboxDirectionsRouteRepository = mapboxDirectionsRouteRepository;
    }

    /**
     * Store the retrieved user data to repositories and load directions to the map
     * @param places ArrayList of {@link Place} instances
     * @param vehicles ArrayList of {@link Vehicle} instances
     * @param matrix ArrayList of {@link MatrixElement} instances
     * @param solutions ArrayList of {@link Solution} instances
     * @param optimizationMethod The OptimizationMethod used in the solution
     */
    public void invoke(
            List<Place> places,
            List<Vehicle> vehicles,
            List<MatrixElement> matrix,
            List<Solution> solutions,
            List<MapboxDirectionsRoute> mapboxDirectionsRoutes,
            OptimizationMethod optimizationMethod,
            Boolean usingAdvancedAlgorithm
    ) {

        if (places != null) {
            placeRepository.setRecords(places, true);

            Place lastPlace = placeRepository.getRecords().get(
                    placeRepository.getRecords().size() - 1
            );

            placeRepository.setLastRecordIndex(
                    placeRepository.getRecords().indexOf(lastPlace) + 1
            ); // Set last place index to match the retrieved place records + 1
        }

        if (vehicles != null)
            vehicleRepository.setRecords(vehicles, true);

        // Re-assign matrix' origin and destination to current matching objectives set
        if (matrix != null) {

            for (MatrixElement d : matrix) {

                assert places != null;
                for (Place p : places) {

                    // Replace origin
                    if (p.getDsmPlace().getId().equals(d.getOrigin().getId()))
                        d.setOrigin(p.getDsmPlace());

                    // Replace destination
                    if (p.getDsmPlace().getId().equals(d.getDestination().getId()))
                        d.setDestination(p.getDsmPlace());

                }

            }

            distanceRepository.setRecords(matrix, true);

        }
        if (solutions != null)
            solutionRepository.setRecords(solutions, true);

        if (mapboxDirectionsRoutes != null)
            mapboxDirectionsRouteRepository.setRecords(mapboxDirectionsRoutes, true);

        if (optimizationMethod != null)
            solutionRepository.setOptimizationMethod(optimizationMethod);

        if (usingAdvancedAlgorithm != null)
            solutionRepository.setUsingAdvancedAlgorithm(usingAdvancedAlgorithm);

    }

}
