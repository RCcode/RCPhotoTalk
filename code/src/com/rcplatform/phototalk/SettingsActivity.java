package com.rcplatform.phototalk;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.activity.ImagePickActivity;
import com.rcplatform.phototalk.api.RCPlatformResponseHandler;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.image.downloader.ImageOptionsFactory;
import com.rcplatform.phototalk.image.downloader.RCPlatformImageLoader;
import com.rcplatform.phototalk.logic.LogicUtils;
import com.rcplatform.phototalk.proxy.FriendsProxy;
import com.rcplatform.phototalk.utils.AppSelfInfo;
import com.rcplatform.phototalk.utils.Contract.Action;
import com.rcplatform.phototalk.utils.Contract;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.PrefsUtils;
import com.rcplatform.phototalk.utils.Utils;
import com.rcplatform.phototalk.views.HeadImageView;
import com.rcplatform.phototalk.views.HorizontalListView;

public class SettingsActivity extends ImagePickActivity implements View.OnClickListener {

	private static final String TAG = "MyFriendsActivity";

	private static final int REQUEST_CODE_EDIT_INFO = 100;
	protected static final int REQUEST_CODE_GALLARY = 1012;
	protected static final int REQUEST_CODE_CAMERA = 1013;

	private Context mContext;
	private Button mCleanBtn;
	private Button editBtn;
	private UserInfo userInfo;
	private RelativeLayout edit_rcId, use_account_message;
	private HorizontalListView mHrzListView;
	private View mBack;
	private TextView mTitleTextView;
	private HeadImageView mHeadView;
	private TextView mNickView;
	private TextView userRcId;
	private ImageView user_bg_View;
	private PopupWindow mImageSelectPopupWindow;
	private Uri mImageUri;
	private RelativeLayout viewAbout;
	private MenueApplication app;
	private String saveBgUrl=null;
	private int CAMERA_CODE = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		app = (MenueApplication) getApplication();
		mContext = this;
		initTitle();
		mHeadView = (HeadImageView) findViewById(R.id.settings_account_head_portrait);
		mHeadView.setOnClickListener(this);
		mNickView = (TextView) findViewById(R.id.settings_user_nick);
		userRcId = (TextView) findViewById(R.id.user_rc_id);
		editBtn = (Button) findViewById(R.id.settings_user_info_edit_action);
		editBtn.setOnClickListener(this);
		edit_rcId = (RelativeLayout) findViewById(R.id.settings_user_edit_rc_id_action);
		edit_rcId.setOnClickListener(this);
		use_account_message = (RelativeLayout) findViewById(R.id.use_account_message);
		use_account_message.setOnClickListener(this);
		mHrzListView = (HorizontalListView) findViewById(R.id.my_friend_details_apps_listview);
		mCleanBtn = (Button) findViewById(R.id.settings_clean_history_record_btn);
		mCleanBtn.setOnClickListener(this);
		user_bg_View = (ImageView) findViewById(R.id.user_bg);
		user_bg_View.setOnClickListener(this);
		userInfo = getPhotoTalkApplication().getCurrentUser();
		viewAbout = (RelativeLayout)findViewById(R.id.rela_about);
		viewAbout.setOnClickListener(this);
		setUserInfo(userInfo);
	}

	private void setUserInfo(UserInfo userInfo) {
		RCPlatformImageLoader.loadImage(SettingsActivity.this, ImageLoader.getInstance(), ImageOptionsFactory.getHeadImageOptions(), userInfo.getHeadUrl(), AppSelfInfo.ImageScaleInfo.thumbnailImageWidthPx, mHeadView, R.drawable.default_head);
		mNickView.setText("" + userInfo.getNick());
		userRcId.setText("" + userInfo.getRcId());
		if (userInfo.getBackground() != null) {
			String url = app.getBackgroundCachePath()+"/"+getFileName(userInfo.getBackground());
			File file = new File(url);
			if(file.exists()){
			user_bg_View.setBackgroundDrawable(new BitmapDrawable(
					getBitmap(userInfo.getBackground())));
			}else{
				//图片有更新 下载新的图片
				RCPlatformImageLoader.loadImage(SettingsActivity.this,
						ImageLoader.getInstance(),
						ImageOptionsFactory.getHeadImageOptions(),
						userInfo.getBackground(),
						AppSelfInfo.ImageScaleInfo.thumbnailImageWidthPx, user_bg_View,
						R.drawable.default_head);
			}
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
			startActivity(new Intent(this, AddFriendActivity.class));
			break;
		case R.id.settings_clean_history_record_btn:
			doCleanDistory();
			break;
		case R.id.user_bg:
			// 点击更改背景图片
			CAMERA_CODE = 1;
			showImagePickMenu(user_bg_View);
			break;
		case R.id.settings_user_edit_rc_id_action:
			startActivity(SystemSettingActivity.class);
			break;
		case R.id.settings_account_head_portrait:
			//更改个人头像设置
			CAMERA_CODE = 2;
			showImagePickMenu(mHeadView);
			break;
		case R.id.rela_about:
			startActivity(AboutActivity.class);
			break;
		}
	}

	@Override
	protected void onImageReceive(Uri imageBaseUri, String imagePath) {
		// TODO Auto-generated method stub
		super.onImageReceive(imageBaseUri, imagePath);
		// 加是判断是背景还是头像设置 两个上传和保存顺序不同 后期需要优化
		switch (CAMERA_CODE) {
		case 1:
			postImage(imagePath,imageBaseUri);
			break;

		case 2:
			new LoadHeadImageTask().execute(imageBaseUri, Uri.parse(imagePath));
			break;
		}
		

	}

	class LoadImageTask extends AsyncTask<Uri, Void, Bitmap> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		}

		@Override
		protected Bitmap doInBackground(Uri... params) {
			// TODO Auto-generated method stub
			Uri imageUri = params[0];
			String headPath = params[1].getPath();
			Bitmap bitmap = null;
			try {
				int rotateAngel = Utils.getUriImageAngel(SettingsActivity.this, imageUri);
				int nWidth = 0, nHeight = 0;
				nHeight = user_bg_View.getHeight();
				nWidth = user_bg_View.getWidth();
				bitmap = Utils.decodeSampledBitmapFromFile(headPath, nWidth, nHeight, rotateAngel);
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			dismissLoadingDialog();
			saveEditedPictrue(result);
			user_bg_View.setBackgroundDrawable(new BitmapDrawable(result));
		}
	}

	private Bitmap getBitmap(String url) {
		Bitmap bitmap = null;
		try {
			bitmap = BitmapFactory.decodeFile(url);
		} catch (OutOfMemoryError e) {
			e.printStackTrace();
		}
		return bitmap;
	}

	public void saveEditedPictrue(final Bitmap bitmap) {
		File file = new File(saveBgUrl);
		userInfo.setBackground(saveBgUrl);
		PrefsUtils.User.saveUserInfo(this, userInfo.getEmail(), userInfo);
		try {
			if (file.exists()) {
				file.delete();
			}
			if (!file.exists())
				file.createNewFile();
			BufferedOutputStream os = new BufferedOutputStream(new FileOutputStream(file)); //
			// b.compress(Bitmap.CompressFormat.JPEG, 100, os);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
			os.flush();
			os.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	private void doCleanDistory() {
		LogicUtils.showInformationClearDialog(this);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == REQUEST_CODE_EDIT_INFO) {
				setUserInfo((UserInfo) data.getSerializableExtra(AccountInfoEditActivity.RESULT_PARAM_USER));
				setUserInfo((UserInfo) data.getSerializableExtra(AccountInfoEditActivity.RESULT_PARAM_USER));
			}
		}
	}

	public void postImage(final String imageUrl,final Uri imageBaseUri) {
		File file = null;
		try {
			file = new File(imageUrl);
			if (file != null) {
				FriendsProxy.upUserBackgroundImage(SettingsActivity.this, file, new RCPlatformResponseHandler() {
							@Override
							public void onSuccess(int statusCode, String content) {
								// TODO Auto-generated method stub
								// 上传成功
								saveBgUrl = decodeUtil(content);
								new LoadImageTask().execute(imageBaseUri, Uri.parse(imageUrl));
							}

							@Override
							public void onFailure(int errorCode, String content) {
								// TODO Auto-generated method stub
								// 上传失败
							}
						});
			}

		} catch (Exception e) {
			// TODO: handle exception
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	public String decodeUtil(String content){
		String headUrl = null;
		JSONObject contentJson = null;
		try {
			contentJson = new JSONObject(content);
			if(contentJson.has("userInfo")){
				JSONObject json = new JSONObject(contentJson.getString("userInfo"));
			if(json.has("headUrl")){
				headUrl = json.getString("headUrl");
				headUrl = app.getBackgroundCachePath()+"/"+getFileName(headUrl);
			}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return headUrl;
	}
	
	public static String getFileName(String imageUrl) {
		String fileName = "";
		if (imageUrl != null && imageUrl.length() != 0) {
			fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
		}
		return fileName;
	}
	
	
	private void updateUserInfo(String url) {
		// 资料发生改变 上传服务器
			File file = file = new File(url);
			FriendsProxy.upUserInfo(
					this,
					file,
					new RCPlatformResponseHandler() {

						@Override
						public void onSuccess(int statusCode, String content) {
							// TODO Auto-generated method stub
						}

						@Override
						public void onFailure(int errorCode, String content) {
							// TODO Auto-generated method stub

						}
					}, userInfo.getNick(), userInfo.getBirthday(),
					userInfo.getSex() + "");
		}
	
	class LoadHeadImageTask extends AsyncTask<Uri, Void, Bitmap> {

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		}

		@Override
		protected Bitmap doInBackground(Uri... params) {
			// TODO Auto-generated method stub
			Uri imageUri = params[0];
			String headPath = params[1].getPath();
			Bitmap bitmap = null;
			try {
				int rotateAngel = Utils.getUriImageAngel(
						SettingsActivity.this, imageUri);
				int nWidth = 0, nHeight = 0;
				nHeight = mHeadView.getHeight();
				nWidth = mHeadView.getWidth();
				bitmap = Utils.decodeSampledBitmapFromFile(headPath, nWidth,
						nHeight, rotateAngel);
				bitmap = Utils.getRectBitmap(bitmap);
				if (bitmap != null) {
					updateUserInfo(cacheHeadImage(bitmap));
				}
			} catch (OutOfMemoryError e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return bitmap;
		}

		@Override
		protected void onPostExecute(Bitmap result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);
			dismissLoadingDialog();
			if (result == null) {
				DialogUtil.showToast(getApplicationContext(),
						R.string.image_unsupport, Toast.LENGTH_SHORT);
				finish();
			} else {
				mHeadView.setImageBitmap(result);
			}
		}
	}

	private String cacheHeadImage(Bitmap bitmap) throws Exception {
		String cachePath =null;
		File file = new File(getCacheDir(), Contract.HEAD_CACHE_PATH);
		FileOutputStream fos = new FileOutputStream(file);
		bitmap.compress(CompressFormat.PNG, 100, fos);
		cachePath = "file://" + file.getPath();
		userInfo.setHeadUrl(cachePath);
		fos.flush();
		fos.close();
		return cachePath;
		
	}
}
