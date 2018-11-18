package xyz.hui_yi.bean;

import java.util.ArrayList;

/**
 * Created by LiFen on 2018/2/18.
 * 管理员管理签到数据
 */

public class AdmQianDaoBean {

    public ArrayList<IsOk> isok;
    public ArrayList<NotOk> notok;

    public static class IsOk{
        public String uid;
        public String uname;
        public String uphoto;
        public String isfingerprint;
        public String utime;
        public String rssi;
    }

    public static class NotOk{
        public String uid;
        public String uname;
        public String uphoto;
        public String uphone;
    }
}
