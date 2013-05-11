package com.rcplatform.tigase;

import com.rcplatform.message.UserMessageService;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

public class TigaseRegisterSerivce extends IntentService {

	public static final String TIGASE_REGISTER_BROADCAST = "com.rcplatform.tigase.register";

	public static final String TIGASE_REGISTER_RESULT_KEY = "tigase_register";

	public TigaseRegisterSerivce(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO Auto-generated method stub
		TigaseNode node = TigaseNodeUtil.getTigaseNode();
		XmppTool.createConnection(node);

		Bundle bunde = intent.getExtras();
		String name = bunde.getString(UserMessageService.TIGASE_USER_NAME_KEY);
		String password = bunde.getString(UserMessageService.TIGASE_USER_PASSWORD_KEY);
		boolean flag = XmppTool.register(name + "@" + node.getDomain(), password);

		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(TIGASE_REGISTER_BROADCAST);
		// 要发送的内容
		broadcastIntent.putExtra(TIGASE_REGISTER_RESULT_KEY, flag);
		// 发送 一个无序广播
		this.sendBroadcast(broadcastIntent);

	}

}
