package com.rcplatform.phototalk.task;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;

import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.api.PhotoTalkApiUrl;
import com.rcplatform.phototalk.listener.UpdateDialogClickListener;
import com.rcplatform.phototalk.request.RCPlatformAsyncHttpClient;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.request.Request;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.PrefsUtils;

public class CheckUpdateTask {
	private RCPlatformAsyncHttpClient mClient;
	private Context mContext;
	private boolean isAutoRequest = true;
	private OnUpdateCheckListener mOnUpdateCheckListener;
	private Request mRequest;

	public CheckUpdateTask(Context context, boolean isAuto) {
		mClient = new RCPlatformAsyncHttpClient();
		mRequest = new Request(context, PhotoTalkApiUrl.CHECK_UPATE_URL, new RCPlatformResponseHandler() {

			@SuppressLint("NewApi")
			@Override
			public void onSuccess(int statusCode, String content) {
				try {
					JSONObject jsonObject = new JSONObject(content).getJSONObject("appConfig");
					String version = jsonObject.getString("clientVersion");
					if (!mContext.getString(R.string.version).equals(version)) {
						String newVersion = jsonObject.getString("clientVersion");
						if (isAutoRequest && newVersion.equals(PrefsUtils.AppInfo.getNeverAttentionVersion(mContext))) {
							return;
						}
						String updateContent = jsonObject.getString("verText");
						String updateUrl = jsonObject.getString("appUrl");
						if (mOnUpdateCheckListener != null) {
							mOnUpdateCheckListener.onHasUpdate(newVersion, updateContent, updateUrl);
							return;
						}
						UpdateDialogClickListener mUpdateListener = new UpdateDialogClickListener(mContext, updateUrl, newVersion);
						AlertDialog.Builder builder =null;
						if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.GINGERBREAD_MR1) {
							builder = new AlertDialog.Builder(mContext,R.style.Theme_Dialog_Update).setMessage(updateContent)
									.setTitle(mContext.getString(R.string.update_dialog_title))
									.setNegativeButton(R.string.update_now, mUpdateListener).setPositiveButton(R.string.attention_later, mUpdateListener);
						}else{
							builder = new AlertDialog.Builder(mContext).setMessage(updateContent)
									.setTitle(mContext.getString(R.string.update_dialog_title))
									.setNegativeButton(R.string.update_now, mUpdateListener).setPositiveButton(R.string.attention_later, mUpdateListener);
						}
						
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
