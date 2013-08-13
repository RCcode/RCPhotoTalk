package com.rcplatform.phototalk.proxy;

import java.io.File;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;

import com.rcplatform.phototalk.PhotoTalkApplication;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.api.PhotoTalkApiUrl;
import com.rcplatform.phototalk.bean.InformationCategory;
import com.rcplatform.phototalk.bean.InformationState;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.request.PhotoTalkParams;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.request.RCPlatformServiceError;
import com.rcplatform.phototalk.request.Request;
import com.rcplatform.phototalk.request.handler.SendInformationResponseHandler;
import com.rcplatform.phototalk.request.inf.PhotoSendListener;

public class InformationProxy {
	public static void sendInformation(Context context, long flag, File file, String timeLimit, final List<String> friendIds, boolean hasVoice,
			boolean hasGraf, boolean hasText, int informationCate, PhotoSendListener listener) {
		String url = null;
		if (informationCate == InformationCategory.VIDEO) {
			url = PhotoTalkApiUrl.SEND_VIDEO_URL;
		} else {
			url = PhotoTalkApiUrl.SEND_PICTURE_URL;
		}
		UserInfo currentUser = ((PhotoTalkApplication) context.getApplicationContext()).getCurrentUser();
		try {
			JSONArray jsonArray = new JSONArray();
			for (String rcId : friendIds) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put(PhotoTalkParams.SendVideo.PARAM_KEY_RECEIVER_ID, rcId);
				jsonArray.put(jsonObject);
			}
			RCPlatformResponseHandler responseHandler = null;
			if (listener != null) {
				responseHandler = new SendInformationResponseHandler(currentUser, friendIds, timeLimit, hasVoice, informationCate, context, file, flag,
						listener);
			}
			Request request = new Request(context, url, responseHandler);
			request.setCreateTime(flag);
			request.setFile(file);
			request.putParam(PhotoTalkParams.SendVideo.PARAM_KEY_FLAG, flag + "");
			request.putParam(PhotoTalkParams.SendVideo.PARAM_KEY_TIME_LIMIT, timeLimit);
			request.putParam(PhotoTalkParams.SendVideo.PARAM_KEY_USERS, jsonArray.toString());
			if (hasVoice)
				request.putParam(PhotoTalkParams.SendVideo.PARAM_KEY_HAS_VOICE, PhotoTalkParams.PARAM_VALUE_CONFIRM);
			else
				request.putParam(PhotoTalkParams.SendVideo.PARAM_KEY_HAS_VOICE, PhotoTalkParams.PARAM_VALUE_NEGATE);
			if (hasGraf)
				request.putParam(PhotoTalkParams.SendVideo.PARAM_KEY_HAS_GRAF, PhotoTalkParams.PARAM_VALUE_CONFIRM);
			else
				request.putParam(PhotoTalkParams.SendVideo.PARAM_KEY_HAS_GRAF, PhotoTalkParams.PARAM_VALUE_NEGATE);

			if (hasText)
				request.putParam(PhotoTalkParams.SendVideo.PARAM_KEY_HAS_TEXT, PhotoTalkParams.PARAM_VALUE_CONFIRM);
			else
				request.putParam(PhotoTalkParams.SendVideo.PARAM_KEY_HAS_TEXT, PhotoTalkParams.PARAM_VALUE_NEGATE);

			request.census();
			request.excuteAsync();
		} catch (Exception e) {
			e.printStackTrace();
			listener.onFail(flag, RCPlatformServiceError.ERROR_CODE_REQUEST_FAIL, context.getString(R.string.net_error));
			PhotoTalkDatabaseFactory.getDatabase().updateTempInformations(currentUser, null, flag, null, friendIds,
					InformationState.PhotoInformationState.STATU_NOTICE_SEND_OR_LOAD_FAIL, Integer.parseInt(timeLimit), hasVoice, informationCate);
		}
	}
}
