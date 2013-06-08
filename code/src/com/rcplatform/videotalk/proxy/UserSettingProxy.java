package com.rcplatform.videotalk.proxy;

import android.content.Context;

import com.rcplatform.videotalk.api.PhotoTalkApiUrl;
import com.rcplatform.videotalk.bean.UserInfo;
import com.rcplatform.videotalk.request.PhotoTalkParams;
import com.rcplatform.videotalk.request.RCPlatformResponseHandler;
import com.rcplatform.videotalk.request.Request;

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

	public static Request checkTrends(Context context, RCPlatformResponseHandler responseHandler, int maxTrendId) {
		Request request = new Request(context, PhotoTalkApiUrl.CHECK_TRENDS_URL, responseHandler);
		request.putParam(PhotoTalkParams.CheckTrends.PARAM_KEY_TRENDID, maxTrendId + "");
		request.excuteAsync();
		return request;
	}

	public static void requestSMS(Context context, RCPlatformResponseHandler responseHandler, String number) {
		Request request = new Request(context, PhotoTalkApiUrl.REQUEST_SMS_URL, responseHandler);
		request.putParam(PhotoTalkParams.RequestSMS.PARAM_KEY_NUMBER, number);
		request.excuteAsync();
	}

	public static void bindPhone(Context context, RCPlatformResponseHandler responseHandler, String validate,String phoneNumber) {
		Request request = new Request(context, PhotoTalkApiUrl.BIND_PHONE_URL, responseHandler);
		request.putParam(PhotoTalkParams.BindPhone.PARAM_KEY_NUMBER, phoneNumber);
		request.putParam(PhotoTalkParams.BindPhone.PARAM_KEY_CODE, validate);
		request.excuteAsync();
	}
}