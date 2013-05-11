package com.rcplatform.phototalk.logic;

import java.util.Map;

import android.content.Context;
import android.content.Intent;

import com.rcplatform.message.UserMessageService;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.RecordUser;
import com.rcplatform.phototalk.request.JSONConver;

public class MessageSender {
	public static void sendInformation(Context context,String tigaseId, Information... informations) {
		String message = JSONConver.informationToJSON(informations);
		Intent intent = new Intent();
		intent.setAction(UserMessageService.MESSAGE_SEND_BROADCAST);
		intent.putExtra(UserMessageService.MESSAGE_TO_USER, "1000032_1");
		intent.putExtra(UserMessageService.MESSAGE_CONTENT_KEY, message);
		context.sendBroadcast(intent);
	}

	public static void sendInformation(Context context, Map<String, Information> informations, Map<String, String> userIds) {

		if (userIds != null && userIds.size() > 0) {
			for (String suid : userIds.keySet()) {
				Information info = informations.get(suid);
				sendInformation(context,userIds.get(suid), info);
			}

		}
	}

	public static Information createInformation(int informationType, int informationState, RecordUser sender, RecordUser receiver, long createTime) {
		Information information = new Information();
		information.setType(informationType);
		information.setStatu(informationState);
		information.setReceiver(receiver);
		information.setSender(sender);
		information.setCreatetime(createTime);
		return information;
	}
}
