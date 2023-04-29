package id.my.dsm.routemate.data.model.behavior;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentContainerView;

import com.google.android.material.card.MaterialCardView;
import com.mapbox.mapboxsdk.maps.MapView;

public class MapViewBehavior extends CoordinatorLayout.Behavior<FragmentContainerView> {
    public MapViewBehavior() {
    }

    public MapViewBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    // DEFAULT BEHAVIOR

    @Override
    public boolean layoutDependsOn(@NonNull CoordinatorLayout parent, @NonNull FragmentContainerView child, @NonNull View dependency) {
        /*
            The dependent of MapView is a MaterialCardView
            which acts a bottom sheet and also it's the MapView's
            sibling in the layout.
         */
        return dependency instanceof MaterialCardView;
    }

    @Override
    public boolean onDependentViewChanged(@NonNull CoordinatorLayout parent, @NonNull FragmentContainerView child, @NonNull View dependency) {
        /*
            Parallax effect with a ratio of 1:2 with dependent
         */

        if (!(dependency instanceof MaterialCardView)) {
            return false;
        }
        int offset = (child.getHeight() / 2) + dependency.getTop() - dependency.getTop() / 2;

        if (offset >= child.getHeight()) {
            child.setTranslationY(0);
            return true;
        }
        child.setTranslationY(offset - child.getHeight());

        return true;
    }
}
