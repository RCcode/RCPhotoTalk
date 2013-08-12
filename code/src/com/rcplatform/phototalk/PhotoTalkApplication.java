package com.rcplatform.phototalk;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.WindowManager;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.clienservice.PTBackgroundService;
import com.rcplatform.phototalk.clienservice.PhotoTalkWebService;
import com.rcplatform.phototalk.db.PhotoTalkDatabase;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.galhttprequest.LogUtil;
import com.rcplatform.phototalk.image.downloader.ImageOptionsFactory;
import com.rcplatform.phototalk.logic.MessageSender;
import com.rcplatform.phototalk.logic.PhotoInformationCountDownService;
import com.rcplatform.phototalk.logic.controller.InformationPageController;
import com.rcplatform.phototalk.request.JSONConver;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.FacebookUtil;
import com.rcplatform.phototalk.utils.PhotoTalkUtils;
import com.rcplatform.phototalk.utils.PrefsUtils;
import com.rcplatform.phototalk.utils.Utils;
import com.rcplatform.tigase.TigaseMessageBinderService;
import com.rcplatform.tigase.TigaseMessageBinderService.LocalBinder;
import com.rcplatform.tigase.TigaseMessageReceiver;

public class PhotoTalkApplication extends Application {

	private WindowManager.LayoutParams wmParams;

	protected static final int NEW_INFORMATION = 4321;

	private Bitmap editeBitmap;

	public File cacheDir;

	private final Map<String, Activity> mActivityMap = new HashMap<String, Activity>();

	private boolean isBindTigaseService = false;

	@Override
	public void onCreate() {
		super.onCreate();
		startService(new Intent(this, PhotoTalkWebService.class));
		startService(new Intent(this, PTBackgroundService.class));
		PhotoInformationCountDownService.getInstance().setApplication(this);
		ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(getApplicationContext()).threadPriority(Thread.NORM_PRIORITY - 2)
				.defaultDisplayImageOptions(ImageOptionsFactory.getDefaultImageOptions()).tasksProcessingOrder(QueueProcessingType.LIFO)
				.discCacheFileNameGenerator(new Md5FileNameGenerator());
		ImageLoaderConfiguration config = builder.build();
		ImageLoader.getInstance().init(config);
		wmParams = new WindowManager.LayoutParams();
		Constants.init(this);
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

	public String getSendFileCachePath() {
//		String imagePath = "";
//		String sdUrl =getFilesDir().getPath()+"/temp";
//		File file = new File(sdUrl);
//		if (!file.exists()) {
//			file.mkdir();
//		}
//		imagePath = file.getAbsolutePath();
//		return imagePath;
		if (Utils.isExternalStorageUsable()) {
			return Constants.PhotoInformationCache.SEND_CACHE.getPath();
		}
		return null;
	}

	public String getSendZipFileCachePath() {
//		String imagePath = "";
//		String sdUrl = getFilesDir().getPath()+"/zip";
//		File file = new File(sdUrl);
//		if (!file.exists()) {
//			file.mkdir();
//		}
//		imagePath = file.getPath();
//		return imagePath;
		if (Utils.isExternalStorageUsable()) {
			return Constants.PhotoInformationCache.ZIP_CACHE.getPath();
		}
		return null;
	}

	public String getCameraPath() {
		String imagePath = "";
		boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
		if (sdCardExist) {
			// 获取根目录
			imagePath = "/sdcard/DCIM/Camera/photoTalk_" + System.currentTimeMillis() + ".jpg";
		} else {
			imagePath = "/stystem/DCIM/Camera/photoTalk_" + System.currentTimeMillis() + ".jpg";
		}
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
		if (!userInfo.getRcId().equals(PrefsUtils.LoginState.getLastRcId(this))) {
			FacebookUtil.clearFacebookVlidated(this);
		}
		UserInfo lastUser = getCurrentUser();
		boolean isUserChange = (lastUser == null || (!userInfo.getRcId().equals(lastUser.getRcId())));
		if (isUserChange) {
			PhotoTalkDatabaseFactory.open(userInfo);
		}
		mService.setCurrentUser(userInfo);
		if (isUserChange) {
			reBindTigaseService();
		}
		lastUser = null;
	}

	private void reBindTigaseService() {
		unBindTigaseService();
		bindTigaseService();
	}

	private void bindTigaseService() {
		Intent service = new Intent(this, TigaseMessageBinderService.class);
		bindService(service, tigaseServiceConnection, Context.BIND_AUTO_CREATE);
		isBindTigaseService = true;
	}

	private void unBindTigaseService() {
		if (isBindTigaseService) {
			unbindService(tigaseServiceConnection);
			isBindTigaseService = false;
		}
	}

	private ServiceConnection tigaseServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LogUtil.e("tigase service bind ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~ ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			LocalBinder binder = (LocalBinder) service;
			binder.getService().setOnMessageReciver(new TigaseMessageReceiver() {

				@Override
				public boolean onMessageHandle(String msg, String from) {
					LogUtil.e("tigase receive message " + msg);
					List<Information> informations = JSONConver.jsonToInformations(msg);
					filteInformations(informations);
					return false;
				}
			});
			binder.getService().tigaseLogin(getCurrentUser().getTigaseId(), getCurrentUser().getTigasePwd());
			MessageSender.getInstance().setTigaseService(binder.getService());
		}
	};

	private void filteInformations(final List<Information> infos) {
		Thread th = new Thread() {
			public void run() {
				Map<Integer, List<Information>> result = PhotoTalkDatabaseFactory.getDatabase().filterNewInformations(infos, getCurrentUser());
				List<Information> newInformations = result.get(PhotoTalkDatabase.NEW_INFORMATION);
				if (newInformations != null && newInformations.size() > 0) {
					Message msg = newInformationHandler.obtainMessage();
					msg.what = NEW_INFORMATION;
					msg.obj = newInformations.size();
					newInformationHandler.sendMessage(msg);
				}
				InformationPageController.getInstance().onNewInformation(result);
			};
		};
		th.start();
	}

	private final Handler newInformationHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			int count = (Integer) msg.obj;
			PhotoTalkUtils.sendNewInformationNotification(PhotoTalkApplication.this, count);
		};
	};
}
