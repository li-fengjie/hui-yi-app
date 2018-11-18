package xyz.hui_yi.net;

/**
 * Created by LiFen on 2018/2/10.
 */

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import xyz.hui_yi.adapter.MeetingListAdapter;
import xyz.hui_yi.bean.MeetingsBean;

import java.util.ArrayList;

import static xyz.hui_yi.activity.HistoryMeetsActivity.emptyLayout;


/**
 * 会议列表异步加载类
 */

public class MHListDownAsynctask extends AsyncTask<String, Void, byte[]>{
    private static final String TAG = "MHListDownAsynctask";
    ArrayList<MeetingsBean.Meeting> data;
    MeetingListAdapter adapter;
    Context context;

    public MHListDownAsynctask(ArrayList<MeetingsBean.Meeting> data, MeetingListAdapter adapter, Context context) {
        super();
        this.data = data;
        this.adapter = adapter;
        this.context = context;
    }

    /*
     * 当主线程中调用executeOnExecutor方法或execute方法时，会调用此方法
     */
    @Override
    protected byte[] doInBackground(String... params) {
        //下载网络数据
        return MHListNetUtils.getNetData(params[0],params[1]);
    }

    /*
     * doInBackground方法执行之后会执行此方法，并把结果传过来
     */
    @Override
    protected void onPostExecute(byte[] result) {
        super.onPostExecute(result);
        if (result != null) {
            //把从网络上获取的byte类型的数据转换为String字符串
            String jsonString = new String(result);
            //用json解析工具来解析该字符串数据
            MeetingsBean cb = JsonUtils.parseJson(jsonString);
            //取出data数据，并保存到集合中
            if(cb.result != null){
                data.addAll(cb.result);
                Log.i(TAG, "onPostExecute: " + data.size());
                if(data.size() == 0){
                    emptyLayout.showEmpty();
                }
            }else {
                emptyLayout.showEmpty();
                if(adapter.isEmpty() || cb.result.isEmpty()){
                    emptyLayout.showEmpty();
                    Log.i(TAG, "onPostExecute: " + "result数据为空，有可能格式错误");
                }
            }
            //刷新数据
            adapter.notifyDataSetChanged();
        }else {
            Toast.makeText(context, "网络异常", Toast.LENGTH_SHORT).show();
            emptyLayout.showError();
        }
    }
}