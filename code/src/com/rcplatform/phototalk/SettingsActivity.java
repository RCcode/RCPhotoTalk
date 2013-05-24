package com.rcplatform.phototalk;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.activity.ImagePickActivity;
import com.rcplatform.phototalk.bean.AppInfo;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.image.downloader.ImageOptionsFactory;
import com.rcplatform.phototalk.image.downloader.RCPlatformImageLoader;
import com.rcplatform.phototalk.logic.LogicUtils;
import com.rcplatform.phototalk.proxy.FriendsProxy;
import com.rcplatform.phototalk.proxy.UserSettingProxy;
import com.rcplatform.phototalk.request.JSONConver;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.utils.AppSelfInfo;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.PrefsUtils;
import com.rcplatform.phototalk.views.RoundImageView;

public class SettingsActivity extends ImagePickActivity implements View.OnClickListener {

	private static final int REQUEST_CODE_EDIT_INFO = 100;
	protected static final int REQUEST_CODE_GALLARY = 1012;
	protected static final int REQUEST_CODE_CAMERA = 1013;

	private Button mCleanBtn;
	private Button editBtn;
	private UserInfo userInfo;
	private RelativeLayout edit_rcId, use_account_message;
	private View mBack;
	private TextView mTitleTextView;
	private RoundImageView mHeadView;
	private TextView mNickView;
	private TextView userRcId;
	private ImageView user_bg_View;
	private PopupWindow mImageSelectPopupWindow;
	private Uri mImageUri;
	private RelativeLayout viewAbout;
	private PhotoTalkApplication app;
	private int CAMERA_CODE = 0;
	private View newTrend;
	private ImageView ivTrend;
	private ImageLoader mImageLoader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		app = (PhotoTalkApplication) getApplication();
		initTitle();
		mImageLoader = ImageLoader.getInstance();
		newTrend = findViewById(R.id.rela_new_trend);
		ivTrend = (ImageView) findViewById(R.id.iv_trend_head);
		mHeadView = (RoundImageView) findViewById(R.id.settings_account_head_portrait);
		mHeadView.setOnClickListener(this);
		mNickView = (TextView) findViewById(R.id.settings_user_nick);
		userRcId = (TextView) findViewById(R.id.user_rc_id);
		editBtn = (Button) findViewById(R.id.settings_user_info_edit_action);
		editBtn.setOnClickListener(this);
		edit_rcId = (RelativeLayout) findViewById(R.id.settings_user_edit_rc_id_action);
		edit_rcId.setOnClickListener(this);
		use_account_message = (RelativeLayout) findViewById(R.id.use_account_message);
		use_account_message.setOnClickListener(this);
		// mHrzListView = (HorizontalListView)
		// findViewById(R.id.my_friend_details_apps_listview);
		mCleanBtn = (Button) findViewById(R.id.settings_clean_history_record_btn);
		mCleanBtn.setOnClickListener(this);
		user_bg_View = (ImageView) findViewById(R.id.user_bg);
		user_bg_View.setOnClickListener(this);
		userInfo = getPhotoTalkApplication().getCurrentUser();
		viewAbout = (RelativeLayout) findViewById(R.id.rela_about);
		viewAbout.setOnClickListener(this);
		setUserInfo(userInfo);
		getAllApps();
	}

	private void getAllApps() {
		UserSettingProxy.getAllAppInfo(this, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				try {
					List<AppInfo> apps = JSONConver.jsonToAppInfos(new JSONObject(content).getJSONArray("allApps").toString());
					PhotoTalkDatabaseFactory.getGlobalDatabase().savePlatformAppInfos(apps);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(int errorCode, String content) {
			}
		});
	}

	private void setUserInfo(UserInfo userInfo) {
		RCPlatformImageLoader.loadImage(SettingsActivity.this, ImageLoader.getInstance(), ImageOptionsFactory.getHeadImageOptions(), userInfo.getHeadUrl(),
				AppSelfInfo.ImageScaleInfo.thumbnailImageWidthPx, mHeadView, R.drawable.default_head);
		mNickView.setText("" + userInfo.getNickName());
		userRcId.setText("" + userInfo.getRcId());
		RCPlatformImageLoader.loadImage(SettingsActivity.this, ImageLoader.getInstance(), ImageOptionsFactory.getDefaultImageOptions(),
				userInfo.getBackground(), AppSelfInfo.ImageScaleInfo.circleUserHeadRadius, user_bg_View, R.drawable.user_detail_bg);
	}

	private void initTitle() {
		mBack = findViewById(R.id.back);
		mBack.setVisibility(View.VISIBLE);
		mBack.setOnClickListener(this);
		mTitleTextView = (TextView) findViewById(R.id.titleContent);
		mTitleTextView.setText(getResources().getString(R.string.my_firend_setting_more_title));
		mTitleTextView.setVisibility(View.VISIBLE);
	}

	protected void failure(JSONObject obj) {
		DialogUtil.createMsgDialog(this, getResources().getString(R.string.login_error), getResources().getString(R.string.ok)).show();
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
		case R.id.use_account_message:
			startActivity(new Intent(this, UserInfoActivity.class));
			break;
		case R.id.choosebutton:
			startActivity(new Intent(this, AddFriendsActivity.class));
			break;
		case R.id.settings_clean_history_record_btn:
			doCleanDistory();
			break;
		case R.id.user_bg:
			// 点击更改背景图片
			CAMERA_CODE = CROP_BACKGROUND_IMAGE;
			showImagePickMenu(user_bg_View, CROP_BACKGROUND_IMAGE);
			break;
		case R.id.settings_user_edit_rc_id_action:
			startActivity(SystemSettingActivity.class);
			break;
		case R.id.settings_account_head_portrait:
			// 更改个人头像设置
			CAMERA_CODE = CROP_HEAD_IMAGE;
			showImagePickMenu(mHeadView, CROP_HEAD_IMAGE);
			break;
		case R.id.rela_about:
			startActivity(AboutActivity.class);
			break;
		}
	}

	@Override
	protected void onImageReceive(Uri imageBaseUri, String imagePath) {
		super.onImageReceive(imageBaseUri, imagePath);
		// 加是判断是背景还是头像设置 两个上传和保存顺序不同 后期需要优化
		switch (CAMERA_CODE) {
		case 1:
			// 背景上传
			upUpdateUserBackground(imagePath);
			new LoadImageTask(user_bg_View).execute(imageBaseUri, Uri.parse(imagePath));
			break;

		case 2:
			upUserInfoHeadImage(imagePath);
			new LoadImageTask(mHeadView).execute(imageBaseUri, Uri.parse(imagePath));
			break;
		}

	}

	/*
	 * 
	 * class LoadImageTask extends AsyncTask<Uri, Void, Bitmap> {
	 * 
	 * @Override protected void onPreExecute() { super.onPreExecute();
	 * showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false); }
	 * 
	 * @Override protected Bitmap doInBackground(Uri... params) { Uri imageUri =
	 * params[0]; String headPath = params[1].getPath(); Bitmap bitmap = null;
	 * try { int rotateAngel = Utils.getUriImageAngel(SettingsActivity.this,
	 * imageUri); int nWidth = 0, nHeight = 0; nHeight =
	 * user_bg_View.getHeight(); nWidth = user_bg_View.getWidth(); bitmap =
	 * Utils.decodeSampledBitmapFromFile(headPath, nWidth, nHeight,
	 * rotateAngel); } catch (OutOfMemoryError e) { e.printStackTrace(); } catch
	 * (Exception e) { e.printStackTrace(); } return bitmap; }
	 * 
	 * @Override protected void onPostExecute(Bitmap result) {
	 * super.onPostExecute(result); dismissLoadingDialog(); String url= null;
	 * try { url = cacheHeadImage(result); } catch (Exception e) {
	 * e.printStackTrace(); } user_bg_View.setImageBitmap(result); //上传
	 * postImage(url); } }
	 */
	private Bitmap getBitmap(String url) {
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeFile(url);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	private void doCleanDistory() {
		LogicUtils.showInformationClearDialog(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == REQUEST_CODE_EDIT_INFO) {
				setUserInfo((UserInfo) data.getSerializableExtra(AccountInfoEditActivity.RESULT_PARAM_USER));
				setUserInfo((UserInfo) data.getSerializableExtra(AccountInfoEditActivity.RESULT_PARAM_USER));
			}
		}
	}

	public void upUpdateUserBackground(final String imageUrl) {
		File file = null;
		try {
			file = new File(imageUrl);
			if (file != null) {
				FriendsProxy.upUserBackgroundImage(SettingsActivity.this, file, new RCPlatformResponseHandler() {
					@Override
					public void onSuccess(int statusCode, String content) {
						// 上传成功
						userInfo.setBackground(decodeUtil(content, "background"));
						PrefsUtils.User.saveUserInfo(SettingsActivity.this, userInfo.getRcId(), userInfo);
					}

					@Override
					public void onFailure(int errorCode, String content) {
						// 上传失败
					}
				});
			}

		} catch (Exception e) {
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	public String decodeUtil(String content, String name) {
		String headUrl = null;
		JSONObject contentJson = null;
		try {
			contentJson = new JSONObject(content);
			if (contentJson.has("userInfo")) {
				JSONObject json = new JSONObject(contentJson.getString("userInfo"));
				if (json.has("headUrl")) {
					headUrl = json.getString(name);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return headUrl;
	}

	private void upUserInfoHeadImage(String path) {
		// 资料发生改变 上传服务器
		File file = new File(path);
		FriendsProxy.upUserInfoHeadImage(this, file, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				userInfo.setHeadUrl(decodeUtil(content, "headUrl"));
				PrefsUtils.User.saveUserInfo(SettingsActivity.this, userInfo.getRcId(), userInfo);
			}

			@Override
			public void onFailure(int errorCode, String content) {

			}
		});
	}

	/*
	 * class LoadHeadImageTask extends AsyncTask<Uri, Void, Bitmap> {
	 * 
	 * @Override protected void onPreExecute() { super.onPreExecute();
	 * showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false); }
	 * 
	 * @Override protected Bitmap doInBackground(Uri... params) { Uri imageUri =
	 * params[0]; String headPath = params[1].getPath(); Bitmap bitmap = null;
	 * try { int rotateAngel = Utils.getUriImageAngel( SettingsActivity.this,
	 * imageUri); int nWidth = 0, nHeight = 0; nHeight = mHeadView.getHeight();
	 * nWidth = mHeadView.getWidth(); bitmap =
	 * Utils.decodeSampledBitmapFromFile(headPath, nWidth, nHeight,
	 * rotateAngel); bitmap = Utils.getRectBitmap(bitmap); } catch
	 * (OutOfMemoryError e) { e.printStackTrace(); } catch (Exception e) {
	 * e.printStackTrace(); } return bitmap; }
	 * 
	 * @Override protected void onPostExecute(Bitmap result) {
	 * super.onPostExecute(result); dismissLoadingDialog(); if (result == null)
	 * { DialogUtil.showToast(getApplicationContext(), R.string.image_unsupport,
	 * Toast.LENGTH_SHORT); finish(); } else { try {
	 * upUserInfoHeadImage(cacheHeadImage(result)); } catch (Exception e) {
	 * e.printStackTrace(); } mHeadView.setImageBitmap(result); } } }
	 */
	private String cacheHeadImage(Bitmap bitmap) throws Exception {
		String cachePath = null;
		File file = new File(app.getBackgroundCachePath(), Constants.HEAD_CACHE_PATH);
		FileOutputStream fos = new FileOutputStream(file);
		bitmap.compress(CompressFormat.PNG, 100, fos);
		cachePath = file.getPath();
		userInfo.setHeadUrl(cachePath);
		fos.flush();
		fos.close();
		return cachePath;
	}

	public void onNewTrend(boolean show, String url) {
		if (!show)
			newTrend.setVisibility(View.GONE);
		else {
			newTrend.setVisibility(View.VISIBLE);
			RCPlatformImageLoader.displayImage(this, ivTrend, url, mImageLoader);
		}

	}

}
