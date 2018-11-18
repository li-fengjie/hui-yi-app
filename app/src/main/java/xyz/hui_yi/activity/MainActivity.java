package xyz.hui_yi.activity;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.Toast;

import xyz.hui_yi.bean.UserInfoBean;
import xyz.hui_yi.fragment.HuiYi;
import xyz.hui_yi.fragment.QianDao;
import xyz.hui_yi.fragment.WoDe;
import xyz.hui_yi.utils.PrefUtils.PrefUtils;
import xyz.hui_yi.utils.tts.TTSUtils;
import xyz.hui_yi.constants.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * 主界面
 * 1.所有会议信息展示界面
 * 2.将要举行会议签到界面
 * 3.我的个人中心界面
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static boolean isUser;
    public static String uid;

    private ViewPager mViewPager;
    private FragmentPagerAdapter mAdapter;
    private List<Fragment> mFragments;
    private BottomNavigationView navigation;
    private MenuItem menuItem;
    public static UserInfoBean userInfo;
    public static Handler mainHandler;
    private boolean isSupportSoter;
    // 定义一个变量，来标识是否退出
    private static boolean isExit = false;

    Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            isExit = false;
        }
    };

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case xyz.hui_yi.R.id.navigation_home:
                    mViewPager.setCurrentItem(0);
                    return true;
                case xyz.hui_yi.R.id.navigation_dashboard:
                    mViewPager.setCurrentItem(1);
                    return true;
                case xyz.hui_yi.R.id.navigation_notifications:
                    mViewPager.setCurrentItem(2);
                    return true;
            }
            return false;
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(xyz.hui_yi.R.layout.activity_main);

        initView();
        prepareData();
        initPermission();

        //页面切换监听
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                if (menuItem != null) {
                    menuItem.setChecked(false);
                } else {
                    navigation.getMenu().getItem(0).setChecked(false);
                }
                menuItem = navigation.getMenu().getItem(position);
                menuItem.setChecked(true);
            }
            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {
            }
        });
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    private void prepareData() {
        isSupportSoter = PrefUtils.getBoolean(getApplication(), Constants.KEY_SOTER_SUPPORTED,false);
        Intent intent = getIntent();
        isUser = intent.getBooleanExtra(Constants.ISUSER, false);
        uid = intent.getStringExtra(Constants.UID);
        userInfo = new UserInfoBean();
        userInfo.setUid(uid);
        if(!isUser){
            navigation.getMenu().getItem(1).setIcon(xyz.hui_yi.R.mipmap.ic_management);
            navigation.getMenu().getItem(1).setTitle(getString(xyz.hui_yi.R.string.title_adm));
        }else {
            if(!isSupportSoter){
                navigation.getMenu().getItem(1).setIcon(xyz.hui_yi.R.mipmap.ic_face);
            }
        }
        mFragments = new ArrayList<Fragment>();
//以上的代码实现了一个Fragment和activity的绑定过程

        Fragment mTab01 = new HuiYi();
        Fragment mTab02 = new QianDao();
        Fragment mTab03 = new WoDe();

        mFragments.add(mTab01);
        mFragments.add(mTab02);
        mFragments.add(mTab03);

        mAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return mFragments.get(position);
            }

            @Override
            public int getCount() {
                return mFragments.size();
            }
        };
        mViewPager.setAdapter(mAdapter);
    }

    private void initView(){
        mViewPager = (ViewPager) findViewById(xyz.hui_yi.R.id.viewpager);
        navigation = (BottomNavigationView) findViewById(xyz.hui_yi.R.id.navigation);

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


    @Override
    protected void onDestroy() {
        super.onDestroy();
        TTSUtils.cancelTTS();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void exit() {
        if (!isExit) {
            isExit = true;
            Toast.makeText(getApplicationContext(), "再按一次退出会易",
                    Toast.LENGTH_SHORT).show();
            // 利用handler延迟发送更改状态信息
            mHandler.sendEmptyMessageDelayed(0, 2000);
        } else {
            finish();
            System.exit(0);
        }
    }

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String[] permissions = {
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.CAMERA
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.
            }
        }
        String[] tmpList = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }
    }
}
