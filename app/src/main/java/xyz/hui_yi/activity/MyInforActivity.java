package xyz.hui_yi.activity;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import xyz.hui_yi.R;
import xyz.hui_yi.face.utils.CustomUtil;
import xyz.hui_yi.face.utils.Logs;
import xyz.hui_yi.face.utils.StringUtils;
import xyz.hui_yi.face.utils.ToastUtils;
import xyz.hui_yi.facesdk.api.CheckAPI;
import xyz.hui_yi.facesdk.conn.Constant;
import xyz.hui_yi.facesdk.entity.PeopleGet;
import xyz.hui_yi.utils.SnackBarUtils.SnackBarUtils;


/**
 * Created by LiFen on 2018/3/5.
 * 我的个人信息界面
 * 1.人脸信息
 * 2.退出登录
 */

public class MyInforActivity extends AppCompatActivity {
    private static final String TAG = "MyInforActivity";
    private static final int CAMERA_OK = 1;
    private String [] wodeitems;
    private ListView lv;
    private int[] logo;
    private int mCameraId = 1;
    private ProgressDialog mProgressDialog;
    private boolean created;
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myinfor);
        initView();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) wodeitems[position];
                Log.d("ListView","Selected item: " + item);
                if(position == 0){
                    peopleGet();
                    return;
                }
                if(position == 1){
//                    PrefUtils.setBoolean(getApplication(), Constants.KEY_USER_ISCHECK,false);
//                    PrefUtils.setBoolean(getApplication(),Constants.KEY_ADM_ISCHECK,false);
                    Intent intent = new Intent(getApplicationContext(),
                            UserLoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            }
        });
    }

    private void initView() {
        lv = (ListView) findViewById(R.id.lv_setting);
        MyAdapter myAdapter = new MyAdapter();
        wodeitems = getResources().getStringArray(R.array.myinforitem);
        logo = new int[]{R.mipmap.ic_setface, R.mipmap.ic_key};
        lv.setAdapter(myAdapter);
        mProgressDialog = new ProgressDialog(this);
    }

    private void peopleGet() {
        if (StringUtils.isBlank(MainActivity.userInfo.getBname()+ "_" + MainActivity.userInfo.getUid())) {
            ToastUtils.show(this, R.string.text_name_not_empty);
            return;
        }
        mProgressDialog.setMessage(CustomUtil.getString(getApplicationContext(), R.string.text_test));
        mProgressDialog.show();
        Call<PeopleGet> call = CheckAPI.peopleGet(MainActivity.userInfo.getBname()+"_"+ MainActivity.userInfo.getUid());
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
                ToastUtils.show(getApplicationContext(), R.string.toast_network_error);
            }
        });
    }

    private void handleCheckData(PeopleGet data) {
        Logs.d(TAG, data == null ? "认证失败" : data.toString());
        handleRegister(data);
    }

    private void handleRegister(PeopleGet data) {
        if (data == null) {
            SnackBarUtils.show(lv, R.string.text_enroll);
            return;
        }
        if (data.getRes_code() != null && Constant.RES_CODE_1025.equals(data.getRes_code())) {
            startCaptureActivity(false);
        } else if (StringUtils.isEquals(data.getRes_code(), Constant.RES_CODE_0000) && data.getFace_count() > 0) {
            SnackBarUtils.show(lv, R.string.text_user_exist);
//            finish();
        } else if (StringUtils.isEquals(data.getRes_code(), Constant.RES_CODE_0000) && data.getFace_count() == 0) {
            startCaptureActivity(true);
        } else {
            SnackBarUtils.show(lv, R.string.toast_enroll_failed);
        }
    }

    private void startCaptureActivity(boolean created) {
        this.created = created;
        requestPermission();
    }

    private class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return wodeitems.length;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null){
                view = View.inflate(MyInforActivity.this, R.layout.item3,null);
            }else{
                view = convertView;
            }
            ImageView iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
            TextView tv_title = (TextView) view.findViewById(R.id.tv_mtitle);

            iv_icon.setImageResource(logo[position]);
            tv_title.setText(wodeitems[position]);
            return view;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // 第一次请求权限时，用户如果拒绝，下一次请求shouldShowRequestPermissionRationale()返回true
            // 向用户解释为什么需要这个权限
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                new AlertDialog.Builder(this)
                        .setMessage("录入人脸信息需要申请相机权限")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //申请相机权限
                                ActivityCompat.requestPermissions(MyInforActivity.this,
                                        new String[]{Manifest.permission.CAMERA}, CAMERA_OK);
                            }
                        })
                        .show();
            } else {
                //申请相机权限
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA}, CAMERA_OK);
            }
        } else {
            Log.i(TAG, "requestPermission: " + "相机权限已申请");
//            tvPermissionStatus.setText();
            Intent intent = new Intent(this, FaceRegActivity.class);
            intent.putExtra(FaceRegActivity.ARG_CAMERA_ID, mCameraId);
            intent.putExtra(FaceRegActivity.ARG_UID, MainActivity.userInfo.getUid());
            intent.putExtra(FaceRegActivity.ARG_BNAME, MainActivity.userInfo.getBname());
            intent.putExtra(FaceRegActivity.ARG_IS_CREATED, created);
            startActivity(intent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_OK) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(this, FaceRegActivity.class);
                intent.putExtra(FaceRegActivity.ARG_CAMERA_ID, mCameraId);
                intent.putExtra(FaceRegActivity.ARG_UID, MainActivity.userInfo.getUid());
                intent.putExtra(FaceRegActivity.ARG_BNAME, MainActivity.userInfo.getBname());
                intent.putExtra(FaceRegActivity.ARG_IS_CREATED, created);
                startActivity(intent);
            } else {
                //用户勾选了不再询问
                //提示用户手动打开权限
                if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                    Toast.makeText(this, "相机权限已被禁止,请在设置打开", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
