package xyz.hui_yi.facesdk.conn;

public interface Constant {
  /**
   * 访问云平台服务接口的地址
   */
  String API_SERVER = "http://api.eyekey.com/";
  String Check = API_SERVER + "/face/Check";
  String Match = API_SERVER + "/face/Match";
  String People = API_SERVER + "/People";
  String FaceGather = API_SERVER + "/face/FaceGather";
  /**
   * 用户不存在
   */
  String RES_CODE_1025 = "1025";

  String RES_CODE_0000 = "0000";

  String RES_CODE_1031 = "1031";

}
