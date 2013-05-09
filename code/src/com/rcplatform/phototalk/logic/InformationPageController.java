package com.rcplatform.phototalk.logic;

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

	public void clearInformations(){
		if (mActivity != null)
			mActivity.clearInformation();
	}
	
	public void friendAdded(Friend friend){
		if (mActivity != null)
			mActivity.onFriendAdded(friend);
	}
	public void destroy() {
		mActivity = null;
	}
}
