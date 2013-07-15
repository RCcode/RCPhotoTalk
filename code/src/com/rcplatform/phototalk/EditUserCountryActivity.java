package com.rcplatform.phototalk;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
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
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.adapter.PhotoTalkFriendsAdapter;
import com.rcplatform.phototalk.adapter.SeachCountryAdapter;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.logic.controller.SettingPageController;
import com.rcplatform.phototalk.proxy.FriendsProxy;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.request.RCPlatformServiceError;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.PhotoTalkUtils;
import com.rcplatform.phototalk.utils.PrefsUtils;

public class EditUserCountryActivity extends BaseActivity implements
		OnClickListener {
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_user_country);
		userDetailInfo = PhotoTalkUtils.copyUserInfo(getPhotoTalkApplication()
				.getCurrentUser());
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
			if (userCountryCode != null
					&& userCountryCode.equals(Constants.COUNTRY_CODE[i])) {
				hodler.isSelect = true;
			} else {
				hodler.isSelect = false;
			}
			oldCountry.add(hodler);
		}
		countryList = oldCountry;

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
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				String keyWords = s.toString().trim();
				if (TextUtils.isEmpty(keyWords)) {
					seach_delete_btn.setVisibility(View.INVISIBLE);
					initData();
					adapter = new SeachCountryAdapter(
							EditUserCountryActivity.this, countryList);
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
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				userCountryCode = countryList.get(position).getCode();
				
//				userDetailInfo.setCountry(userCountryCode);
//				PrefsUtils.User.saveUserInfo(
//						getApplicationContext(),
//						userDetailInfo.getRcId(), userDetailInfo);
//				getPhotoTalkApplication().setCurrentUser(
//						userDetailInfo);
				initCountryListData(position);
				adapter.notifyDataSetChanged();
			}
		});
	}

	private void post(String countryCode){
		FriendsProxy.postCountryCode(this, new RCPlatformResponseHandler() {
			
			@Override
			public void onSuccess(int statusCode, String content) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onFailure(int errorCode, String content) {
				// TODO Auto-generated method stub
				
			}
		}, countryCode);
	}
	
	private void search(String keyWords) {
		if (keyWords != null) {
			keyWords = keyWords.toLowerCase();
		}
		List<CountryHolder> seachListCountry = new ArrayList<CountryHolder>();
		for (int i = 0; i < oldCountry.size(); i++) {
			CountryHolder country = oldCountry.get(i);
			if (country.getName() != null
					&& country.getName().toLowerCase().contains(keyWords)) {
				if (country.getCode() != null
						&& country.getCode().equals(userCountryCode)) {
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
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.back:
			Intent intent = new Intent();
			intent.putExtra("countryCode", userCountryCode);
			setResult(Activity.RESULT_OK,intent);
			this.finish();
			break;
		case R.id.seach_delete_btn:
			seachEdit.setText("");
			initData();
			adapter = new SeachCountryAdapter(EditUserCountryActivity.this, countryList);
			listView.setAdapter(adapter);
			break;
		default:
			break;
		}
	}

	public class CountryHolder {
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

	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK){
			Intent intent = new Intent();
			intent.putExtra("countryCode", userCountryCode);
			setResult(Activity.RESULT_OK,intent);
		}
		return super.onKeyDown(keyCode, event);
	}
}
