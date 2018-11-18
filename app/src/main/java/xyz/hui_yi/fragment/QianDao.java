package xyz.hui_yi.fragment;

/**
 * Created by LiFen on 2018/1/15.
 */

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import xyz.hui_yi.activity.AdmDataManageActivity;
import xyz.hui_yi.activity.FaceVerifyActivity;
import xyz.hui_yi.activity.MainActivity;
import xyz.hui_yi.adapter.QianDaoListAdapter;
import xyz.hui_yi.bean.QianDaosBean;
import xyz.hui_yi.constants.Constants;
import xyz.hui_yi.face.utils.CustomUtil;
import xyz.hui_yi.face.utils.Logs;
import xyz.hui_yi.face.utils.StringUtils;
import xyz.hui_yi.face.utils.ToastUtils;
import xyz.hui_yi.facesdk.api.CheckAPI;
import xyz.hui_yi.facesdk.conn.Constant;
import xyz.hui_yi.facesdk.entity.PeopleGet;
import xyz.hui_yi.layout.EmptyLayout;
import xyz.hui_yi.layout.MySwipeRefreshLayout;
import xyz.hui_yi.net.QListDownAsynctask;
import xyz.hui_yi.net.QianDaoHttpThread;
import xyz.hui_yi.utils.BitmapUtils;
import xyz.hui_yi.utils.CameraUtils;
import xyz.hui_yi.utils.GPSUtils;
import xyz.hui_yi.utils.PrefUtils.PrefUtils;
import xyz.hui_yi.utils.SnackBarUtils.SnackBarUtils;
import xyz.hui_yi.utils.VibrateUtils;
import xyz.hui_yi.utils.bluetoothutils.BluetoothUtils;
import xyz.hui_yi.utils.tts.TTSUtils;
import xyz.hui_yi.utils.wifiutils.WifiUtils;
import xyz.hui_yi.view.RadarView;
import com.tencent.soter.wrapper.SoterWrapperApi;
import com.tencent.soter.wrapper.wrap_callback.SoterProcessAuthenticationResult;
import com.tencent.soter.wrapper.wrap_callback.SoterProcessCallback;
import com.tencent.soter.wrapper.wrap_callback.SoterProcessKeyPreparationResult;
import com.tencent.soter.wrapper.wrap_core.SoterProcessErrCode;
import com.tencent.soter.wrapper.wrap_fingerprint.SoterFingerprintCanceller;
import com.tencent.soter.wrapper.wrap_fingerprint.SoterFingerprintStateCallback;
import com.tencent.soter.wrapper.wrap_task.AuthenticationParam;
import com.tencent.soter.wrapper.wrap_task.InitializeParam;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static xyz.hui_yi.activity.MainActivity.uid;
import static xyz.hui_yi.activity.MainActivity.userInfo;


/**
 * Sample中重点演示了一个场景：指纹签到。
 * {@link SoterWrapperApi#init(Context, SoterProcessCallback, InitializeParam)}
 * 签到界面
 * 一、签到用户
 * 会议签到方式：
 * ①无线方式
 * 指纹签到
 * 人脸签到（设备不支持指纹）
 * ②蓝牙方式
 * 指纹签到
 * 人脸签到（设备不支持指纹）
 * 二、管理员
 * 会议签到情况管理
 */
public class QianDao extends Fragment {

    private static final String TAG = "QianDao";
    private View inflate;
    private ArrayList<QianDaosBean.QianDaos> data;
    private QianDaoListAdapter adapter;

    private ExecutorService es;
    private Dialog mPasswordDialog = null;
    private Dialog mFingerprintDialog = null;
    private ProgressDialog mLoadingDialog = null;
    private View mCustomFingerprintView = null;
    private TextView mFingerprintStatusHintView = null;
    private ImageView mFingerprintErrorImageView;

    private ImageView mFingerprintImageView;
    private SoterFingerprintCanceller mCanceller = null;
    private Animation mFlashAnimation = null;
    private boolean isSupportSoter;
    private int brssi = Integer.MAX_VALUE;
    private int wrssi = Integer.MAX_VALUE;
    private String sign_id;
    private String pid;
    private String bluetoothmac;
    private ListView lv;
    ProgressDialog mProgressDialog;
    //    public static ProgressDialog show;
    private Dialog dialog;
    private RadarView radar;
    public Bitmap image;

    public static EmptyLayout emptyLayout;
    private MySwipeRefreshLayout mSwipeLayout;
    public static Handler mainHandler;

    /*
    30秒指纹自动关闭
     */
    private int count = 30;
    private AlertDialog.Builder openGps;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                mFingerprintDialog.setTitle(getString(xyz.hui_yi.R.string.use_fp_login) + " (" + count-- + ")");
                sendEmptyMessageDelayed(1, 1000);
            }
            if (msg.what == Constants.QIANDAO_SUCCESS) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
//                Log.i(TAG, "onResult: 签到成功语音");
                if(PrefUtils.getBoolean(getActivity(), Constants.VOIUME_OFF_ON_KEY,true)){
                    TTSUtils.speak(getContext(), "签到成功");
                }
//                TTSUtils.speak(getContext(), "签到成功", wrssi == Integer.MAX_VALUE ? brssi : wrssi);
                SnackBarUtils.show(lv, getString(xyz.hui_yi.R.string.text_login_success)+"，距离管理员"+WifiUtils.disByRssi(wrssi == Integer.MAX_VALUE ? brssi : wrssi) + "米");
//                Toast.makeText(getContext(), getString(R.string.text_login_success), Toast.LENGTH_SHORT).show();
                data.clear();
                String url = getResources().getString(xyz.hui_yi.R.string.URL);
                url = url + "/public/api/Meet/meetsignlist";
                new QListDownAsynctask(data, adapter, getContext(), handler).execute(url, userInfo.getUid());
                /*if (PrefUtils.getBoolean(getActivity(), VOIUME_OFF_ON_KEY, true)) {
                    //直接创建，不需要设置setDataSource
                    MediaPlayer mMediaPlayer;
                    mMediaPlayer = MediaPlayer.create(getContext(), R.raw.success);
                    mMediaPlayer.start();
                }*/
            }
            if (msg.what == Constants.SERVER_ERROR) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
                SnackBarUtils.show(lv, getString(xyz.hui_yi.R.string.text_server_error));
            }
            if(msg.what == Constants.PID_FAIL){
                SnackBarUtils.show(lv, "请用注册手指重试");
            }
            if (msg.what == Constants.QIANDAO_FAIL) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
//                TTSUtils.speak(getContext(), "签到未成功,请使用录入的手指重试");
                SnackBarUtils.show(lv, xyz.hui_yi.R.string.text_fp_login_failed);
                TTSUtils.speak(getContext(), getString(xyz.hui_yi.R.string.text_fp_login_failed));
            }
            if (msg.what == Constants.DATA_NUM) {
                int num = (int) msg.obj;
                if (num != 0) {
                    getActivity().setTitle(getActivity().getString(xyz.hui_yi.R.string.app_name) + "(" + num + ")");
                } else {
                    getActivity().setTitle(getActivity().getString(xyz.hui_yi.R.string.app_name));
                }
            }
        }
    };

    private void initViews() {
        lv = (ListView) inflate.findViewById(xyz.hui_yi.R.id.qdlv);
        emptyLayout = (EmptyLayout) inflate.findViewById(xyz.hui_yi.R.id.emptyLayout);
        mSwipeLayout = (MySwipeRefreshLayout) inflate.findViewById(xyz.hui_yi.R.id.swipe_ly);
        mSwipeLayout.setScrollUpChild(lv);
        mSwipeLayout.setColorSchemeResources(xyz.hui_yi.R.color.colorPrimary, xyz.hui_yi.R.color.colorYellow, xyz.hui_yi.R.color.colorAccent);
        mCustomFingerprintView = LayoutInflater.from(getContext()).inflate(xyz.hui_yi.R.layout.layout_fingerprint, null);
        mFingerprintStatusHintView = (TextView) mCustomFingerprintView.findViewById(xyz.hui_yi.R.id.error_hint_msg);
        mFingerprintErrorImageView = (ImageView) mCustomFingerprintView.findViewById(xyz.hui_yi.R.id.fp_ic_error);
        mFingerprintImageView = (ImageView) mCustomFingerprintView.findViewById(xyz.hui_yi.R.id.fp_ic);
        initDialog();
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

    private void initDialog() {
        View view = LayoutInflater.from(getContext()).inflate(xyz.hui_yi.R.layout.dialog_radar, null);
        radar = (RadarView) view.findViewById(xyz.hui_yi.R.id.radar);
        String uphotoPath = PrefUtils.getString(getActivity(), Constants.KEY_UPHOTO, "");
        if (uphotoPath == null || uphotoPath.equals("")) {
            Bitmap bmp = BitmapFactory.decodeResource(getResources(), xyz.hui_yi.R.drawable.image_uphoto_example);
            radar.mbmp = bmp;
        } else {
            Log.i(TAG, "prepareData: " + getActivity().getCacheDir());
            File file = new File(getContext().getCacheDir(), uphotoPath);
            Log.i(TAG, "prepareData: uppath" + uphotoPath);
            Log.i(TAG, "prepareData: " + file.length());
            if (file.exists() && file.length() > 0 && !uphotoPath.equals("")) {
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                Log.i(TAG, "prepareData: " + file.getAbsolutePath());
                radar.mbmp = bitmap;
                radar.bmp = bitmap;
            }
        }
        if (image != null) {
            Bitmap bitmap = BitmapUtils.makeRoundCorner(image);
            Log.i(TAG, "onItemClick: bitmap" + bitmap);
            radar.bmp = bitmap;
        }
        dialog = new Dialog(getContext(), xyz.hui_yi.R.style.NobackDialog);
        dialog.setContentView(view);
        Window window = dialog.getWindow();
        WindowManager m = getActivity().getWindowManager();
        Display d = m.getDefaultDisplay(); // 获取屏幕宽、高用
        WindowManager.LayoutParams p = window.getAttributes(); // 获取对话框当前的参数值
        p.height = (int) (d.getHeight() * 0.6); // 改变的是dialog框在屏幕中的位置而不是大小
        p.width = (int) (d.getWidth() * 0.8); // 宽度设置为屏幕的0.8
        window.setGravity(Gravity.BOTTOM | Gravity.CLIP_VERTICAL);
        window.setAttributes(p);
        dialog.setCancelable(false);
        dialog.setTitle(getString(xyz.hui_yi.R.string.text_search_now));
        dialog.setOnKeyListener(onKeyListener);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        inflate = inflater.inflate(xyz.hui_yi.R.layout.fragment_qiandao, container, false);
        initViews();
        prepareData();
        configLogic();
        return inflate;
    }

    private void configLogic() {
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //这里可以做一下下拉刷新的操作
                data.clear();
                emptyLayout.hide();
                String url = getResources().getString(xyz.hui_yi.R.string.URL);
                url = url + "/public/api/Meet/meetsignlist";
                new QListDownAsynctask(data, adapter, getContext(), handler).execute(url, userInfo.getUid());
                if (mSwipeLayout.isRefreshing()) {
                    //关闭刷新动画
                    mSwipeLayout.setRefreshing(false);
                }
            }
        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }

        if (MainActivity.isUser) {//签到用户
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    /*
                    先判断是否完成签到
                     */
                    ImageView imageView = (ImageView) view.findViewById(xyz.hui_yi.R.id.iv_type);
                    if (imageView.getDrawable().getCurrent().getConstantState()
                            == getResources().getDrawable(xyz.hui_yi.R.mipmap.ic_qiandao_ok).getConstantState()
                            ) {
                        Animation animation = AnimationUtils.loadAnimation(getContext(), xyz.hui_yi.R.anim.anim_refresh);
                        imageView.setAnimation(animation);
                        imageView.startAnimation(animation);
                        return;
                    }
                    ImageView uphoto = (ImageView) view.findViewById(xyz.hui_yi.R.id.iv_uphoto);
                    image = ((BitmapDrawable) uphoto.getDrawable()).getBitmap();
                    TextView tv_bluetoothmac = (TextView) view.findViewById(xyz.hui_yi.R.id.tv_bluetoothmac);
                    TextView tv_wlanmac = (TextView) view.findViewById(xyz.hui_yi.R.id.tv_wlanmac);
                    TextView tv_sign_id = (TextView) view.findViewById(xyz.hui_yi.R.id.tv_sign_id);
                    bluetoothmac = tv_bluetoothmac.getText().toString();
                    String wlanmac = tv_wlanmac.getText().toString();
                    sign_id = tv_sign_id.getText().toString();
                    if (bluetoothmac.equals("") && !wlanmac.equals("")) {
                        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) && !GPSUtils.isOPen(getContext())) {
                            openGps.show();
                            if (dialog != null && dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            return;
                        }
                        WifiManager wifi = (WifiManager) getContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                        if (!wifi.isWifiEnabled()) {
                            if (wifi.getWifiState() != WifiManager.WIFI_STATE_ENABLING) {
                                wifi.setWifiEnabled(true);
                                return;
                            }
                        }
                        wrssi = WifiUtils.openWifi(getActivity(), wlanmac, wlanmac.toLowerCase());
                        String connectedWifiMacAddress = WifiUtils.getConnectedWifiMacAddress(getActivity().getApplicationContext());
                        if (connectedWifiMacAddress == null) {
                            AskForPermission();
                            return;
                        }
                        if (wifi.isWifiEnabled()) {
                            initDialog();
                            dialog.show();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (dialog != null && dialog.isShowing()) {
                                        dialog.dismiss();
                                        Toast.makeText(getContext(),"未搜索到管理员设备，请靠近会场后重试",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }, 30000);
                        } else {
                            return;
                        }
                        Log.i(TAG, "onItemClick: " + wrssi);
                        Log.i(TAG, "onItemClick: + wlanmac " + wlanmac + " " + wlanmac.toLowerCase());
                        if (wrssi != Integer.MAX_VALUE) {
                            VibrateUtils.Vibrate(getContext(), 500);
                            if (dialog != null && dialog.isShowing()) {
                                dialog.dismiss();
                            }
                            faceOrFp();
                        }
                    } else if (!bluetoothmac.equals("") && wlanmac.equals("")) {
                        BluetoothUtils.openBluetooth(getActivity(), mReceiver);
                        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                        if (mBluetoothAdapter.isEnabled()) {
                            initDialog();
                            dialog.show();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (dialog != null && dialog.isShowing()) {
                                        dialog.dismiss();
                                       Toast.makeText(getContext(),"未搜索到管理员设备，请靠近会场后重试",Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }, 20000);
                        }
                    }
                }
            });
        } else {//管理员
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, final View view, int position, long id) {
                    //TODO 打开会议管理页面
                    ImageView imageView = (ImageView) view.findViewById(xyz.hui_yi.R.id.iv_type);
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()).setTitle("签到前请开启签到设备")
                            .setPositiveButton("我已打开", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    TextView tv_mid = (TextView) view.findViewById(xyz.hui_yi.R.id.tv_mid);
                                    Intent intent = new Intent(getContext(), AdmDataManageActivity.class);
                                    intent.putExtra("mid", tv_mid.getText().toString());
                                    startActivity(intent);
                                }
                            });
                    if (imageView.getDrawable().getCurrent().getConstantState()
                            == getResources().getDrawable(xyz.hui_yi.R.mipmap.ic_type_wifi).getConstantState()
                            ) {
                        Animation animation = AnimationUtils.loadAnimation(getContext(), xyz.hui_yi.R.anim.anim_refresh);
                        imageView.setAnimation(animation);
                        imageView.startAnimation(animation);
                        builder.setIcon(xyz.hui_yi.R.mipmap.ic_type_wifi)
                                .setMessage("请打开创建会议时使用的设备wifi或热点开始签到，" +
                                        "不在您设备范围内将无法完成签到")
                                .show();
                    } else {
                        builder.setIcon(xyz.hui_yi.R.mipmap.ic_type_bluetooth)
                                .setMessage("请打开创建会议时使用的设备蓝牙开始签到，" +
                                        "不在您设备范围内将无法完成签到")
                                .show();
                    }
                }
            });
        }
    }

    private void faceOrFp() {
        if (isSupportSoter) {
            Log.i(TAG, "faceOrFp: fp");
            doUseFingerprintPayment();
        } else {
            Log.i(TAG, "faceOrFp: face");
            peopleGet();
        }
    }

    private void peopleGet() {
        if (StringUtils.isBlank(userInfo.getBname() +"_"+ userInfo.getUid())) {
            ToastUtils.show(getActivity(), xyz.hui_yi.R.string.text_name_not_empty);
            return;
        }
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
        mProgressDialog.setMessage(CustomUtil.getString(getActivity(), xyz.hui_yi.R.string.text_test));
        mProgressDialog.show();
        Call<PeopleGet> call = CheckAPI.peopleGet(userInfo.getBname() +"_"+ userInfo.getUid());
        call.enqueue(new Callback<PeopleGet>() {

            public void onFinish() {
                mProgressDialog.dismiss();
            }

            @Override
            public void onResponse(Call<PeopleGet> call, Response<PeopleGet> response) {
                onFinish();
                handleCheckData(response.body());
            }

            @Override
            public void onFailure(Call<PeopleGet> call, Throwable t) {
                onFinish();
                ToastUtils.show(getActivity(), xyz.hui_yi.R.string.toast_network_error);
            }
        });
    }

    private void handleCheckData(PeopleGet data) {
        Logs.d(TAG, data == null ? "认证失败" : data.toString());
        handleVerify(data);
    }

    private void handleVerify(PeopleGet data) {
        if (data == null) {
            SnackBarUtils.show(mFingerprintStatusHintView, xyz.hui_yi.R.string.toast_verify_no_id);
            return;
        }
        if (data.getRes_code() != null && Constant.RES_CODE_1025.equals(data.getRes_code())) {
            SnackBarUtils.show(lv, xyz.hui_yi.R.string.text_user_unregister);
        } else if (StringUtils.isEquals(data.getRes_code(), Constant.RES_CODE_0000) && data.getFace_count() > 0) {
            boolean b = havePrem();
            if(!b){
                return;
            }
            Intent intent = new Intent(getContext(), FaceVerifyActivity.class);
            intent.putExtra(FaceVerifyActivity.ARG_CAMERA_ID, 1);
            intent.putExtra(FaceVerifyActivity.ARG_UID, uid);
            intent.putExtra(FaceVerifyActivity.ARG_MID, sign_id);
            intent.putExtra(FaceVerifyActivity.ARG_BNAME, userInfo.getBname());
            if (wrssi != Integer.MAX_VALUE) {
                intent.putExtra(FaceVerifyActivity.ARG_RSSI, wrssi + "");
            } else {
                intent.putExtra(FaceVerifyActivity.ARG_RSSI, brssi + "");
            }
            Log.i(TAG, "handleVerify: wrssi" + wrssi);
            startActivity(intent);
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        } else if (StringUtils.isEquals(data.getRes_code(), Constant.RES_CODE_0000) && data.getFace_count() == 0) {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            SnackBarUtils.show(lv, xyz.hui_yi.R.string.text_user_unregister);
        } else {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
            SnackBarUtils.show(lv, xyz.hui_yi.R.string.text_verify_failed);
        }
    }

    private boolean havePrem() {
        //调用相机权限判定
        if (CameraUtils.isCameraCanUse()) {
            return true;
        } else {
            ToastUtils.show(getContext(), "没相机权限，请到应用程序权限管理开启权限");
            //跳转至app设置
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("需要开启相机权限，不开启将无法完成签到");
            builder.setIcon(xyz.hui_yi.R.mipmap.ic_dialog_gps);

            builder.setPositiveButton("去开启", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    CameraUtils.getAppDetailSettingIntent(getContext());
                }
            });
            builder.create().show();
        }
        return false;
    }

    private DialogInterface.OnKeyListener onKeyListener = new DialogInterface.OnKeyListener() {
        @Override
        public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
            return false;
        }
    };

    //定义广播接收
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e("ywq", action);
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {    //显示已配对设备
                    if (device.getAddress().equals(bluetoothmac)) {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        faceOrFp();
                        //信号强度。
                        brssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);
                        // 震动0.5秒
                        VibrateUtils.Vibrate(getContext(), 500);
                        Log.i(TAG, "onReceive: brssi" + brssi);
                    }
                } else if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                } else {
                    if (device.getAddress().equals(bluetoothmac)) {
                        if (dialog != null && dialog.isShowing()) {
                            dialog.dismiss();
                        }
                        faceOrFp();
                        brssi = intent.getExtras().getShort(
                                BluetoothDevice.EXTRA_RSSI);
                        // 震动0.5秒
                        VibrateUtils.Vibrate(getContext(), 500);
                        Log.i(TAG, "onReceive: brssi" + brssi);
                    }
                }
                if (device.getAddress().equals(bluetoothmac)) {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    faceOrFp();
                    // 震动0.5秒
                    VibrateUtils.Vibrate(getContext(), 500);
                    brssi = intent.getExtras().getShort(
                            BluetoothDevice.EXTRA_RSSI);
                    Log.i(TAG, "onReceive: brssi" + brssi);
                }
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                if (dialog != null && dialog.isShowing()) {
                    dialog.dismiss();
                }
            }
        }
    };

    private void prepareData() {
        TTSUtils.asnyncInitTTs(getContext(),mainHandler);
        data = new ArrayList<>();
        adapter = new QianDaoListAdapter(data, getActivity());

        //给listview设置一个底部view(必须在设置数据之前)
        /*footView = View.inflate(getContext(), R.layout.footer, null);
        lv.addFooterView(footView);
*/
        //给listview设置适配器
        lv.setAdapter(adapter);
        setHasOptionsMenu(true);
        isSupportSoter = PrefUtils.getBoolean(getActivity(), Constants.KEY_SOTER_SUPPORTED, false);
        mFlashAnimation = AnimationUtils.loadAnimation(getContext(), xyz.hui_yi.R.anim.anim_flash);
        mProgressDialog = new ProgressDialog(getContext());
        //TODO 联网异步获取数据
        es = Executors.newFixedThreadPool(10);
        String url = getResources().getString(xyz.hui_yi.R.string.URL);
        url = url + "/public/api/Meet/meetsignlist";
        new QListDownAsynctask(data, adapter, getContext(), handler).executeOnExecutor(es, url, userInfo.getUid());
        openGps = new AlertDialog.Builder(getActivity()).setTitle("检测到您未打开GPS服务")
                .setMessage("签到前首先要打开GPS服务，您可以签到成功后关闭该服务")
                .setIcon(xyz.hui_yi.R.mipmap.ic_dialog_gps)
                .setPositiveButton("去打开", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        GPSUtils.openGps(getActivity());
                    }
                });
    }

    private void doUseFingerprintPayment() {
//        Log.i(TAG, "soterdemo: 用户请求使用指纹签到");
        startFingerprintAuthentication(new SoterProcessCallback<SoterProcessAuthenticationResult>() {
            @Override
            public void onResult(@NonNull SoterProcessAuthenticationResult result) {
//                Log.d(TAG, "soterdemo: 使用指纹签到结果:\n %s \n, 签名数据是:\n %s\n" + result.toString() + result.getExtData() != null ? result.getExtData().toString() : null);
                dismissLoading();
                if (result.isSuccess()) {// 验证条件
                    pid = result.getExtData().getFid();
                    //TODO 联网pid是否匹配代替下面if uid pid rssi/wssi
                    String url = getResources().getString(xyz.hui_yi.R.string.URL);
                    url = url + "/public/api/Meet/uMeetSign";
                    new QianDaoHttpThread(handler, url, userInfo.getUid(), sign_id,"", pid, (wrssi == Integer.MAX_VALUE ? brssi : wrssi) + "").start();
                } else {
                    // 先判断是否是指纹密钥失效。如果指纹失效，则重新生成并上传authkey，然后直接使用密码签到
                    if (result.errCode == SoterProcessErrCode.ERR_AUTHKEY_NOT_FOUND
                            || result.errCode == SoterProcessErrCode.ERR_AUTHKEY_ALREADY_EXPIRED || result.errCode == SoterProcessErrCode.ERR_ASK_NOT_EXIST) {
//                        Log.w(TAG, "soterdemo: 授权密钥过期或找不到密钥。 再生和上传");
//                        Toast.makeText(getContext(), "authkey已过期或未找到。 开始重新生成",
//                                Toast.LENGTH_SHORT).show();
                        startPrepareAuthKeyAndAuthenticate();
                        SnackBarUtils.show(lv, xyz.hui_yi.R.string.text_not_soter_key);
                        return;
                    } else if (result.errCode == SoterProcessErrCode.ERR_USER_CANCELLED) {
//                        Log.i(TAG, "soterdemo: 用户取消了认证");
                        Toast.makeText(getContext(), "用户取消了签到", Toast.LENGTH_SHORT).show();
                    } else if (result.errCode == SoterProcessErrCode.ERR_FINGERPRINT_LOCKED) {
//                        Log.i(TAG, "soterdemo: 指纹传感器由于尝试次数过多而被锁定");
                        mFingerprintImageView.setImageResource(xyz.hui_yi.R.drawable.ic_fingerprint_error);
                        SnackBarUtils.show(lv, "指纹传感器由于尝试次数过多而被锁定");
//                        TTSUtils.speak(getContext(), "指纹传感器由于尝试次数过多而被锁定,请稍后重试");
                    } else {
//                        Log.w(TAG, "soterdemo: doUseFingerprintPayment中的未知错误: %d" + result.errCode);
//                        Toast.makeText(getContext(), "签到错误。 检查日志以获取更多信息。 回退到正常",
//                                Toast.LENGTH_SHORT).show();
//                        TTSUtils.speak(getContext(), "签到未成功,请用录入的手指重试");
                        SnackBarUtils.show(lv, xyz.hui_yi.R.string.text_fp_login_failed);
                    }
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
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
//                            Log.i(TAG, "soterdemo: 打开完成: result:\n %s, \n签名数据 : \n%s\n" + result.toString() + result.getExtData() != null ? result.getExtData().toString() : null);
                            if (result.errCode == SoterProcessErrCode.ERR_NO_FINGERPRINT_ENROLLED) {
                                dismissLoading();
                                return;
                            }
                            if (result.isSuccess()) {
                                //TODO 联网pid是否匹配代替下面if uid pid rssi/wssi
                                String url = getResources().getString(xyz.hui_yi.R.string.URL);
                                url = url + "/public/api/Meet/uMeetSign";
                                pid = result.getExtData().getFid().toString();
                                new QianDaoHttpThread(handler, url, userInfo.getUid(), sign_id,"", pid, (wrssi == Integer.MAX_VALUE ? brssi : wrssi) + "").start();
                                dismissLoading();
                            } else {
                                SnackBarUtils.show(lv, "请用注册时录入手指重试");
                            }
                        }
                    }, getString(xyz.hui_yi.R.string.text_login_fp));
                } else {
//                    Log.w(TAG, "soterdemo: 生成授权密钥失败！");
                    dismissLoading();
                    Toast.makeText(getContext(), getString(xyz.hui_yi.R.string.app_auth_key_prepare_failed), Toast.LENGTH_LONG).show();
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
//                Log.i(TAG, "soterdemo: 准备结果:\n %s\n, 授权关键结果:\n %s\n" + result + result.getExtData() != null ? result.getExtData().toString() : null);
                if (result.errCode == SoterProcessErrCode.ERR_OK) {
                    if (callback != null) {
                        callback.onResult(pwdDigest, true);
                    }
                } else {
                    if (result.errCode == SoterProcessErrCode.ERR_NO_FINGERPRINT_ENROLLED) {
                        SnackBarUtils.show(lv, xyz.hui_yi.R.string.text_system_not_fp);
                    }
                    if (result.errCode == 2) {
                        PrefUtils.setBoolean(getActivity(), Constants.KEY_SOTER_SUPPORTED, false);
                        Toast.makeText(getContext(), "请重启软件后重试", Toast.LENGTH_SHORT).show();
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
                .setContext(getContext()) // 指定当前上下文。必填。
                .setFingerprintCanceller(mCanceller) // 指定当前用于控制指纹取消的控制器。当因为用户退出界面或者进行其他可能引起取消的操作时，需要开发者通过该控制器取消指纹授权。建议必填。
//                .setIWrapGetChallengeStr(new RemoteGetChallengeStr()) // 用于获取挑战因子的网络封装结构体。如果在授权之前已经通过其他模块拿到后台挑战因子，则可以改为调用setPrefilledChallenge。如果两个方法都没有调用，则会引起错误。
                .setPrefilledChallenge(Constants.STR_CHALLENGE) // 如果之前已经通过其他方式获取了挑战因子，则设置此字段。如果设置了该字段，则忽略获取挑战因子网络封装结构体的设置。如果两个方法都没有调用，则会引起错误。
//                .setIWrapUploadSignature(uploadSignatureWrapper) // 用于上传最终结果的网络封装结构体。该结构体一般来说不独立存在，而是集成在最终授权网络请求中，该请求实现相关接口即可。选填，如果没有填写该字段，则要求应用方自行上传该请求返回字段。
                .setSoterFingerprintStateCallback(new SoterFingerprintStateCallback() { // 指纹回调仅仅用来更新UI相关，不建议在指纹回调中进行任何业务操作。选填。

                    // 指纹回调仅仅用来更新UI相关，不建议在指纹回调中进行任何业务操作
                    // Fingerprint state callbacks are only used for updating UI. Any logic operation is not welcomed.
                    @Override
                    public void onStartAuthentication() {
//                        Log.d(TAG, "soterdemo: 开始认证。 解除加载");
                        dismissLoading();
                        showFingerprintDialog(title);
                    }

                    @Override
                    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
//                        Log.w(TAG, "soterdemo: onAuthenticationHelp: %d, %s" + helpCode + helpString);
                        // 由于厂商实现不同，不建议在onAuthenticationHelp中做任何操作。
                    }

                    @Override
                    public void onAuthenticationSucceed() {
//                        Log.d(TAG, "soterdemo: onAuthenticationSucceed");
                        mCanceller = null;
                        // 可以在这里做相应的UI操作
                        showLoading(getString(xyz.hui_yi.R.string.app_verifying));
                        dismissCurrentDialog();
                    }

                    @Override
                    public void onAuthenticationFailed() {
//                        Log.w(TAG, "soterdemo: onAuthenticationFailed once:");
                        setFingerprintHintMsg(getString(xyz.hui_yi.R.string.fingerprint_normal_hint), true);
                    }

                    @Override
                    public void onAuthenticationCancelled() {
//                        Log.d(TAG, "soterdemo: user cancelled authentication");
                        mCanceller = null;
                        dismissCurrentDialog();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errorString) {
//                        Log.w(TAG, "soterdemo: onAuthenticationError: %d, %s" + errorCode + errorString);
                        mCanceller = null;
                        Toast.makeText(getContext(), errorString, Toast.LENGTH_LONG).show();
                        dismissCurrentDialog();
                    }
                }).build();
        SoterWrapperApi.requestAuthorizeAndSign(processCallback, param);
    }

    private void showFingerprintDialog(String title) {
        if (mFingerprintDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext()).setTitle(title).setCancelable(true)
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
            //30秒后指纹关闭
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    cancelFingerprintAuthentication();
                    dismissCurrentDialog();
                    handler.removeCallbacksAndMessages(null);
                    count = 30;
                }
            }, 30000);
        } else {
            setFingerprintHintMsg("", false);
            mFingerprintDialog.setTitle(title);
        }
        mFingerprintDialog.show();
        //计时开始
        handler.sendEmptyMessage(1);
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
        handler.removeCallbacksAndMessages(null);
        count = 30;
        if (mPasswordDialog != null && mPasswordDialog.isShowing()) {
            mPasswordDialog.dismiss();
        }
        if (mFingerprintDialog != null && mFingerprintDialog.isShowing()) {
            mFingerprintDialog.dismiss();
        }
    }

    @Override
    public void onPause() {
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

    private void showLoading(String wording) {
        if (mLoadingDialog == null) {
            mLoadingDialog = ProgressDialog.show(getContext(), "", wording, true, false, null);
        } else if (!mLoadingDialog.isShowing()) {
            mLoadingDialog.setMessage(wording);
            mLoadingDialog.show();
        } else {
//            Log.d(TAG, "soterdemo: 已经显示。 只改变标题");
            mLoadingDialog.setMessage(wording);
        }
    }

    private void dismissLoading() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case xyz.hui_yi.R.id.action_refresh:
                //TODO 联网刷新
                mSwipeLayout.setRefreshing(true);
                data.clear();
                String url = getResources().getString(xyz.hui_yi.R.string.URL);
                url = url + "/public/api/Meet/meetsignlist";
                new QListDownAsynctask(data, adapter, getContext(), handler).execute(url, userInfo.getUid());
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        if (mSwipeLayout.isRefreshing()) {
                            //关闭刷新动画
                            mSwipeLayout.setRefreshing(false);
                        }
                    }
                }, 2000);
                emptyLayout.hide();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(xyz.hui_yi.R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private interface IOnAuthKeyPrepared {
        void onResult(String passwordDigestUsed, boolean isSuccess);
    }

    private void AskForPermission() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("需要定位权限，不开启将无法完成签到");
        builder.setIcon(xyz.hui_yi.R.mipmap.ic_dialog_gps);
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setData(Uri.parse("package:" + getActivity().getPackageName())); // 根据包名打开对应的设置界面
                startActivity(intent);
            }
        });
        builder.create().show();
    }
}
