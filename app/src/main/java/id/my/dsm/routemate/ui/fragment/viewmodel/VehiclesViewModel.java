package id.my.dsm.routemate.ui.fragment.viewmodel;

import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import id.my.dsm.routemate.data.event.repo.OnRepositoryUpdate;
import id.my.dsm.routemate.library.dsmlib.model.Vehicle;
import id.my.dsm.routemate.usecase.repository.AlterRepositoryUseCase;

@HiltViewModel
public class VehiclesViewModel extends ViewModel {

    private static final String TAG = "VehiclesViewModel";

    // Dependencies


    // Use case
    @Inject
    AlterRepositoryUseCase alterRepositoryUseCase;

    @Inject
    public VehiclesViewModel() {
    }

    public void createVehicle(Vehicle vehicle) {
        alterRepositoryUseCase.invoke(OnRepositoryUpdate.Event.ACTION_CREATE, vehicle, false);
    }

    public void clearVehicles() {
        alterRepositoryUseCase.invoke(OnRepositoryUpdate.Event.ACTION_CLEAR, new Vehicle(), true);
    }

}