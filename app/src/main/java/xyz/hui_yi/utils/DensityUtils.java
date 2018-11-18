package xyz.hui_yi.utils;

import android.content.Context;

/**
 * Created by lyw on 2017/6/7.
 * 引导页圆点工具类
 */

public class DensityUtils {
    public static int dip2px(float dip, Context ctx) {
        float density = ctx.getResources().getDisplayMetrics().density;
        int px = (int) (dip * density + 0.5f);// 4.9->4, 4.1->4, 四舍五入
        return px;
    }

    public static float px2dip(int px, Context ctx) {
        float density = ctx.getResources().getDisplayMetrics().density;
        float dp = px / density;
        return dp;
    }
}
