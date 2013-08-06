package com.rcplatform.phototalk.utils;

import twitter4j.auth.AccessToken;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.bean.UserInfo;

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
		public static final String MAX_FISH_TIME = "maxfishtime";
		public static final String HAS_ADD_SHORTCUT = "hasaddshortcut";

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

		public static synchronized int getMaxFishTime(Context context) {
			return getPreference(context, PREF_APP_INFO).getInt(MAX_FISH_TIME, Constants.MAX_FISH_DRIFT_TIME);
		}

		public static synchronized void setMaxFishTime(Context context, int maxTime) {
			getPreference(context, PREF_APP_INFO).edit().putInt(MAX_FISH_TIME, maxTime).commit();
		}

		public static synchronized boolean hasAddShortCutIcon(Context context) {
			return getPreference(context, PREF_APP_INFO).getBoolean(HAS_ADD_SHORTCUT, false);
		}

		public static synchronized void setAddedShortCutIcon(Context context) {
			getPreference(context, PREF_APP_INFO).edit().putBoolean(HAS_ADD_SHORTCUT, true).commit();
		}
	}

	public static class LoginState {

		private static final String PREF_NAME = "loginstate";
		private static final String PREF_KEY_LOGIN_USER = "loginuser";
		private static final String PREF_KEY_LAST_RCID = "lastrcid";

		public synchronized static void setLoginUser(Context context, UserInfo userInfo) {
			SharedPreferences sh = getPreference(context, PREF_NAME);
			sh.edit().putString(PREF_KEY_LOGIN_USER, userInfo.getRcId()).commit();
			User.saveUserInfo(context, userInfo.getRcId(), userInfo);
			setLastRcId(context, userInfo.getRcId());
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
			sh.edit().remove(PREF_KEY_LOGIN_USER).commit();
		}

		public static void setLastRcId(Context context, String rcId) {
			SharedPreferences sh = getPreference(context, PREF_NAME);
			sh.edit().putString(PREF_KEY_LAST_RCID, rcId).commit();
		}

		public static String getLastRcId(Context context) {
			SharedPreferences sh = getPreference(context, PREF_NAME);
			return sh.getString(PREF_KEY_LAST_RCID, null);
		}
	}

	public static class User {

		private static final String PREF_KEY_LAST_SMS_SEND_TIME = "smssendtime";
		private static final String PREF_KEY_LAST_BIND_NUMBER = "lastbindnumber";

		private static final String PREF_KEY_FACEBOOK_NAME = "facebookname";
		private static final String PREF_KEY_FACEBOOK_ASYNC_TIME = "facebookasynctime";
		private static final String PREF_KEY_VK_SYNC_TIME = "vksynctime";

		private static final String PREF_KEY_MAX_RECORDINFO_ID = "max_record_info_id";
		private static final String PREF_KEY_VK_ACCESSTOKEN = "vk_access_token";
		private static final String PREF_KEY_VK_USERID = "vk_userid";

		private static final String PREF_KEY_TRENDS_MAX_ID = "trendsmaxid";
		private static final String PREF_KEY_TRENDS_SHOWED_MAX_ID = "showedtrendsmaxid";
		private static final String PREF_KEY_TRENDS_MAX_URL = "trendsmaxurl";
		private static final String PREF_KEY_ATTENTION_BIND_PHONE = "hasattentiontobindphone";
		private static final String PREF_KEY_LOADED_FRIENDS = "loadedfriends";

		private static final String PREF_KEY_START_BIND_TIME = "startbindphonetime";

		private static final String PREF_KEY_SELF_BINDPHONE_TIME = "selfbindphonetime";

		private static final String PREF_KEY_HAS_RECOMMENDS = "hasrecommends";

		private static final String PREF_KEY_VK_NAME = "vkname";

		private static final String PREF_KEY_INTO_FRIENDS_LIST_TIME = "intofriendslisttime";

		private static final String PREF_KEY_LAST_THROW_TIME = "lastthrowtime";

		private static final String PREF_KEY_USED_DRIFT = "hasuseddrift";
		private static final String PREF_KEY_LAST_USED_VERSION = "lastusedversion";
		private static final String PREF_KEY_TODAY_FISH_TIME = "todayfishtime";
		private static final String PREF_KEY_LAST_FISH_TIME = "lastfishtime";
		private static final String PREF_KEY_HAS_ATTENTION_AUTO_BIND = "hasattentionautobind";

		private static final String PREF_KEY_IS_FIRSTTIME_CHOOSE_MY_COUNTRY = "isfirsttimechoosemycountry";

		private static final String PREF_KEY_AUTO_BIND = "autobind";

		public static class ThirdPart {

			private static final String PREF_KEY_TWITTER_TOKEN = "twitter_token";

			private static final String PREF_KEY_TWITTER_SECRET = "twitter_secret";

			private static final String PREF_KEY_TWITTER_ID = "twitter_id";

			private static final String PREF_KEY_TWITTER_SCREEN_NAME = "twitter_screenname";

			public static void saveTwitterAccessToken(Context context, String pref, AccessToken token) {
				SharedPreferences sh = getPreference(context, pref);
				sh.edit().putString(PREF_KEY_TWITTER_TOKEN, token.getToken()).putString(PREF_KEY_TWITTER_SECRET, token.getTokenSecret())
						.putLong(PREF_KEY_TWITTER_ID, token.getUserId()).putString(PREF_KEY_TWITTER_SCREEN_NAME, token.getScreenName()).commit();
			}

			public static void clearTwitterAccount(Context context, String pref) {
				SharedPreferences sh = getPreference(context, pref);
				sh.edit().remove(PREF_KEY_TWITTER_TOKEN).remove(PREF_KEY_TWITTER_SECRET).remove(PREF_KEY_TWITTER_ID).remove(PREF_KEY_TWITTER_SCREEN_NAME)
						.commit();
			}

			public static AccessToken getTwitterAccessToken(Context context, String pref) {
				SharedPreferences sh = getPreference(context, pref);
				String token = sh.getString(PREF_KEY_TWITTER_TOKEN, null);
				String secret = sh.getString(PREF_KEY_TWITTER_SECRET, null);
				Long userId = sh.getLong(PREF_KEY_TWITTER_ID, -1l);
				if (token == null || secret == null) {
					return null;
				} else {
					AccessToken accessToken = new AccessToken(token, secret, userId);
					return accessToken;
				}

			}

			public static String getTwitterName(Context context, String pref) {
				SharedPreferences sh = getPreference(context, pref);
				return sh.getString(PREF_KEY_TWITTER_SCREEN_NAME, null);
			}

			public static void setFacebookUserName(Context context, String pref, String userName) {
				SharedPreferences sp = getPreference(context, pref);
				sp.edit().putString(PREF_KEY_FACEBOOK_NAME, userName).commit();
			}

			public static String getFacebookUserName(Context context, String pref) {
				return getPreference(context, pref).getString(PREF_KEY_FACEBOOK_NAME, null);
			}

			public static void refreshFacebookAsyncTime(Context context, String pref) {
				SharedPreferences sh = getPreference(context, pref);
				sh.edit().putLong(PREF_KEY_FACEBOOK_ASYNC_TIME, System.currentTimeMillis()).commit();
			}

			public static long getFacebookLastAsyncTime(Context context, String pref) {
				return getPreference(context, pref).getLong(PREF_KEY_FACEBOOK_ASYNC_TIME, 0);
			}

			public static void clearFacebookAccount(Context context, String pref) {
				SharedPreferences sh = getPreference(context, pref);
				sh.edit().remove(PREF_KEY_FACEBOOK_ASYNC_TIME).remove(PREF_KEY_FACEBOOK_NAME).commit();
			}

			public static void saveVKAccount(Context context, String pref, String accessToken, long userId) {
				SharedPreferences prefs = getPreference(context, pref);
				Editor editor = prefs.edit();
				editor.putString(PREF_KEY_VK_ACCESSTOKEN, accessToken);
				editor.putLong(PREF_KEY_VK_USERID, userId);
				editor.commit();
			}

			/**
			 * index=0-token，String index=1-userId，long
			 * 
			 * @param context
			 * @param pref
			 * @return
			 */
			public static Object[] getVKAccount(Context context, String pref) {
				SharedPreferences prefs = getPreference(context, pref);
				String access_token = prefs.getString(PREF_KEY_VK_ACCESSTOKEN, null);
				long user_id = prefs.getLong(PREF_KEY_VK_USERID, 0);
				if (access_token == null || user_id == 0)
					return null;
				Object[] result = new Object[2];
				result[0] = access_token;
				result[1] = user_id;
				return result;
			}

			public static void clearVKAccount(Context context, String pref) {
				SharedPreferences prefs = getPreference(context, pref);
				prefs.edit().remove(PREF_KEY_VK_SYNC_TIME).remove(PREF_KEY_VK_USERID).remove(PREF_KEY_VK_SYNC_TIME).remove(PREF_KEY_VK_NAME).commit();
			}

			public static void setVKName(Context context, String pref, String vkName) {
				getPreference(context, pref).edit().putString(PREF_KEY_VK_NAME, vkName).commit();
			}

			public static String getVkName(Context context, String pref) {
				return getPreference(context, pref).getString(PREF_KEY_VK_NAME, null);
			}

			public static void refreshVKSyncTime(Context context, String pref) {
				SharedPreferences prefs = getPreference(context, pref);
				prefs.edit().putLong(PREF_KEY_VK_SYNC_TIME, System.currentTimeMillis()).commit();
			}

			public static long getVKSyncTime(Context context, String pref) {
				return getPreference(context, pref).getLong(PREF_KEY_VK_SYNC_TIME, 0);
			}
		}

		public static class MobilePhoneBind {

			public static void setAttentionToBindPhone(Context context, String pref) {
				getPreference(context, pref).edit().putBoolean(PREF_KEY_ATTENTION_BIND_PHONE, true).commit();
			}

			public static boolean hasAttentionToBindPhone(Context context, String pref) {
				return getPreference(context, pref).getBoolean(PREF_KEY_ATTENTION_BIND_PHONE, false);
			}

			public static synchronized void saveBindedPhoneNumber(Context context, String phoneNumber, String pref) {
				SharedPreferences sharedPreferences = getPreference(context, pref);
				sharedPreferences.edit().putString(Constants.KEY_PHONE, phoneNumber).commit();
			}

			public static boolean willTryToBindPhone(Context context, String pref) {
				SharedPreferences sh = getPreference(context, pref);
				String lastNumber = sh.getString(PREF_KEY_LAST_BIND_NUMBER, null);
				return lastNumber == null || (lastNumber != null && !lastNumber.equals(Constants.BIND_PHONE_NUMBER_BACKUP));
			}

			public static void setLastBindNumber(Context context, String pref, String number) {
				SharedPreferences sh = getPreference(context, pref);
				sh.edit().putString(PREF_KEY_LAST_BIND_NUMBER, number).commit();
			}

			public static String getLastBindNumber(Context context, String pref) {
				SharedPreferences sh = getPreference(context, pref);
				return sh.getString(PREF_KEY_LAST_BIND_NUMBER, null);
			}

			public static long getLastBindPhoneTryTime(Context context, String pref) {
				SharedPreferences sh = getPreference(context, pref);
				return sh.getLong(PREF_KEY_LAST_SMS_SEND_TIME, 0);
			}

			public static void setLastBindPhoneTime(Context context, long sendTime, String pref) {
				SharedPreferences sh = getPreference(context, pref);
				sh.edit().putLong(PREF_KEY_LAST_SMS_SEND_TIME, sendTime).commit();
			}

			public static void setFirstBindPhoneTime(Context context, String pref, long time) {
				SharedPreferences sh = getPreference(context, pref);
				if (sh.getLong(PREF_KEY_START_BIND_TIME, 0) > 0) {
					return;
				} else {
					sh.edit().putLong(PREF_KEY_START_BIND_TIME, time).commit();
				}
			}

			public static boolean isUserBindPhoneTimeOut(Context context, String pref) {
				long startBindTime = getPreference(context, pref).getLong(PREF_KEY_START_BIND_TIME, 0);
				if (startBindTime == 0) {
					setFirstBindPhoneTime(context, pref, System.currentTimeMillis());
				}
				return startBindTime > 0 && (System.currentTimeMillis() - startBindTime) > Constants.BIND_PHONE_TIME_OUT;
			}
		}

		public static void setUserTrendSet(Context context, String pref, int set) {
			SharedPreferences sh = getPreference(context, pref);
			sh.edit().putInt(Constants.KEY_TRENDSET, set).commit();
		}

		public static UserInfo getUserInfo(Context context, String pref) {

			SharedPreferences sp = getPreference(context, pref);
			String rcId = sp.getString(Constants.KEY_RCID, null);
			if (rcId == null)
				return null;
			UserInfo userInfo = new UserInfo();
			userInfo.setRcId(rcId);
			userInfo.setEmail(sp.getString(Constants.KEY_EMAIL, null));
			userInfo.setToken(sp.getString(Constants.KEY_USER_TOKEN, null));
			userInfo.setNickName(sp.getString(Constants.KEY_NICK, null));
			userInfo.setHeadUrl(sp.getString(Constants.KEY_HEADURL, null));
			userInfo.setGender(sp.getInt(Constants.KEY_SEX, 0));
			userInfo.setAllowsend(sp.getInt(Constants.KEY_RECEIVESET, UserInfo.RECEIVE_ALL));
			userInfo.setShareNews(sp.getInt(Constants.KEY_TRENDSET, UserInfo.TRENDS_SHOW));
			userInfo.setBirthday(sp.getString(Constants.KEY_BIRTHDAY, null));
			userInfo.setDeviceId(sp.getString(Constants.KEY_DEVICE_ID, null));
			userInfo.setCellPhone(sp.getString(Constants.KEY_PHONE, null));
			userInfo.setBackground(sp.getString(Constants.KEY_BACKGROUND, null));
			userInfo.setTigaseId(sp.getString(Constants.KEY_TIGASE_ID, null));
			userInfo.setTigasePwd((sp.getString(Constants.KEY_TIGASE_PASSWORD, null)));
			userInfo.setCountry(sp.getString(Constants.KEY_COUNTRY, null));
			return userInfo;
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
		public static synchronized void saveUserInfo(Context context, String pref, UserInfo userInfo) {
			SharedPreferences sharedPreferences = getPreference(context, pref);
			sharedPreferences.edit().putString(Constants.KEY_EMAIL, userInfo.getEmail()).putString(Constants.KEY_USER_TOKEN, userInfo.getToken())
					.putString(Constants.KEY_NICK, userInfo.getNickName()).putString(Constants.KEY_HEADURL, userInfo.getHeadUrl())
					.putInt(Constants.KEY_SEX, userInfo.getGender()).putInt(Constants.KEY_RECEIVESET, userInfo.getAllowsend())
					.putInt(Constants.KEY_TRENDSET, userInfo.getShareNews()).putString(Constants.KEY_RCID, userInfo.getRcId())
					.putString(Constants.KEY_PHONE, userInfo.getCellPhone()).putString(Constants.KEY_TIGASE_ID, userInfo.getTigaseId())
					.putString(Constants.KEY_COUNTRY, userInfo.getCountry()).putString(Constants.KEY_TIGASE_PASSWORD, userInfo.getTigasePwd())
					.putString(Constants.KEY_BIRTHDAY, userInfo.getBirthday()).putString(Constants.KEY_DEVICE_ID, userInfo.getDeviceId())
					.putString(Constants.KEY_BACKGROUND, userInfo.getBackground()).commit();
		}

		public static void setUserReceiveSet(Context context, String pref, int set) {
			SharedPreferences sh = getPreference(context, pref);
			sh.edit().putInt(Constants.KEY_RECEIVESET, set).commit();
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
			sh.edit().putString(Constants.KEY_BACKGROUND, bgUrl).commit();
		}

		public static String getBackground(Context context, String pref) {
			SharedPreferences sh = getPreference(context, pref);
			return sh.getString(Constants.KEY_BACKGROUND, null);
		}

		public static void saveShowedMaxTrendsId(Context context, String pref, int id) {
			SharedPreferences sh = getPreference(context, pref);
			sh.edit().putInt(PREF_KEY_TRENDS_SHOWED_MAX_ID, id).commit();
		}

		public static int getShowedMaxTrendsId(Context context, String pref) {
			return getPreference(context, pref).getInt(PREF_KEY_TRENDS_SHOWED_MAX_ID, 0);
		}

		public static void saveMaxTrendsId(Context context, String pref, int id) {
			SharedPreferences sh = getPreference(context, pref);
			sh.edit().putInt(PREF_KEY_TRENDS_MAX_ID, id).commit();
		}

		public static int getMaxTrendsId(Context context, String pref) {
			return getPreference(context, pref).getInt(PREF_KEY_TRENDS_MAX_ID, 0);
		}

		public static void saveMaxTrendUrl(Context context, String pref, String url) {
			getPreference(context, pref).edit().putString(PREF_KEY_TRENDS_MAX_URL, url).commit();
		}

		public static String getMaxTrendUrl(Context context, String pref) {
			return getPreference(context, pref).getString(PREF_KEY_TRENDS_MAX_URL, null);
		}

		public static synchronized void setLoadedFriends(Context context, String pref) {
			getPreference(context, pref).edit().putBoolean(PREF_KEY_LOADED_FRIENDS, true).commit();
		}

		public static boolean hasLoadedFriends(Context context, String pref) {
			return getPreference(context, pref).getBoolean(PREF_KEY_LOADED_FRIENDS, false);
		}

		public static void addSelfBindPhoneTime(Context context, String pref) {
			SharedPreferences sh = getPreference(context, pref);
			int currentTime = sh.getInt(PREF_KEY_SELF_BINDPHONE_TIME, 0);
			sh.edit().putInt(PREF_KEY_SELF_BINDPHONE_TIME, (currentTime + 1)).commit();
		}

		public static int getSelfBindPhoneTimeLeave(Context context, String pref) {
			return Constants.MAX_SELF_BINDPHONE_TIME - getPreference(context, pref).getInt(PREF_KEY_SELF_BINDPHONE_TIME, 0);
		}

		public static boolean hasNewRecommends(Context context, String pref) {
			return getPreference(context, pref).getBoolean(PREF_KEY_HAS_RECOMMENDS, false);
		}

		public static synchronized void setNewRecommends(Context context, String pref, boolean hasNew) {
			getPreference(context, pref).edit().putBoolean(PREF_KEY_HAS_RECOMMENDS, hasNew).commit();
		}

		public static long getIntoFriendsListTime(Context context, String pref) {
			return getPreference(context, pref).getLong(PREF_KEY_INTO_FRIENDS_LIST_TIME, 0);
		}

		public static synchronized void setIntoFriendsListTime(Context context, String pref, long time) {
			getPreference(context, pref).edit().putLong(PREF_KEY_INTO_FRIENDS_LIST_TIME, time).commit();
		}

		public static synchronized boolean isThrowToday(Context context, String pref) {
			long time = getPreference(context, pref).getLong(PREF_KEY_LAST_THROW_TIME, 0);
			if (time == 0)
				return false;
			long currentTime = System.currentTimeMillis();
			if ((currentTime - time) < Constants.TimeMillins.A_DAY) {
				if (Utils.isSameDay(time, currentTime)) {
					return true;
				}
			}
			return false;
		}

		public static synchronized void setThrowToday(Context context, String pref) {
			long time = System.currentTimeMillis();
			getPreference(context, pref).edit().putLong(PREF_KEY_LAST_THROW_TIME, time).commit();
		}

		public static synchronized int getFishLeaveTime(Context context, String pref) {
			return AppInfo.getMaxFishTime(context) - getTodayFishTime(context, pref);
		}

		/**
		 * 获取漂流瓶功能是否用过
		 * 
		 * @param context
		 * @param pref
		 * @return
		 */
		public static boolean hasUsedDrift(Context context, String pref) {
			return getPreference(context, pref).getBoolean(PREF_KEY_USED_DRIFT, false);
		}

		/**
		 * 设置漂流瓶功能被使用
		 * 
		 * @param context
		 * @param pref
		 */
		public static void setDriftUsed(Context context, String pref) {
			getPreference(context, pref).edit().putBoolean(PREF_KEY_USED_DRIFT, true).commit();
		}

		public static String getLastUsedVersion(Context context, String pref) {
			return getPreference(context, pref).getString(PREF_KEY_LAST_USED_VERSION, null);
		}

		/**
		 * 获取当前版本是否使用过
		 * 
		 * @param context
		 * @param pref
		 * @return
		 */
		public static boolean hasCurrentVersionUsed(Context context, String pref) {
			return context.getString(R.string.version).equals(getLastUsedVersion(context, pref));
		}

		/**
		 * 设置当前版本已经被使用
		 * 
		 * @param context
		 * @param pref
		 */
		public static void setCurrentVersionUsed(Context context, String pref) {
			getPreference(context, pref).edit().putString(PREF_KEY_LAST_USED_VERSION, context.getString(R.string.version)).commit();
		}

		public synchronized static void setTodayFishTime(Context context, String pref, int time) {
			getPreference(context, pref).edit().putInt(PREF_KEY_TODAY_FISH_TIME, time).commit();
		}

		public static synchronized int getTodayFishTime(Context context, String pref) {
			return getPreference(context, pref).getInt(PREF_KEY_TODAY_FISH_TIME, 0);
		}

		public static synchronized void addTodayFishTime(Context context, String pref) {
			setTodayFishTime(context, pref, getTodayFishTime(context, pref) + 1);
		}

		public static synchronized void setLastFishTime(Context context, String pref) {
			getPreference(context, pref).edit().putLong(PREF_KEY_LAST_FISH_TIME, System.currentTimeMillis()).commit();
		}

		public static synchronized long getLastFishTime(Context context, String pref) {
			return getPreference(context, pref).getLong(PREF_KEY_LAST_FISH_TIME, 0);
		}

		public static synchronized void setAutoBindAttentioned(Context context, String pref) {
			getPreference(context, pref).edit().putBoolean(PREF_KEY_HAS_ATTENTION_AUTO_BIND, true).commit();
		}

		public static synchronized boolean hasAttentionAutoBind(Context context, String pref) {
			return getPreference(context, pref).getBoolean(PREF_KEY_HAS_ATTENTION_AUTO_BIND, false);
		}

		public static synchronized void setAutoBind(Context context, String pref) {
			getPreference(context, pref).edit().putBoolean(PREF_KEY_AUTO_BIND, true).commit();
		}

		public static synchronized boolean isAutoBind(Context context, String pref) {
			return getPreference(context, pref).getBoolean(PREF_KEY_AUTO_BIND, false);
		}

		public static synchronized boolean isFirstTimeChooseDriftRange(Context context, String pref) {
			return getPreference(context, pref).getBoolean(PREF_KEY_IS_FIRSTTIME_CHOOSE_MY_COUNTRY, true);
		}

		public static synchronized void setDriftRangeCheese(Context context, String pref) {
			getPreference(context, pref).edit().putBoolean(PREF_KEY_IS_FIRSTTIME_CHOOSE_MY_COUNTRY, false).commit();
		}
	}
}
