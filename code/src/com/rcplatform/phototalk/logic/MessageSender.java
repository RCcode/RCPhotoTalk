package com.rcplatform.phototalk.logic;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;

import com.rcplatform.message.UserMessageService;
import com.rcplatform.phototalk.MenueApplication;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.InformationType;
import com.rcplatform.phototalk.bean.RecordUser;
import com.rcplatform.phototalk.request.JSONConver;

public class MessageSender {
	public static void sendInformation(Context context, String tigaseId, Information... informations) {
		String message = JSONConver.informationToJSON(informations);
		Intent intent = new Intent();
		intent.setAction(UserMessageService.MESSAGE_SEND_BROADCAST);
		intent.putExtra(UserMessageService.MESSAGE_TO_USER, tigaseId);
		intent.putExtra(UserMessageService.MESSAGE_CONTENT_KEY, message);
		if (informations[0].getType() == InformationType.TYPE_FRIEND_REQUEST_NOTICE)
			intent.putExtra(UserMessageService.MESSAGE_ACTION_KEY, UserMessageService.MESSAGE_ACTION_FRIEND);
		else if(informations[0].getType() == InformationType.TYPE_PICTURE_OR_VIDEO)
			intent.putExtra(UserMessageService.MESSAGE_ACTION_KEY, UserMessageService.MESSAGE_ACTION_MSG);
		context.sendBroadcast(intent);
	}

	public static void sendInformation(Context context, Map<String, Information> informations, List<String> userIds) {
		String currentRcid = ((MenueApplication) context.getApplicationContext()).getCurrentUser().getRcId();
		if (userIds != null && userIds.size() > 0) {
			for (String rcid : userIds) {
				Information info = informations.get(rcid);
				String tigaseId = null;
				if (info.getReceiver().getRcId().equals(info.getSender().getRcId())) {
					tigaseId = info.getReceiver().getTigaseId();
				} else if (info.getReceiver().getRcId().equals(currentRcid)) {
					tigaseId = info.getSender().getTigaseId();
				} else {
					tigaseId = info.getReceiver().getTigaseId();
				}
				sendInformation(context, tigaseId, info);
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
