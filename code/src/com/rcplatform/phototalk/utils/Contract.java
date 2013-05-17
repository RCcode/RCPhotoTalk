package com.rcplatform.phototalk.utils;

import java.util.Map;

import android.app.Activity;
import android.os.Environment;
import android.util.DisplayMetrics;

import com.rcplatform.phototalk.bean.AppInfo;
import com.rcplatform.phototalk.bean.UserInfo;

public class Contract {

	public static int SCREEN_WIDTH;
	public static int SCREEN_HEIGHT;
	public static int HEAD_IMAGE_WIDTH;

	public static final String IMAGE_FORMAT = ".jpg";
	public static final String AUDIO_FORMAT = ".amr";

	public static final String TEMP_INFORMATION_ID = "temp_information";

	public static Map<AppInfo, UserInfo> userApps;
	
	public static boolean START_COMPLETE=false;

	public static void init(Activity context) {
		DisplayMetrics dm = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(dm);
		SCREEN_HEIGHT = dm.heightPixels;
		SCREEN_WIDTH = dm.widthPixels;
		HEAD_IMAGE_WIDTH = SCREEN_WIDTH / 4;
		initDatabase();
		PhotoInformationCache.FILE_PATH = context.getFilesDir() + "/" + "rcplatform/phototalk";
		userApps = Utils.getRCPlatformAppUsers(context);
	}

	private static void initDatabase() {
		Database.BASE_DATABASE_PATH = Environment.getExternalStorageDirectory().getPath() + "/db";
		Database.USERS_DATABASE_PATH = Database.BASE_DATABASE_PATH + "/users";
		Utils.createNewDir(Database.USERS_DATABASE_PATH);
		Database.REQUEST_DATABASE_PATH = Database.BASE_DATABASE_PATH;
		Database.GLOBAL_DATABASE_PATH = Database.BASE_DATABASE_PATH;
	}

	public static class Database {
		public static String BASE_DATABASE_PATH;
		public static String USERS_DATABASE_PATH;
		public static String REQUEST_DATABASE_PATH;
		public static String GLOBAL_DATABASE_PATH;
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
		public static final String ACTION_INFORMATION_OVER = "com.rcplatform.phototalk.action.INFORMATION_OVER";
		/**
		 * 登出
		 */
		public static final String ACTION_LOGOUT = "com.rcplatform.phototalk.action.LOGOUT";
		/**
		 * 重新登陆
		 */
		public static final String ACTION_RELOGIN = "com.rcplatform.phototalk.action.RELOGIN";
		public static final String ACTION_OTHER_DEVICE_LOGIN = "com.rcplatform.phototalk.action.OTHER_DEVICE_LOGIN";
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

	public static class FriendAddType {
		public static final int ADD_FRIEND_ACTIVE = 0;
		public static final int ADD_FRIEND_PASSIVE = 1;
	}

	public static final String PREFS_FILE_USER_INFO = "com.menue.login.info.prefs";
	public static final long UPDATE_CHECK_WAITING_TIME = 60 * 1000;

	public static final String BIND_PHONE_NUMBER = "+18146193618";
//	public static final String BIND_PHONE_NUMBER_BACKUP = "+18587369880";
	public static final String BIND_PHONE_NUMBER_BACKUP = "+8613718034941";
	// 头像图片本地缓存地址

	public static final String HEAD_CACHE_PATH = "head";

	/*
	 * ================== 登录信息================= 是否已登录.
	 */

	public static final String KEY_USER_TOKEN = "userToken";

	public static final String KEY_EMAIL = "email";

	/*
	 * 登录和注册页面。
	 */
	public static final String KEY_LOGIN_PAGE = "loginpage";

	public static final String APP_ID = "1";

	public static final String PLATFORM = "android";

	public static final String META_DATA = "jpg";

	public static final String KEY_NICK = "nick";

	public static final String KEY_HEADURL = "headUrl";

	public static final String KEY_SEX = "sex";

	public static final String KEY_RECEIVESET = "receiveSet";

	public static final String KEY_TRENDSET = "trendset";

	public static final String KEY_RCID = "rcId";

	public static final String KEY_PHONE = "phone";

	public static final String KEY_CREATETIME = "createTime";

	public static final String KEY_BIRTHDAY = "birthday";

	public static final String KEY_DEVICE_ID = "deviceid";

	public static final String KEY_MAX_RECORD_ID = "max_record_id";

	public static final String KEY_BACKGROUND = "background";

	public static final String KEY_TIGASE_ID = "tigaseid";

	public static final String KEY_TIGASE_PASSWORD = "tigasepassword";
}
