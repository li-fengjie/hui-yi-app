package xyz.hui_yi.activity;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.soter.wrapper.SoterWrapperApi;
import com.tencent.soter.wrapper.wrap_callback.SoterProcessAuthenticationResult;
import com.tencent.soter.wrapper.wrap_callback.SoterProcessCallback;
import com.tencent.soter.wrapper.wrap_callback.SoterProcessKeyPreparationResult;
import com.tencent.soter.wrapper.wrap_core.SoterProcessErrCode;
import com.tencent.soter.wrapper.wrap_fingerprint.SoterFingerprintCanceller;
import com.tencent.soter.wrapper.wrap_fingerprint.SoterFingerprintStateCallback;
import com.tencent.soter.wrapper.wrap_task.AuthenticationParam;

import java.io.File;

import xyz.hui_yi.constants.Constants;
import xyz.hui_yi.fingerprint.IOnAuthKeyPrepared;
import xyz.hui_yi.net.UserLoginHttpThread;
import xyz.hui_yi.utils.PrefUtils.PrefUtils;
import xyz.hui_yi.utils.SnackBarUtils.SnackBarUtils;
import xyz.hui_yi.view.CircleImageView;

import static xyz.hui_yi.constants.Constants.ISUSER;
import static xyz.hui_yi.constants.Constants.KEY_IS_FIRST_ENTER;
import static xyz.hui_yi.constants.Constants.KEY_SOTER_SUPPORTED;
import static xyz.hui_yi.constants.Constants.KEY_UPHOTO;
import static xyz.hui_yi.constants.Constants.KEY_USER_ISCHECK;
import static xyz.hui_yi.constants.Constants.KEY_USER_NAME;
import static xyz.hui_yi.constants.Constants.KEY_USER_PASSWORD;
import static xyz.hui_yi.constants.Constants.LOGIN_FAIL;
import static xyz.hui_yi.constants.Constants.NET_ERROR;
import static xyz.hui_yi.constants.Constants.NO_SOTER_SUPPORT;
import static xyz.hui_yi.constants.Constants.SERVER_ERROR;
import static xyz.hui_yi.constants.Constants.UID;
import static xyz.hui_yi.constants.Constants.USER_LOGIN_SUCCESS;


/**
 * Created by LiFen on 2018/1/14.
 * 签到用户登陆界面
 */

public class UserLoginActivity extends Activity {

    private static final String TAG = "UserLoginActivity";
    private EditText et_username;
    private EditText et_password;
    private Dialog mPasswordDialog = null;
    private Dialog mFingerprintDialog = null;
    private ProgressDialog mLoadingDialog = null;
    private View mCustomFingerprintView = null;
    private ImageView mFingerprintErrorImageView;
    private ImageView mFingerprintImageView;

    private TextView mFingerprintStatusHintView = null;
    private SoterFingerprintCanceller mCanceller = null;
    private Animation mFlashAnimation = null;
    private CheckBox cb_ischeck;
    private ImageView iv_fp;
    private TextView tv_adm;
    private ImageView iv_eye;
    private CircleImageView civ_logo;
    private String name;
    private String password;
    private String pid;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case USER_LOGIN_SUCCESS :
                    if (cb_ischeck.isChecked()) {
                        PrefUtils.setString(getApplication(),KEY_USER_NAME,name);
                        PrefUtils.setString(getApplication(),KEY_USER_PASSWORD,password);
                        PrefUtils.setBoolean(getApplication(),KEY_USER_ISCHECK,cb_ischeck.isChecked());
                    } else {
                        PrefUtils.setString(getApplication(),KEY_USER_PASSWORD,"");
                        PrefUtils.setBoolean(getApplication(),KEY_USER_ISCHECK,false);
                    }
                    Intent intent = new Intent(UserLoginActivity.this, MainActivity.class);
                    intent.putExtra(ISUSER, true);
                    intent.putExtra(UID, name);
                    startActivity(intent);
                    Toast.makeText(UserLoginActivity.this, xyz.hui_yi.R.string.text_welcome_back,
                            Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case LOGIN_FAIL :
                    SnackBarUtils.show(iv_fp, xyz.hui_yi.R.string.text_wrong_pwd);
                    et_password.setText("");
                    break;
                case SERVER_ERROR:
                    SnackBarUtils.show(iv_fp, xyz.hui_yi.R.string.text_server_error);

                    //TODO test
                    Intent intent1 = new Intent(UserLoginActivity.this, MainActivity.class);
                    intent1.putExtra(ISUSER, true);
                    intent1.putExtra(UID, name);
                    startActivity(intent1);
                    Toast.makeText(UserLoginActivity.this, xyz.hui_yi.R.string.text_welcome_back,
                            Toast.LENGTH_SHORT).show();
                    finish();
                    //TODO test

                    break;
                case NET_ERROR:
                    SnackBarUtils.show(iv_fp, xyz.hui_yi.R.string.toast_network_error);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setContentView(xyz.hui_yi.R.layout.activity_userlogin);
        initView();
        initPermission();
        prepareData();
        configLogic();
    }

    private void initPermission() {
        if (!getIntent().getBooleanExtra(KEY_IS_FIRST_ENTER,false)
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }
    }

    private void configLogic() {
        tv_adm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserLoginActivity.this, AdmLoginActivity.class);
                startActivity(intent);
            }
        });

        if (!PrefUtils.getBoolean(getApplication(),KEY_SOTER_SUPPORTED, false)) {
            iv_fp.setVisibility(View.GONE);
        }else {
            iv_fp.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Animation animation = AnimationUtils.loadAnimation(UserLoginActivity.this, xyz.hui_yi.R.anim.anim_refresh);
                    iv_fp.setAnimation(animation);
                    iv_fp.startAnimation(animation);
                    fplogin();
                }
            });
        }
        iv_eye.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    //按下
                    et_password.setTransformationMethod(HideReturnsTransformationMethod
                            .getInstance());
                }else if(event.getAction() == MotionEvent.ACTION_UP){
                    //抬起
                    et_password.setTransformationMethod(PasswordTransformationMethod
                        .getInstance());
                }
                return false;
            }
        });
    }

    private void initView() {
        mCustomFingerprintView = LayoutInflater.from(this).inflate(xyz.hui_yi.R.layout.layout_fingerprint, null);
        mFingerprintStatusHintView = (TextView) mCustomFingerprintView.findViewById(xyz.hui_yi.R.id.error_hint_msg);
        mFingerprintErrorImageView = (ImageView) mCustomFingerprintView.findViewById(xyz.hui_yi.R.id.fp_ic_error);
        mFingerprintImageView = (ImageView) mCustomFingerprintView.findViewById(xyz.hui_yi.R.id.fp_ic);
        et_username = (EditText) findViewById(xyz.hui_yi.R.id.et_mtitle);
        et_password = (EditText) findViewById(xyz.hui_yi.R.id.et_admpwd1);
        cb_ischeck = (CheckBox) findViewById(xyz.hui_yi.R.id.cb_ischeck);
        cb_ischeck.setChecked(true);
        tv_adm = (TextView) findViewById(xyz.hui_yi.R.id.textView5);
        civ_logo = (CircleImageView) findViewById(xyz.hui_yi.R.id.civ_logo);
        iv_fp = (ImageView) findViewById(xyz.hui_yi.R.id.iv_fp);
        iv_eye =  (ImageView)findViewById(xyz.hui_yi.R.id.iv_eye);
    }

    public void registered(View v) {//注册
        Intent intent = new Intent(UserLoginActivity.this, UserRegActivity.class);
        startActivity(intent);
    }

    private void fplogin() {//指纹登陆
        name = et_username.getText().toString().trim();
        password = et_password.getText().toString().trim();

        if (!PrefUtils.getBoolean(getApplication(),KEY_SOTER_SUPPORTED, false)) {
            SnackBarUtils.show(iv_fp, xyz.hui_yi.R.string.text_not_supported_soter);
            return;
        } else {
            if (TextUtils.isEmpty(name)) {
                SnackBarUtils.show(et_username, xyz.hui_yi.R.string.text_name_not_empty);
            } else {
                PrefUtils.setString(getApplication(),KEY_USER_NAME,name);
                PrefUtils.setBoolean(getApplication(),KEY_USER_ISCHECK,cb_ischeck.isChecked());
                doUseFingerprintPayment();
            }
        }
    }

    public void login(View v) {//密码登陆
        name = et_username.getText().toString().trim();
        password = et_password.getText().toString().trim();

        if (!TextUtils.isEmpty(password) && !TextUtils.isEmpty(name)) {
            //TODO 登陆 联网操作 uid pwd pid(null)
            String url = getResources().getString(xyz.hui_yi.R.string.URL);
            url = url + "/public/api/User/login";
            new UserLoginHttpThread(mHandler,url, name, password,"").start();
        } else {
            SnackBarUtils.show(iv_fp, xyz.hui_yi.R.string.text_info_not_empty);
        }
    }

    private void prepareData() {
        String n = PrefUtils.getString(getApplication(),KEY_USER_NAME,"");
        String pwd = PrefUtils.getString(getApplication(),KEY_USER_PASSWORD, "");
        boolean ischeck = PrefUtils.getBoolean(getApplication(),KEY_USER_ISCHECK, true);
        et_username.setText(n);
        et_password.setText(pwd);
        cb_ischeck.setChecked(ischeck);
        mFlashAnimation = AnimationUtils.loadAnimation(this, xyz.hui_yi.R.anim.anim_flash);
        //civ_logo 头像设置
        String uphotoPath = PrefUtils.getString(getApplication(), KEY_UPHOTO, "");
        Log.i(TAG, "prepareData: " + getCacheDir());
        File file = new File(getCacheDir(),uphotoPath);
        Log.i(TAG, "prepareData: uppath" + uphotoPath);
        Log.i(TAG, "prepareData: " + file.length());
        if (file.exists() && file.length() > 0 && !uphotoPath.equals("")) {
            Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
            Log.i(TAG, "prepareData: " + file.getAbsolutePath());
            civ_logo.setImageBitmap(bitmap);
        }else civ_logo.setImageResource(xyz.hui_yi.R.drawable.image_uphoto_example);
    }

    private void doUseFingerprintPayment() {
        Log.i(TAG, "soterdemo: 用户请求使用指纹登陆");
        startFingerprintAuthentication(new SoterProcessCallback<SoterProcessAuthenticationResult>() {
            @Override
            public void onResult(@NonNull SoterProcessAuthenticationResult result) {
//                Log.d(TAG, "soterdemo: 使用指纹登陆结果:\n %s \n, 签名数据是:\n %s\n" + result.toString() + result.getExtData() != null ? result.getExtData().toString() : null);
                dismissLoading();
                //TODO 限制那一根手指， 登陆成功条件 联网操作获取pid = result.getExtData().getFid()
                if (result.isSuccess()) {// 验证条件
                    pid = result.getExtData().getFid();
                    Log.i(TAG, "PID: " + pid);
                    String url = getResources().getString(xyz.hui_yi.R.string.URL);
                    url = url + "/public/api/User/login";
                    new UserLoginHttpThread(mHandler,url, name, "",pid).start();
                } else {
                    // 先判断是否是指纹密钥失效。如果指纹失效，则重新生成并上传authkey，然后直接使用人脸登陆
                        if (result.errCode == SoterProcessErrCode.ERR_AUTHKEY_NOT_FOUND
                            || result.errCode == SoterProcessErrCode.ERR_AUTHKEY_ALREADY_EXPIRED
                                || result.errCode == SoterProcessErrCode.ERR_ASK_NOT_EXIST) {
//                        Log.w(TAG, "soterdemo: 授权密钥过期或找不到密钥。 再生和上传");
                        startPrepareAuthKeyAndAuthenticate();
                    } else if (result.errCode == SoterProcessErrCode.ERR_USER_CANCELLED) {
//                        Log.i(TAG, "soterdemo: 用户取消了认证");
                    } else if (result.errCode == SoterProcessErrCode.ERR_FINGERPRINT_LOCKED) {
//                        Log.i(TAG, "soterdemo: 指纹传感器由于尝试次数过多而被锁定。 回落到人脸登陆");
                        mFingerprintImageView.setImageResource(xyz.hui_yi.R.drawable.ic_fingerprint_error);
                        SnackBarUtils.show(et_password, xyz.hui_yi.R.string.text_request_locking);
                    } else if(result.errCode == SoterProcessErrCode.ERR_NO_FINGERPRINT_ENROLLED){
                            //TODO 引导进入录入指纹
                        SnackBarUtils.show(et_password, xyz.hui_yi.R.string.text_system_not_fp);
                    }
                    else {
//                        Log.w(TAG, "soterdemo: doUseFingerprintPayment中的未知错误: %d" +  result.errCode);
                    }
                }
            }
        }, getString(xyz.hui_yi.R.string.text_login_fp));
    }

    private void startPrepareAuthKeyAndAuthenticate() {
        doPrepareAuthKey(new IOnAuthKeyPrepared() {
            @Override
            public void onResult(String pwdDigestUsed, boolean isSuccess) {
                if (isSuccess) {
                    startFingerprintAuthentication(new SoterProcessCallback<SoterProcessAuthenticationResult>() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onResult(@NonNull SoterProcessAuthenticationResult result) {
//                            Log.i(TAG, "soterdemo: 打开完成: result:\n %s, \n签名数据 : \n%s\n" + result.toString() +
//                                    result.getExtData() != null ? result.getExtData().toString() : null);
                            if(result.errCode == SoterProcessErrCode.ERR_NO_FINGERPRINT_ENROLLED){
                                dismissLoading();
                                return;
                            }
                            if (result.isSuccess()) {
                                pid = result.getExtData().getFid().toString();
                                Log.i(TAG, "PID: " + pid);
                                String url = getResources().getString(xyz.hui_yi.R.string.URL);
                                url = url + "/public/api/User/login";
                                new UserLoginHttpThread(mHandler,url, name, "",pid).start();
                                dismissLoading();
                            } else {

                            }
                        }
                    }, getString(xyz.hui_yi.R.string.text_login_fp));
                } else {
                    Log.w(TAG, "soterdemo: 生成授权密钥失败！");
                    dismissLoading();
                    Toast.makeText(getApplicationContext(), getString(xyz.hui_yi.R.string.app_auth_key_prepare_failed), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void doPrepareAuthKey(final IOnAuthKeyPrepared onAuthKeyPreparedCallback) {
        showLoading(getString(xyz.hui_yi.R.string.app_loading_preparing_open_keys));
        prepareAuthKey("", onAuthKeyPreparedCallback);
    }

    private void prepareAuthKey(final String pwdDigest, final IOnAuthKeyPrepared callback) {
        SoterWrapperApi.prepareAuthKey(new SoterProcessCallback<SoterProcessKeyPreparationResult>() {
            @Override
            public void onResult(@NonNull SoterProcessKeyPreparationResult result) {
//                Toast.makeText(getApplicationContext(),result.toString(),Toast.LENGTH_SHORT).show();
//                Log.i(TAG, "soterdemo: 准备结果:\n %s\n, 授权关键结果:\n %s\n" + result +  result.getExtData() != null ? result.getExtData().toString() : null);
                if (result.errCode == SoterProcessErrCode.ERR_OK) {
                    if (callback != null) {
                        callback.onResult(pwdDigest, true);
                    }
                } else {
                    if(result.errCode == SoterProcessErrCode.ERR_NO_FINGERPRINT_ENROLLED){
                        SnackBarUtils.show(et_username, xyz.hui_yi.R.string.text_system_not_fp);
                    }
                    if (result.errCode == 2){
                        PrefUtils.setBoolean(getApplication(),KEY_SOTER_SUPPORTED,false);
                        Message message = new Message();
                        message.what = NO_SOTER_SUPPORT;
                        mHandler.sendMessage(message);
                    }
                    if (callback != null) {
                        callback.onResult(pwdDigest, false);
                    }
                }
            }
        }, false, true, Constants.SCENE_VALUE, null, null);
    }

    private void startFingerprintAuthentication(SoterProcessCallback<SoterProcessAuthenticationResult> processCallback,
                                                final String title) {
//        Log.i(TAG, "soterdemo: 开始认证: title: %s" + title);
        dismissCurrentDialog();
        if (mCanceller != null) {
            Log.w(TAG, "soterdemo: 最后一个消除器不是null。 不应该发生，因为我们将在每次完成该过程时将其设置为空");
            mCanceller = null;
        }
        mCanceller = new SoterFingerprintCanceller();
        // 认证逻辑部分
        showLoading(getString(xyz.hui_yi.R.string.app_request_challenge));
        // Prepare authentication parameters
        AuthenticationParam param = new AuthenticationParam.AuthenticationParamBuilder() // 通过Builder来构建认证请求
                .setScene(Constants.SCENE_VALUE) // 指定需要认证的场景。必须在init中初始化。必填
                .setContext(this) // 指定当前上下文。必填。
                .setFingerprintCanceller(mCanceller) // 指定当前用于控制指纹取消的控制器。当因为用户退出界面或者进行其他可能引起取消的操作时，需要开发者通过该控制器取消指纹授权。建议必填。
                .setPrefilledChallenge(Constants.STR_CHALLENGE)
                .setSoterFingerprintStateCallback(new SoterFingerprintStateCallback() { // 指纹回调仅仅用来更新UI相关，不建议在指纹回调中进行任何业务操作。选填。

                    // 指纹回调仅仅用来更新UI相关，不建议在指纹回调中进行任何业务操作
                    // Fingerprint state callbacks are only used for updating UI. Any logic operation is not welcomed.
                    @Override
                    public void onStartAuthentication() {
                        Log.d(TAG, "soterdemo: 开始认证。 解除加载");
                        dismissLoading();
                        showFingerprintDialog(title);
                    }

                    @Override
                    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                        Log.w(TAG, "soterdemo: onAuthenticationHelp: %d, %s"+ helpCode+helpString);
                        // 由于厂商实现不同，不建议在onAuthenticationHelp中做任何操作。
                    }

                    @Override
                    public void onAuthenticationSucceed() {
                        Log.d(TAG, "soterdemo: onAuthenticationSucceed");
                        mCanceller = null;
                        // 可以在这里做相应的UI操作
                        showLoading(getString(xyz.hui_yi.R.string.app_verifying));
                        dismissCurrentDialog();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        Log.w(TAG, "soterdemo: onAuthenticationFailed once:");
                        setFingerprintHintMsg(getString(xyz.hui_yi.R.string.fingerprint_normal_hint), true);
                    }

                    @Override
                    public void onAuthenticationCancelled() {
                        Log.d(TAG, "soterdemo: user cancelled authentication");
                        mCanceller = null;
                        dismissCurrentDialog();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errorString) {
                        Log.w(TAG, "soterdemo: onAuthenticationError: %d, %s" + errorCode + errorString);
                        mCanceller = null;
                        Toast.makeText(UserLoginActivity.this, errorString, Toast.LENGTH_LONG).show();
                        dismissCurrentDialog();
                    }
                }).build();
        SoterWrapperApi.requestAuthorizeAndSign(processCallback, param);
    }

    private void showFingerprintDialog(String title) {
        if (mFingerprintDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(UserLoginActivity.this).setTitle(title).setCancelable(true)
                    .setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            cancelFingerprintAuthentication();
                            dismissCurrentDialog();
                        }
                    }).setNegativeButton(getString(xyz.hui_yi.R.string.app_cancel), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancelFingerprintAuthentication();
                            dismissCurrentDialog();
                        }
                    }).setView(mCustomFingerprintView);
            mFingerprintDialog = builder.create();
        } else {
            setFingerprintHintMsg("", false);
            mFingerprintDialog.setTitle(title);
        }
        mFingerprintDialog.show();
    }

    private void setFingerprintHintMsg(String msg, boolean isFlash) {
        if (mCustomFingerprintView != null) {
            mFingerprintStatusHintView.setText(msg);
            if (msg.equals(getString(xyz.hui_yi.R.string.fingerprint_normal_hint))) {
                mFingerprintErrorImageView.setImageResource(xyz.hui_yi.R.drawable.ic_fingerprint_error);
            } else {
                mFingerprintErrorImageView.setImageResource(xyz.hui_yi.R.mipmap.ic_fp_40px);
            }
            if (isFlash) {
                mFingerprintStatusHintView.startAnimation(mFlashAnimation);
                mFingerprintErrorImageView.setAnimation(mFlashAnimation);
                mFingerprintErrorImageView.startAnimation(mFlashAnimation);
            }
        }
    }

    private void dismissCurrentDialog() {
        if (mPasswordDialog != null && mPasswordDialog.isShowing()) {
            mPasswordDialog.dismiss();
        }
        if (mFingerprintDialog != null && mFingerprintDialog.isShowing()) {
            mFingerprintDialog.dismiss();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 确保在onPause的时候结束指纹监听，以免影响其他模块以及应用
        cancelFingerprintAuthentication();
        // 建议在onPause的时候结束掉SOTER相关事件。当然，也可以选择自己管理，但是会更加复杂
        SoterWrapperApi.tryStopAllSoterTask();
        dismissCurrentDialog();
        dismissLoading();
    }

    private void cancelFingerprintAuthentication() {
        if (mCanceller != null) {
            mCanceller.asyncCancelFingerprintAuthentication();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void showLoading(String wording) {
        if (mLoadingDialog == null) {
            mLoadingDialog = ProgressDialog.show(this, "", wording, true, false, null);
        } else if (!mLoadingDialog.isShowing()) {
            mLoadingDialog.setMessage(wording);
            mLoadingDialog.show();
        } else {
            Log.d(TAG, "soterdemo: 已经显示。 只改变标题");
            mLoadingDialog.setMessage(wording);
        }
    }

    private void dismissLoading() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }
}
