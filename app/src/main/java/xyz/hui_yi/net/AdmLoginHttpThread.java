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
 * Created by LiFen on 2018/2/8.
 * 管理员登陆联网操作
 */

public class AdmLoginHttpThread extends Thread {
    private static final String TAG = "AdmLoginHttpThread";
    private String uid;
    private String pwd;
    private String pcode;
    private Handler mHandler;
    private String url;
    private ByteArrayOutputStream baos;

    public AdmLoginHttpThread(Handler handler,String url,String uid, String pwd,String pcode){
        this.mHandler = handler;
        this.uid =  uid;
        this.pwd = pwd;
        this.pcode = pcode;
        this.url = url;
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

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder
                    .append("uid=").append(URLEncoder.encode(uid, "utf-8"))
                    .append("&pwd=").append(URLEncoder.encode(pwd, "utf-8"))
                    .append("&pcode=").append(URLEncoder.encode(pcode, "utf-8"));
//            conn.setRequestProperty("Charset","UTF-8");
//            conn.setRequestProperty("Content-Length", String.valueOf(stringBuilder.toString().getBytes()));
//            conn.setRequestProperty("Content-type","application/json");
            // 获取向服务器写数据的输出流
            Log.i(TAG, "doPost: " + stringBuilder.toString());
//            conn.setRequestProperty("Content-Length", String.valueOf(stringBuilder.toString().getBytes().length));
            // 打开连接
            try {
                conn.connect();
            }catch (Exception e){
                sendMessage(1025);
            }
            conn.getOutputStream().write(stringBuilder.toString().getBytes());

            byte[] result = null;
            Log.i(TAG, "HttpCode " + conn.getResponseCode());
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
            }else sendMessage(30000);
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
            case 20003:
                message.what = Constants.ADM_LOGIN_SUCCESS;
                break;
            case 10011:
                message.what = Constants.LOGIN_PCODE_FAIL;
                break;
            case 30000:
                message.what = Constants.SERVER_ERROR;
                break;
            case 1025:
                message.what = Constants.NET_ERROR;
                break;
            default:
                message.what = Constants.LOGIN_FAIL;
                break;
        }
        mHandler.sendMessage(message);
    }

    @Override
    public void run() {
        doPost();
    }
}
