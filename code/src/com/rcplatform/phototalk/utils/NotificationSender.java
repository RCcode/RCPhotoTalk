package com.rcplatform.phototalk.utils;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.widget.RemoteViews;

import com.rcplatform.phototalk.R;

public class NotificationSender {

	private static NotificationSender instance;
	private NotificationManager mManager;
	private static final int MSG_WHAT_CANCEL_NOTIFICATION = 1000;
	private Application context;

	private NotificationSender(Context context) {
		this.context = (Application) context.getApplicationContext();
		mManager = (NotificationManager) this.context.getSystemService(Context.NOTIFICATION_SERVICE);

	}

	public static synchronized NotificationSender getInstance(Context context) {
		if (instance == null)
			instance = new NotificationSender(context);
		return instance;
	}

	private static final Handler notificationHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == MSG_WHAT_CANCEL_NOTIFICATION) {
				((NotificationManager) msg.obj).cancel(msg.arg1);
			}
		};
	};

	public void sendNotification(String notifyTitle, String notifyText, int drawableId, Intent contentIntent, int notificationId) {
		Notification notification = new Notification();
		notification.icon = drawableId;
		notification.contentView = new RemoteViews(context.getPackageName(), R.layout.gcm_notification);
		notification.contentView.setImageViewResource(R.id.gcm_image, drawableId);
		notification.contentView.setTextViewText(R.id.gcm_title, notifyTitle);
		notification.contentView.setTextViewText(R.id.gcm_decs, notifyText);
		notification.tickerText = notifyText;
		notification.when = System.currentTimeMillis();
		notification.flags = Notification.FLAG_NO_CLEAR;
		if (contentIntent != null) {
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			notification.contentIntent = pendingIntent;
		}
		mManager.notify(notificationId, notification);
	}

	public void cancelNofitication(int notificationId) {
		mManager.cancel(notificationId);
	}

	public void cancelNotification(int notificationId, long delay) {
		Message msg = notificationHandler.obtainMessage(MSG_WHAT_CANCEL_NOTIFICATION);
		msg.arg1 = notificationId;
		msg.obj = mManager;
		notificationHandler.sendMessageDelayed(msg, delay);
	}
}
