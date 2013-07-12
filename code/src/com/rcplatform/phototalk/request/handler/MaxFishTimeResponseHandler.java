package com.rcplatform.phototalk.request.handler;

import org.json.JSONException;
import org.json.JSONObject;

import com.rcplatform.phototalk.activity.ActivityFunction;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.utils.PrefsUtils;

public class MaxFishTimeResponseHandler implements RCPlatformResponseHandler {
	private ActivityFunction mFunction;

	public MaxFishTimeResponseHandler(ActivityFunction mFunction) {
		this.mFunction = mFunction;
	}

	@Override
	public void onFailure(int errorCode, String content) {
	}

	@Override
	public void onSuccess(int statusCode, String content) {
		try {
			JSONObject jsonObject = new JSONObject(content);
			int maxTime = jsonObject.getInt("maxTime");
			if (maxTime == -1)
				maxTime = Integer.MAX_VALUE;
			PrefsUtils.AppInfo.setMaxFishTime(mFunction.getContext(), maxTime);
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}
}
