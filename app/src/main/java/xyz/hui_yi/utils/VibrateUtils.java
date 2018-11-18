package xyz.hui_yi.utils;

import android.app.Service;
import android.content.Context;
import android.os.Vibrator;

import xyz.hui_yi.utils.PrefUtils.PrefUtils;

import static xyz.hui_yi.constants.Constants.VIBRATE_KEY;

/**
 * Created by LiFen on 2018/2/4.
 * 震动操作工具类
 */

public class VibrateUtils {
    public static void Vibrate(Context context,int time){
        boolean vibrate = PrefUtils.getBoolean(context,VIBRATE_KEY, true);
        if(vibrate){
            Vibrator v;
            v = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
            v.vibrate(time);
        }
    }
}
