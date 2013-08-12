package com.rcplatform.phototalk.views;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.rcplatform.phototalk.PhotoTalkApplication;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.RCThreadPool;
import com.rcplatform.phototalk.utils.Utils;

public class CameraView extends ViewGroup implements SurfaceHolder.Callback {

	private SurfaceView mSurfaceView;

	private static final int INVALID_CAMERA = -1;

	private static int mNumCamera;

	private static int mFrontCameraNum;

	private static int mBackCameraNum;

	private static int mCurrentCameraNum;

	private PhotoTalkApplication app;

	private SurfaceHolder mHolder;

	private Camera.Parameters parameters;

	private Camera mCamera;

	private Context mContext;

	private boolean isOpenLight = false;

	private boolean isBackFace = true;

	private boolean isShowCamera = false;

	private int round;

	private TakeOnSuccess takeOnSuccess;

	private OnVideoRecordListener videoRecordListener;

	private VideoRecordState recordState;

	private long maxVideoRecordTime;

	private long videoRecordStartTime;

	private int videoLength;

	public long getMaxVideoRecordTime() {
		return maxVideoRecordTime;
	}

	public void setMaxVideoRecordTime(long maxVideoRecordTime) {
		this.maxVideoRecordTime = maxVideoRecordTime;
	}

	public static interface OnVideoRecordListener {
		public void onRecordStart(String cacheFilePath);

		public void onRecordEnd(String cacheFilePath, int videoLength);

		public void onRecordFail();
	}

	public void setTakeOnSuccess(TakeOnSuccess takeOnSuccess) {
		this.takeOnSuccess = takeOnSuccess;
	}

	public CameraView(Context context) {
		super(context);
		init(context);
	}

	public CameraView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	private void init(Context context) {
		if (!isInEditMode()) {
			mSurfaceView = new SurfaceView(context);
			addView(mSurfaceView);

			mHolder = mSurfaceView.getHolder();
			mHolder.addCallback(this);
			mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		this.mContext = context;
		app = (PhotoTalkApplication) context.getApplicationContext();
		initCamera();
	}

	public void clearVideoTempFile() {
		if (tempFile != null && tempFile.exists())
			tempFile.delete();
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		mNumCamera = Camera.getNumberOfCameras();
		Camera.CameraInfo cameraInfo = new Camera.CameraInfo();

		for (int i = 0; i < mNumCamera; i++) {
			Camera.getCameraInfo(i, cameraInfo);
			if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				mFrontCameraNum = i;
			} else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
				mBackCameraNum = i;
			}
		}
		if (mNumCamera == INVALID_CAMERA)
			return;
		releaseCamera();
		initCamera();

	}

	public static Size getOptimalPreviewSize(List<Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		// Try to find an size match aspect ratio and size
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// Cannot find the one match the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	public static Size getOptimalPictureSize(List<Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.1;
		double targetRatio = (double) w / h;
		if (sizes == null)
			return null;

		Size optimalSize = null;
		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		// Try to find an size match aspect ratio and size
		for (Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
				continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		// Cannot find the one match the aspect ratio, ignore the requirement
		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		initCamera();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		releaseMediaRecorder();
		releaseCamera();
	}

	private void releaseMediaRecorder() {
		if (mTimer != null) {
			mTimer.cancel();
			mTimer = null;
		}
		if (mMediaRecorder != null) {
			if (VideoRecordState.START == recordState)
				mMediaRecorder.stop();
			mMediaRecorder.release();
			mMediaRecorder = null;
		}
	}

	// 准备一个保存图片的pictureCallback对象
	public Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			if (camera != null) {
				int rotateAngel = 0;
				if (mCurrentCameraNum == 1) {
					rotateAngel = 270;
				} else {
					rotateAngel = round;
				}
				BitmapFactory.Options options = new BitmapFactory.Options();
				options.inJustDecodeBounds = true;
				BitmapFactory.decodeByteArray(data, 0, data.length, options);
				int sampleSize = Utils.calculateInSampleSize(options, Constants.SCREEN_WIDTH, Constants.SCREEN_HEIGHT, rotateAngel);

				options = new BitmapFactory.Options();
				options.inSampleSize = sampleSize;
				Bitmap mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
				Bitmap tempBitmap = null;
				Matrix matrix = new Matrix();
				if (mCurrentCameraNum == 1) {
					matrix.postRotate(rotateAngel);
					matrix.preScale(1, -1);
				} else {
					matrix.setRotate(rotateAngel);
				}
				tempBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, true);
				mBitmap.recycle();
				((PhotoTalkApplication) mContext.getApplicationContext()).setEditeBitmap(tempBitmap);
				mBitmap = null;
				tempBitmap = null;
			}

			if (takeOnSuccess != null) {
				takeOnSuccess.successMethod();
			}
			// ((TakePhotoActivity) mContext).startOtherActivity();
		}
	};

	public interface TakeOnSuccess {

		void successMethod();
	}

	public static int setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {
		android.hardware.Camera.CameraInfo info = new android.hardware.Camera.CameraInfo();
		android.hardware.Camera.getCameraInfo(cameraId, info);
		int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}
		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		camera.setDisplayOrientation(result);
		return result;
	}

	public void takePhoto() {
		if (!isShowCamera)
			return;
		mCamera.takePicture(null, null, mPictureCallback);
	}

	public void takeFocuse() {
		try {
			mCamera.autoFocus(new AutoFocusCallback() {

				@Override
				public void onAutoFocus(boolean success, Camera camera) {
					// if (success) {
					// camera.takePicture(null, null, mPictureCallback);
					// } else {
					// camera.takePicture(null, null, mPictureCallback);
					// }
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean setLightStatu() {
		isOpenLight = !isOpenLight;
		releaseCamera();
		initCamera();
		return isOpenLight;
	}

	public boolean changeCamera() {
		isBackFace = !isBackFace;
		releaseCamera();
		initCamera();
		return isBackFace;
	}

	private void muteIfNeeded() {

		/*
		 * String muteMode =
		 * mPreferences.getString(CameraSettings.KEY_MUTE_MODE, "unmute") "";
		 */
		final AudioManager manager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
		int mode = manager.getRingerMode();
		final int maxMusicVolumn = manager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		final int maxSystemVolumn = manager.getStreamMaxVolume(AudioManager.STREAM_SYSTEM);
		final int musicVolumnBeforeTaken = manager.getStreamVolume(AudioManager.STREAM_MUSIC);
		final int systemVolumnBeforeTaken = manager.getStreamVolume(AudioManager.STREAM_SYSTEM);

		if (AudioManager.RINGER_MODE_SILENT == mode) {
			manager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
			manager.setStreamVolume(AudioManager.STREAM_SYSTEM, 0, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);

			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						Thread.sleep(3000);
						manager.setStreamVolume(AudioManager.STREAM_MUSIC, musicVolumnBeforeTaken, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
						manager.setStreamVolume(AudioManager.STREAM_SYSTEM, systemVolumnBeforeTaken, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
					} catch (Exception e) {
						// Log.e(TAG, "静音计时线程被中断。", e);
					}

				}
			}).start();
		} else if (AudioManager.RINGER_MODE_SILENT != mode && musicVolumnBeforeTaken == 0) {
			manager.setStreamVolume(AudioManager.STREAM_MUSIC, maxMusicVolumn, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
		} else if (AudioManager.RINGER_MODE_SILENT != mode && systemVolumnBeforeTaken == 0) {
			manager.setStreamVolume(AudioManager.STREAM_SYSTEM, maxSystemVolumn, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
		}
	}

	private void initCamera() {

		if (!isShowCamera) {
			if (isBackFace) {
				mCurrentCameraNum = mBackCameraNum;
			} else {
				mCurrentCameraNum = mFrontCameraNum;
			}
			mCamera = Camera.open(mCurrentCameraNum);
		}

		if (mCamera != null && !isShowCamera) {
			try {
				// 设置用于显示拍照影像的SurfaceHolder对象
				mCamera.setPreviewDisplay(mHolder);
				parameters = mCamera.getParameters();
				parameters.setPictureFormat(ImageFormat.JPEG);

				if (isOpenLight && mCurrentCameraNum == mBackCameraNum) {
					parameters.setFlashMode(Parameters.FLASH_MODE_ON);
				} else {
					parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
				}
				int w = 0;
				int h = 0;
				if (Build.VERSION.SDK_INT >= 8)
					round = setCameraDisplayOrientation((Activity) mContext, mCurrentCameraNum, mCamera);

				if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
					parameters.set("orientation", "portrait");
					w = app.getScreenWidth();
					h = app.getScreentHeight();
					if (w < 480) {
						w = 480;
						h = 800;
					}
				}
				if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
					parameters.set("orientation", "landscape");
					w = app.getScreentHeight();
					h = app.getScreenWidth();
					if (h < 480) {
						h = 480;
						w = 800;
					}
				}
				Size previewSize = getOptimalPreviewSize(parameters.getSupportedPreviewSizes(), h, w);
				mVideoSize = previewSize;
				if (previewSize != null) {
					// if (isBackFace) {
					// parameters.setPreviewSize(previewSize.width,
					// previewSize.height);
					// } else {
					// int width = previewSize.width;
					// int height = previewSize.height;
					// if (getResources().getConfiguration().orientation ==
					// Configuration.ORIENTATION_PORTRAIT)
					// parameters.setPreviewSize(width, height);
					// else
					// parameters.setPreviewSize(height, width);
					// }
				}

				Size pictureSize = getOptimalPictureSize(parameters.getSupportedPictureSizes(), h, w);
				if (pictureSize != null)
					parameters.setPictureSize(pictureSize.width, pictureSize.height);
				List<String> focusModes = parameters.getSupportedFocusModes();
				if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
					parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
				} else {
				}
				mCamera.setParameters(parameters);

				int height = h;
				int width = w;
				int previewWidth = height;
				int previewHeight = width;
				if (previewSize != null) {
					previewWidth = previewSize.height;
					previewHeight = previewSize.width;
				}

				// Center the child SurfaceView within the parent.
				if (width * previewHeight > height * previewWidth) {
					final int scaledChildWidth = previewWidth * height / previewHeight;
					mLayoutX = (width - scaledChildWidth) / 2;
					mLayoutY = 0;
					mLayoutW = (width + scaledChildWidth) / 2;
					mLayoutH = height;
					// this.layout((width - scaledChildWidth) / 2, 0, (width +
					// scaledChildWidth) / 2, height);
				} else {
					final int scaledChildHeight = previewHeight * width / previewWidth;
					// this.layout(0, (height - scaledChildHeight) / 2, width,
					// (height + scaledChildHeight) / 2);
					mLayoutX = 0;
					mLayoutY = (height - scaledChildHeight) / 2;
					mLayoutW = width;
					mLayoutH = (height + scaledChildHeight) / 2;
				}

				mCamera.startPreview();
				requestLayout();
				isShowCamera = true;
			} catch (Exception exception) {
				// 释放手机摄像头
				releaseCamera();
			}
		}

	}

	private int mLayoutX = 0;

	private int mLayoutY = 0;

	private int mLayoutW = 0;

	private int mLayoutH = 0;

	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		if (getChildCount() > 0) {
			final View child = getChildAt(0);
			if (mLayoutW != 0 && mLayoutH != 0) {
				child.layout(mLayoutX, mLayoutY, mLayoutW, mLayoutH);
			}
		}
	}

	private void releaseCamera() {
		if (mCamera != null) {
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
		if (parameters != null)
			parameters = null;
		isShowCamera = false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		takeFocuse();
		return super.onTouchEvent(event);
	}

	private MediaRecorder mMediaRecorder;
	private File tempFile;
	private Size mVideoSize;

	public void startVideoRecord() {
		showLoadingDialog();
		Thread thread = new Thread() {
			public void run() {
				try {
					tempFile = new File(new File(app.getSendFileCachePath()), "video.3gp");
					// tempFile = new
					// File(Environment.getExternalStorageDirectory(),
					// "video.3gp");
					clearVideoTempFile();
					CamcorderProfile paramCamcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
					mCamera.unlock();
					if (mMediaRecorder == null)
						mMediaRecorder = new MediaRecorder();
					else
						mMediaRecorder.reset();

					mMediaRecorder.setCamera(mCamera);
					mMediaRecorder.setPreviewDisplay(mHolder.getSurface());
					mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
					mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
					// this.mMediaRecorder.setOutputFormat(paramCamcorderProfile.fileFormat);
					mMediaRecorder.setProfile(paramCamcorderProfile);
					// if (mVideoSize != null)
					// this.mMediaRecorder.setVideoSize(mVideoSize.width,
					// mVideoSize.height);
					// else
					// this.mMediaRecorder.setVideoSize(paramCamcorderProfile.videoFrameWidth,
					// paramCamcorderProfile.videoFrameHeight);
					// this.mMediaRecorder.setVideoFrameRate(30);
					// this.mMediaRecorder.setVideoEncoder(paramCamcorderProfile.videoCodec);
//					if (isBackFace)
						mMediaRecorder.setVideoEncodingBitRate(1000000);
					// this.mMediaRecorder.setAudioEncodingBitRate(paramCamcorderProfile.audioBitRate);
					// this.mMediaRecorder.setAudioChannels(paramCamcorderProfile.audioChannels);
					// this.mMediaRecorder.setAudioSamplingRate(paramCamcorderProfile.audioSampleRate);
					// this.mMediaRecorder.setAudioEncoder(paramCamcorderProfile.audioCodec);
					if (isBackFace) {
						mMediaRecorder.setOrientationHint(round);
					} else {
						mMediaRecorder.setOrientationHint(270);
					}
					mMediaRecorder.setOutputFile(tempFile.getPath());
					mMediaRecorder.prepare();
					mMediaRecorder.start();
					mHandler.sendEmptyMessage(MSG_WHAT_VIDEO_RECORD_STARTED);
				} catch (Exception e) {
					e.printStackTrace();
					mHandler.sendEmptyMessage(MSG_WHAT_VIDEO_RECORD_FAIL);
				}
			};
		};
		thread.start();
	}

	private Timer mTimer;

	private void startTimerTask() {
		if (mTimer == null)
			mTimer = new Timer();
		mTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				mHandler.sendEmptyMessage(MSG_WHAT_STOP_RECORD);
			}
		}, maxVideoRecordTime);
	}

	private static final int MSG_WHAT_STOP_RECORD = 20;

	protected static final int MSG_WHAT_VIDEO_RECORD_STARTED = 21;

	protected static final int MSG_WHAT_VIDEO_RECORD_FAIL = 0;
	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			dismissLoadingDialog();
			switch (msg.what) {
			case MSG_WHAT_STOP_RECORD:
				stopRecord();
				break;
			case MSG_WHAT_VIDEO_RECORD_STARTED:
				processVideoListener(VideoRecordState.START);
				startTimerTask();
				videoRecordStartTime = System.currentTimeMillis();
				break;
			case MSG_WHAT_VIDEO_RECORD_FAIL:
				processVideoListener(VideoRecordState.FAIL);
				break;
			default:
				break;
			}
		};
	};

	public void stopRecord() {
		try {
			mMediaRecorder.stop();
			long videoRecordTime = System.currentTimeMillis() - videoRecordStartTime;
			if (videoRecordTime > Constants.TimeMillins.MAX_VIDEO_RECORD_TIME)
				videoRecordTime = Constants.TimeMillins.MAX_VIDEO_RECORD_TIME;
			videoLength = (int) (videoRecordTime / 1000);
			if (videoLength == 0)
				videoLength = 1;
			// mCamera.stopPreview();
			// mCamera.release();
			mTimer.cancel();
			mTimer = null;
			// initCamera();
			processVideoListener(VideoRecordState.END);
		} catch (Exception e) {
			e.printStackTrace();
			mMediaRecorder.release();
			processVideoListener(VideoRecordState.FAIL);
		}
	}

	private ProgressDialog loadingDialog;

	private void showLoadingDialog() {
		if (loadingDialog == null) {
			loadingDialog = new ProgressDialog(getContext());
			loadingDialog.setCancelable(false);
		}
		loadingDialog.show();
	}

	private void dismissLoadingDialog() {
		try {
			if (loadingDialog != null && loadingDialog.isShowing())
				loadingDialog.dismiss();
		} catch (Exception e) {

		}
	}

	private void processVideoListener(VideoRecordState state) {
		recordState = state;
		if (videoRecordListener != null) {
			switch (state) {
			case START:
				videoRecordListener.onRecordStart(tempFile.getPath());
				break;
			case FAIL:
				RCThreadPool.getInstance().addTask(new Runnable() {

					@Override
					public void run() {
						if (tempFile != null && tempFile.exists())
							tempFile.delete();
					}
				});
				videoRecordListener.onRecordFail();
				break;
			case END:
				videoRecordListener.onRecordEnd(tempFile.getPath(), videoLength);
				break;
			}
		}
	}

	public void setOnVideoRecordListener(OnVideoRecordListener listener) {
		this.videoRecordListener = listener;
	}

	enum VideoRecordState {
		START, FAIL, END;
	}
}
