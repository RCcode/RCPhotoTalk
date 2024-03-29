package com.rcplatform.phototalk.utils;

import android.app.Activity;
import android.util.DisplayMetrics;

public class Contract {

	public static int SCREEN_WIDTH;
	public static int SCREEN_HEIGHT;
	public static int HEAD_IMAGE_WIDTH;
	public static String DATABASE_PATH;

	public static void init(Activity context) {
		DisplayMetrics dm = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(dm);
		SCREEN_HEIGHT = dm.heightPixels;
		SCREEN_WIDTH = dm.widthPixels;
		HEAD_IMAGE_WIDTH = SCREEN_WIDTH / 4;
		DATABASE_PATH = context.getFilesDir().getAbsolutePath();
	}

	public static class Action {
		public static final String ACTION_RECOMMEND_DETAIL = "com.rcplatform.phototalk.action.FRIEND_RECOMMEND";
		public static final String ACTION_FRIEND_DETAIL = "com.rcplatform.phototalk.action.FRIEND";
		public static final String ACTION_UPLOAD_INTITE_CONTACT="com.rcplatform.phototalk.action.INVITE_CONTACT";
		public static final String ACTION_UPLOAD_INTITE_THIRDPART="com.rcplatform.phototalk.action.INVITE_THIRDPART";
	}

	public static final String PROVIDER_AUTHORITY = "com.rcplatform.phototalk.provider";
	public static final String PROVIDER_USERS_PATH = "user";
	public static final String PROVIDER_LOGIN_USER_PATH = "user/#";

	public static final String PREFS_FILE_USER_INFO = "com.menue.login.info.prefs";

	public static final String HOME_RECORD_INFO = "com.menue.home.notice";

	public static final String BIND_PHONE_NUMBER = "+18146193618";
	public static final String BIND_PHONE_NUMBER_BACKUP = "+18587369880";
	// 头像图片本地缓存地址

	public static final String HEAD_CACHE_PATH = "head";

	/*
	 * ================== 登录信息================= 是否已登录.
	 */

	public static final String KEY_USER_TOKEN = "userToken";

	public static final String KEY_SUID = "suid";

	public static final String KEY_EMAIL = "email";

	public static final String KEY_PASSWORD = "password";

	public static final String KEY_USERNAME = "userName";

	/*
	 * 登录和注册页面。
	 */
	public static final String KEY_LOGIN_PAGE = "loginpage";

	/* APP_ID photochat 为1，videochat为2 */
	public static final String APP_ID = "1";

	public static final String PLATFORM = "android";

	public static final String META_DATA = "jpg";

	public static final String KEY_SIGNATURE = "signature";

	public static final String KEY_NICK = "nick";

	public static final String KEY_HEADURL = "headUrl";

	public static final String KEY_SEX = "sex";

	public static final String KEY_RECEIVESET = "receiveSet";

	public static final String KEY_RCID = "rcId";

	public static final String KEY_PHONE = "phone";

	public static final String KEY_AGE = "age";

	public static final String KEY_CREATETIME = "createTime";

	public static final String KEY_BIRTHDAY = "birthday";

	public static final String KEY_DEVICE_ID = "deviceid";

	/**
	 * 第三方类型
	 */
	public static final int THIRD_PART_TYPE_FACEBOOK = 1;
}
