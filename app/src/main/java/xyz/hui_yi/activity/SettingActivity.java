package xyz.hui_yi.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import xyz.hui_yi.utils.PrefUtils.PrefUtils;
import xyz.hui_yi.constants.Constants;


/**
 * Created by LiFen on 2018/2/4.
 * 系统设置界面
 */

public class SettingActivity extends AppCompatActivity {
    private static final String TAG = "SettingActivity";
    private ListView lv_setting;
    private String [] items;
    private String [] initsw = {Constants.VIBRATE_KEY, Constants.VOIUME_OFF_ON_KEY};

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
        setContentView(xyz.hui_yi.R.layout.activity_setting);

        initView();
        items = getResources().getStringArray(xyz.hui_yi.R.array.settingitem);
        MyAdapter myAdapter = new MyAdapter();
        lv_setting.setAdapter(myAdapter);
    }

    private void initView() {
        lv_setting = (ListView) findViewById(xyz.hui_yi.R.id.lv_setting);
    }

    private class MyAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return items.length;
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
        public View getView(final int position, View convertView, ViewGroup parent) {
            View view;
            if (convertView == null){
                view = View.inflate(SettingActivity.this, xyz.hui_yi.R.layout.item_setting,null);
            }else{
                view = convertView;
            }
            TextView tv_setting = (TextView) view.findViewById(xyz.hui_yi.R.id.tv_mtitle);
            tv_setting.setText(items[position]);
            Switch sw_setting = (Switch) view.findViewById(xyz.hui_yi.R.id.sw_setting);
            sw_setting.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    PrefUtils.setBoolean(getApplication(),initsw[position],isChecked);
                }
            });
            boolean b = PrefUtils.getBoolean(getApplication(),initsw[position], true);
            sw_setting.setChecked(b);
            return view;
        }
    }

}
