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
import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.api.RCPlatformResponseHandler;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.image.downloader.ImageOptionsFactory;
import com.rcplatform.phototalk.image.downloader.RCPlatformImageLoader;
import com.rcplatform.phototalk.proxy.FriendsProxy;
import com.rcplatform.phototalk.utils.AppSelfInfo;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.Utils;
import com.rcplatform.phototalk.views.HorizontalListView;

public class SettingsActivity extends BaseActivity implements
		View.OnClickListener {

	private static final String TAG = "MyFriendsActivity";

	private static final int REQUEST_CODE_EDIT_INFO = 100;
	protected static final int REQUEST_CODE_GALLARY = 1012;
	protected static final int REQUEST_CODE_CAMERA = 1013;

	private Context mContext;
	private Button mCleanBtn;
	private Button editBtn;
	private UserInfo userInfo;
	private RelativeLayout edit_rcId;
	private HorizontalListView mHrzListView;
	private View mBack;
	private TextView mTitleTextView;
	private ImageView mHeadView;
	private TextView mNickView;
	private TextView userRcId;
	private ImageView user_bg_View;
	private PopupWindow mImageSelectPopupWindow;
	private Uri mImageUri;
	private Bitmap bitmap = null;

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
		mHrzListView = (HorizontalListView) findViewById(R.id.my_friend_details_apps_listview);
		mCleanBtn = (Button) findViewById(R.id.settings_clean_history_record_btn);
		mCleanBtn.setOnClickListener(this);
		user_bg_View = (ImageView) findViewById(R.id.user_bg);
		user_bg_View.setOnClickListener(this);
		userInfo = getPhotoTalkApplication().getCurrentUser();
		setUserInfo(userInfo);
	}

	private void setUserInfo(UserInfo userInfo) {
		RCPlatformImageLoader.loadImage(SettingsActivity.this,
				ImageLoader.getInstance(),
				ImageOptionsFactory.getHeadImageOptions(),
				userInfo.getHeadUrl(),
				AppSelfInfo.ImageScaleInfo.thumbnailImageWidthPx, mHeadView,
				R.drawable.default_head);
		mNickView.setText("" + userInfo.getNick());
		userRcId.setText("" + userInfo.getRcId());
	}

	private void initTitle() {
		mBack = findViewById(R.id.back);
		mBack.setVisibility(View.VISIBLE);
		mBack.setOnClickListener(this);
		//
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
		case R.id.settings_user_info_edit_action:
			startActivityForResult(new Intent(this,
					AccountInfoEditActivity.class), REQUEST_CODE_EDIT_INFO);
			break;
		case R.id.settings_user_edit_tacoty_id_action:

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
		}
	}

	private void doCleanDistory() {

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == REQUEST_CODE_EDIT_INFO) {
				setUserInfo((UserInfo) data
						.getSerializableExtra(AccountInfoEditActivity.RESULT_PARAM_USER));
			} else if (REQUEST_CODE_CAMERA == requestCode) {
				Uri tmpUri = mImageUri;
				if (data != null && data.getData() != null) {
					tmpUri = data.getData();
				}
				String realPath = Utils.getRealPath(this, tmpUri);
				if (realPath != null) {
					System.out.println("-----realPath------>" + realPath);
					myHandler.obtainMessage(1, realPath).sendToTarget();
					postImage(realPath);
				} else {
				}

			} else if (REQUEST_CODE_GALLARY == requestCode) {
				try {
					Uri tmpUri = null;
					if (data != null && data.getData() != null) {
						tmpUri = data.getData();
					}
					String realPath = Utils.getRealPath(this, tmpUri);
					if (realPath != null) {
						myHandler.obtainMessage(1, realPath).sendToTarget();
						System.out.println("---realPath-->" + realPath);
						postImage(realPath);
					} else {
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	Handler myHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			String url = (String) msg.obj;
			bitmap = getLocalBitmap(url);
			if(bitmap!=null){
				user_bg_View.setBackgroundDrawable(new BitmapDrawable(bitmap));
			}
		};
	};

	public Bitmap getLocalBitmap(String url) {
//		Bitmap bitmap = null;
//		FileInputStream fis = null;
//		try {
//			fis = new FileInputStream(url);
//			bitmap  = BitmapFactory.decodeStream(fis);
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		Bitmap bitmap = null;
		InputStream in = null;
		BufferedOutputStream out = null;
		try {
			in = new BufferedInputStream(new URL(url).openStream(), 2 * 1024);
			final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
			out = new BufferedOutputStream(dataStream, 2 * 1024);
			out.flush();
			byte[] data = dataStream.toByteArray();
			bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			data = null;
			return bitmap;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}finally{
			try {
				out.close();
				in.close();
			} catch (Exception e2) {
				// TODO: handle exception
			}
		}
	}

	protected void showImagePickMenu(View view) {
		if (mImageSelectPopupWindow == null) {
			View detailsView = LayoutInflater.from(this).inflate(
					R.layout.picker_head_source_layout, null, false);

			mImageSelectPopupWindow = new PopupWindow(detailsView, getWindow()
					.getWindowManager().getDefaultDisplay().getWidth(),
					((Activity) this).getWindow().getWindowManager()
							.getDefaultDisplay().getHeight());

			mImageSelectPopupWindow.setFocusable(true);
			mImageSelectPopupWindow.setOutsideTouchable(true);
			mImageSelectPopupWindow.setBackgroundDrawable(new BitmapDrawable());
			ImageButton cameraBtn = (ImageButton) detailsView
					.findViewById(R.id.picker_head_source_camera);
			cameraBtn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (!Utils.isExternalStorageUsable()) {
						DialogUtil.showToast(getApplicationContext(),
								R.string.no_sdc, Toast.LENGTH_SHORT);
						return;
					}
					mImageSelectPopupWindow.dismiss();
					startCamera();
				}
			});
			ImageButton gallaryBtn = (ImageButton) detailsView
					.findViewById(R.id.picker_head_source_gallary);
			gallaryBtn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (!Utils.isExternalStorageUsable()) {
						DialogUtil.showToast(getApplicationContext(),
								R.string.no_sdc, Toast.LENGTH_SHORT);
						return;
					}
					mImageSelectPopupWindow.dismiss();
					startGallary();
				}
			});
			Button cancelBtn = (Button) detailsView
					.findViewById(R.id.picker_head_cancel);
			cancelBtn.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (mImageSelectPopupWindow.isShowing()) {
						mImageSelectPopupWindow.dismiss();
					}
				}
			});
		}
		mImageSelectPopupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
	}

	public void startCamera() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		ContentValues values = new ContentValues();
		mImageUri = getContentResolver().insert(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
		intent.putExtra(MediaStore.Images.Media.ORIENTATION, 0);
		intent.putExtra(MediaStore.EXTRA_OUTPUT, mImageUri);
		startActivityForResult(intent, REQUEST_CODE_CAMERA);
	}

	public void startGallary() {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		startActivityForResult(intent, REQUEST_CODE_GALLARY);
	}

	public void postImage(String imageUrl) {
		File file = null;
		try {
			file = new File(imageUrl);
			if (file != null) {
				FriendsProxy.upUserBackgroundImage(SettingsActivity.this, file,
						new RCPlatformResponseHandler() {

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
		if(bitmap!=null&&!bitmap.isRecycled()){
			bitmap.isRecycled();
			bitmap=null;
		};
	}
}
