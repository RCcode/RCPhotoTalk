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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.rcplatform.phototalk.api.MenueApiFactory;
import com.rcplatform.phototalk.api.MenueApiUrl;
import com.rcplatform.phototalk.galhttprequest.GalHttpRequest;
import com.rcplatform.phototalk.galhttprequest.GalHttpRequest.GalHttpLoadTextCallBack;
import com.rcplatform.phototalk.thirdpart.utils.ThirdPartAccessTokenKeeper;
import com.rcplatform.phototalk.utils.Contract;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.FacebookUtil;
import com.rcplatform.phototalk.utils.PrefsUtils;

public class UnbindFacebookActivity extends Activity implements View.OnClickListener {

	protected static final String TAG = "ForgetPasswordActivity";

	public static final int RESULTCODE_TAG_SIGNATURE = 7000;

	public static final int RESULTCODE_TAG_NAME = 7100;

	private ProgressBar mProgressbar;

	/* 密码不正确 */
	private static final int RESPONSE_STATE_NOT_AVAILABLE_TACOTYID = 1;

	private Handler mCraeteTacotyIdHandler = new MenueHandler(this) {

		@Override
		public void handleMessage(Message msg) {
			mProgressbar.setVisibility(View.GONE);
			switch (msg.what) {
				case MenueApiFactory.RESPONSE_STATE_SUCCESS:
					Intent data = new Intent();
					data.putExtra("result", mFacebookIdTextView.getText().toString());
					setResult(RESULT_OK, data);
					finish();
					break;
				case RESPONSE_STATE_NOT_AVAILABLE_TACOTYID:
					showDialog(UnbindFacebookActivity.this, getResources().getString(R.string.setting_update_not_available_tacotyid));
					break;
			}
		}

	};

	private View mBack;

	private TextView mTitleTextView;

	private TextView mFacebookIdTextView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_unbind_facebook_activity);
		mBack = findViewById(R.id.back);
		mBack.setVisibility(View.VISIBLE);
		mBack.setOnClickListener(this);
		mTitleTextView = (TextView) findViewById(R.id.titleContent);
		mTitleTextView.setVisibility(View.VISIBLE);
		mTitleTextView.setText(getResources().getString(R.string.setting_update_facebook_unbind_title));

		mFacebookIdTextView = (TextView) findViewById(R.id.settings_facebook_account_name);
		if (!TextUtils.isEmpty(ThirdPartAccessTokenKeeper.getFacebookLoginName(this))) {
			mFacebookIdTextView.setText(ThirdPartAccessTokenKeeper.getFacebookLoginName(this) + "");
		}
		findViewById(R.id.settings_update_confirm_button).setOnClickListener(this);
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
				FacebookUtil.clearFacebookVlidated(this);
				setResult(RESULT_OK);
				finish();
				break;

		}
	}

	private void createTacotyId(String tacotyid) {
		GalHttpRequest request = GalHttpRequest.requestWithURL(this, MenueApiUrl.CREATE_TACOTY_ID_URL);
		request.setPostValueForKey(MenueApiFactory.USERID, PrefsUtils.LoginState.getLoginUser(this).getSuid());
		request.setPostValueForKey(MenueApiFactory.TOKEN, MenueApiFactory.TOKEN_DEFAULT);
		request.setPostValueForKey(MenueApiFactory.TACOTYID, tacotyid);
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
					mCraeteTacotyIdHandler.sendMessage(mCraeteTacotyIdHandler.obtainMessage(state));
				}
				catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void loadFail() {
				mCraeteTacotyIdHandler.sendMessage(mCraeteTacotyIdHandler.obtainMessage(MenueApiFactory.LOGIN_SERVER_ERROR));
			}
		});
	}

	private void checkPassword() {
		String password = "";

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
