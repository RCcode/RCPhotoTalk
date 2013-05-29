package com.rcplatform.phototalk;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.proxy.UserSettingProxy;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.utils.PrefsUtils;

public class BindPhoneActivity extends BaseActivity implements OnClickListener {
	private static final int SMS_SEND_SPACE = 60;

	public static final String REQUEST_PAMAM_NUMBER = "cellphone_number";

	private Button btnResend;
	private Button btnChnage;
	private Button btnCommit;
	private EditText etValidate;
	private String mNumberTemp;
	private AsyncTask<Void, Void, Void> mCountDownTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bind_phone);
		initData();
		initView();
		mCountDownTask = new CountDownTask().execute();
	}

	private void initData() {
		mNumberTemp = getIntent().getStringExtra(REQUEST_PAMAM_NUMBER);
	}

	private void initView() {
		btnResend = (Button) findViewById(R.id.btn_resend);
		btnChnage = (Button) findViewById(R.id.btn_change_number);
		btnCommit = (Button) findViewById(R.id.btn_commit);
		etValidate = (EditText) findViewById(R.id.et_validate);
		btnResend.setOnClickListener(this);
		btnChnage.setOnClickListener(this);
		btnCommit.setOnClickListener(this);
		initBackButton(R.string.phone_number_validate, this);
	}

	private class CountDownTask extends AsyncTask<Void, Void, Void> {
		private int waitSecond = 0;

		protected void onPreExecute() {
			super.onPreExecute();
			btnResend.setEnabled(false);
		};

		@Override
		protected Void doInBackground(Void... params) {
			while (waitSecond < SMS_SEND_SPACE && !isCancelled()) {
				try {
					Thread.sleep(1000);
					publishProgress();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
			btnResend.setText(getString(R.string.resend_sms_second, SMS_SEND_SPACE - (waitSecond++)));
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			btnResend.setText(R.string.resend_sms);
			btnResend.setEnabled(true);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_resend:
			requestReSendSMS();
			break;
		case R.id.btn_commit:
			sendValidate();
			break;
		case R.id.btn_change_number:
		case R.id.title_linear_back:
			finish();
			break;
		}
	}

	private void sendValidate() {
		String validate = etValidate.getText().toString();
		if (isValidateEnable(validate)) {
			showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
			UserSettingProxy.bindPhone(this, new RCPlatformResponseHandler() {

				@Override
				public void onSuccess(int statusCode, String content) {
					dismissLoadingDialog();
					getCurrentUser().setCellPhone(mNumberTemp);
					PrefsUtils.User.MobilePhoneBind.saveBindedPhoneNumber(getApplicationContext(), mNumberTemp, getCurrentUser().getRcId());
					finishToUserInfoActivity();
				}

				@Override
				public void onFailure(int errorCode, String content) {
					dismissLoadingDialog();
					showErrorConfirmDialog(content);
				}
			}, validate,mNumberTemp);
		}
	}

	private boolean isValidateEnable(String validate) {
		if (TextUtils.isEmpty(validate) || validate.length() < 4) {
			showErrorConfirmDialog(R.string.input_correct_validate);
			return false;
		}
		return true;
	}

	private void finishToUserInfoActivity() {
		Intent intent = new Intent(this, UserInfoActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mCountDownTask != null && mCountDownTask.getStatus() == Status.RUNNING)
			mCountDownTask.cancel(true);
	}

	private void requestReSendSMS() {
		UserSettingProxy.requestSMS(this, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				showErrorConfirmDialog(R.string.sms_sended);
				mCountDownTask = new CountDownTask().execute();
			}

			@Override
			public void onFailure(int errorCode, String content) {
				showErrorConfirmDialog(content);
			}
		}, mNumberTemp);
	}
}
