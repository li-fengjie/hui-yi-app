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
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import xyz.hui_yi.face.utils.CustomUtil;
import xyz.hui_yi.face.utils.FaceGatherAddFace;
import xyz.hui_yi.face.utils.Logs;
import xyz.hui_yi.face.utils.MeasureUtil;
import xyz.hui_yi.face.utils.StringUtils;
import xyz.hui_yi.face.view.CameraSurfaceView;
import xyz.hui_yi.facesdk.api.CheckAPI;
import xyz.hui_yi.facesdk.conn.Constant;
import xyz.hui_yi.facesdk.entity.Face;
import xyz.hui_yi.facesdk.entity.FaceAttrs;
import xyz.hui_yi.facesdk.entity.PeopleAdd;
import xyz.hui_yi.facesdk.entity.PeopleCreate;
import xyz.hui_yi.net.UnloadUphotoHttpThread;
import xyz.hui_yi.utils.SnackBarUtils.SnackBarUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Created by LiFen on 2018/1/29.
 * 人脸录入界面
 */

public class FaceRegActivity extends BaseAppcompatActivity implements CameraSurfaceView.FaceCallback {

    public static final String ARG_CAMERA_ID = FaceRegActivity.class.getSimpleName() + ".camera_id";
    public static final String ARG_UID = FaceRegActivity.class.getSimpleName() + ".uid";
    public static final String ARG_IS_CREATED = FaceRegActivity.class.getSimpleName() + ".created";
    public static final String ARG_BNAME = FaceRegActivity.class.getSimpleName() + ".bname";
    private static final String TAG = FaceRegActivity.class.getSimpleName();
    public final static int SUCCESS = 20001;
    public final static int SERVE_ERROR = 30000;

    @Bind(xyz.hui_yi.R.id.cameraSurface)
    CameraSurfaceView mSurfaceView;
    @Bind(xyz.hui_yi.R.id.img1)
    ImageView mImg1;
    @Bind(xyz.hui_yi.R.id.img2)
    ImageView mImg2;
    @Bind(xyz.hui_yi.R.id.img3)
    ImageView mImg3;
    @Bind(xyz.hui_yi.R.id.toolbar)
    Toolbar mToolbar;
    @Bind(xyz.hui_yi.R.id.takeBtn)
    ImageView mTakeBtn;
    @Bind(xyz.hui_yi.R.id.bgFrame)
    ImageView mBgFrame;
    ProgressDialog mProgressDialog;
    private int mCameraId;
    private String mName;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case SUCCESS:
                    Toast.makeText(getApplicationContext(),getString(xyz.hui_yi.R.string.text_add_face_success),Toast.LENGTH_SHORT).show();
                case SERVE_ERROR:
                    Toast.makeText(getApplicationContext(),getString(xyz.hui_yi.R.string.text_server_error),Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    /**
     * 是否已经创建people
     */
    private boolean isCreated;
    /**
     * 是否有faceId
     */
    private boolean hasFaceId = false;
    private List<String> mBase64List = new ArrayList<String>();
    private String mFaceAll = "";
    private String uid;
    private String mBname;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
            case xyz.hui_yi.R.id.action_jump:
                this.finish(); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void initData() {
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
//        TTSUtils.speak(FaceRegActivity.this,"请直视摄像头");
        Intent intent = getIntent();
        mCameraId = intent.getIntExtra(ARG_CAMERA_ID, 1);
        mBname = intent.getStringExtra(ARG_BNAME);
        mName = intent.getStringExtra(ARG_BNAME) + "_" + intent.getStringExtra(ARG_UID);
        uid = intent.getStringExtra(ARG_UID);
        isCreated = intent.getBooleanExtra(ARG_IS_CREATED, false);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);

        mSurfaceView.setCameraId(mCameraId);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        mToolbar.setBackgroundColor(Color.TRANSPARENT);

        // 设置拍照回掉接口
        mSurfaceView.setFaceCallback(this);

        if (CustomUtil.isLandScape(this)) {
            setLandLayoutParams();
        } else {
            setPortraitLayoutParams();
        }
    }

    private void setLandLayoutParams() {
        mToolbar.setVisibility(View.GONE);

        int screenWidth = MeasureUtil.getScreenSize(this)[0];
        int screenHeight = MeasureUtil.getScreenSize(this)[1];
        Logs.i(TAG, "screenWidth:" + screenWidth);
        Logs.i(TAG, "screenHeight:" + screenHeight);
        int mSurfaceViewWidth = screenHeight / 3 * 4;
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mSurfaceView.getLayoutParams();
        lp.width = mSurfaceViewWidth;
        mSurfaceView.setLayoutParams(lp);

        mImg1.post(new Runnable() {
            @Override
            public void run() {
                LinearLayout.LayoutParams lp1 = (LinearLayout.LayoutParams) mImg1.getLayoutParams();
                lp1.width = mImg1.getMeasuredHeight();
                Log.i(TAG, "img1 width:" + mImg1.getWidth());
                mImg1.setLayoutParams(lp1);

                LinearLayout.LayoutParams lp2 = (LinearLayout.LayoutParams) mImg2.getLayoutParams();
                lp2.width = mImg2.getMeasuredHeight();
                mImg2.setLayoutParams(lp2);

                LinearLayout.LayoutParams lp3 = (LinearLayout.LayoutParams) mImg3.getLayoutParams();
                lp3.width = mImg3.getMeasuredHeight();
                mImg3.setLayoutParams(lp3);
            }
        });

        mTakeBtn.setAdjustViewBounds(true);

        FrameLayout.LayoutParams bgLp = (FrameLayout.LayoutParams) mBgFrame.getLayoutParams();
        bgLp.height = screenHeight / 5 * 3;
        bgLp.width = bgLp.height / 4 * 3;
        mBgFrame.setLayoutParams(bgLp);
    }

    private void setPortraitLayoutParams() {
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

        mTakeBtn.setAdjustViewBounds(true);

        FrameLayout.LayoutParams bgLp = (FrameLayout.LayoutParams) mBgFrame.getLayoutParams();
        bgLp.height = mSurfaceViewHeight / 5 * 3;
        bgLp.width = screenWidth / 5 * 3;
        mBgFrame.setLayoutParams(bgLp);
    }

    @Override
    public int getLayoutId() {
        return xyz.hui_yi.R.layout.activity_faceregister;
    }

    @OnClick(xyz.hui_yi.R.id.takeBtn)
    void takeBtnClick() {
        mSurfaceView.startCapture();
        mTakeBtn.setClickable(false);
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
        }, 600);
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
    public void onResullt(Bitmap[] bitmaps) {
        if (bitmaps == null)
            return;
        mImg1.setImageBitmap(bitmaps[0]);
        mImg2.setImageBitmap(bitmaps[1]);
        mImg3.setImageBitmap(bitmaps[2]);

        String bmp0 = CustomUtil.bitmapToBase64(bitmaps[0]);
        String bmp1 = CustomUtil.bitmapToBase64(bitmaps[1]);
        String bmp2 = CustomUtil.bitmapToBase64(bitmaps[2]);

        mBase64List.add(bmp0);
        mBase64List.add(bmp1);
        mBase64List.add(bmp2);

        mProgressDialog.setMessage(CustomUtil.getString(this, xyz.hui_yi.R.string.text_enroll));
        mProgressDialog.show();
        getImageData(mBase64List.get(0));
        //TODO 联网上传头像
        String url = getResources().getString(xyz.hui_yi.R.string.URL);
        url = url + "/public/api/User/uploadPicture";
        Log.i(TAG, "onResullt: uid" + uid);
        Log.i(TAG, "onResullt: bmp1" + bmp1);
        String uphoto = Base64.encodeToString(bmp1.getBytes(), Base64.URL_SAFE | Base64.NO_WRAP);
        Log.i(TAG, "onResullt: uphoto" + uphoto);
        new UnloadUphotoHttpThread(mHandler,url,uid,uphoto).start();
    }

    /**
     * 获取单张人脸信息
     *
     * @param base64
     */
    private void getImageData(String base64) {
        Call<FaceAttrs> call = CheckAPI.checkingImageData(base64, null, null);
        call.enqueue(new Callback<FaceAttrs>() {

            public void onFinish() {
                if (!mBase64List.isEmpty()) {
                    mBase64List.remove(0);
                }
                if (!mBase64List.isEmpty()) {
                    getImageData(mBase64List.get(0));
                } else {
                    addFaceId();
                }
            }

            @Override
            public void onResponse(Call<FaceAttrs> call, Response<FaceAttrs> response) {
                handleFaceData(response.body());
                onFinish();
            }

            @Override
            public void onFailure(Call<FaceAttrs> call, Throwable t) {
                onFinish();
            }
        });
    }

    private void handleFaceData(FaceAttrs data) {
        Logs.i(TAG, data.toString());
        List<Face> faces = data.getFace();
        if (StringUtils.isEquals("0000", data.getRes_code()) && faces != null) {
            hasFaceId = true;
            mFaceAll += faces.get(0).getFace_id() + ",";
        }
    }

    /**
     * 添加faceId
     */
    private void addFaceId() {
        if (!hasFaceId) {
            mProgressDialog.dismiss();
            SnackBarUtils.show(mSurfaceView, xyz.hui_yi.R.string.toast_no_faceId);
            exit();
            return;
        }
        if (!isCreated) {
            createPeople();
            facegatherAddFaceid();
            return;
        }
        Call<PeopleAdd> call = CheckAPI.peopleAdd(mFaceAll, mName);
        call.enqueue(new Callback<PeopleAdd>() {

            public void onFinish() {
                mProgressDialog.dismiss();
            }

            @Override
            public void onResponse(Call<PeopleAdd> call, Response<PeopleAdd> response) {
                onFinish();
                handleAdd(response.body());
            }

            @Override
            public void onFailure(Call<PeopleAdd> call, Throwable t) {
                onFinish();
                SnackBarUtils.show(mSurfaceView, xyz.hui_yi.R.string.toast_enroll_failed);
                exit();
            }
        });
    }

    /**
     * facegather 中添加faceid
     */
    private void facegatherAddFaceid() {
//        Log.i(TAG, "facegatherAddFaceid: " + mFaceAll);
        //facegather 之前已创建 test
        // 添加faceid到 facegather
        Log.i(TAG, "FaceGather " + mFaceAll);
        Call<FaceGatherAddFace> call = CheckAPI.faceGatherAddFace(mBname, mFaceAll);
        call.enqueue(new Callback<FaceGatherAddFace>() {
            @Override
            public void onResponse(Call<FaceGatherAddFace> call, Response<FaceGatherAddFace> response) {
                if (response.isSuccessful()) {
                    if(StringUtils.isEquals(Constant.RES_CODE_1031, response.body().getRes_code())){
                        Log.i(TAG, "FaceGather" + response.body().toString());
//                        facegather_name 不存在
                    }
                    if (StringUtils.isEquals(Constant.RES_CODE_0000, response.body().getRes_code())) {
                        Toast.makeText(getApplicationContext(),"人脸信息已加入到公司人脸库",Toast.LENGTH_SHORT).show();
                        Log.i(TAG, "FaceGather" + response.body().toString());
                    }
                } else {
                    Log.i(TAG, "FaceGather失败" + response.body().toString());
                }
            }

            @Override
            public void onFailure(Call<FaceGatherAddFace> call, Throwable t) {

            }
        });
    }

    private void handleAdd(PeopleAdd data) {
        Logs.i(TAG, "添加faceId:" + data.toString());
        if (StringUtils.isEquals(Constant.RES_CODE_0000, data.getRes_code())) {
            SnackBarUtils.show(mSurfaceView, xyz.hui_yi.R.string.text_add_face_success);
        } else {
            SnackBarUtils.show(mSurfaceView, xyz.hui_yi.R.string.toast_enroll_failed);
        }
        exit();
    }

    /**
     * 创建用户
     */
    private void createPeople() {
        Logs.i(TAG, mFaceAll);
        Call<PeopleCreate> call = CheckAPI.peopleCreate(mFaceAll, mName, null, null);
        call.enqueue(new Callback<PeopleCreate>() {

            private void onFinish() {
                mProgressDialog.dismiss();
            }

            @Override
            public void onResponse(Call<PeopleCreate> call, Response<PeopleCreate> response) {
                onFinish();
                handleCreate(response.body());
            }

            @Override
            public void onFailure(Call<PeopleCreate> call, Throwable t) {
                onFinish();
                SnackBarUtils.show(mSurfaceView, xyz.hui_yi.R.string.toast_network_error);
                exit();
            }
        });
    }

    private void handleCreate(PeopleCreate data) {
        Logs.i(TAG, data.toString());
        if (StringUtils.isEquals(data.getRes_code(), Constant.RES_CODE_0000)) {
            SnackBarUtils.show(mSurfaceView, xyz.hui_yi.R.string.text_add_face_success);
            exit();
        } else {
            mProgressDialog.dismiss();
            SnackBarUtils.show(mSurfaceView, xyz.hui_yi.R.string.toast_enroll_failed);
            exit();
        }
    }

    private void exit() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 1000);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(xyz.hui_yi.R.menu.menu_jump, menu);
        return true;
    }
}
