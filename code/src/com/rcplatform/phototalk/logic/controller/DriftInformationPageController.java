package com.rcplatform.phototalk.logic.controller;

import java.util.List;

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
	
	public void onDriftInformationSending(List<DriftInformation> informations){
		if(mActivity!=null)
			mActivity.onPhotoSending(informations);
	}
}
