package com.rcplatform.phototalk.logic;

import java.util.HashMap;
import java.util.Map;

public class SendingInformationManager {
	private int maxNotificationId = Integer.MAX_VALUE - 1;
	private int driftNotificationId = Integer.MAX_VALUE;

	private SendingInformationManager() {
	}

	private static final SendingInformationManager instance = new SendingInformationManager();

	public static SendingInformationManager getInstance() {
		return instance;
	}

	private Map<Long, Integer> sendingInformation = new HashMap<Long, Integer>();

	public void addSendingInformation(long key, int notificationId) {
		if (notificationId == driftNotificationId)
			return;
		sendingInformation.put(key, notificationId);
	}

	public int getNotificationId(long flag) {
		if (sendingInformation.containsKey(flag))
			return sendingInformation.get(flag);
		return maxNotificationId--;
	}

	public int getDriftNotificationId() {
		return driftNotificationId;
	}
}
