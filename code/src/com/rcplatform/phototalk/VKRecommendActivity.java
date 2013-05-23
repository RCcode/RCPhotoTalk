package com.rcplatform.phototalk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;

import com.rcplatform.phototalk.activity.AddFriendBaseActivity;
import com.rcplatform.phototalk.adapter.PhotoTalkFriendsAdapter;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.FriendType;
import com.rcplatform.phototalk.logic.LogicUtils;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.request.Request;
import com.rcplatform.phototalk.request.inf.OnFriendsLoadedListener;
import com.rcplatform.phototalk.task.ThirdPartInfoUploadTask;
import com.rcplatform.phototalk.thirdpart.bean.ThirdPartUser;
import com.rcplatform.phototalk.thirdpart.utils.OnAuthorizeSuccessListener;
import com.rcplatform.phototalk.thirdpart.utils.OnGetThirdPartInfoSuccessListener;
import com.rcplatform.phototalk.thirdpart.utils.ThirdPartUtils;
import com.rcplatform.phototalk.thirdpart.utils.VKClient;
import com.rcplatform.phototalk.utils.Constants.Action;

public class VKRecommendActivity extends AddFriendBaseActivity {

	private VKClient mVkClient;
	private boolean hasTryLogin = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vk_recommends);
		setItemType(PhotoTalkFriendsAdapter.TYPE_VK);
		mVkClient = new VKClient(this);
		initView();
	}

	private void initView() {
		initAddFriendsView();
		if (mVkClient.isAuthorize()) {
			getVkRecommends();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		hasTryLogin = false;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		mVkClient.onActivityResult(requestCode, resultCode, data);
		hasTryLogin = true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (!hasTryLogin && !mVkClient.isAuthorize()) {
			authorize();
		}
	}

	private void authorize() {
		mVkClient.authorize(new OnAuthorizeSuccessListener() {

			@Override
			public void onAuthorizeSuccess() {
				getVkInfos();
			}
		});
	}

	private void getVkInfos() {
		mVkClient.getVKInfo(new OnGetThirdPartInfoSuccessListener() {

			@Override
			public void onGetInfoSuccess(ThirdPartUser user, List<ThirdPartUser> friends) {
				uploadVKInfos(user, friends);
			}

			@Override
			public void onGetFail() {

			}
		});
	}

	private void uploadVKInfos(ThirdPartUser vkUser, final List<ThirdPartUser> vkFriends) {
		new ThirdPartInfoUploadTask(VKRecommendActivity.this, vkFriends, vkUser, FriendType.VK, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				getVkRecommends();
			}

			@Override
			public void onFailure(int errorCode, String content) {
				dismissLoadingDialog();
				onRecommendsLoaded(new ArrayList<Friend>(), ThirdPartUtils.parserToFriends(vkFriends, FriendType.VK));
			}
		}).start();
	}

	private void getVkRecommends() {
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		Request.executeGetRecommends(this, FriendType.VK, new OnFriendsLoadedListener() {

			@Override
			public void onServiceFriendsLoaded(List<Friend> friends, List<Friend> recommends) {
				if (mList.getExpandableListAdapter() != null && mList.getExpandableListAdapter().getGroupCount() > 0)
					return;
				dismissLoadingDialog();
				onRecommendsLoaded(recommends, friends);
			}

			@Override
			public void onLocalFriendsLoaded(List<Friend> friends, List<Friend> recommends) {
				if (friends.size() > 0 || recommends.size() > 0) {
					dismissLoadingDialog();
					onRecommendsLoaded(recommends, friends);
				}
			}

			@Override
			public void onError(int errorCode, String content) {
				dismissLoadingDialog();
			}
		});
	}

	private void onRecommendsLoaded(List<Friend> recommends, List<Friend> inviteFriends) {
		this.recommendFriends = recommends;
		this.inviteFriends = inviteFriends;
		setListData(recommendFriends, inviteFriends, mList);
	}

	protected void onInviteButtonClick(final java.util.Set<Friend> willInvateFriends) {
		if (willInvateFriends != null && willInvateFriends.size() > 0) {
			List<String> vkIds = new ArrayList<String>();
			for (Friend f : willInvateFriends) {
				vkIds.add(f.getRcId());
			}
			mVkClient.sendInviteMessage(vkIds);
			showErrorConfirmDialog(R.string.invite_success);
			uploadInviteInfo(vkIds);
		}
	};

	private void uploadInviteInfo(Collection<String> invitedFriendIds) {
		String[] ids = new String[invitedFriendIds.size()];
		Iterator<String> itFs = invitedFriendIds.iterator();
		int i = 0;
		while (itFs.hasNext()) {
			ids[i] = itFs.next();
			i++;
		}
		LogicUtils.uploadFriendInvite(this, Action.ACTION_UPLOAD_INTITE_THIRDPART, FriendType.VK, ids);
	}
}
