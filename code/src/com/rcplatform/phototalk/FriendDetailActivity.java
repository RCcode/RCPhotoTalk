package com.rcplatform.phototalk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.adapter.AppAdapter;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.FriendSourse;
import com.rcplatform.phototalk.bean.FriendType;
import com.rcplatform.phototalk.image.downloader.ImageOptionsFactory;
import com.rcplatform.phototalk.image.downloader.RCPlatformImageLoader;
import com.rcplatform.phototalk.proxy.FriendsProxy;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.task.AddFriendTask;
import com.rcplatform.phototalk.utils.AppSelfInfo;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.PhotoTalkUtils;
import com.rcplatform.phototalk.views.HorizontalListView;
import com.rcplatform.phototalk.views.RoundImageView;

public class FriendDetailActivity extends BaseActivity {
	private Friend mFriend;
	private ImageLoader mImageLoader;
	public static final String PARAM_FRIEND = "friend";
	public static final String RESULT_PARAM_FRIEND = "friend";

	private String mAction;
	private RoundImageView ivHead;
	private ImageView ivBackground;
	private TextView tvSexAge;
	private HorizontalListView hlvApps;
	private TextView tvSource;
	private Button btnPerform;
	private PopupWindow mRemarkEditWindow;
	private View linearSource;

	private String mLastRemark;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friend_detail);
		initData();
		initView();
	}

	private void setFriendInfo() {

		RCPlatformImageLoader.loadImage(FriendDetailActivity.this, ImageLoader.getInstance(), ImageOptionsFactory.getHeadImageOptions(), mFriend.getHeadUrl(),
				AppSelfInfo.ImageScaleInfo.circleUserHeadRadius, ivHead, R.drawable.default_head);
		RCPlatformImageLoader.loadImage(FriendDetailActivity.this, ImageLoader.getInstance(), ImageOptionsFactory.getDefaultImageOptions(),
				mFriend.getBackground(), AppSelfInfo.ImageScaleInfo.circleUserHeadRadius, ivBackground, R.drawable.user_detail_bg);
		if (mFriend.getSource() != null) {
			setFriendSource(mFriend.getSource());
		}
		setFriendName();
		if (mFriend.getAppList() != null)
			hlvApps.setAdapter(new AppAdapter(this, mFriend.getAppList(), mImageLoader));

		tvSexAge.setText(getString(R.string.friend_sex_age, PhotoTalkUtils.getSexString(this, mFriend.getGender()), mFriend.getAge()));
	}

	private void initData() {
		mFriend = (Friend) getIntent().getSerializableExtra(PARAM_FRIEND);
		com.rcplatform.phototalk.request.Request.executeGetFriendDetailAsync(this, mFriend, null, true);
		mLastRemark = mFriend.getLocalName();
		mImageLoader = ImageLoader.getInstance();
		mAction = getIntent().getAction();
	}

	private void coverToRecommendView() {
		hlvApps.setVisibility(View.GONE);
		btnPerform.setText(R.string.add_to_friend);
		btnPerform.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
				new AddFriendTask(FriendDetailActivity.this, getPhotoTalkApplication().getCurrentUser(), new AddFriendTask.AddFriendListener() {

					@Override
					public void onFriendAddSuccess(Friend friend, int addType) {
						mFriend.setFriend(true);
						coverToFriendView();
						dismissLoadingDialog();
					}

					@Override
					public void onFriendAddFail(int statusCode, String content) {
						showErrorConfirmDialog(content);
						dismissLoadingDialog();
					}
				}, mFriend).execute();
			}
		});
	}

	private void coverToFriendView() {
		btnPerform.setText(R.string.friend_detail_send_photo_hint_text);
		btnPerform.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startTakePhotoActivity();
			}
		});
	}

	private void startTakePhotoActivity() {
		Intent intent = new Intent(this, TakePhotoActivity.class);
		intent.putExtra("friend", mFriend);
		startActivity(intent);
	}

	private void initView() {
		linearSource = findViewById(R.id.linear_source);
		ivHead = (RoundImageView) findViewById(R.id.iv_head);
		ivBackground = (ImageView) findViewById(R.id.iv_bg);
		tvSexAge = (TextView) findViewById(R.id.tv_sex_age);
		hlvApps = (HorizontalListView) findViewById(R.id.hlv_apps);
		tvSource = (TextView) findViewById(R.id.tv_source_name);
		tvName = (TextView) findViewById(R.id.tv_name);
		btnPerform = (Button) findViewById(R.id.btn_perform);
		if (mAction.equals(Constants.Action.ACTION_FRIEND_DETAIL)) {
			coverToFriendView();
		} else if (mAction.equals(Constants.Action.ACTION_RECOMMEND_DETAIL)) {
			coverToRecommendView();
		}
		setFriendInfo();
		if (mFriend.getRcId().equals(getCurrentUser().getRcId())) {
			linearSource.setVisibility(View.GONE);
		}

	}

	private void setFriendSource(FriendSourse source) {
		if (source.getAttrType() == FriendType.CONTACT) {
			tvSource.setText(source.getName() + ":" + source.getValue());
		} else {
			tvSource.setText(source.getName());
		}
	}

	private void setFriendName() {
		tvName.setText(!TextUtils.isEmpty(mFriend.getLocalName()) ? mFriend.getLocalName() : mFriend.getNickName());
	}

	private TextView tvName;

	public boolean onKeyDown(int keyCode, android.view.KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (hasChangeUserInfo()) {
				Intent data = new Intent();
				data.putExtra(RESULT_PARAM_FRIEND, mFriend);
				setResult(Activity.RESULT_OK, data);
			}
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	};

	private boolean hasChangeUserInfo() {
		if (mAction.equals(Constants.Action.ACTION_RECOMMEND_DETAIL) && mFriend.isFriend()) {
			return true;
		}
		if (mLastRemark != null && !mLastRemark.equals(mFriend.getLocalName())) {
			return true;
		} else if (mLastRemark == null && null != mFriend.getLocalName()) {
			return true;
		}
		return false;
	}

	private void showRemaikWindow(View v) {
		if (mRemarkEditWindow == null) {
			View editView = getLayoutInflater().inflate(R.layout.my_friend_details_layout_edit, null, false);
			Button btnConfirm = (Button) editView.findViewById(R.id.btn_remark_confirm);
			final EditText etRemark = (EditText) editView.findViewById(R.id.et_remark);
			etRemark.setText(mFriend.getLocalName());
			btnConfirm.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					String remark = etRemark.getText().toString();
					updateRemark(remark);
				}
			});
			mRemarkEditWindow = new PopupWindow(editView, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

			mRemarkEditWindow.setFocusable(true);
			mRemarkEditWindow.setOutsideTouchable(true);
			mRemarkEditWindow.setBackgroundDrawable(new BitmapDrawable());

		}
		mRemarkEditWindow.showAtLocation(v, Gravity.BOTTOM, 0, 0);
	}

	public void updateRemark(final String remark) {
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		FriendsProxy.updateFriendRemark(this, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				mFriend.setLocalName(remark);
				setFriendName();
				mRemarkEditWindow.dismiss();
				dismissLoadingDialog();
			}

			@Override
			public void onFailure(int errorCode, String content) {
				dismissLoadingDialog();
				showErrorConfirmDialog(content);
			}
		}, mFriend.getRcId(), remark);
	}
}
