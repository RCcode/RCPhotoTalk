package com.rcplatform.phototalk;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.FriendSourse;
import com.rcplatform.phototalk.bean.FriendType;
import com.rcplatform.phototalk.image.downloader.ImageOptionsFactory;
import com.rcplatform.phototalk.image.downloader.RCPlatformImageLoader;
import com.rcplatform.phototalk.proxy.FriendsProxy;
import com.rcplatform.phototalk.request.JSONConver;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.task.AddFriendTask;
import com.rcplatform.phototalk.utils.AppSelfInfo;

public class SearchFriendsActivity extends BaseActivity implements View.OnClickListener {

	private EditText mEditText;

	private ImageView mSearchButton;

	private ListView mListView;

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
		mEditText = (EditText) findViewById(R.id.search_et);
		mEditText.setOnKeyListener(new OnKeyListener() {
			private long lastPressTime = 0l;

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					if (System.currentTimeMillis() - lastPressTime > 1000) {
						lastPressTime = System.currentTimeMillis();
						search();
					}
				}
				return false;
			}
		});
		mSearchButton = (ImageView) findViewById(R.id.search_btn);
		mSearchButton.setOnClickListener(this);
		mListView = (ListView) findViewById(R.id.search_result_list);
		mAdapter = new SearchFriendsAdapter();
		mListView.setAdapter(mAdapter);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.search_btn:
			search();
			break;
		}
	}

	private void search() {
		String keyword = mEditText.getText().toString().toLowerCase();
		if (TextUtils.isEmpty(keyword))
			return;
		searchFriends(keyword);
	}

	private void searchFriends(String keyword) {
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		FriendsProxy.searchFriendsAsync(this, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				try {
					JSONObject jsonObject = new JSONObject(content);
					List<Friend> resultFriends = JSONConver.jsonToFriends(jsonObject.getJSONArray("userList").toString());
					mAdapter.setData(resultFriends);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				dismissLoadingDialog();
			}

			@Override
			public void onFailure(int errorCode, String content) {
				dismissLoadingDialog();
				showErrorConfirmDialog(content);
			}
		}, keyword);
	}

	private void doFriendAdd(final Friend friend) {
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		new AddFriendTask(this, getPhotoTalkApplication().getCurrentUser(), new AddFriendTask.AddFriendListener() {

			@Override
			public void onFriendAddSuccess(Friend f,int addType) {
				dismissLoadingDialog();
				friend.setFriend(true);
				mAdapter.notifyDataSetChanged();
				AddFriendsActivity.addFriend(friend);
			}

			@Override
			public void onFriendAddFail(int statusCode, String content) {
				dismissLoadingDialog();
				showErrorConfirmDialog(content);
			}
		}, friend).execute();
	}

	class SearchFriendsAdapter extends BaseAdapter {
		private List<Friend> mFriends = new ArrayList<Friend>();

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
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
				TextView tvFrom = (TextView) convertView.findViewById(R.id.add_friend_list_item_source_from);
				TextView tvName = (TextView) convertView.findViewById(R.id.add_friend_list_item_source_name);
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

			RCPlatformImageLoader.loadImage(SearchFriendsActivity.this, mImageLoader, ImageOptionsFactory.getPublishImageOptions(), friend.getHeadUrl(),
					AppSelfInfo.ImageScaleInfo.thumbnailImageWidthPx, portraitImage, R.drawable.default_head);
			// view friend detail.
			portraitImage.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
				}
			});

			nickTextView.setText(friend.getNickName());
			if (!friend.isFriend()) {
				addFriendBtn.setEnabled(true);
				addFriendBtn.setTag(friend);
				addFriendBtn.setOnClickListener(mAddFriendButtonClickListener);
			} else {
				addFriendBtn.setEnabled(false);
			}
			return convertView;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public int getCount() {
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
			Friend friend = (Friend) v.getTag();
			doFriendAdd(friend);
		}
	};
}
