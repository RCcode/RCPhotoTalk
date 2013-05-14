package com.rcplatform.phototalk;

import java.util.Set;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;

import com.rcplatform.phototalk.activity.AddFriendBaseActivity;
import com.rcplatform.phototalk.adapter.PhotoTalkFriendsAdapter;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.proxy.FriendsProxy;
import com.rcplatform.phototalk.request.JSONConver;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.utils.ContactQuery.OnContactsQueryCompleteListener;
import com.rcplatform.phototalk.utils.ContactUtil;

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
			String msg = String.format(getResources().getString(R.string.my_firend_invite_send_short_msg), "mark.", android.os.Build.VERSION.RELEASE, "http://www.menue.com/photochat/", "123458755");
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

		recommendFriends = FriendsProxy.getContactRecommendFriendsAsync(this, new RCPlatformResponseHandler() {
			@Override
			public void onSuccess(int statusCode, String content) {
				dismissLoadingDialog();
				try {
					JSONObject jObj = new JSONObject(content);
					recommendFriends =JSONConver.jsonToFriends(jObj.getJSONArray("userList").toString());
				} catch (Exception e) {
					e.printStackTrace();
				}
				ContactUtil.getContactsAsync(ContactFriendRecommendActivity.this, new OnContactsQueryCompleteListener() {

					@Override
					public void onContacksQueryComplete(Set<com.rcplatform.phototalk.bean.Contacts> contacts) {
						inviteFriends = ContactUtil.getContactFriendNotRepeat(contacts, recommendFriends);
						setListData(recommendFriends, inviteFriends, mList);
					}
				});
			}

			@Override
			public void onFailure(int errorCode, String content) {
				dismissLoadingDialog();
				showErrorConfirmDialog(content);
			}
		});
		if (recommendFriends == null) {
			showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		}
	}
}
