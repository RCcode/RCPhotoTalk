package com.rcplatform.videotalk;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;

import com.rcplatform.videotalk.R;
import com.rcplatform.videotalk.activity.AddFriendBaseActivity;
import com.rcplatform.videotalk.adapter.PhotoTalkFriendsAdapter;
import com.rcplatform.videotalk.bean.Friend;
import com.rcplatform.videotalk.bean.FriendType;
import com.rcplatform.videotalk.logic.LogicUtils;
import com.rcplatform.videotalk.proxy.FriendsProxy;
import com.rcplatform.videotalk.request.RCPlatformResponseHandler;
import com.rcplatform.videotalk.request.Request;
import com.rcplatform.videotalk.request.inf.LoadFriendsListener;
import com.rcplatform.videotalk.request.inf.OnFriendsLoadedListener;
import com.rcplatform.videotalk.task.ThirdPartInfoUploadTask;
import com.rcplatform.videotalk.thirdpart.bean.ThirdPartUser;
import com.rcplatform.videotalk.thirdpart.utils.OnAuthorizeSuccessListener;
import com.rcplatform.videotalk.thirdpart.utils.OnGetThirdPartInfoSuccessListener;
import com.rcplatform.videotalk.thirdpart.utils.ThirdPartUtils;
import com.rcplatform.videotalk.thirdpart.utils.VKClient;
import com.rcplatform.videotalk.umeng.EventUtil;
import com.rcplatform.videotalk.utils.PrefsUtils;
import com.rcplatform.videotalk.utils.Constants.Action;

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
			EventUtil.Register_Login_Invite.rcpt_vklinklink(baseContext);
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
				PrefsUtils.User.ThirdPart.setVKName(VKRecommendActivity.this, getCurrentUser().getRcId(), user.getNick());
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
		FriendsProxy.getRecommends(this, FriendType.VK, new LoadFriendsListener() {
			
			@Override
			public void onLoadedFail(String reason) {
				dismissLoadingDialog();
			}
			
			@Override
			public void onFriendsLoaded(List<Friend> friends, List<Friend> recommends) {
				dismissLoadingDialog();
				onRecommendsLoaded(recommends, friends);
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
			EventUtil.Register_Login_Invite.rcpt_success_vkinvite(baseContext);
			List<String> vkIds = new ArrayList<String>();
			for (Friend f : willInvateFriends) {
				EventUtil.Register_Login_Invite.rcpt_vkinvite(baseContext);
				vkIds.add(f.getRcId());
			}
			mVkClient.sendInviteMessage(vkIds);
			showErrorConfirmDialog(R.string.invite_success);
			uploadInviteInfo(vkIds);
			clearInviteFriends();
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