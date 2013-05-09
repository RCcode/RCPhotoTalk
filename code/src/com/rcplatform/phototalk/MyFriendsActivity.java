package com.rcplatform.phototalk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
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
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.adapter.PhotoTalkFriendsAdapter;
import com.rcplatform.phototalk.adapter.PhotoTalkFriendsAdapter.OnFriendAddListener;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.logic.LogicUtils;
import com.rcplatform.phototalk.proxy.FriendsProxy;
import com.rcplatform.phototalk.request.JSONConver;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.task.AddFriendTask;
import com.rcplatform.phototalk.utils.Contract;
import com.rcplatform.phototalk.utils.Utils;

public class MyFriendsActivity extends BaseActivity implements OnClickListener {
	private static final int MSG_WHAT_SORT_COMPLETE = 911;
	private static final int REQUEST_KEY_DETAIL = 110;
	private static final int REQUEST_KEY_ADD_FRIEND = 111;

	private ExpandableListView mList;
	private ExpandableListView mSearchList;
	private EditText etSearch;
	private List<Friend> mFriends;
	private List<Friend> mRecommends;
	private ImageLoader mImageLoader;
	private Friend mFriendShowDetail;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_friends);
		initView();
		mImageLoader = ImageLoader.getInstance();
		getFriends();
	}

	private void getFriends() {
		List<Friend>[] friends = FriendsProxy.getMyFriend(this, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				try {
					JSONObject jObj = new JSONObject(content);
					mFriends = JSONConver.jsonToFriends(jObj.getJSONArray("myUsers").toString());
					mRecommends = JSONConver.jsonToFriends(jObj.getJSONArray("recommendUsers").toString());
					sortFriends();
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					dismissLoadingDialog();
					showErrorConfirmDialog(R.string.net_error);
				}

			}

			@Override
			public void onFailure(int errorCode, String content) {
				dismissLoadingDialog();
				showErrorConfirmDialog(content);
			}
		});
		if (friends != null) {
			mFriends = friends[1];
			mRecommends = friends[0];
		} else {
			showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		}

	}

	private static final int ITEM_ID = 101;
@Override
protected void initForwordButton(int resId, OnClickListener onClickListener) {
	// TODO Auto-generated method stub
//	super.initForwordButton(resId, onClickListener);
	TextView tvForward=(TextView) findViewById(R.id.choosebutton);
//	tvForward.setText(resId);
	tvForward.setBackgroundResource(R.drawable.title_add_friend_btn);
	tvForward.setOnClickListener(onClickListener);
	tvForward.setVisibility(View.VISIBLE);
}
	private void initView() {
		initBackButton(R.string.my_firend_title, this);
		initForwordButton(R.string.add_friend_title, this);
		mList = (ExpandableListView) findViewById(R.id.elv_friends);
		mSearchList = (ExpandableListView) findViewById(R.id.elv_search);
		mList.setOnGroupClickListener(mGroupClickListener);
		mSearchList.setOnGroupClickListener(mGroupClickListener);
		mList.setOnChildClickListener(mChildClickListener);
		mSearchList.setOnChildClickListener(mChildClickListener);
		mList.setOnCreateContextMenuListener(new OnCreateContextMenuListener() {

			@Override
			public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
				ExpandableListView.ExpandableListContextMenuInfo info = (ExpandableListView.ExpandableListContextMenuInfo) menuInfo;
				int type = ExpandableListView.getPackedPositionType(info.packedPosition);
				final int group = ExpandableListView.getPackedPositionGroup(info.packedPosition);
				final int child = ExpandableListView.getPackedPositionChild(info.packedPosition);
				if (type == ExpandableListView.PACKED_POSITION_TYPE_CHILD) {
					menu.setHeaderTitle(R.string.delete_friend);
					menu.add(0, ITEM_ID, 0, getString(R.string.delete)).setOnMenuItemClickListener(new OnMenuItemClickListener() {

						@Override
						public boolean onMenuItemClick(MenuItem item) {
							// TODO Auto-generated method stub
							Friend friend = (Friend) ((PhotoTalkFriendsAdapter) mList.getExpandableListAdapter()).getChild(group, child);
							deleteFriend(friend);
							return false;
						}
					});
				}
			}
		});
		etSearch = (EditText) findViewById(R.id.et_search);

		etSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				String keyWords = s.toString().trim();
				if (TextUtils.isEmpty(keyWords)) {
					mSearchList.setVisibility(View.GONE);
					mList.setVisibility(View.VISIBLE);
				} else {
					search(keyWords);
					mList.setVisibility(View.GONE);
					mSearchList.setVisibility(View.VISIBLE);
				}
			}
		});
	}

	private void search(String keyWords) {
		List<Friend> resultRecommends = new ArrayList<Friend>();
		for (Friend friend : mRecommends) {
			if (friend.getNick().contains(keyWords)) {
				resultRecommends.add(friend);
			}
		}
		List<Friend> resultFriends = new ArrayList<Friend>();
		for (Friend friend : mFriends) {
			if (friend.getNick().contains(keyWords)) {
				resultFriends.add(friend);
			}
		}
		setListData(resultFriends, resultRecommends, mSearchList);
	}

	private OnGroupClickListener mGroupClickListener = new OnGroupClickListener() {

		@Override
		public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
			long groupId = parent.getExpandableListAdapter().getGroupId(groupPosition);
			if (groupId == PhotoTalkFriendsAdapter.TYPE_RECOMMENDS) {
				return false;
			}
			return true;
		}
	};
	private OnChildClickListener mChildClickListener = new OnChildClickListener() {

		@Override
		public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
			// TODO Auto-generated method stub
			Friend friend = (Friend) ((PhotoTalkFriendsAdapter) parent.getExpandableListAdapter()).getChild(groupPosition, childPosition);
			showDetail(friend);
			return false;
		}
	};

	private void showDetail(Friend friend) {
		mFriendShowDetail = friend;
		Intent intent = new Intent(this, FriendDetailActivity.class);
		intent.putExtra(FriendDetailActivity.PARAM_FRIEND, friend);
		if (friend.getStatus() == Friend.USER_STATUS_NOT_FRIEND) {
			intent.setAction(Contract.Action.ACTION_RECOMMEND_DETAIL);
		} else {
			intent.setAction(Contract.Action.ACTION_FRIEND_DETAIL);
		}
		startActivityForResult(intent, REQUEST_KEY_DETAIL);
	}

	private void sortFriends() {
		Thread thread = new Thread() {
			public void run() {
				mFriends = Utils.getFriendOrderByLetter(mFriends);
				mHandler.sendEmptyMessage(MSG_WHAT_SORT_COMPLETE);
			};
		};
		thread.start();
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			dismissLoadingDialog();
			setListData(mFriends, mRecommends, mList);
			etSearch.setText(null);
		}

	};

	private void setListData(List<Friend> mFriends, List<Friend> mRecommends, ExpandableListView list) {
		Map<Integer, List<Friend>> listData = new HashMap<Integer, List<Friend>>();
		if (mFriends != null && mFriends.size() > 0) {
			List<Friend> newFriends = new ArrayList<Friend>();
			for (Friend friend : mFriends) {
				friend.setStatus(Friend.USER_STATUS_FRIEND_ADDED);
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
			// TODO Auto-generated method stub
			doAddFriend(friend);
		}

	};

	private void doAddFriend(final Friend friend) {
		// TODO Auto-generated method stub
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		new AddFriendTask(this, getPhotoTalkApplication().getCurrentUser(), new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				// TODO Auto-generated method stub
				friend.setStatus(Friend.USER_STATUS_FRIEND_ADDED);
				refreshList();
				LogicUtils.friendAdded(friend);
				dismissLoadingDialog();
			}

			@Override
			public void onFailure(int errorCode, String content) {
				// TODO Auto-generated method stub
				dismissLoadingDialog();
				showErrorConfirmDialog(content);
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
		case R.id.title_linear_back:
			finish();
			break;
		case R.id.choosebutton:
			Intent intent = new Intent(MyFriendsActivity.this, AddFriendsActivity.class);
			startActivityForResult(intent, REQUEST_KEY_ADD_FRIEND);
			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == REQUEST_KEY_DETAIL) {
				Friend result = (Friend) data.getSerializableExtra(FriendDetailActivity.RESULT_PARAM_FRIEND);
				mFriendShowDetail.setStatus(result.getStatus());
				mFriendShowDetail.setMark(result.getMark());
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
						if (friendNew.getSuid().equals(recommend.getSuid())) {
							friendsDelete.add(recommend);
						}
					}
				}
				mRecommends.removeAll(friendsDelete);
				mFriends = Utils.getFriendOrderByLetter(mFriends);
				mHandler.sendEmptyMessage(MSG_WHAT_SORT_COMPLETE);
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
				mFriends = Utils.getFriendOrderByLetter(mFriends);
				mHandler.sendEmptyMessage(MSG_WHAT_SORT_COMPLETE);
			}
		};
		thread.start();
	}

	private void deleteFriend(final Friend friend) {
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		RCPlatformResponseHandler handler = new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				// TODO Auto-generated method stub
				handlerDeleteResult(friend);
			}

			@Override
			public void onFailure(int errorCode, String content) {
				// TODO Auto-generated method stub
				dismissLoadingDialog();
				showErrorConfirmDialog(content);
			}
		};
		if (friend.getStatus() == Friend.USER_STATUS_FRIEND_ADDED) {
			FriendsProxy.deleteFriend(this, handler, friend.getSuid());
		} else if (friend.getStatus() == Friend.USER_STATUS_NOT_FRIEND) {
			FriendsProxy.deleteRecommendFriend(this, handler, friend);
		}

	}
}
