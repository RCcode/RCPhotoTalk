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
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.rcplatform.phototalk.activity.ImagePickActivity;
import com.rcplatform.phototalk.api.PhotoTalkApiFactory;
import com.rcplatform.phototalk.api.PhotoTalkApiUrl;
import com.rcplatform.phototalk.bean.AppInfo;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.FriendType;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.galhttprequest.LogUtil;
import com.rcplatform.phototalk.galhttprequest.MD5;
import com.rcplatform.phototalk.galhttprequest.RCPlatformServiceError;
import com.rcplatform.phototalk.request.JSONConver;
import com.rcplatform.phototalk.request.PhotoTalkParams;
import com.rcplatform.phototalk.request.RCPlatformResponse;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.request.Request;
import com.rcplatform.phototalk.request.inf.OnUserInfoLoadedListener;
import com.rcplatform.phototalk.task.ContactUploadTask;
import com.rcplatform.phototalk.task.ContactUploadTask.OnUploadOverListener;
import com.rcplatform.phototalk.task.ContactUploadTask.Status;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.ListViewUtils;
import com.rcplatform.phototalk.utils.PrefsUtils;
import com.rcplatform.phototalk.utils.RCPlatformTextUtil;
import com.rcplatform.phototalk.utils.Utils;

public class LoginActivity extends ImagePickActivity implements
		View.OnClickListener {

	public static final String RESULT_KEY_USERINFO = "userinfo";
	private String mGoogleAccount;
	private boolean mIsLoginPage;

	private TextView mTitleTextView;

	private TextView mDescTextView;

	private EditText mLoginIdEditText;

	private EditText mNickEditText;

	private EditText mPswEditText;

	private ListView mLvAcccounts;
	private TextView init_regist_agreement_text;
	private Button mLoginButton;

	private Button mSignupButton;

	private Button mForgetPswButton;
	private View mLinearAccounts;
	// private ImageView mIvHead;
	private TextView btnChange;

	// 正则，必须由数字字母组成
	// private final String RCID_REGEX =
	// "(?!^[0-9]*$)(?!^[a-zA-Z]*$)^([a-zA-Z0-9]{2,})$";
	private final String RCID_REGEX = "\\d{7,}";
	private final String EMAIL_REGEX = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";

	// 密码正则表达式

	private void startPlatformUserEditActivity(Map<AppInfo, UserInfo> userApps) {
		Intent intent = new Intent(this, PlatformEditActivity.class);
		intent.putExtra(PlatformEditActivity.PARAM_USER_APPS,
				(HashMap<AppInfo, UserInfo>) userApps);
		startActivity(intent);
	}

	private UserInfo mUser;

	private void saveUserInfo(UserInfo userInfo) {
		PrefsUtils.LoginState.setLoginUser(getApplicationContext(), userInfo);
		getPhotoTalkApplication().setCurrentUser(userInfo);
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
		mIsLoginPage = (Boolean) intent.getExtras().get(
				Constants.KEY_LOGIN_PAGE);
		setupData();
		setupView();
	}

	private void setupData() {
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

	private void setupView() {
		findViewById(R.id.title_linear_back).setOnClickListener(this);
		ImageButton back_btn = (ImageButton) findViewById(R.id.back);
		back_btn.setVisibility(View.VISIBLE);
		back_btn.setOnClickListener(this);
		init_regist_agreement_text = (TextView) findViewById(R.id.init_regist_agreement_text);
		// mIvHead = (ImageView) findViewById(R.id.iv_registe_head);
		// mIvHead.setOnClickListener(this);
		btnChange = (TextView) findViewById(R.id.choosebutton);
		btnChange.setOnClickListener(this);
		// btnChange.setVisibility(View.VISIBLE);
		mTitleTextView = (TextView) findViewById(R.id.titleContent);
		mTitleTextView.setVisibility(View.VISIBLE);
		mLvAcccounts = (ListView) findViewById(R.id.lv_apps_account);
		mDescTextView = (TextView) findViewById(R.id.reg_bubble_desc_text);
		mDescTextView.setVisibility(View.GONE);
		mLoginIdEditText = (EditText) findViewById(R.id.login_id);
		mNickEditText = (EditText) findViewById(R.id.login_nick);
		mPswEditText = (EditText) findViewById(R.id.login_password);

		mForgetPswButton = (Button) findViewById(R.id.login_page_forget_password_button);
		mForgetPswButton.setOnClickListener(this);
		mLoginButton = (Button) findViewById(R.id.login_page_login_button);
		mLoginButton.setOnClickListener(this);
		mSignupButton = (Button) findViewById(R.id.login_page_signup_button);
		mSignupButton.setOnClickListener(this);
		mLinearAccounts = findViewById(R.id.linear_platform_accounts);
		BaseAdapter adapter = new OtherAppsAdapter(Constants.userApps);
		mLvAcccounts.setAdapter(adapter);
		mLvAcccounts.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Map<AppInfo, UserInfo> userApps = (Map<AppInfo, UserInfo>) parent
						.getAdapter().getItem(position);
				checkPlatformUser(userApps);
			}
		});
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
		mDescTextView.setVisibility(View.VISIBLE);
		mTitleTextView.setText(R.string.login_title_bubble_text);
		mForgetPswButton.setVisibility(View.GONE);
		mLvAcccounts.setVisibility(View.GONE);
		mNickEditText.setVisibility(View.VISIBLE);
		mLoginButton.setVisibility(View.GONE);
		mSignupButton.setVisibility(View.VISIBLE);
		init_regist_agreement_text.setVisibility(View.VISIBLE);

		SpannableString msp = new SpannableString(
				getString(R.string.init_regist_agreement));
		msp.setSpan(new ForegroundColorSpan(Color.BLUE), 8, 12,
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // 设置前景色为洋红色
		msp.setSpan(new ForegroundColorSpan(Color.BLUE), 13, 17,
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // 设置前景色为洋红色
		msp.setSpan(new URLSpan("http://www.baidu.com"), 8, 12, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		msp.setSpan(new URLSpan("http://www.baidu.com"), 13, 17, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		init_regist_agreement_text.setText(msp);
		init_regist_agreement_text.setMovementMethod(LinkMovementMethod
				.getInstance());

		mLoginIdEditText.setHint(getString(R.string.registe_email_hint));
		mPswEditText.setInputType(InputType.TYPE_CLASS_TEXT);
		// mIvHead.setVisibility(View.GONE);
	}

	private void showLoginView() {
		clearInputInfo();
		mDescTextView.setText(R.string.user_other_account);
		mDescTextView.setVisibility(View.GONE);
		btnChange.setText(R.string.landing_page_signup);
		if (Constants.userApps.size() > 0) {
			mLinearAccounts.setVisibility(View.VISIBLE);
		} else {
			mLinearAccounts.setVisibility(View.GONE);
		}
		mTitleTextView.setText(R.string.login_title_login_bubble_text);
		mForgetPswButton.setVisibility(View.VISIBLE);
		mLvAcccounts.setVisibility(View.VISIBLE);
		mNickEditText.setVisibility(View.GONE);
		mLoginButton.setVisibility(View.VISIBLE);
		mSignupButton.setVisibility(View.GONE);
		init_regist_agreement_text.setVisibility(View.GONE);
		mLoginIdEditText.setHint(getString(R.string.login_username_hint));
		mPswEditText.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_VARIATION_PASSWORD);
		// mIvHead.setVisibility(View.GONE);
	}

	private void clearInputInfo() {
		mNickEditText.setText(null);
		mPswEditText.setText(null);
		mLoginIdEditText.setText(null);
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.back:
			finish();
			break;

		case R.id.title_linear_back:
			finish();
			break;
		// case R.id.iv_registe_head:
		// showImagePickMenu(v, CROP_HEAD_IMAGE);
		// break;
		case R.id.login_page_signup_button:
			final String email = mLoginIdEditText.getText().toString();
			final String nick = mNickEditText.getText().toString();
			final String psw = mPswEditText.getText().toString();
			if (invalidate(this, email, nick, psw)) {
				AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(
						LoginActivity.this);
				dialogBuilder
						.setMessage(
								getResources()
										.getString(
												R.string.register_confirm_email_address,
												email))
						.setCancelable(false)
						.setPositiveButton(
								getResources().getString(R.string.modify),
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
									}
								})
						.setNegativeButton(
								getResources().getString(R.string.ok),
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.cancel();
										tigaseRegiste(LoginActivity.this,
												email, psw, nick);
										// register(LoginActivity.this, email,
										// psw,
										// nick);
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
				showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
				// login(this, mHandler, email2, psw2, loginType);
				tigaseLogin(this, email2, psw2);
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
	private boolean invalidate(Context context, String emailId,
			String nickName, String psw2) {
		if (checkEmail(emailId) && checkNickName(nickName)
				&& checkPassword(psw2)) {
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
			DialogUtil.createMsgDialog(this,
					getResources().getString(R.string.registe_email_empty),
					getResources().getString(android.R.string.ok)).show();
			return false;
		}
		Pattern emailPattern = Pattern.compile(EMAIL_REGEX);
		if (!emailPattern.matcher(emailId).matches()) {
			DialogUtil.createMsgDialog(this,
					getResources().getString(R.string.registe_email_error),
					getResources().getString(android.R.string.ok)).show();
			return false;
		}
		return true;
	}

	private int checkAccount(String account) {
		if (account.matches(EMAIL_REGEX))
			return PhotoTalkApiFactory.LOGIN_TYPE_EMAIL;
		else if (account.matches(RCID_REGEX))
			return PhotoTalkApiFactory.LOGIN_TYPE_RCID;
		DialogUtil.createMsgDialog(
				this,
				getResources().getString(
						R.string.login_email_phone_tacotyid_is_null),
				getResources().getString(R.string.ok)).show();
		return -1;
	}

	/*
	 * 检查nickname的格式
	 */
	private boolean checkNickName(String nickeName) {
		// 昵称正则表达式
		if (!RCPlatformTextUtil.isNickMatches(nickeName)) {
			DialogUtil.createMsgDialog(this,
					getResources().getString(R.string.register_nick_empty),
					getResources().getString(android.R.string.ok)).show();
			return false;
		}
		return true;

	}

	/*
	 * 检查password的格式
	 */
	private boolean checkPassword(String psw) {

		if (TextUtils.isEmpty(psw)) {
			DialogUtil.createMsgDialog(this,
					getResources().getString(R.string.registe_password_empty),
					getResources().getString(android.R.string.ok)).show();
			return false;
		}
		if (!RCPlatformTextUtil.isPasswordMatches(psw)) {
			DialogUtil.createMsgDialog(this,
					getResources().getString(R.string.register_password_error),
					getResources().getString(android.R.string.ok)).show();
			return false;
		}
		return true;
	}

	@Override
	protected void onImageReceive(Uri imageBaseUri, String imagePath) {
		super.onImageReceive(imageBaseUri, imagePath);
		// showImage(imagePath, imageBaseUri, mIvHead);
	}

	class OtherAppsAdapter extends BaseAdapter {
		private List<UserInfo> users;
		private Map<AppInfo, UserInfo> installedApps;

		public OtherAppsAdapter(Map<AppInfo, UserInfo> platformUsers) {
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
			if (convertView == null)
				convertView = getLayoutInflater().inflate(
						R.layout.other_app_item, null);
			TextView tv = (TextView) convertView.findViewById(R.id.tv_account);
			UserInfo userInfo = users.get(position);
			tv.setText(userInfo.getEmail());
			ImageView iv = (ImageView) convertView.findViewById(R.id.iv_logo);
			iv.setImageResource(R.drawable.ic_launcher);
			return convertView;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public Object getItem(int position) {
			UserInfo userInfo = users.get(position);
			Map<AppInfo, UserInfo> userApps = new HashMap<AppInfo, UserInfo>();
			for (AppInfo info : installedApps.keySet()) {
				UserInfo user = installedApps.get(info);
				if (userInfo.getRcId().equals(user.getRcId())) {
					userApps.put(info, user);
				}
			}
			return userApps;
		}

		@Override
		public int getCount() {
			return users.size();
		}

	};

	private void checkPlatformUser(final Map<AppInfo, UserInfo> userApps) {
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		Iterator<UserInfo> itUsers = userApps.values().iterator();
		UserInfo userInfo = itUsers.next();
		Request request = new Request(this, PhotoTalkApiUrl.CHECK_USER_URL,
				new RCPlatformResponseHandler() {

					@Override
					public void onSuccess(int statusCode, String content) {
						dismissLoadingDialog();
						try {
							JSONObject obj = new JSONObject(content);
							int showRecommends = obj.getInt("showRecommends");
							if (showRecommends == UserInfo.FIRST_TIME) {
								startPlatformUserEditActivity(userApps);
							} else {
								UserInfo userInfo = JSONConver
										.jsonToUserInfo(obj.getJSONObject(
												"userInfoAll").toString());
								userInfo.setShowRecommends(showRecommends);
								long lastBindTime = obj
										.optLong(RCPlatformResponse.Login.RESPONSE_KEY_LAST_BIND_TIME);
								String lastBindNumber = obj
										.getString(RCPlatformResponse.Login.RESPONSE_KEY_LAST_BIND_NUMBER);
								PrefsUtils.User.MobilePhoneBind
										.setLastBindNumber(
												getApplicationContext(),
												userInfo.getRcId(),
												lastBindNumber);
								PrefsUtils.User.MobilePhoneBind
										.setLastBindPhoneTime(
												getApplicationContext(),
												lastBindTime,
												userInfo.getRcId());
								loginSuccess(userInfo);
							}
						} catch (Exception e) {
							e.printStackTrace();
							showErrorConfirmDialog(R.string.net_error);
						}
					}

					@Override
					public void onFailure(int errorCode, String content) {
						dismissLoadingDialog();
						showErrorConfirmDialog(content);
					}
				});
		PhotoTalkParams.buildBasicParams(this, request);
		request.putParam(PhotoTalkParams.PARAM_KEY_TOKEN, userInfo.getToken());
		request.putParam(PhotoTalkParams.PARAM_KEY_USER_ID, userInfo.getRcId());
		request.excuteAsync();
	}

	private void loginSuccess(final UserInfo userInfo) {
		if (userInfo.getShowRecommends() == UserInfo.FIRST_TIME
				&& !PrefsUtils.AppInfo.hasUploadContacts(LoginActivity.this)) {
			ContactUploadTask task = ContactUploadTask
					.getInstance(LoginActivity.this);
			if (task.getStatus() == Status.STATUS_RUNNING) {
				showLoadingDialog(LOADING_NO_MSG, R.string.uploading_contacts,
						false);
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
		closePage(userInfo);
	}

	private void tigaseRegiste(Context context, final String email,
			String password, final String nick) {
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		Request request = new Request(context, PhotoTalkApiUrl.SIGNUP_URL,
				new RCPlatformResponseHandler() {

					@Override
					public void onSuccess(int statusCode, String content) {
						LogUtil.e(content);
						try {
							JSONObject jsonObject = new JSONObject(content);
							mUser = new UserInfo();
							mUser.setEmail(email);
							mUser.setNickName(nick);
							mUser.setShowRecommends(UserInfo.FIRST_TIME);
							mUser.setToken(jsonObject.getString("token"));
							mUser.setTigaseId(jsonObject.getString("tgId"));
							mUser.setTigasePwd(jsonObject.getString("tgpwd"));
							mUser.setRcId(jsonObject.getString("rcId"));
							mUser.setDeviceId(PhotoTalkParams.PARAM_VALUE_DEVICE_ID);
							mUser.setAppId(PhotoTalkParams.PARAM_VALUE_APP_ID);
							saveUserInfo(mUser);
							JSONArray arrayRecommends = jsonObject
									.getJSONArray("recommendUsers");
							List<Friend> recommends = JSONConver
									.jsonToFriends(arrayRecommends.toString());
							PhotoTalkDatabaseFactory.getDatabase()
									.saveRecommends(recommends,
											FriendType.CONTACT);
							loginSuccess(mUser);
						} catch (Exception e) {
							e.printStackTrace();
							onFailure(
									RCPlatformServiceError.ERROR_CODE_REQUEST_FAIL,
									getString(R.string.net_error));
						}
					}
					@Override
					public void onFailure(int errorCode, String content) {
						dismissLoadingDialog();
						showErrorConfirmDialog(content);
					}
				});
		PhotoTalkParams.buildBasicParams(context, request);
		request.putParam(PhotoTalkParams.Registe.PARAM_KEY_EMAIL, email);
		request.putParam(PhotoTalkParams.Registe.PARAM_KEY_PASSWORD,
				MD5.encodeMD5String(password));
		request.putParam(PhotoTalkParams.Registe.PARAM_KEY_NICK, nick);
		request.putParam(PhotoTalkParams.Registe.PARAM_KEY_COUNTRY, Locale
				.getDefault().getCountry());
		request.putParam(PhotoTalkParams.Registe.PARAM_KEY_TIMEZONE,
				Utils.getTimeZoneId(context) + "");
		request.excuteAsync();
	}

	private void tigaseLogin(Context context, final String account,
			String password) {
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		Request.executeLogin(context, new OnUserInfoLoadedListener() {

			@Override
			public void onSuccess(UserInfo userInfo) {
				dismissLoadingDialog();
				saveUserInfo(userInfo);
				loginSuccess(userInfo);
			}

			@Override
			public void onError(int errorCode, String content) {
				dismissLoadingDialog();
				showErrorConfirmDialog(content);
			}
		}, account, password);
	}

}
