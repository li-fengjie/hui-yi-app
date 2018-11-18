package xyz.hui_yi.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import xyz.hui_yi.bean.UserRegBean;
import xyz.hui_yi.constants.Constants;
import xyz.hui_yi.face.utils.CustomUtil;
import xyz.hui_yi.face.utils.Logs;
import xyz.hui_yi.face.utils.StringUtils;
import xyz.hui_yi.face.utils.ToastUtils;
import xyz.hui_yi.facesdk.api.CheckAPI;
import xyz.hui_yi.facesdk.conn.Constant;
import xyz.hui_yi.facesdk.entity.PeopleGet;
import xyz.hui_yi.fingerprint.IOnAuthKeyPrepared;
import xyz.hui_yi.net.UserRegHttpThread;
import xyz.hui_yi.utils.PrefUtils.PrefUtils;
import xyz.hui_yi.utils.SnackBarUtils.SnackBarUtils;
import com.tencent.soter.wrapper.SoterWrapperApi;
import com.tencent.soter.wrapper.wrap_callback.SoterProcessAuthenticationResult;
import com.tencent.soter.wrapper.wrap_callback.SoterProcessCallback;
import com.tencent.soter.wrapper.wrap_callback.SoterProcessKeyPreparationResult;
import com.tencent.soter.wrapper.wrap_core.SoterProcessErrCode;
import com.tencent.soter.wrapper.wrap_fingerprint.SoterFingerprintCanceller;
import com.tencent.soter.wrapper.wrap_fingerprint.SoterFingerprintStateCallback;
import com.tencent.soter.wrapper.wrap_net.IWrapUploadSignature;
import com.tencent.soter.wrapper.wrap_task.AuthenticationParam;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by LiFen on 2018/1/24.
 * 签到用户注册界面
 */

public class UserRegActivity extends AppCompatActivity {
    private static final String TAG = "UserRegActivity";
    private Dialog mPasswordDialog = null;
    private Dialog mFingerprintDialog = null;
    private ProgressDialog mLoadingDialog = null;
    private SoterFingerprintCanceller mCanceller = null;
    private View mCustomFingerprintView = null;
    private TextView mFingerprintStatusHintView = null;
    private Animation mFlashAnimation = null;
    private ImageView addFingerprint;
    private int mCameraId = 1;
    private EditText et_uid;
    private ProgressDialog mProgressDialog;
    private Button btn_userreg;
    private UserRegBean userRegBean;
    private EditText et_upwd1;
    private EditText et_upwd2;
    private EditText et_uname;
    private EditText et_ucompany;
    private ImageView mFingerprintErrorImageView;
    private TextView tv_fingerprint;
    private EditText et_uphone;
    private boolean isSupportSoter;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case Constants.REG_SUCCESS:
                    Toast.makeText(getApplicationContext(),getString(xyz.hui_yi.R.string.text_reg_success),Toast.LENGTH_SHORT).show();
                    peopleGet();
                    finish();
                    break;
                case Constants.REG_BNAME_NOT_EXISTS:
                    SnackBarUtils.show(et_ucompany, xyz.hui_yi.R.string.text_company_not_exist);
                    et_ucompany.setText("");
                    break;
                case Constants.REG_UID_EXISTS:
                    SnackBarUtils.show(et_uid, xyz.hui_yi.R.string.text_user_exist);
                    break;
                case Constants.SERVER_ERROR:
                    SnackBarUtils.show(et_uid, xyz.hui_yi.R.string.text_server_error);
                    break;
                case Constants.NO_SOTER_SUPPORT:
                    tv_fingerprint.setText(xyz.hui_yi.R.string.text_add_fp_success);
                    addFingerprint.setVisibility(View.GONE);
                    tv_fingerprint.setVisibility(View.GONE);
                    userRegBean.setPid("");
                    break;
                case Constants.NET_ERROR:
                    SnackBarUtils.show(et_uid, xyz.hui_yi.R.string.toast_network_error);
                    break;
            }
        }
    };


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(xyz.hui_yi.R.layout.activity_userreg);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        initView();
        prepareData();
        initPermission();
        configLogic();
    }

    private void configLogic() {
        addFingerprint.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                Animation animation = AnimationUtils.loadAnimation(UserRegActivity.this, xyz.hui_yi.R.anim.anim_refresh);
                addFingerprint.setAnimation(animation);
                addFingerprint.startAnimation(animation);
                doOpenFingerprintPayment();
            }
        });

        btn_userreg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.i(TAG, "onClick: "+et_uid.getText().toString());
                if(et_uid.getText().toString().equals("")){
                    SnackBarUtils.show(et_uid, xyz.hui_yi.R.string.text_name_not_empty);
                    return;
                }
                if(!et_upwd1.getText().toString().equals(et_upwd2.getText().toString())){
                    SnackBarUtils.show(et_upwd1, xyz.hui_yi.R.string.toast_pwd_notsame);
                    return;
                }
                if (et_uname.getText().toString().equals("") || et_ucompany.getText().toString().equals("")
                        || et_uphone.getText().toString().equals("")) {
                    SnackBarUtils.show(et_uname, xyz.hui_yi.R.string.text_info_not_empty);
                    return;
                }
                if(tv_fingerprint.getText().toString().equals(getString(xyz.hui_yi.R.string.text_add_fp))){
                    Animation animation = AnimationUtils.loadAnimation(UserRegActivity.this, xyz.hui_yi.R.anim.anim_refresh);
                    addFingerprint.setAnimation(animation);
                    addFingerprint.startAnimation(animation);
                    SnackBarUtils.show(et_upwd1, xyz.hui_yi.R.string.text_no_face_fp);
                    return;
                }
                userRegBean.setUid(et_uid.getText().toString());
                userRegBean.setPwd(et_upwd1.getText().toString());
                userRegBean.setCompany(et_ucompany.getText().toString());
                userRegBean.setName(et_uname.getText().toString());
                userRegBean.setPhone(et_uphone.getText().toString());

                //TODO 联网注册
                String url = getResources().getString(xyz.hui_yi.R.string.URL);
                url = url + "/public/api/User/register";
                new UserRegHttpThread(mHandler,url,userRegBean).start();
            }
        });
    }

    private void peopleGet() {
        if (StringUtils.isBlank(userRegBean.getUid())) {
            ToastUtils.show(this, xyz.hui_yi.R.string.text_name_not_empty);
            return;
        }

        mProgressDialog.setMessage(CustomUtil.getString(getApplicationContext(), xyz.hui_yi.R.string.text_test));
        mProgressDialog.show();
        Call<PeopleGet> call = CheckAPI.peopleGet(userRegBean.getUid());
        call.enqueue(new Callback<PeopleGet>() {

            public void onFinish() {
                if(mProgressDialog != null && mProgressDialog.isShowing()){
                    mProgressDialog.dismiss();
                }
            }

            @Override
            public void onResponse(Call<PeopleGet> call, Response<PeopleGet> response) {
                onFinish();
                handleCheckData(response.body());
            }

            @Override
            public void onFailure(Call<PeopleGet> call, Throwable t) {
                onFinish();
                ToastUtils.show(getApplicationContext(), xyz.hui_yi.R.string.text_server_error);
            }
        });
    }

    private void handleCheckData(PeopleGet data) {
        Logs.d(TAG, data == null ? "认证失败" : data.toString());
        handleRegister(data);
    }

    private void handleRegister(PeopleGet data) {
        if (data == null) {
            SnackBarUtils.show(et_uid, xyz.hui_yi.R.string.text_verify_failed);
//            Log.i(TAG, "handleRegister: " + "请检查apiid key");
            return;
        }
        if (data.getRes_code() != null && Constant.RES_CODE_1025.equals(data.getRes_code())) {
            startCaptureActivity(false);
        } else if (StringUtils.isEquals(data.getRes_code(), Constant.RES_CODE_0000) && data.getFace_count() > 0) {
            SnackBarUtils.show(et_uid, xyz.hui_yi.R.string.text_user_exist);
        } else if (StringUtils.isEquals(data.getRes_code(), Constant.RES_CODE_0000) && data.getFace_count() == 0) {
            startCaptureActivity(true);
        } else {
            SnackBarUtils.show(et_uid, xyz.hui_yi.R.string.text_verify_failed);
        }
    }

    private void startCaptureActivity(boolean created) {
        Intent intent = new Intent(this, FaceRegActivity.class);
        intent.putExtra(FaceRegActivity.ARG_CAMERA_ID, mCameraId);
        intent.putExtra(FaceRegActivity.ARG_UID, userRegBean.getUid());
        intent.putExtra(FaceRegActivity.ARG_BNAME,userRegBean.getCompany());
        intent.putExtra(FaceRegActivity.ARG_IS_CREATED, created);
        startActivity(intent);
    }
    
    private void initView(){
        mCustomFingerprintView = LayoutInflater.from(this).inflate(xyz.hui_yi.R.layout.layout_fingerprint, null);
        mFingerprintErrorImageView = (ImageView) mCustomFingerprintView.findViewById(xyz.hui_yi.R.id.fp_ic_error);
        mFingerprintStatusHintView = (TextView) mCustomFingerprintView.findViewById(xyz.hui_yi.R.id.error_hint_msg);
        addFingerprint = (ImageView) findViewById(xyz.hui_yi.R.id.addFingerprint);
        et_uid = (EditText) findViewById(xyz.hui_yi.R.id.et_uid);
        et_upwd1 = (EditText) findViewById(xyz.hui_yi.R.id.et_upwd1);
        et_upwd2 = (EditText) findViewById(xyz.hui_yi.R.id.et_admpwd2);
        et_uname = (EditText) findViewById(xyz.hui_yi.R.id.et_uname);
        et_ucompany = (EditText) findViewById(xyz.hui_yi.R.id.et_ucompany);
        tv_fingerprint = (TextView) findViewById(xyz.hui_yi.R.id.tv_fingerprint);
        btn_userreg = (Button) findViewById(xyz.hui_yi.R.id.btn_userreg);
        et_uphone = (EditText) findViewById(xyz.hui_yi.R.id.et_uphone);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void prepareData() {
        userRegBean = new UserRegBean();
        mFlashAnimation = AnimationUtils.loadAnimation(this, xyz.hui_yi.R.anim.anim_flash);
        mProgressDialog = new ProgressDialog(this);
        isSupportSoter = PrefUtils.getBoolean(getApplication(), Constants.KEY_SOTER_SUPPORTED,false);
        if(!isSupportSoter){
            addFingerprint.setVisibility(View.GONE);
            tv_fingerprint.setVisibility(View.GONE);
            tv_fingerprint.setText(getString(xyz.hui_yi.R.string.text_add_fp_success));
            userRegBean.setPid("");
        }
    }

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String[] permissions = {
                Manifest.permission.CAMERA
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.
            }
        }
        String[] tmpList = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }
    }

    private void doOpenFingerprintPayment() {
        doPrepareAuthKey(new IOnAuthKeyPrepared() {
            @Override
            public void onResult(String pwdDigestUsed, boolean isSuccess) {
                if (isSuccess) {
                    startFingerprintAuthentication(new SoterProcessCallback<SoterProcessAuthenticationResult>() {
                        @RequiresApi(api = Build.VERSION_CODES.M)
                        @Override
                        public void onResult(@NonNull SoterProcessAuthenticationResult result) {
//                            Log.i(TAG, "soterdemo: 打开完成: result:\n %s, \n签名数据 : \n%s\n" + result.toString()+ result.getExtData() != null ? result.getExtData().toString() : null);
                            if(result.errCode == SoterProcessErrCode.ERR_NO_FINGERPRINT_ENROLLED){
                                SnackBarUtils.show(addFingerprint, xyz.hui_yi.R.string.text_system_not_fp);
                                dismissLoading();
                                return;
                            }
                            if (result.isSuccess()) {
                                //TODO 指纹签到所用手指  得到 Fid
                                userRegBean.setPid(result.getExtData().getFid().toString());
                                Toast.makeText(UserRegActivity.this, getString(xyz.hui_yi.R.string.text_add_fp_success), Toast.LENGTH_SHORT).show();
                                dismissLoading();
                                tv_fingerprint.setText(getString(xyz.hui_yi.R.string.text_add_fp_success));
//                                tv_fingerprint.setTextColor(getColor(R.color.colorPrimary));
                                addFingerprint.setEnabled(false);
                            } else {
                                Toast.makeText(UserRegActivity.this, String.format("打开失败, reason: %s", result.toString()), Toast.LENGTH_LONG).show();
                            }
                        }
                    }, getString(xyz.hui_yi.R.string.text_login_fp), null);
                } else {
                    dismissLoading();
                    Toast.makeText(UserRegActivity.this, getString(xyz.hui_yi.R.string.app_auth_key_prepare_failed), Toast.LENGTH_LONG).show();
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
//                Log.i(TAG, "soterdemo: 准备结果:\n %s\n, 授权关键结果:\n %s\n"+ result + result.getExtData() != null ? result.getExtData().toString() : null);
                if (result.errCode == SoterProcessErrCode.ERR_OK) {
                    if (callback != null) {
                        callback.onResult(pwdDigest, true);
                    }
                } else {
                    if(result.errCode == SoterProcessErrCode.ERR_NO_FINGERPRINT_ENROLLED){
                        SnackBarUtils.show(addFingerprint, xyz.hui_yi.R.string.text_system_not_fp);
                    }
                    if (result.errCode == 2){
                        PrefUtils.setBoolean(getApplication(), Constants.KEY_SOTER_SUPPORTED,false);
                        Message message = new Message();
                        message.what = Constants.NO_SOTER_SUPPORT;
                        mHandler.sendMessage(message);
                        return;
                    }
                    if (callback != null) {
                        callback.onResult(pwdDigest, false);
                    }
                }
            }
        }, false, true, Constants.SCENE_VALUE, null, null);
    }

    private void startFingerprintAuthentication(SoterProcessCallback<SoterProcessAuthenticationResult> processCallback,
                                                final String title, IWrapUploadSignature uploadSignatureWrapper) {
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
                .setPrefilledChallenge(Constants.STR_CHALLENGE) // 如果之前已经通过其他方式获取了挑战因子，则设置此字段。如果设置了该字段，则忽略获取挑战因子网络封装结构体的设置。如果两个方法都没有调用，则会引起错误。
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
                        Log.w(TAG, "soterdemo: onAuthenticationHelp: %d, %s" + helpCode + helpString);
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
                        Toast.makeText(UserRegActivity.this, errorString, Toast.LENGTH_LONG).show();
                        dismissCurrentDialog();
                    }
                }).build();
        SoterWrapperApi.requestAuthorizeAndSign(processCallback, param);
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

    private void showFingerprintDialog(String title) {
        if (mFingerprintDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(UserRegActivity.this).setTitle(title).setCancelable(true)
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

    private void cancelFingerprintAuthentication() {
        if (mCanceller != null) {
            mCanceller.asyncCancelFingerprintAuthentication();
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
}
