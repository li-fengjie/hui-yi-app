package xyz.hui_yi.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import xyz.hui_yi.R;
import xyz.hui_yi.activity.CreateMeetingActivity;
import xyz.hui_yi.activity.MainActivity;
import xyz.hui_yi.activity.MeetingInforActivity;
import xyz.hui_yi.adapter.MeetingListAdapter;
import xyz.hui_yi.bean.MeetingsBean;
import xyz.hui_yi.layout.EmptyLayout;
import xyz.hui_yi.layout.MySwipeRefreshLayout;
import xyz.hui_yi.net.MListDownAsynctask;

import static android.content.ContentValues.TAG;
import static xyz.hui_yi.activity.MainActivity.userInfo;

/**
 * Created by LiFen on 2018/1/15.
 * 会议展示界面
 */

public class HuiYi extends Fragment {

    public static ArrayList<MeetingsBean.Meeting> data;
    private MeetingListAdapter adapter;
    private ExecutorService es;
    private boolean flag = false;
    private int pageNo = 0;
    public static EmptyLayout emptyLayout;
//    private ImageView ibtn_refresh;
    private MySwipeRefreshLayout mSwipeLayout;
    private ListView lv;
//    private ImageView ibtn_add;
    private View inflate;
    private FloatingActionButton fab;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if(data.isEmpty()){
            es = Executors.newFixedThreadPool(10);
            String url = getResources().getString(R.string.URL);
            url = url + "/public/api/Meet/uMeetList";
            new MListDownAsynctask(data,adapter,getContext()).executeOnExecutor(es, url,userInfo.getUid(),pageNo+"");
            emptyLayout.hide();
        }
    }
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        inflate = inflater.inflate(R.layout.fragment_huiyi, container, false);
        initView();
        prepareData();
        configLogic();
        return inflate;
    }

    private void configLogic() {
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //这里可以做一下下拉刷新的操作
                String url = getResources().getString(R.string.URL);
                url = url + "/public/api/Meet/uMeetList";
//                adapter = new MeetingListAdapter(data,getActivity());
                data.clear();
                new MListDownAsynctask(data,adapter,getContext()).execute(url,userInfo.getUid(),0+"");
                emptyLayout.hide();
                //为了保险起见可以先判断当前是否在刷新中（旋转的小圈圈在旋转）....
                if(mSwipeLayout.isRefreshing()){
                    //关闭刷新动画
                    mSwipeLayout.setRefreshing(false);
                }
            }
        });

        if(MainActivity.isUser){
            fab.setVisibility(View.GONE);
        }else {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getContext(),CreateMeetingActivity.class);
                    startActivity(intent);
                }
            });
        }

        /*ibtn_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animation = AnimationUtils.loadAnimation(getContext(), R.anim.anim_refresh);
                ibtn_refresh.setAnimation(animation);
                ibtn_refresh.startAnimation(animation);
                //TODO 会议刷新
                mSwipeLayout.setRefreshing(true);
                String url = getResources().getString(R.string.URL);
                url = url + "/public/api/Meet/meetlist";
                data.clear();
                new MListDownAsynctask(data,adapter,getContext()).execute(url,userInfo.getUid(),0+"");
                emptyLayout.hide();
                new Handler().postDelayed(new Runnable(){
                    public void run() {
                        if(mSwipeLayout.isRefreshing()){
                            //关闭刷新动画
                            mSwipeLayout.setRefreshing(false);
                        }
                    }
                }, 2000);
            }
        });*/

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView tv_maddress = (TextView) view.findViewById(R.id.tv_maddress);
                TextView tv_mid = (TextView) view.findViewById(R.id.tv_mid);
                TextView tv_mtime = (TextView) view.findViewById(R.id.tv_mtime);
                TextView tv_mtitle = (TextView) view.findViewById(R.id.tv_mtitle);

                //TODO 联网获取会议内容
                Intent intent = new Intent(getContext(), MeetingInforActivity.class);
                intent.putExtra("mtitle",tv_mtitle.getText().toString());
                intent.putExtra("maddress",tv_maddress.getText().toString());
                intent.putExtra("mtime",tv_mtime.getText().toString());
                intent.putExtra("mid",tv_mid.getText().toString());
                startActivity(intent);
            }
        });

        if(!MainActivity.isUser){
            lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    new AlertDialog.Builder(getContext()).setTitle("会议操作")
                            .setIcon(android.R.drawable.ic_dialog_info)
                            .setPositiveButton("修改", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
//                           TODO 联网获取信息修改
                                }
                            })
                            .setNegativeButton("删除", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    //TODO 联网删除操作
                                }
                            })
                            .show();
                    return true;
                }
            });
        }

        /*
         * 对listview设置滚动监听事件，实现分页加载数据
         */
        lv.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                //如果停止了滑动且滑动到了结尾，则更新数据,加载下一页
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE && flag == true) {
//                    View footView = View.inflate(getContext(), R.layout.footer, null);
                    pageNo += 1;
                    Log.i(TAG, "onScrollStateChanged: pageNoDEGE" + pageNo);
                    String url = getResources().getString(R.string.URL);
                    url = url + "/public/api/Meet/uMeetList";
                    new MListDownAsynctask(data,adapter,getActivity()).executeOnExecutor(es, url,userInfo.getUid(),pageNo+"");
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                //判断是否滑动到了结尾
                if (firstVisibleItem + visibleItemCount == totalItemCount) {
                    flag = true;
                }else {
                    flag = false;
                }
            }
        });
    }

    private void prepareData() {
        pageNo = 0;
        data = new ArrayList<MeetingsBean.Meeting>();
        adapter = new MeetingListAdapter(data,getActivity());
        lv.setAdapter(adapter);
    }

    private void initView() {
        setHasOptionsMenu(true);
        lv = (ListView) inflate.findViewById(R.id.lv);
//        ibtn_add = (ImageView) inflate.findViewById(R.id.ibtn_add);
        fab = (FloatingActionButton) inflate.findViewById(R.id.fab);
//        ibtn_refresh = (ImageView) inflate.findViewById(ibtn_refresh);
        emptyLayout = (EmptyLayout) inflate.findViewById(R.id.emptyLayout);
        /*
        mSwipeLayout谷歌自带下拉刷新
         */
        mSwipeLayout = (MySwipeRefreshLayout) inflate.findViewById(R.id.swipe_ly);
        mSwipeLayout.setScrollUpChild(lv);
        mSwipeLayout.setColorSchemeResources(R.color.colorPrimary,R.color.colorYellow,R.color.colorAccent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_refresh:
                //TODO 会议刷新
                mSwipeLayout.setRefreshing(true);
                String url = getResources().getString(R.string.URL);
                url = url + "/public/api/Meet/uMeetList";
                data.clear();
                new MListDownAsynctask(data,adapter,getContext()).execute(url,userInfo.getUid(),0+"");
                emptyLayout.hide();
                new Handler().postDelayed(new Runnable(){
                    public void run() {
                        if(mSwipeLayout.isRefreshing()){
                            //关闭刷新动画
                            mSwipeLayout.setRefreshing(false);
                        }
                    }
                }, 2000);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }
}