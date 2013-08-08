package com.rcplatform.phototalk.request.handler;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;

public class UpdateUserInfoResponseHandler implements RCPlatformResponseHandler {

	private UpdateUserInfoListener mListener;
	private Context context;

	public static interface UpdateUserInfoListener {
		public void onUpdateSucess(String headUrl);

		public void onUpdateFail(String failReason);
	}

	public UpdateUserInfoResponseHandler(Context context, UpdateUserInfoListener listener) {
		this.mListener = listener;
		this.context = context;
	}

	@Override
	public void onFailure(int errorCode, String content) {
		if (mListener != null)
			mListener.onUpdateFail(content);
	}

	@Override
	public void onSuccess(int statusCode, String content) {
		JSONObject jsonObject;
		try {
			jsonObject = new JSONObject(content);
			String url = jsonObject.optString("headUrl", null);
			if (mListener != null)
				mListener.onUpdateSucess(url);
		} catch (JSONException e) {
			e.printStackTrace();
			onFailure(0, context.getString(R.string.net_error));
		}

	}
}
