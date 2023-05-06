package id.my.dsm.routemate.usecase.distance;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import javax.inject.Inject;

import id.my.dsm.routemate.data.event.network.OnDistanceMatrixResponse;
import id.my.dsm.routemate.data.event.view.OnProgressIndicatorUpdate;
import id.my.dsm.routemate.data.model.place.Place;
import id.my.dsm.routemate.data.repo.distance.DistanceRepositoryN;
import id.my.dsm.routemate.data.repo.place.PlaceRepositoryN;
import id.my.dsm.vrpsolver.model.MatrixElement;

public class FillAirDistanceMatrixUseCase {

    private final PlaceRepositoryN placeRepository;
    private final DistanceRepositoryN distanceRepository;

    @Inject
    public FillAirDistanceMatrixUseCase(PlaceRepositoryN placeRepository, DistanceRepositoryN distanceRepository) {
        this.placeRepository = placeRepository;
        this.distanceRepository = distanceRepository;
    }

    public void invoke(boolean continueOptimization) {

        List<Place> places = placeRepository.getRecords(); // Get places object from repository
        List<MatrixElement> matrixElements = distanceRepository.getRecords(); // Get matrixElements object from repository

        EventBus.getDefault().postSticky(new OnProgressIndicatorUpdate(25)); // To use the progressIndicator in activity

        distanceRepository.calculateAirDistances(places, true); // Alter the matrixElements

        // Update progressIndicator to 99 percent if continueOptimization (because 100 will start countdown to hide progressIndicator)
        EventBus.getDefault().postSticky(
                new OnProgressIndicatorUpdate(
                        continueOptimization ? 99 : 100
                )
        ); // To use the progressIndicator in activity

        // OnFinished callback
        EventBus.getDefault().post(
                new OnDistanceMatrixResponse(matrixElements, continueOptimization)
        );

    }

}
