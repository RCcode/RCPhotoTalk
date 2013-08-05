package com.rcplatform.phototalk.utils;

import com.rcplatform.phototalk.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class NotificationSender {

	public static void sendNotification(Context context, String notifyTitle, String notifyText, int drawableId, Intent contentIntent, int notificationId) {
		Notification notification = new Notification();
		notification.icon = drawableId;
		notification.contentView = new RemoteViews(context.getPackageName(), R.layout.gcm_notification);
		notification.contentView.setImageViewResource(R.id.gcm_image, drawableId);
		notification.contentView.setTextViewText(R.id.gcm_title, notifyTitle);
		notification.contentView.setTextViewText(R.id.gcm_decs, notifyText);
		notification.tickerText=notifyText;
		notification.when = System.currentTimeMillis();
		notification.flags = Notification.FLAG_AUTO_CANCEL;
		if (contentIntent != null) {
			PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			notification.contentIntent = pendingIntent;
		}
		NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		manager.notify(notificationId, notification);
	}
}
