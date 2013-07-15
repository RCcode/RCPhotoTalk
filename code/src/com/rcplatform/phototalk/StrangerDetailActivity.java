package com.rcplatform.phototalk;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.PhotoInformationType;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.drift.DriftInformation;
import com.rcplatform.phototalk.image.downloader.ImageOptionsFactory;
import com.rcplatform.phototalk.proxy.DriftProxy;
import com.rcplatform.phototalk.proxy.FriendsProxy;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.task.AddFriendTask;
import com.rcplatform.phototalk.task.SkyPoolAddFriendTask;
import com.rcplatform.phototalk.umeng.EventUtil;
import com.rcplatform.phototalk.utils.PhotoTalkUtils;
import com.rcplatform.phototalk.utils.Utils;

//漂流瓶 好友和陌生人 详细信息页面
public class StrangerDetailActivity extends BaseActivity {
	private Friend mFriend;
	private ImageLoader mImageLoader;
	public static final String PARAM_FRIEND = "friend";
	public static final String PARAM_INFORMATION = "information";
	public static final String PARAM_FROM_PAGE = "isFromStangerPage";
	public static final String RESULT_PARAM_FRIEND = "friend";
	public static final String PARAM_BACK_PAGE = "backpage";
	// private String mAction;
	private ImageView ivHead;
	private ImageView ivBackground;
	private ImageView ivCountryFlag;
	private Button strangePerformBtn;
	private Button addFriendBtn;
	private Button reportBtn;
	private PopupWindow mRemarkEditWindow;
	private TextView tv_rcid;
	private String mLastRemark;
	private LinearLayout linearApps;
	private boolean isFromStangerPage;
	private DriftInformation information;
	private UserInfo userInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stranger_detail);
		mImageLoader = ImageLoader.getInstance();
		initData();
		initView();
	}

	protected void showDialog() {
		AlertDialog dialog = new AlertDialog.Builder(this)
				.setMessage("是否举报")
				.setPositiveButton("举报", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						if (information != null) {
							DriftProxy.reportPic(StrangerDetailActivity.this,
									new RCPlatformResponseHandler() {

										@Override
										public void onSuccess(int statusCode,
												String content) {
											// TODO Auto-generated method stub
											Toast.makeText(
													StrangerDetailActivity.this,
													"举报成功", Toast.LENGTH_LONG)
													.show();
										}

										@Override
										public void onFailure(int errorCode,
												String content) {
											// TODO Auto-generated method stub
											showErrorConfirmDialog(content);
										}
									}, information);
						}
					}
				})
				.setNegativeButton("取消", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						dialog.dismiss();
					}
				}).create();
		dialog.show();
	}

	private void setFriendInfo() {
		mImageLoader.displayImage(mFriend.getHeadUrl(), ivHead,
				ImageOptionsFactory.getFriendHeadImageOptions());
		mImageLoader.displayImage(mFriend.getBackground(), ivBackground,
				ImageOptionsFactory.getFriendBackImageOptions());
		setFriendName();
	}

	private void initData() {
		mFriend = (Friend) getIntent().getSerializableExtra(PARAM_FRIEND);
		isFromStangerPage = getIntent().getBooleanExtra(PARAM_FROM_PAGE, false);
		information = (DriftInformation) getIntent().getSerializableExtra(
				PARAM_INFORMATION);
		mLastRemark = mFriend.getLocalName();
		mImageLoader = ImageLoader.getInstance();
		// mAction = getIntent().getAction();
		userInfo = getCurrentUser();
	}

	private void coverToRecommendView() {
		linearApps.setVisibility(View.GONE);
		strangePerformBtn.setText(R.string.reply);
		addFriendBtn.setText(R.string.add_to_friend);
		addFriendBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (!isFromStangerPage) {
					showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
					new AddFriendTask(StrangerDetailActivity.this,
							getPhotoTalkApplication().getCurrentUser(),
							new AddFriendTask.AddFriendListener() {
								@Override
								public void onFriendAddSuccess(Friend friend,
										int addType) {
									friendAddSuccess();
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
									friendAddSuccess();
									dismissLoadingDialog();
								}
							}, mFriend).execute();
				} else {
					if (information != null) {
						showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
						new SkyPoolAddFriendTask(
								StrangerDetailActivity.this,
								userInfo,
								new SkyPoolAddFriendTask.SkyPoolAddFriendListener() {

									@Override
									public void onFriendAddSuccess(
											Friend friend, int addType) {
										// TODO Auto-generated method stub
										friendAddSuccess();
										dismissLoadingDialog();
									}

									@Override
									public void onFriendAddFail(int statusCode,
											String content) {
										// TODO Auto-generated method stub
										showErrorConfirmDialog(content);
										dismissLoadingDialog();
									}

									@Override
									public void onAlreadyAdded() {
										// TODO Auto-generated method stub
										friendAddSuccess();
										dismissLoadingDialog();
									}
								}, information, mFriend).execute();
					}
				}
			}
		});
	}

	private void friendAddSuccess() {
		mFriend.setFriend(true);
		PhotoTalkDatabaseFactory.getDatabase()
				.updateDriftInformationSenderInfo(mFriend);
		coverToFriendView();
	}

	private void coverToFriendView() {
		// 是好友 且列表不为空显示applist
		if (mFriend.getAppList() != null && mFriend.getAppList().size() > 0
				&& !mFriend.getRcId().equals(getCurrentUser().getRcId())) {
			linearApps.setVisibility(View.VISIBLE);
			PhotoTalkUtils.buildAppList(this, linearApps, mFriend.getAppList(),
					mImageLoader);
		} else {
			linearApps.setVisibility(View.GONE);
		}
		addFriendBtn.setVisibility(View.GONE);
		strangePerformBtn.setText(R.string.friend_detail_send_photo_hint_text);
	}

	private void startTakePhotoActivity() {
		Intent intent = new Intent(this, TakePhotoActivity.class);
		intent.putExtra(RESULT_PARAM_FRIEND, mFriend);
		startActivity(intent);
	}

	private void initView() {
		addFriendBtn = (Button) findViewById(R.id.stranger_add_friend_btn);
		reportBtn = (Button) findViewById(R.id.report_btn);
		if (isFromStangerPage) {
			reportBtn.setVisibility(View.VISIBLE);
		} else {
			reportBtn.setVisibility(View.INVISIBLE);
		}
		reportBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				showDialog();
			}
		});
		ivHead = (ImageView) findViewById(R.id.strange_iv_head);
		ivBackground = (ImageView) findViewById(R.id.stranger_iv_bg);
		tv_rcid = (TextView) findViewById(R.id.strange_rcid);
		tvName = (TextView) findViewById(R.id.strange_name);
		strangePerformBtn = (Button) findViewById(R.id.strange_perform_btn);
		ivCountryFlag = (ImageView) findViewById(R.id.strange_country_flag);
		Bitmap bitmap = Utils.getAssetCountryFlag(this, mFriend.getCountry());
		if (bitmap != null) {
			ivCountryFlag.setImageBitmap(bitmap);
		}
		linearApps = (LinearLayout) findViewById(R.id.stranger_linear_apps);
		setFriendInfo();
		if (mFriend.isFriend() || userInfo.getRcId().equals(mFriend.getRcId())) {
			coverToFriendView();
		} else {
			coverToRecommendView();
		}
		strangePerformBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				EventUtil.Friends_Addfriends
						.rcpt_profile_takephotobutton(baseContext);
				sendBackToStranges();
			}
		});
	}

	private void sendBackToStranges() {
		Intent intent = new Intent(this, TakePhotoActivity.class);
		intent.putExtra("friend", mFriend);
		intent.putExtra("photoType", PhotoInformationType.TYPE_DRIFT);
		if (getIntent().hasExtra(PARAM_BACK_PAGE))
			intent.putExtra(EditPictureActivity.PARAM_KEY_BACK_PAGE,
					getIntent().getSerializableExtra(PARAM_BACK_PAGE));
		startActivity(intent);
	}

	private void setFriendName() {
		switch (mFriend.getGender()) {
		case 0:
			if (!TextUtils.isEmpty(mFriend.getBirthday())) {
				tvName.setText(!TextUtils.isEmpty(mFriend.getLocalName()) ? mFriend
						.getLocalName() : mFriend.getNickName() + ", "
						+ mFriend.getAge());
			} else {
				tvName.setText(!TextUtils.isEmpty(mFriend.getLocalName()) ? mFriend
						.getLocalName() : mFriend.getNickName());

			}
			break;
		case 1:
			if (!TextUtils.isEmpty(mFriend.getBirthday())) {
				tvName.setText(!TextUtils.isEmpty(mFriend.getLocalName()) ? mFriend
						.getLocalName() : mFriend.getNickName() + ", "
						+ mFriend.getAge() + ", " + getString(R.string.male));
			} else {
				tvName.setText(!TextUtils.isEmpty(mFriend.getLocalName()) ? mFriend
						.getLocalName() : mFriend.getNickName() + ", "
						+ getString(R.string.male));

			}
			break;
		case 2:
			if (!TextUtils.isEmpty(mFriend.getBirthday())) {
				tvName.setText(!TextUtils.isEmpty(mFriend.getLocalName()) ? mFriend
						.getLocalName() : mFriend.getNickName() + ", "
						+ mFriend.getAge() + ", " + getString(R.string.famale));
			} else {
				tvName.setText(!TextUtils.isEmpty(mFriend.getLocalName()) ? mFriend
						.getLocalName() : mFriend.getNickName() + ", "
						+ getString(R.string.famale));

			}
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
			// if (hasChangeUserInfo()) {
			// Intent data = new Intent();
			// data.putExtra(RESULT_PARAM_FRIEND, mFriend);
			// setResult(Activity.RESULT_OK, data);
			// }
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	};

	// private boolean hasChangeUserInfo() {
	// if (mAction.equals(Constants.Action.ACTION_RECOMMEND_DETAIL) &&
	// mFriend.isFriend()) {
	// return true;
	// }
	// if (mLastRemark != null && !mLastRemark.equals(mFriend.getLocalName())) {
	// return true;
	// } else if (mLastRemark == null && null != mFriend.getLocalName()) {
	// return true;
	// }
	// return false;
	// }

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
