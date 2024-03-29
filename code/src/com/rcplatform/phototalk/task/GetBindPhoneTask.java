package com.rcplatform.phototalk.task;

import org.json.JSONObject;

import android.content.Context;

import com.rcplatform.phototalk.api.MenueApiUrl;
import com.rcplatform.phototalk.api.PhotoTalkParams;
import com.rcplatform.phototalk.api.RCPlatformAsyncHttpClient;
import com.rcplatform.phototalk.api.RCPlatformResponse;
import com.rcplatform.phototalk.api.RCPlatformResponseHandler;
import com.rcplatform.phototalk.api.RCPlatformAsyncHttpClient.RequestAction;

public class GetBindPhoneTask {
	RCPlatformAsyncHttpClient client;
	public static interface OnBindSuccessListener {
		public void onBindSuccess(String phoneNumber);

		public void onBindFail();
	}

	private Context mContext;
	private OnBindSuccessListener mListener;

	public GetBindPhoneTask(Context context, OnBindSuccessListener listener) {
		mContext = context;
		mListener = listener;
	}

	public void start() {
		client = new RCPlatformAsyncHttpClient(RequestAction.JSON);
		PhotoTalkParams.buildBasicParams(mContext, client);
		client.post(mContext.getApplicationContext(), MenueApiUrl.CHECK_USER_PHONEBIND_URL, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				if (!isCancel) {
					try {
						JSONObject jObj = new JSONObject(content);
						String phoneNumber = jObj.getString(RCPlatformResponse.CheckPhoneBindState.RESPONSE_KEY_PHONE_NUMBER);
						if (mListener != null)
							mListener.onBindSuccess(phoneNumber);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			@Override
			public void onFailure(int errorCode, String content) {
				if (mListener != null && !isCancel)
					mListener.onBindFail();
			}
		});

	}

	private boolean isCancel = false;

	public void cancel() {
		isCancel = true;
		client.cancel();
	}

}
