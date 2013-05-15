package com.rcplatform.phototalk;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.activity.ImagePickActivity;
import com.rcplatform.phototalk.api.MenueApiFactory;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.image.downloader.ImageOptionsFactory;
import com.rcplatform.phototalk.proxy.FriendsProxy;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.utils.Contract;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.PrefsUtils;
import com.rcplatform.phototalk.utils.ShowToast;
import com.rcplatform.phototalk.utils.Utils;
import com.rcplatform.phototalk.views.HeadImageView;

public class AccountInfoEditActivity extends ImagePickActivity implements View.OnClickListener {

	private static final int REQUESTCODE_NAME = 1010;
	private static final int REQUESTCODE_SIGNATURE = 1011;

	protected static final int REQUEST_GALLARY = 1012;
	protected static final int REQUEST_CAMERA = 1013;

	public static final String RESULT_PARAM_USER = "userInfo";
	private MenueApplication app;
	private TextView mNameView;
	private TextView mSexView;
	private TextView mBirthday;
	// private TextView mSignatureView;
	private View mBackView;
	private DatePicker mBirthDayPicker;
	private TextView mTitleView;
	private HeadImageView mMyHeadView;
	private AlertDialog mBirthChooseDialog;
	private Bitmap bitmap = null;
	private Calendar mBirthDayCalender;
	private int selectedSex;
	private String headPath;
	private UserInfo userDetailInfo;
	private String[] sex;
	private boolean isHeadChange = false;
	private boolean isChance = false;
	private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_edit_account_info);
		app = (MenueApplication) getApplication();
		userDetailInfo = app.getCurrentUser();
		sex = new String[] {getString(R.string.male), getString(R.string.famale) };
		// sex = new String[] { getString(R.string.sex_secret),
		// getString(R.string.male), getString(R.string.famale) };
		initView();
	}

	private Handler mHandler2 = new Handler() {

		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case MenueApiFactory.RESPONSE_STATE_SUCCESS:
				mMyHeadView.setImageBitmap(bitmap);// 把拍摄的照片转成圆角显示在预览控件
				// mSexView.setText(sex);
				if (userDetailInfo != null) {
					mNameView.setText("" + userDetailInfo.getNickName());

					selectedSex = Integer.valueOf(userDetailInfo.getGender()) - 1;
					if (selectedSex == -1) {
						mSexView.setText(R.string.settings_select_sex_private);
					} else {
						// String[] arraySex =
						// getResources().getStringArray(R.array.settings_page_sex_arrays);
						// mSexView.setText(arraySex[selectedSex]);
					}
					mBirthday.setText("" + userDetailInfo.getBirthday());
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
		mMyHeadView = (HeadImageView) findViewById(R.id.settings_account_head_portrait);
		mNameView = (TextView) findViewById(R.id.settings_modify_name);
		mSexView = (TextView) findViewById(R.id.settings_modify_sex);
		mBirthday = (TextView) findViewById(R.id.settings_modify_age);
		findViewById(R.id.rela_edit_sex).setOnClickListener(this);
		findViewById(R.id.rela_edit_birthday).setOnClickListener(this);
		findViewById(R.id.rela_edit_nick).setOnClickListener(this);
		mMyHeadView.setOnClickListener(this);
		setUserInfo();
	}

	private void setUserInfo() {
		setNick();
		setSex();
		setBirthday();
		loadHeadPicture();
	}

	private void setBirthday() {
		if (!TextUtils.isEmpty(userDetailInfo.getBirthday())) {
			mBirthday.setText(userDetailInfo.getBirthday());
			isChance = true;
		} else {
			mBirthday.setText(null);
		}
	}

	private void loadHeadPicture() {
		String headUrl = userDetailInfo.getHeadUrl();
		File file = new File(headUrl);
		if (file.exists()) {
			Bitmap bitmap = BitmapFactory.decodeFile(headUrl);
			mMyHeadView.setImageBitmap(Utils.getRoundedCornerBitmap(bitmap));
		} else {
			ImageLoader.getInstance().displayImage(userDetailInfo.getHeadUrl(), mMyHeadView, ImageOptionsFactory.getHeadImageOptions());
		}

	}

	private void setNick() {
		mNameView.setText(userDetailInfo.getNickName());
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
			intentName.putExtra(UpdateNameActivity.REQUEST_PARAM_KEY_TEXT, userDetailInfo.getNickName());
			startActivityForResult(intentName, REQUESTCODE_NAME);
			break;
		case R.id.rela_edit_sex:
			showSexChooseDialog();
			break;
		case R.id.rela_edit_birthday:
			showDateDialog();
			break;
		}
	}

	private void showSexChooseDialog() {
		int selectedSex = userDetailInfo.getGender();
		new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.settings_select_sex))
				.setSingleChoiceItems(sex, selectedSex, new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						userDetailInfo.setGender(which+1);
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
					dialog.dismiss();
					if (which == DialogInterface.BUTTON_NEGATIVE)
						saveBirthDay(mBirthDayPicker.getYear(), mBirthDayPicker.getMonth(), mBirthDayPicker.getDayOfMonth());
				}
			};
			mBirthChooseDialog = new AlertDialog.Builder(this).setTitle(R.string.select_birthday).setView(view)
					.setNegativeButton(R.string.confirm, birthListener).setPositiveButton(R.string.cancel, birthListener).create();
		}
		if (!TextUtils.isEmpty(userDetailInfo.getBirthday())) {
			try {
				mBirthDayCalender.setTime(mDateFormat.parse(userDetailInfo.getBirthday()));
			} catch (ParseException e) {
				e.printStackTrace();
			}

		}
		mBirthDayPicker.init(mBirthDayCalender.get(Calendar.YEAR), mBirthDayCalender.get(Calendar.MONTH), mBirthDayCalender.get(Calendar.DAY_OF_MONTH),
				mOnBirthdayChangeListener);
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
				if (!userDetailInfo.getNickName().equals(nick)) {
					isChance = true;
					userDetailInfo.setNickName(nick);
					setNick();
				}
			}
		}
	}

	@Override
	protected void onImageReceive(Uri imageBaseUri, String imagePath) {
		super.onImageReceive(imageBaseUri, imagePath);
		isChance = true;
		new LoadImageTask().execute(imageBaseUri, Uri.parse(imagePath));
	}

	@Override
	protected void onImagePickFail() {
		super.onImagePickFail();
		DialogUtil.showToast(getApplicationContext(), R.string.get_image_fail, Toast.LENGTH_SHORT);
	}

	@Override
	protected void onDestroy() {

		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			updateUserInfo();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void updateUserInfo() {
		// 资料发生改变 上传服务器
		if (isChance) {

			File file = null;
			UserInfo userInfo = getPhotoTalkApplication().getCurrentUser();
			if (isHeadChange && !TextUtils.isEmpty(headPath)) {
				file = new File(headPath);
			}
			FriendsProxy.upUserInfo(this, file, new RCPlatformResponseHandler() {

				@Override
				public void onSuccess(int statusCode, String content) {
					Gson gson = new Gson();
					try {
						JSONObject obj = new JSONObject(content);
						UserInfo userInfo = gson.fromJson(obj.getJSONObject("userInfo").toString(), UserInfo.class);
						PrefsUtils.User.saveUserInfo(getApplicationContext(), userDetailInfo.getRcId(), userInfo);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				@Override
				public void onFailure(int errorCode, String content) {

				}
			}, userDetailInfo.getNickName(), userDetailInfo.getBirthday(), userDetailInfo.getGender() + "");
		}
		// e.printStackTrace();
		// }
		// }
		//
		// @Override
		// public void onLoadFail(String error) {
		// }
		// });
		// }
		finish();
	}

	private void setResultParam() {
		Intent intent = new Intent();
		intent.putExtra(RESULT_PARAM_USER, userDetailInfo);
		setResult(Activity.RESULT_OK, intent);
	}

	class LoadImageTask extends AsyncTask<Uri, Void, Bitmap> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		}

		@Override
		protected Bitmap doInBackground(Uri... params) {
			Uri imageUri = params[0];
			headPath = params[1].getPath();
			Bitmap bitmap = null;
			try {
				int rotateAngel = Utils.getUriImageAngel(AccountInfoEditActivity.this, imageUri);
				int nWidth = 0, nHeight = 0;
				nHeight = mMyHeadView.getHeight();
				nWidth = mMyHeadView.getWidth();
				bitmap = Utils.decodeSampledBitmapFromFile(headPath, nWidth, nHeight, rotateAngel);

				bitmap = Utils.getRectBitmap(bitmap);
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
