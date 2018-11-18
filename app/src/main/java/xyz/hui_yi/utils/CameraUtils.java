package xyz.hui_yi.utils;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;

/**
 * Created by LiFen on 2018/4/9.
 */

public class CameraUtils {

        /**
         * 测试当前摄像头能否被使用
         *
         * @return
         */
        public static boolean isCameraCanUse() {
            boolean canUse = true;
            Camera mCamera = null;
            try {
                mCamera = Camera.open(0);
                mCamera.setDisplayOrientation(90);
            } catch (Exception e) {
                canUse = false;
            }
            if (canUse) {
                mCamera.release();
                mCamera = null;
            }
            //Timber.v("isCameraCanuse="+canUse);
            return canUse;
    }


    /**
     * 相机权限设置
     * 跳转至设置页面
     */
    public static void getAppDetailSettingIntent(Context context) {
        Intent localIntent = new Intent();
        if (Build.VERSION.SDK_INT >= 9) {
            localIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
            localIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
        } else if (Build.VERSION.SDK_INT <= 8) {
            localIntent.setAction(Intent.ACTION_VIEW);
            localIntent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails");
            localIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
        }
        context.startActivity(localIntent);
    }
}
