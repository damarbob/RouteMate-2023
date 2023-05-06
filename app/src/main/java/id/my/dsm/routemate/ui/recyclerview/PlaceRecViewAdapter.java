package id.my.dsm.routemate.ui.recyclerview;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import org.greenrobot.eventbus.EventBus;

import java.text.NumberFormat;
import java.util.List;

import id.my.dsm.routemate.R;
import id.my.dsm.routemate.data.event.model.OnSelectedPlaceChangedEvent;
import id.my.dsm.routemate.data.event.repo.OnRepositoryUpdate;
import id.my.dsm.routemate.data.event.viewmodel.OnMapsViewModelRequest;
import id.my.dsm.routemate.data.model.place.Place;
import id.my.dsm.routemate.ui.model.OptionsMenu;
import id.my.dsm.routemate.usecase.repository.AlterRepositoryUseCase;
import id.my.dsm.vrpsolver.model.Location;

public class PlaceRecViewAdapter extends RecyclerView.Adapter<PlaceRecViewAdapter.ViewHolder> {

    // Dependencies
    private List<Place> objects;
    private final NavController navController;

    // Use case
    private final AlterRepositoryUseCase alterRepositoryUseCase;

    public PlaceRecViewAdapter(List<Place> objects, AlterRepositoryUseCase alterRepositoryUseCase, NavController navController) {
        this.objects = objects;
        this.alterRepositoryUseCase = alterRepositoryUseCase;
        this.navController = navController;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the list layout to the adapter
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_place, parent, false);
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
        Location location = place.getLocation();

        holder.imagePlacesListIcon.setImageResource(
                place.getLocation().getProfile() == Location.Profile.SOURCE ?
                        R.drawable.ic_app_warehouse : R.drawable.ic_app_pin_drop);

        holder.textPlacesListTitle.setText(place.getName());

        holder.layoutPlacesListDemand.setVisibility(location.getProfile() == Location.Profile.SOURCE ? View.GONE : location.getDemands() > 0 ? View.VISIBLE : View.GONE);
        holder.textPlacesListDemand.setText(NumberFormat.getInstance().format(location.getDemands()));

        holder.textPlacesListProfileValue.setText(place.getLocation().getProfile().toString());
        holder.textPlacesListDemandsValue.setText("" + location.getDemands());

        holder.textPlacesListDebugId.setText("" + place.getId());

        // Set up listeners
        holder.cardPlacesList.setOnClickListener(v -> {
            // TODO: Found duplicate in LocationFragment, SIMPLIFY
            // Set up bundle containing index and navigate to PlacesEditFragment
            Bundle bundle = new Bundle();
            bundle.putString("placeId", place.getId());
            navController.navigate(R.id.action_global_placesEditFragment, bundle);

        });
        holder.cardPlacesList.setOnLongClickListener(v -> {

            // Show places menu options
            OptionsMenu optionsMenu = new OptionsMenu(v.getContext(), v, R.menu.menu_popup_place_item);

            // Set listeners
            optionsMenu.getPopupMenu().setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.place_goto_item:
                        // Center the camera to selected objective
                        EventBus.getDefault().post(
                                new OnMapsViewModelRequest.Builder(OnMapsViewModelRequest.Event.ACTION_CENTER_CAMERA)
                                        .withObjective(place)
                                        .build()
                        );

                        // TODO: Use args
                        // Navigate to location fragment
                        EventBus.getDefault().post(new OnSelectedPlaceChangedEvent(place));
                        navController.navigate(R.id.action_global_locationFragment);
                        break;
                    case R.id.place_delete_item:
                        // Post repository event
                        alterRepositoryUseCase.invoke(OnRepositoryUpdate.Event.ACTION_DELETE, place, true);
                        break;
                }
                return true;
            });

            optionsMenu.show(); // Show options menu
            return true;

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
        MaterialCardView cardPlacesList;
        ImageView imagePlacesListIcon;
        TextView textPlacesListTitle;
        LinearLayout layoutPlacesListDemand;
        TextView textPlacesListDemand;
        TextView textPlacesListProfileValue;
        TextView textPlacesListDemandsValue;
        TextView textPlacesListDebugId;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            /*
                Assign view variables

                DO NOT assign listeners or variables related to adapter position here because the adapter position will always result -1 in this function!
                DO assign variables related to adapter position in other function and call it from bindViewHolder
            */
            cardPlacesList = itemView.findViewById(R.id.cardPlacesList);
            imagePlacesListIcon = itemView.findViewById(R.id.imagePlacesListIcon);
            textPlacesListTitle = itemView.findViewById(R.id.textPlacesListTitle);
            layoutPlacesListDemand = itemView.findViewById(R.id.layoutPlacesListDemand);
            textPlacesListDemand = itemView.findViewById(R.id.textPlacesListDemand);
            textPlacesListProfileValue = itemView.findViewById(R.id.textPlacesListProfileValue);
            textPlacesListDemandsValue = itemView.findViewById(R.id.textPlacesListDemandsValue);
            textPlacesListDebugId = itemView.findViewById(R.id.textPlacesListDebugId);
        }
    }
}
