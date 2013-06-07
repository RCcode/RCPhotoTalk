package com.rcplatform.tigase;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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

import android.content.Context;

import com.rcplatform.phototalk.bean.TigaseMassage;
import com.rcplatform.phototalk.db.impl.TigaseDb4oDatabase;

public class TigaseManager {

	static private TigaseManager instance = null;

	private TigaseNode node = null;

	private XMPPConnection connection = null;

	private ChatManager chatManager = null;

	private TigaseDb4oDatabase db = null;

	private String user = null;

	private String password = null;

	private boolean isConnected = false;

	private TigaseListenter connectListenter = null;

	private TigaseConnectionCreationListener createListener = null;

	private ChatManagerListener chatListener = null;

	private final int INIT_CONNECT_MAX_COUNT = 4;

	private final int CONNECT_INTERVAL = 60000;

	private Timer initConnectTimer = null;

	private int retryConnectCount = 0;

	private Context ctx = null;

	class TigaseListenter implements ConnectionListener {

		@Override
		public void connectionClosed() {
			// TODO Auto-generated method stub
		}

		@Override
		public void connectionClosedOnError(Exception e) {
			// TODO Auto-generated method stub
			disConnect();
			initConnect();
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
			// login
			if (null != user && null != password) {
				try {
					if (!connection.isAuthenticated()) {
						connection.login(user, password);
					}
				}
				catch (XMPPException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
			initConnectTimer = new Timer();
			ConnectTimerTask task = new ConnectTimerTask();
			if (retryConnectCount < INIT_CONNECT_MAX_COUNT) {
				int delay = (int) Math.pow(2, retryConnectCount) * CONNECT_INTERVAL;
				initConnectTimer.schedule(task, delay);
			}
			retryConnectCount++;
			connect();
		}

	}

	private TigaseManager(Context ctx) {
		this.ctx = ctx;
		db = new TigaseDb4oDatabase();
	}

	static public TigaseManager getInstance(Context ctx) {
		if (null == instance) {
			instance = new TigaseManager(ctx);
		}
		return instance;
	}

	synchronized private void connect() {
		node = TigaseNodeManager.getInstance(ctx).getNode();
		try {
			ConnectionConfiguration connConfig = new ConnectionConfiguration(node.getIp(), node.getPort(), node.getDomain());
			connection = new XMPPConnection(connConfig);
			createListener = new TigaseConnectionCreationListener();
			connection.addConnectionCreationListener(createListener);
			connection.connect();

			connectListenter = new TigaseListenter();
			connection.addConnectionListener(connectListenter);
		}
		catch (Exception xe) {
			xe.printStackTrace();
			if (null != connection) {
				if (!connection.isConnected()) {
					disConnect();
				}
			}
		}
	}

	public void initConnect() {
		retryConnectCount = 0;
		initConnectTimer = new Timer();
		ConnectTimerTask task = new ConnectTimerTask();
		initConnectTimer.schedule(task, 0);
	}

	public void disConnect() {
		if (null != connection) {
			try {
				connection.removeConnectionCreationListener(createListener);
				connection.removeConnectionListener(connectListenter);
				connection.disconnect();
			}
			catch (Exception e) {

			}
		}
		connection = null;
		chatManager = null;
		createListener = null;
		connectListenter = null;
		isConnected = false;
	}

	public boolean getIsConnected() {
		return isConnected;
	}

	public void setChatManagerListener(ChatManagerListener listener) {
		if (null != chatManager) {
		//	chatManager.addChatListener(listener);
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
		}

		if (false == isConnected) {
			return false;
		}

		boolean flag = false;
		String toUser = to + "@" + node.getDomain().trim();

		try {
			Chat newchat = chatManager.createChat(toUser, new MessageListener() {

				@Override
				public void processMessage(Chat chat, Message message) {
					// TODO Auto-generated method stub

				}
			});
			newchat.sendMessage(msg);
			flag = true;
		}
		catch (Exception e) {
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
