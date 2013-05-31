package com.rcplatform.phototalk;

import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.bean.CountryCode;
import com.rcplatform.phototalk.db.CountryCodeDatabase;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.proxy.UserSettingProxy;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;

public class RequestSMSActivity extends BaseActivity implements OnClickListener {

	private static final int REQUEST_CODE_BIND = 100;

	private EditText etNumber;
	private Button btnCountryCode;
	private AlertDialog mCountryChooseDialog;
	private CountryCode mCountryCode;
	private List<CountryCode> allCountryCodes;
	private CountryCodeDatabase mCountryCodeDatabase;

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
		temp.setCountryShort(Locale.getDefault().getCountry());
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
		Button btnCommit = (Button) findViewById(R.id.btn_commit);
		btnCountryCode = (Button) findViewById(R.id.btn_country_code);
		etNumber = (EditText) findViewById(R.id.et_number);
		btnCommit.setOnClickListener(this);
		btnCountryCode.setOnClickListener(this);
		if (mCountryCode != null)
			btnCountryCode.setText(getString(R.string.country_code, mCountryCode.getCountryCode(), mCountryCode.getCountryName()));
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_country_code:
			showCountryChooseDialog();
			break;

		case R.id.btn_commit:
			requestSms();
			break;
		case R.id.title_linear_back:
			finish();
			break;
		}
	}

	private void requestSms() {
		String number = etNumber.getText().toString();
		if (isNumberEnable(number)) {
			final String phoneNumber = "+" + mCountryCode.getCountryCode() + number;
			showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
			UserSettingProxy.requestSMS(this, new RCPlatformResponseHandler() {

				@Override
				public void onSuccess(int statusCode, String content) {
					dismissLoadingDialog();
					startBindPhoneActivity(phoneNumber);
				}

				@Override
				public void onFailure(int errorCode, String content) {
					dismissLoadingDialog();
					showErrorConfirmDialog(content);
				}
			}, phoneNumber);
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
		if(resultCode==Activity.RESULT_OK&&requestCode==REQUEST_CODE_BIND){
			setResult(Activity.RESULT_OK);
			finish();
		}
			
	}
}
