package xyz.hui_yi.net;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import xyz.hui_yi.activity.MeetingInforActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

/**
 * Created by LiFen on 2018/2/12.
 * 某一会议详细信息获取线程类
 */

public class MeetingInforHttpThread extends Thread {
    private static final String TAG = "MeetingInforHttpThread";
    private Handler mHandler;
    private String url;
    private String mtitle;
    private String mcontent;
    private String mid;
    private String mstarttime;
    private String mendtime;
    private String maddress;
    private String mmanager;
    private ByteArrayOutputStream baos;
    private String mimage;

    public MeetingInforHttpThread(Handler handler,String url,String mid){
        this.mHandler = handler;
        this.url = url;
        this.mid = mid;
    }

    private void doPost(){
        try {
            URL httpUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Connection", "Keep-Alive");
            // 设置可向服务器输出
            conn.setDoOutput(true);
            conn.setReadTimeout(10000);
            conn.setConnectTimeout(10000);
            // 打开连接后，向服务端写要提交的参数
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder
                    .append("mid=").append(URLEncoder.encode(mid, "utf-8"));
            conn.setRequestProperty("Content-Length", String.valueOf(stringBuilder.toString().getBytes().length));
            // 获取向服务器写数据的输出流
            // 打开连接
            Log.i(TAG, "doPost: " + stringBuilder.toString());
            try {
                conn.connect();
            }catch (Exception e){
                Log.e(TAG, "doPost: ", e);
                sendMessage(10005);
            }
            conn.getOutputStream().write(stringBuilder.toString().getBytes());

            byte[] result = null;
            Log.i(TAG, "Code" + conn.getResponseCode());
            if (conn.getResponseCode() == 200) {
                InputStream is = conn.getInputStream();
                baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = 0;
                while((len=is.read(buffer))!=-1){
                    baos.write(buffer, 0, len);
                }
                Log.i(TAG, "getNetData: " + baos.toString());
                result = baos.toByteArray();
                JSONObject jresult = new JSONObject(baos.toString());
                int code = Integer.parseInt(jresult.getString("code"));
                //解析会议详细信息，通过mHandler 发送到ui
                String result1 = jresult.getString("result");
                JSONObject resultt = new JSONObject(result1);
                mcontent = resultt.getString("mcontent");
                mmanager = resultt.getString("uname");
                mendtime = resultt.getString("mendtime");
                mimage = resultt.getString("mimage");
                Log.i(TAG, "doPost: \n结束时间" + mendtime + "\n发布人" + mmanager);
                sendMessage(code);
            }else {
                sendMessage(10005);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //向UI线程发送代表登录结果的消息
    private void sendMessage(int code){
        Message message = new Message();
        switch (code){
            case 20011:
                message.obj = mcontent + "," + mmanager+ "," +mendtime + "," + mimage;
                message.what = MeetingInforActivity.INFOR_SUCCESS;
                break;
            default:
                message.what = MeetingInforActivity.INFOR_FAIL;
                break;
        }
        mHandler.sendMessage(message);
    }

    @Override
    public void run() {
        doPost();
    }
}
