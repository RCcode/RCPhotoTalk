package com.rcplatform.phototalk.galhttprequest;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;

import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.api.MenueApiFactory;
import com.rcplatform.phototalk.galhttprequest.GalHttpRequest.GalHttpLoadTextCallBack;

public abstract class RCLoadTextCallBack implements GalHttpLoadTextCallBack {
	private Activity mContext;

	public RCLoadTextCallBack(Activity context) {
		// TODO Auto-generated constructor stub
		this.mContext = context;
	}

	@Override
	public void textLoaded(String text) {
		// TODO Auto-generated method stub
	
		try {
			JSONObject jsonObject = new JSONObject(text);
			int statusCode = jsonObject
					.getInt(MenueApiFactory.RESPONSE_KEY_STATUS);
			if (statusCode == MenueApiFactory.RESPONSE_STATE_SUCCESS) {
				onSuccess(statusCode,text);
			} else {
				onError(statusCode,
						jsonObject
								.getString(MenueApiFactory.RESPONSE_KEY_MESSAGE));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			onError(RCPlatformServiceError.ERROR_CODE_REQUEST_FAIL,
					RCPlatformServiceError.getErrorMessage(mContext,
							RCPlatformServiceError.ERROR_CODE_REQUEST_FAIL));
		}
		if(mContext instanceof BaseActivity){
			((BaseActivity)mContext).dismissLoadingDialog();
		}
	}

	@Override
	public void loadFail() {
		// TODO Auto-generated method stub
		if(mContext instanceof BaseActivity){
			((BaseActivity)mContext).dismissLoadingDialog();
		}
		onError(RCPlatformServiceError.ERROR_CODE_REQUEST_FAIL,
				RCPlatformServiceError.getErrorMessage(mContext,
						RCPlatformServiceError.ERROR_CODE_REQUEST_FAIL));
	}

	public abstract void onSuccess(int statusCode,String text);

	public abstract void onError(int errorCode, String error);
}
