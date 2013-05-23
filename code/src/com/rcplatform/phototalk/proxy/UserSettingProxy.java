package com.rcplatform.phototalk.proxy;

import android.content.Context;

import com.rcplatform.phototalk.api.PhotoTalkApiUrl;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.request.PhotoTalkParams;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.request.Request;

public class UserSettingProxy {
	public static void updateUserSetting(Context context, RCPlatformResponseHandler responseHandler, UserInfo userInfo) {
		Request request = new Request(context, PhotoTalkApiUrl.USER_SETTING_URL, responseHandler);
		request.putParam(PhotoTalkParams.UserSetting.PARAM_KEY_RECEIVE_SETTING, userInfo.getAllowsend() + "");
		request.putParam(PhotoTalkParams.UserSetting.PARAM_KEY_TREND_SETTING, userInfo.getShareNews() + "");
		request.excuteAsync();
	}

	public static void checkCurrentPassword(Context context, RCPlatformResponseHandler responseHandler, String currentPassword) {
		Request request = new Request(context, PhotoTalkApiUrl.CHECK_LOGIN_PASSWORD_URL, responseHandler);
		request.putParam(PhotoTalkParams.ChangePassword.PARAM_KEY_CHECK_PASSWORD, currentPassword);
		request.excuteAsync();
	}

	public static void changePassword(Context context, RCPlatformResponseHandler responseHandler, String newPassword, String oldPassword) {
		Request request = new Request(context, PhotoTalkApiUrl.UPDATE_LOGIN_PASSWORD_URL, responseHandler);
		request.putParam(PhotoTalkParams.ChangePassword.PARAM_KEY_NEW_PASSWORD, newPassword);
		request.putParam(PhotoTalkParams.ChangePassword.PARAM_KEY_OLD_PASSWORD, oldPassword);
		request.excuteAsync();
	}

	public static void getAllAppInfo(Context context, RCPlatformResponseHandler responseHandler) {
		Request request = new Request(context, PhotoTalkApiUrl.GET_ALL_APPS_URL, responseHandler);
		request.excuteAsync();
	}
}
