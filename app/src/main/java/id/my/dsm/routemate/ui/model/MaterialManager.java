package id.my.dsm.routemate.ui.model;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.util.TypedValue;
import android.widget.ImageView;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MaterialManager {

    public static final int[] COLOR = new int[]
    {
            Color.parseColor("#2196F3"),
            Color.parseColor("#42a5f5"),
            Color.parseColor("#64b5f6"),
            Color.parseColor("#2962ff"),
            Color.parseColor("#2979ff"),
            Color.parseColor("#00bcd4"),
            Color.parseColor("#26c6da"),
            Color.parseColor("#4dd0e1"),
            Color.parseColor("#00b8d4"),
            Color.parseColor("#00e5ff"),
    };

    public static void setImageColor(ImageView imageView, @ColorInt int color) {
        imageView.setImageTintList(ColorStateList.valueOf(color));
    }

    // Set FAB background color into the given colorResId
    public static void setButtonColor(MaterialButton materialButton, @ColorInt int backgroundColorResId) {
        materialButton.setBackgroundTintList(ColorStateList.valueOf(backgroundColorResId));
    }

    // Set FAB background color into the given colorResId
    public static void setFabColor(FloatingActionButton fab, @ColorRes int backgroundColorResId, @ColorRes int iconColorResId) {
        Context context = fab.getContext();

        fab.setBackgroundTintList(ContextCompat.getColorStateList(context, backgroundColorResId));
        fab.setImageTintList(ContextCompat.getColorStateList(context, iconColorResId));
    }

    // Reset FAB color to original
    public static void resetFabColor(FloatingActionButton fab) {
        Context context = fab.getContext();

        TypedValue tv = new TypedValue();
        context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, tv, true);
        TypedValue tv2 = new TypedValue();
        context.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, tv2, true);

        fab.setBackgroundTintList(ContextCompat.getColorStateList(context, tv.resourceId));
        fab.setImageTintList(ContextCompat.getColorStateList(context, tv2.resourceId));
    }

}
