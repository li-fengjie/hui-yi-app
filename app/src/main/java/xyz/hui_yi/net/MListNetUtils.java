package xyz.hui_yi.net;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by LiFen on 2018/2/10.
 * 会议列表联网获取工具类
 */

public class MListNetUtils {
    private static final String TAG = "MListNetUtils";
    public static byte[] getNetData(String urlString,String uid,String pageNo){
        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            // 设置可向服务器输出
            conn.setDoOutput(true);
            conn.setReadTimeout(40000);
            conn.setConnectTimeout(40000);
            // 打开连接
            conn.connect();

            // 打开连接后，向服务端写要提交的参数
            // 参数格式：“name=asdasdas&age=123123”
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("uid=")
            .append(uid)
            .append("&")
            .append("pageNo=")
            .append(pageNo);
            // 获取向服务器写数据的输出流
            conn.getOutputStream().write(stringBuilder.toString().getBytes());

            byte[] result = null;
            Log.i(TAG, "getNetData: Code"+conn.getResponseCode());
            if (conn.getResponseCode() == 200) {
                InputStream is = conn.getInputStream();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = 0;
                while((len=is.read(buffer))!=-1){
                    baos.write(buffer, 0, len);
                }
                Log.i(TAG, "getNetData: " + baos.toString());
                result = baos.toByteArray();
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}