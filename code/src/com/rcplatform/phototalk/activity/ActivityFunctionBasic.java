package com.rcplatform.phototalk.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;

import com.rcplatform.phototalk.PhotoTalkApplication;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.PrefsUtils;

public class ActivityFunctionBasic implements ActivityFunction {
	private Activity context;
	private ProgressDialog mProgressDialog;

	public ActivityFunctionBasic(Activity context) {
		this.context = context;
	}

	@Override
	public Context getContext() {
		return context;
	}

	@Override
	public void showLoadingDialog(boolean cancelAble) {
		if (mProgressDialog == null) {
			if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.GINGERBREAD_MR1)
				mProgressDialog = new ProgressDialog(context, R.style.Theme_Dialog_Update);
			else
				mProgressDialog = new ProgressDialog(context);
		}
		mProgressDialog.setCancelable(cancelAble);
		mProgressDialog.setTitle(null);
		mProgressDialog.setMessage(null);
		if (!mProgressDialog.isShowing() && !context.isFinishing()) {
			try {
				mProgressDialog.show();
				mProgressDialog.setContentView(R.layout.operation_loading);
			} catch (Exception e) {

			}

		}
	}

	@Override
	public void dissmissLoadingDialog() {
		if (mProgressDialog != null && mProgressDialog.isShowing())
			mProgressDialog.dismiss();
	}

	@Override
	public UserInfo getCurrentUser() {
		UserInfo userInfo = getPhotoTalkApplication().getCurrentUser();
		if (userInfo == null)
			userInfo = PrefsUtils.LoginState.getLoginUser(context);
		return userInfo;
	}

	@Override
	public PhotoTalkApplication getPhotoTalkApplication() {
		return (PhotoTalkApplication) context.getApplicationContext();
	}

	@Override
	public Activity getActivity() {
		return context;
	}

	@Override
	public void showConfirmDialog(String msg) {
		DialogUtil.createErrorInfoDialog(context, msg).show();
	}

	@Override
	public void showConfirmDialog(int resId) {
		DialogUtil.createErrorInfoDialog(context, resId).show();
	}

}
