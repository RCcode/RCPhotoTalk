package com.rcplatform.videotalk;

import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.rcplatform.videotalk.R;
import com.rcplatform.videotalk.activity.BaseActivity;
import com.rcplatform.videotalk.utils.Constants;
import com.rcplatform.videotalk.utils.DialogUtil;
import com.rcplatform.videotalk.utils.RCPlatformTextUtil;

// github.com/RCcode/RCPhotoTalk.git

public class UpdateNameActivity extends BaseActivity implements View.OnClickListener {

	protected static final String TAG = "ForgetPasswordActivity";

	public static final String REQUEST_PARAM_KEY_TEXT = "text";

	private Button mConfirmBtn;

	private ProgressBar mProgressbar;

	private EditText mEditTextView;

	private String currentText;

	private static final int REQUEST_TYPE_SIGNTURE = 1;

	private static final int REQUEST_TYPE_NICK = 0;

	private int requestType = -1;

	private View mBack;

	private TextView mTitleTextView;

	private TextView mUpdateLabelHint;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_update_name_activity);
		mBack = findViewById(R.id.back);
		mBack.setVisibility(View.VISIBLE);
		mBack.setOnClickListener(this);
		mTitleTextView = (TextView) findViewById(R.id.titleContent);
		mTitleTextView.setVisibility(View.VISIBLE);
		mUpdateLabelHint = (TextView) findViewById(R.id.update_hint);
		mUpdateLabelHint.setVisibility(View.GONE);
		mEditTextView = (EditText) findViewById(R.id.settings_update_edit);

		mEditTextView.setHintTextColor(getResources().getColor(R.color.register_input_hint));

		mEditTextView.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				mEditTextView.setHintTextColor(getResources().getColor(R.color.register_input_hint));
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}
		});

		Intent intent = getIntent();
		if (intent.getAction().equals("setting_update_name")) {
			requestType = REQUEST_TYPE_NICK;
			mTitleTextView.setText(getResources().getString(R.string.settings_update_name_title));
			mUpdateLabelHint.setText(getResources().getString(R.string.settings_update_name_limited_hint));
			mEditTextView.setFilters(new InputFilter[] { new InputFilter.LengthFilter(RCPlatformTextUtil.NICK_MAX_LENGTH) });
			mEditTextView.setHint(R.string.login_nick_hint);
		} else if (intent.getAction().equals("setting_update_signature")) {
			requestType = REQUEST_TYPE_SIGNTURE;
			mTitleTextView.setText(getResources().getString(R.string.settings_update_signature_title));
			mUpdateLabelHint.setText(getResources().getString(R.string.settings_update_signature_limited_hint));
			mEditTextView.setFilters(new InputFilter[] { new InputFilter.LengthFilter(RCPlatformTextUtil.SIGNTURE_MAX_LENGTH) });
		}
		currentText = intent.getStringExtra(REQUEST_PARAM_KEY_TEXT);
		mEditTextView.setText(currentText);
		mConfirmBtn = (Button) findViewById(R.id.settings_update_confirm_button);
		mConfirmBtn.setOnClickListener(this);
		mProgressbar = (ProgressBar) findViewById(R.id.login_progressbar);
	}

	private void showDialog(final Context context, String msg) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
		dialogBuilder.setMessage(msg).setCancelable(false)
		        .setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {

			        public void onClick(DialogInterface dialog, int which) {

				        Intent loginIntent = new Intent(context, LoginActivity.class);
				        loginIntent.putExtra(Constants.KEY_LOGIN_PAGE, true);
				        startActivity(loginIntent);
				        finish();
			        }
		        });
		dialogBuilder.create().show();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.back:
				finish();
				break;
			case R.id.settings_update_confirm_button:

				String label = mEditTextView.getText().toString();
				// 如何未修改直接退出。
				if (currentText.equals(label)) {
					finish();
				} else {
					// 验证输入字符无效
					if (label.equals("")) {
						mEditTextView.setHintTextColor(getResources().getColor(R.color.register_input_hint_error));
						mEditTextView.requestFocus();
						InputMethodManager imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
						imm.showSoftInput(mEditTextView, 0);
						return;
					}
					if (isNewTextRight(label)) {
						Intent data = new Intent();
						data.putExtra("result", label);
						setResult(Activity.RESULT_OK, data);
						finish();
					}
				}
				break;

		}
	}

	private boolean isNewTextRight(String text) {
		// TODO Auto-generated method stub
		boolean isRight = false;
		switch (requestType) {
			case REQUEST_TYPE_NICK:
				isRight = checkNickName(text);
				break;
			case REQUEST_TYPE_SIGNTURE:
				isRight = RCPlatformTextUtil.isSigntureMatches(text);
				if (!isRight)
					DialogUtil.showToast(getApplicationContext(), R.string.text_too_long, Toast.LENGTH_SHORT);
				break;
		}
		return isRight;
	}

	private boolean checkNickName(String nick) {
		if (TextUtils.isEmpty(nick.trim())) {
			DialogUtil.showToast(getApplicationContext(), R.string.register_nick_length_error, Toast.LENGTH_SHORT);
			return false;
		}
		if (!RCPlatformTextUtil.isNickMatches(nick)) {
			DialogUtil.showToast(getApplicationContext(), R.string.register_nick_empty, Toast.LENGTH_SHORT);
			return false;
		}
		return true;
	}

	private boolean checkName(String label) {
		final int PWD_MIN_SIZE = 1;
		final int PWD_MAX_SIZE = 20;
		if (PWD_MIN_SIZE > label.length() || PWD_MAX_SIZE < label.length()) {
			DialogUtil.createMsgDialog(this, getResources().getString(R.string.setting_update_name_invalid),
			                           getResources().getString(android.R.string.ok)).show();
			return false;
		}
		return true;
	}

	/*
	 * 检查email的格式
	 */
	private boolean checkEmail(String emailId) {
		// 邮箱正则表达式
		Pattern emailPattern = Pattern
		        .compile("^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$");
		final int SMLL_SIZE = 0;
		if (SMLL_SIZE == emailId.length() || !emailPattern.matcher(emailId).matches()) {
			DialogUtil.createMsgDialog(this, getResources().getString(R.string.register_noinput_address_info),
			                           getResources().getString(android.R.string.ok)).show();
			return false;
		} else {
			return true;
		}
	}

}
