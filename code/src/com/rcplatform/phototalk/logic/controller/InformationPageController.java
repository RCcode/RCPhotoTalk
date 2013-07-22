package com.rcplatform.phototalk.logic.controller;

import java.util.List;
import java.util.Map;

import android.widget.ListView;

import com.rcplatform.phototalk.HomeActivity;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.db.PhotoTalkDatabase;

public class InformationPageController {

	private static final InformationPageController mController = new InformationPageController();
	private static HomeActivity mActivity;

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

	public void onNewTread() {
		if (mActivity != null)
			mActivity.onNewTrends();
	}

	public void onFriendAlreadyAdded(Information information) {
		if (mActivity != null)
			mActivity.onFriendAlreadyAdded(information);
	}

	public void onNewRecommendsShowed() {
		if (mActivity != null)
			mActivity.onNewRecommendsShowed();
	}

	public void onNewRecommends() {
		if (mActivity != null)
			mActivity.onNewRecommends();
	}

	public void destroy() {
		mActivity = null;
	}

	public ListView getInformationList() {
		if (mActivity != null)
			return mActivity.getInformationList();
		return null;
	}

	public void onDriftThrowed() {
		if (mActivity != null)
			mActivity.onDriftThrowed();
	}
}
