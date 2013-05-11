package com.rcplatform.tigase;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;

public class XmppTool {

	private static XMPPConnection con = null;

	private static ChatManager chatManager = null;

	private static final String XMPP_RESOURCES = "";

	private static TigaseNode node = null;

	public enum MessageStatus {
		USER_OFFLINE, SEND_OK, SEND_ERROR
	};

	// 建立xmpp 连接
	private static void openConnection(String service, int port, String domain) {
		try {
			ConnectionConfiguration connConfig = new ConnectionConfiguration(service, port, domain);
			con = new XMPPConnection(connConfig);
			con.connect();

		}
		catch (XMPPException xe) {
			xe.printStackTrace();
			con = null;
		}
	}

	public static void createConnection(TigaseNode node) {
		XmppTool.node = node;
		if (con == null) {
			openConnection(XmppTool.node.getIp(), XmppTool.node.getPort(), XmppTool.node.getDomain());
		}

		if (con != null) {
			if (null == chatManager) {
				chatManager = con.getChatManager();
			}
		}
	}

	// 关闭tigase
	public static void closeConnection() {
		con.disconnect();
		con = null;
		chatManager = null;
	}

	// 登录tigase
	public static boolean login(String user, String password) {
		boolean flag = false;
		try {
			con.login(user, password, XMPP_RESOURCES);
			flag = true;
		}
		catch (XMPPException e) {
			// TODO Auto-generated catch block
			flag = false;
			e.printStackTrace();
		}
		return flag;
	}

	public static MessageStatus sendMessage(String to, String msg) {
		// 没有node获取node
		if (null == node) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					node = TigaseNodeUtil.getTigaseNode();
					XmppTool.createConnection(node);
				}
			}).start();
			return MessageStatus.SEND_ERROR;
		}
		// 连接失败连接
		if (null == con) {
			new Thread(new Runnable() {

				@Override
				public void run() {
					XmppTool.createConnection(node);
				}
			}).start();
			return MessageStatus.SEND_ERROR;
		}

		MessageStatus msgFlag = MessageStatus.SEND_ERROR;
		String toUser = to + "@" + node.getDomain().trim();
		Presence presence = con.getRoster().getPresence(toUser);
		Chat newchat = chatManager.createChat(toUser, null);
		try {
			newchat.sendMessage(msg);
			msgFlag = MessageStatus.SEND_OK;
		}
		catch (XMPPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			msgFlag = MessageStatus.SEND_ERROR;
		}
		if (!presence.isAvailable()) {
			msgFlag = MessageStatus.USER_OFFLINE;
			// TODO gcm
		}

		return msgFlag;
	}

	public static void setChatManagerListener(ChatManagerListener listener) {
		chatManager.addChatListener(listener);
	}

}
