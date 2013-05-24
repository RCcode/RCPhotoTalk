package com.rcplatform.phototalk.logic.controller;

import java.util.List;
import java.util.Map;

import android.os.Handler;

import com.rcplatform.phototalk.HomeActivity;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.db.PhotoTalkDatabase;

public class InformationPageController {
	private static final int MSG_WHAT_SHOWEND = 10000;
	private static final int MSG_WHAT_CLEAR = 10001;
	private static final int MSG_WHAT_ADDFRIEND = 10002;
	private static final int MSG_WHAT_SENDPHOTO_START = 10003;
	private static final int MSG_WHAT_SENDPHOTO_SUCCESS = 10004;
	private static final int MSG_WHAT_SENDPHOTO_FAIL = 10005;
	private static final int MSG_WHAT_RESENDPHOTO_SUCCESS = 10006;
	private static final int MSG_WHAT_RESENDPHOTO_FAIL = 10007;

	private static final InformationPageController mController = new InformationPageController();
	private static HomeActivity mActivity;

	public void setupController(HomeActivity informationPager) {
		mActivity = informationPager;
	}

	public static synchronized InformationPageController getInstance() {
		return mController;
	}

	private static final Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_WHAT_ADDFRIEND: {

			}

				break;
			case MSG_WHAT_CLEAR: {

			}
				break;
			case MSG_WHAT_RESENDPHOTO_FAIL: {

			}
				break;
			case MSG_WHAT_RESENDPHOTO_SUCCESS: {

			}
				break;
			case MSG_WHAT_SENDPHOTO_FAIL: {

			}
				break;
			case MSG_WHAT_SENDPHOTO_START: {

			}
				break;
			case MSG_WHAT_SENDPHOTO_SUCCESS: {

			}
				break;
			case MSG_WHAT_SHOWEND: {

			}
				break;
			default:
				break;
			}
		};
	};

	public void photoInformationShowEnd(Information information) {
		if (mActivity != null)
			mActivity.onInformationShowEnd(information);
	}

	public void clearInformations() {
		if (mActivity != null)
			mActivity.clearInformation();
	}

	public void friendAdded(Information info, int addType) {
		if (mActivity != null)
			mActivity.onFriendAdded(info, addType);
	}

	public void sendPhotos(List<Information> informations) {
		if (mActivity != null && informations.size() > 0)
			mActivity.onPhotoSending(informations);
	}

	public void onPhotoSendSuccess(long flag) {
		if (mActivity != null)
			mActivity.onPhotoSendSuccess(flag);
	}

	public void onPhotoSendFail(long flag) {
		if (mActivity != null)
			mActivity.onPhotoSendFail(flag);
	}

	public void onPhotoResendSuccess(Information information) {
		if (mActivity != null)
			mActivity.onPhotoResendSuccess(information);
	}

	public void onPhotoResendFail(Information information) {
		if (mActivity != null)
			mActivity.onPhotoResendFail(information);
	}

	public void onNewInformation(Map<Integer, List<Information>> infos) {
		if (mActivity != null)
			mActivity.onNewInformation(infos.get(PhotoTalkDatabase.UPDATED_INFORMATION), infos.get(PhotoTalkDatabase.NEW_INFORMATION));
	}

	public void destroy() {
		mActivity = null;
	}
}
