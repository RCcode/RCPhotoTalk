package com.rcplatform.phototalk.adapter;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.rcplatform.phototalk.BaseFriendAdapter;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.bean.Friend;

/**
 * 标题、简要说明. <br>
 * 类详细说明.
 * <p>
 * Copyright: Menue,Inc Copyright (c) 2013-3-21 下午05:06:05
 * <p>
 * Team:Menue Beijing
 * <p>
 * 
 * @author jelly.xiong@menue.com.cn
 * @version 1.0.0
 */
@Deprecated
public class InviteFriendAdapter extends BaseFriendAdapter<Friend> {

	private OnFriendInviteListener mFriendInviteListener;

	private HashMap<String, Friend> selectedFriend;

	private boolean isShowNumber;

	public InviteFriendAdapter(Context mContext, List<Friend> mFriendList) {
		super(mContext, mFriendList);
	}

	@Override
	public View newView(Context context, List friendLists, ViewGroup parent) {
		return LayoutInflater.from(context).inflate(R.layout.invite_friend_list_item, parent, false);
	}

	@Override
	public void bindView(final Context context, View view, List friendLists, int position) {
		final Friend friend = (Friend) friendLists.get(position);

		TextView selecedView = (TextView) view.findViewById(R.id.diaplay_chat_name);
		selecedView.setText(friend.getNick());
		TextView numberView = (TextView) view.findViewById(R.id.display_number);
		numberView.setText(friend.getPhone());

		CheckBox checkBox = (CheckBox) view.findViewById(R.id.add_friend_invite_checkbox);

		checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				System.out.println("===onCheckedChanged=====isChecked=====" + isChecked);
			}
		});
		checkBox.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				CheckBox cb = (CheckBox) v;
				if (cb.isChecked()) {
					if (mFriendInviteListener != null) {
						selectedFriend = mFriendInviteListener.invite(friend);
					}
				} else {
					if (mFriendInviteListener != null) {
						mFriendInviteListener.refuse(friend);
					}
				}
			}
		});

		Button sendMessageBtn = (Button) view.findViewById(R.id.add_friend_invite_single_btn);
		if (isShowNumber) {
			numberView.setVisibility(View.VISIBLE);
			sendMessageBtn.setVisibility(View.GONE);
		} else {
			numberView.setVisibility(View.GONE);
			sendMessageBtn.setVisibility(View.VISIBLE);
			sendMessageBtn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					//
					String msg = String.format(context.getResources().getString(R.string.my_firend_invite_send_short_msg), "mark.",
					                           android.os.Build.VERSION.RELEASE, "http://www.menue.com/photochat/", "123458755");

					Intent intent = new Intent(Intent.ACTION_VIEW);
					intent.putExtra("address", friend.getPhone());
					intent.putExtra("sms_body", msg);
					intent.setType("vnd.android-dir/mms-sms");
					// startActivity(intent);
				}
			});
		}

	}

	public interface OnFriendInviteListener {

		HashMap<String, Friend> invite(Friend friend);

		void refuse(Friend friend);

	}

	public void setShowNumber(boolean isShowNumber) {
		this.isShowNumber = isShowNumber;
	}

	public void setOnFriendInviteListener(OnFriendInviteListener onFriendInviteListener) {
		this.mFriendInviteListener = onFriendInviteListener;

	}
}
