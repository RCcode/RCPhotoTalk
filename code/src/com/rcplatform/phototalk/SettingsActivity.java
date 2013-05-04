package com.rcplatform.phototalk;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.AccountInfoEditActivity.LoadImageTask;
import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.activity.ImagePickActivity;
import com.rcplatform.phototalk.api.RCPlatformResponseHandler;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.image.downloader.ImageOptionsFactory;
import com.rcplatform.phototalk.image.downloader.RCPlatformImageLoader;
import com.rcplatform.phototalk.proxy.FriendsProxy;
import com.rcplatform.phototalk.utils.AppSelfInfo;
import com.rcplatform.phototalk.utils.Contract.Action;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.PhotoTalkUtils;
import com.rcplatform.phototalk.utils.Utils;
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
	private ImageView mHeadView;
	private TextView mNickView;
	private TextView userRcId;
	private ImageView user_bg_View;
	private PopupWindow mImageSelectPopupWindow;
	private Uri mImageUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		mContext = this;
		initTitle();
		mHeadView = (ImageView) findViewById(R.id.settings_account_head_portrait);
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
		setUserInfo(userInfo);
	}

	private void setUserInfo(UserInfo userInfo) {
		RCPlatformImageLoader.loadImage(SettingsActivity.this, ImageLoader.getInstance(), ImageOptionsFactory.getHeadImageOptions(), userInfo.getHeadUrl(), AppSelfInfo.ImageScaleInfo.thumbnailImageWidthPx, mHeadView, R.drawable.default_head);
		mNickView.setText("" + userInfo.getNick());
		userRcId.setText("" + userInfo.getRcId());
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
			this.finish();
			break;
		case R.id.choosebutton:
			startActivity(new Intent(this, AddFriendActivity.class));
			break;
		case R.id.settings_clean_history_record_btn:
			doCleanDistory();
			break;
		case R.id.user_bg:
			// 点击更改背景图片
			showImagePickMenu(user_bg_View);
			break;
		case R.id.settings_user_edit_rc_id_action:
			startActivity(SystemSettingActivity.class);
			break;
		}
	}

	@Override
	protected void onImageReceive(Uri imageBaseUri, String imagePath) {
		// TODO Auto-generated method stub
		super.onImageReceive(imageBaseUri, imagePath);
		new LoadImageTask().execute(imageBaseUri, Uri.parse(imagePath));
		postImage(imagePath);

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
				int rotateAngel = Utils.getUriImageAngel(SettingsActivity.this,
						imageUri);
				int nWidth = 0, nHeight = 0;
				nHeight = user_bg_View.getHeight();
				nWidth = user_bg_View.getWidth();
				bitmap = Utils.decodeSampledBitmapFromFile(headPath, nWidth,
						nHeight, rotateAngel);
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
			user_bg_View.setBackgroundDrawable(new BitmapDrawable(result));
		}
	}

	private void doCleanDistory() {
		PhotoTalkUtils.updateInformationState(this,
				Action.ACTION_INFORMATION_DELETE);
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



	public void postImage(String imageUrl) {
		File file = null;
		try {
			file = new File(imageUrl);
			if (file != null) {
				FriendsProxy.upUserBackgroundImage(SettingsActivity.this, file, new RCPlatformResponseHandler() {

					@Override
					public void onSuccess(int statusCode, String content) {
						// TODO Auto-generated method stub
						// 上传成功
						System.out.println("content--->" + content);
					}

					@Override
					public void onFailure(int errorCode, String content) {
						// TODO Auto-generated method stub
						// 上传失败
						System.out.println("content--->" + content);
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
}
