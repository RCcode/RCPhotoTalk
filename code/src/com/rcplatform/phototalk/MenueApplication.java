package com.rcplatform.phototalk;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Application;
import android.graphics.Bitmap;
import android.os.Environment;
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
import com.rcplatform.phototalk.clienservice.PhotoTalkWebService;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.image.downloader.ImageOptionsFactory;
import com.rcplatform.phototalk.logic.PhotoInformationCountDownService;
import com.rcplatform.phototalk.utils.Constants;

public class MenueApplication extends Application {

	private WindowManager.LayoutParams wmParams;

	private static final int MEMORY_CACHE_SIZE = 2 * 1024 * 1024;

	private static final String CACHE_FILE_PATH = "menue/cache/photochat";

	private static final int THREAD_COUNT = 3;

	private Bitmap editeBitmap;

	public File cacheDir;

	public Map<Long, List<Information>> sendRecords;

	private final Map<String, Activity> mActivityMap = new HashMap<String, Activity>();

	@Override
	public void onCreate() {
		super.onCreate();
		Constants.START_COMPLETE=false;
		PhotoInformationCountDownService.getInstance().setApplication(this);
		ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(
				getApplicationContext())
				.memoryCache(new WeakMemoryCache())
				.threadPriority(THREAD_COUNT)
				.memoryCacheSize(MEMORY_CACHE_SIZE)
				.denyCacheImageMultipleSizesInMemory()
				.imageDownloader(new BaseImageDownloader(this))
				.defaultDisplayImageOptions(
						ImageOptionsFactory.getDefaultImageOptions())
				.tasksProcessingOrder(QueueProcessingType.LIFO);
		if (createImageCacheDir()) {
			builder.discCache(new UnlimitedDiscCache(cacheDir,
					new Md5FileNameGenerator()));
		}
		ImageLoaderConfiguration config = builder.build();
		ImageLoader.getInstance().init(config);

		wmParams = new WindowManager.LayoutParams();
	}

	private boolean createImageCacheDir() {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			cacheDir = new File(Environment.getExternalStorageDirectory(),
					CACHE_FILE_PATH);
			if (!cacheDir.exists()) {
				return cacheDir.mkdirs();
			}
			return true;
		}
		return false;

	}

	private PTBackgroundService mService;
	private PhotoTalkWebService mWebService;

	public void setService(PTBackgroundService service) {
		this.mService = service;
	}

	public PTBackgroundService getService() {
		return mService;
	}

	public PhotoTalkWebService getWebService() {
		return mWebService;
	}

	public void setWebService(PhotoTalkWebService webService) {
		this.mWebService = webService;
	}

	public String getCacheFilePath() {
		return cacheDir.getAbsolutePath();
	}

	@Override
	public void onLowMemory() {
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
		return Constants.SCREEN_WIDTH;
	}

	public int getScreentHeight() {
		return Constants.SCREEN_HEIGHT;
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
//		File sdDir = null;
//		boolean sdCardExist = Environment.getExternalStorageState().equals(
//				android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
//		if (sdCardExist) {
//			sdDir = Environment.getExternalStorageDirectory();
//			// 获取根目录
//			// Logger.d(Constant.RcAdTag, "SD卡存在! ", null);
//			String sdUrl = sdDir.toString() + "/rcplatform/phototalk/temp";
//			File dir = new File(sdUrl);
//			if (!dir.exists())
//				dir.mkdir();
//			imagePath = sdUrl;
//		} else {
		String sdUrl = getCacheDir() + "/temp";
			File file = new File(sdUrl);
			if (!file.exists()) {
				file.mkdir();
			}
			imagePath = file.getPath();
//		}
		return imagePath;
	}

	public String getSendZipFileCachePath() {
		String imagePath = "";
//		File sdDir = null;
//		boolean sdCardExist = Environment.getExternalStorageState().equals(
//				android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
//		if (sdCardExist) {
//			sdDir = Environment.getExternalStorageDirectory();
//			// 获取根目录
//			// Logger.d(Constant.RcAdTag, "SD卡存在! ", null);
//			String sdUrl = sdDir.toString() + "/rcplatform/phototalk/zip";
//			File dir = new File(sdUrl);
//			if (!dir.exists())
//				dir.mkdir();
//			imagePath = sdUrl;
//		} else {
			String sdUrl = getCacheDir() + "/zip";
			File file = new File(sdUrl);
			if (!file.exists()) {
				file.mkdir();
			}
			imagePath = file.getPath();
//		}
		return imagePath;
	}

	public String getBackgroundCachePath() {
		String imagePath = "";
//		File sdDir = null;
//		boolean sdCardExist = Environment.getExternalStorageState().equals(
//				android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
//		if (sdCardExist) {
//			sdDir = Environment.getExternalStorageDirectory();
//			String sdUrl = sdDir.toString() + "/PhotoTalk";
//			File dir = new File(sdUrl);
//			if (!dir.exists())
//				dir.mkdir();
//			imagePath = sdUrl;
//		} else {
			// Logger.d(Constant.RcAdTag, "SD卡不存在! ", null); getCacheDir()
			String sdUrl = getCacheDir().toString() + "/PhotoTalk";
			File file = new File(sdUrl);
			if (!file.exists())
				file.mkdir();
			imagePath = file.getAbsolutePath();
//		}
		// Logger.d(Constant.RcAdTag, "自主广告 图片保存路径为 ：  " + imagePath, null);
		return imagePath;
	}

	public void deleteSendFileCache(String fileName) {
		if (fileName != null) {
			File file = new File(getSendFileCachePath());
			if (file.exists()) {
				File file2 = new File(file, fileName);
				if (file2.exists()) {
					file2.delete();
				}
			}
		}
	}

	public UserInfo getCurrentUser() {
		if (mService != null)
			return mService.getCurrentUser();
		return null;
	}

	public void setCurrentUser(UserInfo userInfo) {
		PhotoTalkDatabaseFactory.open(userInfo);
		mService.setCurrentUser(userInfo);
	}

}
