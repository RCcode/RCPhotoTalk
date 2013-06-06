package com.rcplatform.phototalk.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
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

public class Constants {

	public static int SCREEN_WIDTH;

	public static int SCREEN_HEIGHT;

	public static int HEAD_IMAGE_WIDTH;

	public static final String IMAGE_FORMAT = ".jpg";

	public static final String AUDIO_FORMAT = ".amr";

	public static final String TEMP_INFORMATION_ID = "temp_information";

	public static Map<AppInfo, UserInfo> userApps;

	public static List<AppInfo> installedApps = new ArrayList<AppInfo>();

	public static String VK_API_ID = "3567525";

	public static File USER_IMAGE_DIR;

	public static final int INFORMATION_PAGE_SIZE = 20;

	public static final int MAX_SELF_BINDPHONE_TIME = 5;

	public static final String COUNTRY = Locale.getDefault().getCountry();

	public static final String OS_NAME = "android";

	public static final String OS_VERSION = android.os.Build.MODEL + "," + android.os.Build.VERSION.SDK_INT + "," + android.os.Build.VERSION.RELEASE;

	private static final String DB_DIR = "db";

	private static final String USER_DIR_NAME = "user";

	public static final String INVITE_URL = "http://rctalk.me";

	public static final long BIND_PHONE_TIME_OUT = 5 * 1000 * 60;

	public static String DEVICE_ID;

	public static String LANGUAGE;

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
			if (COUNTRY.equals("HK"))
				language = Locale.TAIWAN.toString();
			else
				language = language + "_" + COUNTRY;
		}
		LANGUAGE = language;
		DEVICE_ID = ((WifiManager) context.getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getMacAddress();
		USER_IMAGE_DIR = context.getDir(USER_DIR_NAME, Context.MODE_PRIVATE);
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

	public static class Message {

		public static final String MESSAGE_CONTENT_KEY = "message_content";

		public static final String MESSAGE_ACTION_MSG = "1";

		public static final int MESSAGE_ACTION_MSG_INT = 1;

		public static final String MESSAGE_ACTION_FRIEND = "2";

		public static final int MESSAGE_ACTION_FRIEND_INT = 2;

		public static final String MESSAGE_ACTION_SEND_MESSAGE = "3";

		public static final int MESSAGE_ACTION_SEND_MESSAGE_INT = 3;

		public static final String MESSAGE_NEW_USER_MESSAGE = "10";

		public static final int MESSAGE_NEW_USER_MESSAGE_INT = 10;

		public static final String MESSAGE_APP_PUSH_MESSAGE = "11";

		public static final int MESSAGE_APP_PUSH_MESSAGE_INT = 11;
	}

	public static class GCM {

		public static final String GCM_URL = "http://192.168.0.86:8083/phototalk/user/pushOfflineMsg.do";

		public final static String GCM_TYPE_MSG = "1";

		public final static String GCM_TYPE_FRIEND = "2";
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

		public static final String ACTION_TIGASE_STATE_CHANGE = "com.rcplatform.phototalk.action.TIGASE_STATE_CHANGE";

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

	public static final String BIND_PHONE_NUMBER = "+61427293943";

	public static final String BIND_PHONE_NUMBER_BACKUP = "+18587369880";

	public static final String FEEDBACK_EMAIL = "rctalk.service@gmail.com";

	public static final String FLURRY_KEY = "93576P6KMW7VYQDQCR3J";

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

	public static final String KEY_APP_ID = "appId";
}
