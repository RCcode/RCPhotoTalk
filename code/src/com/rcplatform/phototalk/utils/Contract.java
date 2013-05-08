package com.rcplatform.phototalk.utils;

import android.app.Activity;
import android.os.Environment;
import android.util.DisplayMetrics;

public class Contract {

	public static int SCREEN_WIDTH;
	public static int SCREEN_HEIGHT;
	public static int HEAD_IMAGE_WIDTH;
	public static String DATABASE_PATH;

	public static final String IMAGE_FORMAT = ".jpg";
	public static final String AUDIO_FORMAT = ".amr";

	public static void init(Activity context) {
		DisplayMetrics dm = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(dm);
	
		SCREEN_HEIGHT = dm.heightPixels;
		SCREEN_WIDTH = dm.widthPixels;
		HEAD_IMAGE_WIDTH = SCREEN_WIDTH / 4;
		DATABASE_PATH = context.getFilesDir().getAbsolutePath();
		PhotoInformationCache.FILE_PATH = Environment.getExternalStorageDirectory().getPath() + "/" + "rcplatform/phototalk";
	}

	public static class Action {
		
		/**
		 * 显示推荐好友详情
		 */
		public static final String ACTION_RECOMMEND_DETAIL = "com.rcplatform.phototalk.action.FRIEND_RECOMMEND";
		/**
		 * 显示好友详情
		 */
		public static final String ACTION_FRIEND_DETAIL = "com.rcplatform.phototalk.action.FRIEND";
		/**
		 * 上传短信邀请的好友
		 */
		public static final String ACTION_UPLOAD_INTITE_CONTACT = "com.rcplatform.phototalk.action.INVITE_CONTACT";
		/**
		 * 上传第三方邀请的好友
		 */
		public static final String ACTION_UPLOAD_INTITE_THIRDPART = "com.rcplatform.phototalk.action.INVITE_THIRDPART";
		/**
		 * 信息状态更改：删除信息
		 */
		public static final String ACTION_INFORMATION_DELETE = "com.rcplatform.phototalk.action.INFORMATION_DELETE";
		/**
		 * 信息状态更改：读取状态改变
		 */
		public static final String ACTION_INFORMATION_STATE_CHANGE = "com.rcplatform.phototalk.action.INFORMATION_STATE_CHANGE";
		/**
		 * 信息状态更改：信息流传完成
		 */
		public static final String ACTION_INFORMATION_OVER="com.rcplatform.phototalk.action.INFORMATION_OVER";
		/**
		 * 登出
		 */
		public static final String ACTION_LOGOUT = "com.rcplatform.phototalk.action.LOGOUT";
	}

	public static class Provider {
		public static final String PROVIDER_AUTHORITY = "com.rcplatform.phototalk.provider";
		public static final String PROVIDER_USERS_PATH = "user";
		public static final String PROVIDER_LOGIN_USER_PATH = "user/#";
	}

	public static class PhotoInformationCache {
		public static String FILE_PATH;
		public static final String UNZIP_SUFFIX = "_unzip";
	}

	public static final String PREFS_FILE_USER_INFO = "com.menue.login.info.prefs";
	public static final long UPDATE_CHECK_WAITING_TIME = 60 * 1000;

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

	public static final String APP_ID = "1";

	public static final String PLATFORM = "android";

	public static final String META_DATA = "jpg";

	public static final String KEY_SIGNATURE = "signature";

	public static final String KEY_NICK = "nick";

	public static final String KEY_HEADURL = "headUrl";

	public static final String KEY_SEX = "sex";

	public static final String KEY_RECEIVESET = "receiveSet";

	public static final String KEY_TRENDSET = "trendset";

	public static final String KEY_RCID = "rcId";

	public static final String KEY_PHONE = "phone";

	public static final String KEY_AGE = "age";

	public static final String KEY_CREATETIME = "createTime";

	public static final String KEY_BIRTHDAY = "birthday";

	public static final String KEY_DEVICE_ID = "deviceid";

	public static final String KEY_MAX_RECORD_ID = "max_record_id";

	public static final String KEY_BACKGROUND = "background";

}
