package com.rcplatform.phototalk.listener;

import com.rcplatform.phototalk.utils.PrefsUtils;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;

public class UpdateDialogClickListener implements OnClickListener {
	private Context mContext;
	private String mUpdateUrl;
	private String mVersion;

	public UpdateDialogClickListener(Context context, String updateUrl, String version) {
		this.mContext = context;
		this.mUpdateUrl = updateUrl;
		this.mVersion = version;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case DialogInterface.BUTTON_NEGATIVE:
			// TODO 现在更新
			break;
		case DialogInterface.BUTTON_NEUTRAL:
			PrefsUtils.AppInfo.setLastCheckUpdateTime(mContext, System.currentTimeMillis());
			break;
		case DialogInterface.BUTTON_POSITIVE:
			PrefsUtils.AppInfo.setNeverAttentionVersion(mContext.getApplicationContext(), mVersion);
			break;

		}
		dialog.dismiss();
	}

}
