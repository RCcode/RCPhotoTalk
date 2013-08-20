package com.rcplatform.phototalk;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.InformationCategory;
import com.rcplatform.phototalk.bean.InformationClassification;
import com.rcplatform.phototalk.bean.InformationType;
import com.rcplatform.phototalk.drift.DriftInformationActivity;
import com.rcplatform.phototalk.umeng.EventUtil;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.PhotoTalkUtils;
import com.rcplatform.phototalk.utils.Utils;
import com.rcplatform.phototalk.views.CameraView;
import com.rcplatform.phototalk.views.CameraView.OnVideoRecordListener;
import com.rcplatform.phototalk.views.CameraView.TakeOnSuccess;
import com.rcplatform.phototalk.views.Rotate3dAnimation;
import com.rcplatform.phototalk.views.VideoRecordProgressView;

public class TakePhotoActivity extends Activity {

	private static final int TAKE_ON_CLICK = 0;

	private static final int OPEN_FLASHLIGHT_ON_CLICK = 1;

	private static final int CHANGE_CAMERA_ON_CLICK = 2;

	private static final int CLOSE_ON_CLICK = 3;

	private static final int ICON_VIDEO = 4;
	private static final int ICON_PHOTO = 5;

	private Button mButtonTake;

	private Button mButtonClose;

	private CameraView mCameraView;

	private Button mButtonOpenFlashLight;

	private Button mButtonChangeCamera;

	private View linearVCChange;

	private float mFromDegrees = 0;

	private float mToDegrees = 180;

	private Intent intent;

	private Context ctx;

	private View btnTakeVideo;

	private CheckBox vcChange;

	private VideoRecordProgressView videoProgressView;

	private TakeMode mMode;

	private boolean isRecordingVideo = false;

	private ImageView ivTakeVideoAttention;

	private View iconCamera;
	private View iconVideo;
	private int informationClassification;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (!Utils.isExternalStorageUsable()) {
			DialogUtil.showToast(this, R.string.no_sdc, Toast.LENGTH_SHORT);
			finish();
			return;
		}
		initData();
		ctx = this;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.take_photo_view);
		// deleteTemp();
		intent = getIntent();
		initCamera();
		mButtonTake = (Button) findViewById(R.id.btn_take_photo_take);
		mButtonOpenFlashLight = (Button) findViewById(R.id.btn_take_photo_flashlight);
		mButtonChangeCamera = (Button) findViewById(R.id.btn_take_photo_change_camera);
		mButtonClose = (Button) findViewById(R.id.btn_close_take_photo);
		if (Camera.getNumberOfCameras() == 1) {
			mButtonChangeCamera.setVisibility(View.GONE);
		}
		mButtonClose.setOnClickListener(clickListener);
		mButtonClose.setTag(CLOSE_ON_CLICK);
		mButtonChangeCamera.setOnClickListener(clickListener);
		mButtonChangeCamera.setTag(CHANGE_CAMERA_ON_CLICK);
		mButtonOpenFlashLight.setOnClickListener(clickListener);
		mButtonOpenFlashLight.setTag(OPEN_FLASHLIGHT_ON_CLICK);
		mButtonTake.setOnClickListener(clickListener);
		mButtonTake.setTag(TAKE_ON_CLICK);
		btnTakeVideo = findViewById(R.id.btn_take_video);
		btnTakeVideo.setOnClickListener(new OnClickListener() {
			private long lastClickTime;
			private long MIN_RECORD_TIME = 1500;

			@Override
			public void onClick(View v) {
				long currentTime = System.currentTimeMillis();
				if ((currentTime - lastClickTime) > MIN_RECORD_TIME) {
					lastClickTime = currentTime;
					if (!isRecordingVideo()) {
						mCameraView.startVideoRecord();
						recordTakeVideoEvent();
					} else {
						mCameraView.stopRecord();
					}
				}
			}
		});
		vcChange = (CheckBox) findViewById(R.id.switch_vc);
		vcChange.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mCameraView.clearTempFile();
				TakeMode targetMode;
				if (isChecked) {
					targetMode = TakeMode.VIDEO;
				} else {
					targetMode = TakeMode.CAMERA;
				}
				changeTakeMode(targetMode);
			}
		});
		vcChange.setChecked(true);
		videoProgressView = (VideoRecordProgressView) findViewById(R.id.video_progress);
		linearVCChange = findViewById(R.id.linear_vc_change);
		ivTakeVideoAttention = (ImageView) findViewById(R.id.iv_video_record_atttention);
		ivTakeVideoAttention.setBackgroundResource(R.anim.video_record_animation);
		animFrame = (AnimationDrawable) ivTakeVideoAttention.getBackground();
		iconCamera = findViewById(R.id.iv_camera);
		iconVideo = findViewById(R.id.iv_video);
		iconCamera.setTag(ICON_PHOTO);
		iconVideo.setTag(ICON_VIDEO);
		iconCamera.setOnClickListener(clickListener);
		iconVideo.setOnClickListener(clickListener);

	}

	private void recordTakeVideoEvent() {
		switch (informationClassification) {
		case InformationClassification.TYPE_DRIFT:

			EventUtil.Make_New_Friends.rcpt_newfriends_video(getApplicationContext());
			break;
		case InformationClassification.TYPE_NORMAL:
			EventUtil.Main_Photo.rcpt_takevideo(getApplicationContext());
			break;
		}
	}

	private void recordTakePhotoEvent() {
		switch (informationClassification) {
		case InformationClassification.TYPE_DRIFT:
			EventUtil.Make_New_Friends.rcpt_newfriends_photo(getApplicationContext());
			break;
		case InformationClassification.TYPE_NORMAL:
			EventUtil.Main_Photo.rcpt_takephoto(ctx);
			break;
		}
	}

	private void initData() {
		Friend friend = (Friend) getIntent().getSerializableExtra(DriftInformationActivity.PARAM_FRIEND);
		if (friend != null && friend.equals(PhotoTalkUtils.getDriftFriend())) {
			informationClassification = InformationClassification.TYPE_DRIFT;
		} else {
			informationClassification = InformationClassification.TYPE_NORMAL;
		}
	}

	private boolean isRecordingVideo() {
		return isRecordingVideo;
	}

	private void startVideoRecordFrameAnimation() {
		animFrame.start();
	}

	private void stopVideoRecordFrameAnimation() {
		animFrame.stop();
	}

	private void initCamera() {
		mCameraView = (CameraView) findViewById(R.id.sf_camera_view);
		mCameraView.setMaxVideoRecordTime(Constants.TimeMillins.MAX_VIDEO_RECORD_TIME);
		mCameraView.setTakeOnSuccess(takeOnSuccess);
		mCameraView.setOnVideoRecordListener(new OnVideoRecordListener() {

			@Override
			public void onRecordStart(String cacheFilePath) {
				startVideoRecord();
			}

			@Override
			public void onRecordFail() {
				endVideoRecord();
				DialogUtil.showToast(TakePhotoActivity.this, R.string.record_too_short, Toast.LENGTH_SHORT);
			}

			@Override
			public void onRecordEnd(String cacheFilePath, int videoLength) {
				endVideoRecord();
				startEditActivity(cacheFilePath, videoLength);
			}

			@Override
			public void onRecordPrepare() {
				prepareVideoRecord();
			}
		});
	}

	private void prepareVideoRecord() {
		btnTakeVideo.setEnabled(false);
	}

	private void startVideoRecord() {
		isRecordingVideo = true;
		btnTakeVideo.setEnabled(true);
		startVideoRecordFrameAnimation();
		videoProgressView.startAnimation(0, 360, Constants.TimeMillins.MAX_VIDEO_RECORD_TIME);
		linearVCChange.setVisibility(View.GONE);
		mButtonChangeCamera.setVisibility(View.GONE);
	}

	private void endVideoRecord() {
		stopVideoRecordFrameAnimation();
		isRecordingVideo = false;
		videoProgressView.resetAnimation();
		linearVCChange.setVisibility(View.VISIBLE);
		mButtonChangeCamera.setVisibility(View.VISIBLE);
	}

	private TakeOnSuccess takeOnSuccess = new TakeOnSuccess() {

		@Override
		public void successMethod() {
			startEditActivity(null, 0);
		}
	};
	private final OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int tag = (Integer) v.getTag();
			switch (tag) {
			case TAKE_ON_CLICK:
				mCameraView.takePhoto();
				recordTakePhotoEvent();
				break;
			case CHANGE_CAMERA_ON_CLICK:
				mCameraView.changeCamera();
				if (mFromDegrees == 0) {
					mFromDegrees = 180;
					mToDegrees = 0;
					mButtonOpenFlashLight.setVisibility(View.GONE);
				} else {
					mFromDegrees = 0;
					mToDegrees = 180;
					mButtonOpenFlashLight.setVisibility(View.VISIBLE);
				}
				Animation animation = new Rotate3dAnimation(mFromDegrees, mToDegrees, mButtonChangeCamera.getWidth() / 2, mButtonChangeCamera.getHeight() / 2,
						200.0f, true);
				animation.setDuration(500);
				animation.setInterpolator(new AccelerateInterpolator());
				mButtonChangeCamera.startAnimation(animation);
				break;
			case OPEN_FLASHLIGHT_ON_CLICK:
				if (mCameraView.setLightStatu()) {
					mButtonOpenFlashLight.setBackgroundResource(R.drawable.flashlight_press);
				} else
					mButtonOpenFlashLight.setBackgroundResource(R.drawable.flashlight_normal);
				break;
			case CLOSE_ON_CLICK:
				finish();
				break;
			case ICON_PHOTO:
				vcChange.setChecked(false);
				break;
			case ICON_VIDEO:
				vcChange.setChecked(true);
				break;
			}

		}
	};

	private AnimationDrawable animFrame;

	private void startEditActivity(String cachePath, int videoLength) {
		switch (mMode) {
		case VIDEO:
			startVideoEditActivity(cachePath, videoLength);
			break;
		case CAMERA:
			startPhotoEditActivity();
			break;
		}
	}

	public void startPhotoEditActivity() {
		intent.setClass(this, EditPictureActivity.class);
		intent.putExtra(EditPictureActivity.PARAM_KEY_RECORD_CATE, InformationCategory.PHOTO);
		startActivity(intent);
	}

	public void startVideoEditActivity(String path, int videoLength) {
		intent.setClass(this, EditPictureActivity.class);
		intent.putExtra(EditPictureActivity.PARAM_KEY_VIDEO_PATH, path);
		intent.putExtra(EditPictureActivity.PARAM_KEY_VIDEO_LENGTH, videoLength);
		intent.putExtra(EditPictureActivity.PARAM_KEY_RECORD_CATE, InformationCategory.VIDEO);
		startActivity(intent);
	}

	public void deleteTemp() {
		PhotoTalkApplication app = (PhotoTalkApplication) getApplication();
		String tempFilePath = app.getSendFileCachePath();
		File tempPic = new File(tempFilePath);
		deleteFile(tempPic);
	}

	public void deleteFile(File file) {
		if (file.exists()) {
			if (file.isDirectory()) {
				File[] files = file.listFiles();
				for (File file2 : files) {
					deleteFile(file2);
				}
			} else {
				file.delete();
			}
		} else {
		}
	}

	private void changeTakeMode(TakeMode mode) {
		switch (mode) {
		case VIDEO:
			changeToTakeVideo();
			break;
		case CAMERA:
			videoProgressView.resetAnimation();
			changeToTakeCamera();
			break;
		}
		this.mMode = mode;
	}

	private void changeToTakeCamera() {
		mButtonOpenFlashLight.setVisibility(View.VISIBLE);
		mButtonTake.setVisibility(View.VISIBLE);
		btnTakeVideo.setVisibility(View.GONE);
		// mCameraView.clearTempFile();
	}

	private void changeToTakeVideo() {
		mButtonOpenFlashLight.setVisibility(View.GONE);
		mButtonTake.setVisibility(View.GONE);
		btnTakeVideo.setVisibility(View.VISIBLE);
	}

	private enum TakeMode {
		CAMERA, VIDEO
	}
}
