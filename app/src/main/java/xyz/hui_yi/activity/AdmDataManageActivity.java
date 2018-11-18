package xyz.hui_yi.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import xyz.hui_yi.adapter.QianDaoIsOkAdapter;
import xyz.hui_yi.adapter.QianDaoNotOkAdapter;
import xyz.hui_yi.bean.AdmQianDaoBean;
import xyz.hui_yi.constants.Constants;
import xyz.hui_yi.layout.EmptyLayout;
import xyz.hui_yi.net.QisOkDownAsynctask;
import xyz.hui_yi.view.MyListView;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * Created by LiFen on 2018/2/2.
 * 管理员签到数据实时管理界面
 */

public class AdmDataManageActivity extends AppCompatActivity{

    private TabHost tab;

    private MyListView lv_isok;
    private MyListView lv_notok;
    private ArrayList<AdmQianDaoBean.IsOk> isOk;
    private ArrayList<AdmQianDaoBean.NotOk> notOk;
    private QianDaoIsOkAdapter isOkAdapter;
    private QianDaoNotOkAdapter notOkAdapter;
    public static EmptyLayout emptyLayout;

    private ExecutorService es;
    private String mid;
    private String notOkNum;
    private String isOkNum;
    public final static int LOGIN_DATA = 4444;
    public final static int NOT_NETWORK = 8888;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case LOGIN_DATA:
                    String num = (String) msg.obj;
                    String[] split = num.split(",");
                    notOkNum = split[1];
                    isOkNum = split[0];
                    if(split[1] != null){
                        setTitle("会议管理"+"(" + split[1] + ")");
                    }
                    break;
                case NOT_NETWORK:
                    emptyLayout.showError();
                    break;
//                    TTSUtils.speak(getApplicationContext(),"还有" + split[0] + "人未签到," + split[1] + "人已完成签到");
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(xyz.hui_yi.R.layout.activity_admdatamanage);

        initView();
        lv_isok.setAdapter(isOkAdapter);
        lv_notok.setAdapter(notOkAdapter);

        es = Executors.newFixedThreadPool(10);
        //TODO
        String url = getResources().getString(xyz.hui_yi.R.string.URL);
        url = url + "/public/api/Meet/meetsigndetail";
        new QisOkDownAsynctask(isOk,notOk,isOkAdapter,notOkAdapter,this,handler).executeOnExecutor(es, url,mid);
        emptyLayout.hide();
    }

    private void initView() {
        Intent intent = getIntent();
        mid = intent.getStringExtra("mid");
        lv_isok = (MyListView) findViewById(xyz.hui_yi.R.id.lv_isok);
        lv_notok = (MyListView) findViewById(xyz.hui_yi.R.id.lv_notok);
        isOk = new ArrayList<>();
        notOk = new ArrayList<>();
        isOkAdapter = new QianDaoIsOkAdapter(isOk,this);
        notOkAdapter = new QianDaoNotOkAdapter(notOk,this);
        emptyLayout = (EmptyLayout) findViewById(xyz.hui_yi.R.id.emptyLayout);

        tab = (TabHost) findViewById(xyz.hui_yi.R.id.tabhost);

        tab.setup();
        //在TabHost创建标签，然后设置：标题／图标／标签页布局
        tab.addTab(tab.newTabSpec("tab1").setIndicator("未签到" ,null).setContent(xyz.hui_yi.R.id.tab1));
        tab.addTab(tab.newTabSpec("tab2").setIndicator("已签到" , null).setContent(xyz.hui_yi.R.id.tab2));

        TabWidget tabWidget = tab.getTabWidget();
        for(int i=0;i<tabWidget.getChildCount();i++){
            // 设置高度、宽度
            tabWidget.getChildAt(i).getLayoutParams().height= 80;
//            tabWidget.getChildAt(i).getLayoutParams().width = 200;
            // 设置tab中标题文字的颜色，不然默认为黑色
            final TextView tv = (TextView) tabWidget.getChildAt(i).findViewById(android.R.id.title);
            tv.setTextSize(16);
            tv.setTextColor(Color.LTGRAY);
        }
        tab.getTabWidget().setDividerDrawable(null);
        updateTabBackground(tab);
        tab.getTabWidget().getChildAt(0).setBackgroundResource(xyz.hui_yi.R.drawable.tag_top_left_selector);
        tab.getTabWidget().getChildAt(1).setBackgroundResource(xyz.hui_yi.R.drawable.tag_top_right_normal);
        tab.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                updateTabBackground(tab);
            }
        });
    }
    /**
     * 更新Tab标签的背景图
     * @param tabHost
     */
    private void updateTabBackground(final TabHost tabHost) {
        for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
            View v = tabHost.getTabWidget().getChildAt(i);
            if (tabHost.getCurrentTab() == i) {
                //选中后的背景
                if(i == 0){
                    if (notOkNum != null) {
                        setTitle("会议管理"+"(" + notOkNum + ")");
                    }
                    ((TextView)v.findViewById(android.R.id.title)).setTextColor(Color.WHITE);
                    v.setBackgroundResource(xyz.hui_yi.R.drawable.tag_top_left_selector);
                }else {
                    if(isOkNum != null){
                        setTitle("会议管理"+"(" + isOkNum + ")");
                    }
                    ((TextView)v.findViewById(android.R.id.title)).setTextColor(Color.WHITE);
                    v.setBackgroundResource(xyz.hui_yi.R.drawable.tag_top_right_selector);
                }
            } else {
                if(i == 0){
                    ((TextView)v.findViewById(android.R.id.title)).setTextColor(Color.GRAY);
                    v.setBackgroundResource(xyz.hui_yi.R.drawable.tag_top_left_normal);
                }
                else {
                    ((TextView)v.findViewById(android.R.id.title)).setTextColor(Color.GRAY);
                    v.setBackgroundResource(xyz.hui_yi.R.drawable.tag_top_right_normal);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
            case xyz.hui_yi.R.id.action_refresh:
                isOk.clear();
                notOk.clear();
                String url = getResources().getString(xyz.hui_yi.R.string.URL);
                url = url + "/public/api/Meet/meetsigndetail";
                new QisOkDownAsynctask(isOk,notOk,isOkAdapter,notOkAdapter,this,handler).executeOnExecutor(es, url,mid);
                emptyLayout.hide();
                return true;
            case xyz.hui_yi.R.id.action_face:
                startSerchFace();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startSerchFace() {
        Intent intent = new Intent(getApplicationContext(), PhotoActivity.class);
        intent.putExtra(Constants.MID,mid);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(xyz.hui_yi.R.menu.menu_face, menu);
        return true;
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        isOk.clear();
        notOk.clear();
        String url = getResources().getString(xyz.hui_yi.R.string.URL);
        url = url + "/public/api/Meet/meetsigndetail";
        new QisOkDownAsynctask(isOk,notOk,isOkAdapter,notOkAdapter,this,handler).executeOnExecutor(es, url,mid);
        emptyLayout.hide();
    }
}
