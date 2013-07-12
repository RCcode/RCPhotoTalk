package com.rcplatform.phototalk.logic.controller;

import java.util.List;

import android.widget.ListView;

import com.rcplatform.phototalk.drift.DriftInformation;
import com.rcplatform.phototalk.drift.DriftInformationActivity;

public class DriftInformationPageController {
	private DriftInformationActivity mActivity;
	private static final DriftInformationPageController instance = new DriftInformationPageController();

	public static DriftInformationPageController getInstance() {
		return instance;
	}

	public void setup(DriftInformationActivity activity) {
		mActivity = activity;
	}

	public void destroy() {
		mActivity = null;
	}

	public void onDriftInformationSending(List<DriftInformation> informations) {
		if (mActivity != null)
			mActivity.onPhotoSending(informations);
	}

	public void onDriftInformationSendSuccess(long flag, int picId) {
		if (mActivity != null)
			mActivity.onPhotoSendSuccess(flag, picId);
	}

	public void onDriftInformationSendFail(long flag) {
		if (mActivity != null)
			mActivity.onPhotoSendFail(flag);
	}

	public ListView getListView() {
		if (mActivity != null)
			return mActivity.getInformationList();
		return null;
	}
	public void onDriftShowEnd(DriftInformation information){
		if(mActivity!=null)
			mActivity.onInformationShowEnd(information);
	}
}
