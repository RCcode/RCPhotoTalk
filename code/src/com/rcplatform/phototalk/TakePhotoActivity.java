package com.rcplatform.phototalk;

import java.io.File;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

import com.rcplatform.phototalk.bean.InformationCategory;
import com.rcplatform.phototalk.umeng.EventUtil;
import com.rcplatform.phototalk.utils.Constants;
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

	private Switch vcChange;

	private VideoRecordProgressView videoProgressView;

	private TakeMode mMode;

	private boolean isRecordingVideo = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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

			@Override
			public void onClick(View v) {
				isRecordingVideo = !isRecordingVideo;
				if (isRecordingVideo) {
					mCameraView.startVideoRecord();
				} else {
					mCameraView.stopRecord();
				}
			}
		});
		vcChange = (Switch) findViewById(R.id.switch_vc);
		vcChange.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
			}

			@Override
			public void onRecordEnd(String cacheFilePath,int videoLength) {
				endVideoRecord();
				startEditActivity(cacheFilePath,videoLength);
			}
		});
	}

	private void startVideoRecord() {
		videoProgressView.startAnimation(0, 360, Constants.TimeMillins.MAX_VIDEO_RECORD_TIME);
		linearVCChange.setVisibility(View.GONE);
		mButtonChangeCamera.setVisibility(View.GONE);
	}

	private void endVideoRecord() {
		isRecordingVideo = false;
		videoProgressView.resetAnimation();
		linearVCChange.setVisibility(View.VISIBLE);
		mButtonChangeCamera.setVisibility(View.VISIBLE);
	}

	private TakeOnSuccess takeOnSuccess = new TakeOnSuccess() {

		@Override
		public void successMethod() {
			startEditActivity(null,0);
		}
	};
	private final OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int tag = (Integer) v.getTag();
			switch (tag) {
			case TAKE_ON_CLICK:
				EventUtil.Main_Photo.rcpt_takephoto(ctx);
				mCameraView.takePhoto();
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

			}

		}
	};

	private void startEditActivity(String cachePath,int videoLength) {
		switch (mMode) {
		case VIDEO:
			startVideoEditActivity(cachePath,videoLength);
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

	public void startVideoEditActivity(String path,int videoLength) {
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
		mCameraView.clearVideoTempFile();
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
