package com.rcplatform.phototalk;

import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.bean.CountryCode;
import com.rcplatform.phototalk.db.CountryCodeDatabase;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;

public class BindPhoneActivity extends BaseActivity implements OnClickListener {
	private EditText etNumber;
	private Button btnCountryCode;
	private AlertDialog mCountryChooseDialog;
	private CountryCode mCountryCode;
	private List<CountryCode> allCountryCodes;
	private CountryCodeDatabase mCountryCodeDatabase;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.bind_phone);
		initData();
		initView();
	}

	private void initData() {
		mCountryCodeDatabase = PhotoTalkDatabaseFactory.getCountryCodeDatabase();
		allCountryCodes = mCountryCodeDatabase.getAllCountry();
		CountryCode temp = new CountryCode();
		temp.setCountryShort(Locale.getDefault().getCountry());
		int index = allCountryCodes.indexOf(temp);
		if (index != -1) {
			mCountryCode = allCountryCodes.get(index);
		}
	}

	private void initView() {
		Button btnCommit = (Button) findViewById(R.id.btn_commit);
		btnCountryCode = (Button) findViewById(R.id.btn_country_code);
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
			
			break;
		}
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
}
