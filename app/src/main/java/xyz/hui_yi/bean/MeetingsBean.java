package xyz.hui_yi.bean;

import java.util.ArrayList;

/**
 * Created by LiFen on 2018/2/10.
 * 会议列表信息bean
 */

public class MeetingsBean {
    public ArrayList<Meeting> result;

    public static class Meeting{

        public String mid;
        public String mimage;
        public String mtitle;
        public String maddress;
        public String mlstartime;
        public String mlendtime;
    }
}
