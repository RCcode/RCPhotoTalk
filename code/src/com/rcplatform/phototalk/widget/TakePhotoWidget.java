package com.rcplatform.phototalk.widget;

import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.utils.PhotoTalkUtils;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.widget.RemoteViews;

public class TakePhotoWidget extends AppWidgetProvider {
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.take_photo_widget);
		PendingIntent intent = PendingIntent.getActivity(context, 0, PhotoTalkUtils.getNotificationTakePhotoIntent(context), PendingIntent.FLAG_UPDATE_CURRENT);
		remoteView.setOnClickPendingIntent(R.id.ib_take_widget, intent);
		appWidgetManager.updateAppWidget(new ComponentName(context, TakePhotoWidget.class), remoteView);
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}
}
