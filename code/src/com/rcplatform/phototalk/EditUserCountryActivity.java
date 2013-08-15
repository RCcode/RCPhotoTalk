package com.rcplatform.phototalk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.adapter.SeachCountryAdapter;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.drift.DriftInformationActivity;
import com.rcplatform.phototalk.proxy.UserSettingProxy;
import com.rcplatform.phototalk.request.handler.UpdateUserInfoResponseHandler;
import com.rcplatform.phototalk.request.handler.UpdateUserInfoResponseHandler.UpdateUserInfoListener;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.PhotoTalkUtils;

public class EditUserCountryActivity extends BaseActivity implements OnClickListener {
	private View mBack;
	private TextView mTitleTextView;
	private EditText seachEdit;
	private Button seach_delete_btn;
	private ListView listView;
	private SeachCountryAdapter adapter;
	private String userCountryCode;
	private UserInfo userDetailInfo;
	private String[] countryNameList;
	private List<CountryHolder> oldCountry = new ArrayList<CountryHolder>();
	private List<CountryHolder> countryList = new ArrayList<CountryHolder>();
	public static final String RESULT_KEY_COUNTRY = "countryCode";
	public static final String PARAM_KEY_PAGE_FROM = "pagefrom";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_user_country);
		userDetailInfo = PhotoTalkUtils.copyUserInfo(getPhotoTalkApplication().getCurrentUser());
		userCountryCode = userDetailInfo.getCountry();
		countryNameList = getResources().getStringArray(R.array.countrys);
		initData();
		initTitle();
		initView();
	}

	private void initCountryListData(int n) {
		for (int i = 0; i < countryList.size(); i++) {
			if (i == n) {
				countryList.get(i).setSelect(true);
			} else {
				countryList.get(i).setSelect(false);
			}
		}
	}

	private void initData() {
		int size = Constants.COUNTRY_CODE.length;

		for (int i = 0; i < size; i++) {
			CountryHolder hodler = new CountryHolder();
			hodler.code = Constants.COUNTRY_CODE[i];
			hodler.name = countryNameList[i];
			if (userCountryCode != null && userCountryCode.equals(Constants.COUNTRY_CODE[i])) {
				hodler.isSelect = true;
			} else {
				hodler.isSelect = false;
			}
			oldCountry.add(hodler);
		}
		Collections.sort(oldCountry, new ReverseSort());
		countryList = oldCountry;
		CountryHolder otherCountry = new CountryHolder();
		String otherCode = getString(R.string.other_country);
		String otherName=getString(R.string.other_country_name);
		if (userCountryCode != null && userCountryCode.equals(otherCode)) {
			otherCountry.isSelect = true;
		} else {
			otherCountry.isSelect = false;
		}
		otherCountry.name = otherName;
		otherCountry.code = otherCode;
		countryList.add(otherCountry);

	}

	private void initTitle() {
		mBack = findViewById(R.id.back);
		mBack.setVisibility(View.VISIBLE);
		mBack.setOnClickListener(this);
		mTitleTextView = (TextView) findViewById(R.id.titleContent);
		mTitleTextView.setText(getResources().getString(R.string.country));
		mTitleTextView.setVisibility(View.VISIBLE);
	}

	private void initView() {
		listView = (ListView) findViewById(R.id.country_list);
		seachEdit = (EditText) findViewById(R.id.search_country);
		seach_delete_btn = (Button) findViewById(R.id.seach_delete_btn);
		seach_delete_btn.setVisibility(View.INVISIBLE);
		seachEdit.clearFocus();
		seach_delete_btn.setFocusable(true);
		seach_delete_btn.setFocusableInTouchMode(true);
		seachEdit.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				String keyWords = s.toString().trim();
				if (TextUtils.isEmpty(keyWords)) {
					seach_delete_btn.setVisibility(View.INVISIBLE);
					// initData();
					// countryList.clear();
					countryList = oldCountry;
					adapter = new SeachCountryAdapter(EditUserCountryActivity.this, countryList);
					listView.setAdapter(adapter);
				} else {
					search(keyWords);
					seach_delete_btn.setVisibility(View.VISIBLE);
				}
			}
		});
		adapter = new SeachCountryAdapter(this, countryList);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				userCountryCode = countryList.get(position).getCode();
				initCountryListData(position);
				adapter.notifyDataSetChanged();
				activityFinish();
			}
		});
	}

	private View getFooterView() {
		View footerView = getLayoutInflater().inflate(R.layout.country_list_item, null);
		ImageView countryFlag = (ImageView) footerView.findViewById(R.id.country_flag_view);
		TextView countryName = (TextView) footerView.findViewById(R.id.country_name);
		Button selectBtn = (Button) footerView.findViewById(R.id.select_btn);
		String countryCode = getString(R.string.other_country);
		countryName.setText(countryCode);
		if (countryCode.equals(userCountryCode)) {
			selectBtn.setVisibility(View.VISIBLE);
		} else {
			selectBtn.setVisibility(View.GONE);
		}
		return footerView;
	}

	public void activityFinish() {
		if (DriftInformationActivity.class.getName().equals(getIntent().getStringExtra(PARAM_KEY_PAGE_FROM))) {
			showLoadingDialog(false);
			UserSettingProxy.updateUserInfo(this, userCountryCode, userDetailInfo.getNickName(), userDetailInfo.getBirthday(), userDetailInfo.getGender() + "",
					new UpdateUserInfoResponseHandler(this, new UpdateUserInfoListener() {

						@Override
						public void onUpdateSucess(String headUrl) {
							dissmissLoadingDialog();
							Intent intent = new Intent();
							intent.putExtra(RESULT_KEY_COUNTRY, userCountryCode);
							setResult(Activity.RESULT_OK, intent);
							finish();
						}

						@Override
						public void onUpdateFail(String failReason) {
							dissmissLoadingDialog();
							showUpdateUserCountryFailDialog();
						}
					}));
			return;
		}
		Intent intent = new Intent();
		intent.putExtra(RESULT_KEY_COUNTRY, userCountryCode);
		setResult(Activity.RESULT_OK, intent);
		this.finish();
	}

	private AlertDialog updateFailDialog;

	private void showUpdateUserCountryFailDialog() {
		if (updateFailDialog == null) {
			DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						activityFinish();
						break;
					case DialogInterface.BUTTON_NEGATIVE:
						finish();
						break;
					}
					dialog.dismiss();
				}
			};
			updateFailDialog = DialogUtil.getAlertDialogBuilder(this).setMessage(R.string.net_error).setPositiveButton(R.string.ok, listener)
					.setNegativeButton(R.string.cancel, listener).setCancelable(false).create();
		}
		updateFailDialog.show();
	}

	private void search(String keyWords) {
		if (keyWords != null) {
			keyWords = keyWords.toLowerCase();
		}
		List<CountryHolder> seachListCountry = new ArrayList<CountryHolder>();
		for (int i = 0; i < oldCountry.size(); i++) {
			CountryHolder country = oldCountry.get(i);
			if (country.getName() != null && country.getName().toLowerCase().contains(keyWords)) {
				if (country.getCode() != null && country.getCode().equals(userCountryCode)) {
					country.setSelect(true);
				} else {
					country.setSelect(false);
				}
				seachListCountry.add(country);
			}
		}
		countryList = seachListCountry;
		adapter = new SeachCountryAdapter(this, countryList);
		listView.setAdapter(adapter);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			activityFinish();
			break;
		case R.id.seach_delete_btn:
			seachEdit.setText("");
			// initData();
			countryList = oldCountry;
			adapter = new SeachCountryAdapter(EditUserCountryActivity.this, countryList);
			listView.setAdapter(adapter);
			break;
		default:
			break;
		}
	}

	public class CountryHolder implements Comparable {
		String code;
		String name;
		boolean isSelect = false;

		public boolean isSelect() {
			return isSelect;
		}

		public void setSelect(boolean isSelect) {
			this.isSelect = isSelect;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		@Override
		public int compareTo(Object another) {
			CountryHolder user = (CountryHolder) another;
			return this.name.compareTo(user.name);
		}

	}

	class ReverseSort implements Comparator {
		public int compare(Object obj1, Object obj2) {
			CountryHolder user1 = (CountryHolder) obj1;
			CountryHolder user2 = (CountryHolder) obj2;
			return user1.name.compareTo(user2.name);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			activityFinish();
		}
		return super.onKeyDown(keyCode, event);
	}
}
