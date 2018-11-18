package xyz.hui_yi.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import xyz.hui_yi.R;
import xyz.hui_yi.adapter.UserInfoAdapter;
import xyz.hui_yi.bean.UsersInforBean;
import xyz.hui_yi.net.UListDownAsynctask;

import static xyz.hui_yi.activity.MainActivity.userInfo;


/**
 * Created by LiFen on 2018/4/6.
 */

public class PeopleActivity extends AppCompatActivity {
    private static final String TAG = "PeopleActivity";
    private Button mButtonView;
    private ListView lv;
    private ArrayList<UsersInforBean.UserInfo> data;
    private UserInfoAdapter mAdapter;
    private ExecutorService es;
    public static HashMap<Integer, Boolean> isSelected;
    public static int PEOPLENUM = 100;
    public Handler pnhandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == PEOPLENUM){
                int num = (int) msg.obj;
                setTitle("人员信息"+"(" + num + ")");
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admuserinfo);
        initView();
        prepareData();
    }

    private void prepareData() {
        mButtonView.setText("搜索");
        data = new ArrayList<>();
        isSelected = new HashMap<Integer, Boolean>();
        mAdapter = new UserInfoAdapter(data, this,isSelected);
        lv.setAdapter(mAdapter);
        //TODO 联网获取会议人员数据
        es = Executors.newFixedThreadPool(10);
        String url = getResources().getString(R.string.URL);
        url = url + "/public/api/Meet/selectmember";
        new UListDownAsynctask(pnhandler,data,mAdapter,this,isSelected).executeOnExecutor(es, url,userInfo.getUid());
    }

    private void initView() {
        mButtonView = (Button) findViewById(R.id.bt_selectall);
        mButtonView.setFocusable(true);
        mButtonView.setFocusableInTouchMode(true);
        mButtonView.requestFocus();
        lv = (ListView)findViewById(R.id.lv);
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
