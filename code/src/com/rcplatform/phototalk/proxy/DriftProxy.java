package com.rcplatform.phototalk.proxy;

import java.util.List;

import org.json.JSONArray;

import android.content.Context;

import com.rcplatform.phototalk.PhotoTalkApplication;
import com.rcplatform.phototalk.api.PhotoTalkApiUrl;
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

	public static void getMaxFishTime(Context context, RCPlatformResponseHandler handler) {
		Request request = new Request(context, "", handler);
		request.excuteAsync();
	}

	public static void fishDrift(Context context, RCPlatformResponseHandler handler) {
		Request request = new Request(context, "", handler);
		UserInfo userInfo = ((PhotoTalkApplication) context.getApplicationContext()).getCurrentUser();
		request.putParam(PhotoTalkParams.FishDrift.PARAM_KEY_COUNTRY, userInfo.getCountry());
		request.putParam(PhotoTalkParams.FishDrift.PARAM_KEY_GENDER, userInfo.getGender() + "");
		request.putParam(PhotoTalkParams.FishDrift.PARAM_KEY_NICK, userInfo.getNickName());
		request.putParam(PhotoTalkParams.FishDrift.PARAM_KEY_OS_NAME, Constants.OS_NAME);
		request.putParam(PhotoTalkParams.FishDrift.PARAM_KEY_OS_VERSION, Constants.OS_VERSION);
		request.putParam(PhotoTalkParams.FishDrift.PARAM_KEY_TIMEZONE, Utils.getTimeZoneId(context) + "");
		request.excuteAsync();
	}

	public static void getDriftInformationShowTime(Context context, RCPlatformResponseHandler responseHandler, List<Integer> picIds) {
		JSONArray jsonArray = new JSONArray();
		for (int picId : picIds) {
			jsonArray.put(picId);
		}
		Request request = new Request(context, "", responseHandler);
		request.putParam(PhotoTalkParams.DriftShowTime.PARAM_KEY_PICIDS, jsonArray.toString());
		request.excuteAsync();
	}
}
