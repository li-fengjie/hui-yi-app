package xyz.hui_yi.bean;

/**
 * Created by LiFen on 2018/2/1.
 * 登录用户的个人信息（我的界面）
 */

public class UserInfoBean {
    private String uid;
    private String bname;
    private String uphoto;
    private String uname;
    private String meetnum;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getBname() {
        return bname;
    }

    public void setBname(String bname) {
        this.bname = bname;
    }

    public String getUphoto() {
        return uphoto;
    }

    public void setUphoto(String uphoto) {
        this.uphoto = uphoto;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getMeetnum() {
        return meetnum;
    }

    public void setMeetnum(String meetnum) {
        this.meetnum = meetnum;
    }
}
