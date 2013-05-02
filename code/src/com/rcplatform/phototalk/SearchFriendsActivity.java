package com.rcplatform.phototalk;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.api.JSONConver;
import com.rcplatform.phototalk.api.RCPlatformResponseHandler;
import com.rcplatform.phototalk.bean.DetailFriend;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.FriendSourse;
import com.rcplatform.phototalk.bean.FriendType;
import com.rcplatform.phototalk.image.downloader.ImageOptionsFactory;
import com.rcplatform.phototalk.image.downloader.RCPlatformImageLoader;
import com.rcplatform.phototalk.proxy.FriendsProxy;
import com.rcplatform.phototalk.task.AddFriendTask;
import com.rcplatform.phototalk.utils.AppSelfInfo;
import com.rcplatform.phototalk.views.PVPopupWindow;

/**
 * 好友搜索页面
 * <p>
 * Copyright: Menue,Inc Copyright (c) 2013-3-11 上午10:18:23
 * <p>
 * Team:Menue Beijing
 * <p>
 * 
 * @author jelly.xiong@menue.com.cn
 * @version 1.0.0
 */
public class SearchFriendsActivity extends BaseActivity implements View.OnClickListener {

	private EditText mEditText;

	private ImageView mSearchButton;

	private ListView mListView;

	private View mShowView;

	private ImageLoader mImageLoader;

	private SearchFriendsAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_friend_search_list_activity);
		mImageLoader = ImageLoader.getInstance();
		initView();
	}

	private void initView() {
		// TODO Auto-generated method stub
		mEditText = (EditText) findViewById(R.id.search_et);
		mSearchButton = (ImageView) findViewById(R.id.search_btn);
		mSearchButton.setOnClickListener(this);
		mListView = (ListView) findViewById(R.id.search_result_list);
		mAdapter = new SearchFriendsAdapter();
		mListView.setAdapter(mAdapter);

	}

	protected void showDetail(DetailFriend detailFriend2) {
		PVPopupWindow.show(this, mShowView, detailFriend2, null);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.search_btn:
			String keyword = mEditText.getText().toString().toLowerCase();
			if (TextUtils.isEmpty(keyword))
				return;
			searchFriends(keyword);
			break;

		}
	}

	private void searchFriends(String keyword) {
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		FriendsProxy.searchFriendsAsync(this, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				// TODO Auto-generated method stub
				try {
					JSONObject jsonObject = new JSONObject(content);
					List<Friend> resultFriends = JSONConver.jsonToFriends(jsonObject.getJSONArray("userList").toString());
					mAdapter.setData(resultFriends);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				dismissLoadingDialog();
			}

			@Override
			public void onFailure(int errorCode, String content) {
				// TODO Auto-generated method stub
				dismissLoadingDialog();
				showErrorConfirmDialog(content);
			}
		}, keyword);
	}

	private void doFriendAdd(final Friend friend) {
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		new AddFriendTask(this, getPhotoTalkApplication().getCurrentUser(), new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				// TODO Auto-generated method stub
				dismissLoadingDialog();
				friend.setStatus(Friend.USER_STATUS_FRIEND_ADDED);
				mAdapter.notifyDataSetChanged();
				AddFriendsActivity.addFriend(friend);
			}

			@Override
			public void onFailure(int errorCode, String content) {
				dismissLoadingDialog();
				showErrorConfirmDialog(content);
			}
		}, friend).execute();
	}

	class SearchFriendsAdapter extends BaseAdapter {
		private List<Friend> mFriends = new ArrayList<Friend>();

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.add_friend_list_item, null);
			}
			Friend friend = mFriends.get(position);
			ImageView portraitImage = (ImageView) convertView.findViewById(R.id.add_friend_list_item_portrait);
			TextView nickTextView = (TextView) convertView.findViewById(R.id.add_friend_list_item_name);
			final Button addFriendBtn = (Button) convertView.findViewById(R.id.add_friend_button);
			View sourceView = convertView.findViewById(R.id.add_friend_list_item_source);
			FriendSourse source = friend.getSource();
			if (source == null) {
				sourceView.setVisibility(View.GONE);
			} else {
				sourceView.setVisibility(View.VISIBLE);
				TextView tvFrom = (TextView) sourceView.findViewById(R.id.add_friend_list_item_source_from);
				TextView tvName = (TextView) sourceView.findViewById(R.id.add_friend_list_item_source_name);
				switch (source.getAttrType()) {
				case FriendType.CONTACT:
					tvFrom.setText(R.string.contact_friend);
					break;
				case FriendType.FACEBOOK:
					tvFrom.setText(R.string.facebook_friend);
					break;
				}
				tvName.setText(source.getName());
			}

			RCPlatformImageLoader.loadImage(SearchFriendsActivity.this, mImageLoader, ImageOptionsFactory.getPublishImageOptions(), friend.getHeadUrl(), AppSelfInfo.ImageScaleInfo.thumbnailImageWidthPx, portraitImage, R.drawable.default_head);
			// view friend detail.
			portraitImage.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
				}
			});

			nickTextView.setText(friend.getNick());
			if (friend.getStatus() == Friend.USER_STATUS_NOT_FRIEND) {
				addFriendBtn.setClickable(true);
				addFriendBtn.setTag(friend);
				addFriendBtn.setOnClickListener(mAddFriendButtonClickListener);
			} else {
				addFriendBtn.setEnabled(false);
			}
			return convertView;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mFriends.size();
		}

		public void setData(List<Friend> result) {
			mFriends.clear();
			mFriends.addAll(result);
			notifyDataSetChanged();
		}
	};

	private OnClickListener mAddFriendButtonClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Friend friend = (Friend) v.getTag();
			doFriendAdd(friend);
		}
	};
}
