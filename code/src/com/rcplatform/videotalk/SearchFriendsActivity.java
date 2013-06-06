package com.rcplatform.videotalk;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.videotalk.R;
import com.rcplatform.videotalk.activity.BaseActivity;
import com.rcplatform.videotalk.bean.Friend;
import com.rcplatform.videotalk.bean.FriendSourse;
import com.rcplatform.videotalk.bean.FriendType;
import com.rcplatform.videotalk.proxy.FriendsProxy;
import com.rcplatform.videotalk.request.JSONConver;
import com.rcplatform.videotalk.request.RCPlatformResponseHandler;
import com.rcplatform.videotalk.request.Request;
import com.rcplatform.videotalk.request.inf.FriendDetailListener;
import com.rcplatform.videotalk.task.AddFriendTask;
import com.rcplatform.videotalk.umeng.EventUtil;
import com.rcplatform.videotalk.utils.Constants.Action;

public class SearchFriendsActivity extends BaseActivity implements View.OnClickListener {

	private EditText mEditText;

	private ListView mListView;

	private TextView search_hint_text;

	private ImageLoader mImageLoader;

	private Button seach_delete_btn;

	private Friend friendShowDetail;

	private SearchFriendsAdapter mAdapter;

	private static final int REQUEST_CODE_DETAIL = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_friend_search_list_activity);
		mImageLoader = ImageLoader.getInstance();
		initView();
	}

	private void initView() {
		mEditText = (EditText) findViewById(R.id.search_et);
		seach_delete_btn = (Button) findViewById(R.id.seach_delete_btn);
		search_hint_text = (TextView) findViewById(R.id.search_hint_text);
		seach_delete_btn.setVisibility(View.GONE);
		seach_delete_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mEditText.setText("");
				mEditText.setFocusable(true);
				seach_delete_btn.setVisibility(View.GONE);
			}
		});
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
		mEditText.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				if (mEditText.getText() != null && mEditText.getText().length() > 0) {
					seach_delete_btn.setVisibility(View.VISIBLE);
				} else {
					seach_delete_btn.setVisibility(View.GONE);

				}
			}
		});
		// mSearchButton = (ImageView) findViewById(R.id.search_btn);
		// mSearchButton.setOnClickListener(this);
		mListView = (ListView) findViewById(R.id.search_result_list);
		mAdapter = new SearchFriendsAdapter();
		mListView.setAdapter(mAdapter);
		mListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Friend friend = (Friend) mAdapter.getItem(arg2);
				showFriendDetail(friend);
			}
		});

	}

	private void showFriendDetail(Friend friend) {
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		Request.executeGetFriendDetailAsync(this, friend, new FriendDetailListener() {

			@Override
			public void onSuccess(Friend friend) {
				dismissLoadingDialog();
				startFriendDetailActivity(friend);
			}

			@Override
			public void onError(int errorCode, String content) {
				dismissLoadingDialog();
				showErrorConfirmDialog(content);
			}
		}, false);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.back:
				finish();
				break;
		// case R.id.search_btn:
		// search();
		// break;
		}
	}

	private void search() {
		String keyword = mEditText.getText().toString().toLowerCase();
		if (TextUtils.isEmpty(keyword))
			return;
		searchFriends(keyword);
	}

	private void searchFriends(String keyword) {
		EventUtil.Register_Login_Invite.rcpt_success_search(baseContext);
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		FriendsProxy.searchFriendsAsync(this, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				try {
					JSONObject jsonObject = new JSONObject(content);
					List<Friend> resultFriends = JSONConver.jsonToFriends(jsonObject.getJSONArray("userList").toString());
					mAdapter.setData(resultFriends);
					if (mAdapter.getCount() == 0) {
						search_hint_text.setVisibility(View.VISIBLE);
					} else {
						search_hint_text.setVisibility(View.GONE);
					}
				}
				catch (JSONException e) {
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
			public void onFriendAddSuccess(Friend f, int addType) {
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

	private void startFriendDetailActivity(Friend friend) {
		String action = null;
		if (friend.isFriend()) {
			action = Action.ACTION_FRIEND_DETAIL;
		} else {
			action = Action.ACTION_RECOMMEND_DETAIL;
		}
		Intent intent = new Intent(this, FriendDetailActivity.class);
		intent.setAction(action);
		intent.putExtra(FriendDetailActivity.PARAM_FRIEND, friend);
		startActivityForResult(intent, REQUEST_CODE_DETAIL);
		friendShowDetail = friend;
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
			setFriendSourceInfo(convertView, friend);
			mImageLoader.displayImage(friend.getHeadUrl(), portraitImage);
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

		private void setFriendSourceInfo(View convertView, Friend friend) {
			View sourceView = convertView.findViewById(R.id.linear_friend_source);
			FriendSourse source = friend.getSource();

			if (source == null) {
				sourceView.setVisibility(View.GONE);
			} else {
				sourceView.setVisibility(View.VISIBLE);
				TextView tvName = (TextView) convertView.findViewById(R.id.tv_source_name);
				TextView tvFrom = (TextView) convertView.findViewById(R.id.tv_source_from);
				switch (source.getAttrType()) {
				case FriendType.CONTACT:
					tvFrom.setText(R.string.contact_friend);
					break;
				case FriendType.FACEBOOK:
					tvFrom.setText(R.string.facebook_friend);
					break;
				case FriendType.VK:
					tvFrom.setText(R.string.vk_friend);
					break;
				default:
					tvFrom.setText(null);
					break;
				}
				tvName.setText(source.getName());
			}
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return mFriends.get(position);
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

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (REQUEST_CODE_DETAIL == requestCode) {
				Friend friendNew = (Friend) data.getSerializableExtra(FriendDetailActivity.RESULT_PARAM_FRIEND);
				friendShowDetail.setFriend(friendNew.isFriend());
				AddFriendsActivity.addFriend(friendNew);
				mAdapter.notifyDataSetChanged();
			}
		}
	};
}
