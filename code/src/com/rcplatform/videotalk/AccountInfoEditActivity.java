package com.rcplatform.videotalk;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.videotalk.R;
import com.rcplatform.videotalk.activity.ImagePickActivity;
import com.rcplatform.videotalk.bean.UserInfo;
import com.rcplatform.videotalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.videotalk.galhttprequest.RCPlatformServiceError;
import com.rcplatform.videotalk.image.downloader.ImageOptionsFactory;
import com.rcplatform.videotalk.proxy.FriendsProxy;
import com.rcplatform.videotalk.request.RCPlatformResponseHandler;
import com.rcplatform.videotalk.umeng.EventUtil;
import com.rcplatform.videotalk.utils.DialogUtil;
import com.rcplatform.videotalk.utils.PhotoTalkUtils;
import com.rcplatform.videotalk.utils.PrefsUtils;

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
	private ImageView mMyHeadView;
	private AlertDialog mBirthChooseDialog;
	private Calendar mBirthDayCalender;
	private UserInfo userDetailInfo;
	private String[] sex;
	private boolean isHeadChange = false;
	private boolean isChance = false;
	private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");
	private ImageLoader mImageLoader;
	private AlertDialog updateFailDialog;
	private String newHeadPath;

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
		mMyHeadView = (ImageView) findViewById(R.id.settings_account_head_portrait);
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
		} else {
			mBirthday.setText(null);
		}
	}

	private void loadHeadPicture() {
		mImageLoader.displayImage(userDetailInfo.getHeadUrl(), mMyHeadView, ImageOptionsFactory.getFriendHeadImageOptions());
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
			EventUtil.More_Setting.rcpt_nameedit(baseContext);
			Intent intentName = new Intent(this, UpdateNameActivity.class);
			intentName.setAction("setting_update_name");
			intentName.putExtra(UpdateNameActivity.REQUEST_PARAM_KEY_TEXT, userDetailInfo.getNickName());
			startActivityForResult(intentName, REQUESTCODE_NAME);
			break;
		case R.id.rela_edit_sex:
			EventUtil.More_Setting.rcpt_genderedit(baseContext);
			showSexChooseDialog();
			break;
		case R.id.rela_edit_birthday:
			EventUtil.More_Setting.rcpt_ageedit(baseContext);
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
		isChance = true;
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
		newHeadPath = imagePath;
		String path = "file:///" + imagePath;
		userDetailInfo.setHeadUrl(path);
		loadHeadPicture();
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
			return false;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void updateUserInfo() {
		// 资料发生改变 上传服务器
		if (isChance) {
			startUpdate();
		} else {
			finish();
		}
	}

	private void startUpdate() {
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		File file = null;
		if (isHeadChange)
			file = new File(newHeadPath);
		FriendsProxy.upUserInfo(this, file, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				try {
					JSONObject jsonObject = new JSONObject(content);
					String url = jsonObject.getString("headUrl");
					if (!TextUtils.isEmpty(url)) {
						userDetailInfo.setHeadUrl(url);
					}
					PrefsUtils.User.saveUserInfo(getApplicationContext(), userDetailInfo.getRcId(), userDetailInfo);
					getPhotoTalkApplication().setCurrentUser(userDetailInfo);
					PhotoTalkDatabaseFactory.getDatabase().addFriend(PhotoTalkUtils.userToFriend(getCurrentUser()));
					setResult(Activity.RESULT_OK);
					finish();
				} catch (JSONException e) {
					e.printStackTrace();
					onFailure(RCPlatformServiceError.ERROR_CODE_REQUEST_FAIL, getString(R.string.net_error));
				}
			}

			@Override
			public void onFailure(int errorCode, String content) {
				dismissLoadingDialog();
				showUpdateFailDialog();
			}
		}, userDetailInfo.getNickName(), userDetailInfo.getBirthday(), userDetailInfo.getGender() + "");
	}

	private void showUpdateFailDialog() {
		if (updateFailDialog == null) {
			DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						finish();
						break;
					case DialogInterface.BUTTON_NEGATIVE:
						startUpdate();
						break;
					}
					dialog.dismiss();
				}
			};
			updateFailDialog = new AlertDialog.Builder(this).setMessage(R.string.update_fail).setPositiveButton(R.string.cancel, listener)
					.setNegativeButton(R.string.retry, listener).setCancelable(false).create();
		}
		updateFailDialog.show();
	}

	@Override
	public boolean onMenuOpened(int featureId, Menu menu) {
		// TODO Auto-generated method stub
		return false;
	}
}