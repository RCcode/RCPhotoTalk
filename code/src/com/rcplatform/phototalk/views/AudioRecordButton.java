package com.rcplatform.phototalk.views;

import java.io.File;
import java.io.IOException;

import com.rcplatform.phototalk.MenueApplication;
import com.rcplatform.phototalk.R;

import android.content.Context;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OutputFormat;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class AudioRecordButton extends Button {
	private int maxRecoedSize = 10;
	private final int STATE_RECORDING = 1;
	// ¼������
	private final int STATE_RECORD_STOP = 2;

	private long mStartTime = 0l;

	private int mRecordedSeconds = 0;

	private MediaRecorder mRecorder;
	private String mFilePath = "/record.amr";
	private Handler mHandler = new Handler();
	private AudioShowView mAttentionView;
	private WindowManager mWindowManager;
	private int state;
	private OnRecordingListener mOnRecordingListener;

	public void setMaxRecoedSize(int maxRecoedSize) {
		this.maxRecoedSize = maxRecoedSize;

	}

	public void setSavePath(String path) {
		mFilePath = path + mFilePath;
	}

	private Runnable mRecordingListenerTask = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (state == STATE_RECORDING) {
				mOnRecordingListener.onRecording(mRecordedSeconds++,
						mRecorder.getMaxAmplitude());
				mHandler.postDelayed(mRecordingListenerTask, 1000);
				mAttentionView.setChanceText(maxRecoedSize - mRecordedSeconds + "");
				if (mRecordedSeconds > maxRecoedSize) {
					stopRecord();
				}
			}
		}
	};

	public AudioRecordButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public AudioRecordButton(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public AudioRecordButton(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			this.setBackgroundResource(R.drawable.btn_download_p);
			startRecord(mFilePath);
			break;
		case MotionEvent.ACTION_UP:
			this.setBackgroundResource(R.drawable.btn_download_n);
			if (state == STATE_RECORDING)
				stopRecord();
			break;
		}
		return true;
	}

	public void setOutputFile(String filePath) {
		mFilePath = filePath;
	}

	private void startRecord(String filePath) {
		// TODO Auto-generated method stub
		File file = new File(filePath);
		if (file.exists()) {
			System.out.println("file.exists()");
			file.delete();
		}
		System.out.println("filePath--->+"+filePath);
		showAttention();
		mStartTime = System.currentTimeMillis();
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFile(filePath);
		mRecorder.setOutputFormat(OutputFormat.RAW_AMR);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		try {
			mRecorder.prepare();
			mRecorder.start();
			state = STATE_RECORDING;
			// mHandler.postDelayed(mStopRecordTask, MAX_RECORD_TIME * 1000);
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void stopMediaRecorder() {
		try {
			mRecorder.stop();
			mRecorder.release();
			state = STATE_RECORD_STOP;
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	private void showAttention() {
		if (mAttentionView != null) {
			LayoutParams params = new WindowManager.LayoutParams();
			params.width = WindowManager.LayoutParams.WRAP_CONTENT;
			params.height = WindowManager.LayoutParams.WRAP_CONTENT;
			params.gravity = Gravity.CENTER;
			mWindowManager.addView(mAttentionView, params);
			mHandler.post(mRecordingListenerTask);
		}
	}

	private void removeAttention() {
		if (mAttentionView != null) {
			mWindowManager.removeViewImmediate(mAttentionView);
		}
	}

	private void stopRecord() {
		
		removeAttention();
		stopMediaRecorder();
		if ((System.currentTimeMillis() - mStartTime) < 1 * 1000) {
			Toast.makeText(getContext(), "录音时间太短", Toast.LENGTH_SHORT).show();
		} else {
			mOnRecordingListener.endRecord(mFilePath,mRecordedSeconds);
		}
		mRecordedSeconds = 0;
	}

	public void setAttentionView(WindowManager manager, AudioShowView view,
			OnRecordingListener listener) {
		this.mAttentionView = view;
		this.mWindowManager = manager;
		this.mOnRecordingListener = listener;
	}

	public static interface OnRecordingListener {
		public void onRecording(int recordedSecord, int amplitude);

		public void endRecord(String savePath,int size);
	}

	public void deleteRecord() {
		File file = new File(mFilePath);
		if (file.exists()) {
			file.delete();
		}
	}
}
