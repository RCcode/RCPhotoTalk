package com.rcplatform.phototalk.task;

import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;

import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.api.MenueApiUrl;
import com.rcplatform.phototalk.listener.UpdateDialogClickListener;
import com.rcplatform.phototalk.request.PhotoTalkParams;
import com.rcplatform.phototalk.request.RCPlatformAsyncHttpClient;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.request.RCPlatformAsyncHttpClient.RequestAction;
import com.rcplatform.phototalk.utils.Contract;
import com.rcplatform.phototalk.utils.PrefsUtils;

public class CheckUpdateTask {
	private RCPlatformAsyncHttpClient mClient;
	private Context mContext;
	private boolean isAutoRequest = true;
	private OnUpdateCheckListener mOnUpdateCheckListener;

	public CheckUpdateTask(Context context, boolean isAuto) {
		mClient = new RCPlatformAsyncHttpClient();
		this.mContext = context;
		PhotoTalkParams.buildBasicParams(mContext, mClient);
		this.isAutoRequest = isAuto;
	}

	/**
	 * {"message":"成功","status":0,"appConfig":{"language":"zh","appId":1,
	 * "createTime"
	 * :1363064832000,"updateTime":1363064865000,"appName":"photochat"
	 * ,"versionId":3,"clientFs":50,"serverFs":50,"isUpdate":3,"notice":
	 * "the king of young men","clientVs":1,"serverVs":1,"updateUrl":"asdasd"}}
	 */
	public void start() {
		if (isAutoRequest) {
			long lastCheckTime = PrefsUtils.AppInfo.getLastCheckUpdateTime(mContext);
			if ((System.currentTimeMillis() - lastCheckTime) < Contract.UPDATE_CHECK_WAITING_TIME)
				return;
		}
		mClient.post(mContext, MenueApiUrl.CHECK_UPATE_URL, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				try {
					JSONObject jsonObject = new JSONObject(content).getJSONObject("appConfig");
					String version = jsonObject.getString("clientVs");
					if (!mContext.getString(R.string.version).equals(version)) {
						String newVersion = jsonObject.getString("clientVs");
						if (isAutoRequest && newVersion.equals(PrefsUtils.AppInfo.getNeverAttentionVersion(mContext))) {
							return;
						}
						String updateContent = jsonObject.getString("notice");
						String updateUrl = jsonObject.getString("updateUrl");
						if (mOnUpdateCheckListener != null) {
							mOnUpdateCheckListener.onHasUpdate(newVersion, updateContent, updateUrl);
							return;
						}
						UpdateDialogClickListener mUpdateListener = new UpdateDialogClickListener(mContext, updateUrl, newVersion);
						AlertDialog.Builder builder = new AlertDialog.Builder(mContext).setMessage(updateContent).setTitle(mContext.getString(R.string.update_dialog_title, mContext.getString(R.string.app_name), newVersion)).setNegativeButton(R.string.update_now, mUpdateListener)
								.setPositiveButton(R.string.attention_later, mUpdateListener);
						AlertDialog mUpdateDialog = builder.create();
						mUpdateDialog.setCancelable(false);
						mUpdateDialog.show();
					} else {
						if (mOnUpdateCheckListener != null) {
							mOnUpdateCheckListener.onNoNewVersion();
							return;
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					if (mOnUpdateCheckListener != null) {
						mOnUpdateCheckListener.onError(statusCode, mContext.getString(R.string.net_error));
					}
				}
			}

			@Override
			public void onFailure(int errorCode, String content) {
				if (mOnUpdateCheckListener != null) {
					mOnUpdateCheckListener.onError(errorCode, content);
				}
			}
		});
	}

	public static interface OnUpdateCheckListener {
		public void onHasUpdate(String versionCode, String updateContent, String updateUrl);

		public void onNoNewVersion();

		public void onError(int statusCode, String message);
	}

	public void cancel() {
		mClient.cancel(mContext);
	}

	public void setAutoRequest(boolean isAuto) {
		this.isAutoRequest = isAuto;
	}

	public void setOnUpdateCheckListener(OnUpdateCheckListener listener) {
		this.mOnUpdateCheckListener = listener;
	}
}
