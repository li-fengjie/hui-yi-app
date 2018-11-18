package xyz.hui_yi.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import xyz.hui_yi.net.AdmLoginHttpThread;
import xyz.hui_yi.utils.PrefUtils.PrefUtils;
import xyz.hui_yi.utils.SnackBarUtils.SnackBarUtils;
import xyz.hui_yi.constants.Constants;


/**
 * Created by LiFen on 20110/1/24.
 * 管理员登陆界面
 */

public class AdmLoginActivity extends AppCompatActivity implements SeekBar.OnSeekBarChangeListener {
    private TextView tv_admreg;
    private TextView tv;
    private SeekBar seekBar;
    private EditText et_admuid;
    private EditText et_admpwd;
    private EditText et_code;
    private ImageView iv_eye_code;
    private ImageView iv_eye_pwd;
    private CheckBox cb_ischeck;
    private String admUid;
    private String admCode;

    private String admPwd;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.ADM_LOGIN_SUCCESS:
                    seekBar.setEnabled(false);
                    if (cb_ischeck.isChecked()) {
                        admUid = et_admuid.getText().toString();
                        admPwd = et_admpwd.getText().toString();
                        admCode = et_code.getText().toString();
                        //获取sp的编辑器
                        PrefUtils.setString(getApplication(), Constants.KEY_ADM_UID, admUid);
                        PrefUtils.setString(getApplication(), Constants.KEY_ADM_PWD, admPwd);
                        PrefUtils.setString(getApplication(), Constants.KEY_PCODE, admCode);
                        PrefUtils.setBoolean(getApplication(), Constants.KEY_ADM_ISCHECK, true);
                    } else {
                        PrefUtils.setString(getApplication(), Constants.KEY_ADM_PWD, "");
                        PrefUtils.setString(getApplication(), Constants.KEY_PCODE, "");
                        PrefUtils.setBoolean(getApplication(), Constants.KEY_ADM_ISCHECK, false);
                    }
                    Intent intent = new Intent(AdmLoginActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.putExtra(Constants.ISUSER, false);
                    intent.putExtra(Constants.UID, admUid);
                    startActivity(intent);
                    Toast.makeText(AdmLoginActivity.this, getString(xyz.hui_yi.R.string.text_welcome_back), Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case Constants.LOGIN_PCODE_FAIL:
                    seekBar.setProgress(10);
                    SnackBarUtils.show(et_code, xyz.hui_yi.R.string.text_wrong_pcode);
                    et_code.setText("");
                    break;
                case Constants.LOGIN_FAIL:
                    seekBar.setProgress(10);
                    SnackBarUtils.show(et_admpwd, xyz.hui_yi.R.string.text_wrong_pwd);
                    et_admpwd.setText("");
                    et_code.setText("");
                    break;
                case Constants.SERVER_ERROR:
                    SnackBarUtils.show(et_admpwd, xyz.hui_yi.R.string.text_server_error);
                    seekBar.setProgress(10);

                    //TODO test
                    Intent intent1 = new Intent(AdmLoginActivity.this, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent1.putExtra(Constants.ISUSER, false);
                    intent1.putExtra(Constants.UID, admUid);
                    startActivity(intent1);
                    Toast.makeText(AdmLoginActivity.this, getString(xyz.hui_yi.R.string.text_welcome_back), Toast.LENGTH_SHORT).show();
                    finish();

                    break;
                case Constants.NET_ERROR:
                    SnackBarUtils.show(et_admpwd, xyz.hui_yi.R.string.toast_network_error);
                    seekBar.setProgress(10);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(xyz.hui_yi.R.layout.activity_admlogin);

        initView();
        prepareData();

        seekBar.setOnSeekBarChangeListener(this);
        tv_admreg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdmLoginActivity.this, AdmRegActivity.class);
                startActivity(intent);
            }
        });
        iv_eye_code.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    //按下
                    et_code.setTransformationMethod(HideReturnsTransformationMethod
                            .getInstance());
                }else if(event.getAction() == MotionEvent.ACTION_UP){
                    //抬起
                    et_code.setTransformationMethod(PasswordTransformationMethod
                            .getInstance());
                }
                return false;
            }
        });
        iv_eye_pwd.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    //按下
                    et_admpwd.setTransformationMethod(HideReturnsTransformationMethod
                            .getInstance());
                }else if(event.getAction() == MotionEvent.ACTION_UP){
                    //抬起
                    et_admpwd.setTransformationMethod(PasswordTransformationMethod
                            .getInstance());
                }
                return false;
            }
        });
    }

    private void initView() {
        tv_admreg = (TextView) findViewById(xyz.hui_yi.R.id.tv_admreg);
        tv = (TextView) findViewById(xyz.hui_yi.R.id.tv);
        seekBar = (SeekBar) findViewById(xyz.hui_yi.R.id.sb);
        et_admuid = (EditText) findViewById(xyz.hui_yi.R.id.et_mtitle);
        et_admpwd = (EditText) findViewById(xyz.hui_yi.R.id.et_admpwd1);
        et_code = (EditText) findViewById(xyz.hui_yi.R.id.et_code);
        cb_ischeck = (CheckBox) findViewById(xyz.hui_yi.R.id.cb_ischeck);
        cb_ischeck.setChecked(true);
        iv_eye_code = (ImageView)findViewById(xyz.hui_yi.R.id.iv_eye);
        iv_eye_pwd = (ImageView) findViewById(xyz.hui_yi.R.id.iv_eye_pwd);
    }

    private void prepareData() {
        String n = PrefUtils.getString(getApplication(), Constants.KEY_ADM_UID, "");
        String pwd = PrefUtils.getString(getApplication(), Constants.KEY_ADM_PWD, "");
        boolean ischeck = PrefUtils.getBoolean(getApplication(), Constants.KEY_ADM_ISCHECK, true);
        String pcode = PrefUtils.getString(getApplication(), Constants.KEY_PCODE, "");
        et_admuid.setText(n);
        et_admpwd.setText(pwd);
        et_code.setText(pcode);
        cb_ischeck.setChecked(ischeck);
        seekBar.setPadding(0, 0, 0, 0);
    }

    /**
     * seekBar进度变化时回调
     *
     * @param seekBar
     * @param progress
     * @param fromUser
     */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (seekBar.getProgress() >= seekBar.getMax() - 8) {
            tv.setVisibility(View.VISIBLE);
            tv.setTextColor(Color.WHITE);
            tv.setText(getString(xyz.hui_yi.R.string.text_login));
        } else {
            tv.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * seekBar开始触摸时回调
     *
     * @param seekBar
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        admUid = et_admuid.getText().toString();
        admPwd = et_admpwd.getText().toString();
        admCode = et_code.getText().toString();
        if (admUid.equals("") || admPwd.equals("") || admCode.equals("")) {
            SnackBarUtils.show(et_admuid, xyz.hui_yi.R.string.text_info_not_empty);
            seekBar.setProgress(10);
            return;
        }
    }

    /**
     * seekBar停止触摸时回调
     *
     * @param seekBar
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (seekBar.getProgress() < seekBar.getMax() - 10) {
            seekBar.setProgress(10);
            tv.setVisibility(View.VISIBLE);
            tv.setTextColor(Color.GRAY);
            tv.setText(getString(xyz.hui_yi.R.string.text_thumb));
        } else {
            admUid = et_admuid.getText().toString();
            admPwd = et_admpwd.getText().toString();
            admCode = et_code.getText().toString();

            if (admUid.equals("") || admPwd.equals("") || admCode.equals("")) {
                SnackBarUtils.show(et_admuid, xyz.hui_yi.R.string.text_info_not_empty);
                seekBar.setProgress(10);
                return;
            }
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    //TODO 联网登陆
                    String url = getResources().getString(xyz.hui_yi.R.string.URL);
                    url = url + "/public/api/User/login";
                    new AdmLoginHttpThread(mHandler, url, admUid, admPwd, admCode).start();
                }
            }, 50);
        }
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
}
