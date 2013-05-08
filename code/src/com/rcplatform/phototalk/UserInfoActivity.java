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

import com.facebook.model.GraphUser;
import com.rcplatform.phototalk.activity.FacebookActivity;
import com.rcplatform.phototalk.api.RCPlatformResponseHandler;
import com.rcplatform.phototalk.bean.FriendType;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.logic.LogicUtils;
import com.rcplatform.phototalk.task.FacebookUploadTask;
import com.rcplatform.phototalk.thirdpart.bean.ThirdPartFriend;

public class UserInfoActivity extends FacebookActivity implements OnClickListener {
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
		setAutoLogin(false);
		initTitle();
		initView();
		setTextView();
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
		if (isFacebookAuthorize()) {
			tvFacebookAuth.setText(R.string.authorized);
		} else {
			tvFacebookAuth.setText(R.string.unauthorized);
		}
	}

	public void setTextView() {
		UserInfo userInfo = getPhotoTalkApplication().getCurrentUser();
		user_Email.setText(userInfo.getEmail());
		user_Phone.setText(userInfo.getPhone());
		user_rcId.setText(userInfo.getRcId());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.user_facebook_layout:
			if (isFacebookAuthorize()) {
				showDeAuthorizeDialog(FriendType.FACEBOOK);
			} else {
				authorize();
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

	private void showDeAuthorizeDialog(int type) {
		if (mDeAuthDialog == null) {
			mDeAuthorizeDialogListener = new DeAuthorizeDialogListener();
			mDeAuthDialog = new AlertDialog.Builder(this).setMessage(R.string.dialog_confirm_deauth).setNegativeButton(R.string.confirm, mDeAuthorizeDialogListener).setPositiveButton(R.string.cancel, mDeAuthorizeDialogListener).create();
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
					deAuthorize();
				}
				break;
			case DialogInterface.BUTTON_POSITIVE:
				dialog.dismiss();
				break;
			}
		}
	};

	@Override
	protected void onFacebookInfoLoaded(GraphUser user, List<ThirdPartFriend> friends) {
		super.onFacebookInfoLoaded(user, friends);
		setAuthText();
		FacebookUploadTask task = new FacebookUploadTask(this, friends, user);
		task.setResponseListener(new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				dismissLoadingDialog();
			}

			@Override
			public void onFailure(int errorCode, String content) {
				dismissLoadingDialog();
			}
		});
		task.start();
	}

	@Override
	protected void onGetFacebookInfoError() {
		super.onGetFacebookInfoError();
		dismissLoadingDialog();
		setAuthText();
	}
}
