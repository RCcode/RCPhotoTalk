package com.rcplatform.phototalk.request.handler;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.SparseIntArray;

import com.rcplatform.phototalk.activity.ActivityFunction;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;

public class DriftShowTimeResponseHandler implements RCPlatformResponseHandler {
	private ActivityFunction mFunction;
	private OnDriftShowTimeListener mListener;

	public DriftShowTimeResponseHandler(ActivityFunction function, OnDriftShowTimeListener listener) {
		this.mFunction = function;
		this.mListener = listener;
	}

	public static interface OnDriftShowTimeListener {
		public void onGetSuccess(SparseIntArray picShowTimes);
	}

	@Override
	public void onFailure(int errorCode, String content) {

	}

	@Override
	public void onSuccess(int statusCode, String content) {
		try {
			JSONObject jsonObject = new JSONObject(content);
			JSONArray picIdsArray = jsonObject.getJSONArray("pics");
			SparseIntArray showTimes = new SparseIntArray();
			for (int i = 0; i < picIdsArray.length(); i++) {
				JSONObject jsonTime = picIdsArray.getJSONObject(i);
				showTimes.put(jsonTime.getInt("picId"), jsonTime.getInt("num"));
			}
			if (mListener != null)
				mListener.onGetSuccess(showTimes);
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

}
