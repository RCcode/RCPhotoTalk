package com.rcplatform.phototalk.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.util.DisplayMetrics;

import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.bean.AppInfo;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.db.DatabaseUtils;
import com.rcplatform.phototalk.request.PhotoTalkParams;

public class Constants {

	public static int SCREEN_WIDTH;

	public static int SCREEN_HEIGHT;

	public static int HEAD_IMAGE_WIDTH;

	public static final String IMAGE_FORMAT = ".jpg";

	public static final String AUDIO_FORMAT = ".amr";

	public final static String GCM_TYPE_MSG = "1";

	public final static String GCM_TYPE_FRIEND = "2";

	public static final String TEMP_INFORMATION_ID = "temp_information";

	public static Map<AppInfo, UserInfo> userApps;

	public static String VK_API_ID = "2904017";

	private static final String DB_DIR = "db";

	public static void initUI(Activity context) {
		DisplayMetrics dm = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(dm);
		SCREEN_HEIGHT = dm.heightPixels;
		SCREEN_WIDTH = dm.widthPixels;
		HEAD_IMAGE_WIDTH = SCREEN_WIDTH / 4;
		PhotoInformationCache.FILE_PATH = context.getFilesDir() + "/" + "rcplatform/phototalk";
		userApps = Utils.getRCPlatformAppUsers(context);
		String language = Locale.getDefault().getLanguage();
		if (language.equals(Locale.CHINESE.toString())) {
			language = language + "_" + Locale.getDefault().getCountry();
		}
		PhotoTalkParams.PARAM_VALUE_LANGUAGE = language;
		PhotoTalkParams.PARAM_VALUE_DEVICE_ID = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getMacAddress();
	}

	public static void initDatabase(Context context) {
		if (Database.DB_DIR == null) {
			Database.DB_DIR = context.getDir(DB_DIR, Context.MODE_PRIVATE);
			Database.BASE_DATABASE_PATH = Database.DB_DIR.getAbsolutePath();
			Database.USERS_DATABASE_PATH = Database.BASE_DATABASE_PATH + "/users";
			Utils.createNewDir(Database.USERS_DATABASE_PATH);
			Database.REQUEST_DATABASE_PATH = Database.BASE_DATABASE_PATH;
			Database.GLOBAL_DATABASE_PATH = Database.BASE_DATABASE_PATH;
		}
	}

	public static void initCountryDatabase(Context context) {
		File temp = new File(DatabaseUtils.getCountryCodeDatabasePath());
		if (!temp.exists()) {
			try {
				InputStream is = context.getResources().openRawResource(R.raw.country_code);
				FileOutputStream fos = new FileOutputStream(temp);
				byte[] buffer = new byte[1024];
				int count = 0;
				while ((count = is.read(buffer)) != -1) {
					fos.write(buffer, 0, count);
				}
				fos.close();
				is.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static class Database {
		public static File DB_DIR;
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
		/**
		 * gcm消息action
		 */
		public static final String ACTION_GCM_MESSAGE = "com.rcplatform.phototalk.action.GCM_MESSAGE";

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

	public static final String PREFS_FILE_USER_INFO = "com.phototalk.login.info.prefs";

	public static final long UPDATE_CHECK_WAITING_TIME = 60 * 1000;

	public static final String BIND_PHONE_NUMBER = "+18146193618";

	public static final String BIND_PHONE_NUMBER_BACKUP = "+18587369880";
	// public static final String BIND_PHONE_NUMBER_BACKUP = "+8613718034941";

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
