package com.rcplatform.videotalk.listener;

import com.rcplatform.videotalk.umeng.EventUtil;
import com.rcplatform.videotalk.utils.PrefsUtils;
import com.rcplatform.videotalk.utils.Utils;

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
				Utils.download(mContext, mUpdateUrl);
				break;
			case DialogInterface.BUTTON_POSITIVE:
				EventUtil.Main_Photo.rcpt_updatepop_later(mContext);
				PrefsUtils.AppInfo.setLastCheckUpdateTime(mContext, System.currentTimeMillis());
				break;

		}
		dialog.dismiss();
	}

}
