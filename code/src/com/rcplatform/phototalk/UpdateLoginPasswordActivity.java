package com.rcplatform.phototalk;

import java.util.Locale;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rcplatform.phototalk.api.MenueApiFactory;
import com.rcplatform.phototalk.api.MenueApiUrl;
import com.rcplatform.phototalk.galhttprequest.GalHttpRequest;
import com.rcplatform.phototalk.galhttprequest.GalHttpRequest.GalHttpLoadTextCallBack;
import com.rcplatform.phototalk.galhttprequest.MD5;
import com.rcplatform.phototalk.utils.Contract;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.PrefsUtils;

public class UpdateLoginPasswordActivity extends Activity implements View.OnClickListener {

	protected static final String TAG = "ForgetPasswordActivity";

	public static final int RESULTCODE_TAG_SIGNATURE = 7000;

	public static final int RESULTCODE_TAG_NAME = 7100;

	private Button mConfirmBtn;

	private ProgressBar mProgressbar;

	private EditText mCheckEditTextView;

	private String editName;

	/* 密码不正确 */
	private static final int RESPONSE_STATE_SUCCESS_PSW_INVLIDE = 1;

	private Handler mCheckHandler = new MenueHandler(this) {

		@Override
		public void handleMessage(Message msg) {
			mProgressbar.setVisibility(View.GONE);
			switch (msg.what) {
				case MenueApiFactory.RESPONSE_STATE_SUCCESS:
					mCheckPswView.setVisibility(View.GONE);
					mConfirmBtn.setVisibility(View.GONE);
					mUpdatePswView.setVisibility(View.VISIBLE);
					mTitleTextView.setText(getResources().getString(R.string.setting_update_set_newlogin_password));// ?
					break;
				case RESPONSE_STATE_SUCCESS_PSW_INVLIDE:
					showDialog(UpdateLoginPasswordActivity.this, getResources().getString(R.string.setting_update_password_invalid));
					break;
			}
		}

	};

	private Handler mUpdateHandler = new MenueHandler(this) {

		@Override
		public void handleMessage(Message msg) {
			mProgressbar.setVisibility(View.GONE);
			switch (msg.what) {
				case MenueApiFactory.RESPONSE_STATE_SUCCESS:
					AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(UpdateLoginPasswordActivity.this);
					dialogBuilder.setMessage(getResources().getString(R.string.setting_update_password_success)).setCancelable(false)
					        .setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {

						        public void onClick(DialogInterface dialog, int which) {
							        finish();
						        }
					        });
					dialogBuilder.create().show();
					break;
			}
		}

	};

	private View mBack;

	private TextView mTitleTextView;

	private TextView mUpdateLabelHint;

	private View mCheckPswView;

	private View mUpdatePswView;

	private EditText mPswView1;

	private EditText mPswView2;

	private Button mModifyBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setting_update_password_activity);
		mBack = findViewById(R.id.back);
		mBack.setVisibility(View.VISIBLE);
		mBack.setOnClickListener(this);
		mTitleTextView = (TextView) findViewById(R.id.titleContent);
		mTitleTextView.setVisibility(View.VISIBLE);
		mTitleTextView.setText(getResources().getString(R.string.setting_update_login_password));

		//
		mCheckPswView = findViewById(R.id.check_psw_layout);

		mUpdatePswView = findViewById(R.id.update_psw_layout);
		mPswView1 = (EditText) findViewById(R.id.settings_psw_1);
		mPswView2 = (EditText) findViewById(R.id.settings_psw_2);

		//
		mUpdateLabelHint = (TextView) findViewById(R.id.update_hint);// ?
		mUpdateLabelHint.setText(getResources().getString(R.string.setting_update_check_password_hint));

		mCheckEditTextView = (EditText) findViewById(R.id.login_password);

		findViewById(R.id.login_page_forget_password_button).setOnClickListener(this);
		mConfirmBtn = (Button) findViewById(R.id.settings_update_confirm_button);
		mConfirmBtn.setOnClickListener(this);
		mModifyBtn = (Button) findViewById(R.id.settings_update_psw_button);
		mModifyBtn.setOnClickListener(this);
		mProgressbar = (ProgressBar) findViewById(R.id.login_progressbar);
	}

	private void showDialog(final Context context, String msg) {
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
		dialogBuilder.setMessage(msg).setCancelable(false)
		        .setPositiveButton(context.getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {

			        public void onClick(DialogInterface dialog, int which) {

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
				String label = mCheckEditTextView.getText().toString();
				checkPsw(label);
				break;
			case R.id.settings_update_psw_button:
				String psw1 = mPswView1.getText().toString();
				String psw2 = mPswView2.getText().toString();
				// 请输入新密码
				if (TextUtils.isEmpty(psw1)) {
					showDialog(this, getResources().getString(R.string.setting_update_new_password));
					return;
				}
				// 请输入确认密码
				if (TextUtils.isEmpty(psw2)) {
					showDialog(this, getResources().getString(R.string.setting_update_new_confirm_password));
					return;
				}
				// 两次密码输入不一致，请重新输入
				if (!psw1.equals(psw2)) {
					showDialog(this, getResources().getString(R.string.setting_update_twice_password_invalid));
					return;
				}

				updatePsw(psw2);
				break;
			case R.id.login_page_forget_password_button:
				startActivity(new Intent(this, ForgetPasswordActivity.class).setAction("action_update_forget_password"));
				break;

		}
	}

	private void updatePsw(String psw) {
		GalHttpRequest request = GalHttpRequest.requestWithURL(this, MenueApiUrl.UPDATE_LOGIN_PASSWORD_URL);
		request.setPostValueForKey(MenueApiFactory.USERID, PrefsUtils.LoginState.getLoginUser(this).getSuid());
		request.setPostValueForKey(MenueApiFactory.TOKEN, MenueApiFactory.TOKEN_DEFAULT);
		request.setPostValueForKey(MenueApiFactory.NEW_PWD, MD5.md5Hash(psw));
		request.setPostValueForKey(MenueApiFactory.LANGUAGE, Locale.getDefault().getLanguage());
		request.setPostValueForKey(MenueApiFactory.APP_ID, Contract.APP_ID);
		request.setPostValueForKey(MenueApiFactory.DEVICE_ID, android.os.Build.DEVICE);
		request.startAsynRequestString(new GalHttpLoadTextCallBack() {

			@Override
			public void textLoaded(String text) {
				System.out.println(text.toString());
				try {
					System.out.println(text);
					JSONObject obj = new JSONObject(text);
					int state = obj.getInt(MenueApiFactory.RESPONSE_KEY_STATUS);
					mUpdateHandler.sendMessage(mUpdateHandler.obtainMessage(state));
				}
				catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void loadFail() {
				mUpdateHandler.sendMessage(mUpdateHandler.obtainMessage(MenueApiFactory.LOGIN_SERVER_ERROR));
			}
		});
	}

	private void checkPassword() {
		String password = "";

	}

	private void checkPsw(String password) {
		GalHttpRequest request = GalHttpRequest.requestWithURL(this, MenueApiUrl.CHECK_LOGIN_PASSWORD_URL);
		request.setPostValueForKey(MenueApiFactory.USERID, PrefsUtils.LoginState.getLoginUser(this).getSuid());
		request.setPostValueForKey(MenueApiFactory.TOKEN, MenueApiFactory.TOKEN_DEFAULT);
		request.setPostValueForKey(MenueApiFactory.PWD, MD5.md5Hash(password));
		request.setPostValueForKey(MenueApiFactory.LANGUAGE, Locale.getDefault().getLanguage());
		request.setPostValueForKey(MenueApiFactory.APP_ID, Contract.APP_ID);
		request.setPostValueForKey(MenueApiFactory.DEVICE_ID, android.os.Build.DEVICE);
		request.startAsynRequestString(new GalHttpLoadTextCallBack() {

			@Override
			public void textLoaded(String text) {
				System.out.println(text.toString());
				try {
					System.out.println(text);
					JSONObject obj = new JSONObject(text);
					int state = obj.getInt(MenueApiFactory.RESPONSE_KEY_STATUS);
					mCheckHandler.sendMessage(mCheckHandler.obtainMessage(state));
				}
				catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void loadFail() {
				mCheckHandler.sendMessage(mCheckHandler.obtainMessage(MenueApiFactory.LOGIN_SERVER_ERROR));
			}
		});
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
