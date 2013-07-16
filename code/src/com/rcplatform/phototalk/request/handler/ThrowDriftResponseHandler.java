package com.rcplatform.phototalk.request.handler;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.rcplatform.phototalk.PhotoTalkApplication;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.galhttprequest.LogUtil;
import com.rcplatform.phototalk.logic.controller.DriftInformationPageController;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.request.RCPlatformServiceError;
import com.rcplatform.phototalk.utils.PrefsUtils;
import com.rcplatform.phototalk.utils.RCThreadPool;

public class ThrowDriftResponseHandler implements RCPlatformResponseHandler {
	private Context mContext;;
	private long mFlag;
	private String mPath;

	public ThrowDriftResponseHandler(Context context, long flag, String filePath) {
		mContext = context;
		this.mFlag = flag;
		this.mPath = filePath;
	}

	@Override
	public void onFailure(int errorCode, String content) {
		PhotoTalkDatabaseFactory.getDatabase().updateDriftInformationSendFail(mFlag);
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
			RCThreadPool.getInstance().addTask(new Runnable() {

				@Override
				public void run() {
					int count = PhotoTalkDatabaseFactory.getDatabase().getUnSendDriftInformationCountByUrl(mPath);
					int countInfo = PhotoTalkDatabaseFactory.getDatabase().getUnSendInformationCountByUrl(mPath);
					if (count == 0 && countInfo == 0) {
						LogUtil.e("delete temp files");
						new File(mPath).delete();
					}
				}
			});
		} catch (JSONException e) {
			onFailure(RCPlatformServiceError.ERROR_CODE_REQUEST_FAIL, content);
			e.printStackTrace();
		}
	}

}
