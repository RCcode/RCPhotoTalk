package com.rcplatform.videotalk.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class FileDownloader {
	public static final int MSG_DOWNLOAD_SUCCESS = 100;
	public static final int MSG_DOWNLOAD_FAIL = 101;
	private static final int TIME_OUT = 10 * 1000;
	private static final int READ_TIME_OUT = 30 * 1000;

	private static FileDownloader mDownloader;
	private ThreadPoolExecutor mPool;
	private Map<String, OnLoadingListener> mTaskStore = new HashMap<String, FileDownloader.OnLoadingListener>();

	private FileDownloader() {
		// TODO Auto-generated constructor stub
		mPool = (ThreadPoolExecutor) Executors.newScheduledThreadPool(5);
	}

	public synchronized static FileDownloader getInstance() {
		if (mDownloader == null)
			mDownloader = new FileDownloader();
		return mDownloader;
	}

	public void loadFile(String videoPath, String savePath, OnLoadingListener listener) {
		if (!mTaskStore.containsKey(videoPath)) {
			addTask(videoPath, savePath, listener);
		}
	}

	private void addTask(String videoPath, String savePath, OnLoadingListener listener) {
		listener.onStartLoad();
		mTaskStore.put(videoPath, listener);
		mPool.execute(new DownloadTask(videoPath, savePath));

	}

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_DOWNLOAD_SUCCESS: {
				String url = (String) msg.obj;
				mTaskStore.get(url).onDownloadSuccess();
				removeTask(url);
			}
				break;
			case MSG_DOWNLOAD_FAIL: {
				String url = (String) msg.obj;
				mTaskStore.get(url).onDownloadFail();
				removeTask(url);
			}
				break;
			}
		};
	};

	private void removeTask(String url) {
		mTaskStore.remove(url);
		if (mTaskStore.size() == 0)
			Log.e("tag", "all download over");
	}

	class DownloadTask implements Runnable {
		private String mVideoPath, mSavePString;

		public DownloadTask(String videoPath, String savePath) {
			// TODO Auto-generated constructor stub
			this.mVideoPath = videoPath;
			this.mSavePString = savePath;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				deleteFailFile(mSavePString);
				downloadFile(mVideoPath, mSavePString);
				sendSuccessMessage(mVideoPath);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				deleteFailFile(mSavePString);
				sendFailMessage(mVideoPath);

			}
		}

	}

	private void deleteFailFile(String path) {
		// TODO Auto-generated method stub
		File file = new File(path);
		if (file.exists())
			file.delete();
	}

	private void downloadFile(String path, String savePath) throws IOException {
		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(TIME_OUT);
		conn.setReadTimeout(READ_TIME_OUT);
		if (conn.getResponseCode() == 200) {
			InputStream in = conn.getInputStream();
			readStream(in, savePath);
		} else {
			conn.disconnect();
			throw new IOException();
		}
		conn.disconnect();
	}

	private void readStream(InputStream in, String savePath) throws IOException {
		ByteArrayOutputStream byteOS = new ByteArrayOutputStream();
		byte[] buffered = new byte[1024];
		int len = 0;
		while ((len = in.read(buffered)) != -1) {
			byteOS.write(buffered, 0, len);
		}
		byteOS.flush();
		in.close();
		writeFile(savePath, byteOS);
		byteOS.close();
	}

	private void writeFile(String savePath, ByteArrayOutputStream byteOS) throws IOException {
		File file = new File(savePath);
		File parent = file.getParentFile();
		if (!parent.exists())
			parent.mkdirs();
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(byteOS.toByteArray());
		fos.flush();
		fos.close();
	}

	private void sendSuccessMessage(String videoPath) {
		Message msg = mHandler.obtainMessage();
		msg.what = MSG_DOWNLOAD_SUCCESS;
		msg.obj = videoPath;
		mHandler.sendMessage(msg);
	}

	private void sendFailMessage(String videoPath) {
		Message msg = mHandler.obtainMessage();
		msg.what = MSG_DOWNLOAD_FAIL;
		msg.obj = videoPath;
		mHandler.sendMessage(msg);
	}

	public static interface OnLoadingListener {
		public void onStartLoad();

		public void onDownloadSuccess();

		public void onDownloadFail();
	}

	public void destroy() {
		mPool.shutdown();
		mPool = null;
		mTaskStore.clear();
		mTaskStore = null;
	}
}
