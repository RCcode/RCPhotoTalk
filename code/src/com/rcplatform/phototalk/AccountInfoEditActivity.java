package com.rcplatform.phototalk;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.activity.ImagePickActivity;
import com.rcplatform.phototalk.api.MenueApiFactory;
import com.rcplatform.phototalk.api.MenueApiUrl;
import com.rcplatform.phototalk.api.PhotoTalkParams;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.galhttprequest.GalHttpRequest;
import com.rcplatform.phototalk.galhttprequest.GalHttpRequest.GalHttpLoadTextCallBack;
import com.rcplatform.phototalk.image.downloader.ImageOptionsFactory;
import com.rcplatform.phototalk.listener.RcplatformhkTextCallBack;
import com.rcplatform.phototalk.utils.Contract;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.PrefsUtils;
import com.rcplatform.phototalk.utils.ShowToast;
import com.rcplatform.phototalk.utils.Utils;

public class AccountInfoEditActivity extends ImagePickActivity implements View.OnClickListener {

	private static final int REQUESTCODE_NAME = 1010;
	private static final int REQUESTCODE_SIGNATURE = 1011;

	protected static final int REQUEST_GALLARY = 1012;
	protected static final int REQUEST_CAMERA = 1013;

	public static final String RESULT_PARAM_USER = "userInfo";

	private TextView mNameView;
	private TextView mSexView;
	private TextView mBirthday;
//	private TextView mSignatureView;
	private View mBackView;
	private DatePicker mBirthDayPicker;
	private TextView mTitleView;
	private ImageView mMyHeadView;
	private AlertDialog mBirthChooseDialog;
	private Bitmap bitmap = null;
	private Calendar mBirthDayCalender;
	private int selectedSex;
	private String headPath;
	private UserInfo userDetailInfo;
	private String[] sex;
	private boolean isHeadChange = false;
	private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_edit_account_info);
		userDetailInfo = PrefsUtils.LoginState.getLoginUser(this);
		sex = new String[] {getString(R.string.male), getString(R.string.famale) };
//		sex = new String[] { getString(R.string.sex_secret), getString(R.string.male), getString(R.string.famale) };
		initView();
	}

	private Handler mHandler2 = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case MenueApiFactory.RESPONSE_STATE_SUCCESS:
				System.out.println("====bitmap>" + bitmap);
				mMyHeadView.setImageBitmap(bitmap);// 把拍摄的照片转成圆角显示在预览控件
				// mSexView.setText(sex);
				if (userDetailInfo != null) {
					mNameView.setText("" + userDetailInfo.getNick());

					selectedSex = Integer.valueOf(userDetailInfo.getSex()) - 1;
					if (selectedSex == -1) {
						mSexView.setText(R.string.settings_select_sex_private);
					} else {
//						String[] arraySex = getResources().getStringArray(R.array.settings_page_sex_arrays);
//						mSexView.setText(arraySex[selectedSex]);
					}

					mBirthday.setText("" + userDetailInfo.getBirthday());

//					mSignatureView.setText("" + userDetailInfo.getSignature());
				}
				break;
			case MenueApiFactory.LOGIN_PASSWORD_ERROR:
				ShowToast.showToast(AccountInfoEditActivity.this, getResources().getString(R.string.reg_pwd_no_email_yes), Toast.LENGTH_LONG);
				break;
			case MenueApiFactory.LOGIN_EMAIL_ERROR:
				ShowToast.showToast(AccountInfoEditActivity.this, getResources().getString(R.string.reg_email_no), Toast.LENGTH_LONG);
				break;
			case MenueApiFactory.LOGIN_SERVER_ERROR:
				ShowToast.showToast(AccountInfoEditActivity.this, getResources().getString(R.string.reg_server_no), Toast.LENGTH_LONG);
				break;
			case MenueApiFactory.LOGIN_ADMIN_ERROR:
				ShowToast.showToast(AccountInfoEditActivity.this, getResources().getString(R.string.reg_admin_no), Toast.LENGTH_LONG);
				break;
			}

		}

	};

	private void initView() {
		initTitle();
		mMyHeadView = (ImageView) findViewById(R.id.settings_account_head_portrait);
		mNameView = (TextView) findViewById(R.id.settings_modify_name);
		mSexView = (TextView) findViewById(R.id.settings_modify_sex);
		mBirthday = (TextView) findViewById(R.id.settings_modify_age);
//		mSignatureView = (TextView) findViewById(R.id.settings_modify_signature);
		findViewById(R.id.rela_edit_sex).setOnClickListener(this);
		findViewById(R.id.rela_edit_birthday).setOnClickListener(this);
		findViewById(R.id.rela_edit_nick).setOnClickListener(this);
//		findViewById(R.id.rela_edit_signture).setOnClickListener(this);
		mMyHeadView.setOnClickListener(this);
		setUserInfo();
	}

	private void setUserInfo() {
		setNick();
		setSex();
		setBirthday();
//		setSignture();
		loadHeadPicture();
	}

	private void setBirthday() {
		if (!TextUtils.isEmpty(userDetailInfo.getBirthday())) {
			mBirthday.setText(userDetailInfo.getBirthday());
		} else {
			mBirthday.setText(null);
		}
	}

	private void loadHeadPicture() {
		String headUrl = userDetailInfo.getHeadUrl();
		if (headUrl.startsWith("file")) {
			ImageLoader.getInstance().displayImage(userDetailInfo.getHeadUrl(), mMyHeadView, ImageOptionsFactory.getHeadImageOptions());
		} else {
			ImageLoader.getInstance().displayImage(userDetailInfo.getHeadUrl(), mMyHeadView);
		}

	}

//	private void setSignture() {
//		// TODO Auto-generated method stub
//		mSignatureView.setText(userDetailInfo.getSignature());
//	}

	private void setNick() {
		mNameView.setText(userDetailInfo.getNick());
	}

	private void setSex() {
		mSexView.setText(userDetailInfo.getSexString(this));
	}

	private void initTitle() {
		mBackView = findViewById(R.id.back);
		mBackView.setVisibility(View.VISIBLE);
		mBackView.setOnClickListener(this);
		mTitleView = (TextView) findViewById(R.id.titleContent);
		mTitleView.setText("" + getResources().getString(R.string.settings_update_person_info_title));
		mTitleView.setVisibility(View.VISIBLE);
	}

	protected void failure(JSONObject obj) {
		DialogUtil.createMsgDialog(this, getResources().getString(R.string.login_error), getResources().getString(R.string.ok)).show();
		finish();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.back:
			updateUserInfo();
			break;
		case R.id.choosebutton:
			startActivity(new Intent(this, AddFriendActivity.class));
			break;
		case R.id.settings_account_head_portrait:
			showImagePickMenu(v);
			break;
		case R.id.rela_edit_nick:
			Intent intentName = new Intent(this, UpdateNameActivity.class);
			intentName.setAction("setting_update_name");
			intentName.putExtra(UpdateNameActivity.REQUEST_PARAM_KEY_TEXT, userDetailInfo.getNick());
			startActivityForResult(intentName, REQUESTCODE_NAME);
			break;
//		case R.id.rela_edit_signture:
//			Intent intentSignature = new Intent(this, UpdateNameActivity.class);
//			intentSignature.setAction("setting_update_signature");
//			intentSignature.putExtra(UpdateNameActivity.REQUEST_PARAM_KEY_TEXT, userDetailInfo.getSignature());
//			startActivityForResult(intentSignature, REQUESTCODE_SIGNATURE);
//			break;
		case R.id.rela_edit_sex:
			showSexChooseDialog();
			break;
		case R.id.rela_edit_birthday:
			showDateDialog();
			break;
		}
	}

	private void showSexChooseDialog() {
		int selectedSex = userDetailInfo.getSex();
		new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.settings_select_sex)).setSingleChoiceItems(sex, selectedSex, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int which) {
				userDetailInfo.setSex(which);
				setSex();
				dialog.dismiss();
			}
		}).show();
	}

	private void saveBirthDay(int year, int monthOfYear, int dayOfMonth) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(year, monthOfYear, dayOfMonth);
		userDetailInfo.setBirthday(mDateFormat.format(new Date(calendar.getTimeInMillis())));
		setBirthday();
	}

	private OnDateChangedListener mOnBirthdayChangeListener = new OnDateChangedListener() {

		@Override
		public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
			// TODO Auto-generated method stub
			if (isSelectedTimeAfterToday(year, monthOfYear, dayOfMonth)) {
				view.init(mBirthDayCalender.get(Calendar.YEAR), mBirthDayCalender.get(Calendar.MONTH), mBirthDayCalender.get(Calendar.DAY_OF_MONTH), this);
			}
		}
	};

	private void showDateDialog() {
		// 生成一个DatePickerDialog对象，并显示。显示的DatePickerDialog控件可以选择年月日，并设置
		if (mBirthChooseDialog == null) {
			View view = getLayoutInflater().inflate(R.layout.date_picker, null);
			mBirthDayPicker = (DatePicker) view.findViewById(R.id.date_picker);
			mBirthDayCalender = Calendar.getInstance();

			DialogInterface.OnClickListener birthListener = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
					dialog.dismiss();
					if (which == DialogInterface.BUTTON_NEGATIVE)
						saveBirthDay(mBirthDayPicker.getYear(), mBirthDayPicker.getMonth(), mBirthDayPicker.getDayOfMonth());
				}
			};
			mBirthChooseDialog = new AlertDialog.Builder(this).setTitle(R.string.select_birthday).setView(view).setNegativeButton(R.string.confirm, birthListener).setPositiveButton(R.string.cancel, birthListener).create();
		}
		if (!TextUtils.isEmpty(userDetailInfo.getBirthday())) {
			try {
				mBirthDayCalender.setTime(mDateFormat.parse(userDetailInfo.getBirthday()));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		mBirthDayPicker.init(mBirthDayCalender.get(Calendar.YEAR), mBirthDayCalender.get(Calendar.MONTH), mBirthDayCalender.get(Calendar.DAY_OF_MONTH), mOnBirthdayChangeListener);
		mBirthChooseDialog.show();
	}

	private boolean isSelectedTimeAfterToday(int year, int monthOfYear, int dayOfMonth) {
		Calendar calenderSelected = Calendar.getInstance();
		calenderSelected.set(year, monthOfYear, dayOfMonth);
		Calendar calendarCurrent = Calendar.getInstance();
		return calenderSelected.getTimeInMillis() > calendarCurrent.getTimeInMillis();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (REQUESTCODE_NAME == requestCode) {
				String nick = data.getStringExtra("result");
				userDetailInfo.setNick(nick);
				setNick();
			} else if (REQUESTCODE_SIGNATURE == requestCode) {
				String signature = data.getStringExtra("result");
				userDetailInfo.setSignature(signature);
//				setSignture();
			}
		}
	}

	@Override
	protected void onImageReceive(Uri imageBaseUri, String imagePath) {
		// TODO Auto-generated method stub
		super.onImageReceive(imageBaseUri, imagePath);
		new LoadImageTask().execute(imageBaseUri, Uri.parse(imagePath));
	}

	@Override
	protected void onImagePickFail() {
		// TODO Auto-generated method stub
		super.onImagePickFail();
		DialogUtil.showToast(getApplicationContext(), R.string.get_image_fail, Toast.LENGTH_SHORT);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub

		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			updateUserInfo();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private boolean isSigntureChanged(UserInfo oldUserInfo, UserInfo currentUserInfo) {
		if (oldUserInfo.getSignature() == null)
			return currentUserInfo.getSignature() != null;
		else
			return !oldUserInfo.getSignature().equals(currentUserInfo.getSignature());
	}

	private void updateUserInfo() {
		UserInfo userInfo = getPhotoTalkApplication().getCurrentUser();
		GalHttpRequest request = GalHttpRequest.requestWithURL(this, MenueApiUrl.USER_INFO_UPDATE_URL);
		PhotoTalkParams.buildBasicParams(this, request);
		if (isHeadChange && !TextUtils.isEmpty(headPath)) {
			request.setPostValueForKey(MenueApiFactory.FILE, headPath);
			request.setPostValueForKey(MenueApiFactory.IMGTYPE, Contract.META_DATA);
		}
		if (userInfo.getSex() != userDetailInfo.getSex()) {
			request.setPostValueForKey(MenueApiFactory.SEX, userDetailInfo.getSex() + "");
		}

		if (userInfo.getBirthday() != userDetailInfo.getBirthday()) {
			request.setPostValueForKey(MenueApiFactory.BIRTHDAY, userDetailInfo.getBirthday());
		}
		if (!userInfo.getNick().equals(userDetailInfo.getNick())) {
			request.setPostValueForKey(MenueApiFactory.NICK, userDetailInfo.getNick());
		}

		if (isSigntureChanged(userInfo, userDetailInfo)) {
			request.setPostValueForKey(MenueApiFactory.SIGNATURE, userDetailInfo.getSignature());
		}
		if (request.getPostData().size() > PhotoTalkParams.BASIC_PARAM_COUNT) {
			PrefsUtils.User.saveUserInfo(this, userDetailInfo.getEmail(), userDetailInfo);
			setResultParam();
			request.startAsynUploadImage(new RcplatformhkTextCallBack() {

				@Override
				public void onLoadSuccess(String content) {
					Gson gson = new Gson();
					try {
						JSONObject obj = new JSONObject(content);
						UserInfo userInfo = gson.fromJson(obj.getJSONObject("userInfo").toString(), UserInfo.class);
						PrefsUtils.User.saveUserInfo(getApplicationContext(), userDetailInfo.getEmail(), userInfo);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				@Override
				public void onLoadFail(String error) {
				}
			});
		}
		finish();
	}

	private void setResultParam() {
		Intent intent = new Intent();
		intent.putExtra(RESULT_PARAM_USER, userDetailInfo);
		setResult(Activity.RESULT_OK, intent);
	}

	public void syncUserInfo() {
		UserInfo userInfo = PrefsUtils.LoginState.getLoginUser(this);
		GalHttpRequest request = GalHttpRequest.requestWithURL(this, MenueApiUrl.USER_INFO_URL);
		request.setPostValueForKey(MenueApiFactory.USERID, userInfo.getSuid());
		request.setPostValueForKey(MenueApiFactory.TOKEN, userInfo.getToken());
		request.setPostValueForKey(MenueApiFactory.LANGUAGE, Locale.getDefault().getLanguage());
		request.setPostValueForKey(MenueApiFactory.DEVICE_ID, android.os.Build.DEVICE);
		request.setPostValueForKey(MenueApiFactory.APP_ID, Contract.APP_ID);

		request.startAsynRequestString(new GalHttpLoadTextCallBack() {

			@Override
			public void textLoaded(String text) {

				try {
					System.out.println(text);
					JSONObject obj = new JSONObject(text);
					final int status = obj.getInt(MenueApiFactory.RESPONSE_KEY_STATUS);
					if (status == MenueApiFactory.RESPONSE_STATE_SUCCESS) {

						Gson gson = new Gson();
						JSONObject uiObj = obj.getJSONObject("userInfo");
						userDetailInfo = gson.fromJson(uiObj.toString(), UserInfo.class);
						mHandler2.sendMessage(mHandler2.obtainMessage());

					} else {
						failure(obj);
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void loadFail() {
				ShowToast.showToast(AccountInfoEditActivity.this, getResources().getString(R.string.net_error), Toast.LENGTH_LONG);
			}
		});

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
			headPath = params[1].getPath();
			Bitmap bitmap = null;
			try {
				int rotateAngel = Utils.getUriImageAngel(AccountInfoEditActivity.this, imageUri);
				int nWidth = 0, nHeight = 0;
				nHeight = mMyHeadView.getHeight();
				nWidth = mMyHeadView.getWidth();
				bitmap = Utils.decodeSampledBitmapFromFile(headPath, nWidth, nHeight, rotateAngel);
				if (bitmap != null) {
					cacheHeadImage(bitmap);
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
				DialogUtil.showToast(getApplicationContext(), R.string.image_unsupport, Toast.LENGTH_SHORT);
				finish();
			} else {
				isHeadChange = true;
				mMyHeadView.setImageBitmap(result);
			}
		}
	}

	private void cacheHeadImage(Bitmap bitmap) throws Exception {
		File file = new File(getCacheDir(), Contract.HEAD_CACHE_PATH);
		FileOutputStream fos = new FileOutputStream(file);
		bitmap.compress(CompressFormat.PNG, 100, fos);
		String cachePath = "file://" + file.getPath();
		userDetailInfo.setHeadUrl(cachePath);
		fos.flush();
		fos.close();
	}
}
