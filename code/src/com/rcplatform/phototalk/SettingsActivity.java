package com.rcplatform.phototalk;

import java.util.ArrayList;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.adapter.AppsAdapter;
import com.rcplatform.phototalk.api.MenueApiFactory;
import com.rcplatform.phototalk.api.MenueApiUrl;
import com.rcplatform.phototalk.api.PhotoTalkParams;
import com.rcplatform.phototalk.bean.AppBean;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.galhttprequest.GalHttpRequest;
import com.rcplatform.phototalk.galhttprequest.GalHttpRequest.GalHttpLoadTextCallBack;
import com.rcplatform.phototalk.galhttprequest.LogUtil;
import com.rcplatform.phototalk.image.downloader.ImageOptionsFactory;
import com.rcplatform.phototalk.image.downloader.RCPlatformImageLoader;
import com.rcplatform.phototalk.utils.AppSelfInfo;
import com.rcplatform.phototalk.utils.Contract;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.PrefsUtils;
import com.rcplatform.phototalk.utils.ShowToast;
import com.rcplatform.phototalk.views.HorizontalListView;

public class SettingsActivity extends Activity implements View.OnClickListener {

	private static final String TAG = "MyFriendsActivity";

	private static final int REQUEST_CODE_EDIT_INFO = 100;

	private ProgressBar mProgressbar;

	private View mTocatyPandleView;

	private Context mContext;

	private Spinner mSpinner;

	private static final String[] privateMode = { "Everyone", "My Friends" };

	private Button mCleanBtn;

	private UserInfo userDetailInfo;

	private ArrayList<AppBean> appLists;

	private String number;

	private HorizontalListView mHrzListView;

	private void setUserInfo(UserInfo userInfo) {
		RCPlatformImageLoader.loadImage(SettingsActivity.this, ImageLoader.getInstance(), ImageOptionsFactory.getHeadImageOptions(), userInfo.getHeadUrl(), AppSelfInfo.ImageScaleInfo.thumbnailImageWidthPx, mHeadView, R.drawable.default_head);
		mNickView.setText("" + userInfo.getNick());
		mTatotyIdView.setText("" + userInfo.getRcId());
		mSpinner.setSelection(userInfo.getReceiveSet());
		number = userInfo.getPhone();
	}

	private Handler mHandler2 = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			mProgressbar.setVisibility(View.GONE);
			super.handleMessage(msg);
			switch (msg.what) {
			case MenueApiFactory.RESPONSE_STATE_SUCCESS:
				if (userDetailInfo != null) {
					setUserInfo(userDetailInfo);
				}

				if (appLists != null && appLists.size() > 0) {
					mAppsView.setVisibility(View.VISIBLE);// ?
					AppsAdapter adapter = new AppsAdapter(SettingsActivity.this, appLists);
					mHrzListView.setAdapter(adapter);
				} else {
					mAppsView.setVisibility(View.GONE);
				}

				break;
			case MenueApiFactory.LOGIN_PASSWORD_ERROR:
				ShowToast.showToast(SettingsActivity.this, getResources().getString(R.string.reg_pwd_no_email_yes), Toast.LENGTH_LONG);
				break;
			case MenueApiFactory.LOGIN_EMAIL_ERROR:
				ShowToast.showToast(SettingsActivity.this, getResources().getString(R.string.reg_email_no), Toast.LENGTH_LONG);
				break;
			case MenueApiFactory.LOGIN_SERVER_ERROR:
				ShowToast.showToast(SettingsActivity.this, getResources().getString(R.string.reg_server_no), Toast.LENGTH_LONG);
				break;
			case MenueApiFactory.LOGIN_ADMIN_ERROR:
				ShowToast.showToast(SettingsActivity.this, getResources().getString(R.string.reg_admin_no), Toast.LENGTH_LONG);
				break;
			}

		}

	};

	private Handler mSetPrivateHandler = new MenueHandler(this) {

		@Override
		public void handleMessage(Message msg) {
			mProgressbar.setVisibility(View.GONE);
			switch (msg.what) {
			case MenueApiFactory.RESPONSE_STATE_SUCCESS:
				break;
			}
			super.handleMessage(msg);
		}

	};

	private View mBack;

	private TextView mTitleTextView;

	private ImageView mHeadView;

	private TextView mNickView;

	private TextView mTatotyIdView;

	private View mAppsView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		mContext = this;
		initTitle();

		mHeadView = (ImageView) findViewById(R.id.settings_account_head_portrait);
		mNickView = (TextView) findViewById(R.id.settings_user_nick);
		mTatotyIdView = (TextView) findViewById(R.id.settings_user_tacoty_id);
		findViewById(R.id.settings_user_info_edit_action).setOnClickListener(this);
		findViewById(R.id.settings_user_edit_tacoty_id_action).setOnClickListener(this);
		mAppsView = findViewById(R.id.settings_apps_list_layout);
		mHrzListView = (HorizontalListView) findViewById(R.id.my_friend_details_apps_listview);

		mTocatyPandleView = findViewById(R.id.settings_account_tacotyid_pandle);
		mTocatyPandleView.setOnClickListener(this);

		mSpinner = (Spinner) findViewById(R.id.settings_private_send_photo_spinner);
		ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, privateMode);
		// 设置下拉列表的风格
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mSpinner.setAdapter(adapter);
		mSpinner.setOnItemSelectedListener(new SpinnerSelectedListener());

		mCleanBtn = (Button) findViewById(R.id.settings_clean_history_record_btn);
		mCleanBtn.setOnClickListener(this);

		mProgressbar = (ProgressBar) findViewById(R.id.login_progressbar);
		mProgressbar.setVisibility(View.VISIBLE);
		syncUserInfo();
	}

	class SpinnerSelectedListener implements OnItemSelectedListener {

		@Override
		public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
			mProgressbar.setVisibility(View.VISIBLE);
			doSetPrivate(position);
		}

		/**
		 * 设置发送图片权限。
		 * 
		 * @param receiverSet
		 */
		private void doSetPrivate(int receiverSet) {

			UserInfo userInfo = PrefsUtils.LoginState.getLoginUser(SettingsActivity.this);
			GalHttpRequest request = GalHttpRequest.requestWithURL(SettingsActivity.this, MenueApiUrl.USER_INFO_UPDATE_URL);
			request.setPostValueForKey(MenueApiFactory.DEVICE_ID, android.os.Build.DEVICE);
			request.setPostValueForKey(MenueApiFactory.APP_ID, Contract.APP_ID);
			request.setPostValueForKey(MenueApiFactory.TOKEN, userInfo.getToken());
			request.setPostValueForKey(MenueApiFactory.USERID, userInfo.getSuid());
			request.setPostValueForKey(MenueApiFactory.LANGUAGE, Locale.getDefault().getLanguage());
			request.setPostValueForKey(MenueApiFactory.RECEIVESET, String.valueOf(receiverSet));
			request.startAsynUploadImage(new GalHttpLoadTextCallBack() {

				@Override
				public void textLoaded(String text) {

					try {
						System.out.println(text);
						JSONObject obj = new JSONObject(text);
						final int status = obj.getInt(MenueApiFactory.RESPONSE_KEY_STATUS);
						if (status == MenueApiFactory.RESPONSE_STATE_SUCCESS) {
							mSetPrivateHandler.sendMessage(mSetPrivateHandler.obtainMessage(status));
						} else {
							failure(obj);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

				@Override
				public void loadFail() {
					LogUtil.e(TAG, getResources().getString(R.string.net_error));
				}
			});

		}

		@Override
		public void onNothingSelected(AdapterView<?> parent) {

		}

	}

	private void initTitle() {
		mBack = findViewById(R.id.back);
		mBack.setVisibility(View.VISIBLE);
		mBack.setOnClickListener(this);
		//
		mTitleTextView = (TextView) findViewById(R.id.titleContent);
		mTitleTextView.setText(getResources().getString(R.string.my_firend_setting_more_title));
		mTitleTextView.setVisibility(View.VISIBLE);
	}

	protected void failure(JSONObject obj) {
		DialogUtil.createMsgDialog(this, getResources().getString(R.string.login_error), getResources().getString(R.string.ok)).show();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.settings_user_info_edit_action:
			startActivityForResult(new Intent(this, AccountInfoEditActivity.class), REQUEST_CODE_EDIT_INFO);
			break;
		case R.id.settings_user_edit_tacoty_id_action:
			//

			break;
		case R.id.choosebutton:
			startActivity(new Intent(this, AddFriendActivity.class));
			break;
		case R.id.settings_account_tacotyid_pandle:
			Intent intent = new Intent(this, AccountInfoActivity.class);
			intent.putExtra("totatyId", mTatotyIdView.getText());
			intent.putExtra("phone", number);// ?
			startActivity(intent);
			break;
		case R.id.settings_clean_history_record_btn:
			doCleanDistory();
			break;
		}
	}

	private void doCleanDistory() {
		UserInfo userInfo = PrefsUtils.LoginState.getLoginUser(this);
		GalHttpRequest request = GalHttpRequest.requestWithURL(this, MenueApiUrl.CLEAN_HISTORY_URL);
		request.setPostValueForKey(MenueApiFactory.TOKEN, userInfo.getToken());
		request.setPostValueForKey(MenueApiFactory.USERID, userInfo.getSuid());
		request.setPostValueForKey(MenueApiFactory.LANGUAGE, Locale.getDefault().getLanguage());
		request.setPostValueForKey(MenueApiFactory.DEVICE_ID, android.os.Build.DEVICE);
		request.setPostValueForKey(MenueApiFactory.APP_ID, Contract.APP_ID);

		request.startAsynRequestString(new GalHttpLoadTextCallBack() {

			@Override
			public void textLoaded(String text) {

				try {
					System.out.println(text);
					JSONObject obj = new JSONObject(text);
					final int status = obj.getInt(MenueApiFactory.RESPONSE_KEY_STATUS);
					if (status == MenueApiFactory.RESPONSE_STATE_SUCCESS) {
						System.out.println(status);
					} else {
						failure(obj);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void loadFail() {
				LogUtil.e(TAG, getResources().getString(R.string.net_error));
			}
		});

	}

	public void syncUserInfo() {
		GalHttpRequest request = GalHttpRequest.requestWithURL(this, MenueApiUrl.USER_INFO_URL);
		PhotoTalkParams.buildBasicParams(this, request);
		request.startAsynRequestString(new GalHttpLoadTextCallBack() {

			@Override
			public void textLoaded(String text) {

				try {
					System.out.println(text);
					JSONObject obj = new JSONObject(text);
					final int status = obj.getInt(MenueApiFactory.RESPONSE_KEY_STATUS);
					if (status == MenueApiFactory.RESPONSE_STATE_SUCCESS) {

						Gson gson = new Gson();
						JSONObject uiObj = obj.getJSONObject("userInfo");
						userDetailInfo = gson.fromJson(uiObj.toString(), UserInfo.class);
						appLists = gson.fromJson(uiObj.getJSONArray("appList").toString(), new TypeToken<ArrayList<AppBean>>() {
						}.getType());
						mHandler2.sendMessage(mHandler2.obtainMessage());
					} else {
						failure(obj);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void loadFail() {
				LogUtil.e(TAG, getResources().getString(R.string.net_error));
			}
		});

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == REQUEST_CODE_EDIT_INFO) {
				setUserInfo((UserInfo) data.getSerializableExtra(AccountInfoEditActivity.RESULT_PARAM_USER));
			}
		}
	}

}
