package xyz.hui_yi.constants;

/**
 * Created by LiFen on 2018/3/14.
 */

public interface Constants {
    //指纹
    int SCENE_VALUE = 0; // 场景值常量，后续使用该常量进行密钥生成或指纹认证
    String KEY_SOTER_SUPPORTED = "SOTER_SUPPORTED"; //是否支持soter 键
    String STR_CHALLENGE = "challenge"; // 挑战因子（此处设为了定值）

    String ISUSER = "isuser";//是否签到用户
    String UID = "uid"; //当前登陆的 uid

    //用户
    String KEY_USER_NAME = "uname";
    String KEY_USER_ISCHECK = "uischeck";
    String KEY_USER_PASSWORD = "upwd";
    String KEY_UPHOTO = "uphoto";
    String MID = "mid";

    //管理员
    String KEY_ADM_UID  = "admuid";
    String KEY_ADM_PWD = "admpwd";
    String KEY_PCODE = "pcode";
    String KEY_ADM_ISCHECK = "admischeck";
    String KEY_IS_FIRST_ENTER = "is_first_enter";

    //震动文件键
    String VIBRATE_KEY = "vibrate";

    String VOIUME_OFF_ON_KEY = "voiumeoffon";

    //是否为后置摄像头
    String KEY_IS_CAMERA_1 ="cameraid";

    int USER_LOGIN_SUCCESS = 90000;
    int LOGIN_FAIL = 80000;
    int ADM_LOGIN_SUCCESS = 70000;
    int SERVER_ERROR = 60000;
    int LOGIN_PCODE_FAIL = 50000;

    int NET_ERROR = 0000;

    int REG_PCODE_NOT_EXISTS = 40000;
    int REG_SUCCESS = 10000;
    int REG_UID_EXISTS = 20000;
    int REG_BNAME_NOT_EXISTS = 30000;

    int NO_SOTER_SUPPORT = 90001;

    int QIANDAO_SUCCESS = 200020;
    int QIANDAO_FAIL = 10026;
    int DATA_NUM = 55555;
    int PID_FAIL = 10032;
}
