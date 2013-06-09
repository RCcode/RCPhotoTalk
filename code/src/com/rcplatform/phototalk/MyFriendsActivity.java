package com.rcplatform.phototalk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.activity.MenuBaseActivity;
import com.rcplatform.phototalk.adapter.PhotoTalkFriendsAdapter;
import com.rcplatform.phototalk.adapter.PhotoTalkFriendsAdapter.OnFriendAddListener;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.logic.controller.InformationPageController;
import com.rcplatform.phototalk.proxy.FriendsProxy;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.request.inf.FriendDetailListener;
import com.rcplatform.phototalk.request.inf.LoadFriendsListener;
import com.rcplatform.phototalk.task.AddFriendTask;
import com.rcplatform.phototalk.umeng.EventUtil;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.PrefsUtils;
import com.rcplatform.phototalk.utils.Utils;

public class MyFriendsActivity extends MenuBaseActivity implements OnClickListener {
	private static final int MSG_WHAT_FRIEND_LOADED = 912;

	private static final int REQUEST_KEY_DETAIL = 110;
	private static final int REQUEST_KEY_ADD_FRIEND = 111;

	private ExpandableListView mList;
	private ExpandableListView mSearchList;
	private EditText etSearch;
	private List<Friend> mFriends;
	private List<Friend> mRecommends;
	private ImageLoader mImageLoader;
	private Friend mFriendShowDetail;
	private Button seach_delete_btn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_friends);
		PrefsUtils.User.setNewRecommends(this, getCurrentUser().getRcId(), false);
		InformationPageController.getInstance().onNewRecommendsShowed();
		initView();
		mImageLoader = ImageLoader.getInstance();
		getFriends();
	}

	private void getFriends() {
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		FriendsProxy.getFriendsAndRecommends(this, new LoadFriendsListener() {

			@Override
			public void onLoadedFail(String reason) {
				dismissLoadingDialog();
				if (!PrefsUtils.User.hasLoadedFriends(MyFriendsActivity.this, getCurrentUser().getRcId())) {
					showErrorConfirmDialog(reason);
				}
			}

			@Override
			public void onFriendsLoaded(List<Friend> friends, List<Friend> recommends) {
				dismissLoadingDialog();
				sendFriendLoadedMessage(friends, recommends);
			}
		});
	}

	private void sendFriendLoadedMessage(List<Friend> friends, List<Friend> recommends) {
		mFriends = friends;
		mRecommends = recommends;
		mHandler.sendEmptyMessage(MSG_WHAT_FRIEND_LOADED);
	}

	private static final int ITEM_ID = 101;
	private OnCreateContextMenuListener mCreateContextMenuListener = new OnCreateContextMenuListener() {

		@Override
		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
			ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
			int type = ExpandableListView.getPackedPositionType(info.packedPosition);
			int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);
			int child = ExpandableListView.getPackedPositionChild(info.packedPosition);
			if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
				final Friend friend = (Friend) ((PhotoTalkFriendsAdapter) mList.getExpandableListAdapter()).getChild(group, child);
				if (getCurrentUser().getRcId().equals(friend.getRcId()) || Constants.OFFICIAL_RCID.equals(friend.getRcId()))
					return;

				EventUtil.Friends_Addfriends.rcpt_friends_longpress(baseContext);
				menu.setHeaderTitle(R.string.operation);
				menu.add(0, ITEM_ID, 0, getString(R.string.delete)).setOnMenuItemClickListener(new OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem item) {
						EventUtil.Friends_Addfriends.rcpt_friendsdelete(baseContext);
						deleteFriend(friend);
						return false;
					}
				});
			}
		}
	};
	private OnCreateContextMenuListener mSearchListCreateContextMenuListener = new OnCreateContextMenuListener() {

		@Override
		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
			ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
			int type = ExpandableListView.getPackedPositionType(info.packedPosition);
			int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);
			int child = ExpandableListView.getPackedPositionChild(info.packedPosition);
			if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
				final Friend friend = (Friend) ((PhotoTalkFriendsAdapter) mSearchList.getExpandableListAdapter()).getChild(group, child);
				if (getCurrentUser().getRcId().equals(friend.getRcId()) || Constants.OFFICIAL_RCID.equals(friend.getRcId()))
					return;

				EventUtil.Friends_Addfriends.rcpt_friends_longpress(baseContext);
				menu.setHeaderTitle(R.string.operation);
				menu.add(0, ITEM_ID, 0, getString(R.string.delete)).setOnMenuItemClickListener(new OnMenuItemClickListener() {

					@Override
					public boolean onMenuItemClick(MenuItem item) {
						EventUtil.Friends_Addfriends.rcpt_friendsdelete(baseContext);
						deleteFriend(friend);
						return false;
					}
				});
			}
		}
	};

	private void initView() {
		initBackButton(R.string.my_firend_title, this);
		initForwordButton(R.drawable.title_add_friend_btn, this);
		mList = (ExpandableListView) findViewById(R.id.elv_friends);
		mSearchList = (ExpandableListView) findViewById(R.id.elv_search);
		mList.setOnGroupClickListener(mGroupClickListener);
		mSearchList.setOnGroupClickListener(mGroupClickListener);
		mList.setOnChildClickListener(mChildClickListener);
		mSearchList.setOnChildClickListener(mChildClickListener);
		mList.setOnCreateContextMenuListener(mCreateContextMenuListener);
		mSearchList.setOnCreateContextMenuListener(mSearchListCreateContextMenuListener);
		etSearch = (EditText) findViewById(R.id.et_search);
		seach_delete_btn = (Button) findViewById(R.id.seach_delete_btn);
		seach_delete_btn.setVisibility(View.GONE);
		seach_delete_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				etSearch.setText("");
				etSearch.setFocusable(true);
				seach_delete_btn.setVisibility(View.GONE);
			}
		});
		etSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				String keyWords = s.toString().trim();
				if (TextUtils.isEmpty(keyWords)) {
					mSearchList.setVisibility(View.GONE);
					mList.setVisibility(View.VISIBLE);
					seach_delete_btn.setVisibility(View.GONE);
				} else {
					search(keyWords);
					mList.setVisibility(View.GONE);
					seach_delete_btn.setVisibility(View.VISIBLE);
					mSearchList.setVisibility(View.VISIBLE);
				}
			}
		});
	}

	private void search(String keyWords) {
		if (keyWords != null) {
			keyWords = keyWords.toLowerCase();
		}
		List<Friend> resultRecommends = new ArrayList<Friend>();
		for (Friend friend : mRecommends) {
			if (friend.getNickName() != null && friend.getNickName().toLowerCase().contains(keyWords)) {
				resultRecommends.add(friend);
			}
		}
		List<Friend> resultFriends = new ArrayList<Friend>();
		for (Friend friend : mFriends) {
			if (friend.getNickName() != null && friend.getNickName().toLowerCase().contains(keyWords)) {
				resultFriends.add(friend);
			}
		}
		setListData(resultFriends, resultRecommends, mSearchList);
	}

	private OnGroupClickListener mGroupClickListener = new OnGroupClickListener() {

		@Override
		public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
			long groupId = parent.getExpandableListAdapter().getGroupId(groupPosition);
			if (groupId == PhotoTalkFriendsAdapter.TYPE_RECOMMENDS || groupId == PhotoTalkFriendsAdapter.TYPE_THIRD_RECOMMENDS) {
				return false;
			}
			return true;
		}
	};
	private OnChildClickListener mChildClickListener = new OnChildClickListener() {

		@Override
		public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
			EventUtil.Friends_Addfriends.rcpt_friends_profileview(baseContext);
			showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
			Friend friend = (Friend) ((PhotoTalkFriendsAdapter) parent.getExpandableListAdapter()).getChild(groupPosition, childPosition);
			com.rcplatform.phototalk.request.Request.executeGetFriendDetailAsync(MyFriendsActivity.this, friend, new FriendDetailListener() {

				@Override
				public void onSuccess(Friend friend) {
					dismissLoadingDialog();
					showDetail(friend);
				}

				@Override
				public void onError(int errorCode, String content) {
					dismissLoadingDialog();
					showErrorConfirmDialog(content);
				}
			}, false);
			return false;
		}
	};

	private void showDetail(Friend friend) {
		mFriendShowDetail = friend;
		Intent intent = new Intent(this, FriendDetailActivity.class);
		intent.putExtra(FriendDetailActivity.PARAM_FRIEND, friend);
		if (!friend.isFriend()) {
			intent.setAction(Constants.Action.ACTION_RECOMMEND_DETAIL);
		} else {
			intent.setAction(Constants.Action.ACTION_FRIEND_DETAIL);
		}
		startActivityForResult(intent, REQUEST_KEY_DETAIL);
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_WHAT_FRIEND_LOADED:
				dismissLoadingDialog();
				setListData(mFriends, mRecommends, mList);
				etSearch.setText(null);
				break;
			}
		}

	};

	private void setListData(List<Friend> mFriends, List<Friend> mRecommends, ExpandableListView list) {
		Map<Integer, List<Friend>> listData = new HashMap<Integer, List<Friend>>();
		if (mFriends != null && mFriends.size() > 0) {
			List<Friend> newFriends = new ArrayList<Friend>();
			for (Friend friend : mFriends) {
				if (friend.isNew()) {
					newFriends.add(friend);
				}
			}
			if (newFriends.size() > 0) {
				listData.put(PhotoTalkFriendsAdapter.TYPE_FRIEND_NEW, newFriends);
			}
			listData.put(PhotoTalkFriendsAdapter.TYPE_FRIEND_ADDED, mFriends);
		}
		if (mRecommends != null && mRecommends.size() > 0) {
			listData.put(PhotoTalkFriendsAdapter.TYPE_RECOMMENDS, mRecommends);
		}
		if (list.getExpandableListAdapter() != null) {
			PhotoTalkFriendsAdapter adapter = (PhotoTalkFriendsAdapter) list.getExpandableListAdapter();
			adapter.setListData(listData);
		} else {
			PhotoTalkFriendsAdapter adapter = new PhotoTalkFriendsAdapter(this, listData, new HashSet<Friend>(), mImageLoader);
			adapter.setOnFriendAddListener(mFriendAddListener);
			list.setAdapter(adapter);
		}
		for (int i = 0; i < listData.size(); i++) {
			list.expandGroup(i);
		}
	};

	private OnFriendAddListener mFriendAddListener = new OnFriendAddListener() {

		@Override
		public void addFriend(Friend friend, Handler h) {
			doAddFriend(friend);
		}

	};

	private void doAddFriend(final Friend friend) {
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		new AddFriendTask(this, getPhotoTalkApplication().getCurrentUser(), new AddFriendTask.AddFriendListener() {

			@Override
			public void onFriendAddSuccess(Friend f, int addType) {
				friend.setFriend(true);
				refreshList();
				dismissLoadingDialog();
			}

			@Override
			public void onFriendAddFail(int statusCode, String content) {
				dismissLoadingDialog();
				showErrorConfirmDialog(content);
			}

			@Override
			public void onAlreadyAdded() {
				// TODO Auto-generated method stub

			}
		}, friend).execute();
	}

	private void refreshList() {
		if (mList.getExpandableListAdapter() != null) {
			((BaseExpandableListAdapter) mList.getExpandableListAdapter()).notifyDataSetChanged();
		}
		if (mSearchList.getExpandableListAdapter() != null) {
			((BaseExpandableListAdapter) mSearchList.getExpandableListAdapter()).notifyDataSetChanged();
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.choosebutton:
			EventUtil.Friends_Addfriends.rcpt_addfriends(baseContext);
			Intent intent = new Intent(MyFriendsActivity.this, AddFriendsActivity.class);
			intent.setData(Uri.parse("data"));
			startActivityForResult(intent, REQUEST_KEY_ADD_FRIEND);
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == REQUEST_KEY_DETAIL) {
				Friend result = (Friend) data.getSerializableExtra(FriendDetailActivity.RESULT_PARAM_FRIEND);
				mFriendShowDetail.setFriend(true);
				mFriendShowDetail.setLocalName(result.getLocalName());
				refreshList();
			} else if (requestCode == REQUEST_KEY_ADD_FRIEND) {
				List<Friend> newFriends = (List<Friend>) data.getSerializableExtra(AddFriendsActivity.RESULT_PARAM_KEY_NEW_ADD_FRIENDS);
				handlerAddResult(newFriends);
			}

		}
	}

	private void handlerAddResult(final List<Friend> newFriends) {
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		Thread thread = new Thread() {
			@Override
			public void run() {
				mFriends.addAll(newFriends);
				List<Friend> friendsDelete = new ArrayList<Friend>();
				for (Friend friendNew : newFriends) {
					for (Friend recommend : mRecommends) {
						if (friendNew.getRcId().equals(recommend.getRcId())) {
							friendsDelete.add(recommend);
						}
					}
				}
				mRecommends.removeAll(friendsDelete);
				mFriends = Utils.getFriendOrderByLetter(mFriends);
				mHandler.sendEmptyMessage(MSG_WHAT_FRIEND_LOADED);
			}
		};
		thread.start();
	}

	private void handlerDeleteResult(final Friend friend) {
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		Thread thread = new Thread() {
			@Override
			public void run() {
				mFriends.remove(friend);
				mRecommends.remove(friend);
				PhotoTalkDatabaseFactory.getDatabase().deleteFriend(friend);
				mFriends = Utils.getFriendOrderByLetter(mFriends);
				mHandler.sendEmptyMessage(MSG_WHAT_FRIEND_LOADED);
			}
		};
		thread.start();
	}

	private void deleteFriend(final Friend friend) {
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		RCPlatformResponseHandler handler = new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				handlerDeleteResult(friend);
			}

			@Override
			public void onFailure(int errorCode, String content) {
				dismissLoadingDialog();
				showErrorConfirmDialog(content);
			}
		};
		if (friend.isFriend()) {
			FriendsProxy.deleteFriend(this, handler, friend.getRcId());
		} else {
			FriendsProxy.deleteRecommendFriend(this, handler, friend);
		}

	}
}
