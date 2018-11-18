package xyz.hui_yi.facesdk.api;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import xyz.hui_yi.face.utils.FaceGatherAddFace;
import xyz.hui_yi.facesdk.conn.Constant;
import xyz.hui_yi.facesdk.entity.FaceAttrs;
import xyz.hui_yi.facesdk.entity.MatchSearch;
import xyz.hui_yi.facesdk.entity.MatchVerify;
import xyz.hui_yi.facesdk.entity.PeopleAdd;
import xyz.hui_yi.facesdk.entity.PeopleCreate;
import xyz.hui_yi.facesdk.entity.PeopleGet;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Created by LiFen on 2017/1/15.
 * <p>
 * eyekey人脸识别API
 */
public class CheckAPI implements Constant {

  private static final String TAG = "CheckAPI";

  private static final String EYEKEY_APP_ID = "eyekey_appid";
  private static final String EYEKEY_APP_KEY = "eyekey_appkey";
  private static final Retrofit sRetrofit = new Retrofit.Builder()
          .baseUrl(API_SERVER)
          .addConverterFactory(GsonConverterFactory.create())
          .build();
  public static final EyekeyService sEyekeyManagerService = sRetrofit.create(EyekeyService.class);
  private static String sAppId = "";
  private static String sAppKey = "";
  private static ArrayList<Call> sCalls = new ArrayList<>();

  public static void init(Context context) {
    ApplicationInfo appInfo = null;
    try {
      appInfo = context.getPackageManager()
              .getApplicationInfo(context.getPackageName(),
                      PackageManager.GET_META_DATA);
      Bundle bundle = appInfo.metaData;
      if (bundle != null) {
        sAppId = bundle.getString(EYEKEY_APP_ID);
        sAppKey = bundle.getString(EYEKEY_APP_KEY);
      }
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }
    Log.i(TAG, "appid:" + sAppId + " appkey:" + sAppKey);
  }

  /**
   * 检测给定图片(Image)中的所有人脸(Face)的位置和相应的面部属性
   *
   * @param dataImage 待检测图片数据信息,通过POST方法上传的二进制数据，原始图片大小需要小于3M
   * @param mode      (可选)检测模式 (默认) oneface
   *                  。在oneface模式中，检测器仅找出图片中最大的一张脸。如果图中有多张人脸大小相同，随机返回一张人脸信息。
   * @param tip       (可选)指定一个不包含^@,&=*'"等非法字符且不超过255字节的字符串作为tip
   */
  public static Call<FaceAttrs> checkingImageData(String dataImage, String mode, String tip) {
    Call<FaceAttrs> call = sEyekeyManagerService.checkingImageData(sAppId, sAppKey, dataImage, mode, tip);
    sCalls.add(call);
    return call;
  }

  /**
   * 给定一个Face和一个People，返回是否是同一个人的判断以及可信度
   *
   * @param faceId     待验证的face_id
   * @param peopleName 对应的People
   * @return
   */
  public static Call<MatchVerify> matchVerify(String faceId, String peopleName) {
    Call call = sEyekeyManagerService.matchVerify(sAppId, sAppKey, faceId, peopleName);
    sCalls.add(call);
    return call;
  }

  /**
   * 创建一个People
   *
   * @param peopleName (可选)People的Name信息，必须在App中全局唯一。Name不能包含^@,&=*
   *                   '"等非法字符，且长度不得超过255。Name也可以不指定，此时系统将产生一个随机的name
   * @param faceId     (可选)一组用逗号分隔的face_id, 表示将这些Face加入到该People中
   * @param tip        (可选)People相关的tip，不需要全局唯一，不能包含^@,&=*'"等非法字符，长度不能超过255
   * @param crowdName  (可选)一组用逗号分割的crowd id列表或者crowd
   *                   name列表。如果该参数被指定，该People被create之后就会被加入到这些组中
   * @return
   */
  public static Call<PeopleCreate> peopleCreate(String faceId, String peopleName, String tip, String crowdName) {
    Call<PeopleCreate> call = sEyekeyManagerService.peopleCreate(sAppId, sAppKey, faceId, peopleName, tip, crowdName);
    sCalls.add(call);
    return call;
  }

  /**
   * 将一组Face加入到一个facegather中
   *
   * @param faceGatherName 相应facegather的name或者id
   * @param faceId         一组用逗号分隔的face_id,表示将这些Face加入到相应facegather中
   * @return
   */
  public static Call<FaceGatherAddFace> faceGatherAddFace(String faceGatherName, String faceId) {
    Call<FaceGatherAddFace> call = sEyekeyManagerService.faceGatherAddFace(sAppId, sAppKey, faceGatherName, faceId);
    sCalls.add(call);
    return call;
  }

  /**
   * 给定一个Face和一个Facegather，在Facegather内搜索最相似的Face
   *
   * @param faceId         待搜索的Face的face_id
   * @param faceGatherName 指定搜索范围为此Facegather
   * @param count          (可选)表示一共获取不超过count 个 搜索结果。默认count=3，选择相似度最高的三个face返回
   * @return
   */
  public static Call<MatchSearch> matchSearch(String faceId, String faceGatherName, int count) {
    Call<MatchSearch> call = sEyekeyManagerService.matchSearch(sAppId, sAppKey, faceId, faceGatherName, count);
    sCalls.add(call);
    return call;
  }

  /**
   * ·将一组Face加入到一个People中。注意，一个Face只能被加入到一个People中。 ·一个People最多允许包含10000个Face
   *
   * @param faceId     一组用逗号分隔的face_id,表示将这些Face加入到相应People中。
   * @param peopleName 相应People的name或者id
   * @return
   */
  public static Call<PeopleAdd> peopleAdd(String faceId, String peopleName) {
    Call<PeopleAdd> call = sEyekeyManagerService.peopleAdd(sAppId, sAppKey, faceId, peopleName);
    sCalls.add(call);
    return call;
  }

  /**
   * 获取一个People的信息, 包括name, id, tip, 相关的face, 以及crowds等信息
   *
   * @param peopleName 相应People的name或者id
   * @return
   */
  public static Call<PeopleGet> peopleGet(String peopleName) {
    Call<PeopleGet> call = sEyekeyManagerService.peopleGet(sAppId, sAppKey, peopleName);
    sCalls.add(call);
    return call;
  }

  public static void cancelAllCall() {
    for (Call call : sCalls) {
      if (call != null) {
        call.cancel();
      }
    }
    sCalls.clear();
  }

  public interface EyekeyService {

    @FormUrlEncoded
    @POST(Constant.Check + "/checking")
    Call<FaceAttrs> checkingImageData(
            @Field("app_id") String appId,
            @Field("app_key") String appKey,
            @Field("img") String dataImage,
            @Field("mode") String mode,
            @Field("tip") String tip
    );

    @GET(Constant.Match + "/match_verify")
    Call<MatchVerify> matchVerify(
            @Query("app_id") String appId,
            @Query("app_key") String appKey,
            @Query("face_id") String faceId,
            @Query("people_name") String peopleName
    );

    @GET(Constant.People + "/people_create")
    Call<PeopleCreate> peopleCreate(
            @Query("app_id") String appId,
            @Query("app_key") String appKey,
            @Query("face_id") String faceId,
            @Query("people_name") String peopleName,
            @Query("tip") String tip,
            @Query("crowd_name") String crowdName
    );

    @GET(Constant.People + "/people_add")
    Call<PeopleAdd> peopleAdd(
            @Query("app_id") String appId,
            @Query("app_key") String appKey,
            @Query("face_id") String faceId,
            @Query("people_name") String peopleName
    );

    @GET(Constant.FaceGather + "/facegather_addface")
    Call<FaceGatherAddFace> faceGatherAddFace(
            @Query("app_id") String appId,
            @Query("app_key") String appKey,
            @Query("facegather_name") String faceGatherName,
            @Query("face_id") String faceId
    );

    @GET(Constant.People + "/people_get")
    Call<PeopleGet> peopleGet(
            @Query("app_id") String appId,
            @Query("app_key") String appKey,
            @Query("people_name") String peopleName
    );

    @GET(Constant.Match + "/match_search")
    Call<MatchSearch> matchSearch(
            @Query("app_id") String appId,
            @Query("app_key") String appKey,
            @Query("face_id") String faceId,
            @Query("facegather_name") String faceGatherName,
            @Query("count") int count
    );
  }
}