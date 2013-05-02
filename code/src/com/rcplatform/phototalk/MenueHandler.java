package com.rcplatform.phototalk;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.rcplatform.phototalk.api.MenueApiFactory;
import com.rcplatform.phototalk.utils.ShowToast;

public class MenueHandler extends Handler {

	private Context mContext;

	@Override
	public void handleMessage(Message msg) {

		switch (msg.what) {
			case MenueApiFactory.LOGIN_PASSWORD_ERROR:
				ShowToast.showToast(mContext, mContext.getResources().getString(R.string.reg_pwd_no_email_yes), Toast.LENGTH_LONG);
				break;
			case MenueApiFactory.LOGIN_SERVER_ERROR:
				ShowToast.showToast(mContext, mContext.getResources().getString(R.string.reg_server_no), Toast.LENGTH_LONG);
				break;
			case MenueApiFactory.LOGIN_ADMIN_ERROR:
				ShowToast.showToast(mContext, mContext.getResources().getString(R.string.reg_admin_no), Toast.LENGTH_LONG);
				break;
			default:
				break;
		}
	}

	public MenueHandler(Context context) {
		super();
		mContext = context;
	}

}
