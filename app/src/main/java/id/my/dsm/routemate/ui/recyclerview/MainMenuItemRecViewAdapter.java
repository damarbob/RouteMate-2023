package id.my.dsm.routemate.ui.recyclerview;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.util.ArrayList;

import id.my.dsm.routemate.R;
import id.my.dsm.routemate.data.model.user.DSMPlan;
import id.my.dsm.routemate.ui.model.DisplayMetrics;

public class MainMenuItemRecViewAdapter extends RecyclerView.Adapter<MainMenuItemRecViewAdapter.ViewHolder> {

    private ArrayList<MainMenuItem> objects;
    private NavController navController;
    private DSMPlan plan;

    public MainMenuItemRecViewAdapter(ArrayList<MainMenuItem> objects, NavController navController, DSMPlan plan) {
        this.objects = objects;
        this.navController = navController;
        this.plan = plan;
    }

    // In case of plan change
    public void setPlan(DSMPlan plan) {
        this.plan = plan;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the list layout to the adapter
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_menu_main, parent, false);
        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        // Set the item's content matching the position/index here
        MainMenuItem object = objects.get(position);

        if (object.getTitle() == MainMenuItem.Title.Matrix && plan == DSMPlan.FREE)
            holder.mainMenuItemParent.setVisibility(GONE);

        holder.mainMenuItemTitle.setText(object.getTitle().toString());
        holder.mainMenuItemDescription.setText(object.getDescription());
        holder.mainMenuItemIcon.setImageResource(object.getDrawableId());
        holder.mainMenuItemNumber.setText("" + object.getCountable());

        if (object.getLabel() != null) {
            holder.mainMenuItemLabel.setVisibility(VISIBLE);
            holder.mainMenuItemLabel.setText(object.getLabel());
        }
        else {
            holder.mainMenuItemLabel.setVisibility(GONE);
        }

        if (object.getActionId() > 0) {
            holder.mainMenuItemParent.setOnClickListener(v -> {
                navController.navigate(
                            object.getActionId()
                        );
            });
        }

        Context context = holder.mainMenuItemParent.getContext();
        Resources resources = context.getResources();
        boolean isDark = false;

        int nightModeFlags =
                context.getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                isDark = true;
                break;
            case Configuration.UI_MODE_NIGHT_NO:
            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                isDark = false;
                break;
        }


        @ColorInt int colorOnPrimary = getColorFromAttr(com.google.android.material.R.attr.colorOnPrimary, context);

        switch (object.getStyle()) {
            case DEFAULT:

                holder.mainMenuItemNumber.setVisibility(GONE);
                holder.imageMainMenuItemArrow.setVisibility(VISIBLE);

                break;
            case DEFAULT_COUNT:

                holder.mainMenuItemNumber.setVisibility(VISIBLE);
                holder.imageMainMenuItemArrow.setVisibility(GONE);

                break;
            case OUTLINE:

                holder.mainMenuItemParent.setStrokeColor(getColorFromAttr(com.google.android.material.R.attr.colorPrimary, context));
                holder.mainMenuItemParent.setStrokeWidth((int) DisplayMetrics.dpFromPx(context, 2));

                holder.mainMenuItemNumber.setVisibility(GONE);
                holder.imageMainMenuItemArrow.setVisibility(VISIBLE);

                break;
            case OUTLINE_COUNT:

                holder.mainMenuItemParent.setStrokeColor(getColorFromAttr(com.google.android.material.R.attr.colorPrimary, context));
                holder.mainMenuItemParent.setStrokeWidth(4);

                holder.mainMenuItemNumber.setVisibility(VISIBLE);
                holder.imageMainMenuItemArrow.setVisibility(GONE);

                break;
            case OPAQUE:

                holder.mainMenuItemParent.setCardBackgroundColor(getColorFromAttr(com.google.android.material.R.attr.colorPrimary, context));
                holder.mainMenuItemTitle.setTextColor(colorOnPrimary);
                holder.mainMenuItemDescription.setTextColor(colorOnPrimary);
                holder.mainMenuItemNumber.setTextColor(colorOnPrimary);
                holder.mainMenuItemIcon.setImageTintList(ColorStateList.valueOf(colorOnPrimary));
                holder.imageMainMenuItemArrow.setImageTintList(ColorStateList.valueOf(colorOnPrimary));

                holder.mainMenuItemNumber.setVisibility(GONE);
                holder.imageMainMenuItemArrow.setVisibility(VISIBLE);

                break;
            case OPAQUE_COUNT:

                holder.mainMenuItemParent.setCardBackgroundColor(getColorFromAttr(com.google.android.material.R.attr.colorPrimary, context));
                holder.mainMenuItemTitle.setTextColor(colorOnPrimary);
                holder.mainMenuItemDescription.setTextColor(colorOnPrimary);
                holder.mainMenuItemNumber.setTextColor(colorOnPrimary);
                holder.mainMenuItemIcon.setImageTintList(ColorStateList.valueOf(colorOnPrimary));
                holder.imageMainMenuItemArrow.setImageTintList(ColorStateList.valueOf(colorOnPrimary));

                holder.mainMenuItemNumber.setVisibility(VISIBLE);
                holder.imageMainMenuItemArrow.setVisibility(GONE);

                break;
            case OPAQUE_GREEN:

                holder.mainMenuItemParent.setCardBackgroundColor(resources.getColor(R.color.green_500));
                holder.mainMenuItemTitle.setTextColor(colorOnPrimary);
                holder.mainMenuItemDescription.setTextColor(colorOnPrimary);
                holder.mainMenuItemNumber.setTextColor(colorOnPrimary);
                holder.mainMenuItemIcon.setImageTintList(ColorStateList.valueOf(colorOnPrimary));
                holder.imageMainMenuItemArrow.setImageTintList(ColorStateList.valueOf(colorOnPrimary));

                holder.mainMenuItemNumber.setVisibility(GONE);
                holder.imageMainMenuItemArrow.setVisibility(VISIBLE);

                break;
            case OPAQUE_GREEN_COUNT:

                holder.mainMenuItemParent.setCardBackgroundColor(resources.getColor(R.color.green_500));
                holder.mainMenuItemTitle.setTextColor(colorOnPrimary);
                holder.mainMenuItemDescription.setTextColor(colorOnPrimary);
                holder.mainMenuItemNumber.setTextColor(colorOnPrimary);
                holder.mainMenuItemIcon.setImageTintList(ColorStateList.valueOf(colorOnPrimary));
                holder.imageMainMenuItemArrow.setImageTintList(ColorStateList.valueOf(colorOnPrimary));

                holder.mainMenuItemNumber.setVisibility(VISIBLE);
                holder.imageMainMenuItemArrow.setVisibility(GONE);

                break;
        }
    }

    @ColorInt
    private int getColorFromAttr(int attr, Context context) {
        TypedValue tv = new TypedValue();
        context.getTheme().resolveAttribute(attr, tv, true);
        return context.getResources().getColor(tv.resourceId);
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
        MaterialCardView mainMenuItemParent;
        TextView mainMenuItemTitle;
        TextView mainMenuItemDescription;
        TextView mainMenuItemLabel;
        TextView mainMenuItemNumber;
        ImageView mainMenuItemIcon;
        ImageView imageMainMenuItemArrow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            /*
                Assign view variables

                DO NOT assign listeners or variables related to adapter position here because the adapter position will always result -1 in this function!
                DO assign variables related to adapter position in other function and call it from bindViewHolder
            */
            mainMenuItemParent = itemView.findViewById(R.id.mainMenuItemParent);
            mainMenuItemTitle = itemView.findViewById(R.id.mainMenuItemTitle);
            mainMenuItemDescription = itemView.findViewById(R.id.mainMenuItemDescription);
            mainMenuItemLabel = itemView.findViewById(R.id.mainMenuItemLabel);
            mainMenuItemNumber = itemView.findViewById(R.id.mainMenuItemNumber);
            mainMenuItemIcon = itemView.findViewById(R.id.mainMenuItemIcon);
            imageMainMenuItemArrow = itemView.findViewById(R.id.imageMainMenuItemArrow);

        }
    }
}
