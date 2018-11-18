package xyz.hui_yi.bean;

import java.util.ArrayList;

/**
 * Created by LiFen on 2018/2/13.
 * 添加参会人员信息列表 bean
 */

public class UsersInforBean {

    public ArrayList<UserInfo> result;

    public static class UserInfo{

        /*"result":[
        {
            "uid":"xxx",
                "uname":"xxxxx",
                "uphone":"xxxxx",
                "uphoto":"xxxxx"
        }]*/

        public String uid;
        public String uname;
        public String uphone;
        public String uphoto;
//        public String title;
//        public String pubDate;
//        public String image;
    }
}
