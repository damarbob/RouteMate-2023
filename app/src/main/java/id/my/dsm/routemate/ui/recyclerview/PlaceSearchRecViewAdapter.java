package id.my.dsm.routemate.ui.recyclerview;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import id.my.dsm.routemate.R;
import id.my.dsm.routemate.data.event.repo.OnRepositoryUpdate;
import id.my.dsm.routemate.data.event.view.OnMainActivityFeatureRequest;
import id.my.dsm.routemate.data.event.viewmodel.OnMapsViewModelRequest;
import id.my.dsm.routemate.data.model.place.Place;
import id.my.dsm.routemate.usecase.repository.AlterRepositoryUseCase;

public class PlaceSearchRecViewAdapter extends RecyclerView.Adapter<PlaceSearchRecViewAdapter.ViewHolder> {

    // Dependencies
    private List<Place> objects;

    // Use case
    private final AlterRepositoryUseCase alterRepositoryUseCase;

    public PlaceSearchRecViewAdapter(List<Place> objects, AlterRepositoryUseCase alterRepositoryUseCase) {
        this.objects = objects;
        this.alterRepositoryUseCase = alterRepositoryUseCase;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the list layout to the adapter
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_place_search, parent, false);
        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    public void setObjects(List<Place> objects) {
        this.objects = objects;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // Set the item's content matching the position/index here
        Place place = objects.get(position);

        holder.textPlacesSearchItemTitle.setText(place.getName());
        holder.textPlacesSearchItemDescription.setText(place.getDescription());

        holder.cardPlaceSearchItem.setOnClickListener(v -> {

            // Hide search panel
            EventBus.getDefault().post(
                    new OnMainActivityFeatureRequest(OnMainActivityFeatureRequest.Event.HIDE_SEARCH_PANEL)
            );

            // Center the camera to selected place
            EventBus.getDefault().post(
                    new OnMapsViewModelRequest.Builder(OnMapsViewModelRequest.Event.ACTION_CENTER_CAMERA)
                            .withObjective(place)
                            .build()
            );

        });
        holder.buttonPlaceSearchAddLocation.setOnClickListener(v -> {

            // Hide search panel
            EventBus.getDefault().post(
                    new OnMainActivityFeatureRequest(OnMainActivityFeatureRequest.Event.HIDE_SEARCH_PANEL)
            );

            // Create selected place
            alterRepositoryUseCase.invoke(
                    OnRepositoryUpdate.Event.ACTION_CREATE,
                    place,
                    true
            );

        });

    }

    @Override
    public int getItemCount() {
        return objects.size();
    }

    // ViewHolder can implements interface too
    public class ViewHolder extends RecyclerView.ViewHolder {

        /*
            Declare view variables here
         */
        MaterialCardView cardPlaceSearchItem;
        TextView textPlacesSearchItemTitle;
        TextView textPlacesSearchItemDescription;
        MaterialButton buttonPlaceSearchAddLocation;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            /*
                Assign view variables

                DO NOT assign listeners or variables related to adapter position here because the adapter position will always result -1 in this function!
                DO assign variables related to adapter position in other function and call it from bindViewHolder
            */
            cardPlaceSearchItem = itemView.findViewById(R.id.cardPlaceSearchItem);
            textPlacesSearchItemTitle = itemView.findViewById(R.id.textPlacesSearchItemTitle);
            textPlacesSearchItemDescription = itemView.findViewById(R.id.textPlacesSearchItemDescription);
            buttonPlaceSearchAddLocation = itemView.findViewById(R.id.buttonPlaceSearchAddLocation);
        }
    }
}
