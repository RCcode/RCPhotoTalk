package com.rcplatform.phototalk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.os.Bundle;
import android.view.View;

import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.rcplatform.phototalk.activity.FacebookAddFriendsActivity;
import com.rcplatform.phototalk.adapter.PhotoTalkFriendsAdapter;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.FriendType;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.logic.LogicUtils;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.request.Request;
import com.rcplatform.phototalk.request.inf.OnFriendsLoadedListener;
import com.rcplatform.phototalk.task.ThirdPartInfoUploadTask;
import com.rcplatform.phototalk.thirdpart.bean.ThirdPartUser;
import com.rcplatform.phototalk.thirdpart.utils.ThirdPartUtils;
import com.rcplatform.phototalk.utils.Constants.Action;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.PrefsUtils;

public class FacebookFriendRecommendActivity extends FacebookAddFriendsActivity {
	private LoginButton mLoginButton;
	private View loginLayout;
	private View friendsLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.facebook_recommends);
		initAddFriendsView();
		mLoginButton = (LoginButton) findViewById(R.id.login_button);
		mLoginButton.setPublishPermissions(Arrays.asList("publish_actions"));
		loginLayout = findViewById(R.id.login_layout);
		friendsLayout = findViewById(R.id.friends_layout);
		setItemType(PhotoTalkFriendsAdapter.TYPE_FACEBOOK);
		if (ThirdPartUtils.isFacebookVlidate(this))
			getRecommentFriends();
		else
			mLoginButton.setUserInfoChangedCallback(this);

	}

	private void setShowLogin() {
		loginLayout.setVisibility(View.VISIBLE);
		friendsLayout.setVisibility(View.GONE);

	}

	@Override
	protected void onResume() {
		super.onResume();
		updateUI();
	}

	private void setShowRecommends() {
		loginLayout.setVisibility(View.GONE);
		friendsLayout.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onInviteButtonClick(Set<Friend> willInvateFriends) {
		super.onInviteButtonClick(willInvateFriends);
		if (willInvateFriends != null && willInvateFriends.size() > 0) {
			String[] ids = new String[willInvateFriends.size()];
			Iterator<Friend> itFriends = willInvateFriends.iterator();
			int i = 0;
			while (itFriends.hasNext()) {
				Friend friend = itFriends.next();
				ids[i] = friend.getRcId();
				i++;
			}
			sendInviteMessage(ids);
		}
	}

	@Override
	protected void onInviteComplete(String... ids) {
		super.onInviteComplete(ids);
		DialogUtil.createMsgDialog(this, R.string.invite_success, R.string.confirm).show();
		asyncInviteInfo(ids);
	}

	private void asyncInviteInfo(String... ids) {
		LogicUtils.uploadFriendInvite(this, Action.ACTION_UPLOAD_INTITE_THIRDPART, FriendType.FACEBOOK, ids);
	}

	private void getRecommentFriends() {
		Request.executeGetRecommends(this, FriendType.FACEBOOK, new OnFriendsLoadedListener() {

			@Override
			public void onServiceFriendsLoaded(List<Friend> friends, List<Friend> recommends) {
				if (mList.getExpandableListAdapter() != null && mList.getExpandableListAdapter().getGroupCount() > 0)
					return;
				dismissLoadingDialog();
				recommendsLoaded(friends, recommends);
			}

			@Override
			public void onLocalFriendsLoaded(List<Friend> friends, List<Friend> recommends) {
				dismissLoadingDialog();
				recommendsLoaded(friends, recommends);
			}

			@Override
			public void onError(int errorCode, String content) {
				dismissLoadingDialog();
			}
		});
	}

	private void recommendsLoaded(List<Friend> inviteFriends, List<Friend> recommends) {
		recommendFriends = recommends;
		this.inviteFriends = inviteFriends;
		setListData(recommendFriends, inviteFriends, mList);
	}

	@Override
	protected void onFacebookInfoLoaded(GraphUser user, final List<ThirdPartUser> friends) {
		super.onFacebookInfoLoaded(user, friends);
		mLoginButton.setUserInfoChangedCallback(null);
		PrefsUtils.User.ThirdPart.refreshFacebookAsyncTime(getApplicationContext(), getCurrentUser().getRcId());
		PhotoTalkDatabaseFactory.getDatabase().saveThirdPartFriends(friends, FriendType.FACEBOOK);
		ThirdPartInfoUploadTask task = new ThirdPartInfoUploadTask(this, friends, ThirdPartUtils.parserFacebookUserToThirdPartUser(user), FriendType.FACEBOOK,
				new RCPlatformResponseHandler() {
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

	@Override
	protected void onGetFacebookInfoError() {
		super.onGetFacebookInfoError();
		dismissLoadingDialog();
	}

	private void updateUI() {
		if (isSessionOpened())
			setShowRecommends();
		else
			setShowLogin();
	}

	private boolean isSessionOpened() {
		Session session = Session.getActiveSession();
		return (session != null && session.isOpened());
	}
}
