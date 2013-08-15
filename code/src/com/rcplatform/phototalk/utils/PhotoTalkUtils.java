package com.rcplatform.phototalk.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RemoteViews;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.WelcomeActivity;
import com.rcplatform.phototalk.bean.AppInfo;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.galhttprequest.MD5;
import com.rcplatform.phototalk.umeng.EventUtil;

public class PhotoTalkUtils {

	public static String getSexString(Context context, int sex) {
		String result = null;
		switch (sex) {
		case 0:
			result = context.getString(R.string.sex_secret);
			break;
		case 1:
			result = context.getString(R.string.male);
			break;
		case 2:
			result = context.getString(R.string.famale);
			break;
		}
		return result;
	}

	public static String getFilePath(String url) {
		StringBuilder sbPath = new StringBuilder();
		sbPath.append(Constants.PhotoInformationCache.FILE_PATH).append("/").append(MD5.encodeMD5String(url));
		return sbPath.toString();
	}

	public static String getUnZipDirPath(String url) {
		return getFilePath(url) + Constants.PhotoInformationCache.UNZIP_SUFFIX;
	}

	public static String getInformationTagBase(Information information) {
		return information.getReceiver().getRcId() + "|" + information.getSender().getRcId() + "|" + information.getCreatetime();
	}

	public static Friend userToFriend(UserInfo userInfo) {
		Friend friend = new Friend();
		friend.setRcId(userInfo.getRcId());
		friend.setBirthday(userInfo.getBirthday());
		friend.setGender(userInfo.getGender());
		friend.setAppList(new ArrayList<AppInfo>(Constants.userApps.keySet()));
		friend.setCellPhone(userInfo.getCellPhone());
		friend.setHeadUrl(userInfo.getHeadUrl());
		friend.setBackground(userInfo.getBackground());
		friend.setFriend(true);
		friend.setNickName(userInfo.getNickName());
		friend.setLetter(RCPlatformTextUtil.getLetter(userInfo.getNickName()));
		return friend;
	}

	public static void buildAppList(Context context, LinearLayout linearApps, List<AppInfo> apps, ImageLoader loader) {
		for (AppInfo info : apps) {
			if (info.getAppPackage().equals("com.rcplatform.phototalk"))
				continue;
			linearApps.addView(getAppImage(context, info, loader));
		}
		if (linearApps.getChildCount() == 0)
			linearApps.setVisibility(View.GONE);
	}

	private static ImageView getAppImage(final Context context, final AppInfo appInfo, ImageLoader loader) {
		ImageView iv = new ImageView(context);
		int width = context.getResources().getDimensionPixelSize(R.dimen.app_icon_width);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, width);
		params.setMargins(0, 0, context.getResources().getDimensionPixelSize(R.dimen.app_icon_margin), 0);
		iv.setLayoutParams(params);
		iv.setScaleType(ScaleType.FIT_CENTER);

		if (Utils.checkApkExist(context, appInfo.getAppPackage())) {
			loader.displayImage(appInfo.getColorPicUrl(), iv);
			iv.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					EventUtil.Friends_Addfriends.rcpt_profile_rcapp(context);
					Utils.startApplicationByPackage(context, appInfo.getAppPackage());
				}
			});
		} else {
			loader.displayImage(appInfo.getGrayPicUrl(), iv);
			iv.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Utils.searchAppInGooglePlay(context, appInfo.getAppPackage());
				}
			});
		}

		return iv;
	}

	public static boolean isUserNeedToBindPhone(Context context, UserInfo userInfo) {
		if (context == null)
			return false;
		if (Constants.DEVICE_ID != null) {
			if (userInfo != null)
				return RCPlatformTextUtil.isEmpty(userInfo.getCellPhone()) && Constants.DEVICE_ID.equals(userInfo.getDeviceId())
						&& !PrefsUtils.User.MobilePhoneBind.isUserBindPhoneTimeOut(context, userInfo.getRcId());
		}
		return false;
	}

	public static UserInfo copyUserInfo(UserInfo userInfo) {
		UserInfo userCopy = new UserInfo();
		userCopy.clone(userInfo);
		return userCopy;
	}

	public static void sendNewInformationNotification(Context context, int count) {
		Notification notification = new Notification();
		notification.icon = R.drawable.ic_launcher;
		notification.contentView = new RemoteViews(context.getPackageName(), R.layout.gcm_notification);
		notification.contentView.setImageViewResource(R.id.gcm_image, R.drawable.ic_launcher);
		notification.contentView.setTextViewText(R.id.gcm_title, context.getString(R.string.app_name));
		notification.contentView.setTextViewText(R.id.gcm_decs, context.getString(R.string.gcm_message, count));
		notification.when = System.currentTimeMillis();
		notification.defaults |= Notification.DEFAULT_SOUND;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Intent notificationIntent = new Intent(context, WelcomeActivity.class);
		PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
		notification.contentIntent = intent;
		notificationManager.notify(0, notification);
	}

	public static Friend getDriftFriend() {
		Friend friend = new Friend();
		friend.setRcId("-1");
		return friend;
	}

	public static void showCommentAttentionDialog(final Context context) {
		AlertDialog dialog = DialogUtil.getAlertDialogBuilder(context).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Utils.searchAppInGooglePlay(context, context.getPackageName());
			}
		}).setTitle(R.string.comment_title).setNegativeButton(R.string.cancel, null).setMessage(R.string.comment_message).create();
		dialog.show();
	}

	public static void showMustUpdateDialog(final Activity context, boolean isQuit) {
		AlertDialog mUpdateDialog = DialogUtil.getAlertDialogBuilder(context).setMessage(PrefsUtils.AppInfo.getUpdateDesc(context))
				.setTitle(context.getString(R.string.update_dialog_title)).setPositiveButton(R.string.update_now, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Utils.setDialogNotAutoDismiss(dialog);
						Utils.searchAppInGooglePlay(context, context.getPackageName());
					}
				}).create();
		mUpdateDialog.setCancelable(false);
		if (isQuit)
			mUpdateDialog.setOnKeyListener(new OnKeyListener() {

				@Override
				public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_BACK) {
						context.finish();
						return true;
					}
					return false;
				}
			});
		mUpdateDialog.show();
	}

	public static Intent getNotificationTakePhotoIntent(Context context) {
		Intent intent = new Intent(context, WelcomeActivity.class);
		intent.putExtra(Constants.ApplicationStartMode.APPLICATION_START_KEY, Constants.ApplicationStartMode.APPLICATION_START_TAKE_PHOTO);
		return intent;
	}
	
	public static Intent getNotificationDriftInformationIntent(Context context){
		Intent intent = new Intent(context, WelcomeActivity.class);
		intent.putExtra(Constants.ApplicationStartMode.APPLICATION_START_KEY, Constants.ApplicationStartMode.APPLICATION_START_DRIFT);
		return intent;
	}
	public static Intent getNotificationNormalIntent(Context context){
		Intent intent = new Intent(context, WelcomeActivity.class);
		return intent;
	}

	public static File getSavedVideoDir() {
		if (Constants.PhotoInformationCache.SAVED_VIDEO_DIR == null) {
			Constants.initSavedDirs();
		}
		return Constants.PhotoInformationCache.SAVED_VIDEO_DIR;
	}

	public static File getSavedPhotoDir() {
		if (Constants.PhotoInformationCache.SAVED_PHOTO_DIR == null) {
			Constants.initSavedDirs();
		}
		return Constants.PhotoInformationCache.SAVED_PHOTO_DIR;
	}
}
