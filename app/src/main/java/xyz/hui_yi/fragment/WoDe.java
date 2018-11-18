package xyz.hui_yi.fragment;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.shenyuanqing.zxingsimplify.zxing.Activity.CaptureActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import xyz.hui_yi.R;
import xyz.hui_yi.activity.AboutActivity;
import xyz.hui_yi.activity.FeedbackActivity;
import xyz.hui_yi.activity.GuideActivity;
import xyz.hui_yi.activity.HistoryMeetsActivity;
import xyz.hui_yi.activity.MyInforActivity;
import xyz.hui_yi.activity.PeopleActivity;
import xyz.hui_yi.activity.SettingActivity;
import xyz.hui_yi.constants.Constants;
import xyz.hui_yi.net.UserInforHttpThread;
import xyz.hui_yi.utils.PrefUtils.PrefUtils;
import xyz.hui_yi.view.CircleImageView;
import xyz.hui_yi.view.MyListView;

import static android.app.Activity.RESULT_OK;
import static xyz.hui_yi.activity.MainActivity.isUser;
import static xyz.hui_yi.activity.MainActivity.userInfo;


/**
 * Created by LiFen on 2018/1/23.
 * 我的个人中心界面
 */

public class WoDe extends Fragment {
    private static final String TAG = "WoDe";
    private static final int REQUEST_SCAN = 0;
    private int[] logo1;
    private int[] logo2;
    private int[] logo3;
    private int[] logo4;
    private String[] wodeitems1;
    private String[] wodeitems2;
    private String[] wodeitems3;
    private String[] wodeitems4;
    private TextView tv_uid;
//    private Button btn_meetnum;
    private TextView tv_bname;
    private CircleImageView civ_logo;
    public final static int INFOR_SUCCESS = 20;
    public final static int INFOR_FAIL = 10;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case INFOR_SUCCESS:
                    tv_bname.setText(userInfo.getBname());
//                    btn_meetnum.setText("会议:" + userInfo.getMeetnum());
                    unloadUphoto();
                    break;
                case INFOR_FAIL:
                    userInfo.setBname("");
                    userInfo.setMeetnum("");
                    userInfo.setUname("");
                    userInfo.setUphoto("");
                    Toast.makeText(getContext(), getString(R.string.toast_network_error), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bitmap bitmap = (Bitmap) msg.obj;
            civ_logo.setImageBitmap(bitmap);
        }
    };
    private MyListView lv1;
    private MyListView lv4;
    private MyListView lv2;
    private MyListView lv3;
    private ImageView iv_scan;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View inflate = inflater.inflate(R.layout.fragment_wode, container, false);
        lv1 = (MyListView) inflate.findViewById(R.id.listView1);
        lv2 = (MyListView) inflate.findViewById(R.id.listView2);
        lv3 = (MyListView) inflate.findViewById(R.id.listView3);
        lv4 = (MyListView) inflate.findViewById(R.id.listView4);
        tv_uid = (TextView) inflate.findViewById(R.id.tv_username);
//        btn_meetnum = (Button) inflate.findViewById(btn_meetnum);
        tv_bname = (TextView) inflate.findViewById(R.id.tv_bname);
        civ_logo = (CircleImageView) inflate.findViewById(R.id.civ_logo);
        iv_scan = (ImageView)inflate.findViewById(R.id.iv_scan);
        prepareData();
        lv1.setAdapter(new WoDeAdapter());
        lv2.setAdapter(new WoDeAdapter2());
        lv3.setAdapter(new WoDeAdapter3());
        if(!isUser){
            lv4.setAdapter(new WoDeAdapter4());
        }else {
            lv4.setVisibility(View.GONE);
        }
        configLogic();
        return inflate;
    }

    /**
     * 获得运行时权限
     */
    private void getRuntimeRight() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, 1);
        } else {
            jumpScanPage();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    jumpScanPage();
                } else {
                    Toast.makeText(getActivity(), "拒绝", Toast.LENGTH_LONG).show();
                }
            default:
                break;
        }
    }

    /**
     * 跳转到扫码页
     */
    private void jumpScanPage() {
        startActivityForResult(new Intent(getContext(), CaptureActivity.class),REQUEST_SCAN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_SCAN && resultCode == RESULT_OK){
//            TODO 扫码后回调 操作
            Toast.makeText(getContext(),data.getStringExtra("barCode"),Toast.LENGTH_LONG).show();
        }
    }

    private void configLogic() {
        lv1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) wodeitems1[position];
                Log.d("ListView", "Selected item: " + item);
                if (position == 0) {
                    Intent intent = new Intent(getContext(), MyInforActivity.class);
                    startActivity(intent);
                }
                if(position == 1){
                    //历史会议界面
                    Intent intent = new Intent(getContext(), HistoryMeetsActivity.class);
                    startActivity(intent);
                }
            }
        });
        lv2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) wodeitems2[position];
                Log.d("ListView", "Selected item: " + item);
                if (position == 0) {
                    Intent intent = new Intent(getContext(), GuideActivity.class);
                    startActivity(intent);
                    return;
                }
                if (position == 1) {
                    Intent intent = new Intent(getContext(), FeedbackActivity.class);
                    startActivity(intent);
                    return;
                }
                if (position == 2) {
                    Intent intent = new Intent(getContext(), AboutActivity.class);
                    startActivity(intent);
                    return;
                }
                if(position == 3){
                    Toast.makeText(getContext(),"当前已经是最新的版本",Toast.LENGTH_SHORT).show();
                }
                if (position == 4) {
                    Intent intent = new Intent(getContext(), SettingActivity.class);
                    startActivity(intent);
                    return;
                }
            }
        });
        lv3.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) wodeitems3[position];
                Log.d("ListView", "Selected item: " + item);
                if (position == 0) {
                    Intent intent = new Intent(getContext(), SettingActivity.class);
                    startActivity(intent);
                }
            }
        });
        lv4.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) wodeitems4[position];
                Log.d("ListView", "Selected item: " + item);
                if (position == 0) {
                    Intent intent = new Intent(getContext(), PeopleActivity.class);
                    startActivity(intent);
                }
            }
        });

        iv_scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRuntimeRight();
            }
        });
    }

    private void prepareData() {
        //TODO 联网获取 个人信息
        String uid = userInfo.getUid();
        tv_uid.setText(uid);
        if(userInfo.getBname() != null){
            tv_bname.setText(userInfo.getBname());
//            btn_meetnum.setText(userInfo.getMeetnum());
        }
        if (userInfo.getUphoto() != null) {
            final String path = userInfo.getUphoto();
            Log.i(TAG, "run: uphotopath" + path);
            if (path.length() > 3) {
                final File file = new File(getContext().getCacheDir(), Base64.encodeToString(path.getBytes(), Base64.DEFAULT)
                        .substring(0, Base64.encodeToString(path.getBytes(), Base64.DEFAULT).length() - 2));
                if (file.exists() && file.length() > 0) {
                    Message msg = new Message();
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    msg.obj = bitmap;
                    handler.sendMessage(msg);
                }
            }
        }
        if(userInfo.getBname() == null || userInfo.getBname().equals("")
                || userInfo.getUphoto() == null || userInfo.getUphoto().equals("")){
            String url = getResources().getString(R.string.URL);
            url = url + "/public/api/User/inforShow";
            new UserInforHttpThread(mHandler, url, uid).start();
        }

        wodeitems1 = getResources().getStringArray(R.array.wodeitem1);
        wodeitems2 = getResources().getStringArray(R.array.wodeitem2);
        wodeitems3 = getResources().getStringArray(R.array.wodeitem3);
        wodeitems4 = getResources().getStringArray(R.array.wodeitem4);
        logo1 = new int[]{R.mipmap.ic_myinfor, R.mipmap.ic_history};
        logo2 = new int[]{R.mipmap.ic_guide, R.mipmap.ic_feedback, R.mipmap.ic_aboutus, R.mipmap.ic_versionupdate};
        logo3 = new int[]{R.mipmap.ic_setting};
        logo4 = new int[]{R.mipmap.ic_people};
    }

    private void unloadUphoto(){
        if (userInfo.getUphoto() != null) {
//            加载头像，缓存中不存在再联网加载
            final String path = userInfo.getUphoto();
            Log.i(TAG, "run: uphotopath" + path);
            if (path.length() > 3) {
                final File file = new File(getContext().getCacheDir(), Base64.encodeToString(path.getBytes(), Base64.DEFAULT)
                        .substring(0, Base64.encodeToString(path.getBytes(), Base64.DEFAULT).length() - 2));
                if (file.exists() && file.length() > 0) {
                    Message msg = new Message();
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    msg.obj = bitmap;
                    handler.sendMessage(msg);
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                URL url = new URL(path);
                                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                                conn.setRequestMethod("GET");
                                conn.setConnectTimeout(5000);

                                int code = conn.getResponseCode();
                                if (code == 200) {
                                    InputStream in = conn.getInputStream();
                                    FileOutputStream fos = new FileOutputStream(file);
                                    byte[] buffer = new byte[1024];//1kb
                                    int len = -1;
                                    while ((len = in.read(buffer)) != -1) {
                                        fos.write(buffer, 0, len);
                                    }
                                    fos.close();
                                    in.close();
                                    PrefUtils.setString(getActivity(), Constants.KEY_UPHOTO, Base64.encodeToString(path.getBytes(), Base64.DEFAULT)
                                            .substring(0, Base64.encodeToString(path.getBytes(), Base64.DEFAULT).length() - 2));
                                    Log.i(TAG, "run: " + Base64.encodeToString(path.getBytes(), Base64.DEFAULT));
                                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                                    Message msg = Message.obtain();
                                    msg.obj = bitmap;
                                    handler.sendMessage(msg);
                                }
                            } catch (Exception e) {

                            }
                        }
                    }).start();
                }
            }
        }
    }
    private class WoDeAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return wodeitems1.length;
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
            if (convertView == null) {
                view = View.inflate(getContext(), R.layout.item3, null);
            } else {
                view = convertView;
            }
            ImageView iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
            TextView tv_title = (TextView) view.findViewById(R.id.tv_mtitle);
            iv_icon.setImageResource(logo1[position]);
            tv_title.setText(wodeitems1[position]);

            return view;
        }
    }

    private class WoDeAdapter2 extends BaseAdapter {
        @Override
        public int getCount() {
            return wodeitems2.length;
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
            if (convertView == null) {
                view = View.inflate(getContext(), R.layout.item3, null);
            } else {
                view = convertView;
            }
            ImageView iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
            TextView tv_title = (TextView) view.findViewById(R.id.tv_mtitle);
            iv_icon.setImageResource(logo2[position]);
            tv_title.setText(wodeitems2[position]);

            return view;
        }
    }

    private class WoDeAdapter3 extends BaseAdapter {
        @Override
        public int getCount() {
            return wodeitems3.length;
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
            if (convertView == null) {
                view = View.inflate(getContext(), R.layout.item3, null);
            } else {
                view = convertView;
            }
            ImageView iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
            TextView tv_title = (TextView) view.findViewById(R.id.tv_mtitle);
            iv_icon.setImageResource(logo3[position]);
            tv_title.setText(wodeitems3[position]);

            return view;
        }
    }

    private class WoDeAdapter4 extends BaseAdapter {
        @Override
        public int getCount() {
            return wodeitems4.length;
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
            if (convertView == null) {
                view = View.inflate(getContext(), R.layout.item3, null);
            } else {
                view = convertView;
            }
            ImageView iv_icon = (ImageView) view.findViewById(R.id.iv_icon);
            TextView tv_title = (TextView) view.findViewById(R.id.tv_mtitle);
            iv_icon.setImageResource(logo4[position]);
            tv_title.setText(wodeitems4[position]);
            return view;
        }
    }
}