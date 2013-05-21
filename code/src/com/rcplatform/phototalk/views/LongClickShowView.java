package com.rcplatform.phototalk.views;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.InformationType;
import com.rcplatform.phototalk.utils.Contract;
import com.rcplatform.phototalk.utils.PhotoTalkUtils;
import com.rcplatform.phototalk.utils.SoundManager;
import com.rcplatform.phototalk.utils.ZipUtil;
import com.rcplatform.phototalk.views.PlayVidoeView.OnStartPlayListener;
import com.rcplatform.phototalk.views.RecordTimerLimitView.OnTimeEndListener;
import android.media.SoundPool;

public class LongClickShowView extends Dialog {

	public Rect invaildRange; // 无效区域，

	public boolean invalidTouch;

	private RecordTimerLimitView glTimer;

	private LayoutParams params;

	private RelativeLayout contentView;

	private Bitmap currentBitmap;

	public LongClickShowView(Context context, int theme) {
		super(context, theme);
		this.getWindow().setWindowAnimations(R.style.ContentOverlay);
	}

	public LongClickShowView(Context context) {
		super(context);

	}

	/**
	 * Helper class for creating a custom dialog
	 */
	public static class Builder {

		private final Context context;

		private static ImageView mImageView;

		private static PlayVidoeView mPlayVidoeView;

		private static MediaPlayer mAudioPlayer;

		private int layoutResId;

		private LongClickShowView dialog;

		private static SoundPool soundPool = new SoundPool(3, AudioManager.STREAM_MUSIC, 0);

		public Builder(Context context, int layoutResId) {
			this.context = context;
			this.layoutResId = layoutResId;
			mAudioPlayer = new MediaPlayer();
		}

		public Builder(Context context, RelativeLayout view) {
			this.context = context;
		}

		/**
		 * Create the custom dialog
		 */
		public LongClickShowView create() {
			if (dialog == null)
				dialog = new LongClickShowView(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
			if (dialog.contentView == null) {
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				dialog.contentView = (RelativeLayout) inflater.inflate(layoutResId, null);
				mImageView = (ImageView) dialog.contentView.findViewById(R.id.iv_rts_pic);
				mPlayVidoeView = (PlayVidoeView) dialog.contentView.findViewById(R.id.pv_rts_video);
				dialog.setContentView(dialog.contentView);
			} else {
				dialog.setContentView(dialog.contentView);
			}
			Log.i("ABC", "DIALOG = " + dialog.toString());
			/*
			 * contentView.setOnClickListener(new View.OnClickListener(){
			 * @Override public void onClick(View view) { dialog.hide(); } });
			 */
			return dialog;
		}

	}

	public void ShowDialog(Information info) {
		if (info.getUrl() == null)
			return;
		if (glTimer == null) {
			initTimer();
		}
		if (info.getType() == InformationType.TYPE_PICTURE_OR_VIDEO) {// 图片
			try {
				File[] files = unZipFile(info.getUrl());
				showZipContent(files, info);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		} else if (info.getType() == InformationType.TYPE_SYSTEM_NOTICE) { // 系统消息
			glTimer.setVisibility(View.GONE);
			Builder.mImageView.setVisibility(View.GONE);
		}
		glTimer.scheuleTask(info);
		show();
	}

	private void showZipContent(File[] fileList, Information info) throws Exception {
		for (File file : fileList) {
			if (isImage(file.getName())) {
				showImage(file);
			} else if (isAudio(file.getName())) {
				playAudio(file, info);
			}
		}
		glTimer.setVisibility(View.VISIBLE);
		Builder.mPlayVidoeView.setVisibility(View.GONE);
		Builder.mImageView.setVisibility(View.VISIBLE);
	}

	private File[] unZipFile(String url) throws Exception {
		String unZipPath = getUnZipPath(url);
		File file = new File(unZipPath);
		if (file.exists())
			return file.listFiles()[0].listFiles();
		else {
			if (file.mkdirs()) {
				String filePath = PhotoTalkUtils.getFilePath(url);
				ZipUtil.UnZipFolder(filePath, unZipPath);
				return file.listFiles()[0].listFiles();
			}
		}
		return null;
	}

	private String getUnZipPath(String url) {
		return PhotoTalkUtils.getUnZipDirPath(url);
	}

	private void playAudio(final File file, final Information info) throws Exception {

		FileInputStream fis = new FileInputStream(file.getAbsolutePath());
		Builder.mAudioPlayer.reset();
		Builder.mAudioPlayer.setDataSource(fis.getFD());
		Builder.mAudioPlayer.prepare();
		Builder.mAudioPlayer.start();
		fis.close();

		if (info.getTotleLength() != info.getLimitTime())
			Builder.mAudioPlayer.seekTo(info.getTotleLength() * 1000 - info.getLimitTime() * 1000);

	}

	private void showImage(File file) {
		currentBitmap = BitmapFactory.decodeFile(file.getPath());

		Builder.mImageView.setImageBitmap(currentBitmap);
	}

	private boolean isImage(String fileName) {
		return fileName.endsWith(Contract.IMAGE_FORMAT);
	}

	private boolean isAudio(String fileName) {
		return fileName.endsWith(Contract.AUDIO_FORMAT);
	}

	public void hideDialog() {
		hide();
		Builder.mAudioPlayer.stop();
		if (currentBitmap != null && !currentBitmap.isRecycled()) {
			currentBitmap.recycle();
			currentBitmap = null;
		}
	}

	public void initTimer() {

		glTimer = new RecordTimerLimitView(getContext());
		params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		params.setMargins(0, 10, 40, 0);
		glTimer.setTextSize(56);
		glTimer.setTextColor(Color.RED);
		glTimer.setOnTimeEndListener(new OnTimeEndListener() {

			@Override
			public void onEnd(Object statuTag, Object buttonTag) {
				hideDialog();
			}
		}, null, null);
		Builder.mPlayVidoeView.setOnStartPlayListener(new OnStartPlayListener() {

			@Override
			public void onStart() {
				glTimer.setVisibility(View.VISIBLE);
			}
		});
		contentView.addView(glTimer, params);
	}
}
