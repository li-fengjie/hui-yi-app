package xyz.hui_yi.net;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import xyz.hui_yi.activity.PeopleActivity;
import xyz.hui_yi.adapter.UserInfoAdapter;
import xyz.hui_yi.bean.UsersInforBean;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by LiFen on 2018/2/13.
 * 联网异步加载参会人员数据
 */

public class UListDownAsynctask extends AsyncTask<String, Void, byte[]> {
    private static final String TAG = "UListDownAsynctask";
    ArrayList<UsersInforBean.UserInfo> data;
    UserInfoAdapter adapter;
    Context context;
    HashMap<Integer, Boolean> isSelected;
    Handler handler;

    public UListDownAsynctask(ArrayList<UsersInforBean.UserInfo> data, UserInfoAdapter adapter, Context context, HashMap<Integer, Boolean> isSelected) {
        super();
        this.data = data;
        this.adapter = adapter;
        this.context = context;
        this.isSelected =isSelected;
    }

    public UListDownAsynctask(Handler handler, ArrayList<UsersInforBean.UserInfo> data, UserInfoAdapter adapter, Context context, HashMap<Integer, Boolean> isSelected) {
        super();
        this.data = data;
        this.adapter = adapter;
        this.context = context;
        this.isSelected =isSelected;
        this.handler = handler;
    }

    /*
     * 当主线程中调用executeOnExecutor方法或execute方法时，会调用此方法
     */
    @Override
    protected byte[] doInBackground(String... params) {
        //下载网络数据
        return UListNetUtils.getNetData(params[0],params[1]);
//        return NetUtils.getNetData(params[0],params[1],params[2],params[3]);
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
            UsersInforBean cb = JsonUtils.uparseJson(jsonString);
            //取出data数据，并保存到集合中
            if(cb.result != null){
                data.addAll(cb.result);
                if(handler != null){
                    Message msg = new Message();
                    msg.what = PeopleActivity.PEOPLENUM;
                    msg.obj = data.size();
                    handler.sendMessage(msg);
                }
            }else {
                Log.i(TAG, "onPostExecute: " + "result数据为空，有可能格式错误");
                //
            }
            //刷新数据
            adapter.notifyDataSetChanged();
            for (int i = 0; i < data.size(); i++) {
                isSelected.put(i, false);
            }
        }else {
            Toast.makeText(context, "网络异常", Toast.LENGTH_SHORT).show();
        }
    }
}