package com.rcplatform.phototalk.proxy;

import android.content.Context;

import com.rcplatform.phototalk.api.MenueApiUrl;
import com.rcplatform.phototalk.api.PhotoTalkParams;
import com.rcplatform.phototalk.api.RCPlatformAsyncHttpClient;
import com.rcplatform.phototalk.api.RCPlatformResponseHandler;
import com.rcplatform.phototalk.api.RCPlatformAsyncHttpClient.RequestAction;
import com.rcplatform.phototalk.bean.UserInfo;

public class UserSettingProxy {
	public static void updateUserSetting(Context context, RCPlatformResponseHandler responseHandler, UserInfo userInfo) {
		RCPlatformAsyncHttpClient client = new RCPlatformAsyncHttpClient(RequestAction.JSON);
		PhotoTalkParams.buildBasicParams(context, client);
		client.putRequestParam(PhotoTalkParams.UserSetting.PARAM_KEY_RECEIVE_SETTING, userInfo.getReceiveSet() + "");
		client.putRequestParam(PhotoTalkParams.UserSetting.PARAM_KEY_TREND_SETTING, userInfo.getTrendsSet() + "");
		client.post(context, MenueApiUrl.USER_SETTING_URL, responseHandler);
	}

	public static void checkCurrentPassword(Context context, RCPlatformResponseHandler responseHandler, String currentPassword) {
		RCPlatformAsyncHttpClient client = new RCPlatformAsyncHttpClient(RequestAction.JSON);
		PhotoTalkParams.buildBasicParams(context, client);
		client.putRequestParam(PhotoTalkParams.ChangePassword.PARAM_KEY_CHECK_PASSWORD, currentPassword);
		client.post(context, MenueApiUrl.CHECK_LOGIN_PASSWORD_URL, responseHandler);
	}

	public static void changePassword(Context context, RCPlatformResponseHandler responseHandler, String newPassword) {
		RCPlatformAsyncHttpClient client = new RCPlatformAsyncHttpClient(RequestAction.JSON);
		PhotoTalkParams.buildBasicParams(context, client);
		client.putRequestParam(PhotoTalkParams.ChangePassword.PARAM_KEY_NEW_PASSWORD, newPassword);
		client.post(context, MenueApiUrl.UPDATE_LOGIN_PASSWORD_URL, responseHandler);
	}
}
