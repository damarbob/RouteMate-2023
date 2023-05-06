package id.my.dsm.routemate.usecase.userdata;

import java.util.List;

import javax.inject.Inject;

import id.my.dsm.routemate.data.model.fleet.Fleet;
import id.my.dsm.routemate.data.model.maps.MapboxDirectionsRoute;
import id.my.dsm.routemate.data.model.place.Place;
import id.my.dsm.routemate.data.repo.distance.DistanceRepositoryN;
import id.my.dsm.routemate.data.repo.distance.SolutionRepositoryN;
import id.my.dsm.routemate.data.repo.fleet.FleetRepository;
import id.my.dsm.routemate.data.repo.mapbox.MapboxDirectionsRouteRepository;
import id.my.dsm.routemate.data.repo.place.PlaceRepositoryN;
import id.my.dsm.vrpsolver.enums.OptimizationMethod;
import id.my.dsm.vrpsolver.model.MatrixElement;
import id.my.dsm.vrpsolver.model.Solution;
import id.my.dsm.vrpsolver.model.Vehicle;

public class LoadUserDataIntoRepositoriesUseCase {

    private static final String TAG = LoadUserDataIntoRepositoriesUseCase.class.getSimpleName();
    private final PlaceRepositoryN placeRepository;
    private final FleetRepository vehicleRepository;
    private final DistanceRepositoryN distanceRepository;
    private final SolutionRepositoryN solutionRepository;
    private final MapboxDirectionsRouteRepository mapboxDirectionsRouteRepository;

    @Inject
    public LoadUserDataIntoRepositoriesUseCase(PlaceRepositoryN placeRepository, FleetRepository vehicleRepository, DistanceRepositoryN distanceRepository, SolutionRepositoryN solutionRepository, MapboxDirectionsRouteRepository mapboxDirectionsRouteRepository) {
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
            List<Fleet> vehicles,
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
                    if (p.getLocation().getId().equals(d.getOrigin().getId()))
                        d.setOrigin(p.getLocation());

                    // Replace destination
                    if (p.getLocation().getId().equals(d.getDestination().getId()))
                        d.setDestination(p.getLocation());

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
