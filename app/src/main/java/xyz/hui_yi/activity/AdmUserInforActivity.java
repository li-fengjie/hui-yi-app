package xyz.hui_yi.activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import xyz.hui_yi.adapter.UserInfoAdapter;
import xyz.hui_yi.bean.UsersInforBean;
import xyz.hui_yi.net.CreateMeetingHttpThread;
import xyz.hui_yi.net.UListDownAsynctask;
import xyz.hui_yi.net.UnloadMimageHttpThread;
import xyz.hui_yi.utils.SnackBarUtils.SnackBarUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * 管理员选择需要参会人员界面
 */

public class AdmUserInforActivity extends AppCompatActivity {
    private static final String TAG = "AdmUserInforActivity";
    private ListView lv;
    private UserInfoAdapter mAdapter;
    private ArrayList<UsersInforBean.UserInfo> data;
    private Button bt_selectall;
    private int checkNum = 0; // 记录选中的条目数量

    // 用来控制CheckBox的选中状况
    public static HashMap<Integer, Boolean> isSelected;

    private ExecutorService es;
    private String uidSet = "";
    private String mtitle;
    private String mcontent;
    private String maddress;
    private String mstarttime;
    private String mendtime;
    private String wlanmac;
    private String bluetoothmac;
    public final static int CREATE_SUCCESS = 20007;
    public final static int CREATE_FAIL = 10013;
    public final static int UNLOAD_SUCCESS = 20001;
    public final static int UNLOAD_FAIL = 10026;

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){
            switch (msg.what){
                case CREATE_SUCCESS:
                    String mid = (String) msg.obj;
                    String url = getResources().getString(xyz.hui_yi.R.string.URL);
                    url = url + "/public/api/User/uploadPicture";
                    Log.i(TAG, "onResullt: mid" + mid);
                    if(mimage != null){
                        Log.i(TAG, "onResullt: mimage" + mimage);
                        String image = Base64.encodeToString(mimage.getBytes(), Base64.URL_SAFE | Base64.NO_WRAP);
                        Log.i(TAG, "onResullt: uphoto" + image);
                        new UnloadMimageHttpThread(mHandler,url,mid,image).start();
                    }else {
                        Message msg1 = new Message();
                        msg1.what =UNLOAD_SUCCESS;
                        mHandler.sendMessage(msg1);
                    }
                    break;
                case UNLOAD_FAIL:
                    dismissLoading();
                    Toast.makeText(AdmUserInforActivity.this,"会议图片上传失败",Toast.LENGTH_SHORT).show();
                case UNLOAD_SUCCESS:
                    dismissLoading();
                    Toast.makeText(AdmUserInforActivity.this,"会议创建成功",Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case CREATE_FAIL:
                    dismissLoading();
                    SnackBarUtils.show(bt_selectall, xyz.hui_yi.R.string.text_server_error);
                    break;
                default:
                    dismissLoading();
                    SnackBarUtils.show(bt_selectall, xyz.hui_yi.R.string.toast_network_error);
                    break;
            }
        }
    };
    private String mimage;
    private ProgressDialog mLoadingDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(xyz.hui_yi.R.layout.activity_admuserinfo);
        /* 实例化各个控件 */
        lv = (ListView) findViewById(xyz.hui_yi.R.id.lv);
        bt_selectall = (Button) findViewById(xyz.hui_yi.R.id.bt_selectall);
        bt_selectall.setFocusable(true);
        bt_selectall.setFocusableInTouchMode(true);
        bt_selectall.requestFocus();
        // 为Adapter准备数据
        initData();
        // 实例化自定义的MyAdapter
        data = new ArrayList<>();
        AdmUserInforActivity.isSelected = new HashMap<Integer, Boolean>();
        mAdapter = new UserInfoAdapter(data, this,isSelected);
        // 绑定Adapter
        lv.setAdapter(mAdapter);

        //TODO 联网获取会议人员数据
        es = Executors.newFixedThreadPool(10);
        String url = getResources().getString(xyz.hui_yi.R.string.URL);
        url = url + "/public/api/Meet/selectmember";
        new UListDownAsynctask(data,mAdapter,this,isSelected).executeOnExecutor(es, url, MainActivity.userInfo.getUid());

//        es = Executors.newFixedThreadPool(10);
//        new UListDownAsynctask(data,mAdapter,this).executeOnExecutor(es, "http://mrobot.pcauto.com.cn/v2/cms/channels/3?pageNo="+1+"&pageSize=20&v=4.0.0");

        // 全选按钮的回调接口
        bt_selectall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 遍历list的长度，将MyAdapter中的map值全部设为true
                for (int i = 0; i < data.size(); i++) {
                    isSelected.put(i, true);
                }
                // 数量设为list的长度
                checkNum = data.size();
                // 刷新listview和TextView的显示
                dataChanged();
            }
        });

        // 绑定listView的监听器
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                // 取得ViewHolder对象，这样就省去了通过层层的findViewById去实例化我们需要的cb实例的步骤
                UserInfoAdapter.ViewHolder holder = (UserInfoAdapter.ViewHolder) arg1.getTag();
                // 改变CheckBox的状态
                holder.cb.toggle();
                // 将CheckBox的选中状况记录下来
                isSelected.put(arg2, holder.cb.isChecked());
                // 调整选定条目
                if (holder.cb.isChecked() == true) {
                    checkNum++;
                } else {
                    checkNum--;
                }
                // 用lable显示
                setTitle("已添加:" + checkNum);
            }
        });
    }

    private void showLoading(String wording) {
        if (mLoadingDialog == null) {
            mLoadingDialog = ProgressDialog.show(AdmUserInforActivity.this, "", wording, true, false, null);
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

    // 初始化数据
    private void initData() {
        Intent intent = getIntent();
        mtitle = intent.getStringExtra("mtitle");
        mcontent = intent.getStringExtra("mcontent");
        maddress = intent.getStringExtra("maddress");
        mstarttime = intent.getStringExtra("mstarttime");
        mendtime = intent.getStringExtra("mendtime");
        wlanmac = intent.getStringExtra("wlanmac");
        bluetoothmac = intent.getStringExtra("bluetoothmac");
        mimage = intent.getStringExtra("mimage");
        Log.i(TAG, "initData: " + mtitle + " " + mcontent
                + " " + maddress + " " + mstarttime + " " +
                mendtime + " " + wlanmac + " " + bluetoothmac);
        Log.i(TAG, "mstarttime " + mstarttime);
    }

    // 刷新listview和TextView的显示
    private void dataChanged() {
        // 通知listView刷新
        mAdapter.notifyDataSetChanged();
        // lable显示最新的选中数目
        setTitle("已添加:" + checkNum);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
            case xyz.hui_yi.R.id.action_ok:
                create();
                return true;
            case xyz.hui_yi.R.id.action_cancel:
                deselectAll();
        }
        return super.onOptionsItemSelected(item);
    }

    private void create(){
        if(checkNum <= 0){
            SnackBarUtils.show(bt_selectall,"请添加会议人员");
            return;
        }
        new AlertDialog.Builder(this)
                .setTitle("创建会议？")
                .setIcon(xyz.hui_yi.R.mipmap.ic_dialog_gps)
                .setPositiveButton("确认", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                            backUidSet();//返回 uidset
                            //TODO  联网创建会议
                            String url = getResources().getString(xyz.hui_yi.R.string.URL);
                            url = url + "/public/api/Meet/meetCreate";
                            new CreateMeetingHttpThread(mHandler,url,mtitle,
                                    mcontent,maddress, MainActivity.userInfo.getUid(),
                                    mstarttime,mendtime,wlanmac,
                                    bluetoothmac,uidSet).start();
                        showLoading("正在创建...");
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                }).show();
    }
    private void backUidSet(){
        // 遍历list的长度
        for (int i = 0; i < data.size(); i++) {
            if (isSelected.get(i)) {
                uidSet += (data.get(i).uid + ",");
            }
        }
        Log.i(TAG, "backUidSet: " + uidSet);
    }

    private void deselectAll(){
        // 遍历list的长度，将已选的按钮设为未选
        for (int i = 0; i < data.size(); i++) {
            if (isSelected.get(i)) {
                isSelected.put(i, false);
                checkNum--;// 数量减1
            }
        }
        // 刷新listview和TextView的显示
        dataChanged();
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(xyz.hui_yi.R.menu.menu_uidset, menu);
        return true;
    }
}