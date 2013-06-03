package com.rcplatform.phototalk.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.AddFriendsActivity;
import com.rcplatform.phototalk.FriendDetailActivity;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.adapter.PhotoTalkFriendsAdapter;
import com.rcplatform.phototalk.adapter.PhotoTalkFriendsAdapter.OnCheckBoxChangedListener;
import com.rcplatform.phototalk.adapter.PhotoTalkFriendsAdapter.OnFriendAddListener;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.request.Request;
import com.rcplatform.phototalk.request.inf.FriendDetailListener;
import com.rcplatform.phototalk.task.AddFriendTask;
import com.rcplatform.phototalk.task.AddFriendTask.AddFriendListener;
import com.rcplatform.phototalk.utils.Constants.Action;

//github.com/RCcode/RCPhotoTalk.git

public class AddFriendBaseActivity extends BaseActivity {
	private static final int REQUEST_CODE_RECOMMENDS_DETAIL = 200;

	protected ExpandableListView mList;
	protected ExpandableListView mSearchList;
	protected List<Friend> recommendFriends;
	protected List<Friend> inviteFriends;
private Button seach_delete_btn;
	private Friend mFriendShowDetail;
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
		seach_delete_btn = (Button) findViewById(R.id.seach_delete_btn);
		seach_delete_btn.setVisibility(View.GONE);
		seach_delete_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mEtSearch.setText("");
				mEtSearch.setFocusable(true);
				seach_delete_btn.setVisibility(View.GONE);
			}
		});
		mEtSearch.addTextChangedListener(mSearchTextChangeListener);
		initInvateView();
		mList = (ExpandableListView) findViewById(R.id.my_friend_listview);
		mSearchList = (ExpandableListView) findViewById(R.id.lv_search);
		mList.setOnGroupClickListener(mOnGroupClickListener);
		mSearchList.setOnGroupClickListener(mOnGroupClickListener);
		mList.setOnChildClickListener(mOnChildClickListener);
		mSearchList.setOnChildClickListener(mOnChildClickListener);
	}

	private OnGroupClickListener mOnGroupClickListener = new OnGroupClickListener() {

		@Override
		public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
			return true;
		}
	};
	private TextWatcher mSearchTextChangeListener = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			String searchText = mEtSearch.getText().toString();
//			String searchText = s.toString().trim().toLowerCase();
			if (searchText!=null&&searchText.length()>0) {
				searchFriends(searchText.toLowerCase());
				seach_delete_btn.setVisibility(View.VISIBLE);
				mList.setVisibility(View.GONE);
				mSearchList.setVisibility(View.VISIBLE);
			} else {
				seach_delete_btn.setVisibility(View.GONE);
				mSearchList.setVisibility(View.GONE);
				mList.setVisibility(View.VISIBLE);
			}
		}
	};

	private void searchFriends(String keyWord) {
		if(recommendFriends==null||inviteFriends==null)
			return;
		List<Friend> resultFriends = new ArrayList<Friend>();
		for (Friend friend : recommendFriends) {
			
			if (friend.getSource().getName()!=null&&friend.getSource().getName().toLowerCase().contains(keyWord)) {
				resultFriends.add(friend);
			}
		}
		List<Friend> resultInvite = new ArrayList<Friend>();
		for (Friend friend : inviteFriends) {
			if (friend.getNickName()!=null&&friend.getNickName().toLowerCase().contains(keyWord)) {
				resultInvite.add(friend);
			}
		}
		setListData(resultFriends, resultInvite, mSearchList);
	}

	protected void setListData(List<Friend> friends, List<Friend> invateFriends, ExpandableListView listView) {
		Map<Integer, List<Friend>> baseFriendsList = new HashMap<Integer, List<Friend>>();
		if (friends != null && friends.size() > 0) {
			baseFriendsList.put(PhotoTalkFriendsAdapter.TYPE_RECOMMENDS, friends);
		}
		if (invateFriends != null && invateFriends.size() > 0) {
			baseFriendsList.put(mItemType, invateFriends);
		}
		PhotoTalkFriendsAdapter adapter = null;
		if (listView.getExpandableListAdapter() != null) {
			adapter = (PhotoTalkFriendsAdapter) listView.getExpandableListAdapter();
			adapter.setListData(baseFriendsList);
			// listView.setSelection(0);

		} else {
			adapter = new PhotoTalkFriendsAdapter(AddFriendBaseActivity.this, baseFriendsList, willInvateFriends, ImageLoader.getInstance());
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
		new AddFriendTask(this, getPhotoTalkApplication().getCurrentUser(), new AddFriendListener() {
			@Override
			public void onFriendAddSuccess(Friend f, int addType) {
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
			((BaseExpandableListAdapter) mList.getExpandableListAdapter()).notifyDataSetChanged();
		}
		if (mSearchList.getExpandableListAdapter() != null) {
			((BaseExpandableListAdapter) mSearchList.getExpandableListAdapter()).notifyDataSetChanged();
		}
	}

	private void initInvateView() {
		final View view = findViewById(R.id.wish_to_invate);
		final HorizontalScrollView hsv = (HorizontalScrollView) view.findViewById(R.id.hs_wish_list);
		mLinearInvate = (LinearLayout) view.findViewById(R.id.linear_wish);
		if (mLinearInvate.getChildCount() == 0) {
			view.setVisibility(View.GONE);
		}
		mLinearInvate.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {

			@Override
			public void onGlobalLayout() {
				hsv.smoothScrollTo(mLinearInvate.getWidth(), 0);
				if (mLinearInvate.getChildCount() == 0) {
					view.setVisibility(View.GONE);
				} else {
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

	protected void clearInviteFriends() {
		willInvateFriends.clear();
		mLinearInvate.removeAllViews();
		refreshList();
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
			tvName.setTextSize(16);
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
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		int marginLeft = getResources().getDimensionPixelSize(R.dimen.invate_friend_space);
		params.setMargins(marginLeft, 0, 0, 0);
		tvName.setLayoutParams(params);
		tvName.setText(friend.getNickName()+",");
		tvName.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				removeInvateFriend(friend);
			}
		});
		return tvName;
	}

	private OnChildClickListener mOnChildClickListener = new OnChildClickListener() {

		@Override
		public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
			Friend friend = (Friend) parent.getExpandableListAdapter().getChild(groupPosition, childPosition);
			if (friend.getSource() != null) {
				showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
				mFriendShowDetail = friend;
				Request.executeGetFriendDetailAsync(AddFriendBaseActivity.this, friend, new FriendDetailListener() {

					@Override
					public void onSuccess(Friend friend) {
						dismissLoadingDialog();
						startFriendDetail(friend);
					}

					@Override
					public void onError(int errorCode, String content) {
						dismissLoadingDialog();
						showErrorConfirmDialog(content);
					}
				}, false);
			} else if (mItemType == PhotoTalkFriendsAdapter.TYPE_FACEBOOK) {
				onFacebookFriendItemClick(friend);
			}
			return false;
		}
	};

	protected void onFacebookFriendItemClick(Friend f) {

	}

	private void startFriendDetail(Friend friend) {
		Intent intent = new Intent(this, FriendDetailActivity.class);
		intent.setAction(Action.ACTION_RECOMMEND_DETAIL);
		intent.putExtra(FriendDetailActivity.PARAM_FRIEND, friend);
		startActivityForResult(intent, REQUEST_CODE_RECOMMENDS_DETAIL);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == REQUEST_CODE_RECOMMENDS_DETAIL) {
				Friend friendNew = (Friend) data.getSerializableExtra(FriendDetailActivity.RESULT_PARAM_FRIEND);
				mFriendShowDetail.setFriend(friendNew.isFriend());
				AddFriendsActivity.addFriend(friendNew);
				refreshList();
			}
		}
	}
}
