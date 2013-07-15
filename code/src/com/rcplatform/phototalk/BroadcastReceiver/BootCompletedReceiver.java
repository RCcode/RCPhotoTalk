package com.rcplatform.phototalk.BroadcastReceiver;

import com.rcplatform.phototalk.WelcomeActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootCompletedReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		// 开机启动服务请判断是否登录状态
		Log.d("boot complete", "service to start...");
		// Intent newIntent = new Intent(context, WelcomeActivity.class);
		// newIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //
		// 注意，必须添加这个标记，否则启动会失败
		// context.startActivity(newIntent);
	}
}