package com.rcplatform.phototalk.request.handler;

import org.json.JSONException;
import org.json.JSONObject;

import com.rcplatform.phototalk.request.RCPlatformResponseHandler;

public class ThrowDriftResponseHandler implements RCPlatformResponseHandler {

	@Override
	public void onFailure(int errorCode, String content) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSuccess(int statusCode, String content) {
		try {
			JSONObject jsonObject = new JSONObject(content);
			long flag = jsonObject.getLong("flag");
			int picId=jsonObject.getInt("picId");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
