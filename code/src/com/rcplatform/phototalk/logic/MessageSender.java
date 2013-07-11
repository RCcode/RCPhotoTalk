package com.rcplatform.phototalk.logic;

import java.util.List;
import java.util.Map;

import android.content.Context;

import com.google.android.gcm.RCGcmUtil;
import com.rcplatform.phototalk.PhotoTalkApplication;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.InformationState;
import com.rcplatform.phototalk.bean.InformationType;
import com.rcplatform.phototalk.bean.RecordUser;
import com.rcplatform.phototalk.request.JSONConver;
import com.rcplatform.phototalk.utils.Constants.Message;
import com.rcplatform.phototalk.utils.RCThreadPool;
import com.rcplatform.tigase.TigaseMessageBinderService;

public class MessageSender {
	private static final MessageSender instance = new MessageSender();
	private TigaseMessageBinderService mService;

	public MessageSender() {
	}

	public static synchronized MessageSender getInstance() {
		return instance;
	}

	public synchronized void setTigaseService(TigaseMessageBinderService service) {
		mService = service;
	}

	public synchronized void stop() {
		mService = null;
	}

	public void sendInformation(final Context context, String tigaseId, final String rcId, Information... informations) {
		final String message = JSONConver.informationToJSON(informations);
		String action = null;
		if (informations[0].getType() == InformationType.TYPE_FRIEND_REQUEST_NOTICE) {
			action = Message.MESSAGE_ACTION_FRIEND;
		} else if (informations[0].getType() == InformationType.TYPE_PICTURE_OR_VIDEO) {
			if (LogicUtils.isSender(context, informations[0])
					&& informations[0].getStatu() == InformationState.PhotoInformationState.STATU_NOTICE_SENDED_OR_NEED_LOADD) {
				action = Message.MESSAGE_ACTION_SEND_MESSAGE;
			} else {
				action = Message.MESSAGE_ACTION_MSG;
			}
		}
		if (mService != null)
			mService.sendMessage(message, tigaseId, rcId, action);
		else {
			final String gcmAction=action;
			RCThreadPool.getInstance().addTask(new Runnable() {

				@Override
				public void run() {
					RCGcmUtil.pushGcmMsg(context, gcmAction, rcId, message);
				}
			});
		}

	}

	public void sendInformation(Context context, Map<String, Information> informations, List<String> userIds) {
		String currentRcid = ((PhotoTalkApplication) context.getApplicationContext()).getCurrentUser().getRcId();
		if (userIds != null && userIds.size() > 0) {
			for (String rcid : userIds) {
				Information info = informations.get(rcid);
				String tigaseId = null;
				String rcId = null;
				if (info.getReceiver().getRcId().equals(info.getSender().getRcId())) {
					rcId = info.getReceiver().getRcId();
					tigaseId = info.getReceiver().getTigaseId();
				} else if (info.getReceiver().getRcId().equals(currentRcid)) {
					rcId = info.getSender().getRcId();
					tigaseId = info.getSender().getTigaseId();
				} else {
					rcId = info.getReceiver().getRcId();
					tigaseId = info.getReceiver().getTigaseId();
				}
				sendInformation(context, tigaseId, rcId, info);
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
