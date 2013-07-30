package com.rcplatform.phototalk.views;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.rcplatform.phototalk.utils.Constants;

public class VideoRecordView extends SurfaceView {
	private static final int MIN_FPS = 24000;
	private static int rotate;
	private Camera mCamera;
	private File tempFile;

	public VideoRecordView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init();
	}

	public VideoRecordView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	public VideoRecordView(Context context) {
		super(context);
		init();
	}

	private void init() {
		getHolder().addCallback(new Callback() {

			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				mCamera.stopPreview();
				mCamera.release();
			}

			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				try {
					initCamera(holder);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
			}
		});
	}

	private Size mVideoSize;

	private void initCamera(SurfaceHolder holder) throws Exception {
		mCamera = Camera.open();
		Camera.Parameters parameters = mCamera.getParameters();
		mVideoSize = getOptimalPreviewSize(parameters.getSupportedPreviewSizes(), 720, 1280);
		parameters.setPreviewSize(mVideoSize.width, mVideoSize.height);
		int[] previewFpsRange = getPreviewFpsRange(parameters.getSupportedPreviewFpsRange());
		parameters.setPreviewFpsRange(previewFpsRange[0], previewFpsRange[1]);
		mCamera.setParameters(parameters);
		mCamera.setPreviewDisplay(holder);
		setCameraDisplayOrientation((Activity) getContext(), 0, mCamera);
		mCamera.startPreview();
	}

	private int[] getPreviewFpsRange(List<int[]> supportFpsRange) {
		for (int[] fpsRange : supportFpsRange) {
			if (fpsRange[0] >= MIN_FPS) {
				return fpsRange;
			}
		}
		return supportFpsRange.get(supportFpsRange.size() - 1);
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

	public static void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera) {
		Camera.CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(cameraId, info);
		int degrees = getDisplayRotation(activity);
		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		rotate = result;
		camera.setDisplayOrientation(result);
	}

	public static int getDisplayRotation(Activity activity) {
		int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
		switch (rotation) {
		case Surface.ROTATION_0:
			return 0;
		case Surface.ROTATION_90:
			return 90;
		case Surface.ROTATION_180:
			return 180;
		case Surface.ROTATION_270:
			return 270;
		}
		return 0;
	}

	public void start() {
		CamcorderProfile profile = CamcorderProfile.get(0, CamcorderProfile.QUALITY_HIGH);
		startRecord(profile);
	}

	private MediaRecorder mMediaRecorder;

	private void startRecord(CamcorderProfile paramCamcorderProfile) {
		mCamera.stopPreview();
		mCamera.unlock();
		if (mMediaRecorder == null)
			mMediaRecorder = new MediaRecorder();
		else
			mMediaRecorder.reset();
		try {
			this.mMediaRecorder.setCamera(mCamera);
			this.mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
			this.mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
			this.mMediaRecorder.setOutputFormat(paramCamcorderProfile.fileFormat);
			this.mMediaRecorder.setVideoSize(mVideoSize.width, mVideoSize.height);
			this.mMediaRecorder.setVideoFrameRate(30);
			this.mMediaRecorder.setVideoEncoder(paramCamcorderProfile.videoCodec);
			this.mMediaRecorder.setVideoEncodingBitRate(1000000);
			this.mMediaRecorder.setAudioEncodingBitRate(paramCamcorderProfile.audioBitRate);
			this.mMediaRecorder.setAudioChannels(paramCamcorderProfile.audioChannels);
			this.mMediaRecorder.setAudioSamplingRate(paramCamcorderProfile.audioSampleRate);
			this.mMediaRecorder.setAudioEncoder(paramCamcorderProfile.audioCodec);
			this.mMediaRecorder.setOrientationHint(rotate);
			tempFile = File.createTempFile("record_720_480_文字", ".3gp", Environment.getExternalStorageDirectory());
			mMediaRecorder.setOutputFile(tempFile.getPath());
			mMediaRecorder.prepare();
			mMediaRecorder.start();
			startTimerTask();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Timer mTimer;

	private void startTimerTask() {
		if (mTimer == null)
			mTimer = new Timer();
		mTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				stopRecord();
			}
		}, Constants.TimeMillins.MAX_VIDEO_RECORD_TIME);
	}

	public void stopRecord() {

		try {
			mMediaRecorder.stop();
			mCamera.stopPreview();
			mCamera.release();
			mTimer.cancel();
			initCamera(getHolder());
		} catch (Exception e) {
			e.printStackTrace();
			mMediaRecorder.release();
		}
	}
}
