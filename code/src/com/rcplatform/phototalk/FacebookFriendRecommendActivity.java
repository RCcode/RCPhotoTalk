package com.rcplatform.phototalk;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;

import android.os.Bundle;

import com.facebook.model.GraphUser;
import com.rcplatform.phototalk.activity.FacebookAddFriendsActivity;
import com.rcplatform.phototalk.adapter.PhotoTalkFriendsAdapter;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.FriendType;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.logic.LogicUtils;
import com.rcplatform.phototalk.proxy.FriendsProxy;
import com.rcplatform.phototalk.request.JSONConver;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.request.Request;
import com.rcplatform.phototalk.request.inf.OnFriendsLoadedListener;
import com.rcplatform.phototalk.task.FacebookUploadTask;
import com.rcplatform.phototalk.thirdpart.bean.ThirdPartFriend;
import com.rcplatform.phototalk.thirdpart.utils.ThirdPartUtils;
import com.rcplatform.phototalk.utils.Contract.Action;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.FacebookUtil;

public class FacebookFriendRecommendActivity extends FacebookAddFriendsActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setItemType(PhotoTalkFriendsAdapter.TYPE_FACEBOOK);
		if (FacebookUtil.isFacebookVlidate(this)) {
			getRecommentFriends();
		}

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
		// TODO Auto-generated method stub
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
	protected void onFacebookInfoLoaded(GraphUser user, final List<ThirdPartFriend> friends) {
		super.onFacebookInfoLoaded(user, friends);
		PhotoTalkDatabaseFactory.getDatabase().saveThirdPartFriends(friends);

		FacebookUploadTask task = new FacebookUploadTask(this, friends, user);
		task.setResponseListener(new RCPlatformResponseHandler() {
			@Override
			public void onSuccess(int statusCode, String content) {
				getRecommentFriends();
			}

			@Override
			public void onFailure(int errorCode, String content) {
				dismissLoadingDialog();
				recommendFriends = new ArrayList<Friend>();
				inviteFriends = ThirdPartUtils.parserToFriends(friends, FriendType.FACEBOOK);
				setListData(recommendFriends, inviteFriends, mList);
			}
		});
		task.start();
	}

	@Override
	protected void onGetFacebookInfoError() {
		super.onGetFacebookInfoError();
		dismissLoadingDialog();
	}
}
