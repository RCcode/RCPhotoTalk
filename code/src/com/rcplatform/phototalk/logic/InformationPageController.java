package com.rcplatform.phototalk.logic;

import java.util.List;
import java.util.Map;

import com.rcplatform.phototalk.HomeActivity;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.Information;

public class InformationPageController {
	private static final InformationPageController mController = new InformationPageController();
	private HomeActivity mActivity;

	public void setupController(HomeActivity informationPager) {
		mActivity = informationPager;
	}

	public static synchronized InformationPageController getInstance() {
		return mController;
	}

	public void photoInformationShowEnd(Information information) {
		if (mActivity != null)
			mActivity.onInformationShowEnd(information);
	}

	public void clearInformations() {
		if (mActivity != null)
			mActivity.clearInformation();
	}

	public void friendAdded(Friend friend) {
		if (mActivity != null)
			mActivity.onFriendAdded(friend);
	}

	public void sendPhotos(List<Information> informations) {
		if (mActivity != null&&informations.size()>0)
			mActivity.onPhotoSending(informations);
	}

	public void photosSendSuccess(Map<String,Information> informations, long flag) {
		if (mActivity != null&&informations.size()>0)
			mActivity.onPhotoSendSuccess(informations, flag);
	}

	public void destroy() {
		mActivity = null;
	}
}
