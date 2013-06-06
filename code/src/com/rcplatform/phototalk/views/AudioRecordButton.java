package com.rcplatform.phototalk.views;

import java.io.File;
import java.io.IOException;

import com.rcplatform.phototalk.PhotoTalkApplication;
import com.rcplatform.phototalk.R;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OutputFormat;
import android.os.Handler;
import android.os.Message;
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

	static public final int AUDIO_RECORD_TOO_SHORT = 3;

	static public final int AUDIO_RECORD_TOO_SHORT_SHOW_END = 4;

	static public final int AUDIO_RECORD_START = 5;

	static public final int AUDIO_RECORD_END = 6;

	static public final int AUDIO_RECORDING = 7;

	private long mStartTime = 0l;

	private int mRecordedSeconds = 0;

	private MediaRecorder mRecorder;

	private String mFilePath = "/record.amr";

	private Handler mHandler = new Handler();

	private int state = 0;

	private OnRecordingListener mOnRecordingListener;

	private Handler mRecordHandler;

	public void setMaxRecoedSize(int maxRecoedSize) {
		this.maxRecoedSize = maxRecoedSize;

	}

	public void setSavePath(String path) {
		mFilePath = path+mFilePath;
	}

	public void setVoiceHandler(Handler handler) {
		this.mRecordHandler = handler;
	}

	private Runnable mRecordingListenerTask = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if (state == STATE_RECORDING) {
				mOnRecordingListener.onRecording(mRecordedSeconds++, mRecorder.getMaxAmplitude());
				mHandler.postDelayed(mRecordingListenerTask, 50);

				if (mRecordedSeconds > maxRecoedSize*20) {
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
				state = 0;
				// 开始录音
				play(mFilePath);
				break;
			case MotionEvent.ACTION_UP:
				if (state == STATE_RECORDING) {
					stopRecord();
				} else {
					mRecordHandler.sendEmptyMessage(AUDIO_RECORD_TOO_SHORT);

				}
				state = STATE_RECORD_STOP;
				break;
		}
		return true;
	}

	public void play(final String voicePath) {
		MediaPlayer player = new MediaPlayer();
		player.setAudioStreamType(AudioManager.STREAM_RING);
		try {
			AssetFileDescriptor fileDescriptor = this.getContext().getAssets().openFd("start.mp3");
			player.setDataSource(fileDescriptor.getFileDescriptor(), fileDescriptor.getStartOffset(), fileDescriptor.getLength());
			player.prepare();
		}
		catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		player.start();
		player.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				// TODO Auto-generated method stub
				try {
					mp.release();
					if (state != STATE_RECORD_STOP) {
						startRecord(voicePath);
					}
				}
				catch (Exception e) {
					// TODO: handle exception
				}
			}
		});
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
		// showAttention();
		mStartTime = System.currentTimeMillis();
		mRecorder = new MediaRecorder();
		mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		mRecorder.setOutputFile(filePath);
		mRecorder.setOutputFormat(OutputFormat.DEFAULT);
		mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		try {
			mRecorder.prepare();
			mRecorder.start();
			state = STATE_RECORDING;
		}
		catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Message msg = new Message();
		msg.what = AUDIO_RECORD_START;
		msg.obj = this.maxRecoedSize;
		mRecordHandler.sendMessage(msg);
		mHandler.post(mRecordingListenerTask);
	}

	private void stopMediaRecorder() {
		try {
			mRecorder.stop();
			mRecorder.release();
			state = STATE_RECORD_STOP;
		}
		catch (Exception e) {
			// TODO: handle exception
		}

	}

	private void stopRecord() {

		stopMediaRecorder();
		if ((System.currentTimeMillis() - mStartTime) < 1 * 1000) {
			// Toast.makeText(getContext(), "录音时间太短",
			// Toast.LENGTH_SHORT).show();
			mRecordHandler.sendEmptyMessage(AUDIO_RECORD_TOO_SHORT);
		} else {
			mRecordedSeconds = (int) (System.currentTimeMillis() - mStartTime) / 1000;
			mOnRecordingListener.endRecord(mFilePath, mRecordedSeconds);
		}
		mRecordedSeconds = 0;

		mRecordHandler.sendEmptyMessage(AUDIO_RECORD_END);
	}

	public void setOnRecordingListener(OnRecordingListener listener) {

		this.mOnRecordingListener = listener;
	}

	public static interface OnRecordingListener {

		public void onRecording(int recordedSecord, int amplitude);

		public void endRecord(String savePath, int size);
	}

	public void deleteRecord() {
		File file = new File(mFilePath);
		if (file.exists()) {
			file.delete();
		}
	}
}
