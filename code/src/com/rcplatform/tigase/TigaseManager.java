package com.rcplatform.tigase;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.protocol.HTTP;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionCreationListener;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.google.android.gcm.GCMRegistrar;
import com.google.android.gcm.MetaHelper;
import com.rcplatform.phototalk.PhotoTalkApplication;
import com.rcplatform.phototalk.bean.TigaseMassage;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.db.impl.TigaseDb4oDatabase;
import com.rcplatform.phototalk.galhttprequest.LogUtil;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.PrefsUtils;
import com.rcplatform.phototalk.utils.Utils;

public class TigaseManager {

	static private TigaseManager instance = null;

	private TigaseNode node = null;

	private XMPPConnection connection = null;

	private ChatManager chatManager = null;

	static private TigaseDb4oDatabase db = null;

	private String user = null;

	private String password = null;

	private boolean isConnected = false;

	private TigaseListenter connectListenter = null;

	private TigaseConnectionCreationListener createListener = null;

	private ChatManagerListener chatListener = null;

	private final int INIT_CONNECT_MAX_COUNT = 4;

	private final int CONNECT_INTERVAL = 1000;

	private final int CONNECT_MAX_INTERVAL = 300000;

	private final int RESET_PASSWORD_MAX_COUNT = 5;

	private final int RESET_PASSWORD_INTERVAL = 10000;

	private final int RESET_PASSWORD_MAX_INTERVAL = 300000;

	private int resetPasswordCount = 0;

	private final String RESET_PASSWORD_URL = "http://rc.rcplatformhk.net/rcboss/user/syncTigasePwd.do";

	private Timer initConnectTimer = null;

	private Timer resetPasswordTimer = null;

	private int retryConnectCount = 0;

	private Context ctx = null;

	private boolean isConnecting = false; //

	class ResetTigasePasswordTask extends TimerTask {

		@Override
		public void run() {
			resetPasswordTimer = new Timer();
			ResetTigasePasswordTask task = new ResetTigasePasswordTask();
			if (resetPasswordCount < RESET_PASSWORD_MAX_COUNT) {
				long delay = Utils.exponentialBackOff(resetPasswordCount, RESET_PASSWORD_INTERVAL, RESET_PASSWORD_MAX_INTERVAL);
				resetPasswordTimer.schedule(task, delay);
			}
			resetPasswordCount++;

			UserInfo userInfo = ((PhotoTalkApplication) ctx.getApplicationContext()).getCurrentUser();
			JSONObject json = new JSONObject();
			try {
				json.put("rcId", userInfo.getRcId());
				// TODO 设置真是token
				json.put("token", userInfo.getToken());
				json.put("appId", Constants.APP_ID);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			OutputStream output = null;
			InputStream is = null;
			HttpURLConnection conn = null;
			String content = null;
			try {
				conn = (HttpURLConnection) new URL(RESET_PASSWORD_URL).openConnection();
				conn.setConnectTimeout(5000);
				conn.setReadTimeout(5000);
				conn.setDoOutput(true);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/json");
				String paramStr = json.toString();
				byte[] bodyBytes = paramStr.getBytes(HTTP.UTF_8);
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
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				try {
					output.close();
					is.close();
				} catch (Exception e) {
				}
				if (conn != null) {
					conn.disconnect();
				}

			}

			try {
				JSONObject resultJSON = new JSONObject(content);
				if (resultJSON.has("status")) {
					int flag = resultJSON.getInt("status");
					if (10000 == flag) {
						// 更新密码
						String pwd = resultJSON.getString("tigasePwd");
						userInfo.setTigasePwd(pwd);
						PrefsUtils.User.saveUserInfo(ctx, userInfo.getRcId(), userInfo);
						password = pwd;
						resetPasswordTimer.cancel();
						disConnect();
						initConnect();
					}
				} else {

				}
			} catch (Exception e) {

				e.printStackTrace();

			}
		}

	}

	class TigaseListenter implements ConnectionListener {

		@Override
		public void connectionClosed() {
			// TODO Auto-generated method stub
		}

		@Override
		public void connectionClosedOnError(Exception e) {
			// TODO Auto-generated method stub
			disConnect();
			retryConnect();
		}

		@Override
		public void reconnectingIn(int seconds) {
			// TODO Auto-generated method stub

		}

		@Override
		public void reconnectionSuccessful() {
			// TODO Auto-generated method stub
		}

		@Override
		public void reconnectionFailed(Exception e) {
			// TODO Auto-generated method stub
		}

	}

	class TigaseConnectionCreationListener implements ConnectionCreationListener {

		@Override
		public void connectionCreated(Connection connection) {
			// TODO Auto-generated method stub
			initConnectTimer.cancel();
			isConnecting = false;
			// login
			LogUtil.e("connection created success~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			if (null != user && null != password) {
				try {

					if (!connection.isAuthenticated()) {
						LogUtil.e("connection created login~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
						connection.login(user + "@" + node.getDomain().trim(), password);
					}
				} catch (XMPPException e) {
					// TODO Auto-generated catch block
					LogUtil.e("login error~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
					e.printStackTrace();
					String message = e.getMessage().trim();
					if (message.equals("SASL authentication failed using mechanism PLAIN")) {
						resetPasswordTimer = new Timer();
						ResetTigasePasswordTask task = new ResetTigasePasswordTask();
						resetPasswordTimer.schedule(task, 0);
					} else {
						retryConnect();
					}
				}
			}

			chatManager = connection.getChatManager();
			chatManager.addChatListener(chatListener);

			isConnected = true;
			// 发送备份消息
			List<TigaseMassage> list = db.getTigaseMassages();
			for (int i = 0; i < list.size(); i++) {
				TigaseMassage msg = list.get(i);
				boolean sendStatus = sendMessage(msg.getToUser(), msg.getMassage());
				if (true == sendStatus) {
					db.deleteTigaseMassage(msg);
				}
			}
		}

	}

	class ConnectTimerTask extends TimerTask {

		public void run() {
			connect();
		}

	}

	private TigaseManager(Context ctx) {
		this.ctx = ctx;
		if (db == null) {
			db = new TigaseDb4oDatabase();
		}
	}

	static public TigaseManager getInstance(Context ctx) {
		if (null == instance) {
			instance = new TigaseManager(ctx);
		}
		return instance;
	}

	synchronized private void connect() {
		if (isConnecting) {
			return;
		}
		isConnecting = true;
		node = TigaseNodeManager.getInstance(ctx).getNode();
		try {
			ConnectionConfiguration connConfig = new ConnectionConfiguration(node.getIp(), node.getPort(), node.getDomain());
			connection = new XMPPConnection(connConfig);
			createListener = new TigaseConnectionCreationListener();
			connection.addConnectionCreationListener(createListener);
			connection.connect();

			connectListenter = new TigaseListenter();
			connection.addConnectionListener(connectListenter);
			LogUtil.e("tigase connect success ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		}
		catch (Exception xe) {
			LogUtil.e("tigase connect fail ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			isConnecting = false;
			xe.printStackTrace();
			if (null != connection) {
				if (!connection.isConnected()) {
					disConnect();
				}
			}
			retryConnect();
		}
	}

	public void initConnect() {

		retryConnectCount = 0;
		initConnectTimer = new Timer();
		ConnectTimerTask task = new ConnectTimerTask();
		initConnectTimer.schedule(task, 0);

	}

	private void retryConnect() {
		retryConnectCount++;
		if (null != initConnectTimer) {
			initConnectTimer.cancel();
			initConnectTimer = null;
		}
		initConnectTimer = new Timer();
		ConnectTimerTask task = new ConnectTimerTask();
		long delay = Utils.exponentialBackOff(retryConnectCount, CONNECT_INTERVAL, CONNECT_MAX_INTERVAL);
		initConnectTimer.schedule(task, delay);
	}

	public void disConnect() {
		isConnecting = false;
		if (null != connection) {
			try {
				connection.removeConnectionCreationListener(createListener);
				connection.removeConnectionListener(connectListenter);

				Thread thread = new Thread(new Runnable() {

					@Override
					public void run() {
						if (null != connection) {
							connection.disconnect();
							connection = null;
							chatManager = null;
							createListener = null;
							connectListenter = null;
							isConnected = false;
						}
					}
				});
				thread.start();

			} catch (Exception e) {

			}
		}

	}

	public boolean getIsConnected() {
		return isConnected;
	}

	public void setChatManagerListener(ChatManagerListener listener) {
		if (null != chatManager) {
			// chatManager.addChatListener(listener);
		}
		this.chatListener = listener;
	}

	public void setLoginInfo(String user, String password) {
		this.user = user;
		this.password = password;
	}

	public boolean sendMessage(String to, String msg) {
		if (null == connection) {
			initConnect();
			return false;
		}

		if (false == isConnected) {
			return false;
		}

		boolean flag = false;
		String toUser = to + "@" + node.getDomain().trim();

		try {
			Chat newchat = chatManager.createChat(toUser, null);
			newchat.sendMessage(msg);
			flag = true;
		} catch (Exception e) {
			e.printStackTrace();
			flag = false;
		}

		return flag;
	}

	public String getFullUser(String userName) {
		return userName + "@" + node.getDomain();
	}

	public void sendMessageBackup(String toUser, String msgStr) {
		boolean flag = sendMessage(toUser, msgStr);
		if (true != flag) {
			// 备份消息
			TigaseMassage massage = new TigaseMassage();
			massage.setMassage(msgStr);
			massage.setToUser(toUser);
			List<TigaseMassage> list = new ArrayList<TigaseMassage>();
			list.add(massage);
			db.saveTigaseMassages(list);
		}
	}

}
