package com.rcplatform.phototalk;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
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
	private LinearLayout linearApps;
	private boolean isFromStangerPage;
	private DriftInformation information;
	private UserInfo userInfo;
private ImageView report_line;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stranger_detail);
		mImageLoader = ImageLoader.getInstance();
		initData();
		initView();
	}

	protected void showDialog() {
		AlertDialog dialog = new AlertDialog.Builder(this).setMessage(getString(R.string.report)).setPositiveButton(R.string.report, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (information != null) {
					DriftProxy.reportPic(StrangerDetailActivity.this, new RCPlatformResponseHandler() {

						@Override
						public void onSuccess(int statusCode, String content) {
							// TODO Auto-generated method stub
							Toast.makeText(StrangerDetailActivity.this, getString(R.string.report_success), Toast.LENGTH_LONG).show();
						}

						@Override
						public void onFailure(int errorCode, String content) {
							// TODO Auto-generated method stub
							showConfirmDialog(content);
						}
					}, information);
				}
			}
		}).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		}).create();
		dialog.show();
	}

	private void setFriendInfo() {
		mImageLoader.displayImage(mFriend.getHeadUrl(), ivHead, ImageOptionsFactory.getFriendHeadImageOptions());
		mImageLoader.displayImage(mFriend.getBackground(), ivBackground, ImageOptionsFactory.getFriendBackImageOptions());
		setFriendName();
	}

	private void initData() {
		mFriend = (Friend) getIntent().getSerializableExtra(PARAM_FRIEND);
		isFromStangerPage = getIntent().getBooleanExtra(PARAM_FROM_PAGE, false);
		information = (DriftInformation) getIntent().getSerializableExtra(PARAM_INFORMATION);
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
					showLoadingDialog(false);
					new AddFriendTask(StrangerDetailActivity.this, getPhotoTalkApplication().getCurrentUser(), new AddFriendTask.AddFriendListener() {
						@Override
						public void onFriendAddSuccess(Friend friend, int addType) {
							friendAddSuccess();
							dissmissLoadingDialog();
						}

						@Override
						public void onFriendAddFail(int statusCode, String content) {
							showConfirmDialog(content);
							dissmissLoadingDialog();
						}

						@Override
						public void onAlreadyAdded() {
							friendAddSuccess();
							dissmissLoadingDialog();
						}
					}, mFriend).execute();
				} else {
					if (information != null) {
						showLoadingDialog(false);
						new SkyPoolAddFriendTask(StrangerDetailActivity.this, userInfo, new SkyPoolAddFriendTask.SkyPoolAddFriendListener() {

							@Override
							public void onFriendAddSuccess(Friend friend, int addType) {
								friendAddSuccess();
								dissmissLoadingDialog();
							}

							@Override
							public void onFriendAddFail(int statusCode, String content) {
								showConfirmDialog(content);
								dissmissLoadingDialog();
							}

							@Override
							public void onAlreadyAdded() {
								friendAddSuccess();
								dissmissLoadingDialog();
							}
						}, information, mFriend).execute();
					}
				}
			}
		});
	}

	private void friendAddSuccess() {
		mFriend.setFriend(true);
		PhotoTalkDatabaseFactory.getDatabase().updateDriftInformationSenderInfo(mFriend);
		coverToFriendView();
	}

	private void coverToFriendView() {
		// 是好友 且列表不为空显示applist
		if (mFriend.getAppList() != null && mFriend.getAppList().size() > 0 && !mFriend.getRcId().equals(getCurrentUser().getRcId())) {
			linearApps.setVisibility(View.VISIBLE);
			PhotoTalkUtils.buildAppList(this, linearApps, mFriend.getAppList(), mImageLoader);
		} else {
			linearApps.setVisibility(View.GONE);
		}
		addFriendBtn.setVisibility(View.GONE);
		strangePerformBtn.setText(R.string.friend_detail_send_photo_hint_text);
	}

	private void initView() {
		addFriendBtn = (Button) findViewById(R.id.stranger_add_friend_btn);
		reportBtn = (Button) findViewById(R.id.report_btn);
		report_line = (ImageView)findViewById(R.id.report_line);
		if (isFromStangerPage) {
			reportBtn.setVisibility(View.VISIBLE);
			report_line.setVisibility(View.VISIBLE);
		} else {
			reportBtn.setVisibility(View.INVISIBLE);
			report_line.setVisibility(View.INVISIBLE);
		}
		reportBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
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
				EventUtil.Friends_Addfriends.rcpt_profile_takephotobutton(baseContext);
				sendBackToStranges();
			}
		});
	}

	private void sendBackToStranges() {
		Intent intent = new Intent(this, TakePhotoActivity.class);
		intent.putExtra("friend", mFriend);
		if (!mFriend.isFriend()) {
			intent.putExtra("photoType", PhotoInformationType.TYPE_DRIFT);
			if (information != null) {
				intent.putExtra(EditPictureActivity.PARAM_KEY_PIC_ID, information.getPicId());
				intent.putExtra(EditPictureActivity.PARAM_KEY_PIC_URL, information.getUrl());
			}
		} else {
			intent.putExtra("photoType", PhotoInformationType.TYPE_NORMAL);
		}
		if (getIntent().hasExtra(PARAM_BACK_PAGE))
			intent.putExtra(EditPictureActivity.PARAM_KEY_BACK_PAGE, getIntent().getSerializableExtra(PARAM_BACK_PAGE));
		startActivity(intent);
	}

	private void setFriendName() {
		switch (mFriend.getGender()) {
		case 0:
			if (!TextUtils.isEmpty(mFriend.getBirthday())) {
				tvName.setText(!TextUtils.isEmpty(mFriend.getLocalName()) ? mFriend.getLocalName() : mFriend.getNickName() + ", " + mFriend.getAge());
			} else {
				tvName.setText(!TextUtils.isEmpty(mFriend.getLocalName()) ? mFriend.getLocalName() : mFriend.getNickName());

			}
			break;
		case 1:
			if (!TextUtils.isEmpty(mFriend.getBirthday())) {
				tvName.setText(!TextUtils.isEmpty(mFriend.getLocalName()) ? mFriend.getLocalName() : mFriend.getNickName() + ", " + mFriend.getAge() + ", "
						+ getString(R.string.male));
			} else {
				tvName.setText(!TextUtils.isEmpty(mFriend.getLocalName()) ? mFriend.getLocalName() : mFriend.getNickName() + ", " + getString(R.string.male));

			}
			break;
		case 2:
			if (!TextUtils.isEmpty(mFriend.getBirthday())) {
				tvName.setText(!TextUtils.isEmpty(mFriend.getLocalName()) ? mFriend.getLocalName() : mFriend.getNickName() + ", " + mFriend.getAge() + ", "
						+ getString(R.string.famale));
			} else {
				tvName.setText(!TextUtils.isEmpty(mFriend.getLocalName()) ? mFriend.getLocalName() : mFriend.getNickName() + ", " + getString(R.string.famale));

			}
			break;
		}
		// tvName.setText(!TextUtils.isEmpty(mFriend.getLocalName()) ? mFriend
		// .getLocalName() : mFriend.getNickName());

		if (mFriend.getCountry() != null && !mFriend.getCountry().equals("")) {
			Bitmap bitmapFlag = Utils.getAssetCountryFlag(this, mFriend.getCountry());
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

	public void updateRemark(final String remark) {
		showLoadingDialog(false);
		FriendsProxy.updateFriendRemark(this, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				mFriend.setLocalName(remark);
				setFriendName();
				mRemarkEditWindow.dismiss();
				dissmissLoadingDialog();
			}

			@Override
			public void onFailure(int errorCode, String content) {
				dissmissLoadingDialog();
				showConfirmDialog(content);
			}
		}, mFriend.getRcId(), remark);
	}
}
