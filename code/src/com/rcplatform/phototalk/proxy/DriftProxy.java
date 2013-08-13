package com.rcplatform.phototalk.proxy;

import java.io.File;
import java.util.List;

import android.content.Context;

import com.google.gson.Gson;
import com.rcplatform.phototalk.PhotoTalkApplication;
import com.rcplatform.phototalk.api.PhotoTalkApiUrl;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.drift.DriftInformation;
import com.rcplatform.phototalk.request.PhotoTalkParams;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.request.Request;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.Utils;

public class DriftProxy {

	public static void reportPic(Context context, RCPlatformResponseHandler handler, DriftInformation information) {
		Request request = new Request(context, PhotoTalkApiUrl.REPORT_URL, handler);
		UserInfo userInfo = ((PhotoTalkApplication) context.getApplicationContext()).getCurrentUser();
		request.putParam(PhotoTalkParams.ReportPicture.PARAM_KEY_COUNTRY, userInfo.getCountry());
		request.putParam(PhotoTalkParams.ReportPicture.PARAM_KEY_GENDER, userInfo.getGender() + "");
		request.putParam(PhotoTalkParams.ReportPicture.PARAM_KEY_PICID, information.getPicId() + "");
		request.putParam(PhotoTalkParams.ReportPicture.PARAM_KEY_PICURL, information.getUrl());
		request.putParam(PhotoTalkParams.ReportPicture.PARAM_KEY_REP_COUNTRY, information.getSender().getCountry());
		request.putParam(PhotoTalkParams.ReportPicture.PARAM_KEY_REP_GENDER, information.getSender().getGender() + "");
		request.putParam(PhotoTalkParams.ReportPicture.PARAM_KEY_REP_RCID, information.getSender().getRcId());
		request.excuteAsync();
	}

	public static void AddFriend(Context context, RCPlatformResponseHandler handler, DriftInformation information) {
		Request request = new Request(context, PhotoTalkApiUrl.SKY_POOL_ADD_FRIEND, handler);
		UserInfo userInfo = ((PhotoTalkApplication) context.getApplicationContext()).getCurrentUser();
		request.putParam(PhotoTalkParams.ReportPicture.PARAM_KEY_COUNTRY, userInfo.getCountry());
		request.putParam(PhotoTalkParams.ReportPicture.PARAM_KEY_GENDER, userInfo.getGender() + "");
		request.putParam(PhotoTalkParams.ReportPicture.PARAM_KEY_PICID, information.getPicId() + "");
		// request.putParam(PhotoTalkParams.ReportPicture.PARAM_KEY_PICURL,
		// information.getUrl());
		request.putParam(PhotoTalkParams.ReportPicture.PARAM_KEY_REP_COUNTRY, information.getSender().getCountry());
		request.putParam(PhotoTalkParams.ReportPicture.PARAM_KEY_REP_GENDER, information.getSender().getGender() + "");
		// request.putParam(PhotoTalkParams.ReportPicture.PARAM_KEY_REP_RCID,
		// information.getSender().getRcId());
		request.excuteAsync();
	}

	public static void getMaxFishTime(Context context, RCPlatformResponseHandler handler) {
		Request request = new Request(context, PhotoTalkApiUrl.MAX_FISH_TIMES_URL, handler);
		request.excuteAsync();
	}

	public static void fishDrift(Context context, RCPlatformResponseHandler handler, boolean isCountry, boolean isGender) {
		Request request = new Request(context, PhotoTalkApiUrl.FISH_DRIFT, handler);
		UserInfo userInfo = ((PhotoTalkApplication) context.getApplicationContext()).getCurrentUser();
		request.putParam(PhotoTalkParams.FishDrift.PARAM_KEY_COUNTRY, userInfo.getCountry());
		request.putParam(PhotoTalkParams.FishDrift.PARAM_KEY_GENDER, userInfo.getGender() + "");
		request.putParam(PhotoTalkParams.FishDrift.PARAM_KEY_NICK, userInfo.getNickName());
		request.putParam(PhotoTalkParams.FishDrift.PARAM_KEY_OS_NAME, Constants.OS_NAME);
		request.putParam(PhotoTalkParams.FishDrift.PARAM_KEY_OS_VERSION, Constants.OS_VERSION);
		request.putParam(PhotoTalkParams.FishDrift.PARAM_KEY_TIMEZONE, Utils.getTimeZoneId(context) + "");
		request.putParam(PhotoTalkParams.FishDrift.PARAM_KEY_TIMESNAMP, System.currentTimeMillis() + "");
		if (isCountry)
			request.putParam(PhotoTalkParams.FishDrift.PARAM_KEY_IS_COUNTRY, PhotoTalkParams.PARAM_VALUE_CONFIRM);
		else
			request.putParam(PhotoTalkParams.FishDrift.PARAM_KEY_IS_COUNTRY, PhotoTalkParams.PARAM_VALUE_NEGATE);
		if (isGender)
			request.putParam(PhotoTalkParams.FishDrift.PARAM_KEY_IS_GENDER, PhotoTalkParams.PARAM_VALUE_CONFIRM);
		else
			request.putParam(PhotoTalkParams.FishDrift.PARAM_KEY_IS_GENDER, PhotoTalkParams.PARAM_VALUE_NEGATE);
		request.putParam(PhotoTalkParams.FishDrift.PARAM_KEY_TIMESNAMP, System.currentTimeMillis() + "");
		request.excuteAsync();
	}

	public static void getDriftInformationShowTime(Context context, RCPlatformResponseHandler responseHandler, List<Integer> picIds) {
		String pids = new Gson().toJson(picIds);
		Request request = new Request(context, PhotoTalkApiUrl.DRIFT_SHOW_TIME_URL, responseHandler);
		request.putParam(PhotoTalkParams.DriftShowTime.PARAM_KEY_PICIDS, pids);
		request.excuteAsync();
	}

	public static void throwDriftInformation(Context context, RCPlatformResponseHandler responseHandler, UserInfo currentUser, String picUrl,
			String totalLength, boolean hasGraf, boolean hasVoice, String filePath, long flag, int informationCate) {
		Request request = new Request(context, PhotoTalkApiUrl.THROW_DRIFT_URL, responseHandler);
		request.putParam(PhotoTalkParams.ThrowDriftInformation.PARAM_KEY_BACKGROUND, currentUser.getBackground());
		request.putParam(PhotoTalkParams.ThrowDriftInformation.PARAM_KEY_COUNTRY, currentUser.getCountry());
		if (picUrl == null)
			request.setFile(new File(filePath));
		else
			request.putParam(PhotoTalkParams.ThrowDriftInformation.PARAM_KEY_PICURL, picUrl);
		request.putParam(PhotoTalkParams.ThrowDriftInformation.PARAM_KEY_HEAD_URL, currentUser.getHeadUrl());
		request.putParam(PhotoTalkParams.ThrowDriftInformation.PARAM_KEY_FLAG, flag + "");
		request.putParam(PhotoTalkParams.ThrowDriftInformation.PARAM_KEY_GENDER, currentUser.getGender() + "");
		request.putParam(PhotoTalkParams.ThrowDriftInformation.PARAM_KEY_NICK, currentUser.getNickName());
		request.putParam(PhotoTalkParams.ThrowDriftInformation.PARAM_KEY_OSNAME, Constants.OS_NAME);
		request.putParam(PhotoTalkParams.ThrowDriftInformation.PARAM_KEY_OSVERSION, Constants.OS_VERSION);
		request.putParam(PhotoTalkParams.ThrowDriftInformation.PARAM_KEY_SHOW_LENGTH, totalLength);
		request.putParam(PhotoTalkParams.ThrowDriftInformation.PARAM_KEY_BIRTHDAY, currentUser.getBirthday());
		request.putParam(PhotoTalkParams.ThrowDriftInformation.PARAM_KEY_TIMEZONE, Utils.getTimeZoneId(context) + "");
		request.putParam(PhotoTalkParams.ThrowDriftInformation.PARAM_KEY_TYPE, informationCate + "");
		if (hasGraf)
			request.putParam(PhotoTalkParams.SendPhoto.PARAM_KEY_HAS_GRAF, PhotoTalkParams.PARAM_VALUE_CONFIRM);
		else
			request.putParam(PhotoTalkParams.SendPhoto.PARAM_KEY_HAS_GRAF, PhotoTalkParams.PARAM_VALUE_NEGATE);
		if (hasVoice)
			request.putParam(PhotoTalkParams.SendPhoto.PARAM_KEY_HAS_VOICE, PhotoTalkParams.PARAM_VALUE_CONFIRM);
		else
			request.putParam(PhotoTalkParams.SendPhoto.PARAM_KEY_HAS_VOICE, PhotoTalkParams.PARAM_VALUE_NEGATE);
		request.executePostNameValuePairAsync();
	}

	public static void serviceLog(Context context, String picUrl, int picId, boolean hasGraf, boolean hasVoice, UserInfo currentUser, Friend receiver) {
		Request request = new Request(context, PhotoTalkApiUrl.DRIFT_BACK_URL, null);
		request.putParam(PhotoTalkParams.DriftBackLog.PARAM_KEY_COUNTRY, currentUser.getCountry());
		request.putParam(PhotoTalkParams.DriftBackLog.PARAM_KEY_GENDER, currentUser.getGender() + "");
		if (hasGraf)
			request.putParam(PhotoTalkParams.DriftBackLog.PARAM_KEY_HAS_GRAF, PhotoTalkParams.DriftBackLog.PARAM_VALUE_HAS_GRAF);
		else
			request.putParam(PhotoTalkParams.DriftBackLog.PARAM_KEY_HAS_GRAF, PhotoTalkParams.DriftBackLog.PARAM_VALUE_NO_GRAF);
		if (hasVoice)
			request.putParam(PhotoTalkParams.DriftBackLog.PARAM_KEY_HAS_VOICE, PhotoTalkParams.DriftBackLog.PARAM_VALUE_HAS_VOICE);
		else
			request.putParam(PhotoTalkParams.DriftBackLog.PARAM_KEY_HAS_VOICE, PhotoTalkParams.DriftBackLog.PARAM_VALUE_NO_VOICE);
		request.putParam(PhotoTalkParams.DriftBackLog.PARAM_KEY_PIC_ID, picId + "");
		request.putParam(PhotoTalkParams.DriftBackLog.PARAM_KEY_PIC_URL, picUrl);
		request.putParam(PhotoTalkParams.DriftBackLog.PARAM_KEY_RECEIVER_COUNTRY, receiver.getCountry());
		request.putParam(PhotoTalkParams.DriftBackLog.PARAM_KEY_RECEIVER_GENDER, receiver.getGender() + "");
		request.putParam(PhotoTalkParams.DriftBackLog.PARAM_KEY_RECEIVER_RCID, receiver.getRcId());
		request.excuteAsync();
	}
}
