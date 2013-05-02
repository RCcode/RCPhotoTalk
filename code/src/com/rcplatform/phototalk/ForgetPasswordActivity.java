package com.rcplatform.phototalk;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.api.MenueApiFactory;
import com.rcplatform.phototalk.api.MenueApiUrl;
import com.rcplatform.phototalk.api.PhotoTalkParams;
import com.rcplatform.phototalk.api.RCPlatformAsyncHttpClient;
import com.rcplatform.phototalk.api.RCPlatformAsyncHttpClient.RequestAction;
import com.rcplatform.phototalk.api.RCPlatformResponse;
import com.rcplatform.phototalk.api.RCPlatformResponseHandler;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.RCPlatformTextUtil;
import com.rcplatform.phototalk.utils.ShowToast;

public class ForgetPasswordActivity extends BaseActivity implements View.OnClickListener {

	protected static final String TAG = "ForgetPasswordActivity";

	private Button mGetPasswordBtn;

	private EditText mEmailEditTextView;

	private String mEmail;

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case RCPlatformResponse.ResponseStatus.RESPONSE_VALUE_SUCCESS:

				break;
			// 密码错误
			case MenueApiFactory.LOGIN_PASSWORD_ERROR:
				ShowToast.showToast(ForgetPasswordActivity.this, getResources().getString(R.string.reg_pwd_no_email_yes), Toast.LENGTH_LONG);
				break;
			// 邮箱没有注册
			case MenueApiFactory.LOGIN_EMAIL_ERROR:
				ShowToast.showToast(ForgetPasswordActivity.this, getResources().getString(R.string.reg_email_no), Toast.LENGTH_LONG);
				break;
			// 服务器异常
			case MenueApiFactory.LOGIN_SERVER_ERROR:
				ShowToast.showToast(ForgetPasswordActivity.this, getResources().getString(R.string.reg_server_no), Toast.LENGTH_LONG);
				break;
			// 管理员不允许客户端登录
			case MenueApiFactory.LOGIN_ADMIN_ERROR:
				ShowToast.showToast(ForgetPasswordActivity.this, getResources().getString(R.string.reg_admin_no), Toast.LENGTH_LONG);
				break;
			}

		}

	};

	private View mBack;

	private TextView mTitleTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.forget_password);
		initTitle();
		httpClient = new RCPlatformAsyncHttpClient(RequestAction.JSON);
		mEmailEditTextView = (EditText) findViewById(R.id.settings_update_edit);
		mGetPasswordBtn = (Button) findViewById(R.id.forget_password_confirm_button);
		mGetPasswordBtn.setOnClickListener(this);
	}

	private void initTitle() {
		mBack = findViewById(R.id.back);
		mBack.setVisibility(View.VISIBLE);
		mBack.setOnClickListener(this);
		mTitleTextView = (TextView) findViewById(R.id.titleContent);
		mTitleTextView.setText(getResources().getString(R.string.forget_password_title));
		mTitleTextView.setVisibility(View.VISIBLE);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.forget_password_confirm_button:
			String email = mEmailEditTextView.getText().toString();
			if (checkEmail(email)) {
				doGetPassword(email);
			}
			break;

		}
	}

	/*
	 * 检查email的格式
	 */
	private boolean checkEmail(String email) {
		// 邮箱正则表达式
		if (TextUtils.isEmpty(email)) {
			DialogUtil.createMsgDialog(this, getString(R.string.registe_email_empty), getString(R.string.confirm)).show();
			return false;
		}
		if (!RCPlatformTextUtil.isEmailMatches(email)) {
			DialogUtil.createMsgDialog(this, getResources().getString(R.string.registe_email_error), getResources().getString(R.string.confirm)).show();
			return false;
		}
		return true;
	}

	private void doGetPassword(String email) {
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		mEmail = email;
		httpClient.clearParams();
		PhotoTalkParams.buildBasicParams(this, httpClient);
		httpClient.putRequestParam("email", email);
		httpClient.post(this, MenueApiUrl.FORGET_PASSWORD_URL, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				// TODO Auto-generated method stub
				dismissLoadingDialog();
				String resetMsg = String.format(getResources().getString(R.string.forget_password_reset_text), mEmail);
				DialogUtil.createMsgDialog(ForgetPasswordActivity.this, resetMsg, getString(R.string.confirm));
			}

			@Override
			public void onFailure(int errorCode, String content) {
				dismissLoadingDialog();
				showErrorConfirmDialog(content);
			}
		});
	}
}
