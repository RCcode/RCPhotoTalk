package com.rcplatform.phototalk;

import java.util.List;
import java.util.Set;

import android.content.Intent;
import android.os.Bundle;

import com.rcplatform.phototalk.activity.AddFriendBaseActivity;
import com.rcplatform.phototalk.adapter.PhotoTalkFriendsAdapter;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.FriendType;
import com.rcplatform.phototalk.request.inf.OnFriendsLoadedListener;

public class ContactFriendRecommendActivity extends AddFriendBaseActivity {
	@Override
	protected void onInviteButtonClick(Set<Friend> willInvateFriends) {
		super.onInviteButtonClick(willInvateFriends);
		invateFriendsBySms(willInvateFriends);
	}

	private void invateFriendsBySms(Set<Friend> willInvateFriends) {
		if (willInvateFriends != null && willInvateFriends.size() > 0) {
			StringBuilder mobiles = new StringBuilder();
			for (Friend f : willInvateFriends) {
				mobiles.append(f.getCellPhone()).append(";");
			}
			System.out.println(mobiles.toString());
			String msg = String.format(getResources().getString(R.string.my_firend_invite_send_short_msg), "mark.", android.os.Build.VERSION.RELEASE,
					"http://www.menue.com/photochat/", "123458755");
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.putExtra("address", mobiles.toString().substring(0, mobiles.length() - 1));
			intent.putExtra("sms_body", msg);
			intent.setType("vnd.android-dir/mms-sms");
			startActivity(intent);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setItemType(PhotoTalkFriendsAdapter.TYPE_CONTACTS);
		getContactRecommends();
	}

	private void getContactRecommends() {

		com.rcplatform.phototalk.request.Request.executeGetRecommends(this, FriendType.CONTACT, new OnFriendsLoadedListener() {

			@Override
			public void onServiceFriendsLoaded(List<Friend> friends, List<Friend> recommends) {
				recommendFriends = recommends;
				inviteFriends = friends;
				setListData(recommendFriends, inviteFriends, mList);
			}

			@Override
			public void onLocalFriendsLoaded(List<Friend> friends, List<Friend> recommends) {
				recommendFriends = recommends;
				inviteFriends = friends;
				setListData(recommendFriends, inviteFriends, mList);
			}

			@Override
			public void onError(int errorCode, String content) {

			}
		});
	}
}
