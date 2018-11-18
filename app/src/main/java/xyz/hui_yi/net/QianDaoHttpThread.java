package xyz.hui_yi.net;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import static xyz.hui_yi.constants.Constants.PID_FAIL;
import static xyz.hui_yi.constants.Constants.QIANDAO_FAIL;
import static xyz.hui_yi.constants.Constants.QIANDAO_SUCCESS;
import static xyz.hui_yi.constants.Constants.SERVER_ERROR;


/**
 * Created by LiFen on 2018/2/16.
 * 签到人员签到线程类
 */

public class QianDaoHttpThread extends Thread {
    private static final String TAG = "QianDaoHttpThread";
    private String url;
    private String uid;
    private String sign_id;
    private String mid;
    private String pid;
    private String rssi;
    private Handler mHandler;
    private ByteArrayOutputStream baos;
    private String uname;

    public QianDaoHttpThread(Handler handler, String url,
                             String uid, String sign_id,String mid,String pid, String rssi){
        this.mHandler = handler;
        this.url = url;
        this.uid = uid;
        this.sign_id = sign_id;
        this.mid = mid;
        this.pid = pid;
        this.rssi = rssi;
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
            Log.i(TAG, "doPost: " + uid + " " + sign_id + " "  + rssi + " " + pid);
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder
                    .append("uid=").append(URLEncoder.encode(uid, "utf-8"))
                    .append("&sign_id=").append(URLEncoder.encode(sign_id, "utf-8"))
                    .append("&rssi=").append(URLEncoder.encode(rssi, "utf-8"))
                    .append("&pid=").append(URLEncoder.encode(pid, "utf-8"))
                    .append("&mid=").append(URLEncoder.encode(mid, "utf-8"));
//            conn.setRequestProperty("Charset","UTF-8");
//            conn.setRequestProperty("Content-Length", String.valueOf(stringBuilder.toString().getBytes()));
//            conn.setRequestProperty("Content-type","application/json");
            // 获取向服务器写数据的输出流
            Log.i(TAG, "doPost: " + stringBuilder.toString());
//            conn.setRequestProperty("Content-Length", String.valueOf(stringBuilder.toString().getBytes().length));
            // 打开连接
            conn.connect();
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
                Log.i(TAG, "doPost: " + result.toString());
                JSONObject resultt = new JSONObject(baos.toString());
                int code = Integer.parseInt(resultt.getString("code"));
                if(code == 20031){
                    uname = resultt.getString("uname");
                }else {
                    uname = "";
                }
                sendMessage(code);
            }else {
                sendMessage(500);
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
            case 20031:
                message.what = QIANDAO_SUCCESS;
                message.obj = uname;
                break;
            case 10032:
                message.what = PID_FAIL;
                break;
            case 500:
                message.what = SERVER_ERROR;
                break;
            default:
                message.what = QIANDAO_FAIL;
                break;
        }
        mHandler.sendMessage(message);
    }

    @Override
    public void run() {
        doPost();
    }
}
