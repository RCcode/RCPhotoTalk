package com.rcplatform.phototalk.logic.controller;

import com.rcplatform.phototalk.SettingsActivity;

public class SettingPageController {
	private SettingsActivity mActivity;
	private static final SettingPageController instance = new SettingPageController();

	public static SettingPageController getInstance() {
		return instance;
	}

	public void setActivity(SettingsActivity activity) {
		this.mActivity = activity;
	}

	public void onNewTrends(boolean show, String headUrl) {
		if (mActivity != null)
			mActivity.onNewTrend(show, headUrl);
	}

	public void destroy() {
		mActivity = null;
	}
}
