package com.rcplatform.phototalk.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.AddFriendsActivity;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.adapter.PhotoTalkFriendsAdapter;
import com.rcplatform.phototalk.adapter.PhotoTalkFriendsAdapter.OnCheckBoxChangedListener;
import com.rcplatform.phototalk.adapter.PhotoTalkFriendsAdapter.OnFriendAddListener;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.logic.LogicUtils;
import com.rcplatform.phototalk.task.AddFriendTask;
import com.rcplatform.phototalk.task.AddFriendTask.AddFriendListener;

//github.com/RCcode/RCPhotoTalk.git

public class AddFriendBaseActivity extends BaseActivity {
	protected ExpandableListView mList;
	protected ExpandableListView mSearchList;
	protected List<Friend> recommendFriends;
	protected List<Friend> inviteFriends;

	private Set<Friend> willInvateFriends = new HashSet<Friend>();
	private Map<String, TextView> willInvateViews = new HashMap<String, TextView>();

	private EditText mEtSearch;
	private LinearLayout mLinearInvate;
	private int mItemType;

	protected void setItemType(int itemType) {
		mItemType = itemType;
	}

	protected void initAddFriendsView() {
		mEtSearch = (EditText) findViewById(R.id.et_search);
		mEtSearch.addTextChangedListener(mSearchTextChangeListener);
		initInvateView();
		mList = (ExpandableListView) findViewById(R.id.my_friend_listview);
		mSearchList = (ExpandableListView) findViewById(R.id.lv_search);
		mList.setOnGroupClickListener(mOnGroupClickListener);
		mSearchList.setOnGroupClickListener(mOnGroupClickListener);
	}

	private OnGroupClickListener mOnGroupClickListener = new OnGroupClickListener() {

		@Override
		public boolean onGroupClick(ExpandableListView parent, View v,
				int groupPosition, long id) {
			return true;
		}
	};
	private TextWatcher mSearchTextChangeListener = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			String searchText = s.toString().trim().toLowerCase();
			if (!TextUtils.isEmpty(searchText)) {
				searchFriends(searchText);
				mList.setVisibility(View.GONE);
				mSearchList.setVisibility(View.VISIBLE);
			} else {
				mSearchList.setVisibility(View.GONE);
				mList.setVisibility(View.VISIBLE);
			}
		}
	};

	private void searchFriends(String keyWord) {
		List<Friend> resultFriends = new ArrayList<Friend>();
		for (Friend friend : recommendFriends) {
			if (friend.getSource().getName().toLowerCase().contains(keyWord)) {
				resultFriends.add(friend);
			}
		}
		List<Friend> resultInvite = new ArrayList<Friend>();
		for (Friend friend : inviteFriends) {
			if (friend.getNickName().toLowerCase().contains(keyWord)) {
				resultInvite.add(friend);
			}
		}
		setListData(resultFriends, resultInvite, mSearchList);
	}

	protected void setListData(List<Friend> friends,
			List<Friend> invateFriends, ExpandableListView listView) {
		Map<Integer, List<Friend>> baseFriendsList = new HashMap<Integer, List<Friend>>();
		if (friends != null && friends.size() > 0) {
			baseFriendsList.put(PhotoTalkFriendsAdapter.TYPE_RECOMMENDS,
					friends);
		}
		if (invateFriends != null && invateFriends.size() > 0) {
			baseFriendsList.put(mItemType, invateFriends);
		}
		PhotoTalkFriendsAdapter adapter = null;
		if (listView.getExpandableListAdapter() != null) {
			adapter = (PhotoTalkFriendsAdapter) listView
					.getExpandableListAdapter();
			adapter.setListData(baseFriendsList);
			// listView.setSelection(0);

		} else {
			adapter = new PhotoTalkFriendsAdapter(AddFriendBaseActivity.this,
					baseFriendsList, willInvateFriends,
					ImageLoader.getInstance());
			adapter.setOnCheckBoxChangedListener(mInvateCheckBoxChangeListener);
			adapter.setOnFriendAddListener(mFriendAddListener);
			listView.setAdapter(adapter);
		}
		for (int i = 0; i < baseFriendsList.size(); i++) {
			listView.expandGroup(i);
		}
	}

	private OnFriendAddListener mFriendAddListener = new OnFriendAddListener() {

		@Override
		public void addFriend(Friend friend, Handler h) {
			doFriendAdd(friend);
		}
	};
	private OnCheckBoxChangedListener mInvateCheckBoxChangeListener = new OnCheckBoxChangedListener() {

		@Override
		public void onChange(Friend friend, boolean isChecked) {
			if (isChecked)
				addInvateFriend(friend);
			else
				removeInvateFriend(friend);
		}
	};

	private void doFriendAdd(final Friend friend) {
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		new AddFriendTask(this, getPhotoTalkApplication().getCurrentUser(),
				new AddFriendListener() {
					@Override
					public void onFriendAddSuccess(Friend friend, int addType) {
						dismissLoadingDialog();
						friend.setFriend(true);
						refreshList();
						AddFriendsActivity.addFriend(friend);
					}

					@Override
					public void onFriendAddFail(int statusCode, String content) {
						dismissLoadingDialog();
						showErrorConfirmDialog(content);
					}
				}, friend).execute();
	}

	protected void refreshList() {
		if (mList.getExpandableListAdapter() != null) {
			((BaseExpandableListAdapter) mList.getExpandableListAdapter())
					.notifyDataSetChanged();
		}
		if (mSearchList.getExpandableListAdapter() != null) {
			((BaseExpandableListAdapter) mSearchList.getExpandableListAdapter())
					.notifyDataSetChanged();
		}
	}

	private void initInvateView() {
		final View view = findViewById(R.id.wish_to_invate);
		final HorizontalScrollView hsv = (HorizontalScrollView) view
				.findViewById(R.id.hs_wish_list);
		mLinearInvate = (LinearLayout) view.findViewById(R.id.linear_wish);
		if (mLinearInvate.getChildCount() == 0) {
			view.setVisibility(View.INVISIBLE);
		}
		mLinearInvate.getViewTreeObserver().addOnGlobalLayoutListener(
				new OnGlobalLayoutListener() {

					@Override
					public void onGlobalLayout() {
						hsv.smoothScrollTo(mLinearInvate.getWidth(), 0);
						if (mLinearInvate.getChildCount() == 0) {
							view.setVisibility(View.INVISIBLE);
						}else{
							view.setVisibility(View.VISIBLE);
						}
					}
				});
		Button btnInvate = (Button) findViewById(R.id.btn_invate);
		btnInvate.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onInviteButtonClick(willInvateFriends);
			}
		});
	}

	protected void onInviteButtonClick(Set<Friend> willInvateFriends) {

	}

	/**
	 * 传入参数是通讯录信息伪装成的好友对象，phone是手机号，nick是名字
	 * 
	 * @param friend
	 */
	private void addInvateFriend(Friend friend) {
		if (!willInvateFriends.contains(friend)) {
			willInvateFriends.add(friend);
			TextView tvName = buildInvateTextView(friend);
			tvName.setTextColor(Color.WHITE);
			willInvateViews.put(friend.getRcId(), tvName);
			mLinearInvate.addView(tvName);
			refreshList();
		}
	}

	/**
	 * 传入参数是通讯录信息伪装成的好友对象，phone是手机号，nick是名字
	 * 
	 * @param friend
	 */
	private void removeInvateFriend(Friend friend) {
		willInvateFriends.remove(friend);
		TextView tvName = willInvateViews.get(friend.getRcId());
		if (tvName != null) {
			willInvateViews.remove(friend.getNickName());
			mLinearInvate.removeView(tvName);
		}
		refreshList();
	}

	private TextView buildInvateTextView(final Friend friend) {
		TextView tvName = new TextView(this);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		int marginLeft = getResources().getDimensionPixelSize(
				R.dimen.invate_friend_space);
		params.setMargins(marginLeft, 0, 0, 0);
		tvName.setLayoutParams(params);
		tvName.setText(friend.getNickName());
		tvName.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				removeInvateFriend(friend);
			}
		});
		return tvName;
	}

}
