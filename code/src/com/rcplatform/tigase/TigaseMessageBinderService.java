package com.rcplatform.tigase;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
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
import com.rcplatform.phototalk.HomeActivity;
import com.rcplatform.phototalk.PhotoTalkApplication;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.Constants.Action;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Vibrator;
import android.util.Log;

public class TigaseMessageBinderService extends Service {

	private static final String MESSAGE_TYPE_MESSAGE = "m";

	private static final String MESSAGE_TYPE_RECEIPT = "r";

	private static final String MESSAGE_SPLIT = ":";

	public static final String MESSAGE_ACTION_MSG = "1";

	public static final String MESSAGE_ACTION_FRIEND = "2";

	public static final String MESSAGE_ACTION_SEND_MESSAGE = "3";

	private static final String GCM_URL = "http://192.168.0.86:8083/phototalk/user/pushOfflineMsg.do";

	// Binder given to clients
	private final IBinder mBinder = new LocalBinder();

	private Context ctx;

	private TigaseMessageReceiver messageRecevier = null;

	private HashMap<String, Timer> gcmTimers = new HashMap<String, Timer>();

	ChatManagerListener chatListener = new ChatManagerListener() {

		@Override
		public void chatCreated(Chat chat, boolean able) {
			chat.addMessageListener(new MessageListener() {

				@Override
				public void processMessage(Chat chat, Message message) {

					String str = message.getBody();
					int typeEnd = str.indexOf(MESSAGE_SPLIT);
					String msgType = str.substring(0, typeEnd);
					str = str.substring(typeEnd + 1);
					int actionEnd = str.indexOf(MESSAGE_SPLIT);
					String action = str.substring(0, actionEnd);
					if (msgType.equals(MESSAGE_TYPE_MESSAGE)) {
						String msgContent = str.substring(actionEnd + 1);
						messageRecevier.onMessageHandle(msgContent, message.getFrom());
						try {
							chat.sendMessage(MESSAGE_TYPE_RECEIPT + MESSAGE_SPLIT + action + MESSAGE_SPLIT);
						}
						catch (XMPPException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if (action.equals(MESSAGE_ACTION_FRIEND) || action.equals(MESSAGE_ACTION_SEND_MESSAGE)) {
							Vibrator vib = (Vibrator) ctx.getSystemService(Service.VIBRATOR_SERVICE);
							vib.vibrate(200);
						}

					} else if (msgType.equals(MESSAGE_TYPE_RECEIPT)) {
						// TODO 取消gcm 发送

						String formUser = message.getFrom();
						int end = formUser.indexOf("/");
						formUser = formUser.substring(0, end);
						String cancelKey = formUser + action;
						Timer cancelTimer = gcmTimers.get(cancelKey);
						if (null != cancelTimer) {
							cancelTimer.cancel();
							gcmTimers.remove(cancelKey);
						}
					}
				}
			});
		}
	};

	/**
	 * Class used for the client Binder. Because we know this service always
	 * runs in the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {

		public TigaseMessageBinderService getService() {
			// Return this instance of LocalService so clients can call public
			// methods
			return TigaseMessageBinderService.this;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		ctx = this;

		Timer timer = new Timer();

		TimerTask task = new TimerTask() {

			public void run() {
				Intent intent = new Intent(Constants.Action.ACTION_TIGASE_STATE_CHANGE);
				String status = "";
				if (TigaseManager.getInstance(ctx).getIsConnected()) {
					status = "online";
				} else {
					status = "offline";
				}
				intent.putExtra(HomeActivity.INTENT_KEY_STATE, status);
				sendBroadcast(intent);
			}

		};
		timer.schedule(task, 10000, 10000);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	public void tigaseLogin(String name, String password) {
		TigaseManager.getInstance(ctx).setLoginInfo(name, password);
		TigaseManager.getInstance(ctx).initConnect();
		TigaseManager.getInstance(ctx).setChatManagerListener(chatListener);
	}

	// 发送消息至tagise
	public void sendMessage(String msg, String toUser, String toRcID, String action) {
		String msgStr = MESSAGE_TYPE_MESSAGE + MESSAGE_SPLIT + action + MESSAGE_SPLIT + msg;

		TigaseManager.getInstance(ctx).sendMessageBackup(toUser, msgStr);
		// 需要 gcm 推送的消息
		if (action.equals(MESSAGE_ACTION_FRIEND) || action.equals(MESSAGE_ACTION_SEND_MESSAGE)) {
			String timerKey = TigaseManager.getInstance(ctx).getFullUser(toUser) + action;
			Timer timer = new Timer();
			String type = "";

			if (action.equals(MESSAGE_ACTION_SEND_MESSAGE)) {
				type = Constants.GCM_TYPE_MSG;
			} else if (action.equals(MESSAGE_ACTION_FRIEND)) {
				type = Constants.GCM_TYPE_FRIEND;
			}

			GcmTask gcmTask = new GcmTask(ctx, type, toRcID, msg);
			timer.schedule(gcmTask, TigaseNodeManager.getInstance(ctx).getNodeTimeout());
			gcmTimers.put(timerKey, timer);
		}

	}

	// 注册接收消息监听器
	public void setOnMessageReciver(TigaseMessageReceiver messageReceiver) {
		this.messageRecevier = messageReceiver;
	}

	class GcmTask extends TimerTask {

		private Context ctx;

		private String type;

		private String toRcId;

		private String extra;

		GcmTask(Context ctx, String type, String toRcId, String extra) {
			this.ctx = ctx;
			this.type = type;
			this.toRcId = toRcId;
			this.extra = extra;

		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Log.d("GcmTask", "push to service");

			JSONObject json = new JSONObject();
			try {
				// TODO
				json.put("appId", Constants.APP_ID);
				json.put("type", type);
				// TODO 设置真是token
				PhotoTalkApplication app = (PhotoTalkApplication) ctx.getApplicationContext();

				json.put("token", app.getCurrentUser().getToken());
				json.put("fRcId", toRcId);
				json.put("deviceId", MetaHelper.getMACAddress(ctx));
				json.put("rcId", app.getCurrentUser().getRcId());
				json.put("language", "");
				json.put("extra", extra);
			}
			catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			OutputStream output = null;
			InputStream is = null;
			HttpURLConnection conn = null;
			String content = null;
			try {
				conn = (HttpURLConnection) new URL(GCM_URL).openConnection();
				conn.setConnectTimeout(5000);
				conn.setReadTimeout(5000);
				conn.setDoOutput(true);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/json");
				byte[] bodyBytes = json.toString().getBytes(HTTP.UTF_8);
				output = conn.getOutputStream();
				output.write(bodyBytes);
				is = conn.getInputStream();
				InputStreamReader reader = new InputStreamReader(is, "UTF-8");
				StringBuilder builder = new StringBuilder();
				char[] readChars = new char[1024];
				String temp = null;
				int result = -1;
				while ((result = reader.read(readChars, 0, 1024)) != -1) {
					temp = new String(readChars, 0, result);
					builder.append(temp);
				}
				reader.close();
				content = builder.toString();
			}
			catch (ConnectException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			finally {
				try {
					output.close();
				}
				catch (Exception e) {
				}
				try {
					is.close();
				}
				catch (Exception e) {
				}
				if (conn != null)
					conn.disconnect();

			}

		}
	}

}
