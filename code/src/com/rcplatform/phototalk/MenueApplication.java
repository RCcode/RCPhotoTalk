package com.rcplatform.phototalk;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.clienservice.PTBackgroundService;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.image.downloader.ImageOptionsFactory;
import com.rcplatform.phototalk.utils.Contract;

public class MenueApplication extends Application {

	private WindowManager.LayoutParams wmParams;
	

	private static final int MEMORY_CACHE_SIZE = 2 * 1024 * 1024;

	private static final int READ_TIME_OUT = 20 * 1000;

	private static final int CONNECT_TIME_OUT = 5 * 1000;

	private static final String CACHE_FILE_PATH = "menue/cache/photochat";

	private static final int CACHE_DISK_SIZE = 1024 * 1024 * 200;

	private static final int THREAD_COUNT = 3;

	private Bitmap editeBitmap;

	public File cacheDir;

	private int mScreenWidth;

	private int mScreentHeight;

	public Map<Long, List<Information>> sendRecords;

	private static UserInfo userInfo;

	private final Map<String, Activity> mActivityMap = new HashMap<String, Activity>();

	private String sendFileCachePath;

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		DisplayMetrics metrics = getResources().getDisplayMetrics();
		mScreenWidth = metrics.widthPixels;
		mScreentHeight = metrics.heightPixels;
		ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(getApplicationContext()).memoryCache(new WeakMemoryCache()).threadPriority(THREAD_COUNT).memoryCacheSize(MEMORY_CACHE_SIZE).denyCacheImageMultipleSizesInMemory().imageDownloader(new BaseImageDownloader(this)).defaultDisplayImageOptions(ImageOptionsFactory.getDefaultImageOptions()).tasksProcessingOrder(QueueProcessingType.LIFO);
		if (createImageCacheDir()) {
			builder.discCache(new UnlimitedDiscCache(cacheDir, new Md5FileNameGenerator()));
		}
		ImageLoaderConfiguration config = builder.build();
		ImageLoader.getInstance().init(config);

		wmParams = new WindowManager.LayoutParams();
	}

	private boolean createImageCacheDir() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			cacheDir = new File(Environment.getExternalStorageDirectory(), CACHE_FILE_PATH);
			if (!cacheDir.exists()) {
				return cacheDir.mkdirs();
			}
			return true;
		}
		return false;

	}

	private PTBackgroundService mService;

	public void setService(PTBackgroundService service) {
		this.mService = service;
	}

	public PTBackgroundService getService() {
		return mService;
	}

	public String getCacheFilePath() {
		return cacheDir.getAbsolutePath();
	}

	@Override
	public void onLowMemory() {
		// TODO Auto-generated method stub
		ImageLoader.getInstance().clearMemoryCache();
		super.onLowMemory();
	}

	public Bitmap getEditeBitmap() {
		return editeBitmap;
	}

	public void setEditeBitmap(Bitmap editeBitmap) {
		this.editeBitmap = editeBitmap;
	}

	public int getScreenWidth() {
		return mScreenWidth;
	}

	public int getScreentHeight() {
		return mScreentHeight;
	}

	public WindowManager.LayoutParams getMywmParams() {
		return wmParams;
	}

	public void addActivity(String key, Activity value) {
		mActivityMap.put(key, value);
	}

	public Activity getActivity(String key) {
		return mActivityMap.get(key);
	}

	public void removeActivity(String key) {
		mActivityMap.remove(key);
	}

	public void addSendRecords(long key, List<Information> list) {
		if (sendRecords == null)
			sendRecords = new HashMap<Long, List<Information>>();
		sendRecords.put(key, list);

	}

	public List<Information> getSendRecordsList(long key) {
		if (sendRecords == null)
			return null;
		else
			return sendRecords.get(key);
	}

	public Map<Long, List<Information>> getSendRecords() {
		if (sendRecords == null)
			return null;
		else
			return sendRecords;
	}

	public String getSendFileCachePath() {
			String imagePath = "";
			File sdDir = null;
			boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
			if (sdCardExist) {
				sdDir = Environment.getExternalStorageDirectory();
				// 获取根目录
				// Logger.d(Constant.RcAdTag, "SD卡存在! ", null);
				String sdUrl = sdDir.toString()+"/temp";
				File dir = new File(sdUrl);
				if (!dir.exists())
					dir.mkdir();
				imagePath = sdUrl;
			} else {
				// Logger.d(Constant.RcAdTag, "SD卡不存在! ", null); getCacheDir()
				File file = new File(getFilesDir(), "temp");
				if (!file.exists())
					file.mkdir();
				imagePath = file.getAbsolutePath();
			}
			// Logger.d(Constant.RcAdTag, "自主广告 图片保存路径为 ：  " + imagePath, null);
			return imagePath;
		
		
		
//		if (sendFileCachePath == null || sendFileCachePath.length() <= 0) {
//			File file = new File(getFilesDir(), "temp");
//			if (!file.exists())
//				file.mkdir();
//			sendFileCachePath = file.getAbsolutePath();
//		}
//		return sendFileCachePath;
	}
	public String getBackgroundCachePath() {
		String imagePath = "";
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();
			// 获取根目录
			// Logger.d(Constant.RcAdTag, "SD卡存在! ", null);
			String sdUrl = sdDir.toString()+"/PhotoTalk";
			File dir = new File(sdUrl);
			if (!dir.exists())
				dir.mkdir();
			imagePath = sdUrl;
		} else {
			// Logger.d(Constant.RcAdTag, "SD卡不存在! ", null); getCacheDir()
			File file = new File(getFilesDir(), "PhotoTalk");
			if (!file.exists())
				file.mkdir();
			imagePath = file.getAbsolutePath();
		}
		// Logger.d(Constant.RcAdTag, "自主广告 图片保存路径为 ：  " + imagePath, null);
		return imagePath;
		}

	public void deleteSendFileCache(String fileName) {
		File file = new File(getSendFileCachePath());
		if (file.exists()) {
			File file2 = new File(file, fileName);
			if (file2.exists()) {
				file2.delete();
			}
		}
	}

	public UserInfo getCurrentUser() {
		return mService.getCurrentUser();
	}

	public void setCurrentUser(UserInfo userInfo) {
		mService.setCurrentUser(userInfo);
		PhotoTalkDatabaseFactory.open(userInfo);
	}
}
