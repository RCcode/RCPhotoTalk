package com.rcplatform.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.protocol.HTTP;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gcm.MetaHelper;
import com.rcplatform.phototalk.PhotoTalkApplication;
import com.rcplatform.phototalk.bean.TigaseMassage;
import com.rcplatform.phototalk.db.impl.TigaseDb4oDatabase;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.tigase.TigaseNode;
import com.rcplatform.tigase.TigaseNodeUtil;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

public class UserMessageService extends Service {

	@Override
    public IBinder onBind(Intent intent) {
	    // TODO Auto-generated method stub
	    return null;
    }


//	private static final int MSG_WHAT_XMPP_CONNECT_SUCCESS = 101;
//
//	public static final String TIGASE_USER_NAME_KEY = "tg_id";
//
//	public static final String TIGASE_USER_PASSWORD_KEY = "tg_pwd";
//
//	public static final String MESSAGE_TO_USER = "message_to_user";
//
//	public static final String MESSAGE_FROM_USER = "message_from_user";
//
//	public static final String MESSAGE_TYPE_KEY = "message_type";
//
//	public static final String MESSAGE_CONTENT_KEY = "message_content";
//
//	public static final String MESSAGE_RECIVE_BROADCAST = "com.rcplatform.message.recive";
//
//	public static final String MESSAGE_SEND_BROADCAST = "com.rcplatform.message.send";
//
//	private static final String MESSAGE_TYPE_MESSAGE = "m";
//
//	private static final String MESSAGE_TYPE_RECEIPT = "r";
//
//	private static final String MESSAGE_SPLIT = ":";
//
//	public static final String MESSAGE_ACTION_KEY = "action";
//
//	public static final String MESSAGE_ACTION_MSG = "1";
//
//	public static final String MESSAGE_ACTION_FRIEND = "2";
//
//	public static final String MESSAGE_ACTION_SEND_MESSAGE = "3";
//
//	public static final String MESSAGE_RCID_KEY = "rcid";
//
//	private static final String GCM_URL = "http://192.168.0.86:8083/phototalk/user/pushOfflineMsg.do";
//
//	private Context ctx;
//	private HashMap<String, Timer> gcmTimers;
//
//	private boolean hasRegisteSendReceiver = false;
//
//	ChatManagerListener chatListener = new ChatManagerListener() {
//
//		@Override
//		public void chatCreated(Chat chat, boolean able) {
//			chat.addMessageListener(new MessageListener() {
//
//				@Override
//				public void processMessage(Chat chat, Message message) {
///*
//					String str = message.getBody();
//					int typeEnd = str.indexOf(MESSAGE_SPLIT);
//					String msgType = str.substring(0, typeEnd);
//					str = str.substring(typeEnd + 1);
//					int actionEnd = str.indexOf(MESSAGE_SPLIT);
//					String action = str.substring(0, actionEnd);
//					if (msgType.equals(MESSAGE_TYPE_MESSAGE)) {
//						String msgContent = str.substring(actionEnd + 1);
//						Intent intent = new Intent();
//						intent.setAction(MESSAGE_RECIVE_BROADCAST);
//						// 要发送的内容
//						intent.putExtra(MESSAGE_FROM_USER, message.getFrom());
//						intent.putExtra(MESSAGE_CONTENT_KEY, msgContent);
//						// 发送 一个无序广播
////						ctx.sendBroadcast(intent);
//
//						try {
//							chat.sendMessage(MESSAGE_TYPE_RECEIPT + MESSAGE_SPLIT + action + MESSAGE_SPLIT);
//						} catch (XMPPException e) {
//							// TODO Auto-generated catch block
//							e.printStackTrace();
//						}
//						if (action.equals(MESSAGE_ACTION_FRIEND) || action.equals(MESSAGE_ACTION_SEND_MESSAGE)) {
//							Vibrator vib = (Vibrator) ctx.getSystemService(Service.VIBRATOR_SERVICE);
//							vib.vibrate(200);
//						}
//
//					} else if (msgType.equals(MESSAGE_TYPE_RECEIPT)) {
//						// TODO 取消gcm 发送
//
//						String formUser = message.getFrom();
//						int end = formUser.indexOf("/");
//						formUser = formUser.substring(0, end);
//						String cancelKey = formUser + action;
//						Timer cancelTimer = gcmTimers.get(cancelKey);
//						if (null != cancelTimer) {
//							cancelTimer.cancel();
//							gcmTimers.remove(cancelKey);
//						}
//					}
//					
//					*/
//				}
//			});
//		}
//	};
//
//	private BroadcastReceiver sendBroadcastReceiver = new BroadcastReceiver() {
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
///*
//			Bundle extras = intent.getExtras();
//			String toUser = extras.getString(MESSAGE_TO_USER);
//			String msg = extras.getString(MESSAGE_CONTENT_KEY);
//
//			String action = extras.getString(MESSAGE_ACTION_KEY);
//			String toRcId = extras.getString(MESSAGE_RCID_KEY);
//
//			String msgStr = MESSAGE_TYPE_MESSAGE + MESSAGE_SPLIT + action + MESSAGE_SPLIT + msg;
//
//			XmppTool.sendMessageBackup(toUser, msgStr);
//			// 需要 gcm 推送的消息
//			if (action.equals(MESSAGE_ACTION_FRIEND) || action.equals(MESSAGE_ACTION_SEND_MESSAGE)) {
//				String timerKey = XmppTool.getFullUser(toUser) + action;
//				Timer timer = new Timer();
//				String type = "";
//
//				if (action.equals(MESSAGE_ACTION_SEND_MESSAGE)) {
//					type = Constants.GCM_TYPE_MSG;
//				} else if (action.equals(MESSAGE_ACTION_FRIEND)) {
//					type = Constants.GCM_TYPE_FRIEND;
//				}
//
//				GcmTask gcmTask = new GcmTask(context, type, toRcId, msg);
//				timer.schedule(gcmTask, 10000);
//				gcmTimers.put(timerKey, timer);
//			}
//			*/
//
//		}
//	};
//
//	@Override
//	public IBinder onBind(Intent intent) {
//		return null;
//	}
//
//	@Override
//	public void onStart(Intent intent, int startId) {
//		super.onStart(intent, startId);
////		createXmppConnection(intent);
//		gcmTimers = new HashMap<String, Timer>();
//	}
//
//	private void createXmppConnection(final Intent intent) {
//		Thread thread = new Thread() {
//
//			public void run() {
//				try {
//					TigaseNode node = TigaseNodeUtil.getTigaseNode();
//					XmppTool.createConnection(node);
//
//					Bundle bunde = intent.getExtras();
//					String name = bunde.getString(TIGASE_USER_NAME_KEY);
//					String password = bunde.getString(TIGASE_USER_PASSWORD_KEY);
//					XmppTool.login(name + "@" + node.getDomain(), password);
//					XmppTool.setChatManagerListener(chatListener);
//					xmppHandler.sendEmptyMessage(MSG_WHAT_XMPP_CONNECT_SUCCESS);
//				} catch (Exception e) {
//
//				}
//			};
//		};
//		thread.start();
//	}
//
//	private Handler xmppHandler = new Handler() {
//
//		public void handleMessage(android.os.Message msg) {
//			switch (msg.what) {
//
//			case MSG_WHAT_XMPP_CONNECT_SUCCESS:
//				IntentFilter intentFilter = new IntentFilter();
//				intentFilter.addAction(MESSAGE_SEND_BROADCAST);
//				registerReceiver(sendBroadcastReceiver, intentFilter);
//				hasRegisteSendReceiver = true;
//				break;
//			}
//		};
//	};
//
//	@Override
//	public void onCreate() {
//		super.onCreate();
//		ctx = this;
//	}
//
//	@Override
//	public void onDestroy() {
//		super.onDestroy();
//		XmppTool.closeConnection();
//		if (hasRegisteSendReceiver)
//			this.unregisterReceiver(sendBroadcastReceiver);
//	}
//
//	class GcmTask extends TimerTask {
//
//		private Context ctx;
//
//		private String type;
//
//		private String toRcId;
//
//		private String extra;
//
//		GcmTask(Context ctx, String type, String toRcId, String extra) {
//			this.ctx = ctx;
//			this.type = type;
//			this.toRcId = toRcId;
//			this.extra = extra;
//
//		}
//
//		@Override
//		public void run() {
//			// TODO Auto-generated method stub
//			Log.d("GcmTask", "push to service");
//
//			JSONObject json = new JSONObject();
//			try {
//				// TODO
//				json.put("appId", Constants.APP_ID);
//				json.put("type", type);
//				// TODO 设置真是token
//				PhotoTalkApplication app = (PhotoTalkApplication) ctx.getApplicationContext();
//
//				json.put("token", app.getCurrentUser().getToken());
//				json.put("fRcId", toRcId);
//				json.put("deviceId", MetaHelper.getMACAddress(ctx));
//				json.put("rcId", app.getCurrentUser().getRcId());
//				json.put("language", "");
//				json.put("extra", extra);
//				/*
//				 * json.put("packageName", MetaHelper.getAppName(context));
//				 * json.put("status", STATUS_CREATE_USERINFO);
//				 * json.put("deviceID", MetaHelper.getImsi(context));
//				 * json.put("clientMac", MetaHelper.getMACAddress(context));
//				 * json.put("osVersion", MetaHelper.getOsVersion(context));
//				 * json.put("language", MetaHelper.getLanguage(context));
//				 * json.put("timeZone", MetaHelper.getTimeZone(context));
//				 * json.put("timeZoneID", MetaHelper.getTimeZoneId(context));
//				 */
//			}
//			catch (JSONException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//
//			OutputStream output = null;
//			InputStream is = null;
//			HttpURLConnection conn = null;
//			String content = null;
//			try {
//				conn = (HttpURLConnection) new URL(GCM_URL).openConnection();
//				conn.setConnectTimeout(5000);
//				conn.setReadTimeout(5000);
//				conn.setDoOutput(true);
//				conn.setRequestMethod("POST");
//				conn.setRequestProperty("Content-Type", "application/json");
//				byte[] bodyBytes = json.toString().getBytes(HTTP.UTF_8);
//				output = conn.getOutputStream();
//				output.write(bodyBytes);
//				is = conn.getInputStream();
//				InputStreamReader reader = new InputStreamReader(is, "UTF-8");
//				StringBuilder builder = new StringBuilder();
//				char[] readChars = new char[1024];
//				String temp = null;
//				int result = -1;
//				while ((result = reader.read(readChars, 0, 1024)) != -1) {
//					temp = new String(readChars, 0, result);
//					builder.append(temp);
//				}
//				reader.close();
//				content = builder.toString();
//			} catch (ConnectException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} finally {
//				try {
//					output.close();
//				} catch (Exception e) {
//				}
//				try {
//					is.close();
//				} catch (Exception e) {
//				}
//				if (conn != null)
//					conn.disconnect();
//
//			}
//
//		}
//	}
}
