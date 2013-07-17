package com.rcplatform.phototalk.logic;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import android.app.Application;
import android.os.Handler;
import android.os.Message;

import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.InformationState;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.logic.controller.InformationPageController;
import com.rcplatform.phototalk.utils.PhotoTalkUtils;

public class PhotoInformationCountDownService {

	private static final PhotoInformationCountDownService service = new PhotoInformationCountDownService();
	private static final long COUNT_DOWN_SPACEING = 1000;

	public static synchronized PhotoInformationCountDownService getInstance() {
		return service;
	}

	private Application mApplication;
	private Map<String, Information> mShowingInformations = new HashMap<String, Information>();
	private ThreadPoolExecutor mPool;
	private Timer mCountDownTimer = new Timer();

	private PhotoInformationCountDownService() {
		mPool = (ThreadPoolExecutor) Executors.newScheduledThreadPool(5);
	}

	public void addInformation(Information info) {
		if (!mShowingInformations.containsKey(PhotoTalkUtils.getInformationTagBase(info))) {
			info.setStatu(InformationState.PhotoInformationState.STATU_NOTICE_SHOWING);
			PhotoTalkDatabaseFactory.getDatabase().updateInformationState(info);
			mShowingInformations.put(PhotoTalkUtils.getInformationTagBase(info), info);
			startCountDown(info);
		}
	}

	private void startCountDown(Information info) {
		mCountDownTimer.schedule(new InformationCountDownTask(info), 0, COUNT_DOWN_SPACEING);
	}

	private void sendShowEndMessage(Information info) {
		Message msg = mCountDownHandler.obtainMessage();
		msg.obj = info;
		mCountDownHandler.sendMessage(msg);
	}

	private Handler mCountDownHandler = new Handler() {
		public void handleMessage(Message msg) {
			Information info = (Information) msg.obj;
			mShowingInformations.remove(PhotoTalkUtils.getInformationTagBase(info));
			info.setStatu(InformationState.PhotoInformationState.STATU_NOTICE_OPENED);
			if (!info.getReceiver().getRcId().equals(info.getSender().getRcId())) {
				MessageSender.getInstance().sendInformation(mApplication, info.getSender().getTigaseId(), info.getSender().getRcId(), info);
			}
			InformationPageController.getInstance().photoInformationShowEnd(info);
			mPool.execute(new ClearPhotoInformationCacheTask(info));
		};
	};

	public void setApplication(Application application) {
		mApplication = application;
	}

	class ClearPhotoInformationCacheTask implements Runnable {
		private Information mInfo;

		public ClearPhotoInformationCacheTask(Information info) {
			this.mInfo = info;
		}

		@Override
		public void run() {
			PhotoTalkDatabaseFactory.getDatabase().updateInformationState(mInfo);
			deleteCacheFiles(mInfo);
		}
	}

	private void deleteCacheFiles(Information info) {
		String filePath = PhotoTalkUtils.getFilePath(info.getUrl());
		String unZipDirPath = PhotoTalkUtils.getUnZipDirPath(info.getUrl());
		File zipFile = new File(filePath);
		File unZipDir = new File(unZipDirPath);
		deleteFile(zipFile);
		deleteFile(unZipDir);
	}

	private void deleteFile(File file) {
		if (!file.exists())
			return;
		if (file.isFile()) {
			// SoundManager.getInstance().release(file.getAbsolutePath());
			file.delete();
		} else if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				deleteFile(f);
			}
			file.delete();
		}

	}

	class InformationCountDownTask extends TimerTask {
		private Information mInfo;

		public InformationCountDownTask(Information information) {
			this.mInfo = information;
		}

		@Override
		public void run() {
			mInfo.setStatu(InformationState.PhotoInformationState.STATU_NOTICE_SHOWING);
			mInfo.setLimitTime(mInfo.getLimitTime() - 1);
			if (mInfo.getLimitTime() <= 0) {
				sendShowEndMessage(mInfo);
				cancel();
			}
		}

	}

	public void finishAllShowingMessage() {
		mCountDownTimer.cancel();
		mCountDownTimer = new Timer();
		for (Information info : mShowingInformations.values()) {
			info.setLimitTime(0);
			sendShowEndMessage(info);
		}
		mShowingInformations.clear();
	}
}
