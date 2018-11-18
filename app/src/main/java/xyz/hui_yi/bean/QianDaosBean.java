package xyz.hui_yi.bean;

import java.util.ArrayList;

/**
 * Created by LiFen on 2018/2/16.
 * 签到列表信息beam
 */

public class QianDaosBean {
    public ArrayList<QianDaos> result;

    public static class QianDaos{

        public String mid;
        public String uphoto;
        public String mtitle;
        public String maddress;
        public String mlstartime;
        public String mlendtime;
        public String wlanmac;
        public String bluetoothmac;
        public String issign;
        public String sign_id;

    }
}
