package xyz.hui_yi.activity;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.hui_yi.R;
import xyz.hui_yi.face.utils.CustomUtil;
import xyz.hui_yi.face.utils.Logs;
import xyz.hui_yi.face.utils.MeasureUtil;
import xyz.hui_yi.face.utils.StringUtils;
import xyz.hui_yi.face.utils.ToastUtils;
import xyz.hui_yi.face.view.CameraSurfaceView;
import xyz.hui_yi.facesdk.api.CheckAPI;
import xyz.hui_yi.facesdk.conn.Constant;
import xyz.hui_yi.facesdk.entity.Face;
import xyz.hui_yi.facesdk.entity.FaceAttrs;
import xyz.hui_yi.facesdk.entity.MatchVerify;
import xyz.hui_yi.net.QianDaoHttpThread;

import static xyz.hui_yi.activity.MainActivity.userInfo;


/**
 * Created by wangzhi on 2016/1/12.
 * 人脸验证界面
 */

public class FaceVerifyActivity extends BaseAppcompatActivity implements CameraSurfaceView.FaceCallback {

    public static final String ARG_CAMERA_ID = FaceVerifyActivity.class.getSimpleName() + ".camera_id";
    public static final String ARG_UID = FaceVerifyActivity.class.getSimpleName() + ".uid";
    public static final String ARG_MID = FaceVerifyActivity.class.getSimpleName() + ".mid";
    public static final String ARG_RSSI = FaceVerifyActivity.class.getSimpleName() + ".rssi";
    public static final String ARG_BNAME = FaceVerifyActivity.class.getSimpleName() + ".bname";

    private static final String TAG = FaceVerifyActivity.class.getSimpleName();
    @Bind(R.id.cameraSurface)
    CameraSurfaceView mSurfaceView;
    @Bind(R.id.img1)
    ImageView mImg1;
    @Bind(R.id.img2)
    ImageView mImg2;
    @Bind(R.id.img3)
    ImageView mImg3;
    @Bind(R.id.toolbar)
    Toolbar mToolbar;
    @Bind(R.id.takeBtn)
    ImageView mTakeBtn;
    @Bind(R.id.verifyLayout)
    LinearLayout mVerifyLayout;
    @Bind(R.id.verifyImg)
    ImageView mVerifyImg;
    @Bind(R.id.verifyText)
    TextView mVerifyText;
    @Bind(R.id.bgFrame)
    ImageView mBgFrame;
    ProgressDialog mProgressDialog;
    private int mCameraId;
    private String mName;
    private boolean isRefresh = true;
    private String mId;
    private String rssi;
    public final static int QIANDAO_SUCCESS = 200020;
    public final static int QIANDAO_FAIL = 10026;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == QIANDAO_SUCCESS) {
//                Log.i(TAG, "onResult: 签到成功语音");
                Toast.makeText(FaceVerifyActivity.this, getString(R.string.text_login_success), Toast.LENGTH_SHORT).show();
//                TTSUtils.speak(FaceVerifyActivity.this, "签到成功", Integer.parseInt(rssi));
                finish();
            }
            if(msg.what == QIANDAO_FAIL){
                Toast.makeText(FaceVerifyActivity.this, getString(R.string.text_server_error), Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    public void initData() {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
//        TTSUtils.speak(FaceVerifyActivity.this,"请将你脸部放入人脸框内，并直视摄像头");
        Intent intent = null;
        try{
            intent = getIntent();
        }catch (Exception e){
            e.printStackTrace();
        }
        mCameraId = intent.getIntExtra(ARG_CAMERA_ID, 1);
        mName = intent.getStringExtra(ARG_BNAME) + "_" + intent.getStringExtra(ARG_UID);
        mId = intent.getStringExtra(ARG_MID);
        rssi = intent.getStringExtra(ARG_RSSI);
        Log.i(TAG, "initData: rssi" + rssi + "mName" + mName);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);

        mSurfaceView.setCameraId(mCameraId);

//        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        mToolbar.setBackgroundColor(Color.TRANSPARENT);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FaceVerifyActivity.this.finish();
            }
        });

        // 设置拍照回掉接口
        mSurfaceView.setFaceCallback(this);
//        mSurfaceView.getHolder().addCallback();
        setLayoutParams();

    }

    private void setLayoutParams() {
        int screenWidth = MeasureUtil.getScreenSize(this)[0];
        int mSurfaceViewHeight = screenWidth / 3 * 4;
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mSurfaceView.getLayoutParams();
        lp.height = mSurfaceViewHeight;
        mSurfaceView.setLayoutParams(lp);

        mImg1.post(new Runnable() {
            @Override
            public void run() {
                LinearLayout.LayoutParams lp1 = (LinearLayout.LayoutParams) mImg1.getLayoutParams();
                lp1.height = mImg1.getMeasuredWidth();
                Log.i(TAG, "img1 width:" + mImg1.getWidth());
                mImg1.setLayoutParams(lp1);

                LinearLayout.LayoutParams lp2 = (LinearLayout.LayoutParams) mImg2.getLayoutParams();
                lp2.height = mImg2.getMeasuredWidth();
                mImg2.setLayoutParams(lp2);

                LinearLayout.LayoutParams lp3 = (LinearLayout.LayoutParams) mImg3.getLayoutParams();
                lp3.height = mImg3.getMeasuredWidth();
                mImg3.setLayoutParams(lp3);
            }
        });

        FrameLayout.LayoutParams bgLp = (FrameLayout.LayoutParams) mBgFrame.getLayoutParams();
        bgLp.height = mSurfaceViewHeight / 5 * 3;
        bgLp.width = screenWidth / 5 * 3;
        mBgFrame.setLayoutParams(bgLp);
        mTakeBtn.setAdjustViewBounds(true);
        mVerifyImg.setAdjustViewBounds(true);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_faceverify;
    }

    /**
     * 拍照
     */
    @OnClick(R.id.takeBtn)
    void takeBtnClick() {
        mSurfaceView.startCapture();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSurfaceView.onResume();
        mTakeBtn.setScaleX(0f);
        mTakeBtn.setScaleY(0f);

        mTakeBtn.postDelayed(new Runnable() {
            @Override
            public void run() {
                startTakeAnim();
            }
        }, 1000);

    }

    private void startTakeAnim() {
        ObjectAnimator takeAnim1 = ObjectAnimator.ofFloat(mTakeBtn, "scaleY", 0f, 1f);
        ObjectAnimator takeAnim2 = ObjectAnimator.ofFloat(mTakeBtn, "scaleX", 0f, 1f);
        AnimatorSet animSet = new AnimatorSet();
        animSet.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
        animSet.setInterpolator(new OvershootInterpolator());
        //两个动画同时执行
        animSet.playTogether(takeAnim1, takeAnim2);
        animSet.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSurfaceView.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            actionRefresh();
            return true;
        }if(id == android.R.id.home){
            this.finish(); // back button
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void actionRefresh() {
        if (isRefresh)
            return;
        isRefresh = true;
        mSurfaceView.getCameraManager().startPreview();
        ObjectAnimator takeAnim1 = ObjectAnimator.ofFloat(mTakeBtn, "translationY", 300f, 0f);
        ObjectAnimator takeAnim2 = ObjectAnimator.ofFloat(mTakeBtn, "alpha", 0f, 1f);

        ObjectAnimator animator1 = ObjectAnimator.ofFloat(mVerifyLayout, "translationY", 0f, -300f);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(mVerifyLayout, "alpha", 1f, 0f);

        AnimatorSet animSet = new AnimatorSet();
        animSet.setDuration(getResources().getInteger(android.R.integer.config_shortAnimTime));
        animSet.setInterpolator(new OvershootInterpolator());
        //两个动画同时执行
        animSet.playTogether(animator1, animator2, takeAnim1, takeAnim2);
        animSet.start();
        mImg1.setImageBitmap(null);
    }

    @Override
    public void onResullt(Bitmap[] bitmaps) {
        mImg1.setImageBitmap(bitmaps[0]);
        mImg2.setImageBitmap(bitmaps[1]);
        mImg3.setImageBitmap(bitmaps[2]);

        checkImageData(bitmaps);
    }

    private void checkImageData(Bitmap[] bitmaps) {
        if (bitmaps == null) {
            ToastUtils.show(this, R.string.toast_no_faceId);
//            TTSUtils.speak(FaceVerifyActivity.this,"检测失败,请重试");
            return;
        }

//        FaceDetector.Face[] faces = new FaceDetector.Face[2];
//        //格式必须为RGB_565才可以识别
//        Bitmap bmp = bitmaps[0].copy(Bitmap.Config.RGB_565, true);
//        //返回识别的人脸数
//        int faceCount = new FaceDetector(bmp.getWidth(), bmp.getHeight(), 2).findFaces(bmp, faces);
//        bmp.recycle();
//        bmp = null;
//        Log.e("tag", "识别的人脸数:" + faceCount);

        String dataImage = CustomUtil.bitmapToBase64(bitmaps[1]);
        mProgressDialog.setMessage(getString(R.string.app_verifying));
//        TTSUtils.speak(FaceVerifyActivity.this,"正在验证");
        mProgressDialog.show();
        Call<FaceAttrs> call = CheckAPI.checkingImageData(dataImage, null, null);
        call.enqueue(new Callback<FaceAttrs>() {
            @Override
            public void onResponse(Call<FaceAttrs> call, Response<FaceAttrs> response) {
                FaceAttrs faceAttrs = response.body();
                Log.i(TAG, "response:" + faceAttrs == null ? null : faceAttrs.toString());
                handlerCheck(faceAttrs);
            }

            @Override
            public void onFailure(Call<FaceAttrs> call, Throwable t) {
                Log.e(TAG, "onFailure:" + (t == null ? null : t.getMessage()));
                mProgressDialog.dismiss();
                ToastUtils.show(FaceVerifyActivity.this, R.string.toast_network_error);
                exit();
            }
        });
    }

    private void handlerCheck(FaceAttrs data) {
        Logs.i(TAG, data.toString());
        if (data == null) {
            ToastUtils.show(this, R.string.text_verify_failed);
//            TTSUtils.speak(FaceVerifyActivity.this,"认证失败，签到未成功");
            return;
        }
        List<Face> faces = data.getFace();
        if (!StringUtils.isEquals(Constant.RES_CODE_0000, data.getRes_code()) || faces == null) {
            mProgressDialog.dismiss();
            ToastUtils.show(this, R.string.toast_no_faceId);
//            TTSUtils.speak(FaceVerifyActivity.this,"人脸检测失败，签到未成功");
            verifyError();
            return;
        }
        if (!faces.isEmpty() && !StringUtils.isBlank(faces.get(0).getFace_id())) {
            matchVerify(faces.get(0).getFace_id());
        } else {
            mProgressDialog.dismiss();
            ToastUtils.show(this, R.string.text_verify_failed);
//            TTSUtils.speak(FaceVerifyActivity.this,"认证失败，签到未成功");
            verifyError();
        }
    }

    /**
     * 比对
     */
    private void matchVerify(String faceId) {
        Call<MatchVerify> call = CheckAPI.matchVerify(faceId, mName);
        call.enqueue(new Callback<MatchVerify>() {

            public void onFinish() {
                mProgressDialog.dismiss();
            }

            @Override
            public void onResponse(Call<MatchVerify> call, Response<MatchVerify> response) {
                onFinish();
                handleVerify(response.body());
            }

            @Override
            public void onFailure(Call<MatchVerify> call, Throwable t) {
                onFinish();
                ToastUtils.show(FaceVerifyActivity.this, "认证失败，请检查网络连接");
            }
        });
    }

    private void handleVerify(MatchVerify data) {
        if (data == null) {
            ToastUtils.show(this, "认证失败");
//            TTSUtils.speak(FaceVerifyActivity.this,"认证失败，签到未成功");
            verifyError();
            return;
        }
        mVerifyLayout.setVisibility(View.VISIBLE);
        if (StringUtils.isEquals(data.getRes_code(), Constant.RES_CODE_0000) && data.isResult() == true) {
            //TODO 签到成功联网操作 uid:用户名 mid:会议 pid:指纹ID（null) rssi:无线信号值//
            verifySuc(data.getSimilarity());
        } else if (StringUtils.isEquals(data.getRes_code(), Constant.RES_CODE_0000) && data.isResult() == false) {
            ToastUtils.show(this, R.string.text_fp_login_failed_no);
//            TTSUtils.speak(FaceVerifyActivity.this,"签到失败，不是同一个人");
            verifyFail(data.getSimilarity());
        } else if (StringUtils.isEquals(data.getRes_code(), Constant.RES_CODE_1025)) {
            ToastUtils.show(this, R.string.text_user_unregister);
            verifyError();
        } else {
            ToastUtils.show(this,R.string.toast_verify_no_id);
//            TTSUtils.speak(FaceVerifyActivity.this,"签到失败");
            verifyError();
        }
    }

    private void verifyError() {
        mVerifyLayout.setVisibility(View.VISIBLE);
        mVerifyImg.setImageResource(R.mipmap.verify_fail);
        mVerifyText.setText("");
        startVerifyAnim();
    }

    private void verifyFail(double similarity) {
        mVerifyText.setText(similarity + "");
        mVerifyImg.setImageResource(R.mipmap.verify_fail);
        startVerifyAnim();
    }

    private void verifySuc(double similarity) {
        mVerifyText.setText(similarity + "");
        mVerifyImg.setImageResource(R.mipmap.verify_suc);
        String url = getResources().getString(R.string.URL);
        url = url + "/public/api/Meet/uMeetSign";
        Log.i(TAG, "handleVerify: mid" + mId + "uid" + userInfo.getUid()+ "rssi" +rssi);
        String pid = "";
        new QianDaoHttpThread(handler,url,userInfo.getUid(),mId,"",pid,rssi).start();
        startVerifyAnim();
    }

    private void startVerifyAnim() {
        isRefresh = false;
        ObjectAnimator takeAnim1 = ObjectAnimator.ofFloat(mTakeBtn, "translationY", 0f, 300f);
        ObjectAnimator takeAnim2 = ObjectAnimator.ofFloat(mTakeBtn, "alpha", 1f, 0f);

        ObjectAnimator animator1 = ObjectAnimator.ofFloat(mVerifyLayout, "translationY", -300f, 0f);
        ObjectAnimator animator2 = ObjectAnimator.ofFloat(mVerifyLayout, "alpha", 0f, 1f);

        AnimatorSet animSet = new AnimatorSet();
        animSet.setDuration(300);
        animSet.setInterpolator(new OvershootInterpolator());
        //两个动画同时执行
        animSet.playTogether(animator1, animator2, takeAnim1, takeAnim2);
        animSet.start();
    }

    private void exit() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 1000);
    }
}
