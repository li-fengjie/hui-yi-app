package xyz.hui_yi.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import xyz.hui_yi.adapter.MeetingListAdapter;
import xyz.hui_yi.bean.MeetingsBean;
import xyz.hui_yi.layout.EmptyLayout;
import xyz.hui_yi.net.MHListDownAsynctask;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by LiFen on 2018/4/15.
 */

public class HistoryMeetsActivity extends AppCompatActivity {
    private ListView lv;
    public static ArrayList<MeetingsBean.Meeting> data;
    private MeetingListAdapter adapter;
    private ExecutorService es;
    public static EmptyLayout emptyLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(xyz.hui_yi.R.layout.activity_historymeets);
        initView();
        prepareData();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (MainActivity.isUser) {
                    TextView tv_maddress = (TextView) view.findViewById(xyz.hui_yi.R.id.tv_maddress);
                    TextView tv_mid = (TextView) view.findViewById(xyz.hui_yi.R.id.tv_mid);
                    TextView tv_mtime = (TextView) view.findViewById(xyz.hui_yi.R.id.tv_mtime);
                    TextView tv_mtitle = (TextView) view.findViewById(xyz.hui_yi.R.id.tv_mtitle);

                    //TODO 联网获取会议内容
                    Intent intent = new Intent(getApplication(), MeetingInforActivity.class);
                    intent.putExtra("mtitle", tv_mtitle.getText().toString());
                    intent.putExtra("maddress", tv_maddress.getText().toString());
                    intent.putExtra("mtime", tv_mtime.getText().toString());
                    intent.putExtra("mid", tv_mid.getText().toString());
                    startActivity(intent);
                } else {
                    TextView tv_mid = (TextView) view.findViewById(xyz.hui_yi.R.id.tv_mid);
                    Intent intent = new Intent(getApplicationContext(), AdmDataManageActivity.class);
                    intent.putExtra("mid", tv_mid.getText().toString());
                    startActivity(intent);
                }
            }
        });
    }

    private void prepareData() {
        data = new ArrayList<MeetingsBean.Meeting>();
        adapter = new MeetingListAdapter(data, getApplicationContext());
        lv.setAdapter(adapter);
        data.clear();
        es = Executors.newFixedThreadPool(10);
        String url = getResources().getString(xyz.hui_yi.R.string.URL);
        url = url + "/public/api/Meet/historyMeet";
        new MHListDownAsynctask(data, adapter, getApplicationContext()).executeOnExecutor(es, url, MainActivity.userInfo.getUid());
        emptyLayout.hide();
    }

    private void initView() {
        lv = (ListView) findViewById(xyz.hui_yi.R.id.lv);
        emptyLayout = (EmptyLayout) findViewById(xyz.hui_yi.R.id.emptyLayout);
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
