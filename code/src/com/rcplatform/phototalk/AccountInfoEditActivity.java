package com.rcplatform.phototalk;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.image.downloader.ImageOptionsFactory;
import com.rcplatform.phototalk.proxy.FriendsProxy;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.PhotoTalkUtils;
import com.rcplatform.phototalk.utils.PrefsUtils;
import com.rcplatform.phototalk.utils.Utils;
import com.rcplatform.phototalk.views.RoundImageView;

public class AccountInfoEditActivity extends ImagePickActivity implements View.OnClickListener {

	private static final int REQUESTCODE_NAME = 1010;

	protected static final int REQUEST_GALLARY = 1012;
	protected static final int REQUEST_CAMERA = 1013;

	public static final String RESULT_PARAM_USER = "userInfo";
	private TextView mNameView;
	private TextView mSexView;
	private TextView mBirthday;
	private View mBackView;
	private DatePicker mBirthDayPicker;
	private TextView mTitleView;
	private RoundImageView mMyHeadView;
	private AlertDialog mBirthChooseDialog;
	private Calendar mBirthDayCalender;
	private UserInfo userDetailInfo;
	private String[] sex;
	private boolean isHeadChange = false;
	private boolean isChance = false;
	private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private ImageLoader mImageLoader;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_edit_account_info);
		userDetailInfo = getPhotoTalkApplication().getCurrentUser();
		sex = new String[] { getString(R.string.male), getString(R.string.famale) };
		mImageLoader = ImageLoader.getInstance();
		initView();
	}

	private void initView() {
		initTitle();
		mMyHeadView = (RoundImageView) findViewById(R.id.settings_account_head_portrait);
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
		File fileHead = PhotoTalkUtils.getUserHead(getCurrentUser());
		if (fileHead.exists()) {
			String urlLocal = "file:///" + fileHead.getPath();
			mImageLoader.displayImage(urlLocal, mMyHeadView, ImageOptionsFactory.getHeadImageOptions());
		} else {
			mImageLoader.displayImage(getCurrentUser().getHeadUrl(), mMyHeadView, ImageOptionsFactory.getHeadImageOptions());
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
			startActivity(new Intent(this, AddFriendsActivity.class));
			break;
		case R.id.settings_account_head_portrait:
			showImagePickMenu(v, CROP_HEAD_IMAGE);
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
						userDetailInfo.setGender(which + 1);
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
		isHeadChange = true;
		Utils.copyFile(new File(imagePath), PhotoTalkUtils.getUserHead(getCurrentUser()));
		new LoadImageTask(mMyHeadView).execute(imageBaseUri, Uri.parse(imagePath));
	}

	@Override
	protected void onImagePickFail() {
		super.onImagePickFail();
		DialogUtil.showToast(getApplicationContext(), R.string.get_image_fail, Toast.LENGTH_SHORT);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			updateUserInfo();
			startActivity(new Intent(AccountInfoEditActivity.this, SettingsActivity.class));
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void updateUserInfo() {
		// 资料发生改变 上传服务器
		if (isChance) {
			File file = null;
			if (isHeadChange)
				file = PhotoTalkUtils.getUserHead(getCurrentUser());
			FriendsProxy.upUserInfo(this, file, new RCPlatformResponseHandler() {

				@Override
				public void onSuccess(int statusCode, String content) {
				}

				@Override
				public void onFailure(int errorCode, String content) {
				}
			}, userDetailInfo.getNickName(), userDetailInfo.getBirthday(), userDetailInfo.getGender() + "");
			PrefsUtils.User.saveUserInfo(getApplicationContext(), userDetailInfo.getRcId(), userDetailInfo);
			getPhotoTalkApplication().setCurrentUser(userDetailInfo);
			setResult(Activity.RESULT_OK);
		}
		finish();
	}
}
