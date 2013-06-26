package com.rcplatform.phototalk.activity;

import android.app.Activity;
import android.app.TabActivity;
import android.content.Context;
import android.os.Bundle;

import com.rcplatform.phototalk.PhotoTalkApplication;
import com.rcplatform.phototalk.bean.UserInfo;

@SuppressWarnings("deprecation")
public class BaseTabActivity extends TabActivity implements ActivityFunction {
	private ActivityFunction functionImpl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		functionImpl = new ActivityFunctionBasic(this);
	}

	@Override
	public Context getContext() {
		return functionImpl.getContext();
	}

	@Override
	public void showLoadingDialog(boolean cancelAble) {
		functionImpl.showLoadingDialog(cancelAble);
	}

	@Override
	public void dissmissLoadingDialog() {
		functionImpl.dissmissLoadingDialog();
	}

	@Override
	public UserInfo getCurrentUser() {
		return functionImpl.getCurrentUser();
	}

	@Override
	public PhotoTalkApplication getPhotoTalkApplication() {
		return functionImpl.getPhotoTalkApplication();
	}

	@Override
	public Activity getActivity() {
		return functionImpl.getActivity();
	}

	@Override
	public void showConfirmDialog(String msg) {
		functionImpl.showConfirmDialog(msg);
	}

	@Override
	public void showConfirmDialog(int resId) {
		functionImpl.showConfirmDialog(resId);
	}

}
