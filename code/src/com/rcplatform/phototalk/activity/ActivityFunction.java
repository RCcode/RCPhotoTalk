package com.rcplatform.phototalk.activity;

import android.app.Activity;
import android.content.Context;

import com.rcplatform.phototalk.PhotoTalkApplication;
import com.rcplatform.phototalk.bean.UserInfo;

public interface ActivityFunction {
	public Context getContext();

	public Activity getActivity();

	public void showLoadingDialog(boolean cancelAble);

	public void dissmissLoadingDialog();

	public UserInfo getCurrentUser();

	public PhotoTalkApplication getPhotoTalkApplication();
	
	public void showConfirmDialog(String msg);
	
	public void showConfirmDialog(int resId);
}
