package com.rcplatform.videotalk;

import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.rcplatform.videotalk.R;
import com.rcplatform.videotalk.activity.BaseActivity;
import com.rcplatform.videotalk.bean.CountryCode;
import com.rcplatform.videotalk.db.CountryCodeDatabase;
import com.rcplatform.videotalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.videotalk.proxy.UserSettingProxy;
import com.rcplatform.videotalk.request.RCPlatformResponseHandler;
import com.rcplatform.videotalk.umeng.EventUtil;
import com.rcplatform.videotalk.utils.Constants;
import com.rcplatform.videotalk.utils.PrefsUtils;

public class RequestSMSActivity extends BaseActivity implements OnClickListener {

	private static final int REQUEST_CODE_BIND = 100;

	private EditText etNumber;

	private Button btnCountryCode;

	private AlertDialog mCountryChooseDialog;

	private CountryCode mCountryCode;

	private List<CountryCode> allCountryCodes;

	private CountryCodeDatabase mCountryCodeDatabase;

	private Button btnCommit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.request_sms);
		initData();
		initView();
	}

	private void initData() {
		initBackButton(R.string.bind_phone, this);
		mCountryCodeDatabase = PhotoTalkDatabaseFactory.getCountryCodeDatabase();
		allCountryCodes = mCountryCodeDatabase.getAllCountry();
		mCountryCodeDatabase.close();
		CountryCode temp = new CountryCode();
		temp.setCountryShort(Constants.COUNTRY);
		int index = allCountryCodes.indexOf(temp);
		if (index != -1) {
			mCountryCode = allCountryCodes.get(index);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private void initView() {
		btnCommit = (Button) findViewById(R.id.btn_commit);
		btnCountryCode = (Button) findViewById(R.id.btn_country_code);
		etNumber = (EditText) findViewById(R.id.et_number);
		etNumber.setHint(this.getResources().getString(R.string.bind_phone_input_phone_hint));

		etNumber.setHintTextColor(getResources().getColor(R.color.register_input_hint));

		etNumber.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				etNumber.setHintTextColor(getResources().getColor(R.color.register_input_hint));
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}
		});

		btnCommit.setOnClickListener(this);
		btnCountryCode.setOnClickListener(this);
		if (mCountryCode != null)
			btnCountryCode.setText(getString(R.string.country_code, mCountryCode.getCountryCode(), mCountryCode.getCountryName()));
	}

	@Override
	protected void onResume() {
		super.onResume();
		int leaveTime = PrefsUtils.User.getSelfBindPhoneTimeLeave(this, getCurrentUser().getRcId());
		btnCommit.setText(getString(R.string.next_number, leaveTime));
		if (leaveTime == 0)
			btnCommit.setEnabled(false);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.btn_country_code:
				showCountryChooseDialog();
				break;

			case R.id.btn_commit:
				EventUtil.More_Setting.rcpt_getcode(baseContext);
				requestSms();
				break;
			case R.id.title_linear_back:
				finish();
				break;
		}
	}

	private void requestSms() {
		String number = etNumber.getText().toString();

		if (number.equals("")) {
			etNumber.setHintTextColor(getResources().getColor(R.color.register_input_hint_error));
			etNumber.requestFocus();
			InputMethodManager imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
			imm.showSoftInput(etNumber, 0);
			return;
		}

		if (isNumberEnable(number)) {
			final String phoneNumber = "+" + mCountryCode.getCountryCode() + number;
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(RequestSMSActivity.this);
			dialogBuilder.setTitle(phoneNumber).setMessage(getResources().getString(R.string.sms_reciver_info)).setCancelable(false)
			        .setPositiveButton(getResources().getString(R.string.modify), new DialogInterface.OnClickListener() {

				        @Override
				        public void onClick(DialogInterface dialog, int which) {
				        }
			        }).setNegativeButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {

				        @Override
				        public void onClick(DialogInterface dialog, int which) {
					        showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
					        UserSettingProxy.requestSMS(RequestSMSActivity.this, new RCPlatformResponseHandler() {

						        @Override
						        public void onSuccess(int statusCode, String content) {
							        dismissLoadingDialog();
							        PrefsUtils.User.addSelfBindPhoneTime(RequestSMSActivity.this, getCurrentUser().getRcId());
							        startBindPhoneActivity(phoneNumber);
						        }

						        @Override
						        public void onFailure(int errorCode, String content) {
							        dismissLoadingDialog();
							        showErrorConfirmDialog(content);
						        }
					        }, phoneNumber);

				        }
			        });
			dialogBuilder.create().show();

		}
	}

	private void startBindPhoneActivity(String number) {
		Intent intent = new Intent(this, BindPhoneActivity.class);
		intent.putExtra(BindPhoneActivity.REQUEST_PAMAM_NUMBER, number);
		startActivityForResult(intent, REQUEST_CODE_BIND);
	}

	private boolean isNumberEnable(String number) {
		if (TextUtils.isEmpty(number)) {
			showErrorConfirmDialog(R.string.input_correct_phonenumber);
			return false;
		}
		return true;
	}

	private void showCountryChooseDialog() {
		if (mCountryChooseDialog == null) {
			final String[] items = new String[allCountryCodes.size()];
			String itemTextBase = getString(R.string.country_code);
			for (int i = 0; i < allCountryCodes.size(); i++) {
				CountryCode code = allCountryCodes.get(i);
				items[i] = String.format(itemTextBase, code.getCountryCode(), code.getCountryName());
			}
			mCountryChooseDialog = new AlertDialog.Builder(this).setItems(items, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					btnCountryCode.setText(items[which]);
					mCountryCode = allCountryCodes.get(which);
					dialog.dismiss();
				}
			}).create();
		}
		mCountryChooseDialog.show();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_BIND) {
			setResult(Activity.RESULT_OK);
			finish();
		}

	}
}
