package com.rcplatform.tigase;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;

import com.google.android.gcm.RCGcmUtil;
import com.rcplatform.phototalk.HomeActivity;
import com.rcplatform.phototalk.utils.Constants;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Vibrator;

public class TigaseMessageBinderService extends Service {

	private static final String MESSAGE_TYPE_MESSAGE = "m";

	private static final String MESSAGE_TYPE_RECEIPT = "r";

	private static final String MESSAGE_SPLIT = ":";

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
						if (action.equals(com.rcplatform.phototalk.utils.Constants.Message.MESSAGE_ACTION_FRIEND)
						        || action.equals(com.rcplatform.phototalk.utils.Constants.Message.MESSAGE_ACTION_SEND_MESSAGE)) {
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
	public boolean onUnbind (Intent intent){
		super.onUnbind(intent);
		TigaseManager.getInstance(ctx).disConnect();
		return true;
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
		if (action.equals(com.rcplatform.phototalk.utils.Constants.Message.MESSAGE_ACTION_FRIEND)
		        || action.equals(com.rcplatform.phototalk.utils.Constants.Message.MESSAGE_ACTION_SEND_MESSAGE)) {
			String timerKey = TigaseManager.getInstance(ctx).getFullUser(toUser) + action;
			Timer timer = new Timer();

			GcmTask gcmTask = new GcmTask(ctx, action, toRcID, msg);
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
			RCGcmUtil.pushGcmMsg(ctx, type, toRcId, extra);
		}
	}
}
