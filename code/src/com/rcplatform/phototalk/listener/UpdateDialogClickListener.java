package com.rcplatform.phototalk.listener;

import com.rcplatform.phototalk.umeng.EventUtil;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.PrefsUtils;
import com.rcplatform.phototalk.utils.Utils;

import android.R;
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
				EventUtil.Main_Photo.rcpt_updatepop_update(mContext);
				Utils.searchAppInGooglePlay(mContext, Constants.PAGEAGE);
				break;
			case DialogInterface.BUTTON_POSITIVE:
				EventUtil.Main_Photo.rcpt_updatepop_later(mContext);
				PrefsUtils.AppInfo.setLastCheckUpdateTime(mContext, System.currentTimeMillis());
				break;

		}
		dialog.dismiss();
	}

}
