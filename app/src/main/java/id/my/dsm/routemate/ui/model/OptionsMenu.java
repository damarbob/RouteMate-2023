package id.my.dsm.routemate.ui.model;

import android.content.Context;
import android.view.Gravity;
import android.view.MenuInflater;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.MenuRes;
import androidx.appcompat.view.ContextThemeWrapper;

import java.lang.reflect.Field;

import id.my.dsm.routemate.R;

public class OptionsMenu {

    private PopupMenu popupMenu;
    private Context context;
    private View view;
    private int menuGravity = Gravity.START;
    private @MenuRes int menuRes;

    public OptionsMenu(Context context, View view, int menuRes) {
        this.context = context;
        this.view = view;
        this.menuRes = menuRes;
        setup();
    }

    private void setup() {
        Context wrapper = new ContextThemeWrapper(context, R.style.Widget_App_PopupMenu);
        popupMenu = new PopupMenu(wrapper, view, menuGravity);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(menuRes, popupMenu.getMenu());

        // From https://stackoverflow.com/questions/15454995/popupmenu-with-icons
        Object menuHelper;
        Class[] argTypes;
        try {
            Field fMenuHelper = PopupMenu.class.getDeclaredField("mPopup");
            fMenuHelper.setAccessible(true);
            menuHelper = fMenuHelper.get(popupMenu);
            argTypes = new Class[]{boolean.class};
            menuHelper.getClass().getDeclaredMethod("setForceShowIcon", argTypes).invoke(menuHelper, true);
        } catch (Exception e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    public PopupMenu getPopupMenu() {
        return popupMenu;
    }

    public void show() {
        popupMenu.show();
    }

}
