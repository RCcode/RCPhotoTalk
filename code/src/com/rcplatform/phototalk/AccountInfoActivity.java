package com.rcplatform.phototalk;

import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Process;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.task.GetBindPhoneTask;
import com.rcplatform.phototalk.task.GetBindPhoneTask.OnBindSuccessListener;
import com.rcplatform.phototalk.thirdpart.utils.ThirdPartAccessTokenKeeper;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.PrefsUtils;

public class AccountInfoActivity extends BaseActivity implements View.OnClickListener {

	private static final int REQUEST_CODE_UNBIND_FACEBOOK = 10087;

	private TextView mEmailView;

	private TextView mBindPhoneTextView;

	private TextView mTatotyIdView;

	private TextView mFacebookView;

	private GetBindPhoneTask mTask;

	private View mBack;

	private TextView mTitleTextView;
	private UserInfo mUserInfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_account_info);
		mUserInfo = PrefsUtils.LoginState.getLoginUser(getApplicationContext());
		initTitle();
		mEmailView = (TextView) findViewById(R.id.account_email);
		mEmailView.setText(mUserInfo.getEmail());
		mBindPhoneTextView = (TextView) findViewById(R.id.account_bind_number);
		mBindPhoneTextView.setOnClickListener(this);
		mTatotyIdView = (TextView) findViewById(R.id.account_tacaty_id);
		mTatotyIdView.setOnClickListener(this);
		String tocatyId = getIntent().getExtras().getString("totatyId");
		if (!TextUtils.isEmpty(tocatyId)) {
			mTatotyIdView.setText("" + tocatyId);
			mTatotyIdView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
		}
		//
		mFacebookView = (TextView) findViewById(R.id.account_facebook_id);
		mFacebookView.setOnClickListener(this);
		String facebookName = ThirdPartAccessTokenKeeper.getFacebookLoginName(this); // ???????????????
		if (!TextUtils.isEmpty(facebookName)) {
			mFacebookView.setText("" + facebookName);
			mFacebookView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
		}
		//
		findViewById(R.id.rela_change_password).setOnClickListener(this);
		findViewById(R.id.settings_user_logout_btn).setOnClickListener(this);
		checkBindPhone();
	}

	private void initTitle() {
		mBack = findViewById(R.id.back);
		mBack.setVisibility(View.VISIBLE);
		mBack.setOnClickListener(this);
		mTitleTextView = (TextView) findViewById(R.id.titleContent);
		mTitleTextView.setVisibility(View.VISIBLE);
		mTitleTextView.setText(getResources().getString(R.string.settings_tacoty_account_info_title));
	}

	protected void failure(JSONObject obj) {
		DialogUtil.createMsgDialog(this, getResources().getString(R.string.login_error), getResources().getString(R.string.ok)).show();
		finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.choosebutton:
			startActivity(new Intent(this, AddFriendActivity.class));
			break;
		case R.id.settings_account_tacotyid_pandle:
			startActivity(new Intent(this, AccountInfoActivity.class));
			break;
		case R.id.settings_user_logout_btn:
			PrefsUtils.User.cleanUserInfoLogin(this);
			Intent loginIntent = new Intent(this, InitPageActivity.class);
			loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(loginIntent);
			Process.killProcess(Process.myPid());
			break;

		case R.id.account_bind_number:
			break;
		case R.id.account_facebook_id:
			startActivityForResult(new Intent(this, UnbindFacebookActivity.class), REQUEST_CODE_UNBIND_FACEBOOK);
			break;
		case R.id.rela_change_password:
			startActivity(new Intent(this, ChangePasswordActivity.class));
			break;

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_CODE_UNBIND_FACEBOOK:
			if (RESULT_OK == resultCode) {
				mFacebookView.setText(getResources().getString(R.string.setting_update_facebook_unbind));
				mFacebookView.setCompoundDrawables(null, null, null, null);
			}
			break;

		}

	}

	public void checkBindPhone() {
		if (!TextUtils.isEmpty(mUserInfo.getPhone())) {
			setPhoneNumer(mUserInfo.getPhone());
			return;
		}
		mTask = new GetBindPhoneTask(this, new OnBindSuccessListener() {

			@Override
			public void onBindSuccess(String phoneNumber) {
				mUserInfo.setPhone(phoneNumber);
				setPhoneNumer(phoneNumber);
				PrefsUtils.User.saveBindedPhoneNumber(getApplicationContext(), phoneNumber, mUserInfo.getEmail());
			}

			@Override
			public void onBindFail() {
			}
		});
		mTask.start();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mTask != null)
			mTask.cancel();
	}

	private void setPhoneNumer(String phoneNumber) {
		mBindPhoneTextView.setText(phoneNumber);
		mBindPhoneTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
	}

}
