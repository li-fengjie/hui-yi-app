package xyz.hui_yi.activity;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.widget.RelativeLayout;
import android.widget.Toast;

import xyz.hui_yi.net.AdmLoginHttpThread;
import xyz.hui_yi.net.UserLoginHttpThread;
import xyz.hui_yi.utils.PrefUtils.PrefUtils;

import static xyz.hui_yi.constants.Constants.ADM_LOGIN_SUCCESS;
import static xyz.hui_yi.constants.Constants.ISUSER;
import static xyz.hui_yi.constants.Constants.KEY_ADM_ISCHECK;
import static xyz.hui_yi.constants.Constants.KEY_ADM_PWD;
import static xyz.hui_yi.constants.Constants.KEY_ADM_UID;
import static xyz.hui_yi.constants.Constants.KEY_IS_FIRST_ENTER;
import static xyz.hui_yi.constants.Constants.KEY_PCODE;
import static xyz.hui_yi.constants.Constants.KEY_USER_ISCHECK;
import static xyz.hui_yi.constants.Constants.KEY_USER_NAME;
import static xyz.hui_yi.constants.Constants.KEY_USER_PASSWORD;
import static xyz.hui_yi.constants.Constants.LOGIN_FAIL;
import static xyz.hui_yi.constants.Constants.NET_ERROR;
import static xyz.hui_yi.constants.Constants.SERVER_ERROR;
import static xyz.hui_yi.constants.Constants.UID;
import static xyz.hui_yi.constants.Constants.USER_LOGIN_SUCCESS;

/**
 * 闪屏页面
 *
 * @date 2015-10-17
 */
public class SplashActivity extends AppCompatActivity {

	private RelativeLayout rlRoot;

	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch (msg.what){
				case USER_LOGIN_SUCCESS:
					Uid = PrefUtils.getString(getApplication(),KEY_USER_NAME,"");
					Toast.makeText(getApplicationContext(), xyz.hui_yi.R.string.text_welcome_back,
							Toast.LENGTH_SHORT).show();
					Intent intent5 = new Intent(getApplicationContext(), MainActivity.class);
					intent5.putExtra(ISUSER, true);
					intent5.putExtra(UID, Uid);
					startActivity(intent5);
					finish();
					break;
				case ADM_LOGIN_SUCCESS:
					Toast.makeText(getApplicationContext(), getString(xyz.hui_yi.R.string.text_welcome_back), Toast.LENGTH_SHORT).show();
					Intent intent8 = new Intent(getApplicationContext(),MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
					intent8.putExtra(ISUSER,false);
					intent8.putExtra(UID,Uid);
					startActivity(intent8);
					finish();
					break;
				case LOGIN_FAIL :
					Toast.makeText(getApplicationContext(),"账号信息已过期，请重新登陆",Toast.LENGTH_SHORT).show();
					Intent intent1 = new Intent(getApplicationContext(), UserLoginActivity.class);
					startActivity(intent1);
//					SnackBarUtils.show(rlRoot, "账号信息已过期，请重新登陆");
					break;
				case SERVER_ERROR:
					Toast.makeText(getApplicationContext(),getString(xyz.hui_yi.R.string.text_server_error),Toast.LENGTH_SHORT).show();
					Intent intent2 = new Intent(getApplicationContext(), UserLoginActivity.class);
					startActivity(intent2);
					finish();
//					SnackBarUtils.show(rlRoot, R.string.text_server_error);
					break;
				case NET_ERROR:
					Toast.makeText(getApplicationContext(),getString(xyz.hui_yi.R.string.toast_network_error),Toast.LENGTH_SHORT).show();
                    Intent intent6 = new Intent(getApplicationContext(),MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent6.putExtra(ISUSER,false);
                    intent6.putExtra(UID,Uid);
                    startActivity(intent6);
                    finish();
                    break;
			}
		}
	};
	private String Uid;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(xyz.hui_yi.R.layout.activity_splash);
		rlRoot = (RelativeLayout) findViewById(xyz.hui_yi.R.id.rl_root);

		// 旋转动画
		RotateAnimation animRotate = new RotateAnimation(0, 0,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		animRotate.setDuration(1500);// 动画时间
		animRotate.setFillAfter(true);// 保持动画结束状态

		// 缩放动画
		ScaleAnimation animScale = new ScaleAnimation(0, 1, 0, 1,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		animScale.setDuration(1000);
		animScale.setFillAfter(true);// 保持动画结束状态

		// 渐变动画
		AlphaAnimation animAlpha = new AlphaAnimation(0, 1);
		animAlpha.setDuration(1100);// 动画时间
		animAlpha.setFillAfter(true);// 保持动画结束状态

		// 动画集合
		AnimationSet set = new AnimationSet(true);
		set.addAnimation(animRotate);
//		set.addAnimation(animScale);
//		set.addAnimation(animAlpha);

		// 启动动画
		rlRoot.startAnimation(set);

		set.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {

			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				// 动画结束,跳转页面
				// 如果是第一次进入, 跳新手引导
				// 否则跳主页面
				boolean isFirstEnter = PrefUtils.getBoolean(
						SplashActivity.this, KEY_IS_FIRST_ENTER, true);

				Intent intent;
				if (isFirstEnter) {
					// 新手引导
					intent = new Intent(getApplicationContext(),
							GuideActivity.class);
					startActivity(intent);
					finish();// 结束当前页面
					return;
				} else {
					// 主页面
					//TODO 是否登陆状态
					boolean user_login_now = PrefUtils.getBoolean(
							SplashActivity.this, KEY_USER_ISCHECK, false);
					boolean adm_login_now = PrefUtils.getBoolean(getApplication(), KEY_ADM_ISCHECK, false);
					if(user_login_now){
						Uid = PrefUtils.getString(getApplication(),KEY_USER_NAME,"");
						String pwd = PrefUtils.getString(getApplication(),KEY_USER_PASSWORD, "");
						boolean ischeck = PrefUtils.getBoolean(getApplication(),KEY_USER_ISCHECK, true);
						if (ischeck && !pwd.equals("")) {
							//TODO 自动联网登陆
							if (!Uid.equals("") && !pwd.equals("")) {
								String url = getResources().getString(xyz.hui_yi.R.string.URL);
								url = url + "/public/api/User/login";
								new UserLoginHttpThread(mHandler,url, Uid, pwd,"").start();
								return;
							}else {
                                Intent intent1 = new Intent(getApplicationContext(),
                                        UserLoginActivity.class);
                                startActivity(intent1);
                            }
						}else {
                            Intent intent2 = new Intent(getApplicationContext(),
                                    UserLoginActivity.class);
                            startActivity(intent2);
                        }
					}else if(adm_login_now){
						Uid = PrefUtils.getString(getApplication(), KEY_ADM_UID, "");
						String admPwd = PrefUtils.getString(getApplication(), KEY_ADM_PWD, "");
						String admCode = PrefUtils.getString(getApplication(),KEY_PCODE,"");
						String url = getResources().getString(xyz.hui_yi.R.string.URL);
						url = url + "/public/api/User/login";
						new AdmLoginHttpThread(mHandler,url,Uid,admPwd,admCode).start();
						return;
					}
					else {
						intent = new Intent(getApplicationContext(),
								UserLoginActivity.class);
						startActivity(intent);
					}
				}
				finish();// 结束当前页面
			}
		});
//		finish();// 结束当前页面
	}


}
