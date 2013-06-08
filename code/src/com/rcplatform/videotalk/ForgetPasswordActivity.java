package com.rcplatform.videotalk;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.rcplatform.videotalk.R;
import com.rcplatform.videotalk.activity.BaseActivity;
import com.rcplatform.videotalk.api.PhotoTalkApiUrl;
import com.rcplatform.videotalk.request.RCPlatformResponseHandler;
import com.rcplatform.videotalk.request.Request;
import com.rcplatform.videotalk.umeng.EventUtil;
import com.rcplatform.videotalk.utils.DialogUtil;
import com.rcplatform.videotalk.utils.RCPlatformTextUtil;

public class ForgetPasswordActivity extends BaseActivity implements View.OnClickListener {

	protected static final String TAG = "ForgetPasswordActivity";

	private Button mGetPasswordBtn;

	private EditText mEmailEditTextView;

	private String mEmail;

	private View mBack;

	private TextView mTitleTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.forget_password);
		initTitle();
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
				EventUtil.Register_Login_Invite.rcpt_resetpassword(baseContext);
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
		Request request = new Request(this, PhotoTalkApiUrl.FORGET_PASSWORD_URL, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				dismissLoadingDialog();
				String resetMsg = String.format(getResources().getString(R.string.forget_password_reset_text), mEmail);
				DialogUtil.createMsgDialog(ForgetPasswordActivity.this, resetMsg, getString(R.string.confirm)).show();
			}

			@Override
			public void onFailure(int errorCode, String content) {
				dismissLoadingDialog();
				showErrorConfirmDialog(content);
			}
		});
		request.putParam("email", email);
		request.excuteAsync();
	}
}