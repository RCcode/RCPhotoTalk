package com.rcplatform.phototalk.request.handler;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.rcplatform.phototalk.PhotoTalkApplication;
import com.rcplatform.phototalk.activity.ActivityFunction;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.logic.controller.DriftInformationPageController;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.utils.PrefsUtils;

public class ThrowDriftResponseHandler implements RCPlatformResponseHandler {
	private Context mContext;;
	private long mFlag;

	public ThrowDriftResponseHandler(Context context, long flag) {
		mContext = context;
		this.mFlag = flag;
	}

	@Override
	public void onFailure(int errorCode, String content) {
		DriftInformationPageController.getInstance().onDriftInformationSendFail(mFlag);
	}

	@Override
	public void onSuccess(int statusCode, String content) {
		try {
			PrefsUtils.User.setThrowToday(mContext, ((PhotoTalkApplication) mContext.getApplicationContext()).getCurrentUser().getRcId());
			JSONObject jsonObject = new JSONObject(content);
			long flag = jsonObject.getLong("flag");
			int picId = jsonObject.getInt("picId");
			PhotoTalkDatabaseFactory.getDatabase().updateDriftInformationSendSuccess(flag, picId);
			DriftInformationPageController.getInstance().onDriftInformationSendSuccess(flag, picId);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

}
