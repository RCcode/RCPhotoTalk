package com.rcplatform.phototalk.listener;

import org.json.JSONException;
import org.json.JSONObject;

import com.rcplatform.phototalk.api.MenueApiFactory;
import com.rcplatform.phototalk.galhttprequest.GalHttpRequest;
import com.rcplatform.phototalk.galhttprequest.GalHttpRequest.GalHttpLoadTextCallBack;

public abstract class RcplatformhkTextCallBack implements GalHttpLoadTextCallBack {
	
	@Override
	public void textLoaded(String text) {
		// TODO Auto-generated method stub
		try {
			JSONObject jsonObject=new JSONObject(text);
			int state = jsonObject.getInt(MenueApiFactory.RESPONSE_KEY_STATUS);
			if (state == MenueApiFactory.RESPONSE_STATE_SUCCESS) {
				onLoadSuccess(text);
			} else {
				onLoadFail(null);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void loadFail() {
		// TODO Auto-generated method stub
		onLoadFail(null);
	}
	
	public abstract void onLoadSuccess(String content);
	public abstract void onLoadFail(String error);

}
