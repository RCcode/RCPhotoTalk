package com.rcplatform.phototalk;

import org.json.JSONObject;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.galhttprequest.MD5;
import com.rcplatform.phototalk.galhttprequest.RCPlatformServiceError;
import com.rcplatform.phototalk.logic.LogicUtils;
import com.rcplatform.phototalk.proxy.UserSettingProxy;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.umeng.EventUtil;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.PrefsUtils;
import com.rcplatform.phototalk.utils.RCPlatformTextUtil;

public class ChangePasswordActivity extends BaseActivity implements OnClickListener {

	private ViewSwitcher mViewSwitcher;

	private EditText etPassword;

	private EditText etNewPassword;

	private EditText etConfirmPassword;

	private Button btnConfirm;

	private Button btnConfirmPassword;

	private TextView tvForget;

	private String mCurrentPassword;

	private OnClickListener listener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.change_password);
		initView();
		listener = this;
	}

	private void initView() {
		initBackButton(R.string.change_password, this);
		mViewSwitcher = (ViewSwitcher) findViewById(R.id.vs_change);
		etPassword = (EditText) findViewById(R.id.et_password_current);
		etNewPassword = (EditText) findViewById(R.id.et_new_password);
		etNewPassword.setHint(this.getResources().getString(R.string.change_password_new_password_hint));
		etConfirmPassword = (EditText) findViewById(R.id.et_confirm_password);
		etConfirmPassword.setHint(this.getResources().getString(R.string.change_password_confirm_password_hint));
		btnConfirm = (Button) findViewById(R.id.btn_current_confirm);
		btnConfirmPassword = (Button) findViewById(R.id.btn_confirm_password);
		tvForget = (TextView) findViewById(R.id.tv_forget_password);
		tvForget.setOnClickListener(this);
		btnConfirmPassword.setOnClickListener(this);
		btnConfirm.setOnClickListener(this);

		etNewPassword.setHintTextColor(getResources().getColor(R.color.register_input_hint));

		etNewPassword.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				etNewPassword.setHintTextColor(getResources().getColor(R.color.register_input_hint));
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}
		});

		etConfirmPassword.setHintTextColor(getResources().getColor(R.color.register_input_hint));

		etConfirmPassword.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				etConfirmPassword.setHintTextColor(getResources().getColor(R.color.register_input_hint));
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}
		});

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
			EventUtil.More_Setting.rcpt_success_changepassword(baseContext);
			commitNewPassword(etNewPassword.getText().toString().trim(), etConfirmPassword.getText().toString().trim());
			break;
		case R.id.back:
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
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		mCurrentPassword = MD5.encodeMD5String(currentPassword);
		UserSettingProxy.checkCurrentPassword(this, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				dismissLoadingDialog();
				mViewSwitcher.showNext();
				initBackButton(R.string.change_password_edit_title, listener);
			}

			@Override
			public void onFailure(int errorCode, String content) {
				dismissLoadingDialog();
				showErrorConfirmDialog(content);
			}
		}, mCurrentPassword);
	}

	private void commitNewPassword(String newPassword, String passwordConfirm) {

		if (newPassword.equals("")) {
			etNewPassword.setHintTextColor(getResources().getColor(R.color.register_input_hint_error));
			etNewPassword.requestFocus();
			InputMethodManager imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
			imm.showSoftInput(etNewPassword, 0);
			return;
		}

		if (passwordConfirm.equals("")) {
			etConfirmPassword.setHintTextColor(getResources().getColor(R.color.register_input_hint_error));
			etConfirmPassword.requestFocus();
			InputMethodManager imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
			imm.showSoftInput(etConfirmPassword, 0);
			return;
		}

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
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		UserSettingProxy.changePassword(this, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				try {
					DialogUtil.showToast(getApplicationContext(), R.string.password_change_complete, Toast.LENGTH_SHORT);
					JSONObject jsonObject = new JSONObject(content);
					String token = jsonObject.getString("token");
					String tgpwd = jsonObject.getString("tgpwd");
					UserInfo userInfo = getCurrentUser();
					userInfo.setToken(token);
					userInfo.setTigasePwd(tgpwd);
					PrefsUtils.LoginState.setLoginUser(ChangePasswordActivity.this, userInfo);
					LogicUtils.logout(ChangePasswordActivity.this);
				} catch (Exception e) {
					e.printStackTrace();
					onFailure(RCPlatformServiceError.ERROR_CODE_REQUEST_FAIL, getString(R.string.net_error));
				}
			}

			@Override
			public void onFailure(int errorCode, String content) {
				dismissLoadingDialog();
				showErrorConfirmDialog(content);
			}
		}, MD5.encodeMD5String(newPassword), mCurrentPassword);
	}
}
