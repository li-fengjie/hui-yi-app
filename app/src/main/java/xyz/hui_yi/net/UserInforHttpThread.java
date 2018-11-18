package xyz.hui_yi.net;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import xyz.hui_yi.fragment.WoDe;
import xyz.hui_yi.activity.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * Created by LiFen on 2018/2/8.
 * 我的个人信息获取线程类
 */

public class UserInforHttpThread extends Thread{
    private static final String TAG = "UserInforHttpThread";
    private String uid;
    private String uname;
    private String uphoto;
    private String bname;
    /*管理员 发表会议数*/
    /* 签到用户 已参加/应参加*/
    private String meetnum;
    private Handler mHandler;
    private String url;

    public UserInforHttpThread(Handler handler,String url,String uid){
        this.mHandler = handler;
        this.url = url;
        this.uid = uid;
    }

    private void doPost(){
        try {
            URL httpUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
            conn.setRequestMethod("POST");
//            conn.setDoOutput(true);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.connect();
            conn.setReadTimeout(5000);
            OutputStream out = conn.getOutputStream();
            String content = "uid=" + uid;
            out.write(content.getBytes());
            out.flush();
            // 执行完out.close()后，POST请求结束
            out.close();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuffer sb = new StringBuffer();
            String str;
            while((str = reader.readLine())!= null){
                sb.append(str);
            }
            Log.i(TAG, "doPost: " + sb.toString());

//            int c = conn.getResponseCode();

            JSONObject result = new JSONObject(sb.toString());
            if(result == null){
                return;
            }
            int code = result.getInt("code");
            if(code == 10024){
                sendMessage(code);
                return;
            }
            String result1 = result.getString("result");
            JSONObject resultt = new JSONObject(result1);
            uname = resultt.getString("uname");
            MainActivity.userInfo.setUname(uname);
            bname = resultt.getString("bname");
            MainActivity.userInfo.setBname(bname);
            meetnum = resultt.getString("meetnum");
            MainActivity.userInfo.setMeetnum(meetnum);
            uphoto = resultt.getString("uphoto");
            MainActivity.userInfo.setUphoto(uphoto);
            Log.i(TAG, "doPost: " + uname + " " + bname+ " " + meetnum+ " " + uphoto+ "\n");
            sendMessage(code);
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
            case 20013:
                message.what = WoDe.INFOR_SUCCESS;
                break;
            default:
                message.what = WoDe.INFOR_FAIL;
                break;
        }
        mHandler.sendMessage(message);
    }

    public void run(){
        doPost();
    }
}
