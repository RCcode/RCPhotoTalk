package com.rcplatform.phototalk.logic;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import com.rcplatform.phototalk.utils.Contract.Action;
import com.rcplatform.phototalk.utils.PhotoTalkUtils;

public class PhotoInformationCountDownService {

	private static final PhotoInformationCountDownService service = new PhotoInformationCountDownService();
	private static final long COUNT_DOWN_SPACEING = 1000;

	public static synchronized PhotoInformationCountDownService getInstance() {
		return service;
	}

	private Application mApplication;
	private Map<String, Information> mShowingInformations = new HashMap<String, Information>();
	private List<Message> mCountDownMsgs = new ArrayList<Message>();
	private ThreadPoolExecutor mPool;
	private Timer mCountDownTimer = new Timer();

	private PhotoInformationCountDownService() {
		mPool = (ThreadPoolExecutor) Executors.newScheduledThreadPool(5);
	}

	public void addInformation(Information info) {
		mShowingInformations.put(info.getRecordId(), info);
		sendDelayMessage(info);
		startCountDown(info);
	}

	private void startCountDown(Information info) {
		mCountDownTimer.schedule(new InformationCountDownTask(info), 0, COUNT_DOWN_SPACEING);
	}

	private void sendDelayMessage(Information info) {
		Message msg = mCountDownHandler.obtainMessage();
		msg.obj = info.getRecordId();
		mCountDownMsgs.add(msg);
		mCountDownHandler.sendMessageDelayed(msg, info.getTotleLength() * 1000);
	}

	private Handler mCountDownHandler = new Handler() {
		public void handleMessage(Message msg) {
			mCountDownMsgs.remove(msg);
			String recordId = (String) msg.obj;
			Information info = mShowingInformations.get(recordId);
			info.setLastUpdateTime(System.currentTimeMillis());
			info.setStatu(InformationState.STATU_NOTICE_OPENED);
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
			LogicUtils.updateInformationState(mApplication, Action.ACTION_INFORMATION_STATE_CHANGE, mInfo);
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
			mInfo.setStatu(InformationState.STATU_NOTICE_SHOWING);
			mInfo.setLimitTime(mInfo.getLimitTime() - 1);
			if (mInfo.getLimitTime() <= 0) {
				cancel();
			}
		}

	}
}
