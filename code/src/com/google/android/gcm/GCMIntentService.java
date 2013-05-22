/*
 * Copyright 2012 Google Inc. Licensed under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package com.google.android.gcm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.RemoteViews;

import com.google.android.gcm.GCMBaseIntentService;
import com.rcplatform.message.UserMessageService;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.WelcomeActivity;
import com.rcplatform.phototalk.utils.Constants;

/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {

	@SuppressWarnings("hiding")
	private static final String TAG = "GCMIntentService";

	public static String getFileName(String imageUrl) {
		String fileName = "";
		if (imageUrl != null && imageUrl.length() != 0) {
			fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
		}
		return fileName;
	}

	public static String imageSaveUri(Context context, String imageUrl) {
		String imagePath = "";
		String fileName = getFileName(imageUrl);
		// 获取url中图片的文件名与后缀
		// 图片保存路径地址
		File sdDir = null;
		boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED); // 判断sd卡是否存在
		if (sdCardExist) {
			sdDir = Environment.getExternalStorageDirectory();
			// 获取根目录
			// Logger.d(Constant.AdlayoutTag, "SD卡存在! ", null);
			String sdUrl = sdDir.toString() + "/cacheImageTmp";
			File dir = new File(sdUrl);
			if (!dir.exists())
				dir.mkdir();
			imagePath = sdUrl + "/" + fileName;
		} else {
			// Logger.d(Constant.AdlayoutTag, "SD卡不存在! ", null);
			imagePath = context.getCacheDir() + "/" + fileName;
		}
		// Logger.d(Constant.AdlayoutTag, "自主广告 图片保存路径为 ：  " + imagePath, null);
		return imagePath;
	}

	public static File getFile(Context context, String fileName) {
		File file = null;
		try {
			String url = imageSaveUri(context, fileName);
			file = new File(url);
		}
		catch (Exception ex) {
		}
		return file;
	}

	public static Bitmap loadImageFromUrl(Context context, String imageUrl) {
		if (imageUrl == null)
			return null;
		String fileName = getFileName(imageUrl);
		Bitmap bm = null;
		FileOutputStream fos = null;
		HttpURLConnection conn = null;
		File file = getFile(context, fileName);
		if (!file.exists() && !file.isDirectory()) {
			try {
				fos = new FileOutputStream(file);
				conn = (HttpURLConnection) new URL(imageUrl).openConnection();
				int lentegth = conn.getContentLength();
				conn.setConnectTimeout(5000);
				conn.setReadTimeout(5000);
				// conn.setRequestMethod("GET");
				// InputStream is = new URL(imageUrl).openStream();
				InputStream is = conn.getInputStream();
				// InputStream is = new URL(imageUrl).openStream();
				int data = is.read();
				while (data != -1) {
					fos.write(data);
					data = is.read();
				}
				fos.close();
				is.close();
				// 判断是否完整下载 否则为下载失败
				if (lentegth == file.length()) {
					bm = BitmapFactory.decodeFile(file.toString());
				} else {
					file.delete();
					return null;
				}
			}
			catch (IOException e) {
				return null;
			}
		} else {
			bm = BitmapFactory.decodeFile(file.toString());
		}
		return bm;
	}

	private boolean checkApkExist(Context context, String packageName) {
		if (packageName == null || "".equals(packageName)) {
			return false;
		}
		try {
			context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
			return true;
		}
		catch (NameNotFoundException e) {
			return false;
		}
	}

	public GCMIntentService() {
		super(ServerUtilities.SENDER_ID);
	}

	@Override
	protected void onRegistered(Context context, String registrationId) {
		Log.i(TAG, "Device registered: regId = " + registrationId);
		ServerUtilities.registerUserID(context, registrationId);
	}

	@Override
	protected void onUnregistered(Context context, String registrationId) {
		Log.i(TAG, "Device unregistered");
		ServerUtilities.unregister(context, registrationId);
	}

	@Override
	protected void onMessage(Context context, Intent intent) {
		Log.i(TAG, "Received message. Extras: " + intent.getExtras());

		int count = 0;
		
		
		String typeStr = intent.getStringExtra("type");
		String msg = intent.getStringExtra("extra");
		int type = Integer.parseInt(typeStr);
		if(1== type || 2 == type){
			Intent it = new Intent();
			it.setAction( Constants.Action.ACTION_GCM_MESSAGE);
			it.putExtra(UserMessageService.MESSAGE_CONTENT_KEY, msg);
			context.sendBroadcast(it);
		}
		switch (type) {
			case 1:
				count = ServerUtilities.getGcmMessageCount(context, ServerUtilities.GCM_MSG_USER_MESSAGE)+1;
				ServerUtilities.setGcmMessageCount(context, ServerUtilities.GCM_MSG_USER_MESSAGE, count);
				generateMessageNotification(context,context.getString(R.string.gcm_message,count).toString());

				break;
			case 2:
				generateMessageNotification(context,context.getText(R.string.gcm_friend).toString());
				break;
			case 3:
				String iconUrl = intent.getStringExtra("icon");
				String titleStr = intent.getStringExtra("title");
				String descStr = intent.getStringExtra("desc");
				String downloadUrl = intent.getStringExtra("url");
				String packageName = intent.getStringExtra("package");
				String pushIdStr = intent.getStringExtra("pushID");
				String pushResult = "";

				if (!checkApkExist(context, packageName)) {
					pushResult = ServerUtilities.STATUS_RECIVIE_MSG_NEW_APP;
					generateNotification(context, iconUrl, titleStr, descStr, downloadUrl);
				} else {
					pushResult = ServerUtilities.STATUS_RECIVIE_MSG_INSTALLED;
				}
				ServerUtilities.logPushResult(context, pushIdStr, pushResult);
				break;
		}

	}

	@Override
	protected void onDeletedMessages(Context context, int total) {
		Log.i(TAG, "Received deleted messages notification");
		// notifies user
	}

	@Override
	public void onError(Context context, String errorId) {
		Log.i(TAG, "Received error: " + errorId);
	}

	@Override
	protected boolean onRecoverableError(Context context, String errorId) {
		// log message
		Log.i(TAG, "Received recoverable error: " + errorId);
		return super.onRecoverableError(context, errorId);
	}

	/**
	 * Issues a notification to inform the user that server has sent a message.
	 */
	private static void generateNotification(Context context, String iconUrl, String titleStr, String descStr, String downloadUrl) {
		try {
			downloadUrl.trim();
			Bitmap bp = loadImageFromUrl(context, iconUrl);
			/*
			 * // Notification notification = new Notification(); Notification
			 * notification = new Notification(); notification.icon =
			 * R.drawable.ic_stat_gcm; notification.contentView = new
			 * RemoteViews(context.getPackageName(), R.layout.gcm_notification);
			 * notification.contentView.setImageViewBitmap(R.id.gcm_image, bp);
			 * notification.contentView.setTextViewText(R.id.gcm_title,
			 * titleStr);
			 * notification.contentView.setTextViewText(R.id.gcm_decs, descStr);
			 * notification.when = System.currentTimeMillis();
			 * notification.defaults |= Notification.DEFAULT_SOUND;
			 * NotificationManager notificationManager = (NotificationManager)
			 * context.getSystemService(Context.NOTIFICATION_SERVICE); Uri uri =
			 * Uri.parse(downloadUrl); Intent notificationIntent = new
			 * Intent(Intent.ACTION_VIEW, uri); // set intent so it does not
			 * start a new activity PendingIntent intent =
			 * PendingIntent.getActivity(context, 0, notificationIntent, 0);
			 * notification.contentIntent = intent;
			 * notificationManager.notify(0, notification);
			 */
		}
		catch (Exception e) {

		}

	}

	/**
	 * Issues a notification to inform the user that server has sent a message.
	 */
	private static void generateMessageNotification(Context context, String msg) {
		try {

			// Notification notification = new Notification();
			Notification notification = new Notification();
			notification.icon = R.drawable.ic_launcher;

			notification.contentView = new RemoteViews(context.getPackageName(), R.layout.gcm_notification);
			notification.contentView.setImageViewResource(R.id.gcm_image, R.drawable.ic_launcher);
			notification.contentView.setTextViewText(R.id.gcm_title, context.getText(R.string.app_name));
			notification.contentView.setTextViewText(R.id.gcm_decs, msg);
			notification.when = System.currentTimeMillis();
			notification.defaults |= Notification.DEFAULT_SOUND;
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);;
			Intent notificationIntent = new Intent(context, WelcomeActivity.class);
			// set intent so it does not start a new activity
			PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
			notification.contentIntent = intent;
			notificationManager.notify(0, notification);
		}
		catch (Exception e) {

		}

	}

}
