package com.rcplatform.phototalk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rcplatform.phototalk.activity.ImagePickActivity;
import com.rcplatform.phototalk.api.JSONConver;
import com.rcplatform.phototalk.api.MenueApiFactory;
import com.rcplatform.phototalk.api.MenueApiUrl;
import com.rcplatform.phototalk.api.PhotoTalkParams;
import com.rcplatform.phototalk.api.RCPlatformAsyncHttpClient;
import com.rcplatform.phototalk.api.RCPlatformAsyncHttpClient.RequestAction;
import com.rcplatform.phototalk.api.RCPlatformResponse;
import com.rcplatform.phototalk.api.RCPlatformResponseHandler;
import com.rcplatform.phototalk.bean.AppInfo;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.galhttprequest.MD5;
import com.rcplatform.phototalk.task.ContactUploadTask;
import com.rcplatform.phototalk.task.ContactUploadTask.OnUploadOverListener;
import com.rcplatform.phototalk.task.ContactUploadTask.Status;
import com.rcplatform.phototalk.utils.Contract;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.ListViewUtils;
import com.rcplatform.phototalk.utils.PrefsUtils;
import com.rcplatform.phototalk.utils.RCPlatformTextUtil;
import com.rcplatform.phototalk.utils.ShowToast;
import com.rcplatform.phototalk.utils.Utils;

public class LoginActivity extends ImagePickActivity implements View.OnClickListener {

	public static final String RESULT_KEY_USERINFO = "userinfo";
	private static final int MSG_LOAD_RCPLATFORM_USERS_OVER = 100;
	private String mGoogleAccount;
	private boolean mIsLoginPage;

	private TextView mTitleTextView;

	private TextView mDescTextView;

	private EditText mLoginIdEditText;

	private EditText mNickEditText;

	private EditText mPswEditText;

	private View mLine2View;

	private ListView mLvAcccounts;

	private Button mLoginButton;

	private Button mSignupButton;

	private Button mForgetPswButton;
	private View mLinearAccounts;
	private ImageView mIvHead;
	private TextView btnChange;

	// 正则，必须由数字字母组成
	private final String RCID_REGEX = "(?!^[0-9]*$)(?!^[a-zA-Z]*$)^([a-zA-Z0-9]{2,})$";
	private final String EMAIL_REGEX = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
	// 密码正则表达式
	private final Pattern passwordPattern = Pattern.compile("[a-zA-Z0-9]{6,16}");

	// 用户登录。
	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case MenueApiFactory.RESPONSE_STATE_SUCCESS:
				// b)登录成功,保持用户信息，请求进入好友采集页面。
				UserInfo userInfo = (UserInfo) msg.obj;
				userLoginSuccess(userInfo);
				break;
			// 密码错误
			case MenueApiFactory.LOGIN_PASSWORD_ERROR:
				ShowToast.showToast(LoginActivity.this, getResources().getString(R.string.reg_pwd_no_email_yes), Toast.LENGTH_LONG);
				break;
			// 邮箱没有注册
			case MenueApiFactory.LOGIN_EMAIL_ERROR:
				ShowToast.showToast(LoginActivity.this, getResources().getString(R.string.reg_email_no), Toast.LENGTH_LONG);
				break;
			// 服务器异常
			case MenueApiFactory.LOGIN_SERVER_ERROR:
				ShowToast.showToast(LoginActivity.this, getResources().getString(R.string.reg_server_no), Toast.LENGTH_LONG);
				break;
			// 管理员不允许客户端登录
			case MenueApiFactory.LOGIN_ADMIN_ERROR:
				ShowToast.showToast(LoginActivity.this, getResources().getString(R.string.reg_admin_no), Toast.LENGTH_LONG);
				break;
			default:
				break;

			}
		}

	};

	private void startPlatformUserEditActivity(Map<AppInfo, UserInfo> userApps) {
		Intent intent = new Intent(this, PlatformEditActivity.class);
		intent.putExtra(PlatformEditActivity.PARAM_USER_APPS, (HashMap<AppInfo, UserInfo>) userApps);
		startActivity(intent);
	}

	// 注册handler
	private Handler mHandler2 = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_LOAD_RCPLATFORM_USERS_OVER:
				dismissLoadingDialog();
				final BaseAdapter adapter = new OtherAppsAdapter((Map<AppInfo, UserInfo>) msg.obj);
				mLvAcccounts.setAdapter(adapter);
				mLvAcccounts.setOnItemClickListener(new OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						// TODO Auto-generated method stub
						Map<AppInfo, UserInfo> userApps = (Map<AppInfo, UserInfo>) adapter.getItem(position);
						checkPlatformUser(userApps);
						// startPlatformUserEditActivity(userApps);
					}
				});
				ListViewUtils.setListViewHeightBasedOnChildren(mLvAcccounts);
				break;
			case MenueApiFactory.RESPONSE_STATE_SUCCESS:
				// a)注册成功后,发短信短信到+8618146193618，然后登录
				UserInfo userInfo = (UserInfo) msg.obj;
				userLoginSuccess(userInfo);
				// bindSuidByPhone(Contract.BIND_PHONE_NUMBER, userInfo);
				break;
			// 后台异常错误
			case MenueApiFactory.REGISTER_SERVER_ERROR:
				ShowToast.showToast(LoginActivity.this, getResources().getString(R.string.reg_server_error));
				break;
			// 邮箱已被注册
			case MenueApiFactory.REGISTER_EMAIL_ERROR:
				ShowToast.showToast(LoginActivity.this, getResources().getString(R.string.reg_email_is_register));
				break;
			// 昵称已被注册
			case MenueApiFactory.REGISTER_NICK_ERROR:
				ShowToast.showToast(LoginActivity.this, getResources().getString(R.string.reg_nick_is_register));
				break;
			default:
				break;

			}

		}

	};

	private void userLoginSuccess(final UserInfo userInfo) {
		if (userInfo.getShowRecommends() == UserInfo.FIRST_TIME && !PrefsUtils.AppInfo.hasUploadContacts(LoginActivity.this)) {
			ContactUploadTask task = ContactUploadTask.getInstance(LoginActivity.this);
			if (task.getStatus() == Status.STATUS_RUNNING) {
				showLoadingDialog(LOADING_NO_MSG, R.string.uploading_contacts, false);
				task.setOnUploadOverListener(new OnUploadOverListener() {

					@Override
					public void onUploadOver(boolean isSuccess) {
						dismissLoadingDialog();
						saveUserInfo(userInfo);
						closePage(userInfo);
					}
				});
				return;
			}
		}
		saveUserInfo(userInfo);
		closePage(userInfo);
	}

	private void saveUserInfo(UserInfo userInfo) {
		String psw = mPswEditText.getText().toString();
		userInfo.setPassWord(MD5.md5Hash(psw));
		PrefsUtils.LoginState.setLoginUser(getApplicationContext(), userInfo);
	}

	private void closePage(UserInfo userInfo) {
		Intent result = new Intent();
		result.putExtra(RESULT_KEY_USERINFO, userInfo);
		setResult(Activity.RESULT_OK, result);
		finish();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		Intent intent = getIntent();
		mIsLoginPage = (Boolean) intent.getExtras().get(Contract.KEY_LOGIN_PAGE);
		httpClient = new RCPlatformAsyncHttpClient(RequestAction.JSON);
		setupData();
		setupView();
		loadRcplatfromUsers();
	}

	private void setupData() {
		// TODO Auto-generated method stub
		AccountManager manager = AccountManager.get(this);
		Account[] accounts = manager.getAccounts();
		if (accounts != null && accounts.length > 0) {
			for (Account account : accounts) {
				if (account.type.equals("com.google")) {
					mGoogleAccount = account.name;
					return;
				}
			}
		}
	}

	private void loadRcplatfromUsers() {
		// TODO Auto-generated method stub
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		Thread th = new Thread() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				Map<AppInfo, UserInfo> appUsers = Utils.getRCPlatformAppUsers(LoginActivity.this);
				Message msg = mHandler2.obtainMessage();
				msg.what = MSG_LOAD_RCPLATFORM_USERS_OVER;
				msg.obj = appUsers;
				mHandler2.sendMessage(msg);
			}
		};
		th.start();
	}

	private void setupView() {
		findViewById(R.id.title_linear_back).setOnClickListener(this);
		findViewById(R.id.back).setVisibility(View.VISIBLE);
		mIvHead = (ImageView) findViewById(R.id.iv_registe_head);
		mIvHead.setOnClickListener(this);
		btnChange = (TextView) findViewById(R.id.choosebutton);
		btnChange.setOnClickListener(this);
		btnChange.setVisibility(View.VISIBLE);
		mTitleTextView = (TextView) findViewById(R.id.titleContent);
		mTitleTextView.setVisibility(View.VISIBLE);
		mLvAcccounts = (ListView) findViewById(R.id.lv_apps_account);
		mDescTextView = (TextView) findViewById(R.id.reg_bubble_desc_text);

		mLoginIdEditText = (EditText) findViewById(R.id.login_id);
		mNickEditText = (EditText) findViewById(R.id.login_nick);
		mPswEditText = (EditText) findViewById(R.id.login_password);
		mLine2View = findViewById(R.id.login_field_line2);

		mForgetPswButton = (Button) findViewById(R.id.login_page_forget_password_button);
		mForgetPswButton.setOnClickListener(this);
		mLoginButton = (Button) findViewById(R.id.login_page_login_button);
		mLoginButton.setOnClickListener(this);
		mSignupButton = (Button) findViewById(R.id.login_page_signup_button);
		mSignupButton.setOnClickListener(this);
		mLinearAccounts = findViewById(R.id.linear_platform_accounts);
		ListViewUtils.setListViewHeightBasedOnChildren(mLvAcccounts);
		if (mIsLoginPage) {
			showLoginView();
		} else {
			showSignupView();
		}
	}

	private void showSignupView() {
		clearInputInfo();
		btnChange.setText(R.string.landing_page_login);
		mLinearAccounts.setVisibility(View.GONE);
		mLoginIdEditText.setText(mGoogleAccount);
		mDescTextView.setText(R.string.reg_bubble_desc_text);
		mTitleTextView.setText(R.string.login_title_bubble_text);
		mLine2View.setVisibility(View.VISIBLE);
		mForgetPswButton.setVisibility(View.GONE);
		mLvAcccounts.setVisibility(View.GONE);
		mNickEditText.setVisibility(View.VISIBLE);
		mLoginButton.setVisibility(View.GONE);
		mSignupButton.setVisibility(View.VISIBLE);
		mLoginIdEditText.setHint(getString(R.string.registe_email_hint));
		mPswEditText.setInputType(InputType.TYPE_CLASS_TEXT);
		mIvHead.setVisibility(View.GONE);
	}

	private void showLoginView() {
		clearInputInfo();
		mDescTextView.setText(R.string.user_other_account);
		btnChange.setText(R.string.landing_page_signup);
		mLinearAccounts.setVisibility(View.VISIBLE);
		mTitleTextView.setText(R.string.login_title_login_bubble_text);
		mLine2View.setVisibility(View.GONE);
		mForgetPswButton.setVisibility(View.VISIBLE);
		mLvAcccounts.setVisibility(View.VISIBLE);
		mNickEditText.setVisibility(View.GONE);
		mLoginButton.setVisibility(View.VISIBLE);
		mSignupButton.setVisibility(View.GONE);
		mLoginIdEditText.setHint(getString(R.string.login_username_hint));
		mPswEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		mIvHead.setVisibility(View.GONE);
	}

	private void clearInputInfo() {
		mNickEditText.setText(null);
		mPswEditText.setText(null);
		mLoginIdEditText.setText(null);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.title_linear_back:
			finish();
			break;
		case R.id.iv_registe_head:
			showImagePickMenu(v);
			break;
		case R.id.login_page_signup_button:
			final String email = mLoginIdEditText.getText().toString();
			final String nick = mNickEditText.getText().toString();
			final String psw = mPswEditText.getText().toString();
			if (invalidate(this, email, nick, psw)) {
				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(LoginActivity.this);
				dialogBuilder.setMessage(getResources().getString(R.string.register_confirm_email_address, email)).setCancelable(false).setPositiveButton(getResources().getString(R.string.modify), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
				}).setNegativeButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
						showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
						register(LoginActivity.this, email, psw, nick);

					}
				});
				dialogBuilder.create().show();
			}
			break;
		case R.id.login_page_login_button:
			String email2 = mLoginIdEditText.getText().toString().trim();
			String psw2 = mPswEditText.getText().toString().trim();
			int loginType = invalidate(email2, psw2);
			if (loginType != -1) {
				// ------------------测试暂加----------------------------
				if (loginType == MenueApiFactory.LOGIN_TYPE_PHONE)
					email2 = "+86" + email2;
				// --------------------------------------------------
				showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
				login(this, mHandler, email2, psw2, loginType);
			}
			break;
		case R.id.login_page_forget_password_button:
			startActivity(new Intent(this, ForgetPasswordActivity.class));
			break;
		case R.id.choosebutton:
			if (mIsLoginPage) {
				showSignupView();
			} else {
				showLoginView();
			}
			mIsLoginPage = !mIsLoginPage;
			break;
		default:
			break;
		}
	}

	private int invalidate(String email2, String psw2) {
		int type = checkAccount(email2);
		if (type != -1 && checkPassword(psw2)) {
			return type;
		}
		return -1;
	}

	/**
	 * 验证注册信息。 Method description
	 * 
	 * @return
	 */
	private boolean invalidate(Context context, String emailId, String nickName, String psw2) {
		if (checkEmail(emailId) && checkNickName(nickName) && checkPassword(psw2)) {
			return true;
		}
		return false;
	}

	/*
	 * 检查email的格式
	 */
	private boolean checkEmail(String emailId) {
		// 邮箱正则表达式
		if (TextUtils.isEmpty(emailId)) {
			DialogUtil.createMsgDialog(this, getResources().getString(R.string.registe_email_empty), getResources().getString(android.R.string.ok)).show();
			return false;
		}
		Pattern emailPattern = Pattern.compile(EMAIL_REGEX);
		if (!emailPattern.matcher(emailId).matches()) {
			DialogUtil.createMsgDialog(this, getResources().getString(R.string.registe_email_error), getResources().getString(android.R.string.ok)).show();
			return false;
		}
		return true;
	}

	private int checkAccount(String account) {
		if (account.matches(EMAIL_REGEX))
			return MenueApiFactory.LOGIN_TYPE_EMAIL;
		else if (account.matches(RCID_REGEX))
			return MenueApiFactory.LOGIN_TYPE_RCID;
		DialogUtil.createMsgDialog(this, getResources().getString(R.string.login_email_phone_tacotyid_is_null), getResources().getString(R.string.ok)).show();
		return -1;
	}

	/*
	 * 检查nickname的格式
	 */
	private boolean checkNickName(String nickeName) {
		// 昵称正则表达式
		if (!RCPlatformTextUtil.isNickMatches(nickeName)) {
			DialogUtil.createMsgDialog(this, getResources().getString(R.string.register_nick_empty), getResources().getString(android.R.string.ok)).show();
			return false;
		}
		return true;

	}

	/*
	 * 检查password的格式
	 */
	private boolean checkPassword(String psw) {

		if (TextUtils.isEmpty(psw)) {
			DialogUtil.createMsgDialog(this, getResources().getString(R.string.registe_password_empty), getResources().getString(android.R.string.ok)).show();
			return false;
		}
		if (!RCPlatformTextUtil.isPasswordMatches(psw)) {
			DialogUtil.createMsgDialog(this, getResources().getString(R.string.register_password_error), getResources().getString(android.R.string.ok)).show();
			return false;
		}
		return true;
	}

	private void register(final Context context, String email, String psw, String nick) {
		httpClient.clearParams();
		PhotoTalkParams.buildBasicParams(this, httpClient);
		httpClient.putRequestParam(PhotoTalkParams.Registe.PARAM_KEY_EMAIL, email);
		httpClient.putRequestParam(PhotoTalkParams.Registe.PARAM_KEY_PASSWORD, MD5.md5Hash(psw));
		httpClient.putRequestParam(PhotoTalkParams.Registe.PARAM_KEY_NICK, nick);
		httpClient.putRequestParam(PhotoTalkParams.Registe.PARAM_KEY_COUNTRY, Locale.getDefault().getCountry());
		httpClient.post(this, MenueApiUrl.SIGNUP_URL, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				try {
					JSONObject obj = new JSONObject(content);
					final UserInfo userInfo = new Gson().fromJson(obj.getJSONObject("userInfoAll").toString(), UserInfo.class);
					int showRecommends = obj.getInt("showRecommends");
					userInfo.setShowRecommends(showRecommends);
					sendLoginMessage(mHandler2, statusCode, userInfo);
					return;
				} catch (Exception e) {
					e.printStackTrace();
					dismissLoadingDialog();
					showErrorConfirmDialog(R.string.net_error);
				}
			}

			@Override
			public void onFailure(int errorCode, String content) {
				dismissLoadingDialog();
				showErrorConfirmDialog(content);
			}
		});
	}

	/**
	 * Method description 通过email(或id或TacotyId)和密码登录系統。
	 * 
	 * @param context
	 * @param email
	 * @param psw
	 */
	public void login(final Context context, final Handler handler, String email, String psw, int loginType) {
		httpClient.clearParams();
		PhotoTalkParams.buildBasicParams(context, httpClient);
		httpClient.putRequestParam(PhotoTalkParams.Login.PARAM_KEY_ACCOUNT, email);
		httpClient.putRequestParam(PhotoTalkParams.Login.PARAM_KEY_TYPE, loginType + "");
		httpClient.putRequestParam(PhotoTalkParams.Login.PARAM_KEY_PASSWORD, MD5.md5Hash(psw));
		httpClient.putRequestParam(PhotoTalkParams.Login.PARAM_KEY_SYSTEM, android.os.Build.VERSION.RELEASE);
		httpClient.putRequestParam(PhotoTalkParams.Login.PARAM_KEY_MODEL, android.os.Build.MODEL);
		httpClient.putRequestParam(PhotoTalkParams.Login.PARAM_KEY_BRAND, android.os.Build.BRAND);
		httpClient.putRequestParam(MenueApiFactory.ADDRESS, "");
		httpClient.post(this, MenueApiUrl.LOGIN_URL, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				dismissLoadingDialog();
				try {
					JSONObject obj = new JSONObject(content);
					UserInfo userInfo = JSONConver.jsonToUserInfo(obj.getJSONObject("userInfoAll").toString());
					int showRecommends = obj.getInt("showRecommends");
					if (showRecommends == UserInfo.FIRST_TIME) {
						Map<AppInfo, UserInfo> userApps = new HashMap<AppInfo, UserInfo>();
						JSONArray appArray = obj.getJSONArray("userAppList");
						for (int i = 0; i < appArray.length(); i++) {
							JSONObject jsonApp = appArray.getJSONObject(i);
							UserInfo appUser = new UserInfo();
							appUser.clone(userInfo);
							appUser.setHeadUrl(jsonApp.getString("headUrl"));
							appUser.setNick(jsonApp.getString("nick"));
							AppInfo appInfo = new AppInfo();
							appInfo.setAppName(jsonApp.getString("appName"));
							userApps.put(appInfo, appUser);
						}
						startPlatformUserEditActivity(userApps);
					} else {
						userInfo.setShowRecommends(showRecommends);
						long lastBindTime = obj.optLong(RCPlatformResponse.Login.RESPONSE_KEY_LAST_BIND_TIME);
						String lastBindNumber = obj.getString(RCPlatformResponse.Login.RESPONSE_KEY_LAST_BIND_NUMBER);
						PrefsUtils.User.setLastBindNumber(getApplicationContext(), userInfo.getEmail(), lastBindNumber);
						PrefsUtils.User.setLastBindPhoneTime(getApplicationContext(), lastBindTime, userInfo.getEmail());
						sendLoginMessage(handler, statusCode, userInfo);
					}
				} catch (Exception e) {
					e.printStackTrace();
					dismissLoadingDialog();
					showErrorConfirmDialog(R.string.net_error);
				}
			}

			@Override
			public void onFailure(int errorCode, String content) {
				dismissLoadingDialog();
				showErrorConfirmDialog(content);
			}
		});

	}

	@Override
	protected void onImageReceive(Uri imageBaseUri, String imagePath) {
		super.onImageReceive(imageBaseUri, imagePath);
		showImage(imagePath, imageBaseUri, mIvHead);
	}

	class OtherAppsAdapter extends BaseAdapter {
		private List<UserInfo> users;
		private Map<AppInfo, UserInfo> installedApps;

		public OtherAppsAdapter(Map<AppInfo, UserInfo> platformUsers) {
			// TODO Auto-generated constructor stub
			users = new ArrayList<UserInfo>();
			for (UserInfo user : platformUsers.values()) {
				if (!users.contains(user)) {
					users.add(user);
				}
			}
			installedApps = platformUsers;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if (convertView == null)
				convertView = getLayoutInflater().inflate(R.layout.other_app_item, null);
			TextView tv = (TextView) convertView.findViewById(R.id.tv_account);
			UserInfo userInfo = users.get(position);
			tv.setText(userInfo.getEmail());
			ImageView iv = (ImageView) convertView.findViewById(R.id.iv_logo);
			iv.setImageResource(R.drawable.ic_launcher);
			return convertView;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			UserInfo userInfo = users.get(position);
			Map<AppInfo, UserInfo> userApps = new HashMap<AppInfo, UserInfo>();
			for (AppInfo info : installedApps.keySet()) {
				UserInfo user = installedApps.get(info);
				if (userInfo.getSuid().equals(user.getSuid())) {
					userApps.put(info, user);
				}
			}
			return userApps;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return users.size();
		}

	};

	private void checkPlatformUser(final Map<AppInfo, UserInfo> userApps) {
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		Iterator<UserInfo> itUsers = userApps.values().iterator();
		UserInfo userInfo = itUsers.next();
		httpClient.clearParams();
		PhotoTalkParams.buildBasicParams(this, httpClient);
		httpClient.putRequestParam(PhotoTalkParams.PARAM_KEY_TOKEN, userInfo.getToken());
		httpClient.putRequestParam(PhotoTalkParams.PARAM_KEY_USER_ID, userInfo.getSuid());
		httpClient.post(this, MenueApiUrl.CHECK_USER_URL, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				// TODO Auto-generated method stub
				dismissLoadingDialog();
				try {
					JSONObject obj = new JSONObject(content);
					int showRecommends = obj.getInt("showRecommends");
					if (showRecommends == UserInfo.FIRST_TIME) {
						startPlatformUserEditActivity(userApps);
					} else {
						UserInfo userInfo = JSONConver.jsonToUserInfo(obj.getJSONObject("userInfoAll").toString());
						userInfo.setShowRecommends(showRecommends);
						long lastBindTime = obj.optLong(RCPlatformResponse.Login.RESPONSE_KEY_LAST_BIND_TIME);
						String lastBindNumber = obj.getString(RCPlatformResponse.Login.RESPONSE_KEY_LAST_BIND_NUMBER);
						PrefsUtils.User.setLastBindNumber(getApplicationContext(), userInfo.getEmail(), lastBindNumber);
						PrefsUtils.User.setLastBindPhoneTime(getApplicationContext(), lastBindTime, userInfo.getEmail());
						sendLoginMessage(mHandler, statusCode, userInfo);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					showErrorConfirmDialog(R.string.net_error);
				}
			}

			@Override
			public void onFailure(int errorCode, String content) {
				// TODO Auto-generated method stub
				dismissLoadingDialog();
				showErrorConfirmDialog(content);
			}
		});
	}

	private void sendLoginMessage(Handler handler, int statusCode, UserInfo userInfo) {
		Message msg = handler.obtainMessage(statusCode);
		msg.obj = userInfo;
		handler.sendMessage(msg);
	}
}
