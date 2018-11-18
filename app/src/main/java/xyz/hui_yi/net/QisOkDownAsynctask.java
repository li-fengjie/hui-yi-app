package xyz.hui_yi.net;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import xyz.hui_yi.activity.AdmDataManageActivity;
import xyz.hui_yi.adapter.QianDaoIsOkAdapter;
import xyz.hui_yi.adapter.QianDaoNotOkAdapter;
import xyz.hui_yi.bean.AdmQianDaoBean;

import java.util.ArrayList;

/**
 * Created by LiFen on 2018/2/18.
 * 会议签到成功人员列表信息异步获取类
 */

public class QisOkDownAsynctask extends AsyncTask<String, Void, byte[]> {
    private static final String TAG = "QisOkDownAsynctask";
    private ArrayList<AdmQianDaoBean.IsOk> isok;
    private ArrayList<AdmQianDaoBean.NotOk> notok;
    private QianDaoIsOkAdapter isOkAdapter;
    private QianDaoNotOkAdapter notOkAdapter;
    private Context context;
    private Handler handler;

    public QisOkDownAsynctask(ArrayList<AdmQianDaoBean.IsOk> isok,
                              ArrayList<AdmQianDaoBean.NotOk> notok,
                              QianDaoIsOkAdapter isOkAdapter,
                              QianDaoNotOkAdapter notOkAdapter,
                              Context context, Handler handler) {
        super();
        this.isok = isok;
        this.notok = notok;
        this.isOkAdapter = isOkAdapter;
        this.notOkAdapter = notOkAdapter;
        this.context = context;
        this.handler = handler;
    }

    /*
     * 当主线程中调用executeOnExecutor方法或execute方法时，会调用此方法
     */
    @Override
    protected byte[] doInBackground(String... params) {
        //下载网络数据
        return AdmQianDaoNetUtils.getNetData(params[0], params[1]);
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
            AdmQianDaoBean cb = JsonUtils.admqparseJson(jsonString);
            //取出isok数据，并保存到集合中
            Log.i(TAG, "onPostExecute: isok" + cb.isok.size());
            if (cb.isok != null) {
                isok.addAll(cb.isok);
            } else {
                Log.i(TAG, "onPostExecute: " + "result数据为空，有可能格式错误");
                //
            }
            Log.i(TAG, "onPostExecute: notok" + cb.notok.size());
            if (cb.notok != null) {
                notok.addAll(cb.notok);
            } else {
                Log.i(TAG, "onPostExecute: " + "result数据为空，有可能格式错误");
            }
            //刷新数据
            isOkAdapter.notifyDataSetChanged();
            notOkAdapter.notifyDataSetChanged();

            Message message = new Message();
            message.what = AdmDataManageActivity.LOGIN_DATA;
            message.obj = cb.isok.size()+"," +cb.notok.size();
            handler.sendMessage(message);
        } else {
            Toast.makeText(context, "网络异常", Toast.LENGTH_SHORT).show();
            Message message = new Message();
            message.what = AdmDataManageActivity.NOT_NETWORK;
            handler.sendMessage(message);
        }
    }
}