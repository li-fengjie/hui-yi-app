package xyz.hui_yi.net;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import xyz.hui_yi.adapter.QianDaoListAdapter;
import xyz.hui_yi.bean.QianDaosBean;
import xyz.hui_yi.constants.Constants;
import xyz.hui_yi.fragment.QianDao;

import java.util.ArrayList;


/**
 * Created by LiFen on 2018/2/16.
 * 要举行会议签到列表异步获取
 */

public class QListDownAsynctask extends AsyncTask<String, Void, byte[]> {
    private static final String TAG = "QListDownAsynctask";
    private ArrayList<QianDaosBean.QianDaos> data;
    private QianDaoListAdapter adapter;
    private Context context;
    private Handler handler;

    public QListDownAsynctask(ArrayList<QianDaosBean.QianDaos> data,
                              QianDaoListAdapter adapter,
                              Context context, Handler handler) {
        super();
        this.data = data;
        this.adapter = adapter;
        this.context = context;
        this.handler = handler;
    }

    /*
     * 当主线程中调用executeOnExecutor方法或execute方法时，会调用此方法
     */
    @Override
    protected byte[] doInBackground(String... params) {
        //下载网络数据
        return QListNetUtils.getNetData(params[0], params[1]);
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
            QianDaosBean cb = JsonUtils.qparseJson(jsonString);
            //取出data数据，并保存到集合中
            if (cb.result != null) {
                data.addAll(cb.result);
                Message message = new Message();
                message.what = Constants.DATA_NUM;
                message.obj = cb.result.size();
                handler.sendMessage(message);
            } else {
                Log.i(TAG, "onPostExecute: " + "result数据为空，有可能格式错误");
                Message message = new Message();
                message.what = Constants.DATA_NUM;
                message.obj = 0;
                handler.sendMessage(message);
                QianDao.emptyLayout.showEmpty();
            }
            //刷新数据
            adapter.notifyDataSetChanged();
//            footView.setVisibility(View.INVISIBLE);
        } else {
            Toast.makeText(context, "网络异常", Toast.LENGTH_SHORT).show();
            Message message = new Message();
            message.what = Constants.DATA_NUM;
            message.obj = 0;
            handler.sendMessage(message);
            QianDao.emptyLayout.showError();
        }
    }
}