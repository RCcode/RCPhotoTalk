package com.rcplatform.phototalk.views;

import java.io.File;
import java.io.FileInputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.VideoView;

import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.InformationType;
import com.rcplatform.phototalk.drift.DriftInformation;
import com.rcplatform.phototalk.galhttprequest.LogUtil;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.PhotoTalkUtils;
import com.rcplatform.phototalk.utils.ZipUtil;
import com.rcplatform.phototalk.views.RecordTimerLimitView.OnTimeEndListener;

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

		private static MediaPlayer mAudioPlayer;

		private static VideoView mVideoView;

		private int layoutResId;

		private LongClickShowView dialog;

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
				dialog = new LongClickShowView(context, android.R.style.Theme_Black_NoTitleBar);
			if (dialog.contentView == null) {
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				dialog.contentView = (RelativeLayout) inflater.inflate(layoutResId, null);
				mImageView = (ImageView) dialog.contentView.findViewById(R.id.iv_rts_pic);
				mVideoView = (VideoView) dialog.contentView.findViewById(R.id.vv_show);
				dialog.setContentView(dialog.contentView);
			} else {
				// dialog.setContentView(dialog.contentView);
			}
			Log.i("ABC", "DIALOG = " + dialog.toString());
			/*
			 * contentView.setOnClickListener(new View.OnClickListener(){
			 * 
			 * @Override public void onClick(View view) { dialog.hide(); } });
			 */
			return dialog;
		}

	}

	public void ShowDialog(Information info) {
		resetViews();
		if (info.getUrl() == null)
			return;
		if (glTimer == null) {
			initTimer();
		}
		if (info.getType() == InformationType.TYPE_PICTURE_OR_VIDEO) {// 图片
			try {
				List<File> files = unZipFile(info.getUrl());
				showZipContent(files, info);
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (info.getType() == InformationType.TYPE_SYSTEM_NOTICE) { // 系统消息
			glTimer.setVisibility(View.GONE);
			Builder.mImageView.setVisibility(View.GONE);
		}
		glTimer.scheuleTask(info);
		show();
	}

	public void ShowDialog(DriftInformation info) {
		resetViews();
		if (info.getUrl() == null)
			return;
		if (glTimer == null) {
			initTimer();
		}
		try {
			List<File> files = unZipFile(info.getUrl());
			showZipContent(files, info);
		} catch (Exception e) {
			e.printStackTrace();
		}
		glTimer.scheuleTask(info);
		show();
	}
	private void resetViews(){
		Builder.mImageView.setImageBitmap(null);
	}
	private void showZipContent(List<File> fileList, Information info) throws Exception {
		for (File file : fileList) {
			if (isImage(file.getName())) {
				showImage(file);
			} else if (isAudio(file.getName())) {
				playAudio(file, info);
			} else if (isVideo(file.getName())) {
				playVideo(file, info);
			}
		}
		glTimer.setVisibility(View.VISIBLE);
		Builder.mImageView.setVisibility(View.VISIBLE);
	}

	private void playVideo(File file, Information info) {
		LogUtil.e(file.getTotalSpace()+"~~~~~~~~~~~~~~~~~"+file.getPath());
		Builder.mVideoView.setVisibility(View.VISIBLE);
		Builder.mVideoView.setVideoURI(Uri.fromFile(file));
		Builder.mVideoView.start();
		if (info.getTotleLength() != info.getLimitTime())
			Builder.mVideoView.seekTo(info.getTotleLength() * 1000 - info.getLimitTime() * 1000);
	}
	private void playVideo(File file, DriftInformation info) {
		Builder.mVideoView.setVisibility(View.VISIBLE);
		Builder.mVideoView.setVideoPath(file.getPath());
		Builder.mVideoView.start();
		if (info.getTotleLength() != info.getLimitTime())
			Builder.mVideoView.seekTo(info.getTotleLength() * 1000 - info.getLimitTime() * 1000);
	}

	private void showZipContent(List<File> fileList, DriftInformation info) throws Exception {
		for (File file : fileList) {
			if (isImage(file.getName())) {
				showImage(file);
			} else if (isAudio(file.getName())) {
				playAudio(file, info);
			}else if(isVideo(file.getName())){
				playVideo(file, info);
			}
		}
		glTimer.setVisibility(View.VISIBLE);
		Builder.mImageView.setVisibility(View.VISIBLE);
	}

	private List<File> unZipFile(String url) throws Exception {
		resultFiles.clear();
		String unZipPath = getUnZipPath(url);
		File file = new File(unZipPath);
		if (file.exists()) {
			listFiles(file);
			return resultFiles;
		} else {
			if (file.mkdirs()) {
				String filePath = PhotoTalkUtils.getFilePath(url);
				ZipUtil.UnZipFolder(filePath, unZipPath);
				listFiles(file);
				return resultFiles;
			}
		}
		return null;
	}

	private List<File> resultFiles = new ArrayList<File>();

	private void listFiles(File file) {

		if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				listFiles(f);
			}
		} else {
			resultFiles.add(file);
		}
	}

	private String getUnZipPath(String url) {
		return PhotoTalkUtils.getUnZipDirPath(url);
	}

	private void playAudio(final File file, final Information info) throws Exception {

		FileInputStream fis = new FileInputStream(file.getAbsolutePath());
		Builder.mAudioPlayer.reset();
		// Builder.mAudioPlayer.setVolume(arg0, arg1)
		Builder.mAudioPlayer.setAudioStreamType(AudioManager.STREAM_RING);
		Builder.mAudioPlayer.setDataSource(fis.getFD());
		Builder.mAudioPlayer.prepare();
		Builder.mAudioPlayer.start();
		fis.close();

		if (info.getTotleLength() != info.getLimitTime())
			Builder.mAudioPlayer.seekTo(info.getTotleLength() * 1000 - info.getLimitTime() * 1000);

	}

	private void playAudio(final File file, final DriftInformation info) throws Exception {

		FileInputStream fis = new FileInputStream(file.getAbsolutePath());
		Builder.mAudioPlayer.reset();
		// Builder.mAudioPlayer.setVolume(arg0, arg1)
		Builder.mAudioPlayer.setAudioStreamType(AudioManager.STREAM_RING);
		Builder.mAudioPlayer.setDataSource(fis.getFD());
		Builder.mAudioPlayer.prepare();
		Builder.mAudioPlayer.start();
		fis.close();

		if (info.getTotleLength() != info.getLimitTime())
			Builder.mAudioPlayer.seekTo(info.getTotleLength() * 1000 - info.getLimitTime() * 1000);

	}

	private void showImage(File file) {
		try {
			currentBitmap = new WeakReference<Bitmap>(BitmapFactory.decodeFile(file.getPath())).get();
		} catch (Throwable e) {
		}
		Builder.mImageView.setImageBitmap(currentBitmap);
	}

	private boolean isImage(String fileName) {
		return fileName.endsWith(Constants.IMAGE_FORMAT);
	}

	private boolean isAudio(String fileName) {
		return fileName.endsWith(Constants.AUDIO_FORMAT);
	}

	private boolean isVideo(String fileName) {
		return fileName.endsWith(Constants.VIDEO_FORMAT);
	}

	public void hideDialog() {
		hide();
		// Builder.mImageView = null;
		Builder.mAudioPlayer.stop();
		if (Builder.mVideoView.getVisibility() == View.VISIBLE && Builder.mVideoView.isPlaying())
			Builder.mVideoView.pause();
		Builder.mVideoView.setVisibility(View.GONE);
		if (currentBitmap != null && !currentBitmap.isRecycled()) {
			currentBitmap.recycle();
			currentBitmap = null;
		}
		// contentView.removeAllViews();
		// contentView = null;
		// if(Builder.dialog!=null&&Builder.dialog.isShowing()){
		// Builder.dialog.dismiss();
		// }
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
		contentView.addView(glTimer, params);
	}
}
