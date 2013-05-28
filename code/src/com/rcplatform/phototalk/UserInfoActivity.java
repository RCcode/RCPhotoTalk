package com.rcplatform.phototalk;

import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.bean.FriendType;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.logic.LogicUtils;
import com.rcplatform.phototalk.task.ThirdPartInfoUploadTask;
import com.rcplatform.phototalk.thirdpart.bean.ThirdPartUser;
import com.rcplatform.phototalk.thirdpart.utils.FacebookClient;
import com.rcplatform.phototalk.thirdpart.utils.OnAuthorizeSuccessListener;
import com.rcplatform.phototalk.thirdpart.utils.OnDeAuthorizeListener;
import com.rcplatform.phototalk.thirdpart.utils.OnGetThirdPartInfoSuccessListener;
import com.rcplatform.phototalk.thirdpart.utils.VKClient;
import com.rcplatform.phototalk.utils.PrefsUtils;

public class UserInfoActivity extends BaseActivity implements OnClickListener {

	private FacebookClient mFacebookClient;
	private VKClient mVKClient;
	private TextView user_Email;
	private TextView user_Phone;
	private TextView user_rcId;
	private TextView mTitleTextView;
	private RelativeLayout faceBook_layout;
	private TextView tvFacebookAuth;

	private RelativeLayout vK_layout;
	private Button reset_pw_btn;
	private Button login_out_btn;
	private View mBack;
	private AlertDialog mDeAuthDialog;
	private DeAuthorizeDialogListener mDeAuthorizeDialogListener;
	private TextView tvVKAuth;
	private View phoneLayout;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_info);
		initThirdPartClient(savedInstanceState);
		initTitle();
		initView();
		setTextView();
	}

	private void initThirdPartClient(Bundle savedInstanceState) {
		mFacebookClient = new FacebookClient(this);
		mFacebookClient.onCreate(savedInstanceState);
		mVKClient = new VKClient(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mFacebookClient.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mFacebookClient.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mFacebookClient.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		mFacebookClient.onActivityResult(requestCode, resultCode, data);
		mVKClient.onActivityResult(requestCode, resultCode, data);
	}

	private void initTitle() {
		mBack = findViewById(R.id.back);
		mBack.setVisibility(View.VISIBLE);
		mBack.setOnClickListener(this);
		mTitleTextView = (TextView) findViewById(R.id.titleContent);
		mTitleTextView.setText(getResources().getString(R.string.user_message));
		mTitleTextView.setVisibility(View.VISIBLE);
	}

	public void initView() {
		phoneLayout = findViewById(R.id.rela_phone);
		phoneLayout.setOnClickListener(this);
		user_Email = (TextView) findViewById(R.id.user_email);
		user_Phone = (TextView) findViewById(R.id.user_phone);
		user_rcId = (TextView) findViewById(R.id.user_rcid);
		faceBook_layout = (RelativeLayout) findViewById(R.id.user_facebook_layout);
		faceBook_layout.setOnClickListener(this);
		vK_layout = (RelativeLayout) findViewById(R.id.user_vk_layout);
		vK_layout.setOnClickListener(this);
		reset_pw_btn = (Button) findViewById(R.id.reset_pw_btn);
		reset_pw_btn.setOnClickListener(this);
		login_out_btn = (Button) findViewById(R.id.login_out_btn);
		login_out_btn.setOnClickListener(this);
		tvFacebookAuth = (TextView) findViewById(R.id.tv_facebook_authstate);
		tvVKAuth = (TextView) findViewById(R.id.tv_vk_authstate);
		setFacebookAuthText();
		setVKAuthText();
	}

	private void setVKAuthText() {
		if (mVKClient.isAuthorize()) {
			tvVKAuth.setText(R.string.authorized);
		} else {
			tvVKAuth.setText(R.string.unauthorized);
		}
	}

	private void setFacebookAuthText() {
		if (mFacebookClient.isAuthorize()) {
			tvFacebookAuth.setText(R.string.authorized);
		} else {
			tvFacebookAuth.setText(R.string.unauthorized);
		}
	}

	public void setTextView() {
		UserInfo userInfo = getPhotoTalkApplication().getCurrentUser();
		user_Email.setText(userInfo.getEmail());
		user_Phone.setText(userInfo.getCellPhone());
		user_rcId.setText(userInfo.getRcId());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.user_facebook_layout:
			if (mFacebookClient.isAuthorize()) {
				showDeAuthorizeDialog(FriendType.FACEBOOK);
			} else {
				authorizeFacebook();
			}
			break;
		case R.id.user_vk_layout:
			if (mVKClient.isAuthorize()) {
				showDeAuthorizeDialog(FriendType.VK);
			} else {
				authorizeVK();
			}
			break;
		case R.id.reset_pw_btn:
			startActivity(ChangePasswordActivity.class);
			break;
		case R.id.login_out_btn:
			LogicUtils.logout(this);
			break;
		case R.id.back:
			startActivity(new Intent(this, SettingsActivity.class));
			this.finish();
			break;
		case R.id.rela_phone:
			if (TextUtils.isEmpty(getCurrentUser().getCellPhone())) {
				startActivity(RequestSMSActivity.class);
			}
			break;
		}
	}

	private void authorizeFacebook() {
		mFacebookClient.authorize(new OnAuthorizeSuccessListener() {

			@Override
			public void onAuthorizeSuccess() {
				setFacebookAuthText();
				getFacebookInfo();
			}
		});
	}

	private void authorizeVK() {
		mVKClient.authorize(new OnAuthorizeSuccessListener() {

			@Override
			public void onAuthorizeSuccess() {
				setVKAuthText();
				getVkInfo();
			}
		});
	}

	private void getVkInfo() {
		mVKClient.getVKInfo(new OnGetThirdPartInfoSuccessListener() {

			@Override
			public void onGetInfoSuccess(ThirdPartUser user, List<ThirdPartUser> friends) {
				saveThirdInfo(FriendType.VK, friends, user);
			}

			@Override
			public void onGetFail() {
			}
		});
	}

	private void getFacebookInfo() {
		mFacebookClient.getFacebookInfo(new OnGetThirdPartInfoSuccessListener() {

			@Override
			public void onGetFail() {
			}

			@Override
			public void onGetInfoSuccess(ThirdPartUser user, List<ThirdPartUser> friends) {
				saveThirdInfo(FriendType.FACEBOOK, friends, user);
			}
		});
	}

	private void saveThirdInfo(int type, List<ThirdPartUser> friends, ThirdPartUser user) {
		if (type == FriendType.FACEBOOK) {
			PrefsUtils.User.ThirdPart.refreshFacebookAsyncTime(getApplicationContext(), getCurrentUser().getRcId());
		} else if (type == FriendType.VK) {
			PrefsUtils.User.ThirdPart.refreshVKSyncTime(getApplicationContext(), getCurrentUser().getRcId());
		}
		PhotoTalkDatabaseFactory.getDatabase().saveThirdPartFriends(friends, type);
		new ThirdPartInfoUploadTask(getApplicationContext(), friends, user, type, null);
	}

	private void showDeAuthorizeDialog(int type) {
		if (mDeAuthDialog == null) {
			mDeAuthorizeDialogListener = new DeAuthorizeDialogListener();
			mDeAuthDialog = new AlertDialog.Builder(this).setMessage(R.string.dialog_confirm_deauth)
					.setNegativeButton(R.string.confirm, mDeAuthorizeDialogListener).setPositiveButton(R.string.cancel, mDeAuthorizeDialogListener).create();
		}
		mDeAuthorizeDialogListener.setType(type);
		mDeAuthDialog.show();
	}

	class DeAuthorizeDialogListener implements DialogInterface.OnClickListener {
		private int mType;

		public void setType(int type) {
			this.mType = type;
		}

		private OnDeAuthorizeListener mDeAuthorizeListener = new OnDeAuthorizeListener() {

			@Override
			public void onDeAuthorizeSuccess() {
				showErrorConfirmDialog(R.string.deauthorize_complete);
				if (mType == FriendType.FACEBOOK)
					setFacebookAuthText();
				else if (mType == FriendType.VK)
					setVKAuthText();
			}

			@Override
			public void onDeAuthorizeFail() {
				showErrorConfirmDialog(R.string.deauthorize_fail);
			}
		};

		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_NEGATIVE:
				if (mType == FriendType.FACEBOOK) {
					mFacebookClient.deAuthorize(mDeAuthorizeListener);
				} else if (mType == FriendType.VK) {
					mVKClient.deAuthorize(mDeAuthorizeListener);
				}
				break;
			case DialogInterface.BUTTON_POSITIVE:
				dialog.dismiss();
				break;
			}
		}
	};

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		user_Phone.setText(getCurrentUser().getCellPhone());
	}
}
