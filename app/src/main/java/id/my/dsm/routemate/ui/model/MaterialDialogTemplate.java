package id.my.dsm.routemate.ui.model;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import id.my.dsm.routemate.R;

/**
 * All Material Dialog templates are stored here
 */
public class MaterialDialogTemplate {

    public static void showVersionInfo(Context context, DialogInterface.OnDismissListener onDismissListener) {
        new MaterialAlertDialogBuilder(context)
                .setTitle("Version Info")
                .setMessage(context.getString(R.string.version_info_dialog_message))
                .setNeutralButton("Close", null)
                .setPositiveButton("Official Site", (dialogInterface, i) -> {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(context.getString(R.string.app_url)));
                    context.startActivity(browserIntent);
                })
                .setOnDismissListener(onDismissListener)
                .show();
    }

}
