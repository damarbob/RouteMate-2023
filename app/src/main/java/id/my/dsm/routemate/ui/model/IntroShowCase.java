package id.my.dsm.routemate.ui.model;

import android.content.Context;
import android.view.View;

import smartdevelop.ir.eram.showcaseviewlib.GuideView;
import smartdevelop.ir.eram.showcaseviewlib.config.DismissType;
import smartdevelop.ir.eram.showcaseviewlib.listener.GuideListener;

public class IntroShowCase {

    /**
     * Creates a new user guide.
     *
     * @param title string title
     * @param text string description
     * @param viewAnchor view anchor
     * @param onDismissListener a {@link GuideListener} instance
     */
    public static void show(Context context, String title, String text, View viewAnchor, GuideListener onDismissListener) {

        new GuideView.Builder(context)
                .setTitle(title)
                .setContentText(text)
                .setTargetView(viewAnchor)
                .setDismissType(DismissType.anywhere) //optional - default dismissible by TargetView
                .setGuideListener(onDismissListener)
                .build()
                .show();
    }

    /**
     * Creates a new user guide.
     *
     * @param title string title
     * @param text string description
     * @param viewAnchor view anchor
     */
    public static void show(Context context, String title, String text, View viewAnchor) {

        if (context == null)
            return;

        new GuideView.Builder(context)
                .setTitle(title)
                .setContentText(text)
                .setTargetView(viewAnchor)
                .setDismissType(DismissType.anywhere) //optional - default dismissible by TargetView
                .build()
                .show();
    }

    /**
     * Creates a new user guide.
     *
     * @param title string title
     * @param text string description
     * @param viewAnchor view anchor
     * @param onDismissListener a {@link GuideListener} instance
     */
    public static void show(Context context, String title, String text, View viewAnchor, boolean condition, GuideListener onDismissListener) {

        if (!condition)
            return;

        if (onDismissListener == null) {
            new GuideView.Builder(context)
                    .setTitle(title)
                    .setContentText(text)
                    .setTargetView(viewAnchor)
                    .setDismissType(DismissType.anywhere) //optional - default dismissible by TargetView
                    .build()
                    .show();

            return;
        }

        new GuideView.Builder(context)
                .setTitle(title)
                .setContentText(text)
                .setTargetView(viewAnchor)
                .setDismissType(DismissType.anywhere) //optional - default dismissible by TargetView
                .setGuideListener(onDismissListener)
                .build()
                .show();

    }

    /**
     * Creates a new user guide.
     *
     * @param title string title
     * @param text string description
     * @param viewAnchor view anchor
     */
    public static void show(Context context, String title, String text, View viewAnchor, boolean condition) {

        if (!condition)
            return;

        new GuideView.Builder(context)
                .setTitle(title)
                .setContentText(text)
                .setTargetView(viewAnchor)
                .setDismissType(DismissType.anywhere) //optional - default dismissible by TargetView
                .build()
                .show();

    }

}
