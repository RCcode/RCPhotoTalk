package com.rcplatform.phototalk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
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
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.FriendSourse;
import com.rcplatform.phototalk.bean.FriendType;
import com.rcplatform.phototalk.image.downloader.ImageOptionsFactory;
import com.rcplatform.phototalk.proxy.FriendsProxy;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.task.AddFriendTask;
import com.rcplatform.phototalk.umeng.EventUtil;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.PhotoTalkUtils;
import com.rcplatform.phototalk.utils.Utils;

public class FriendDetailActivity extends BaseActivity {
	private Friend mFriend;
	private ImageLoader mImageLoader;
	public static final String PARAM_FRIEND = "friend";
	public static final String RESULT_PARAM_FRIEND = "friend";
	private String mAction;
	private ImageView ivHead;
	private ImageView ivBackground;
	private ImageView ivCountryFlag;
	private TextView tvSexAge;
	private TextView tvSource;
	private Button btnPerform;
	private PopupWindow mRemarkEditWindow;
	private View linearSource;
	private TextView tv_rcid;
	private String mLastRemark;
	private LinearLayout linearApps;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friend_detail);
		mImageLoader = ImageLoader.getInstance();
		initData();
		initView();
	}

	private void setFriendInfo() {
		mImageLoader.displayImage(mFriend.getHeadUrl(), ivHead,
				ImageOptionsFactory.getFriendHeadImageOptions());
		mImageLoader.displayImage(mFriend.getBackground(), ivBackground,
				ImageOptionsFactory.getFriendBackImageOptions());
		if (mFriend.getSource() != null) {
			setFriendSource(mFriend.getSource());
		}
		if (mFriend.getAppList() != null && mFriend.getAppList().size() > 0)
			PhotoTalkUtils.buildAppList(this, linearApps, mFriend.getAppList(),
					mImageLoader);
		setFriendName();
		tvSexAge.setText(getString(R.string.friend_sex_age,
				PhotoTalkUtils.getSexString(this, mFriend.getGender()),
				mFriend.getAge()));
	}

	private void initData() {
		mFriend = (Friend) getIntent().getSerializableExtra(PARAM_FRIEND);
		if (!mFriend.getRcId().equals(getCurrentUser().getRcId()))
			com.rcplatform.phototalk.request.Request
					.executeGetFriendDetailAsync(this, mFriend, null, true);
		mLastRemark = mFriend.getLocalName();
		mImageLoader = ImageLoader.getInstance();
		mAction = getIntent().getAction();
	}

	private void coverToRecommendView() {
		linearApps.setVisibility(View.GONE);
		btnPerform.setText(R.string.add_to_friend);
		btnPerform.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
				new AddFriendTask(FriendDetailActivity.this,
						getPhotoTalkApplication().getCurrentUser(),
						new AddFriendTask.AddFriendListener() {

							@Override
							public void onFriendAddSuccess(Friend friend,
									int addType) {
								mFriend.setFriend(true);
								coverToFriendView();
								dismissLoadingDialog();
							}

							@Override
							public void onFriendAddFail(int statusCode,
									String content) {
								showErrorConfirmDialog(content);
								dismissLoadingDialog();
							}

							@Override
							public void onAlreadyAdded() {
								mFriend.setFriend(true);
								coverToFriendView();
								dismissLoadingDialog();
							}
						}, mFriend).execute();
			}
		});
	}

	private void coverToFriendView() {
		if (mFriend.getAppList() != null && mFriend.getAppList().size() > 0
				&& !mFriend.getRcId().equals(getCurrentUser().getRcId()))
			linearApps.setVisibility(View.VISIBLE);
		else
			linearApps.setVisibility(View.GONE);
		btnPerform.setText(R.string.friend_detail_send_photo_hint_text);
		btnPerform.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				EventUtil.Friends_Addfriends
						.rcpt_profile_takephotobutton(baseContext);
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
		linearApps = (LinearLayout) findViewById(R.id.linear_apps);
		ivHead = (ImageView) findViewById(R.id.iv_head);
		ivBackground = (ImageView) findViewById(R.id.iv_bg);
		tvSexAge = (TextView) findViewById(R.id.tv_sex_age);
		tv_rcid = (TextView) findViewById(R.id.tv_rcid);
		tvSource = (TextView) findViewById(R.id.tv_source_name);
		tvName = (TextView) findViewById(R.id.tv_name);
		btnPerform = (Button) findViewById(R.id.btn_perform);
		ivCountryFlag = (ImageView) findViewById(R.id.iv_country_flag);
		setFriendInfo();
		if (mAction.equals(Constants.Action.ACTION_FRIEND_DETAIL)) {
			coverToFriendView();
		} else if (mAction.equals(Constants.Action.ACTION_RECOMMEND_DETAIL)) {
			coverToRecommendView();
		}
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
		switch (mFriend.getGender()) {
		case 0:
			tvName.setText(!TextUtils.isEmpty(mFriend.getLocalName()) ? mFriend
					.getLocalName() : mFriend.getNickName() + ", "
					+ mFriend.getAge());
			break;
		case 1:
			tvName.setText(!TextUtils.isEmpty(mFriend.getLocalName()) ? mFriend
					.getLocalName() : mFriend.getNickName() + ", "
					+ mFriend.getAge() + ", " + getString(R.string.male));
			break;
		case 2:
			tvName.setText(!TextUtils.isEmpty(mFriend.getLocalName()) ? mFriend
					.getLocalName() : mFriend.getNickName() + ", "
					+ mFriend.getAge() + ", " + getString(R.string.famale));
			break;
		}
		// tvName.setText(!TextUtils.isEmpty(mFriend.getLocalName()) ? mFriend
		// .getLocalName() : mFriend.getNickName());

		if (mFriend.getCountry() != null && !mFriend.getCountry().equals("")) {
			Bitmap bitmapFlag = Utils.getAssetCountryFlag(this,
					mFriend.getCountry());
			if (bitmapFlag != null) {
				ivCountryFlag.setImageBitmap(bitmapFlag);
			}
		}
		tv_rcid.setText(mFriend.getRcId());

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
		if (mAction.equals(Constants.Action.ACTION_RECOMMEND_DETAIL)
				&& mFriend.isFriend()) {
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
			View editView = getLayoutInflater().inflate(
					R.layout.my_friend_details_layout_edit, null, false);
			Button btnConfirm = (Button) editView
					.findViewById(R.id.btn_remark_confirm);
			final EditText etRemark = (EditText) editView
					.findViewById(R.id.et_remark);
			etRemark.setText(mFriend.getLocalName());
			btnConfirm.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					String remark = etRemark.getText().toString();
					updateRemark(remark);
				}
			});
			mRemarkEditWindow = new PopupWindow(editView,
					WindowManager.LayoutParams.MATCH_PARENT,
					WindowManager.LayoutParams.WRAP_CONTENT);

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
