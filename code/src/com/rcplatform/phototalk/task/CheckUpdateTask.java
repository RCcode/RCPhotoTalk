package com.rcplatform.phototalk.task;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;

import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.api.PhotoTalkApiUrl;
import com.rcplatform.phototalk.listener.UpdateDialogClickListener;
import com.rcplatform.phototalk.request.RCPlatformAsyncHttpClient;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.request.Request;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.PhotoTalkUtils;
import com.rcplatform.phototalk.utils.PrefsUtils;

public class CheckUpdateTask {
	private RCPlatformAsyncHttpClient mClient;
	private Activity mContext;
	private boolean isAutoRequest = true;
	private OnUpdateCheckListener mOnUpdateCheckListener;
	private Request mRequest;

	public CheckUpdateTask(Activity context, boolean isAuto) {
		mClient = new RCPlatformAsyncHttpClient();
		mRequest = new Request(context, PhotoTalkApiUrl.CHECK_UPATE_URL, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				try {
					JSONObject jsonObject = new JSONObject(content).getJSONObject("appConfig");
					String newVersion = jsonObject.getString("serverVersion");
					if (!mContext.getString(R.string.version).equals(newVersion)) {
						if (isAutoRequest && newVersion.equals(PrefsUtils.AppInfo.getNeverAttentionVersion(mContext))) {
							return;
						}
						String updateUrl = jsonObject.getString("appUrl");
						String updateContent = jsonObject.getString("verText");
						int versionCode = jsonObject.getInt("versionCode");
						boolean isMust = jsonObject.getInt("isUpdate") == 1;
						if (isMust) {
							PrefsUtils.AppInfo.setMustUpdate(mContext, versionCode, updateContent);
						}
						if (mOnUpdateCheckListener != null) {
							mOnUpdateCheckListener.onHasUpdate(newVersion, updateContent, updateUrl, isMust);
							return;
						}
						if (isMust) {
							PhotoTalkUtils.showMustUpdateDialog(mContext,true);
						} else {
							UpdateDialogClickListener mUpdateListener = new UpdateDialogClickListener(mContext, updateUrl, newVersion);
							AlertDialog mUpdateDialog = DialogUtil.getAlertDialogBuilder(mContext).setMessage(updateContent)
									.setTitle(mContext.getString(R.string.update_dialog_title)).setNegativeButton(R.string.attention_later, mUpdateListener)
									.setPositiveButton(R.string.update_now, mUpdateListener).create();
							mUpdateDialog.setCancelable(false);
							mUpdateDialog.show();
						}

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
		this.mContext = context;
		this.isAutoRequest = isAuto;
	}

	public void start() {
		if (isAutoRequest) {
			long lastCheckTime = PrefsUtils.AppInfo.getLastCheckUpdateTime(mContext);
			if ((System.currentTimeMillis() - lastCheckTime) < Constants.UPDATE_CHECK_WAITING_TIME)
				return;
		}
		mClient.post(mRequest);
	}

	public static interface OnUpdateCheckListener {
		public void onHasUpdate(String versionCode, String updateContent, String updateUrl, boolean isMust);

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
