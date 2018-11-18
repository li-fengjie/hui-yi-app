package xyz.hui_yi.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import xyz.hui_yi.constants.Constants;
import xyz.hui_yi.face.utils.CustomUtil;
import xyz.hui_yi.face.utils.StringUtils;
import xyz.hui_yi.facesdk.api.CheckAPI;
import xyz.hui_yi.facesdk.entity.Face;
import xyz.hui_yi.facesdk.entity.FaceAttrs;
import xyz.hui_yi.facesdk.entity.MatchSearch;
import xyz.hui_yi.facesdk.entity.Result;
import xyz.hui_yi.net.QianDaoHttpThread;
import xyz.hui_yi.utils.BitmapUtils;
import xyz.hui_yi.utils.CameraInterface;
import xyz.hui_yi.utils.DisplayUtil;
import xyz.hui_yi.utils.PrefUtils.PrefUtils;
import xyz.hui_yi.utils.tts.TTSUtils;
import xyz.hui_yi.view.MySurfaceView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by LiFen on 2018/4/1.
 */

public class PhotoActivity extends Activity implements SurfaceHolder.Callback{
    private static final String TAG = "PhotoActivity";
    private SurfaceHolder mSurfaceHolder = null;
    public MySurfaceView mSurfaceView;
    private int DST_CENTER_RECT_WIDTH = 200;  //单位是dip
    private int DST_CENTER_RECT_HEIGHT = 200;
    private float previewRate = -1f;
    private Point scrSize;
    private Point picSize;
    private Rect[] screenCenterRect;  //边框矩阵，用于设置边框位置和获取边框图片的位置
    private TextView mFaceCountView;
    private ImageView mFacePreviewView;
    private Bitmap mFacePreviewBitmap;
    private ImageView mLightBtnView;
    private View mScanView;
    private View mLight;
    private View mCameraChangedView;
    private int mFaceCount;
    private int count = 0;
    private String mid;
    private boolean hasFaceId;
    private String uname;
    private List<String> mFacesIDList = new ArrayList<String>();
    public static Handler mainHandler;
    private int CAMERA_ID;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == Constants.QIANDAO_SUCCESS) {
                uname = (String) msg.obj;
                if(PrefUtils.getBoolean(getApplication(),Constants.VOIUME_OFF_ON_KEY,true)){
                    TTSUtils.speak(getApplicationContext(), uname);
                }
                Toast.makeText(getApplicationContext(), uname + getString(xyz.hui_yi.R.string.text_login_success), Toast.LENGTH_SHORT).show();
                mFacesIDList.remove(0);
                if(!mFacesIDList.isEmpty()){
                    searchFace();
                }
            }
            if (msg.what == Constants.SERVER_ERROR) {
                Toast.makeText(getApplicationContext(), getString(xyz.hui_yi.R.string.text_server_error), Toast.LENGTH_SHORT).show();
            }
            if (msg.what == Constants.QIANDAO_FAIL || msg.what == 10031) {
                Toast.makeText(getApplicationContext(), getString(xyz.hui_yi.R.string.text_fp_login_failed), Toast.LENGTH_SHORT).show();
            }
        }
    };
    private CameraInterface.FaceChangedCallback faceChangedCallback = new CameraInterface.FaceChangedCallback() {
        @Override
        public void facechanged(Rect[] rects) {
            if (rects == null){
                mSurfaceView.setCenterRect(null);
            }else {
                screenCenterRect = rects;
                mSurfaceView.setCenterRect(screenCenterRect);
            }
        }

        @Override
        public void faceCapture(Camera.Face[] faces) {
            if(faces != null){
                mFaceCount = faces.length;
            }else {
                mFaceCount = 0;
            }
            mFaceCountView.setText(mFaceCount+"");
        }

        @Override
        public void faceBitmap(Bitmap bitmap) {
            if(mFaceCount > 0){
                mFacePreviewBitmap = bitmap;
                if(PrefUtils.getBoolean(getApplication(), Constants.KEY_IS_CAMERA_1, true)){
                    mFacePreviewBitmap = BitmapUtils.convert(mFacePreviewBitmap);
                }
                mFacePreviewView.setImageBitmap(mFacePreviewBitmap);
                mScanView.setVisibility(View.VISIBLE);
                int face_count = PrefUtils.getInt(getApplication(), "face_count", 0);
                if(face_count != mFaceCount){
                    PrefUtils.setInt(getApplication(),"face_count",mFaceCount);
                    Log.i(TAG, "run " + face_count);
                    String base64Bitmap = CustomUtil.bitmapToBase64(mFacePreviewBitmap);
                    checkFace(base64Bitmap);
                }
            }else {
                PrefUtils.setInt(getApplication(),"face_count",0);
                count = 0;
//                mFacePreviewView.setImageBitmap(null);
            }
        }
    };

    private void checkFace(String base64Bitmap) {
        Call<FaceAttrs> call = CheckAPI.checkingImageData(base64Bitmap, null, null);
        call.enqueue(new Callback<FaceAttrs>() {

            @Override
            public void onResponse(Call<FaceAttrs> call, Response<FaceAttrs> response) {
                List<Face> faces = response.body().getFace();
                Log.i(TAG, "faces_id: " + response.body().getFace());
                if (StringUtils.isEquals("0000", response.body().getRes_code()) && faces != null && faces.size() > 0) {
                    hasFaceId = true;
                    for(int i = 0; i < faces.size(); i++){
                        Log.i(TAG, "faces_id: " + faces.get(i).getFace_id());
                        mFacesIDList.add(faces.get(i).getFace_id());
                    }
                    if(faces.size() == 0){
                        Toast.makeText(getApplicationContext(),"未检测到人脸",Toast.LENGTH_SHORT).show();
                    }
                    if(!mFacesIDList.isEmpty()){
                        searchFace();
                    }else{
                        Toast.makeText(getApplicationContext(),"未检测到人脸",Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getApplicationContext(),"未检测到人脸",Toast.LENGTH_SHORT).show();
                }
                Log.i(TAG, "onResponse: " + response.body());
            }

            @Override
            public void onFailure(Call<FaceAttrs> call, Throwable t) {

            }
        });
    }

    private void searchFace() {
        if(MainActivity.userInfo.getBname() != null){
            Log.i(TAG, "searchFace: Bname" + MainActivity.userInfo.getBname());
            Call<MatchSearch> matchSearchCall = CheckAPI.matchSearch(mFacesIDList.get(0), MainActivity.userInfo.getBname(), 1);
            matchSearchCall.enqueue(new Callback<MatchSearch>() {
                @Override
                public void onResponse(Call<MatchSearch> call, Response<MatchSearch> response) {
                    if(response.body() != null) {
                        if(StringUtils.isEquals("0000", response.body().getRes_code())) {
                            Result result = response.body().getResult().get(0);
                            Log.i(TAG, "faces_id: searchFace" + result.getSimilarity() + " " + result.getPeople_name());
                            String people_name = result.getPeople_name();
                            Log.i(TAG, "getNetData:" + people_name);
                            String[] split = people_name.split("_");
                            String _uid;
                            if(split.length > 1){
                                _uid = split[1];
                            }else {
                                Log.i(TAG, "onResponse: " + "people_name error");
                                return;
                            }
                            Toast.makeText(getApplicationContext(), result.getSimilarity() + " " + _uid, Toast.LENGTH_SHORT).show();
                            if(result.getSimilarity() > 55){
                                //TODO 会议签到接口
                                String url = getResources().getString(xyz.hui_yi.R.string.URL);
                                url = url + "/public/api/Meet/uMeetSign";
                                Log.i(TAG, "getNetData:" + _uid + " " + mid);
                                new QianDaoHttpThread(handler, url,_uid ,"",mid, "","0").start();
                            }else {
                                Toast.makeText(getApplicationContext(), "未知人员，请重试", Toast.LENGTH_SHORT).show();
                            }
                        }else {
                            TTSUtils.speak(getApplicationContext(),"扫描失败,请重试");
//                            Toast.makeText(getApplicationContext(), response.body().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(Call<MatchSearch> call, Throwable t) {
                    Log.i(TAG, "faces_id: " +"searchFail");
                    Log.i(TAG, "onFailure: " + t);
                    return;
                }
            });
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(xyz.hui_yi.R.layout.activity_photo);
        initView();
        prepareData();
        Intent intent = getIntent();
        mid = intent.getStringExtra(Constants.MID);
        initViewParams();
        CameraInterface.getInstance().setFaceChangedCallback(faceChangedCallback);
        mLightBtnView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mFaceCountView.getVisibility() == View.VISIBLE){
                    mLight.setBackgroundColor(Color.BLACK);
                    mFaceCountView.setVisibility(View.INVISIBLE);
                    mCameraChangedView.setVisibility(View.INVISIBLE);
                }else {
                    mLight.setBackgroundColor(Color.TRANSPARENT);
                    mFaceCountView.setVisibility(View.VISIBLE);
                    mCameraChangedView.setVisibility(View.VISIBLE);
                }
            }
        });
        mCameraChangedView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CameraInterface.getInstance().doStopCamera();
                boolean isCameraRear = PrefUtils.getBoolean(getApplication(), Constants.KEY_IS_CAMERA_1, true);
                PrefUtils.setBoolean(getApplication(), Constants.KEY_IS_CAMERA_1,!isCameraRear);
                int CAMERA_ID;
                if(isCameraRear){
                    CAMERA_ID = 0;
                }else {
                    CAMERA_ID = 1;
                }
                CameraInterface.getInstance().doOpenCamera(CAMERA_ID);
                CameraInterface.getInstance().doStartPreview(scrSize, picSize, mSurfaceHolder, previewRate);
//                Intent intent = new Intent(getApplicationContext(), PhotoActivity.class);
//                startActivity(intent);
            }
        });
    }

    private void prepareData() {
        TTSUtils.asnyncInitTTs(getApplication(),mainHandler);
    }

    private void initView() {
        mSurfaceView = (MySurfaceView)findViewById(xyz.hui_yi.R.id.mySurfaceVIew);
        mFaceCountView= (TextView) findViewById(xyz.hui_yi.R.id.tv_facecount);
        mFacePreviewView = (ImageView) findViewById(xyz.hui_yi.R.id.imageView2);
        mLight = findViewById(xyz.hui_yi.R.id.light_bg);
        mLightBtnView = (ImageView) findViewById(xyz.hui_yi.R.id.iv_light);
        mScanView = findViewById(xyz.hui_yi.R.id.scanview);
        mCameraChangedView = findViewById(xyz.hui_yi.R.id.iv_camera_changed);
        mainHandler = new Handler() {
            /*
             * @param msg
             */
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.obj != null) {
                    Log.i(TAG, "handleMessage: " + msg.obj.toString());
                }
            }
        };
    }

    private void initViewParams() {
        if (mSurfaceView != null) {
            picSize = new Point(DisplayUtil.dip2px(this, DST_CENTER_RECT_WIDTH),
                    DisplayUtil.dip2px(this, DST_CENTER_RECT_HEIGHT));
//            screenCenterRect = createCenterScreenRect(picSize.x, picSize.y);
//            mSurfaceView.setCenterRect(screenCenterRect);
        }
        ViewGroup.LayoutParams params = mSurfaceView.getLayoutParams();
        Point p = DisplayUtil.getScreenMetrics(this);
        params.width = p.x;
        params.height = p.y;
        previewRate = DisplayUtil.getScreenRate(this);
        mSurfaceView.setLayoutParams(params);
        mSurfaceView.setZOrderOnTop(false);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.setFormat(PixelFormat.TRANSPARENT);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSurfaceHolder.addCallback(this);
        int wScreen = DisplayUtil.getScreenMetrics(this).x;
        int hScreen = DisplayUtil.getScreenMetrics(this).y;
        scrSize = new Point(wScreen, hScreen);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        boolean isCameraRear = PrefUtils.getBoolean(getApplication(), Constants.KEY_IS_CAMERA_1, true);
        if(isCameraRear){
            CAMERA_ID = 1;
        }else {
            CAMERA_ID = 0;
        }
        CameraInterface.getInstance().doOpenCamera(CAMERA_ID);
        CameraInterface.getInstance().doStartPreview(scrSize, picSize, mSurfaceHolder, previewRate);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        CameraInterface.getInstance().doStopCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        TTSUtils.cancelTTS();
    }

}
