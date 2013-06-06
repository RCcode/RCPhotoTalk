package com.rcplatform.videotalk;

import java.util.List;
import java.util.Set;

import android.content.Intent;
import android.os.Bundle;

import com.rcplatform.videotalk.R;
import com.rcplatform.videotalk.activity.AddFriendBaseActivity;
import com.rcplatform.videotalk.adapter.PhotoTalkFriendsAdapter;
import com.rcplatform.videotalk.bean.Friend;
import com.rcplatform.videotalk.bean.FriendType;
import com.rcplatform.videotalk.proxy.FriendsProxy;
import com.rcplatform.videotalk.request.inf.LoadFriendsListener;
import com.rcplatform.videotalk.umeng.EventUtil;

public class ContactFriendRecommendActivity extends AddFriendBaseActivity {

	@Override
	protected void onInviteButtonClick(Set<Friend> willInvateFriends) {
		super.onInviteButtonClick(willInvateFriends);
		invateFriendsBySms(willInvateFriends);
	}

	private void invateFriendsBySms(Set<Friend> willInvateFriends) {
		if (willInvateFriends != null && willInvateFriends.size() > 0) {
			EventUtil.Register_Login_Invite.rcpt_success_smsinvite(baseContext);
			StringBuilder mobiles = new StringBuilder();
			for (Friend f : willInvateFriends) {
				EventUtil.Register_Login_Invite.rcpt_smsinvite(baseContext);
				mobiles.append(f.getCellPhone()).append(";");
			}
			System.out.println(mobiles.toString());
			String msg = String.format(getResources().getString(R.string.invite_message), getCurrentUser().getRcId());
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.putExtra("address", mobiles.toString().substring(0, mobiles.length() - 1));
			intent.putExtra("sms_body", msg);
			intent.setType("vnd.android-dir/mms-sms");
			startActivity(intent);
		}
		clearInviteFriends();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_friend_source_activity);
		initAddFriendsView();
		setItemType(PhotoTalkFriendsAdapter.TYPE_CONTACTS);
		getContactRecommends();
	}

	private void getContactRecommends() {
		FriendsProxy.getRecommends(this, FriendType.CONTACT, new LoadFriendsListener() {

			@Override
			public void onLoadedFail(String reason) {

			}

			@Override
			public void onFriendsLoaded(List<Friend> friends, List<Friend> recommends) {
				recommendFriends = recommends;
				inviteFriends = friends;
				setListData(recommendFriends, inviteFriends, mList);
			}
		});
		// com.rcplatform.videotalk.Request.Request.executeGetRecommends(this,
		// FriendType.CONTACT, new OnFriendsLoadedListener() {
		//
		// @Override
		// public void onServiceFriendsLoaded(List<Friend> friends, List<Friend>
		// recommends) {
		// if (mList.getExpandableListAdapter() != null &&
		// mList.getExpandableListAdapter().getGroupCount() > 0)
		// return;
		// recommendFriends = recommends;
		// inviteFriends = friends;
		// setListData(recommendFriends, inviteFriends, mList);
		// }
		//
		// @Override
		// public void onLocalFriendsLoaded(List<Friend> friends, List<Friend>
		// recommends) {
		// recommendFriends = recommends;
		// inviteFriends = friends;
		// setListData(recommendFriends, inviteFriends, mList);
		// }
		//
		// @Override
		// public void onError(int errorCode, String content) {
		//
		// }
		// });
	}
}
