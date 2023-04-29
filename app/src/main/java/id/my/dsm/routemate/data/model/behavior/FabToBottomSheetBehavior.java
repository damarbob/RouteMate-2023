package id.my.dsm.routemate.data.model.behavior;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class FabToBottomSheetBehavior extends CoordinatorLayout.Behavior<FloatingActionButton> {

    // Dependencies
    private FloatingActionButton fab;
    private int fabLevel;

    // TRIGGER ZONE CONSTANT
    private static final float TRIGGER_ZONE = 280;
    private static final float TRIGGER_OFFSET = 84;

    // LEVEL CONSTANTS
    public static final int BOTTOM_LEVEL_1 = 1;
    public static final int BOTTOM_LEVEL_2 = 2;
    public static final int TOP_LEVEL_1 = 101;

    public FabToBottomSheetBehavior() {
        super();
    }

    public FabToBottomSheetBehavior(Context context, FloatingActionButton fab, int fabLevel, AttributeSet attrs) {
        super(context, attrs);
        this.fab = fab;
        this.fabLevel = fabLevel;
    }

    // DEFAULT FUNCTIONALITY

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull FloatingActionButton child, @NonNull View dependency) {
        return dependency instanceof MaterialCardView;
    }

    @Override
    public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent, @NonNull FloatingActionButton child, @NonNull View dependency) {

        // Check level and apply different behavior depending on its level
        switch (fabLevel) {
            case BOTTOM_LEVEL_1:
                updateFabLevel1Bottom(parent, fab, dependency);
                break;
            case BOTTOM_LEVEL_2:
                updateFabLevel2Bottom(parent, fab, dependency);
                break;
            case TOP_LEVEL_1:
                updateFabLevel1Top(parent, fab, dependency);
                break;
        }

        return super.onDependentViewChanged(parent, child, dependency);
    }

    // BASIC FUNCTIONALITY

    /**
     * Updates position of fab at level 101 (at the very top).
     *
     * @param parent a parent instance of {@link CoordinatorLayout}
     * @param child a child instance of {@link MaterialCardView}
     * @param dependency a dependency instance of {@link View}
     */
    private void updateFabLevel1Top(@NonNull CoordinatorLayout parent, @NonNull FloatingActionButton child, @NonNull View dependency) {

        if (dependency.getTop() < TRIGGER_ZONE + TRIGGER_OFFSET) {
            float a = (float) (dependency.getTop()) / (TRIGGER_ZONE + TRIGGER_OFFSET);
            child.setScaleX(a);
            child.setScaleY(a);
            child.setEnabled(false);
        }
        else {
            child.setScaleX(1);
            child.setScaleY(1);
            child.setEnabled(true);
        }
    }

    /**
     * Updates position of fab at level 2 (near the FAB level 1).
     *
     * @param parent a parent instance of {@link CoordinatorLayout}
     * @param child a child instance of {@link MaterialCardView}
     * @param dependency a dependency instance of {@link View}
     */
    private void updateFabLevel2Bottom(@NonNull CoordinatorLayout parent, @NonNull FloatingActionButton child, @NonNull View dependency) {

        if (child.getTop() - TRIGGER_ZONE < TRIGGER_ZONE) {
            float a = (float) (child.getTop() - TRIGGER_ZONE) / TRIGGER_ZONE;
//            child.setScaleX(a);
//            child.setScaleY(a);
            child.setEnabled(false);
        }
        else {
//            child.setScaleX(1);
//            child.setScaleY(1);
            child.setEnabled(true);
        }
    }

    /**
     * Updates position of fab at level 1 (near the BottomSheet).
     *
     * @param parent a parent instance of {@link CoordinatorLayout}
     * @param child a child instance of {@link MaterialCardView}
     * @param dependency a dependency instance of {@link View}
     */
    private void updateFabLevel1Bottom(@NonNull CoordinatorLayout parent, @NonNull FloatingActionButton child, @NonNull View dependency) {

//        if (child.getTop() < TRIGGER_ZONE) {
//            float a = (float) (child.getTop()) / TRIGGER_ZONE;
//            child.setScaleX(a);
//            child.setScaleY(a);
//            child.setEnabled(false);
//
//        }
//        else {
//            child.setScaleX(1);
//            child.setScaleY(1);
//            child.setEnabled(true);
//
//        }
    }

}
