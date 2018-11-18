package xyz.hui_yi.app;

import android.app.Application;
import android.support.annotation.NonNull;
import android.util.Log;

import xyz.hui_yi.constants.Constants;
import xyz.hui_yi.face.utils.SharedPreferenceUtil;
import xyz.hui_yi.facesdk.api.CheckAPI;
import xyz.hui_yi.utils.PrefUtils.PrefUtils;
import com.tencent.soter.wrapper.SoterWrapperApi;
import com.tencent.soter.wrapper.wrap_callback.SoterProcessCallback;
import com.tencent.soter.wrapper.wrap_callback.SoterProcessNoExtResult;
import com.tencent.soter.wrapper.wrap_core.SoterProcessErrCode;
import com.tencent.soter.wrapper.wrap_task.InitializeParam;

/**
 * Created by LiFen on 2018/3/14.
 * 程序入口 配置文件中配置
 */

public class App extends Application {
    private static final String TAG = "App";
    @Override
    public void onCreate() {
        super.onCreate();
        initSoterSupport();
        // 模拟获取开通状态
        SharedPreferenceUtil.getInstance().init(getApplicationContext());
        // 初始化eyekey接口 （需在AndroidManifest.xml中添加appid和appkey）
        CheckAPI.init(getApplicationContext());
    }

    private void initSoterSupport() {
        InitializeParam param = new InitializeParam.InitializeParamBuilder()
                .setScenes(Constants.SCENE_VALUE) // 场景值常量，后续使用该常量进行密钥生成或指纹认证
                .build();
        SoterWrapperApi.init(getApplicationContext(), // 场景句柄
                new SoterProcessCallback<SoterProcessNoExtResult>() {

                    @Override
                    public void onResult(@NonNull SoterProcessNoExtResult result) {
                        Log.i(TAG, "onResult: " + result.toString());
                        if(result.errCode != SoterProcessErrCode.ERR_SOTER_NOT_SUPPORTED){//支持Soter
                            PrefUtils.setBoolean(getApplicationContext(),Constants.KEY_SOTER_SUPPORTED,true);
//                            Toast.makeText(getApplicationContext(),"支持",Toast.LENGTH_SHORT).show();
                        }
                    }
                }, // 初始化回调
                param);

    }
}
