package id.my.dsm.routemate.ui.recyclerview;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.List;

import id.my.dsm.routemate.R;
import id.my.dsm.routemate.data.place.Place;
import id.my.dsm.routemate.data.repo.place.PlaceRepositoryN;
import id.my.dsm.routemate.library.dsmlib.model.MatrixElement;

public class DistanceRecViewAdapter extends RecyclerView.Adapter<DistanceRecViewAdapter.ViewHolder> {

    // Dependencies
    private final List<MatrixElement> objects;
    private final PlaceRepositoryN placeRepository;
    private final NavController navController;
    private final Context context;

    public DistanceRecViewAdapter(List<MatrixElement> objects, PlaceRepositoryN placeRepository, NavController navController) {
        this.objects = objects;
        this.placeRepository = placeRepository;
        this.navController = navController;
        this.context = navController.getContext();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the list layout to the adapter
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_distance, parent, false);
        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Set the item's content matching the position/index here
        MatrixElement matrixElement = objects.get(position);

        Place origin = Place.Toolbox.getById(placeRepository.getRecords(), matrixElement.getOrigin().getId());
        Place destination = Place.Toolbox.getById(placeRepository.getRecords(), matrixElement.getDestination().getId());

        holder.textDistancesListOrigin.setText(origin.getName());
        holder.textDistancesDestinationValue.setText(destination.getName());
        holder.textDistancesDistanceValue.setText("" + Math.round(matrixElement.getDistance() * 100.0) / 100.0 + " m");

        double savingDistance = Math.round(matrixElement.getSavingDistance() * 100.0) / 100.0;
        holder.textDistancesSavingDestinationValue.setText("" + savingDistance);

        if (Math.ceil(matrixElement.getSavingDistance()) > 0 || matrixElement.getSavingDistance() < 0) {
            holder.textDistancesSavingDestinationTitle.setVisibility(View.VISIBLE);
            holder.textDistancesSavingDestinationValue.setVisibility(View.VISIBLE);
        }
        else {
            holder.textDistancesSavingDestinationTitle.setVisibility(View.GONE);
            holder.textDistancesSavingDestinationValue.setVisibility(View.GONE);
        }

        holder.cardDistancesList.setOnClickListener(v -> {
            // Set up bundle containing index and navigate to DistancesEditFragment
            Bundle bundle = new Bundle();
            bundle.putInt("distanceIndex", position);
            bundle.putStringArray("placesName", new String[]{origin.getName(), destination.getName()});
            navController.navigate(R.id.action_global_distancesEditFragment, bundle);
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
        ImageView imageDistancesListIcon;
        MaterialCardView cardDistancesList;
        TextView textDistancesListOrigin;
        TextView textDistancesDestinationValue;
        TextView textDistancesDistanceValue;
        TextView textDistancesSavingDestinationTitle;
        TextView textDistancesSavingDestinationValue;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            /*
                Assign view variables

                DO NOT assign listeners or variables related to adapter position here because the adapter position will always result -1 in this function!
                DO assign variables related to adapter position in other function and call it from bindViewHolder
            */
            imageDistancesListIcon = itemView.findViewById(R.id.imageDistancesListIcon);
            cardDistancesList = itemView.findViewById(R.id.cardDistancesList);
            textDistancesListOrigin = itemView.findViewById(R.id.textDistancesListOrigin);
            textDistancesDestinationValue = itemView.findViewById(R.id.textDistancesDestinationValue);
            textDistancesDistanceValue = itemView.findViewById(R.id.textDistancesDistanceValue);
            textDistancesSavingDestinationTitle = itemView.findViewById(R.id.textDistancesSavingDestinationTitle);
            textDistancesSavingDestinationValue = itemView.findViewById(R.id.textDistancesSavingDestinationValue);
        }
    }
}
