package com.rcplatform.phototalk.utils;

import android.app.Notification;
import android.content.Context;

public class NotificationSender {
	
	public static void sendNotification(Context context, String nofityText, int drawableId,int notificationId) {
		Notification notification=new Notification();
		notification.icon=drawableId;
		notification.flags=Notification.FLAG_NO_CLEAR;
	}
}
