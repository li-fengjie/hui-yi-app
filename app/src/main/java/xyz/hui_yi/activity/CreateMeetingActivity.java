package xyz.hui_yi.activity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import xyz.hui_yi.face.utils.CustomUtil;
import xyz.hui_yi.utils.GPSUtils;
import xyz.hui_yi.utils.SnackBarUtils.SnackBarUtils;
import xyz.hui_yi.utils.bluetoothutils.BluetoothUtils;
import xyz.hui_yi.utils.wifiutils.WifiUtils;

import java.io.FileNotFoundException;
import java.util.Calendar;


/**
 * Created by LiFen on 2018/1/24.
 * 创建会议界面
 */

public class CreateMeetingActivity extends AppCompatActivity {

    private static final String TAG = "CreateMeetingActivity";
    private TextView tv_startdate;
    private TextView tv_starttime;
    private TextView tv_endtime;
    private int mStartYear, mStartMonth, mStartDay;
    private int mEndYear, mEndMonth, mEndDay;
    private int mStartHour, mStartMinute;
    private int mEndHour, mEndMinute;
    final int STARTDATE_DIALOG = 1;
    final int STARTTIME_DIALOG = 2;
    final int ENDDATE_DIALOG = 3;
    final int ENDTIME_DIALOG = 4;
    private TextView tv_enddate;
    private EditText et_mtitle;
    private EditText et_maddress;
    private EditText et_mcontent;
    private String mtitle;
    private String mContent;
    private String startTime;
    private String endTime;
    private String maddress;
    private String bluetoothMac = "";
    private String wlanMac = "";
    private ImageView iv_startdate;
    private ImageView iv_starttime;
    private ImageView iv_enddate;
    private ImageView iv_endtime;
    private String mImgBase641;
    private static final int REQUEST_CODE1 = 11;

    private ImageView iv_mimage;
    private ImageView iv_photo;
    private TextView tv_add;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_admcreatemeeting);
        setContentView(xyz.hui_yi.R.layout.activity_createmeeting);
        initView();
        prepareData();
        configLogic();
    }

    private void configLogic() {
        iv_mimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startAlbumActivity(REQUEST_CODE1);
            }
        });
        iv_startdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(STARTDATE_DIALOG);
            }
        });
        iv_starttime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(STARTTIME_DIALOG);
            }
        });
        iv_enddate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDialog(ENDDATE_DIALOG);
            }
        });
        iv_endtime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(ENDTIME_DIALOG);
            }
        });

    }


    public void click(View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择会议签到设备");

        final String items[] = {
                "本手机的无线热点",
                "当前连接的无线设备"
                ,"本手机的无线蓝牙"};
        final boolean[] checkedItems = {false, false, false};

        builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String item = items[which];
                if(which == 0) {
                    wifi();
                }else if(which == 1){
                    router();
                }else if(which == 2){
                    bluetooth();
                }
                Toast.makeText(getApplicationContext(),item,Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void wifi() {
        if (!WifiUtils.getMacAddress().equals("02:00:00:00:00:02")) {
            wlanMac = WifiUtils.getMacAddress();
            bluetoothMac = "";
            tv_add.setText("本手机热点");
        } else {
            SnackBarUtils.show(et_maddress, xyz.hui_yi.R.string.text_open_rwifi);
            bluetoothMac = "";
            wlanMac = "";
        }
    }

    private void bluetooth() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBluetoothAdapter.isEnabled()) {
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), 1);
            return;
        }
        if (mBluetoothAdapter.isEnabled()) {
            if (BluetoothUtils.getBtAddressByReflection() != null) {
                bluetoothMac = BluetoothUtils.getBtAddressByReflection();
                Log.i(TAG, "onCheckedChanged: bluetoothMac" + bluetoothMac);
                tv_add.setText("本手机蓝牙");
                wlanMac = "";
            } else {
                SnackBarUtils.show(et_maddress, xyz.hui_yi.R.string.text_not_support);
                bluetoothMac = "";
                wlanMac = "";
            }
        }
    }

    private void router() {
        boolean wifiConnected = WifiUtils.isWifiConnected(getApplicationContext());
        if (!wifiConnected) {
            SnackBarUtils.show(et_maddress, xyz.hui_yi.R.string.text_open_wifi);
            bluetoothMac = "";
            wlanMac = "";
        } else {
            String connectedWifiMacAddress = WifiUtils.getConnectedWifiMacAddress(getApplicationContext());
            if (connectedWifiMacAddress == null || connectedWifiMacAddress.equals("")) {
                new AlertDialog.Builder(CreateMeetingActivity.this).setTitle("打开GPS服务?")
                        .setMessage("\t请打开GPS定位开关，并在设置中打开权限，您可以创建会议成功后关闭测服务")
                        .setIcon(xyz.hui_yi.R.mipmap.ic_dialog_gps)
                        .setPositiveButton("去打开", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                GPSUtils.openGps(CreateMeetingActivity.this);
                            }
                        })
                        .show();
                bluetoothMac = "";
                wlanMac = "";
            }
            if (connectedWifiMacAddress != null) {
                wlanMac = connectedWifiMacAddress;
                bluetoothMac = "";
                tv_add.setText("连接的无线设备");
//                            SnackBarUtils.show(rg, wlanMac);
            } else {
                SnackBarUtils.show(et_maddress, "请开启GPS服务");
            }
            if (wlanMac.equals("")) {
                bluetoothMac = "";
                wlanMac = "";
            }
        }
    }

    private void initView() {
        iv_mimage = (ImageView) findViewById(xyz.hui_yi.R.id.iv_mimage);
        iv_photo = (ImageView) findViewById(xyz.hui_yi.R.id.iv_photo);
        tv_startdate = (TextView) findViewById(xyz.hui_yi.R.id.tv_startdate);
        tv_starttime = (TextView) findViewById(xyz.hui_yi.R.id.tv_starttime);
        tv_enddate = (TextView) findViewById(xyz.hui_yi.R.id.tv_enddate);
        tv_endtime = (TextView) findViewById(xyz.hui_yi.R.id.tv_endtime);

        et_mtitle = (EditText) findViewById(xyz.hui_yi.R.id.et_mtitle);
        et_maddress = (EditText) findViewById(xyz.hui_yi.R.id.et_maddress);
        et_mcontent = (EditText) findViewById(xyz.hui_yi.R.id.et_mcontent);

        iv_startdate = (ImageView) findViewById(xyz.hui_yi.R.id.iv_startdate);
        iv_starttime = (ImageView) findViewById(xyz.hui_yi.R.id.iv_starttime);
        iv_enddate = (ImageView) findViewById(xyz.hui_yi.R.id.iv_enddate);
        iv_endtime = (ImageView) findViewById(xyz.hui_yi.R.id.iv_endtime);

        tv_add = (TextView) findViewById(xyz.hui_yi.R.id.tv_wifi);
    }

    private void prepareData() {
        final Calendar ca = Calendar.getInstance();
        mStartYear = ca.get(Calendar.YEAR);
        mStartMonth = ca.get(Calendar.MONTH);
        mStartDay = ca.get(Calendar.DAY_OF_MONTH);
        mStartHour = ca.get(Calendar.HOUR_OF_DAY);
        mStartMinute = ca.get(Calendar.MINUTE);

        mEndYear = ca.get(Calendar.YEAR);
        mEndMonth = ca.get(Calendar.MONTH);
        mEndDay = ca.get(Calendar.DAY_OF_MONTH);
        mEndHour = ca.get(Calendar.HOUR_OF_DAY);
        mEndMinute = ca.get(Calendar.MINUTE);
    }

    private void startAlbumActivity(int requestCode) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, requestCode);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (data == null)
            return;
        Uri uri = data.getData();
        Log.e("uri", uri.toString());
        ContentResolver cr = this.getContentResolver();
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));
                /* 将Bitmap设定到ImageView */
        } catch (FileNotFoundException e) {
            Log.e("Exception", e.getMessage(), e);
        }
        if (resultCode == RESULT_OK && requestCode == REQUEST_CODE1) {
            iv_mimage.setImageBitmap(bitmap);
            iv_photo.setVisibility(View.GONE);
            mImgBase641 = CustomUtil.bitmapToBase64(bitmap);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void create() {
        mtitle = et_mtitle.getText().toString();
        maddress = et_maddress.getText().toString();
        mContent = et_mcontent.getText().toString();
        startTime = tv_startdate.getText().toString() + " " + tv_starttime.getText().toString();
        endTime = tv_enddate.getText().toString() + " " + tv_endtime.getText().toString();
        Log.i(TAG, "create: " + startTime + "\n" + endTime);

        if (mtitle == null || mtitle.equals("")) {
            SnackBarUtils.show(et_mtitle, "请输入会议名");
            return;
        }
        if (maddress == null || maddress.equals("")) {
            SnackBarUtils.show(et_mtitle, "请输入会议地点");
            return;
        }
        if (mContent == null || mContent.equals("")) {
            SnackBarUtils.show(et_mtitle, "请输入会议内容");
            return;
        }
        String startdate = tv_startdate.getText().toString();
        String enddate = tv_enddate.getText().toString();
        String starttime = tv_starttime.getText().toString();
        String endtime = tv_endtime.getText().toString();
        if (startdate == null || enddate == null || starttime == null || endtime == null ||
                startdate.equals("") || enddate.equals("") ||
                starttime.equals("") || endtime.equals("")) {
            SnackBarUtils.show(et_mtitle, "请补全会议时间");
            return;
        }
        boolean b = timeCompared();
        if (!b) {
            SnackBarUtils.show(et_mtitle, "会议开始时间不得晚与结束时间");
            return;
        }
        if (bluetoothMac.equals("") && wlanMac.equals("")) {
            SnackBarUtils.show(et_mtitle, "请添加会议签到管理设备");
            return;
        } else {

        }
    /*    mtitle:会议标题
        mcontent:会议内容
        maddress:会议地点
        uid:会议管理员
        mstarttime:会议开始时间
        mendtime:会议结束时间
        wlamac:会议签到设备wifi
        bluetoothmac:会议签到设备蓝牙
*/
        Intent intent = new Intent(CreateMeetingActivity.this, AdmUserInforActivity.class);
        intent.putExtra("mtitle", mtitle);
        intent.putExtra("mcontent", mContent);
        intent.putExtra("maddress", maddress);
        intent.putExtra("mstarttime", tv_startdate.getText().toString() +
                " " + tv_starttime.getText().toString());
        intent.putExtra("mendtime", tv_enddate.getText().toString() + " " + tv_endtime.getText().toString());
        intent.putExtra("wlanmac", wlanMac);
        intent.putExtra("bluetoothmac", bluetoothMac);
        intent.putExtra("mimage", mImgBase641);
        startActivity(intent);
        finish();
    }

    private boolean timeCompared() {
        Log.i(TAG, "timeCompared: " + mStartMonth + " " + mEndMonth
                + " " + mStartDay + mEndDay +
                " " + mStartHour + mEndHour
                + " " + mStartMinute + mEndMinute);
        if (mStartYear > mEndYear) {
            return false;
        } else {
            if (mStartMonth > mEndMonth) {
                return false;
            } else {
                if (mStartDay > mEndDay) {
                    return false;
                } else {
                    if (mStartHour > mEndHour) {
                        return false;
                    } else {
                        if (mStartMinute > mEndMinute) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case STARTDATE_DIALOG:
                return new DatePickerDialog(this, mStartdateListener, mStartYear, mStartMonth, mStartDay);
            case STARTTIME_DIALOG:
                return new TimePickerDialog(this, mStarttimeListener, mStartHour, mStartMinute, false);
            case ENDDATE_DIALOG:
                return new DatePickerDialog(this, mEnddateListener, mEndYear, mEndMonth, mEndDay);
            case ENDTIME_DIALOG:
                return new TimePickerDialog(this, mEndtimeListener, mEndHour, mEndMinute, false);
        }
        return null;
    }

    private DatePickerDialog.OnDateSetListener mStartdateListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            mStartYear = year;
            mStartMonth = monthOfYear;
            mStartMonth++;
            String month = mStartMonth + "";
            if (mStartMonth <= 9) {
                month = "0" + mStartMonth;
            }
            mStartDay = dayOfMonth;
            String day = mStartDay + "";
            if (dayOfMonth < 10) {
                day = "0" + mStartDay;
            }
            tv_startdate.setText(mStartYear + "-" + month + "-" + day);
            mEndYear = mStartYear;
            mEndMonth = mStartMonth;
            mEndDay = mStartDay;
            tv_enddate.setText(mStartYear + "-" + month + "-" + day);
        }
    };

    private TimePickerDialog.OnTimeSetListener mStarttimeListener = new TimePickerDialog.OnTimeSetListener() {

        @Override
        public void onTimeSet(TimePicker timePicker, int hour, int mounte) {
            mStartHour = hour;
            String shour = hour + "";
            if (hour < 10) {
                shour = "0" + shour;
            }
            mStartMinute = mounte;
            String sminute = mounte + "";
            if (mounte < 10) {
                sminute = "0" + sminute;
            }
            tv_starttime.setText(shour + ":" + sminute);
        }
    };

    private DatePickerDialog.OnDateSetListener mEnddateListener = new DatePickerDialog.OnDateSetListener() {

        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            mEndYear = year;
            mEndMonth = monthOfYear;
            mEndMonth++;
            String month = mEndMonth + "";
            if (mEndMonth <= 9) {
                month = "0" + mEndMonth;
            }
            mEndDay = dayOfMonth;
            String day = mEndDay + "";
            if (dayOfMonth < 10) {
                day = "0" + mEndDay;
            }
            tv_enddate.setText(mEndYear + "-" + month + "-" + day);
        }
    };

    private TimePickerDialog.OnTimeSetListener mEndtimeListener = new TimePickerDialog.OnTimeSetListener() {

        @Override
        public void onTimeSet(TimePicker timePicker, int hour, int mounte) {
            mEndHour = hour;
            String shour = hour + "";
            if (hour < 10) {
                shour = "0" + shour;
            }
            mEndMinute = mounte;
            String sminute = mounte + "";
            if (mounte < 10) {
                sminute = "0" + sminute;
            }
            tv_endtime.setText(shour + ":" + sminute);
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
            case xyz.hui_yi.R.id.action_ok:
                create();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(xyz.hui_yi.R.menu.menu_ok, menu);
        return true;
    }
}