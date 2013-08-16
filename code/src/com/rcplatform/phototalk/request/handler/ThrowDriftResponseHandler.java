package com.rcplatform.phototalk.request.handler;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.rcplatform.phototalk.PhotoTalkApplication;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.bean.InformationCategory;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.galhttprequest.LogUtil;
import com.rcplatform.phototalk.logic.SendingInformationManager;
import com.rcplatform.phototalk.logic.controller.DriftInformationPageController;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.request.RCPlatformServiceError;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.NotificationSender;
import com.rcplatform.phototalk.utils.PhotoTalkUtils;
import com.rcplatform.phototalk.utils.PrefsUtils;
import com.rcplatform.phototalk.utils.RCThreadPool;

public class ThrowDriftResponseHandler implements RCPlatformResponseHandler {
	private Context mContext;;
	private long mFlag;
	private String mPath;
	private int informationCate;

	public ThrowDriftResponseHandler(Context context, long flag, String filePath, int informationCate) {
		mContext = context;
		this.mFlag = flag;
		this.mPath = filePath;
		this.informationCate = informationCate;
	}

	@Override
	public void onFailure(int errorCode, String content) {
		PhotoTalkDatabaseFactory.getDatabase().updateDriftInformationSendFail(mFlag);
		DriftInformationPageController.getInstance().onDriftInformationSendFail(mFlag);
		if (informationCate == InformationCategory.VIDEO) {
			int notificationId = SendingInformationManager.getInstance().getDriftNotificationId();
			NotificationSender.getInstance(mContext).sendNotification(mContext.getString(R.string.sending_to_stranger),
					mContext.getString(R.string.send_video_fail), R.drawable.ic_launcher, PhotoTalkUtils.getNotificationDriftInformationIntent(mContext),
					notificationId, true);
		}
	}

	@Override
	public void onSuccess(int statusCode, String content) {
		try {
			PrefsUtils.User.setThrowToday(mContext, ((PhotoTalkApplication) mContext.getApplicationContext()).getCurrentUser().getRcId());
			JSONObject jsonObject = new JSONObject(content);
			long flag = jsonObject.getLong("flag");
			int picId = jsonObject.getInt("picId");
			if (informationCate == InformationCategory.VIDEO) {
				int notificationId = SendingInformationManager.getInstance().getDriftNotificationId();
				NotificationSender.getInstance(mContext).sendNotification(mContext.getString(R.string.sending_to_stranger),
						mContext.getString(R.string.send_success), R.drawable.ic_launcher, PhotoTalkUtils.getNotificationDriftInformationIntent(mContext),
						notificationId, false);
				NotificationSender.getInstance(mContext).cancelNotification(notificationId, Constants.TimeMillins.SEND_SUCCESS_NOTIFICATION_SHOW_TIME);
			}
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
