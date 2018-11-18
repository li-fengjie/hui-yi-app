package xyz.hui_yi.net;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import xyz.hui_yi.constants.Constants;

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
 * Created by LiFen on 2018/2/7.
 * 签到用户登陆线程类
 */

public class UserLoginHttpThread extends Thread {
    private static final String TAG = "UserLoginThread";
    private String url = null;
    private String name = null;
    private String pwd = null;
    private String pid = null;
    private Handler mHandler = null;
    private ByteArrayOutputStream baos;

    public UserLoginHttpThread(Handler handler, String url, String name, String pwd, String pid){
        this.url = url;
        this.name = name;
        this.pwd = pwd;
        this.pid = pid;
        this.mHandler = handler;
    }

    private void doPost(){
        try {
            URL httpUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Connection", "Keep-Alive");
//   conn.setRequestProperty("Content-type","application/json");
            // 设置可向服务器输出
            conn.setDoOutput(true);
            conn.setReadTimeout(3000);
            conn.setConnectTimeout(3000);
            // 打开连接
            try {
                conn.connect();
            }catch (Exception e){
                sendMessage(1025);
            }

            // 打开连接后，向服务端写要提交的参数
            // 参数格式：“name=asdasdas&age=123123”
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder
                    .append("uid=").append(URLEncoder.encode(name, "utf-8"))
                    .append("&pwd=").append(URLEncoder.encode(pwd, "utf-8"))
                    .append("&pid=").append(URLEncoder.encode(pid, "utf-8"));
//            conn.setRequestProperty("Charset","UTF-8");
            // 获取向服务器写数据的输出流
            conn.getOutputStream().write(stringBuilder.toString().getBytes());

            byte[] result = null;
            Log.i(TAG, "doPost: " + conn.getResponseCode());
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
                Log.i(TAG, "doPost: " + baos.toString());
                JSONObject resultt = new JSONObject(baos.toString());
                int code = Integer.parseInt(resultt.getString("code"));
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
            case 20004:
                message.what = Constants.USER_LOGIN_SUCCESS;
                break;
            case 10010:
                message.what = Constants.LOGIN_FAIL;
                break;
            case 10009:
                message.what = Constants.LOGIN_FAIL;
                break;
            case 500:
                message.what = Constants.SERVER_ERROR;
                break;
            case 1025:
                message.what = Constants.NET_ERROR;
                break;
            default:
                message.what = Constants.LOGIN_FAIL;
        }
        mHandler.sendMessage(message);
    }

    @Override
    public void run() {
        doPost();
    }
}
