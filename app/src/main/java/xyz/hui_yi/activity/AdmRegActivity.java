package xyz.hui_yi.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import xyz.hui_yi.R;
import xyz.hui_yi.net.AdmRegHttpThread;
import xyz.hui_yi.utils.SnackBarUtils.SnackBarUtils;

import static xyz.hui_yi.constants.Constants.NET_ERROR;
import static xyz.hui_yi.constants.Constants.REG_PCODE_NOT_EXISTS;
import static xyz.hui_yi.constants.Constants.REG_SUCCESS;
import static xyz.hui_yi.constants.Constants.REG_UID_EXISTS;
import static xyz.hui_yi.constants.Constants.SERVER_ERROR;


/**
 * Created by LiFen on 2018/1/24.
 * 管理员注册界面
 */

public class AdmRegActivity extends AppCompatActivity {

    private static final String TAG = "AdmRegActivity";
    private EditText et_admname;
    private EditText et_admphone;
    private EditText et_admpwd1;
    private EditText et_admpwd2;
    private Button btn_admreg;
    private EditText et_admuid;
    private EditText et_code;
    private String uid;
    private String phone;
    private String pwd1;
    private String pwd2;
    private String code;
    private String name;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case REG_SUCCESS:
                    Toast.makeText(AdmRegActivity.this,"注册成功",Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case REG_PCODE_NOT_EXISTS:
                    SnackBarUtils.show(et_code, "授权码无效，请重新输入");
                    et_code.setText("");
                    break;
                case REG_UID_EXISTS:
                    SnackBarUtils.show(et_admuid, R.string.text_user_exist);
                    break;
                case SERVER_ERROR:
                    SnackBarUtils.show(et_admuid, R.string.text_server_error);
                    break;
                case NET_ERROR:
                    SnackBarUtils.show(et_admuid, R.string.toast_network_error);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admreg);

        initView();
        configLogic();
    }

    private void configLogic() {
        btn_admreg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uid = et_admuid.getText().toString();
                pwd1 = et_admpwd1.getText().toString();
                pwd2 = et_admpwd2.getText().toString();
                name = et_admname.getText().toString();
                code = et_code.getText().toString();
                phone = et_admphone.getText().toString();

                if(uid.equals("")){
                    SnackBarUtils.show(et_admuid, R.string.text_name_not_empty);
                    return;
                }
                if(code.equals("")){
                    SnackBarUtils.show(et_admuid, R.string.text_no_code);
                    return;
                }
                if(!pwd1.equals(pwd2)){
                    SnackBarUtils.show(et_admpwd1, R.string.toast_pwd_notsame);
                    return;
                }
                if(name.equals("") || phone.equals("")){
                    SnackBarUtils.show(et_admname, R.string.text_info_not_empty);
                    return;
                }
                //TODO 联网管理员注册
                String url = getResources().getString(R.string.URL);
                url = url + "/public/api/User/register";
                new AdmRegHttpThread(mHandler,url,uid,pwd1,name,phone,code).start();
            }
        });
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

    private void initView(){
        et_admname = (EditText) findViewById(R.id.et_admname);
        et_admphone = (EditText) findViewById(R.id.et_admphone);
        et_admpwd1 = (EditText) findViewById(R.id.et_admpwd1);
        et_admpwd2 = (EditText) findViewById(R.id.et_admpwd2);
        btn_admreg = (Button) findViewById(R.id.btn_admreg);
        et_admuid = (EditText) findViewById(R.id.et_mtitle);
        et_code = (EditText) findViewById(R.id.et_code);
    }
}

