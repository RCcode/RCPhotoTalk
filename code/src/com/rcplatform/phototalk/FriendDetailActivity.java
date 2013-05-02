package com.rcplatform.phototalk;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.adapter.AppAdapter;
import com.rcplatform.phototalk.api.RCPlatformResponseHandler;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.FriendType;
import com.rcplatform.phototalk.task.AddFriendTask;
import com.rcplatform.phototalk.utils.Contract;
import com.rcplatform.phototalk.utils.PhotoTalkUtils;
import com.rcplatform.phototalk.views.HorizontalListView;

public class FriendDetailActivity extends BaseActivity {
	private Friend mFriend;
	private ImageLoader mImageLoader;
	public static final String PARAM_FRIEND = "friend";

	public static final String REULST_PARAM_HASADD = "hasadd";

	private String mAction;
	private ImageView ivHead;
	private ImageView ivBackground;
	private Button btnEdit;
	private TextView tvSexAge;
	private HorizontalListView hlvApps;
	private TextView tvSource;
	private Button btnPerform;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friend_detail);
		initData();
		initView();
	}

	private void initData() {
		mFriend = (Friend) getIntent().getSerializableExtra(PARAM_FRIEND);
		mImageLoader = ImageLoader.getInstance();
		mAction = getIntent().getAction();
	}

	private void coverToRecommendView() {
		hlvApps.setVisibility(View.GONE);
		btnEdit.setVisibility(View.GONE);
		btnPerform.setText(R.string.add_to_friend);
		btnPerform.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
				new AddFriendTask(FriendDetailActivity.this, getPhotoTalkApplication().getCurrentUser(), new RCPlatformResponseHandler() {

					@Override
					public void onSuccess(int statusCode, String content) {
						mFriend.setStatus(Friend.USER_STATUS_FRIEND_ADDED);
						coverToFriendView();
						dismissLoadingDialog();
					}

					@Override
					public void onFailure(int errorCode, String content) {
						showErrorConfirmDialog(content);
						dismissLoadingDialog();
					}
				}, mFriend).execute();
			}
		});
	}

	private void coverToFriendView() {
		btnPerform.setText(R.string.friend_detail_send_photo_hint_text);
		btnEdit.setVisibility(View.VISIBLE);
	}

	private void initView() {
		ivHead = (ImageView) findViewById(R.id.iv_head);
		ivBackground = (ImageView) findViewById(R.id.iv_bg);
		btnEdit = (Button) findViewById(R.id.btn_edit);
		tvSexAge = (TextView) findViewById(R.id.tv_sex_age);
		hlvApps = (HorizontalListView) findViewById(R.id.hlv_apps);
		tvSource = (TextView) findViewById(R.id.tv_source);
		TextView tvName = (TextView) findViewById(R.id.tv_name);
		mImageLoader.displayImage(mFriend.getHeadUrl(), ivHead);
		mImageLoader.displayImage(mFriend.getBackground(), ivBackground);
		btnEdit.setOnClickListener(mOnClickListener);
		tvSexAge.setText(getString(R.string.friend_sex_age, PhotoTalkUtils.getSexString(this, mFriend.getSex()), mFriend.getAge()));
		tvName.setText(!TextUtils.isEmpty(mFriend.getMark()) ? mFriend.getMark() : mFriend.getNick());
		tvSource.setText(mFriend.getSource().getAttrType() == FriendType.CONTACT ? mFriend.getSource().getValue() : mFriend.getSource().getName());
		btnPerform = (Button) findViewById(R.id.btn_perform);
		hlvApps.setAdapter(new AppAdapter(this, mFriend.getAppList(), mImageLoader));
		if (mAction.equals(Contract.Action.ACTION_FRIEND_DETAIL)) {
			coverToFriendView();
		} else if (mAction.equals(Contract.Action.ACTION_RECOMMEND_DETAIL)) {
			coverToRecommendView();
		}
	}

	private OnClickListener mOnClickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.btn_edit:

				break;

			default:
				break;
			}
		}
	};

	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (mAction.equals(Contract.Action.ACTION_RECOMMEND_DETAIL)) {
				if (mFriend.getStatus() == Friend.USER_STATUS_FRIEND_ADDED)
					setResult(Activity.RESULT_OK);
			}
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	};
}
