package com.rcplatform.phototalk;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.activity.ImagePickActivity;
import com.rcplatform.phototalk.api.MenueApiUrl;
import com.rcplatform.phototalk.bean.AppInfo;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.image.downloader.RCPlatformImageLoader;
import com.rcplatform.phototalk.listener.RCPlatformOnClickListener;
import com.rcplatform.phototalk.request.JSONConver;
import com.rcplatform.phototalk.request.PhotoTalkParams;
import com.rcplatform.phototalk.request.RCPlatformResponse;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.request.Request;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.PrefsUtils;
import com.rcplatform.phototalk.utils.RCPlatformTextUtil;

public class PlatformEditActivity extends ImagePickActivity {

	public static final String PARAM_USER = "user";
	public static final String PARAM_USER_APPS = "userapps";
	private ImageView ivHead;
	private EditText etNick;
	private GridView gvUsers;
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
		TextView tvNext = (TextView) findViewById(R.id.choosebutton);
		tvNext.setText(R.string.next);
		tvNext.setOnClickListener(mOnClickListener);
		tvNext.setVisibility(View.VISIBLE);
	}

	private void initView() {
		initTitle();
		ivHead = (ImageView) findViewById(R.id.iv_head);
		ivHead.setOnClickListener(mOnClickListener);
		etNick = (EditText) findViewById(R.id.et_nick);
		gvUsers = (GridView) findViewById(R.id.gv_accounts);
		Map<AppInfo, UserInfo> userApps = (Map<AppInfo, UserInfo>) getIntent().getSerializableExtra(PARAM_USER_APPS);
		final BaseAdapter adapter = new AccountAdapter(userApps);
		gvUsers.setAdapter(adapter);
		gvUsers.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				UserInfo userInfo = (UserInfo) adapter.getItem(position);
				mUserInfo = userInfo;
				setUserInfo();
			}
		});
		ivHead.setImageResource(R.drawable.ic_launcher);
	}

	private void setUserInfo() {
		etNick.setText(mUserInfo.getNickName());
		mHeadImagePath = mUserInfo.getHeadUrl();
		RCPlatformImageLoader.displayImage(this, ivHead, mUserInfo.getHeadUrl(), mImageLoader);
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
				convertView.setLayoutParams(new AbsListView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			}
			AppInfo appInfo = mAppInfos.get(position);
			UserInfo userInfo = mUserApps.get(appInfo);
			if (mUserInfo == null)
				mUserInfo = userInfo;
			TextView tvNick = (TextView) convertView.findViewById(R.id.tv_nick);
			ImageView ivHead = (ImageView) convertView.findViewById(R.id.iv_head);
			TextView tvAppName = (TextView) convertView.findViewById(R.id.tv_app_name);
			RCPlatformImageLoader.displayImage(PlatformEditActivity.this, ivHead, userInfo.getHeadUrl(), mImageLoader);
			tvNick.setText(userInfo.getNickName());
			tvAppName.setText(appInfo.getAppName());
			return convertView;
		}

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mImageLoader.stop();
	}

	private OnClickListener mOnClickListener = new RCPlatformOnClickListener(this) {

		@Override
		public void onViewClick(View v) {
			switch (v.getId()) {
			case R.id.title_linear_back:
				finish();
				break;
			case R.id.iv_head:
				showImagePickMenu(v,CROP_HEAD_IMAGE);
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
			Request request = new Request(this, MenueApiUrl.RCPLATFORM_ACCOUNT_LOGIN_URL, mResponseHandler);
			request.putParam(PhotoTalkParams.PARAM_KEY_USER_ID, mUserInfo.getRcId());
			request.putParam(PhotoTalkParams.PARAM_KEY_TOKEN, mUserInfo.getToken());
			request.putParam(PhotoTalkParams.PLATFORM_ACCOUNT_LOGIN.PARAM_KEY_NICK, nick);
			if ((mHeadImagePath != null && mHeadImagePath.startsWith("http://"))) {
				request.putParam(PhotoTalkParams.PLATFORM_ACCOUNT_LOGIN.PARAM_KEY_HEAD_URL, mHeadImagePath);
			} else if (mHeadImagePath != null) {
				request.setFile(new File(mHeadImagePath));
			}
			showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
			request.excutePostNameValuePairAsync();
		}
	}

	private RCPlatformResponseHandler mResponseHandler = new RCPlatformResponseHandler() {

		@Override
		public void onSuccess(int statusCode, String content) {
			dismissLoadingDialog();
			try {
				JSONObject jsonObject = new JSONObject(content);
				UserInfo userInfo = JSONConver.jsonToUserInfo(jsonObject.getJSONObject("userInfoAll").toString());
				PrefsUtils.LoginState.setLoginUser(getApplicationContext(), userInfo);
				long lastBindTime = jsonObject.optLong(RCPlatformResponse.Login.RESPONSE_KEY_LAST_BIND_TIME);
				String lastBindNumber = jsonObject.getString(RCPlatformResponse.Login.RESPONSE_KEY_LAST_BIND_NUMBER);
				PrefsUtils.User.MobilePhoneBind.setLastBindNumber(getApplicationContext(), userInfo.getRcId(), lastBindNumber);
				PrefsUtils.User.MobilePhoneBind.setLastBindPhoneTime(getApplicationContext(), lastBindTime, userInfo.getRcId());
				loginSuccess(userInfo);
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
	};

	private void loginSuccess(UserInfo userInfo) {
		Intent intent = new Intent(this, InitPageActivity.class);
		intent.putExtra(PARAM_USER, userInfo);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	@Override
	protected void onImageReceive(Uri imageBaseUri, String imagePath) {
		super.onImageReceive(imageBaseUri, imagePath);
	}

	@Override
	protected void onImageCutSuccess(String tmpPath) {
		super.onImageCutSuccess(tmpPath);
		Uri uri = Uri.parse(tmpPath);
		mHeadImagePath = tmpPath;
		new LoadImageTask(ivHead).execute(uri, uri);
	}

	@Override
	protected void onImagePickFail() {
		super.onImagePickFail();
		DialogUtil.showToast(getApplicationContext(), R.string.image_pick_fail, Toast.LENGTH_SHORT);
	}
}
