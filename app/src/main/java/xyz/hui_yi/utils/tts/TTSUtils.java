package xyz.hui_yi.utils.tts;

import android.content.Context;
import android.media.AudioManager;
import android.os.Handler;
import android.util.Log;

import com.baidu.tts.auth.AuthInfo;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by LiFen on 2018/2/3.
 * TTS 文本转语音工具类
 */

public class TTSUtils {
    private static final String TAG = "TTSUtils";
    // ================选择TtsMode.ONLINE  不需要设置以下参数; 选择TtsMode.MIX 需要设置下面2个离线资源文件的路径
    private static final String TEMP_DIR = "/sdcard/baiduTTS"; // 重要！请手动将assets目录下的3个dat 文件复制到该目录
    // 请确保该PATH下有这个文件
    private static final String TEXT_FILENAME = TEMP_DIR + "/" + "bd_etts_text.dat";
    // 请确保该PATH下有这个文件 ，m15是离线男声
    private static final String MODEL_FILENAME =
            TEMP_DIR + "/" + "bd_etts_common_speech_m15_mand_eng_high_am-mix_v3.0.0_20170505.dat";

    static TtsMode ttsMode = TtsMode.ONLINE;

    static String appId = "10250719";
    static String appKey = "bUvzFxRyelDpAVDs7PAUGxjC";
    static String secretKey = "eb664a8bfbaddf0c8acba65a9493e44b";

    static SpeechSynthesizer mSpeechSynthesizer;

    public static void asnyncInitTTs(final Context cotext, final Handler mainHandler) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isMix = ttsMode.equals(TtsMode.MIX);
                boolean isSuccess;
                if (isMix) {
                    // 检查2个离线资源是否可读
                    isSuccess = checkOfflineResources();
                    if (!isSuccess) {
                        return;
                    } else {
                        Log.i(TAG, "run: 检查2个离线资源读取失败");
                    }
                }
                SpeechSynthesizerListener listener = new UiMessageListener(mainHandler);
                // 1. 获取实例
                mSpeechSynthesizer = SpeechSynthesizer.getInstance();
                mSpeechSynthesizer.setContext(cotext);

                // 2. 设置listener
                mSpeechSynthesizer.setSpeechSynthesizerListener(listener);

                // 3. 设置appId，appKey.secretKey
                int result = mSpeechSynthesizer.setAppId(appId);
                checkResult(result, "setAppId");
                result = mSpeechSynthesizer.setApiKey(appKey, secretKey);
                checkResult(result, "setApiKey");

                // 4. 支持离线的话，需要设置离线模型
                if (isMix) {
                    // 检查离线授权文件是否下载成功，离线授权文件联网时SDK自动下载管理，有效期3年，3年后的最后一个月自动更新。
                    isSuccess = checkAuth();
                    if (!isSuccess) {
                        return;
                    }
                    // 文本模型文件路径 (离线引擎使用)， 注意TEXT_FILENAME必须存在并且可读
                    mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, TEXT_FILENAME);
                    // 声学模型文件路径 (离线引擎使用)， 注意TEXT_FILENAME必须存在并且可读
                    mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, MODEL_FILENAME);
                }
                // 5. 以下setParam 参数选填。不填写则默认值生效
                // 设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
                mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
                // 设置合成的音量，0-9 ，默认 5
                mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "7");
                // 设置合成的语速，0-9 ，默认 5
                mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "4");
                // 设置合成的语调，0-9 ，默认 5
                mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "5");

                mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
                // 该参数设置为TtsMode.MIX生效。即纯在线模式不生效。
                // MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
                // MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
                // MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
                // MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线

                mSpeechSynthesizer.setAudioStreamType(AudioManager.MODE_IN_CALL);

                // x. 额外 ： 自动so文件是否复制正确及上面设置的参数
                Map<String, String> params = new HashMap<>();
                // 复制下上面的 mSpeechSynthesizer.setParam参数
                // 上线时请删除AutoCheck的调用
                if (isMix) {
                    params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, TEXT_FILENAME);
                    params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, MODEL_FILENAME);
                }

                // 6. 初始化
                result = mSpeechSynthesizer.initTts(ttsMode);
                checkResult(result, "initTts");

            }
        }).start();
    }

    /**
     * 检查 TEXT_FILENAME, MODEL_FILENAME 这2个文件是否存在，不存在请自行从assets目录里手动复制
     *
     * @return
     */
    public static boolean checkOfflineResources() {
        String[] filenames = {TEXT_FILENAME, MODEL_FILENAME};
        for (String path : filenames) {
            File f = new File(path);
            if (!f.canRead()) {
                Log.i(TAG, "checkOfflineResources: [ERROR] 文件不存在或者不可读取，请从assets目录复制同名文件到：" + path);
                Log.i(TAG, "checkOfflineResources: [ERROR] TTS初始化失败！！！");
                return false;
            }
        }
        return true;
    }

    public static void checkResult(int result, String method) {
        if (result != 0) {
            Log.i(TAG, "checkResult: error code :" + result + " method:" + method + ", 错误码文档:http://yuyin.baidu.com/docs/tts/122 ");
        }
    }

    /**
     * 检查appId ak sk 是否填写正确，另外检查官网应用内设置的包名是否与运行时的包名一致。本demo的包名定义在build.gradle文件中
     *
     * @return
     */
    public static boolean checkAuth() {
        AuthInfo authInfo = mSpeechSynthesizer.auth(ttsMode);
        if (!authInfo.isSuccess()) {
            // 离线授权需要网站上的应用填写包名。本demo的包名是com.baidu.tts.sample，定义在build.gradle中
            String errorMsg = authInfo.getTtsError().getDetailMessage();
            Log.i(TAG, "checkAuth: error】鉴权失败 errorMsg=" + errorMsg);
            return false;
        } else {
            Log.i(TAG, "checkAuth: 验证通过，离线正式授权文件存在。");
            return true;
        }
    }

    public static void speak(Context context,String text) {
        /* 以下参数每次合成时都可以修改
         *  mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
         *  设置在线发声音人： 0 普通女声（默认） 1 普通男声 2 特别男声 3 情感男声<度逍遥> 4 情感儿童声<度丫丫>
         *  mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "5"); 设置合成的音量，0-9 ，默认 5
         *  mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "5"); 设置合成的语速，0-9 ，默认 5
         *  mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "5"); 设置合成的语调，0-9 ，默认 5
         *
         *  mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);
         *  MIX_MODE_DEFAULT 默认 ，wifi状态下使用在线，非wifi离线。在线状态下，请求超时6s自动转离线
         *  MIX_MODE_HIGH_SPEED_SYNTHESIZE_WIFI wifi状态下使用在线，非wifi离线。在线状态下， 请求超时1.2s自动转离线
         *  MIX_MODE_HIGH_SPEED_NETWORK ， 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
         *  MIX_MODE_HIGH_SPEED_SYNTHESIZE, 2G 3G 4G wifi状态下使用在线，其它状态离线。在线状态下，请求超时1.2s自动转离线
         */
        if (mSpeechSynthesizer == null) {
            Log.i(TAG, "speak: [ERROR], 初始化失败");
            return;
        }
        int result = mSpeechSynthesizer.speak(text);
        Log.i(TAG, "speak: 合成并播放 按钮已经点击");
        checkResult(result, "speak");
    }

    public static void cancelTTS() {
        if (mSpeechSynthesizer != null) {
            mSpeechSynthesizer.stop();
            mSpeechSynthesizer.release();
            mSpeechSynthesizer = null;
            Log.i(TAG, "onDestroy: tts释放资源成功");
        }
    }
}
