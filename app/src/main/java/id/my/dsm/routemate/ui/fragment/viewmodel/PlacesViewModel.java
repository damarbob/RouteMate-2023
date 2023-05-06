package id.my.dsm.routemate.ui.fragment.viewmodel;

import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
import id.my.dsm.routemate.data.event.repo.OnRepositoryUpdate;
import id.my.dsm.routemate.data.model.place.Place;
import id.my.dsm.routemate.usecase.repository.AlterRepositoryUseCase;

@HiltViewModel
public class PlacesViewModel extends ViewModel {

    private static final String TAG = "PlacesViewModel";

    // Use case
    @Inject
    AlterRepositoryUseCase alterRepositoryUseCase;

    @Inject
    public PlacesViewModel() {
    }

    public void clearPlaces() {
        alterRepositoryUseCase.invoke(OnRepositoryUpdate.Event.ACTION_CLEAR, new Place(), true);
    }

}