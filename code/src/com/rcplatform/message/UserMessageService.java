package com.rcplatform.message;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.packet.Message;

import com.rcplatform.tigase.TigaseNode;
import com.rcplatform.tigase.TigaseNodeUtil;
import com.rcplatform.tigase.XmppTool;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;

public class UserMessageService extends Service {

	private static final int MSG_WHAT_XMPP_CONNECT_SUCCESS = 101;

	public static final String TIGASE_USER_NAME_KEY = "tg_id";

	public static final String TIGASE_USER_PASSWORD_KEY = "tg_pwd";

	public static final String MESSAGE_TO_USER = "message_to_user";

	public static final String MESSAGE_FROM_USER = "message_from_user";

	public static final String MESSAGE_TYPE_KEY = "message_type";

	public static final String MESSAGE_CONTENT_KEY = "message_content";

	public static final String MESSAGE_RECIVE_BROADCAST = "com.rcplatform.message.recive";

	public static final String MESSAGE_SEND_BROADCAST = "com.rcplatform.message.send";

	private Context ctx;

	ChatManagerListener chatListener = new ChatManagerListener() {

		@Override
		public void chatCreated(Chat chat, boolean able) {
			chat.addMessageListener(new MessageListener() {

				@Override
				public void processMessage(Chat chat, Message message) {
					Intent intent = new Intent();
					intent.setAction(MESSAGE_RECIVE_BROADCAST);
					// 要发送的内容
					intent.putExtra(MESSAGE_FROM_USER, message.getFrom());
					intent.putExtra(MESSAGE_CONTENT_KEY, message.getBody());
					// 发送 一个无序广播
					ctx.sendBroadcast(intent);
				}
			});
		}
	};

	private BroadcastReceiver sendBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			Bundle extras = intent.getExtras();
			String toUser = extras.getString(MESSAGE_TO_USER);
			String msg = extras.getString(MESSAGE_CONTENT_KEY);

			XmppTool.sendMessage(toUser, msg);

		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		createXmppConnection(intent);
	}

	private void createXmppConnection(final Intent intent) {
		Thread thread = new Thread() {
			public void run() {
				TigaseNode node = TigaseNodeUtil.getTigaseNode();
				XmppTool.createConnection(node);

				Bundle bunde = intent.getExtras();
				String name = bunde.getString(TIGASE_USER_NAME_KEY);
				String password = bunde.getString(TIGASE_USER_PASSWORD_KEY);
				XmppTool.login(name + "@" + node.getDomain(), password);
				XmppTool.setChatManagerListener(chatListener);
				xmppHandler.sendEmptyMessage(MSG_WHAT_XMPP_CONNECT_SUCCESS);
			};
		};
		thread.start();
	}

	private Handler xmppHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_WHAT_XMPP_CONNECT_SUCCESS:
				IntentFilter intentFilter = new IntentFilter();
				intentFilter.addAction(MESSAGE_SEND_BROADCAST);
				registerReceiver(sendBroadcastReceiver, intentFilter);
				break;
			}
		};
	};

	@Override
	public void onCreate() {
		super.onCreate();
		ctx = this;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		XmppTool.closeConnection();
		this.unregisterReceiver(sendBroadcastReceiver);
	}
}