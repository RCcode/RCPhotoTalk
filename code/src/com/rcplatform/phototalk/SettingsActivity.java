package com.rcplatform.phototalk;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.activity.ImagePickActivity;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.image.downloader.ImageOptionsFactory;
import com.rcplatform.phototalk.logic.LogicUtils;
import com.rcplatform.phototalk.logic.controller.InformationPageController;
import com.rcplatform.phototalk.logic.controller.SettingPageController;
import com.rcplatform.phototalk.proxy.FriendsProxy;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.PhotoTalkUtils;
import com.rcplatform.phototalk.utils.Utils;
import com.rcplatform.phototalk.views.RoundImageView;

public class SettingsActivity extends ImagePickActivity implements
		View.OnClickListener {

	private static final int REQUEST_CODE_EDIT_INFO = 100;
	protected static final int REQUEST_CODE_GALLARY = 1012;
	protected static final int REQUEST_CODE_CAMERA = 1013;

	private Button mCleanBtn;
	private Button editBtn;
	private RelativeLayout edit_rcId, use_account_message, my_friend_dynamic;
	private View mBack;
	private TextView mTitleTextView;
	private RoundImageView mHeadView;
	private TextView mNickView;
	private TextView userRcId;
	private TextView userAge;
	private ImageView user_bg_View;
	private ImageView tv_sex;
	private RelativeLayout viewAbout;
	private int CAMERA_CODE = 0;
	private View newTrend;
	private ImageView ivTrend;
	private ImageLoader mImageLoader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		SettingPageController.getInstance().setActivity(this);
		initTitle();
		mImageLoader = ImageLoader.getInstance();
		newTrend = findViewById(R.id.rela_new_trend);
		tv_sex = (ImageView) findViewById(R.id.tv_sex);
		ivTrend = (ImageView) findViewById(R.id.iv_trend_head);
		mHeadView = (RoundImageView) findViewById(R.id.settings_account_head_portrait);
		mHeadView.setOnClickListener(this);
		mNickView = (TextView) findViewById(R.id.settings_user_nick);
		userRcId = (TextView) findViewById(R.id.user_rc_id);
		userAge = (TextView) findViewById(R.id.tv_age);
		editBtn = (Button) findViewById(R.id.settings_user_info_edit_action);
		editBtn.setOnClickListener(this);
		edit_rcId = (RelativeLayout) findViewById(R.id.settings_user_edit_rc_id_action);
		edit_rcId.setOnClickListener(this);
		use_account_message = (RelativeLayout) findViewById(R.id.use_account_message);
		use_account_message.setOnClickListener(this);
		my_friend_dynamic = (RelativeLayout) findViewById(R.id.my_friend_dynamic);
		my_friend_dynamic.setOnClickListener(this);
		mCleanBtn = (Button) findViewById(R.id.settings_clean_history_record_btn);
		mCleanBtn.setOnClickListener(this);
		user_bg_View = (ImageView) findViewById(R.id.user_bg);
		user_bg_View.setOnClickListener(this);
		viewAbout = (RelativeLayout) findViewById(R.id.rela_about);
		viewAbout.setOnClickListener(this);
		InformationPageController.getInstance().onNewTread();
		setUserInfo(getCurrentUser());
	}

	private void setUserInfo(UserInfo userInfo) {
		setUserImage();
		switch (userInfo.getGender()) {
		case 0:
			tv_sex.setVisibility(View.GONE);
			break;
		case 1:
			tv_sex.setVisibility(View.VISIBLE);
			tv_sex.setBackgroundResource(R.drawable.boy_icon);
			break;
		case 2:
			tv_sex.setVisibility(View.VISIBLE);
			tv_sex.setBackgroundResource(R.drawable.girl_icon);
			break;
		default:
			tv_sex.setVisibility(View.GONE);
			break;
		}
		mNickView.setText("" + userInfo.getNickName());
		userRcId.setText("" + userInfo.getRcId());
		userAge.setText("" + userInfo.getAge());
	}

	private void setUserImage() {
		File fileHead = PhotoTalkUtils.getUserHead(getCurrentUser());
		if (fileHead.exists()) {
			String urlLocal = "file:///" + fileHead.getPath();
			mImageLoader.displayImage(urlLocal, mHeadView,
					ImageOptionsFactory.getHeadImageOptions());
		} else {
			mImageLoader.displayImage(getCurrentUser().getHeadUrl(), mHeadView,
					ImageOptionsFactory.getHeadImageOptions());
		}

		File fileBackground = PhotoTalkUtils
				.getUserBackground(getCurrentUser());
		if (fileBackground.exists()) {
			String urlLocal = "file:///" + fileBackground.getPath();
			mImageLoader.displayImage(urlLocal, user_bg_View, ImageOptionsFactory.getUserBackImageOptions());
		} else {
			mImageLoader.displayImage(getCurrentUser().getBackground(),
					user_bg_View, ImageOptionsFactory.getUserBackImageOptions());
		}
	}

	private void initTitle() {
		mBack = findViewById(R.id.back);
		mBack.setVisibility(View.VISIBLE);
		mBack.setOnClickListener(this);
		mTitleTextView = (TextView) findViewById(R.id.titleContent);
		mTitleTextView.setText(getResources().getString(
				R.string.my_firend_setting_more_title));
		mTitleTextView.setVisibility(View.VISIBLE);
	}

	protected void failure(JSONObject obj) {
		DialogUtil.createMsgDialog(this,
				getResources().getString(R.string.login_error),
				getResources().getString(R.string.ok)).show();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			finish();
			break;
		case R.id.my_friend_dynamic:
			startActivity(new Intent(this, FriendDynamicActivity.class));
			break;
		case R.id.settings_user_info_edit_action:
			startActivityForResult(new Intent(this,
					AccountInfoEditActivity.class), REQUEST_CODE_EDIT_INFO);
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
			Utils.copyFile(new File(imagePath),
					PhotoTalkUtils.getUserBackground(getCurrentUser()));
			new LoadImageTask(user_bg_View).execute(imageBaseUri,
					Uri.parse(imagePath));
			break;
		case 2:
			upUserInfoHeadImage(imagePath);
			Utils.copyFile(new File(imagePath),
					PhotoTalkUtils.getUserHead(getCurrentUser()));
			new LoadImageTask(mHeadView).execute(imageBaseUri,
					Uri.parse(imagePath));
			break;
		}

	}

	private void doCleanDistory() {
		LogicUtils.showInformationClearDialog(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == REQUEST_CODE_EDIT_INFO) {
				setUserInfo(getCurrentUser());
			}
		}
	}

	public void upUpdateUserBackground(final String imageUrl) {
		File file = null;
		try {
			file = new File(imageUrl);
			if (file != null) {
				FriendsProxy.upUserBackgroundImage(SettingsActivity.this, file,
						new RCPlatformResponseHandler() {
							@Override
							public void onSuccess(int statusCode, String content) {
								// 上传成功
								// userInfo.setBackground(decodeUtil(content,
								// "background"));
								// PrefsUtils.User.saveUserInfo(SettingsActivity.this,
								// userInfo.getRcId(), userInfo);
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
		SettingPageController.getInstance().destroy();
	}

	public String decodeUtil(String content, String name) {
		String headUrl = null;
		JSONObject contentJson = null;
		try {
			contentJson = new JSONObject(content);
			if (contentJson.has("userInfo")) {
				JSONObject json = new JSONObject(
						contentJson.getString("userInfo"));
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
		FriendsProxy.upUserInfoHeadImage(this, file,
				new RCPlatformResponseHandler() {

					@Override
					public void onSuccess(int statusCode, String content) {
						// userInfo.setHeadUrl(decodeUtil(content, "headUrl"));
						// PrefsUtils.User.saveUserInfo(SettingsActivity.this,
						// userInfo.getRcId(), userInfo);
					}

					@Override
					public void onFailure(int errorCode, String content) {

					}
				});
	}

	public void onNewTrend(boolean show, String url) {
		if (!show)
			newTrend.setVisibility(View.GONE);
		else {
			newTrend.setVisibility(View.VISIBLE);
			mImageLoader.displayImage(url, ivTrend);
		}

	}

}
