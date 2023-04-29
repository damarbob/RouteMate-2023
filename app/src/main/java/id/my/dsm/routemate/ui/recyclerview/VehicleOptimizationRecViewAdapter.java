package id.my.dsm.routemate.ui.recyclerview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import id.my.dsm.routemate.R;
import id.my.dsm.routemate.library.dsmlib.model.Vehicle;
import id.my.dsm.routemate.usecase.repository.AlterRepositoryUseCase;

public class VehicleOptimizationRecViewAdapter extends RecyclerView.Adapter<VehicleOptimizationRecViewAdapter.ViewHolder> {

    private static final String TAG = VehicleOptimizationRecViewAdapter.class.getName();

    // Dependencies
    private final List<Vehicle> objects;
    private NavController navController;
    private Context context;

    // Use case
    private AlterRepositoryUseCase alterRepositoryUseCase;

    public VehicleOptimizationRecViewAdapter(List<Vehicle> objects) {
        this.objects = objects;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the list layout to the adapter
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_vehicle_optimization, parent, false);
        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // Set the item's content matching the position/index here
        Vehicle vehicle = objects.get(position);

        holder.textOptimizationList.setText(vehicle.getName());

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
        TextView textOptimizationList;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            /*
                Assign view variables

                DO NOT assign listeners or variables related to adapter position here because the adapter position will always result -1 in this function!
                DO assign variables related to adapter position in other function and call it from bindViewHolder
            */
            textOptimizationList = itemView.findViewById(R.id.textOptimizationList);
        }
    }
}
