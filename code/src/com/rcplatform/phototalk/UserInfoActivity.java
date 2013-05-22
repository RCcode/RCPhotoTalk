package com.rcplatform.phototalk;

import java.util.List;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import com.rcplatform.phototalk.thirdpart.utils.FacebookClient.OnAuthorizeSuccessListener;
import com.rcplatform.phototalk.thirdpart.utils.FacebookClient.OnDeAuthorizeListener;
import com.rcplatform.phototalk.thirdpart.utils.FacebookClient.OnGetFacebookInfoSuccessListener;
import com.rcplatform.phototalk.thirdpart.utils.ThirdPartUtils;
import com.rcplatform.phototalk.utils.PrefsUtils;

public class UserInfoActivity extends BaseActivity implements OnClickListener {

	private FacebookClient mFacebookClient;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.user_info);
		mFacebookClient = new FacebookClient(this);
		mFacebookClient.onCreate(savedInstanceState);
		initTitle();
		initView();
		setTextView();
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
		setAuthText();
	}

	private void setAuthText() {
		if (ThirdPartUtils.isFacebookVlidate(this)) {
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
			if (ThirdPartUtils.isFacebookVlidate(this)) {
				showDeAuthorizeDialog(FriendType.FACEBOOK);
			} else {
				authorizeFacebook();
			}
			break;
		case R.id.user_vk_layout:

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
		}
	}

	private void authorizeFacebook() {
		mFacebookClient.authorize(new OnAuthorizeSuccessListener() {

			@Override
			public void onAuthorizeSuccess() {
				setAuthText();
				getFacebookInfo();
			}
		});
	}

	private void getFacebookInfo() {
		mFacebookClient.getFacebookInfo(new OnGetFacebookInfoSuccessListener() {

			@Override
			public void onGetFail() {
			}

			@Override
			public void onGetFacebookInfoSuccess(ThirdPartUser user, List<ThirdPartUser> friends) {
				PrefsUtils.User.ThirdPart.refreshFacebookAsyncTime(getApplicationContext(), getCurrentUser().getRcId());
				PhotoTalkDatabaseFactory.getDatabase().saveThirdPartFriends(friends, FriendType.FACEBOOK);
				new ThirdPartInfoUploadTask(getApplicationContext(), friends, user, FriendType.FACEBOOK, null);
			}
		});
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

		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case DialogInterface.BUTTON_NEGATIVE:
				if (mType == FriendType.FACEBOOK) {
					mFacebookClient.deAuthorize(new OnDeAuthorizeListener() {

						@Override
						public void onDeAuthorizeSuccess() {
							setAuthText();
						}

						@Override
						public void onDeAuthorizeFail() {
						}
					});
				}
				break;
			case DialogInterface.BUTTON_POSITIVE:
				dialog.dismiss();
				break;
			}
		}
	};
}
