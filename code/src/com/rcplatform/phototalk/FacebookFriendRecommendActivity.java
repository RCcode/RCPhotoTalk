package com.rcplatform.phototalk;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;

import com.facebook.model.GraphUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.rcplatform.phototalk.activity.FacebookActivity;
import com.rcplatform.phototalk.adapter.PhotoTalkFriendsAdapter;
import com.rcplatform.phototalk.api.JSONConver;
import com.rcplatform.phototalk.api.RCPlatformResponseHandler;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.FriendType;
import com.rcplatform.phototalk.clienservice.InviteFriendUploadService;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.proxy.FriendsProxy;
import com.rcplatform.phototalk.task.FacebookUploadTask;
import com.rcplatform.phototalk.thirdpart.bean.ThirdPartFriend;
import com.rcplatform.phototalk.thirdpart.utils.ThirdPartUtils;
import com.rcplatform.phototalk.utils.Contract;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.FacebookUtil;

public class FacebookFriendRecommendActivity extends FacebookActivity {

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
				ids[i] = friend.getSuid();
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
		Intent intent = new Intent(this, InviteFriendUploadService.class);
		intent.putExtra(InviteFriendUploadService.PARAM_FRIENDS_IDS, ids);
		intent.putExtra(InviteFriendUploadService.PARAM_TYPE, FriendType.FACEBOOK);
		intent.setAction(Contract.Action.ACTION_UPLOAD_INTITE_THIRDPART);
		startService(intent);
	}

	private void getRecommentFriends() {

		recommendFriends = FriendsProxy.getFacebookRecommendFriendsAsync(this, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				dismissLoadingDialog();
				try {
					JSONObject jsonObject = new JSONObject(content);
					recommendFriends = JSONConver.jsonToFriends(jsonObject.getJSONArray("thirdUsers").toString());
					List<Friend> friends = PhotoTalkDatabaseFactory.getDatabase().getThirdPartFriends(FriendType.FACEBOOK);
					inviteFriends = ThirdPartUtils.getFriendsNotRepeat(friends, recommendFriends);
					setListData(recommendFriends, inviteFriends, mList);
				} catch (Exception e) {
					e.printStackTrace();
					showErrorConfirmDialog(R.string.net_error);
				}
			}

			@Override
			public void onFailure(int errorCode, String content) {
				dismissLoadingDialog();
				showErrorConfirmDialog(content);
			}
		});
		if (recommendFriends == null || recommendFriends.size() == 0) {
			showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		}
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
