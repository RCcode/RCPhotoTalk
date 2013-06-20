package com.rcplatform.phototalk;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.activity.ImagePickActivity;
import com.rcplatform.phototalk.api.PhotoTalkApiUrl;
import com.rcplatform.phototalk.bean.AppInfo;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.image.downloader.ImageOptionsFactory;
import com.rcplatform.phototalk.listener.RCPlatformOnClickListener;
import com.rcplatform.phototalk.request.JSONConver;
import com.rcplatform.phototalk.request.PhotoTalkParams;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.request.RCPlatformServiceError;
import com.rcplatform.phototalk.request.Request;
import com.rcplatform.phototalk.task.ContactUploadTask;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.PhotoTalkUtils;
import com.rcplatform.phototalk.utils.PrefsUtils;
import com.rcplatform.phototalk.utils.RCPlatformTextUtil;
import com.rcplatform.phototalk.utils.Utils;
import com.rcplatform.phototalk.views.HorizontalListView;

public class PlatformEditActivity extends ImagePickActivity {

	public static final String PARAM_USER = "user";
	public static final String PARAM_USER_APPS = "userapps";
	private ImageView ivHead;
	private EditText etNick;
	private HorizontalListView vpUsers;
	private ImageLoader mImageLoader;
	private UserInfo mUserInfo;
	private String mHeadImagePath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		cancelRelogin();
		setContentView(R.layout.platform_user_edit);
		mImageLoader = ImageLoader.getInstance();
		initView();
	}

	private void initTitle() {
		initBackButton(R.string.user_info, mOnClickListener);
		initForwordButton(R.drawable.ok_done_btn, mOnClickListener);
	}

	private void initView() {
		initTitle();
		ivHead = (ImageView) findViewById(R.id.iv_head);
		ivHead.setOnClickListener(mOnClickListener);
		etNick = (EditText) findViewById(R.id.et_nick);

		etNick.setHintTextColor(getResources().getColor(R.color.register_input_hint));

		etNick.addTextChangedListener(new TextWatcher() {

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				etNick.setHintTextColor(getResources().getColor(R.color.register_input_hint));
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {

			}
		});

		vpUsers = (HorizontalListView) findViewById(R.id.vp_accounts);
		Map<AppInfo, UserInfo> userApps = (Map<AppInfo, UserInfo>) getIntent().getSerializableExtra(PARAM_USER_APPS);
		final BaseAdapter adapter = new AccountAdapter(userApps);
		vpUsers.setAdapter(adapter);
		vpUsers.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				UserInfo userInfo = (UserInfo) adapter.getItem(arg2);
				mUserInfo = userInfo;
				setUserInfo();
			}
		});
		mImageLoader.displayImage(null, ivHead);
	}

	private void setUserInfo() {
		etNick.setText(mUserInfo.getNickName());
		mHeadImagePath = mUserInfo.getHeadUrl();
		mImageLoader.displayImage(mUserInfo.getHeadUrl(), ivHead, ImageOptionsFactory.getCircleImageOption());
	}

	class AccountAdapter extends BaseAdapter {
		private Map<AppInfo, UserInfo> mUserApps;
		private List<AppInfo> mAppInfos;

		public AccountAdapter(Map<AppInfo, UserInfo> userApps) {
			this.mUserApps = userApps;
			this.mAppInfos = new ArrayList<AppInfo>(mUserApps.keySet());
		}

		@Override
		public int getCount() {
			return mAppInfos.size();
		}

		@Override
		public Object getItem(int position) {
			return mUserApps.get(mAppInfos.get(position));
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.user_app_info_item, null);
			}
			AppInfo appInfo = mAppInfos.get(position);
			UserInfo userInfo = mUserApps.get(appInfo);
			if (mUserInfo == null)
				mUserInfo = userInfo;
			TextView tvNick = (TextView) convertView.findViewById(R.id.tv_nick);
			ImageView ivHead = (ImageView) convertView.findViewById(R.id.iv_head);
			TextView tvAppName = (TextView) convertView.findViewById(R.id.tv_app_name);
			mImageLoader.displayImage(userInfo.getHeadUrl(), ivHead, ImageOptionsFactory.getCircleImageOption());
			tvNick.setText(userInfo.getNickName());
			tvAppName.setText(appInfo.getAppName());
			return convertView;
		}

	}

	private class UserInfoAdapter extends PagerAdapter {
		private List<View> mViews = new ArrayList<View>();
		private Map<AppInfo, UserInfo> mData;
		private List<AppInfo> mKeys;

		public UserInfoAdapter(Map<AppInfo, UserInfo> appUsers) {
			mKeys = new ArrayList<AppInfo>(appUsers.keySet());
			this.mData = appUsers;
			init(mKeys);
		}

		private void init(List<AppInfo> apps) {
			for (AppInfo appInfo : apps) {
				UserInfo userInfo = mData.get(appInfo);
				View convertView = getLayoutInflater().inflate(R.layout.user_app_info_item, null);
				if (mUserInfo == null)
					mUserInfo = userInfo;
				TextView tvNick = (TextView) convertView.findViewById(R.id.tv_nick);
				ImageView ivHead = (ImageView) convertView.findViewById(R.id.iv_head);
				TextView tvAppName = (TextView) convertView.findViewById(R.id.tv_app_name);
				mImageLoader.displayImage(userInfo.getHeadUrl(), ivHead);
				tvNick.setText(userInfo.getNickName());
				tvAppName.setText(appInfo.getAppName());
				mViews.add(convertView);
			}
		}

		@Override
		public int getCount() {
			return mViews.size();
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			super.destroyItem(container, position, object);
			container.removeView(mViews.get(position));
		}

		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			View view = mViews.get(position);
			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					UserInfo userInfo = (UserInfo) mData.get(mKeys.get(position));
					mUserInfo = userInfo;
					setUserInfo();
				}
			});
			container.addView(view);
			return view;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	private OnClickListener mOnClickListener = new RCPlatformOnClickListener(this) {

		@Override
		public void onViewClick(View v) {
			switch (v.getId()) {
			case R.id.back:
				finish();
				break;
			case R.id.iv_head:
				showImagePickMenu(v, CROP_HEAD_IMAGE);
				break;
			case R.id.choosebutton:
				updateUserInfo();
				break;
			}
		}
	};

	private boolean checkInfo(String nick) {
		if (!RCPlatformTextUtil.isNickMatches(nick)) {
			DialogUtil.createMsgDialog(this, getString(R.string.register_nick_empty), getString(R.string.ok)).show();
			return false;
		}
		return true;
	}

	private void updateUserInfo() {
		String nick = etNick.getText().toString().trim();
		if (nick.equals("")) {
			etNick.setHintTextColor(getResources().getColor(R.color.register_input_hint_error));
			etNick.requestFocus();
			InputMethodManager imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
			imm.showSoftInput(etNick, 0);
			return;
		}
		if (checkInfo(nick)) {
			Request request = new Request(this, PhotoTalkApiUrl.RCPLATFORM_ACCTION_CREATE_USERINFO, mResponseHandler);
			request.putParam(PhotoTalkParams.PARAM_KEY_USER_ID, mUserInfo.getRcId());
			request.putParam(PhotoTalkParams.CreateUserInfo.PARAM_KEY_NICK, nick);
			if ((!RCPlatformTextUtil.isEmpty(mHeadImagePath) && mHeadImagePath.startsWith("http://"))) {
				request.putParam(PhotoTalkParams.CreateUserInfo.PARAM_KEY_HEAD_URL, mHeadImagePath);
			} else if (!RCPlatformTextUtil.isEmpty(mHeadImagePath)) {
				request.setFile(new File(mHeadImagePath));
			}
			request.putParam(PhotoTalkParams.CreateUserInfo.PARAM_KEY_TIMEZONE, Utils.getTimeZoneId(this) + "");
			request.putParam(PhotoTalkParams.CreateUserInfo.PARAM_KEY_COUNTRY, Constants.COUNTRY);
			request.putParam(PhotoTalkParams.CreateUserInfo.PARAM_KEY_TOKEN, mUserInfo.getToken());
			showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
			request.executePostNameValuePairAsync();
		}
	}

	private RCPlatformResponseHandler mResponseHandler = new RCPlatformResponseHandler() {

		@Override
		public void onSuccess(int statusCode, String content) {
//06-20 21:28:53.165: E/PhotoTalk(21650): {"message":"成功","rcId":1000012,"recommendUsers":[],"email":"a@a.com","token":"x5lPlqbz1XdsVhBxmCBVPOQzzMOGL1d2rN22b6DY0XqQMRSBEZMa2cqfPwl5iulm","status":10000,"tgId":"1000012_1","tgpwd":"96e79218965eb72c92a549dd5a330112"}

			try {
				JSONObject jsonObject = new JSONObject(content);
				String tigaseId=jsonObject.getString("tgId");
				String tigasePwd=jsonObject.getString("tgpwd");
				List<Friend> recommends = JSONConver.jsonToFriends(jsonObject.getJSONArray("recommendUsers").toString());
				UserInfo userInfo = JSONConver.jsonToUserInfo(content);
				userInfo.setNickName(mUserInfo.getNickName());
				userInfo.setAppId(Constants.APP_ID);
				userInfo.setDeviceId(Constants.DEVICE_ID);
				userInfo.setTigaseId(tigaseId);
				userInfo.setTigasePwd(tigasePwd);
				PrefsUtils.LoginState.setLoginUser(getApplicationContext(), userInfo);
				getPhotoTalkApplication().setCurrentUser(userInfo);
				PhotoTalkDatabaseFactory.getDatabase().saveRecommends(recommends);
				loginSuccess(userInfo);
			} catch (JSONException e) {
				e.printStackTrace();
				onFailure(RCPlatformServiceError.ERROR_CODE_REQUEST_FAIL, getString(R.string.net_error));
			}
			dismissLoadingDialog();
		}

		@Override
		public void onFailure(int errorCode, String content) {
			dismissLoadingDialog();
			showErrorConfirmDialog(content);
		}
	};

	private void loginSuccess(UserInfo userInfo) {
		ContactUploadTask task = ContactUploadTask.createNewTask(this);
		PhotoTalkDatabaseFactory.getDatabase().addFriend(PhotoTalkUtils.userToFriend(userInfo));
		task.setLogin();
		task.startUpload();
		Intent intent = new Intent(this, InitPageActivity.class);
		intent.putExtra(PARAM_USER, userInfo);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	@Override
	protected void onImageReceive(Uri imageBaseUri, String imagePath) {
		super.onImageReceive(imageBaseUri, imagePath);
		mHeadImagePath = imagePath;
		mImageLoader.displayImage("file:///" + imagePath, ivHead, ImageOptionsFactory.getHeadImageOptions());
	}

	@Override
	protected void onImagePickFail() {
		super.onImagePickFail();
		DialogUtil.showToast(getApplicationContext(), R.string.image_pick_fail, Toast.LENGTH_SHORT);
	}
}
