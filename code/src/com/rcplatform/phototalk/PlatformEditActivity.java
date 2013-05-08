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
import com.rcplatform.phototalk.api.JSONConver;
import com.rcplatform.phototalk.api.MenueApiUrl;
import com.rcplatform.phototalk.api.PhotoTalkParams;
import com.rcplatform.phototalk.api.RCPlatformAsyncHttpClient;
import com.rcplatform.phototalk.api.RCPlatformAsyncHttpClient.RequestAction;
import com.rcplatform.phototalk.api.RCPlatformResponse;
import com.rcplatform.phototalk.api.RCPlatformResponseHandler;
import com.rcplatform.phototalk.bean.AppInfo;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.image.downloader.RCPlatformImageLoader;
import com.rcplatform.phototalk.listener.RCPlatformOnClickListener;
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
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
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
				// TODO Auto-generated method stub
				UserInfo userInfo = (UserInfo) adapter.getItem(position);
				mUserInfo = userInfo;
				setUserInfo();
			}
		});
		ivHead.setImageResource(R.drawable.ic_launcher);
	}

	private void setUserInfo() {
		etNick.setText(mUserInfo.getNick());
		mHeadImagePath = mUserInfo.getHeadUrl();
		RCPlatformImageLoader.displayImage(this, ivHead, mUserInfo.getHeadUrl(), mImageLoader);
	}

	class AccountAdapter extends BaseAdapter {
		private Map<AppInfo, UserInfo> mUserApps;
		private List<AppInfo> mAppInfos;

		public AccountAdapter(Map<AppInfo, UserInfo> userApps) {
			// TODO Auto-generated constructor stub
			this.mUserApps = userApps;
			this.mAppInfos = new ArrayList<AppInfo>(mUserApps.keySet());
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mAppInfos.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return mUserApps.get(mAppInfos.get(position));
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
			tvNick.setText(userInfo.getNick());
			tvAppName.setText(appInfo.getAppName());
			return convertView;
		}

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mImageLoader.stop();
	}

	private OnClickListener mOnClickListener = new RCPlatformOnClickListener(this) {

		@Override
		public void onViewClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.title_linear_back:
				finish();
				break;
			case R.id.iv_head:
				showImagePickMenu(v);
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
			RCPlatformAsyncHttpClient httpClient = new RCPlatformAsyncHttpClient(RequestAction.FILE);
			PhotoTalkParams.buildBasicParams(this, httpClient);
			httpClient.putRequestParam(PhotoTalkParams.PARAM_KEY_USER_ID, mUserInfo.getSuid());
			httpClient.putRequestParam(PhotoTalkParams.PARAM_KEY_TOKEN, mUserInfo.getToken());
			httpClient.putRequestParam(PhotoTalkParams.PLATFORM_ACCOUNT_LOGIN.PARAM_KEY_NICK, nick);
			if ((mHeadImagePath != null && mHeadImagePath.startsWith("http://"))) {
				httpClient.putRequestParam(PhotoTalkParams.PLATFORM_ACCOUNT_LOGIN.PARAM_KEY_HEAD_URL, mHeadImagePath);
				httpClient.postFile(this, MenueApiUrl.RCPLATFORM_ACCOUNT_LOGIN_URL, null, mResponseHandler);
			} else if (mHeadImagePath != null) {
				httpClient.postFile(this, MenueApiUrl.RCPLATFORM_ACCOUNT_LOGIN_URL, new File(mHeadImagePath), mResponseHandler);
			} else {
				httpClient.postFile(this, MenueApiUrl.RCPLATFORM_ACCOUNT_LOGIN_URL, null, mResponseHandler);
			}
			showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);

		}
	}

	private RCPlatformResponseHandler mResponseHandler = new RCPlatformResponseHandler() {

		@Override
		public void onSuccess(int statusCode, String content) {
			// TODO Auto-generated method stub
			dismissLoadingDialog();
			try {
				JSONObject jsonObject = new JSONObject(content);
				UserInfo userInfo = JSONConver.jsonToUserInfo(jsonObject.getJSONObject("userInfoAll").toString());
				PrefsUtils.LoginState.setLoginUser(getApplicationContext(), userInfo);
				long lastBindTime = jsonObject.optLong(RCPlatformResponse.Login.RESPONSE_KEY_LAST_BIND_TIME);
				String lastBindNumber = jsonObject.getString(RCPlatformResponse.Login.RESPONSE_KEY_LAST_BIND_NUMBER);
				PrefsUtils.User.setLastBindNumber(getApplicationContext(), userInfo.getEmail(), lastBindNumber);
				PrefsUtils.User.setLastBindPhoneTime(getApplicationContext(), lastBindTime, userInfo.getEmail());
				loginSuccess(userInfo);
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
	};

	private void loginSuccess(UserInfo userInfo) {
		Intent intent = new Intent(this, InitPageActivity.class);
		intent.putExtra(PARAM_USER, userInfo);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	@Override
	protected void onImageReceive(Uri imageBaseUri, String imagePath) {
		// TODO Auto-generated method stub
		super.onImageReceive(imageBaseUri, imagePath);
		cutImage(imageBaseUri);
	}

	@Override
	protected void onImageCutSuccess(String tmpPath) {
		// TODO Auto-generated method stub
		super.onImageCutSuccess(tmpPath);
		Uri uri = Uri.parse(tmpPath);
		mHeadImagePath = tmpPath;
		new LoadImageTask(ivHead).execute(uri, uri);
	}

	@Override
	protected void onImagePickFail() {
		// TODO Auto-generated method stub
		super.onImagePickFail();
		DialogUtil.showToast(getApplicationContext(), R.string.image_pick_fail, Toast.LENGTH_SHORT);
	}
}
