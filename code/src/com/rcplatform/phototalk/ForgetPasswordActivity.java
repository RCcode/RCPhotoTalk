package com.rcplatform.phototalk;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.api.PhotoTalkApiUrl;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.request.Request;
import com.rcplatform.phototalk.umeng.EventUtil;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.RCPlatformTextUtil;

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
				final String email = mEmailEditTextView.getText().toString();
				if (checkEmail(email)) {

					AlertDialog.Builder dialogBuilder = DialogUtil.getAlertDialogBuilder(this);
					dialogBuilder.setTitle(R.string.register_confirm_email_address).setMessage(email).setCancelable(false)
					        .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {

						        @Override
						        public void onClick(DialogInterface dialog, int which) {
							        dialog.cancel();
							        EventUtil.Register_Login_Invite.rcpt_resetpassword(baseContext);
							        doGetPassword(email);
						        }
					        }).setNegativeButton(getResources().getString(R.string.modify), new DialogInterface.OnClickListener() {

						        @Override
						        public void onClick(DialogInterface dialog, int which) {

						        }
					        });
					dialogBuilder.create().show();

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
			DialogUtil.createMsgDialog(this, getResources().getString(R.string.registe_email_error), getResources().getString(R.string.confirm))
			        .show();
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
				DialogUtil.showToast(getApplicationContext(), resetMsg, Toast.LENGTH_LONG);
				finish();
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
