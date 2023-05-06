package id.my.dsm.routemate.ui.recyclerview;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.greenrobot.eventbus.EventBus;

import java.text.NumberFormat;
import java.util.List;

import id.my.dsm.routemate.R;
import id.my.dsm.routemate.data.enums.MapsAPI;
import id.my.dsm.routemate.data.event.repo.OnRepositoryUpdate;
import id.my.dsm.routemate.data.event.viewmodel.OnMapsViewModelRequest;
import id.my.dsm.routemate.data.model.fleet.Fleet;
import id.my.dsm.routemate.data.model.user.DSMPlan;
import id.my.dsm.routemate.data.repo.distance.SolutionRepositoryN;
import id.my.dsm.routemate.ui.model.MaterialManager;
import id.my.dsm.routemate.ui.model.OptionsMenu;
import id.my.dsm.routemate.usecase.repository.AlterRepositoryUseCase;
import id.my.dsm.vrpsolver.model.Vehicle;

public class VehicleRecViewAdapter extends RecyclerView.Adapter<VehicleRecViewAdapter.ViewHolder> {

    private static final String TAG = VehicleRecViewAdapter.class.getName();

    // Dependencies
    private final List<Fleet> objects;
    private final SolutionRepositoryN solutionRepository;
    private final NavController navController;
    private final Context context;
    private MapsAPI mapsAPI;
    private DSMPlan plan;

    // Use case
    private final AlterRepositoryUseCase alterRepositoryUseCase;

    public VehicleRecViewAdapter(List<Fleet> objects, AlterRepositoryUseCase alterRepositoryUseCase, SolutionRepositoryN solutionRepository, NavController navController, MapsAPI mapsAPI, DSMPlan plan) {
        this.objects = objects;
        this.alterRepositoryUseCase = alterRepositoryUseCase;
        this.solutionRepository = solutionRepository;
        this.navController = navController;
        this.context = navController.getContext();
        this.mapsAPI = mapsAPI;
        this.plan = plan;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the list layout to the adapter
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_vehicle, parent, false);
        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    // Check whether a certain fleet is used in solution
    private boolean isVehicleOnline(Fleet fleet) {
        List<String> onlineVehicleId = solutionRepository.getOnlineVehiclesId();
        return onlineVehicleId.contains(fleet.getId());
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // Set the item's content matching the position/index here
        Fleet fleet = objects.get(position);
        Vehicle vehicle = fleet.getVehicle();

        // Adjust layout based on plan
        if (plan == DSMPlan.FREE)
            holder.layoutVehiclesListDemand.setVisibility(GONE);

        // Display the profile depending on the selected MapsAPI
        CharSequence vehicleProfile = mapsAPI == MapsAPI.MAPBOX ?
                fleet.getMapboxProfile().toPrettyString(context.getResources()) :
                fleet.getGoogleProfile().toPrettyString(context.getResources());

        // Change icon based on the fleet profile
        switch (mapsAPI) {
            case MAPBOX:
                switch (fleet.getMapboxProfile()) {
                    case DRIVING:
                        holder.imageVehiclesListIcon.setImageResource(R.drawable.ic_app_local_shipping);
                        break;
                    case DRIVING_TRAFFIC:
                        holder.imageVehiclesListIcon.setImageResource(R.drawable.ic_app_local_shipping);
                        break;
                    case WALKING:
                        holder.imageVehiclesListIcon.setImageResource(R.drawable.ic_app_directions_walk);
                        break;
                    case CYCLING:
                        holder.imageVehiclesListIcon.setImageResource(R.drawable.ic_app_directions_bike);
                        break;
                }
                break;
            case GOOGLE:
                switch (fleet.getGoogleProfile()) {
                    case DRIVING:
                        holder.imageVehiclesListIcon.setImageResource(R.drawable.ic_app_local_shipping);
                        break;
                    case WALKING:
                        holder.imageVehiclesListIcon.setImageResource(R.drawable.ic_app_directions_walk);
                        break;
                    case CYCLING:
                        holder.imageVehiclesListIcon.setImageResource(R.drawable.ic_app_directions_bike);
                        break;
                    case TRANSIT:
                        holder.imageVehiclesListIcon.setImageResource(R.drawable.ic_app_directions_transit);
                        break;
                }
                break;
        }

        MaterialManager.setButtonColor(holder.buttonVehiclesListVehicleColor, fleet.getColor()); // Vehicle color
        holder.textVehiclesListTitle.setText(fleet.getName());
        holder.textVehiclesListCapacity.setText(String.valueOf(NumberFormat.getInstance().format(vehicle.getCapacity())));
        holder.layoutVehiclesListIsDefault.setVisibility(vehicle.isDefault() ? VISIBLE : GONE);
        holder.textVehiclesListProfileValue.setText(vehicleProfile);

        if (!isVehicleOnline(fleet))
            holder.imageVehiclesListOnlineIcon.setVisibility(GONE);

        holder.cardVehiclesList.setOnClickListener(v -> {
            // Set up bundle containing index and navigate to VehiclesEditFragment
            Bundle bundle = new Bundle();
            bundle.putInt("vehicleIndex", position);
            navController.navigate(R.id.action_global_vehiclesEditFragment, bundle);
            Log.e(TAG, "Vehicle: " + fleet);
        });
        holder.cardVehiclesList.setOnLongClickListener(v -> {
            // Show places menu options
            OptionsMenu optionsMenu = new OptionsMenu(v.getContext(), v, R.menu.menu_popup_vehicle_item);

            // Set listeners
            optionsMenu.getPopupMenu().setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.vehicle_set_default_item:
                        if (vehicle.isDefault()) {
                            Toast.makeText(context, "The fleet is already default", Toast.LENGTH_SHORT).show();
                            break;
                        }
                        // Post repository event
                        alterRepositoryUseCase.invoke(OnRepositoryUpdate.Event.ACTION_SET_DEFAULT, fleet, true);
                        notifyItemRangeChanged(0, objects.size()); // Notify specific item has changed
                        break;
                    case R.id.vehicle_delete_item:
                        if (!vehicle.isDefault()) {
                            // Post repository event
                            alterRepositoryUseCase.invoke(OnRepositoryUpdate.Event.ACTION_DELETE, fleet, true);
                            notifyItemRemoved(position); // Notify item removed
                        }
                        else
                            Toast.makeText(context, "Unable to delete default fleet!", Toast.LENGTH_SHORT).show();
                        break;
                }
                return true;
            });

            optionsMenu.show(); // Show options menu
            return true;
        });
        holder.buttonVehiclesListVehicleColor.setOnClickListener(v -> {
            ColorPickerDialogBuilder
                    .with(context)
                    .setTitle("Choose Vehicle Route Color")
                    .initialColor(fleet.getColor())
                    .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
                    .density(12)
                    .setOnColorSelectedListener(selectedColor -> {

                    })
                    .setPositiveButton("Apply", (dialog, selectedColor, allColors) -> {

                        // Previous state
                        int previousVehicleColor = fleet.getColor();

                        // Save changes
                        fleet.setColor(selectedColor);
                        notifyItemChanged(position);

                        // Apply changes on the map if any
                        if (fleet.getColor() != previousVehicleColor)
                            EventBus.getDefault().post(
                                    new OnMapsViewModelRequest
                                            .Builder(OnMapsViewModelRequest.Event.ACTION_RELOAD_MAP)
                                            .build()
                            );

                    })
                    .setNegativeButton("Cancel", (dialog, which) -> {

                    })
                    .lightnessSliderOnly()
                    .build()
                    .show();
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
        MaterialCardView cardVehiclesList;
        MaterialButton buttonVehiclesListVehicleColor;
        TextView textVehiclesListTitle;
        ImageView imageVehiclesListIcon;
        ImageView imageVehiclesListOnlineIcon;
        TextView textVehiclesListCapacity;
        LinearLayout layoutVehiclesListDemand;
        LinearLayout layoutVehiclesListIsDefault;
        TextView textVehiclesListProfileValue;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            /*
                Assign view variables

                DO NOT assign listeners or variables related to adapter position here because the adapter position will always result -1 in this function!
                DO assign variables related to adapter position in other function and call it from bindViewHolder
            */
            cardVehiclesList = itemView.findViewById(R.id.cardVehiclesList);
            buttonVehiclesListVehicleColor = itemView.findViewById(R.id.buttonVehiclesListVehicleColor);
            imageVehiclesListIcon = itemView.findViewById(R.id.imageVehiclesListIcon);
            imageVehiclesListOnlineIcon = itemView.findViewById(R.id.imageVehiclesListOnlineIcon);
            textVehiclesListTitle = itemView.findViewById(R.id.textVehiclesListTitle);
            textVehiclesListCapacity = itemView.findViewById(R.id.textVehiclesListCapacity);
            layoutVehiclesListDemand = itemView.findViewById(R.id.layoutVehiclesListDemand);
            layoutVehiclesListIsDefault = itemView.findViewById(R.id.layoutVehiclesListIsDefault);
            textVehiclesListProfileValue = itemView.findViewById(R.id.textVehiclesListProfileValue);
        }
    }
}
