package com.rcplatform.phototalk.logic;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import android.app.Application;
import android.os.Handler;
import android.os.Message;
import android.util.SparseArray;

import com.rcplatform.phototalk.bean.InformationState;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.drift.DriftInformation;
import com.rcplatform.phototalk.logic.controller.DriftInformationPageController;
import com.rcplatform.phototalk.utils.PhotoTalkUtils;

public class DriftInformationCountDownService {

	private static final DriftInformationCountDownService service = new DriftInformationCountDownService();
	private static final long COUNT_DOWN_SPACEING = 1000;

	public static synchronized DriftInformationCountDownService getInstance() {
		return service;
	}

	private SparseArray<DriftInformation> mShowingInformations = new SparseArray<DriftInformation>();
	private ThreadPoolExecutor mPool;
	private Timer mCountDownTimer = new Timer();

	private DriftInformationCountDownService() {
		mPool = (ThreadPoolExecutor) Executors.newScheduledThreadPool(5);
	}

	public void addInformation(DriftInformation info) {
		if (mShowingInformations.get(info.getPicId()) == null) {
			info.setState(InformationState.PhotoInformationState.STATU_NOTICE_SHOWING);
			PhotoTalkDatabaseFactory.getDatabase().updateDriftInformationState(info.getPicId(), InformationState.PhotoInformationState.STATU_NOTICE_SHOWING);
			mShowingInformations.put(info.getPicId(), info);
			startCountDown(info);
		}
	}

	private void startCountDown(DriftInformation info) {
		mCountDownTimer.schedule(new InformationCountDownTask(info), 0, COUNT_DOWN_SPACEING);
	}

	private void sendShowEndMessage(DriftInformation info) {
		Message msg = mCountDownHandler.obtainMessage();
		msg.obj = info;
		mCountDownHandler.sendMessage(msg);
	}

	private Handler mCountDownHandler = new Handler() {
		public void handleMessage(Message msg) {
			DriftInformation info = (DriftInformation) msg.obj;
			mShowingInformations.remove(info.getPicId());
			info.setState(InformationState.PhotoInformationState.STATU_NOTICE_OPENED);
			DriftInformationPageController.getInstance().onDriftShowEnd(info);
			mPool.execute(new ClearPhotoInformationCacheTask(info));
		};
	};

	class ClearPhotoInformationCacheTask implements Runnable {
		private DriftInformation mInfo;

		public ClearPhotoInformationCacheTask(DriftInformation info) {
			this.mInfo = info;
		}

		@Override
		public void run() {
			PhotoTalkDatabaseFactory.getDatabase().updateDriftInformationState(mInfo.getPicId(), mInfo.getState());
			deleteCacheFiles(mInfo);
		}
	}

	private void deleteCacheFiles(DriftInformation info) {
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
			file.delete();
		} else if (file.isDirectory()) {
			for (File f : file.listFiles()) {
				deleteFile(f);
			}
			file.delete();
		}

	}

	class InformationCountDownTask extends TimerTask {
		private DriftInformation mInfo;

		public InformationCountDownTask(DriftInformation information) {
			this.mInfo = information;
		}

		@Override
		public void run() {
			mInfo.setState(InformationState.PhotoInformationState.STATU_NOTICE_SHOWING);
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
		for (int i = 0; i < mShowingInformations.size(); i++) {
			DriftInformation info = mShowingInformations.get(mShowingInformations.keyAt(i));
			info.setLimitTime(0);
			sendShowEndMessage(info);
		}
		mShowingInformations.clear();
	}
}
