package xyz.hui_yi.net;


import xyz.hui_yi.bean.AdmQianDaoBean;
import xyz.hui_yi.bean.MeetingsBean;
import xyz.hui_yi.bean.QianDaosBean;
import xyz.hui_yi.bean.UsersInforBean;
import com.google.gson.Gson;

/**
 * Created by LiFen on 2018/2/10.
 * json 工具类
 */

public class JsonUtils {
    public static MeetingsBean parseJson(String jsonString){
        Gson gson = new Gson();
        MeetingsBean cb = gson.fromJson(jsonString, MeetingsBean.class);
        return cb;
    }

    public static UsersInforBean uparseJson(String jsonString){
        Gson gson = new Gson();
        UsersInforBean cb = gson.fromJson(jsonString, UsersInforBean.class);
        return cb;
    }

    public static QianDaosBean qparseJson(String jsonString){
        Gson gson = new Gson();
        QianDaosBean cb = gson.fromJson(jsonString, QianDaosBean.class);
        return cb;
    }

    public static AdmQianDaoBean admqparseJson(String jsonString){
        Gson gson = new Gson();
        AdmQianDaoBean cb = gson.fromJson(jsonString, AdmQianDaoBean.class);
        return cb;
    }
}