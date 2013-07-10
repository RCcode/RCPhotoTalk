package com.rcplatform.phototalk.request.handler;

import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.activity.ActivityFunction;
import com.rcplatform.phototalk.drift.DriftInformation;
import com.rcplatform.phototalk.request.JSONConver;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.request.RCPlatformServiceError;

public class FishDriftResponseHandler implements RCPlatformResponseHandler {

	private OnFishListener mListener;
	private ActivityFunction mFunction;

	public FishDriftResponseHandler(ActivityFunction function, OnFishListener listener) {
		this.mFunction = function;
		this.mListener = listener;
	}

	public static interface OnFishListener {
		public void onFishSuccess(DriftInformation information);

		public void onFishFail(String failReason);
	}

	@Override
	public void onFailure(int errorCode, String content) {
		mFunction.dissmissLoadingDialog();
		if (mListener != null)
			mListener.onFishFail(content);
	}

	@Override
	public void onSuccess(int statusCode, String content) {
		DriftInformation driftInformation = JSONConver.jsonToObject(content, DriftInformation.class);
		if (driftInformation == null || driftInformation.getPicId() == 0) {
			onFailure(RCPlatformServiceError.ERROR_CODE_REQUEST_FAIL, mFunction.getContext().getString(R.string.net_error));
		} else {
			if (mListener != null)
				mListener.onFishSuccess(driftInformation);
		}
	}

}
