package id.my.dsm.routemate.ui.recyclerview;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;

import id.my.dsm.routemate.R;
import id.my.dsm.routemate.data.model.fleet.Fleet;
import id.my.dsm.routemate.data.model.place.Place;
import id.my.dsm.routemate.data.repo.fleet.FleetRepository;
import id.my.dsm.routemate.data.repo.place.PlaceRepositoryN;
import id.my.dsm.routemate.ui.model.DistanceMeasurementUnit;
import id.my.dsm.routemate.ui.model.MaterialManager;
import id.my.dsm.routemate.ui.model.MeasurementConversion;
import id.my.dsm.routemate.ui.model.RouteMatePref;
import id.my.dsm.vrpsolver.model.Solution;

public class SolutionRecViewAdapter extends RecyclerView.Adapter<SolutionRecViewAdapter.ViewHolder> {

    // Dependencies
    private List<Solution> objects;
    private final PlaceRepositoryN placeRepository;
    private final FleetRepository vehicleRepository;
    private final Activity activity;
    private DistanceMeasurementUnit distanceMeasurementUnit;

    public SolutionRecViewAdapter(List<Solution> objects, PlaceRepositoryN placeRepository , FleetRepository vehicleRepository, Activity activity, DistanceMeasurementUnit distanceMeasurementUnit) {
        this.objects = objects;
        this.placeRepository = placeRepository;
        this.vehicleRepository = vehicleRepository;
        this.activity = activity;
        this.distanceMeasurementUnit = distanceMeasurementUnit;
    }

    public void setObjects(List<Solution> objects) {
        this.objects = objects;
    }

    public List<Solution> getObjects() {
        return objects;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the list layout to the adapter
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_solution, parent, false);
        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // Set the item's content matching the position/index here
        Solution solution = objects.get(position);

        Place origin = Place.Toolbox.getById(placeRepository.getRecords(), solution.getOrigin().getId());
        Place destination = Place.Toolbox.getById(placeRepository.getRecords(), solution.getDestination().getId());

        double dist = solution.getDistance(); // Get solution value

        String originName = origin != null ? origin.getName() : "Missing Place";
        String destName = destination != null ? destination.getName() : "Missing Place";

        holder.textSolutionDestination.setText(destName);

        String formattedTravelDistance = null;
        int unitStringRes = R.string.measurement_kilometer;
        distanceMeasurementUnit = RouteMatePref.readMeasurementDistanceUnit((Activity) activity);
        switch (distanceMeasurementUnit) {
            case METER:
                formattedTravelDistance = NumberFormat.getInstance().format(dist);
                unitStringRes = R.string.measurement_meter;
                break;
            case KILOMETER:
                formattedTravelDistance = NumberFormat.getInstance().format(MeasurementConversion.Companion.metersToKilometers(dist));
                unitStringRes = R.string.measurement_kilometer;
                break;
            case MILE:
                formattedTravelDistance = NumberFormat.getInstance().format(MeasurementConversion.Companion.metersToMiles(dist));
                unitStringRes = R.string.measurement_mile;
                break;
        }
        holder.textSolutionDistance.setText(activity.getString(unitStringRes, formattedTravelDistance));

        holder.textSolutionToDeliverValue.setText("" + solution.getDemand() + " units");

        String vehicleName = "";
        if (solution.getVehicleId() != null) {
            Fleet fleet = vehicleRepository.getFleetById(solution.getVehicleId());
            vehicleName = fleet.getName();
            int vehicleColor = fleet.getColor();
            MaterialManager.setImageColor(holder.imageSolutionPreIcon, vehicleColor);
            MaterialManager.setImageColor(holder.imageSolutionPreLine, vehicleColor);
            MaterialManager.setImageColor(holder.imageSolutionLine, vehicleColor);
            MaterialManager.setImageColor(holder.imageSolutionIcon, vehicleColor);
        }

        holder.textSolutionCarriedValue.setText("" + solution.getCarry() + " units");

        holder.textSolutionVehicle.setText(vehicleName);

        // Display demand and carry as needed
        if (solution.getDemand() == 0.0 || solution.getDemand() == 0)
            holder.layoutSolutionDemand.setVisibility(GONE);

        if (solution.getCarry() == 0.0)
            holder.layoutSolutionCarry.setVisibility(GONE);

        // Position checking should be the last
        if (position == 0) {
            holder.layoutSolutionPreLine.setVisibility(VISIBLE); // Display pre line for the first solution (by default it's GONE)
            holder.textSolutionStart.setText("Start at " + originName);
        }
        else {
            holder.layoutSolutionPreLine.setVisibility(GONE);

            Solution prevSolution = objects.get(position - 1);
            if (prevSolution.getVehicleId() == null)
                return;

            String vehicle = vehicleRepository.getFleetById(solution.getVehicleId()).getName();
            String prevVehicle = vehicleRepository.getFleetById(prevSolution.getVehicleId()).getName();
//            holder.textSolutionDebug.setText(vehicle + " | " + prevVehicle);

            if (solution.getVehicleId().equals(prevSolution.getVehicleId())) {
                holder.textSolutionVehicle.setVisibility(GONE);
            }
            else {
                // Same as if position == 0 above
                holder.layoutSolutionPreLine.setVisibility(VISIBLE);
                holder.textSolutionStart.setText("Start at " + originName);
            }

            if (position < getObjects().size() - 1 - 1) {
                Solution nextSolution = objects.get(position + 1);
                if (!solution.getVehicleId().equals(nextSolution.getVehicleId())) {
                    holder.imageSolutionLine.setVisibility(GONE);
                }
            }
            else if (position == getObjects().size() - 1) {
                holder.imageSolutionLine.setVisibility(GONE);
            }

        }

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
        LinearLayout layoutSolutionPreLine;
        ImageView imageSolutionPreIcon;
        ImageView imageSolutionPreLine;
        ImageView imageSolutionIcon;
        ImageView imageSolutionLine;
        TextView textSolutionStart;
        TextView textSolutionDestination;
        TextView textSolutionDistance;
        TextView textSolutionDebug;
        LinearLayout layoutSolutionDemand;
        LinearLayout layoutSolutionCarry;
        TextView textSolutionToDeliverValue;
        TextView textSolutionCarriedValue;
        TextView textSolutionVehicle;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            /*
                Assign view variables

                DO NOT assign listeners or variables related to adapter position here because the adapter position will always result -1 in this function!
                DO assign variables related to adapter position in other function and call it from bindViewHolder
            */
            layoutSolutionPreLine = itemView.findViewById(R.id.layoutSolutionPreLine);
            imageSolutionPreIcon = itemView.findViewById(R.id.imageSolutionPreIcon);
            imageSolutionPreLine = itemView.findViewById(R.id.imageSolutionPreLine);
            imageSolutionIcon = itemView.findViewById(R.id.imageSolutionIcon);
            imageSolutionLine = itemView.findViewById(R.id.imageSolutionLine);
            textSolutionStart = itemView.findViewById(R.id.textSolutionStart);
            textSolutionDestination = itemView.findViewById(R.id.textSolutionDestination);
            textSolutionDistance = itemView.findViewById(R.id.textSolutionDistance);
            textSolutionDebug = itemView.findViewById(R.id.textSolutionDebug);
            layoutSolutionDemand = itemView.findViewById(R.id.layoutSolutionDemand);
            layoutSolutionCarry = itemView.findViewById(R.id.layoutSolutionCarry);
            textSolutionToDeliverValue = itemView.findViewById(R.id.textSolutionToDeliverValue);
            textSolutionCarriedValue = itemView.findViewById(R.id.textSolutionCarriedValue);
            textSolutionVehicle = itemView.findViewById(R.id.textSolutionVehicle);
        }
    }
}
