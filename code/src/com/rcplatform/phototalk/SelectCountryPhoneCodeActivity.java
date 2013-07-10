package com.rcplatform.phototalk;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.adapter.PhotoTalkFriendsAdapter;
import com.rcplatform.phototalk.adapter.SeachCountryAdapter;
import com.rcplatform.phototalk.bean.CountryCode;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.db.CountryCodeDatabase;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.logic.controller.SettingPageController;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.PhotoTalkUtils;
import com.rcplatform.phototalk.utils.PrefsUtils;

public class SelectCountryPhoneCodeActivity extends BaseActivity implements
		OnClickListener {
	private View mBack;
	private TextView mTitleTextView;
	private EditText seachEdit;
	private Button seach_delete_btn;
	private ListView listView;
	private UserInfo userDetailInfo;
	private String phone;
	private CountryCode mCountryCode;
	private List<CountryCode> allCountryCodes;
	private CountryCodeDatabase mCountryCodeDatabase;
	private MyAdapter myAdapter;
	private List<PhoneHodler> listData = new ArrayList<PhoneHodler>();
	private List<PhoneHodler> oldData = new ArrayList<PhoneHodler>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_user_country);
		initData();
		getOldList();
		initTitle();
		initView();
	}

	// 初始化数据 获取 当前语言的国家电话码
	private void initData() {
		mCountryCodeDatabase = PhotoTalkDatabaseFactory
				.getCountryCodeDatabase();
		allCountryCodes = mCountryCodeDatabase.getAllCountry();
		mCountryCodeDatabase.close();
		CountryCode temp = new CountryCode();
		temp.setCountryShort(Constants.COUNTRY);
		int index = allCountryCodes.indexOf(temp);
		if (index != -1) {
			mCountryCode = allCountryCodes.get(index);
		}
		if (mCountryCode != null) {
			phone = getString(R.string.country_code,
					mCountryCode.getCountryCode(),
					mCountryCode.getCountryName());
		}
	}

	// 获取最原始的数据内容列表
	private void getOldList() {
		String itemTextBase = getString(R.string.country_code);
		for (int i = 0; i < allCountryCodes.size(); i++) {
			PhoneHodler phoneHodler = new PhoneHodler();
			CountryCode code = allCountryCodes.get(i);
			String text = String.format(itemTextBase, code.getCountryCode(),
					code.getCountryName());
			phoneHodler.setText(text);
			if (text != null && text.equals(phone)) {
				phoneHodler.setSelect(true);
			} else {
				phoneHodler.setSelect(false);
			}
			oldData.add(phoneHodler);
		}
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
		seach_delete_btn = (Button) findViewById(R.id.seach_delete_btn);
		seachEdit = (EditText) findViewById(R.id.search_country);
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
					seach_delete_btn.setVisibility(View.GONE);
					// initData();
					// adapter = new SeachCountryAdapter(
					// SelectCountryPhoneCodeActivity.this, countryList);
					// listView.setAdapter(adapter);
				} else {
					search(keyWords);
					seach_delete_btn.setVisibility(View.VISIBLE);
				}
			}
		});
		listData = oldData;
		myAdapter = new MyAdapter(this, listData);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// TODO Auto-generated method stub
				phone = listData.get(position).getText();
			}
		});
	}

	private void search(String keyWords) {
		if (keyWords != null) {
			keyWords = keyWords.toLowerCase();
		}
		List<PhoneHodler> list = new ArrayList<PhoneHodler>();
		for (int i = 0; i < oldData.size(); i++) {
			if(oldData.get(i).getText().toLowerCase().contains(keyWords)){
				PhoneHodler hodler = new PhoneHodler();
				hodler.setText(oldData.get(i).getText());
				if(oldData.get(i).getText().equals(phone)){
					hodler.setSelect(true);
				}else{
					hodler.setSelect(false);
				}
				list.add(hodler);
			}
		}
		listData = list;
		myAdapter = new MyAdapter(this, listData);
		listView.setAdapter(myAdapter);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.back:
			setResult(Activity.RESULT_OK);
			this.finish();
			break;
		case R.id.seach_delete_btn:
			seachEdit.setText("");
			initData();
			// adapter = new
			// SeachCountryAdapter(SelectCountryPhoneCodeActivity.this,
			// countryList);
			// listView.setAdapter(adapter);
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			setResult(Activity.RESULT_OK);
		}
		return super.onKeyDown(keyCode, event);
	}

	class MyAdapter extends BaseAdapter {
		Context context;
		List<PhoneHodler> listData;
		ViewHodler viewHodler;

		public MyAdapter(Context context, List<PhoneHodler> listData) {
			// TODO Auto-generated constructor stub
			this.context = context;
			this.listData = listData;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return listData.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if (convertView == null) {
				convertView = LayoutInflater.from(context).inflate(
						R.layout.select_phone_country_item, null);
				viewHodler = new ViewHodler();
				viewHodler.phoneCode = (TextView) convertView
						.findViewById(R.id.phone_code);
				viewHodler.selectView = (ImageView) convertView
						.findViewById(R.id.select_view);
				convertView.setTag(viewHodler);
			} else {
				viewHodler = (ViewHodler) convertView.getTag();
			}
			viewHodler.phoneCode.setText(listData.get(position).getText());
			if (listData.get(position).isSelect) {
				viewHodler.phoneCode.setVisibility(View.VISIBLE);
			} else {
				viewHodler.phoneCode.setVisibility(View.INVISIBLE);
			}
			return convertView;
		}

	}

	class ViewHodler {
		TextView phoneCode;
		ImageView selectView;

		public TextView getPhoneCode() {
			return phoneCode;
		}

		public void setPhoneCode(TextView phoneCode) {
			this.phoneCode = phoneCode;
		}

		public ImageView getSelectView() {
			return selectView;
		}

		public void setSelectView(ImageView selectView) {
			this.selectView = selectView;
		}

	}

	class PhoneHodler {
		String text;
		boolean isSelect;

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}

		public boolean isSelect() {
			return isSelect;
		}

		public void setSelect(boolean isSelect) {
			this.isSelect = isSelect;
		}
	}
}
