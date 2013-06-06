package com.rcplatform.videotalk;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.rcplatform.videotalk.R;
import com.rcplatform.videotalk.activity.AddFriendBaseActivity;
import com.rcplatform.videotalk.adapter.PhotoTalkFriendsAdapter;
import com.rcplatform.videotalk.bean.Friend;
import com.rcplatform.videotalk.bean.FriendType;
import com.rcplatform.videotalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.videotalk.logic.LogicUtils;
import com.rcplatform.videotalk.proxy.FriendsProxy;
import com.rcplatform.videotalk.request.RCPlatformResponseHandler;
import com.rcplatform.videotalk.request.inf.LoadFriendsListener;
import com.rcplatform.videotalk.task.ThirdPartInfoUploadTask;
import com.rcplatform.videotalk.thirdpart.bean.ThirdPartUser;
import com.rcplatform.videotalk.thirdpart.utils.FacebookClient;
import com.rcplatform.videotalk.thirdpart.utils.OnAuthorizeSuccessListener;
import com.rcplatform.videotalk.thirdpart.utils.OnGetThirdPartInfoSuccessListener;
import com.rcplatform.videotalk.thirdpart.utils.ThirdPartUtils;
import com.rcplatform.videotalk.thirdpart.utils.FacebookClient.OnInviteSuccessListener;
import com.rcplatform.videotalk.umeng.EventUtil;
import com.rcplatform.videotalk.utils.PrefsUtils;
import com.rcplatform.videotalk.utils.Constants.Action;

public class FacebookFriendRecommendActivity extends AddFriendBaseActivity {
	private FacebookClient mFacebookClient;
	private boolean hasTryLogin = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.facebook_recommends);
		findViewById(R.id.wish_to_invate).setVisibility(View.GONE);
		mFacebookClient = new FacebookClient(this);
		mFacebookClient.onCreate(savedInstanceState);
		initAddFriendsView();
		setItemType(PhotoTalkFriendsAdapter.TYPE_FACEBOOK);
		if (mFacebookClient.isAuthorize())
			getRecommentFriends();
	}

	@Override
	protected void onResume() {
		super.onResume();
		mFacebookClient.onResume();
		if (!mFacebookClient.isAuthorize() && !hasTryLogin)
			authorize();
	}

	@Override
	protected void onPause() {
		super.onPause();
		hasTryLogin = false;
		mFacebookClient.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mFacebookClient.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mFacebookClient.onActivityResult(requestCode, resultCode, data);
		hasTryLogin = true;
	}

	private void authorize() {
		EventUtil.Register_Login_Invite.rcpt_facebooklink(baseContext);
		mFacebookClient.authorize(new OnAuthorizeSuccessListener() {

			@Override
			public void onAuthorizeSuccess() {
				getFacebookInfos();
			}
		});
	}

	private void getFacebookInfos() {
		mFacebookClient.getFacebookInfo(new OnGetThirdPartInfoSuccessListener() {

			@Override
			public void onGetFail() {
			}

			@Override
			public void onGetInfoSuccess(ThirdPartUser user, List<ThirdPartUser> friends) {
				onFacebookInfoLoaded(user, friends);
			}
		});
	}

	private void asyncInviteInfo(String... ids) {
		LogicUtils.uploadFriendInvite(this, Action.ACTION_UPLOAD_INTITE_THIRDPART, FriendType.FACEBOOK, ids);
	}

	private void getRecommentFriends() {
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		FriendsProxy.getRecommends(this, FriendType.FACEBOOK, new LoadFriendsListener() {

			@Override
			public void onLoadedFail(String reason) {
				dismissLoadingDialog();
			}

			@Override
			public void onFriendsLoaded(List<Friend> friends, List<Friend> recommends) {
				dismissLoadingDialog();
				recommendsLoaded(friends, recommends);
			}
		});

		// Request.executeGetRecommends(this, FriendType.FACEBOOK, new
		// OnFriendsLoadedListener() {
		//
		// @Override
		// public void onServiceFriendsLoaded(List<Friend> friends, List<Friend>
		// recommends) {
		// if (mList.getExpandableListAdapter() != null &&
		// mList.getExpandableListAdapter().getGroupCount() > 0)
		// return;
		// dismissLoadingDialog();
		// recommendsLoaded(friends, recommends);
		// }
		//
		// @Override
		// public void onLocalFriendsLoaded(List<Friend> friends, List<Friend>
		// recommends) {
		// dismissLoadingDialog();
		// recommendsLoaded(friends, recommends);
		// }
		//
		// @Override
		// public void onError(int errorCode, String content) {
		// dismissLoadingDialog();
		// }
		// });
	}

	private void recommendsLoaded(List<Friend> inviteFriends, List<Friend> recommends) {
		recommendFriends = recommends;
		this.inviteFriends = inviteFriends;
		setListData(recommendFriends, inviteFriends, mList);
	}

	protected void onFacebookInfoLoaded(ThirdPartUser user, final List<ThirdPartUser> friends) {
		PrefsUtils.User.ThirdPart.setFacebookUserName(this, getCurrentUser().getRcId(), user.getNick());
		PrefsUtils.User.ThirdPart.refreshFacebookAsyncTime(getApplicationContext(), getCurrentUser().getRcId());
		PhotoTalkDatabaseFactory.getDatabase().saveThirdPartFriends(friends, FriendType.FACEBOOK);
		ThirdPartInfoUploadTask task = new ThirdPartInfoUploadTask(this, friends, user, FriendType.FACEBOOK, new RCPlatformResponseHandler() {
			@Override
			public void onSuccess(int statusCode, String content) {
				getRecommentFriends();
			}

			@Override
			public void onFailure(int errorCode, String content) {
				dismissLoadingDialog();
				recommendsLoaded(ThirdPartUtils.parserToFriends(friends, FriendType.FACEBOOK), new ArrayList<Friend>());
			}
		});
		task.start();
	}

	protected void onGetFacebookInfoError() {
		dismissLoadingDialog();
	}

	protected void onFacebookFriendItemClick(Friend f) {
		EventUtil.Register_Login_Invite.rcpt_facebookinvite(baseContext);
		super.onFacebookFriendItemClick(f);
		mFacebookClient.sendInviteMessageToUser(f.getRcId(), new OnInviteSuccessListener() {

			@Override
			public void onInviteSuccess(String uid) {
				asyncInviteInfo(uid);
			}
		});
	}
}
