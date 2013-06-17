package com.rcplatform.videotalk;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.videotalk.R;
import com.rcplatform.videotalk.activity.ImagePickActivity;
import com.rcplatform.videotalk.api.PhotoTalkApiUrl;
import com.rcplatform.videotalk.bean.AppInfo;
import com.rcplatform.videotalk.bean.UserInfo;
import com.rcplatform.videotalk.image.downloader.ImageOptionsFactory;
import com.rcplatform.videotalk.listener.RCPlatformOnClickListener;
import com.rcplatform.videotalk.request.JSONConver;
import com.rcplatform.videotalk.request.PhotoTalkParams;
import com.rcplatform.videotalk.request.RCPlatformResponseHandler;
import com.rcplatform.videotalk.request.Request;
import com.rcplatform.videotalk.utils.Constants;
import com.rcplatform.videotalk.utils.DialogUtil;
import com.rcplatform.videotalk.utils.PrefsUtils;
import com.rcplatform.videotalk.utils.RCPlatformTextUtil;
import com.rcplatform.videotalk.utils.Utils;
import com.rcplatform.videotalk.views.HorizontalListView;

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
			DialogUtil.createMsgDialog(this, getString(R.string.register_nick_empty), getString(R.string.confirm)).show();
			return false;
		}
		return true;
	}

	private void updateUserInfo() {
		String nick = etNick.getText().toString().trim();
		if (checkInfo(nick)) {
			Request request = new Request(this, PhotoTalkApiUrl.RCPLATFORM_ACCTION_CREATE_USERINFO, mResponseHandler);
			request.putParam(PhotoTalkParams.PARAM_KEY_USER_ID, mUserInfo.getRcId());
			request.putParam(PhotoTalkParams.CreateUserInfo.PARAM_KEY_NICK, nick);
			if (!(RCPlatformTextUtil.isEmpty(mHeadImagePath) && mHeadImagePath.startsWith("http://"))) {
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
			dismissLoadingDialog();
			UserInfo userInfo = JSONConver.jsonToUserInfo(content);
			userInfo.setShowRecommends(UserInfo.NOT_FIRST_TIME);
			PrefsUtils.LoginState.setLoginUser(getApplicationContext(), userInfo);
			loginSuccess(userInfo);
		}

		@Override
		public void onFailure(int errorCode, String content) {
			dismissLoadingDialog();
			showErrorConfirmDialog(content);
		}
	};

	private void loginSuccess(UserInfo userInfo) {
		getPhotoTalkApplication().setCurrentUser(mUserInfo);
		Intent intent = new Intent(this, InitPageActivity.class);
		intent.putExtra(PARAM_USER, userInfo);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	@Override
	protected void onImageReceive(Uri imageBaseUri, String imagePath) {
		super.onImageReceive(imageBaseUri, imagePath);
		mHeadImagePath = imagePath;
		new LoadImageTask(ivHead).execute(imageBaseUri, Uri.parse(imagePath));
	}

	@Override
	protected void onImagePickFail() {
		super.onImagePickFail();
		DialogUtil.showToast(getApplicationContext(), R.string.image_pick_fail, Toast.LENGTH_SHORT);
	}
}
