package com.rcplatform.phototalk.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.rcplatform.phototalk.bean.UserInfo;

/**
 * 标题、简要说明. <br>
 * 类详细说明.
 * <p>
 * Copyright: Menue,Inc Copyright (c) 2013-3-12 下午03:31:57
 * <p>
 * Team:Menue Beijing
 * <p>
 * 
 * @author jelly.xiong@menue.com.cn
 * @version 1.0.0
 */
public class PrefsUtils {

	private static SharedPreferences getPreference(Context context, String name) {
		return context.getSharedPreferences(name, Context.MODE_PRIVATE);
	}

	public static class AppInfo {
		public static final String PREF_APP_INFO = "appinfo";

		public static final String APP_KEY_CONTACT_UPLOADED = "contactuploaded";
		public static final String LAST_CONTACT_UPLOAD_TIME = "lastuploadtime";
		public static final String NEVER_ATTENTION_VERSION = "never_attention";
		public static final String LAST_UPDATE_TIME = "last_update_time";

		public static boolean hasUploadContacts(Context context) {
			SharedPreferences sh = getPreference(context, PREF_APP_INFO);
			return sh.getBoolean(APP_KEY_CONTACT_UPLOADED, false);
		}

		public static void setLastContactUploadTime(Context context) {
			SharedPreferences sh = getPreference(context, PREF_APP_INFO);
			sh.edit().putLong(LAST_CONTACT_UPLOAD_TIME, System.currentTimeMillis()).commit();
		}

		public static void setContactsUploaded(Context context) {
			SharedPreferences sh = getPreference(context, PREF_APP_INFO);
			sh.edit().putBoolean(APP_KEY_CONTACT_UPLOADED, true).putLong(LAST_CONTACT_UPLOAD_TIME, System.currentTimeMillis()).commit();
		}

		public static long getLastContactUploadTime(Context context) {
			SharedPreferences sh = getPreference(context, PREF_APP_INFO);
			return sh.getLong(LAST_CONTACT_UPLOAD_TIME, 0l);
		}

		public static void setNeverAttentionVersion(Context context, String version) {
			SharedPreferences sh = getPreference(context, PREF_APP_INFO);
			sh.edit().putString(NEVER_ATTENTION_VERSION, version).commit();
		}

		public static String getNeverAttentionVersion(Context context) {
			return getPreference(context, PREF_APP_INFO).getString(NEVER_ATTENTION_VERSION, null);
		}

		public static void setLastCheckUpdateTime(Context context, long time) {
			SharedPreferences sh = getPreference(context, PREF_APP_INFO);
			sh.edit().putLong(LAST_UPDATE_TIME, time).commit();
		}

		public static long getLastCheckUpdateTime(Context context) {
			return getPreference(context, PREF_APP_INFO).getLong(LAST_UPDATE_TIME, 0);
		}
	}

	public static class LoginState {
		private static final String PREF_NAME = "loginstate";
		private static final String PREF_KEY_LOGIN_USER = "loginuser";
		private static final String PREF_KEY_HAS_USED = "hasused";

		public static void setLoginUser(Context context, UserInfo userInfo) {
			SharedPreferences sh = getPreference(context, PREF_NAME);
			sh.edit().putString(PREF_KEY_LOGIN_USER, userInfo.getRcId()).commit();
			User.saveUserInfo(context, userInfo.getRcId(), userInfo);
		}

		public static UserInfo getLoginUser(Context context) {
			SharedPreferences sh = getPreference(context, PREF_NAME);
			String pref = sh.getString(PREF_KEY_LOGIN_USER, null);
			if (pref != null) {
				return User.getUserInfo(context, pref);
			}
			return null;
		}

		public static void clearLoginInfo(Context context) {
			SharedPreferences sh = getPreference(context, PREF_NAME);
			sh.edit().clear().commit();
		}

		public static void setAppUsed(Context context) {
			SharedPreferences sh = getPreference(context, PREF_NAME);
			sh.edit().putBoolean(PREF_KEY_HAS_USED, true).commit();
		}

		public static boolean hasAppUsed(Context context) {
			SharedPreferences sh = getPreference(context, PREF_NAME);
			return sh.getBoolean(PREF_KEY_HAS_USED, false);
		}
	}

	public static class User {

		private static final String PREF_KEY_LAST_SMS_SEND_TIME = "smssendtime";
		private static final String PREF_KEY_LAST_BIND_NUMBER = "lastbindnumber";
		private static final String PREF_KEY_FACEBOOK_NAME = "facebookname";
		private static final String PREF_KEY_FACEBOOK_ASYNC_TIME = "facebookasynctime";
		private static final String PREF_KEY_MAX_RECORDINFO_ID = "max_record_info_id";

		public static UserInfo getUserInfo(Context context, String pref) {

			SharedPreferences sp = getPreference(context, pref);
			String rcId = sp.getString(Contract.KEY_RCID, null);
			if (rcId == null)
				return null;
			UserInfo userInfo = new UserInfo();
			userInfo.setRcId(rcId);
			userInfo.setEmail(sp.getString(Contract.KEY_EMAIL, null));
			userInfo.setPassWord(sp.getString(Contract.KEY_PASSWORD, null));
			userInfo.setToken(sp.getString(Contract.KEY_USER_TOKEN, null));
			userInfo.setNickName(sp.getString(Contract.KEY_NICK, null));
			userInfo.setHeadUrl(sp.getString(Contract.KEY_HEADURL, null));
			userInfo.setGender(sp.getInt(Contract.KEY_SEX, 0));
			userInfo.setAllowsend(sp.getInt(Contract.KEY_RECEIVESET, UserInfo.RECEIVE_ALL));
			userInfo.setShareNews(sp.getInt(Contract.KEY_TRENDSET, UserInfo.TRENDS_SHOW));
			userInfo.setBirthday(sp.getString(Contract.KEY_BIRTHDAY, null));
			userInfo.setDeviceId(sp.getString(Contract.KEY_DEVICE_ID, null));
			userInfo.setCellPhone(sp.getString(Contract.KEY_PHONE, null));
			userInfo.setBackground(sp.getString(Contract.KEY_BACKGROUND, null));
			userInfo.setTigaseId(sp.getString(Contract.KEY_TIGASE_ID, null));
			userInfo.setTigasePwd((sp.getString(Contract.KEY_TIGASE_PASSWORD, null)));
			return userInfo;
		}

		public static void setFacebookUserName(Context context, String pref, String userName) {
			SharedPreferences sp = getPreference(context, pref);
			sp.edit().putString(PREF_KEY_FACEBOOK_NAME, userName).commit();
		}

		public static String getFacebookUserName(Context context, String pref) {
			return getPreference(context, pref).getString(PREF_KEY_FACEBOOK_NAME, null);
		}

		public static void refreshFacebookAsyncTime(Context context, String pref, long time) {
			SharedPreferences sh = getPreference(context, pref);
			sh.edit().putLong(PREF_KEY_FACEBOOK_ASYNC_TIME, time).commit();
		}

		public static long getFacebookLastAsyncTime(Context context, String pref) {
			return getPreference(context, pref).getLong(PREF_KEY_FACEBOOK_ASYNC_TIME, 0);
		}

		/**
		 * 保存当前用户登录信息。 Method description
		 * 
		 * @param context
		 * @param isLogged
		 *            是否登录。
		 * @param userInfo
		 *            用户登录信息
		 */
		public static void saveUserInfo(Context context, String pref, UserInfo userInfo) {
			SharedPreferences sharedPreferences = getPreference(context, pref);
			sharedPreferences.edit().putString(Contract.KEY_EMAIL, userInfo.getEmail()).putString(Contract.KEY_PASSWORD, userInfo.getPassWord())
					.putString(Contract.KEY_USER_TOKEN, userInfo.getToken()).putString(Contract.KEY_NICK, userInfo.getNickName())
					.putString(Contract.KEY_HEADURL, userInfo.getHeadUrl()).putInt(Contract.KEY_SEX, userInfo.getGender())
					.putInt(Contract.KEY_RECEIVESET, userInfo.getAllowsend()).putInt(Contract.KEY_TRENDSET, userInfo.getShareNews())
					.putString(Contract.KEY_RCID, userInfo.getRcId()).putString(Contract.KEY_PHONE, userInfo.getCellPhone())
					.putString(Contract.KEY_TIGASE_ID, userInfo.getTigaseId()).putString(Contract.KEY_TIGASE_PASSWORD, userInfo.getTigasePwd())
					.putString(Contract.KEY_BIRTHDAY, userInfo.getBirthday()).putString(Contract.KEY_DEVICE_ID, userInfo.getDeviceId())
					.putString(Contract.KEY_BACKGROUND, userInfo.getBackground()).commit();
		}

		/**
		 * 退出登录。 Method description
		 * 
		 * @param context
		 */
		public static void cleanUserInfoLogin(Context context) {
			SharedPreferences sharedPreferences = context.getSharedPreferences(Contract.PREFS_FILE_USER_INFO, Context.MODE_WORLD_READABLE);
			sharedPreferences.edit().clear().commit();
		}

		public static void setUserReceiveSet(Context context, String pref, int set) {
			SharedPreferences sh = getPreference(context, pref);
			sh.edit().putInt(Contract.KEY_RECEIVESET, set).commit();
		}

		public static void setUserTrendSet(Context context, String pref, int set) {
			SharedPreferences sh = getPreference(context, pref);
			sh.edit().putInt(Contract.KEY_TRENDSET, set).commit();
		}

		public static void saveBindedPhoneNumber(Context context, String phoneNumber, String pref) {
			SharedPreferences sharedPreferences = getPreference(context, pref);
			sharedPreferences.edit().putString(Contract.KEY_PHONE, phoneNumber).commit();
		}

		public static long getLastBindPhoneTryTime(Context context, String pref) {
			SharedPreferences sh = getPreference(context, pref);
			return sh.getLong(PREF_KEY_LAST_SMS_SEND_TIME, 0);
		}

		public static void setLastBindPhoneTime(Context context, long sendTime, String pref) {
			SharedPreferences sh = getPreference(context, pref);
			sh.edit().putLong(PREF_KEY_LAST_SMS_SEND_TIME, sendTime).commit();
		}

		public static boolean willTryToBindPhone(Context context, String pref) {
			SharedPreferences sh = getPreference(context, pref);
			String lastNumber = sh.getString(PREF_KEY_LAST_BIND_NUMBER, null);
			return lastNumber != null && !lastNumber.equals(Contract.BIND_PHONE_NUMBER_BACKUP);
		}

		public static void setLastBindNumber(Context context, String pref, String number) {
			SharedPreferences sh = getPreference(context, pref);
			sh.edit().putString(PREF_KEY_LAST_BIND_NUMBER, number).commit();
		}

		public static String getLastBindNumber(Context context, String pref) {
			SharedPreferences sh = getPreference(context, pref);
			return sh.getString(PREF_KEY_LAST_BIND_NUMBER, null);
		}

		public static void setUserMaxRecordInfoId(Context context, String pref, int maxRecordInfoId) {
			SharedPreferences sh = getPreference(context, pref);
			sh.edit().putInt(PREF_KEY_MAX_RECORDINFO_ID, maxRecordInfoId).commit();
		}

		public static int getUserMaxRecordInfoId(Context context, String pref) {
			SharedPreferences sh = getPreference(context, pref);
			return sh.getInt(PREF_KEY_MAX_RECORDINFO_ID, 0);
		}

		public static void setBackground(Context context, String pref, String bgUrl) {
			SharedPreferences sh = getPreference(context, pref);
			sh.edit().putString(Contract.KEY_BACKGROUND, bgUrl).commit();
		}

		public static String getBackground(Context context, String pref) {
			SharedPreferences sh = getPreference(context, pref);
			return sh.getString(Contract.KEY_BACKGROUND, null);
		}
	}
}
