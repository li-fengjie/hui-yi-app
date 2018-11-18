package xyz.hui_yi.net;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import xyz.hui_yi.activity.AdmUserInforActivity;

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
 * Created by LiFen on 2018/2/13.
 * 会议创建线程类
 */

public class CreateMeetingHttpThread extends Thread {
    private static final String TAG = "CreateMeetingHttpThread";
    private String url;
    private Handler mHandler;
    private String mtitle;
    private String mcontent;
    private String maddress;
    private String uid;
    private String mstarttime;
    private String mendtime;
    private String wlanmac;
    private String bluetoothmac;
    private String uidset;
    private ByteArrayOutputStream baos;
    private String mid;

    public CreateMeetingHttpThread(Handler handler,String url,String mtitle,
                                   String mcontent,String maddress,String uid,
                                   String mstarttime,String mendtime,String wlanmac,
                                   String bluetoothmac,String uidset){
        this.mHandler = handler;
        this.url = url;
        this.mtitle = mtitle; this.mcontent = mcontent;
        this.maddress = maddress;
        this.uid = uid;
        this.mstarttime = mstarttime; this.mendtime = mendtime;
        this.wlanmac = wlanmac; this.bluetoothmac = bluetoothmac;
        this.uidset = uidset;
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
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(5000);
            // 打开连接后，向服务端写要提交的参数
            /*String content = "mtitle=" + mtitle +
                                "&mcontent=" + mcontent+
                                "&maddress=" + maddress+
                                "&uid=" + uid+
                                "&mstarttime=" + mstarttime +
                                "&mendtime=" + mendtime
                                +"&wlanmac=" + wlanmac+
                                "&bluetoothmac=" + bluetoothmac
                                +"&uidset=" + uidset;*/
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder
                    .append("mtitle=").append(URLEncoder.encode(mtitle, "utf-8"))
                    .append("&mcontent=").append(URLEncoder.encode(mcontent, "utf-8"))
                    .append("&maddress=").append(URLEncoder.encode(maddress, "utf-8"))
                    .append("&uid=").append(URLEncoder.encode(uid, "utf-8"))
                    .append("&mstarttime=").append(URLEncoder.encode(mstarttime, "utf-8"))
                    .append("&mendtime=").append(URLEncoder.encode(mendtime, "utf-8"))
                    .append("&wlanmac=").append(URLEncoder.encode(wlanmac, "utf-8"))
                    .append("&bluetoothmac=").append(URLEncoder.encode(bluetoothmac, "utf-8"))
                    .append("&uidset=").append(URLEncoder.encode(uidset, "utf-8"));
//            conn.setRequestProperty("Charset","UTF-8");
            conn.setRequestProperty("Content-Length", String.valueOf(stringBuilder.toString().getBytes().length));
            Log.i(TAG, "doPost: Content-Length: "+ stringBuilder.toString().getBytes().length);
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
                Log.i(TAG, "doPost: " + baos.toString());
                JSONObject resultt = new JSONObject(baos.toString());
                int code = Integer.parseInt(resultt.getString("code"));
                mid = resultt.getString("mid");
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
            case 20030:
                message.what = AdmUserInforActivity.CREATE_SUCCESS;
                message.obj = mid;
                break;
            case 10013:
                message.what = AdmUserInforActivity.CREATE_FAIL;
                break;
            default :
                break;
        }
        mHandler.sendMessage(message);
    }

    public void run(){
        doPost();
    }
}
