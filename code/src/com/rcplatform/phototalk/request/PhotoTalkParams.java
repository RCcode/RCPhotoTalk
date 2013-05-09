package com.rcplatform.phototalk.request;

import java.util.Locale;

import android.content.Context;

import com.rcplatform.phototalk.MenueApplication;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.galhttprequest.GalHttpRequest;

public class PhotoTalkParams {

	public static int BASIC_PARAM_COUNT = 5;

	public static String PARAM_KEY_TOKEN = "token";
	public static String PARAM_KEY_LANGUAGE = "language";
	public static String PARAM_KEY_DEVICE_ID = "deviceId";
	public static String PARAM_KEY_USER_ID = "userId";
	public static String PARAM_KEY_APP_ID = "appId";

	public static String PARAM_VALUE_TOKEN_DEFAULT = "000000";
	public static String PARAM_VALUE_LANGUAGE = Locale.getDefault().getLanguage();
	public static String PARAM_VALUE_DEVICE_ID = android.os.Build.SERIAL;
	public static String PARAM_VALUE_APP_ID = "1";

	public static class ChangePassword {
		public static final String PARAM_KEY_CHECK_PASSWORD = "pwd";
		public static final String PARAM_KEY_NEW_PASSWORD = "newpwd";
	}
	public static class SendPhoto{
		public static final String PARAM_KEY_FLAG="time";
		public static final String PARAM_KEY_TIME_LIMIT="timeLimit";
		public static final String PARAM_KEY_USERS="userArray";
		public static final String PARAM_KEY_RECEIVER_ID = "userId";
	}
	public static class UserSetting {
		public static final String PARAM_KEY_TREND_SETTING = "tset";
		public static final String PARAM_KEY_RECEIVE_SETTING = "rset";
	}

	public static class SearchFriends {
		public static final String PARAM_KEY_KEYWORDS = "keyword";
	}

	public static class ClearInformation {
		public static final String PARAM_KEY_NOTICE_ID = "noticeId";
	}

	public static class InformationStateChange {
		public static final String PARAM_KEY_INFOS = "notices";
		public static final String PARAM_KEY_TIME = "time";
	}

	public static class UploadContacts {
		public static String PARAM_KEY_NAME = "friendName";
		public static String PARAM_KEY_PHONE_NUMBER = "friendPhone";
		public static String PARAM_KEY_CONTACT_LIST = "contactList";
	}

	public static class Registe {
		public static final String PARAM_KEY_EMAIL = "email";
		public static final String PARAM_KEY_PASSWORD = "pwd";
		public static final String PARAM_KEY_NICK = "nick";
		public static final String PARAM_KEY_COUNTRY = "country";
	}

	public static class PLATFORM_ACCOUNT_LOGIN {
		public static final String PARAM_KEY_HEAD_URL = "headUrl";
		public static final String PARAM_KEY_NICK = "nick";
		public static final String PARAM_KEY_IMAGE = "file";
	}

	public static void buildBasicParams(Context context, GalHttpRequest request) {
		UserInfo userInfo = ((MenueApplication) context.getApplicationContext()).getCurrentUser();
		request.setPostValueForKey(PARAM_KEY_USER_ID, userInfo.getSuid());
		request.setPostValueForKey(PARAM_KEY_TOKEN, userInfo.getToken());
		request.setPostValueForKey(PARAM_KEY_LANGUAGE, PARAM_VALUE_LANGUAGE);
		request.setPostValueForKey(PARAM_KEY_DEVICE_ID, PARAM_VALUE_DEVICE_ID);
		request.setPostValueForKey(PARAM_KEY_APP_ID, PARAM_VALUE_APP_ID);
	}

	public static void buildBasicParams(Context context, RCPlatformAsyncHttpClient client) {
		UserInfo userInfo = ((MenueApplication) context.getApplicationContext()).getCurrentUser();
		if (userInfo != null) {
			client.putRequestParam(PARAM_KEY_USER_ID, userInfo.getSuid());
			client.putRequestParam(PARAM_KEY_TOKEN, userInfo.getToken());
		} else {
			client.putRequestParam(PARAM_KEY_USER_ID, UserInfo.DEFAULT_USER_ID);
			client.putRequestParam(PARAM_KEY_TOKEN, UserInfo.DEFAULT_TOKEN);
		}
		client.putRequestParam(PARAM_KEY_LANGUAGE, PARAM_VALUE_LANGUAGE);
		client.putRequestParam(PARAM_KEY_DEVICE_ID, PARAM_VALUE_DEVICE_ID);
		client.putRequestParam(PARAM_KEY_APP_ID, PARAM_VALUE_APP_ID);
	}

	public static void buildBasicParams(Context context, Request request) {
		UserInfo userInfo = ((MenueApplication) context.getApplicationContext()).getCurrentUser();
		if (userInfo != null) {
			request.putParam(PARAM_KEY_USER_ID, userInfo.getSuid());
			request.putParam(PARAM_KEY_TOKEN, userInfo.getToken());
		} else {
			request.putParam(PARAM_KEY_USER_ID, UserInfo.DEFAULT_USER_ID);
			request.putParam(PARAM_KEY_TOKEN, UserInfo.DEFAULT_TOKEN);
		}
		request.putParam(PARAM_KEY_LANGUAGE, PARAM_VALUE_LANGUAGE);
		request.putParam(PARAM_KEY_DEVICE_ID, PARAM_VALUE_DEVICE_ID);
		request.putParam(PARAM_KEY_APP_ID, PARAM_VALUE_APP_ID);
	}

	public static class UpdateBindState {
		public static final String PARAM_KEY_BIND_TIME = "time";
		public static final String PARAM_KEY_BIND_NUMBER = "phone";
	}

	public static class Login {
		public static String PARAM_KEY_ACCOUNT = "account";
		public static String PARAM_KEY_PASSWORD = "pwd";
		public static String PARAM_KEY_SYSTEM = "system";
		public static String PARAM_KEY_TYPE = "type";
		public static String PARAM_KEY_MODEL = "model";
		public static String PARAM_KEY_BRAND = "brand";
	}

	public static class AddFriends {
		public static final String PARAM_KEY_USER_SUID = "seSuid";
		public static final String PARAM_KEY_FRIENDS = "friends";
		public static final String PARAM_KEY_FRIEND_SUID = "reSuid";
		public static final String PARAM_KEY_FRIEND_TYPE = "attrType";
	}

	public static class DelFriends {
		public static final String PARAM_KEY_FRIEND_ID = "atUserId";
	}

	public static class DelRecommend {
		public static final String PARAM_KEY_FRIEND_ID = "atUserId";
		public static final String PARAM_KEY_RECOMMEND_TYPE = "attrType";
	}

	public static class UpdateFriendRemark {
		public static final String PARAM_KEY_REMARK = "mark";
		public static final String PARAM_KEY_FRIEND_ID = "atUserId";
	}

	public static class RecordInfo {
		public static final String PARAM_MAX_RECORD_ID = "noticeId";
	}

	public static class FriendDetail {
		public static final String PARAM_KEY_FRIEND_ID = "atUserId";
	}

	public static class AddFriendFromInformation {
		public static final String PARAM_KEY_INFORMATION_ID = "noticeId";
		public static final String PARAM_KEY_INFORMATION_TYPE = "noType";
		public static final String PARAM_KEY_INFORMATION_STATE = "state";
		public static final String PARAM_KEY_FRIEND_SUID = "friendId";
	}
}
