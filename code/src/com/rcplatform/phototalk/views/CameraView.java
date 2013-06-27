package com.rcplatform.phototalk.views;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
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
import android.os.Build;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.rcplatform.phototalk.PhotoTalkApplication;
import com.rcplatform.phototalk.TakePhotoActivity;
import com.rcplatform.phototalk.utils.Constants;
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

	private Bitmap mBitmap;

	private boolean isOpenLight = false;

	private boolean isBackFace = true;

	private boolean isShowCamera = false;

	private boolean isAutoFocus;

	private static int round;

	private TakeOnSuccess takeOnSuccess;

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
		// 释放手机摄像头
		releaseCamera();
	}

	// 准备一个保存图片的pictureCallback对象
	public Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			// TODO Auto-generated method stub

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
		round = result;
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
		}
		catch (Exception e) {
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
					}
					catch (Exception e) {
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
					setCameraDisplayOrientation((Activity) mContext, mCurrentCameraNum, mCamera);

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

				if (mCurrentCameraNum == mFrontCameraNum) {
					parameters.setRotation(270);
				} else {
					parameters.setRotation(90);
				}

				Size previewSize = getOptimalPreviewSize(parameters.getSupportedPreviewSizes(), h, w);
				if (previewSize != null) {
					parameters.setPreviewSize(previewSize.width, previewSize.height);

				}

				Size pictureSize = getOptimalPictureSize(parameters.getSupportedPictureSizes(), h, w);
				if (pictureSize != null)
					parameters.setPictureSize(pictureSize.width, pictureSize.height);
				List<String> focusModes = parameters.getSupportedFocusModes();
				if (focusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
					parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
					isAutoFocus = true;
				} else {
					isAutoFocus = false;
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
			}
			catch (IOException exception) {
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
}
