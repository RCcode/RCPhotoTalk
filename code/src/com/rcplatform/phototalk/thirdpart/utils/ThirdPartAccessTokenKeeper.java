package com.rcplatform.phototalk.thirdpart.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class ThirdPartAccessTokenKeeper {

	private static final String KEEPER_NAME = "accessToken_keeper";

	private static final String KEY_SINA_TOKEN = "sina_token";

	private static final String KEY_SINA_EXPIRES_IN = "sina_secret";

	private static final String KEY_TENCENT_TOKEN = "tencent_token";

	private static final String KEY_TENCENT_SECRET = "tencent_secret";

	private static final String KEY_TWITTER_TOKEN = "twitter_token";

	private static final String KEY_TWITTER_SECRET = "twitter_secret";

	private static final String KEY_TWITTER_ID = "twitter_id";

	private static final String KEY_TWITTER_SCREEN_NAME = "twitter_screenname";

	private static final String KEY_FACEBOOK_TOKEN = "facebooktoken";

	private static final String KEY_FACEBOOK_NAME = "facebook_name";

	// public static Oauth2AccessToken getSinaAccessToken(Context context) {
	//
	// SharedPreferences sh = context.getSharedPreferences(KEEPER_NAME, Context.MODE_PRIVATE);
	// String token = sh.getString(KEY_SINA_TOKEN, null);
	// long expires = sh.getLong(KEY_SINA_EXPIRES_IN, 0);
	// if (token == null || expires == 0) {
	// return null;
	// }
	// Oauth2AccessToken oauthToken = new Oauth2AccessToken();
	// oauthToken.setToken(token);
	// oauthToken.setExpiresTime(expires);
	// return oauthToken;
	// }

	// public static void saveSinaAccessToken(Context context, Oauth2AccessToken token) {
	// SharedPreferences sh = context.getSharedPreferences(KEEPER_NAME, Context.MODE_PRIVATE);
	// sh.edit().putString(KEY_SINA_TOKEN, token.getToken()).putLong(KEY_SINA_EXPIRES_IN,
	// token.getExpiresTime()).commit();
	// }
	//
	// public static void saveTencentAccessToken(Context context, OAuthV1 oauth) {
	// SharedPreferences sh = context.getSharedPreferences(KEEPER_NAME, Context.MODE_PRIVATE);
	// sh.edit().putString(KEY_TENCENT_TOKEN, oauth.getOauthToken()).putString(KEY_TENCENT_SECRET,
	// oauth.getOauthTokenSecret()).commit();
	// }

	// public static TencentAccessToken getTencentAccessToken(Context context) {
	// SharedPreferences sh = context.getSharedPreferences(KEEPER_NAME,
	// Context.MODE_PRIVATE);
	// String token = sh.getString(KEY_TENCENT_TOKEN, null);
	// String secret = sh.getString(KEY_TENCENT_SECRET, null);
	// if (token == null || secret == null)
	// return null;
	// return new TencentAccessToken(token, secret);
	// }

	// public static void saveTwitterAccessToken(Context context, AccessToken token) {
	// SharedPreferences sh = context.getSharedPreferences(KEEPER_NAME, Context.MODE_PRIVATE);
	// sh.edit().putString(KEY_TWITTER_TOKEN, token.getToken()).putString(KEY_TWITTER_SECRET, token.getTokenSecret())
	// .putLong(KEY_TWITTER_ID, token.getUserId()).putString(KEY_TWITTER_SCREEN_NAME, token.getScreenName()).commit();
	// }

	public static long getTwitterUserId(Context context) {
		SharedPreferences sh = context.getSharedPreferences(KEEPER_NAME, Context.MODE_PRIVATE);
		return sh.getLong(KEY_TWITTER_ID, -1l);
	}

	// public static AccessToken getTwitterAccessToken(Context context) {
	// SharedPreferences sh = context.getSharedPreferences(KEEPER_NAME,
	// Context.MODE_PRIVATE);
	// String token = sh.getString(KEY_TWITTER_TOKEN, null);
	// String secret = sh.getString(KEY_TWITTER_SECRET, null);
	// if (token == null || secret == null) {
	// return null;
	// }
	// AccessToken accessToken = new AccessToken(token, secret);
	// return accessToken;
	// }

	public static void saveFacebookAccessToken(Context context, String accessToken) {
		SharedPreferences sh = context.getSharedPreferences(KEEPER_NAME, Context.MODE_PRIVATE);
		sh.edit().putString(KEY_FACEBOOK_TOKEN, accessToken).commit();
	}
	public static void saveFacebookName(Context context,String name){
		SharedPreferences sh = context.getSharedPreferences(KEEPER_NAME, Context.MODE_PRIVATE);
		sh.edit().putString(KEY_FACEBOOK_NAME, name).commit();
	}
	public static String getFacebookAccessToken(Context context) {
		SharedPreferences sh = context.getSharedPreferences(KEEPER_NAME, Context.MODE_PRIVATE);
		return sh.getString(KEY_FACEBOOK_TOKEN, null);
	}

	public static String getFacebookLoginName(Context context) {
		SharedPreferences sh = context.getSharedPreferences(KEEPER_NAME, Context.MODE_PRIVATE);
		return sh.getString(KEY_FACEBOOK_NAME, null);
	}

	public static void removeSinaAccessToken(Context context) {
		SharedPreferences sh = context.getSharedPreferences(KEEPER_NAME, Context.MODE_PRIVATE);
		sh.edit().remove(KEY_SINA_EXPIRES_IN).remove(KEY_SINA_TOKEN).commit();
	}

	public static void removeTencentAccessToken(Context context) {
		SharedPreferences sh = context.getSharedPreferences(KEEPER_NAME, Context.MODE_PRIVATE);
		sh.edit().remove(KEY_TENCENT_TOKEN).remove(KEY_TENCENT_SECRET).commit();
	}

	public static void removeFacebookAccessToekn(Context context) {
		SharedPreferences sh = context.getSharedPreferences(KEEPER_NAME, Context.MODE_PRIVATE);
		sh.edit().remove(KEY_FACEBOOK_TOKEN).remove(KEY_FACEBOOK_NAME).commit();
	}

	public static void removeTwitterAccessToken(Context context) {
		SharedPreferences sh = context.getSharedPreferences(KEEPER_NAME, Context.MODE_PRIVATE);
		sh.edit().remove(KEY_TWITTER_ID).remove(KEY_TWITTER_SCREEN_NAME).remove(KEY_TWITTER_TOKEN).remove(KEY_TWITTER_SECRET).commit();
	}
}
