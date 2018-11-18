package xyz.hui_yi.utils.SnackBarUtils;

import android.support.design.widget.Snackbar;
import android.view.View;

/**
 * Created by LiFen on 2018/3/25.
 */

public class SnackBarUtils {

    public static void show(View view, String text) {
        if (view == null)
            return;
        Snackbar.make(view, text, Snackbar.LENGTH_SHORT).show();
    }

    public static void show(View view, int resId) {
        if (view == null)
            return;
        Snackbar.make(view, resId, Snackbar.LENGTH_SHORT).show();
    }

}
