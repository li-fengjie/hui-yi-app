package xyz.hui_yi.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import xyz.hui_yi.layout.EmptyLayout;
import xyz.hui_yi.net.HttpmImage;
import xyz.hui_yi.net.MeetingInforHttpThread;
import xyz.hui_yi.utils.image.SmartImageView;


/**
 * Created by LiFen on 2018/2/1.
 * 会议详细信息界面
 */

public class MeetingInforActivity extends AppCompatActivity {
    private static final String TAG = "MeetingInforActivity";
    private TextView tv_mfounder;
    private TextView tv_maddress;
    private TextView tv_mcontent;
    private TextView tv_mstarttime;
    private TextView tv_mendtime;
    private TextView tv_mtitle;
    private SmartImageView iv_mimage;
    public static EmptyLayout emptyLayout;


    public final static int INFOR_SUCCESS = 20019;
    public final static int INFOR_FAIL = 10025;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case INFOR_SUCCESS :
                    String mcontent = (String) msg.obj;
                    String[] split = mcontent.split(",");
                    tv_mcontent.setText(split[0]);
                    tv_mfounder.append(split[1]);
                    tv_mendtime.append(split[2]);
                    if (split.length >= 4){
                        Log.i(TAG, "handleMessage: mimageurl" + split[3]);
                        new HttpmImage(split[3], mHandler, iv_mimage).start();
                        if(split[3] == null){
                            iv_mimage.setImageResource(xyz.hui_yi.R.drawable.image_huiyi_example);
                        }
                    }else {
                        iv_mimage.setImageResource(xyz.hui_yi.R.drawable.image_huiyi_example);
                    }
                    Log.i(TAG, "handleMessage: 结束时间" + split[2] + "发布人" + split[1]);
                    break;
                case INFOR_FAIL:
                    emptyLayout.showError();
                    Toast.makeText(MeetingInforActivity.this,getString(xyz.hui_yi.R.string.error_message),Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(xyz.hui_yi.R.layout.activity_meetting_info);

        initView();
        prepareData();
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

    private void initView() {
        tv_mfounder = (TextView) findViewById(xyz.hui_yi.R.id.tv_info_founder);
        tv_maddress = (TextView) findViewById(xyz.hui_yi.R.id.tv_info_maddress);
        tv_mcontent = (TextView) findViewById(xyz.hui_yi.R.id.tv_info_mcontent);
        tv_mstarttime = (TextView) findViewById(xyz.hui_yi.R.id.tv_info_mstarttime);
        tv_mendtime = (TextView) findViewById(xyz.hui_yi.R.id.tv_info_mendtime);
        tv_mtitle = (TextView) findViewById(xyz.hui_yi.R.id.tv_info_mtitle);
        iv_mimage = (SmartImageView) findViewById(xyz.hui_yi.R.id.iv_info_mimage);
        emptyLayout = (EmptyLayout) findViewById(xyz.hui_yi.R.id.emptyLayout);
    }

    private void prepareData() {
        Intent intent = getIntent();
        //TODO 联网获取会议详细信息
        String mtitle = intent.getStringExtra("mtitle");
        String maddress = intent.getStringExtra("maddress");
        String mtime = intent.getStringExtra("mtime");
        String mid = intent.getStringExtra("mid");

        tv_mtitle.setText(mtitle);
        tv_maddress.append(maddress);
        tv_mstarttime.append(mtime);

        String url = getResources().getString(xyz.hui_yi.R.string.URL);
        url = url + "/public/api/Meet/meetcontent";
        new MeetingInforHttpThread(mHandler,url,mid).start();
        emptyLayout.hide();
    }
}
