package com.rcplatform.phototalk;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.galhttprequest.MD5;
import com.rcplatform.phototalk.proxy.UserSettingProxy;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.utils.RCPlatformTextUtil;

public class ChangePasswordActivity extends BaseActivity implements OnClickListener {

	private ViewSwitcher mViewSwitcher;
	private EditText etPassword;
	private EditText etNewPassword;
	private EditText etConfirmPassword;
	private Button btnConfirm;
	private Button btnConfirmPassword;

	private TextView tvForget;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.change_password);
		initView();
	}

	private void initView() {
		initBackButton(R.string.change_password, this);
		mViewSwitcher = (ViewSwitcher) findViewById(R.id.vs_change);
		etPassword = (EditText) findViewById(R.id.et_password_current);
		etNewPassword = (EditText) findViewById(R.id.et_new_password);
		etConfirmPassword = (EditText) findViewById(R.id.et_confirm_password);
		btnConfirm = (Button) findViewById(R.id.btn_current_confirm);
		btnConfirmPassword = (Button) findViewById(R.id.btn_confirm_password);
		tvForget = (TextView) findViewById(R.id.tv_forget_password);
		tvForget.setOnClickListener(this);
		btnConfirmPassword.setOnClickListener(this);
		btnConfirm.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_current_confirm:
			commitCurrentPassword(etPassword.getText().toString().trim());
			break;
		case R.id.tv_forget_password:
			startActivity(ForgetPasswordActivity.class);
			break;
		case R.id.btn_confirm_password:
			commitNewPassword(etNewPassword.getText().toString().trim(), etConfirmPassword.getText().toString());
			break;
		case R.id.title_linear_back:
			finish();
			break;
		}
	}

	private void commitCurrentPassword(String currentPassword) {
		if (TextUtils.isEmpty(currentPassword)) {
			showErrorConfirmDialog(R.string.registe_password_empty);
			return;
		}
		if (!RCPlatformTextUtil.isPasswordMatches(currentPassword)) {
			showErrorConfirmDialog(R.string.register_password_error);
			return;
		}
		UserSettingProxy.checkCurrentPassword(this, new RCPlatformResponseHandler() {
			@Override
			public void onSuccess(int statusCode, String content) {
				mViewSwitcher.showNext();
			}

			@Override
			public void onFailure(int errorCode, String content) {
				showErrorConfirmDialog(content);
			}
		}, MD5.md5Hash(currentPassword));
	}

	private void commitNewPassword(String newPassword, String passwordConfirm) {
		if (TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(passwordConfirm)) {
			showErrorConfirmDialog(R.string.registe_password_empty);
			return;
		}
		if (!RCPlatformTextUtil.isPasswordMatches(newPassword) || !RCPlatformTextUtil.isPasswordMatches(passwordConfirm)) {
			showErrorConfirmDialog(R.string.register_password_error);
			return;
		}
		if (!newPassword.equals(passwordConfirm)) {
			showErrorConfirmDialog(R.string.two_password_not_same);
			return;
		}

		UserSettingProxy.changePassword(this, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				showErrorConfirmDialog(R.string.password_change_complete);
				finish();
			}

			@Override
			public void onFailure(int errorCode, String content) {
				showErrorConfirmDialog(content);
			}
		}, MD5.md5Hash(newPassword));
	}
}
