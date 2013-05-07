package com.rcplatform.phototalk.logic;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.Intent;

import com.rcplatform.phototalk.MenueApplication;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.InformationState;
import com.rcplatform.phototalk.bean.InformationType;
import com.rcplatform.phototalk.bean.ServiceSimpleNotice;
import com.rcplatform.phototalk.clienservice.InformationStateChangeService;
import com.rcplatform.phototalk.clienservice.InviteFriendUploadService;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.utils.PhotoTalkUtils;
import com.rcplatform.phototalk.utils.Contract.Action;

public class LogicUtils {
	public static void updateInformationState(Context context, String action, Information... infos) {
		Intent intent = new Intent(context, InformationStateChangeService.class);
		if (infos.length > 0) {
			ServiceSimpleNotice[] ssns = new ServiceSimpleNotice[infos.length];
			for (int i = 0; i < infos.length; i++) {
				Information info = infos[i];
				ssns[i] = new ServiceSimpleNotice(info.getStatu() + "", info.getRecordId(), info.getType() + "");
			}
			intent.putExtra(InformationStateChangeService.PARAM_KEY_INFORMATION, ssns);
		}
		intent.setAction(action);
		context.startService(intent);
	}

	public static void clearInformationHistory(Context context) {
		updateInformationState(context, Action.ACTION_INFORMATION_DELETE);
		PhotoTalkDatabaseFactory.getDatabase().clearInformation();
	}

	public static void uploadFriendInvite(Context context, String action, int type, String... ids) {
		Intent intent = new Intent(context, InviteFriendUploadService.class);
		intent.setAction(action);
		if (action.equals(Action.ACTION_UPLOAD_INTITE_THIRDPART)) {
			intent.putExtra(InviteFriendUploadService.PARAM_FRIENDS_IDS, ids);
			intent.putExtra(InviteFriendUploadService.PARAM_TYPE, type);
		}
		context.startService(intent);
	}

	public static boolean isNeedToNotifyServiceOverInformation(Context context, Information information) {
		return ((information.getType() == InformationType.TYPE_PICTURE_OR_VIDEO && information.getStatu() == InformationState.STATU_NOTICE_OPENED) || (information.getType() == InformationType.TYPE_FRIEND_REQUEST_NOTICE && information.getStatu() == InformationState.STATU_QEQUEST_ADDED))
				&& isSender(context, information);
	}

	public static List<Information> informationFilter(Context context, List<Information> informations, List<Information> localData) {
		List<Information> newNotices = new ArrayList<Information>();
		List<Information> updateInfos = new ArrayList<Information>();
		Iterator<Information> iterator = informations.iterator();
		while (iterator.hasNext()) {
			Information serviceInfo = iterator.next();
			if (isNeedToNotifyServiceOverInformation(context, serviceInfo)) {
				updateInformationState(context, Action.ACTION_INFORMATION_OVER, serviceInfo);
			}
			// 如果是通知
			if (localData != null && localData.contains(serviceInfo)) {
				Information localInfo = localData.get(localData.indexOf(serviceInfo));
				if (serviceInfo.getStatu() == localInfo.getStatu()) {
					// 状态没有改变
					iterator.remove();
				} else {
					// 状态改变
					if (serviceInfo.getType() == InformationType.TYPE_FRIEND_REQUEST_NOTICE) {
						// 好友请求信息
						localInfo.setStatu(serviceInfo.getStatu());
					} else if (serviceInfo.getType() == InformationType.TYPE_PICTURE_OR_VIDEO) {
						// 图片信息
						if (InformationState.isServiceState(localInfo.getStatu()) && localInfo.getStatu() > serviceInfo.getStatu()) {
							updateInformationState(context, Action.ACTION_INFORMATION_STATE_CHANGE, localInfo);
						} else {
							localInfo.setStatu(serviceInfo.getStatu());
							updateInfos.add(localInfo);
						}
					}
				}
			} else {
				newNotices.add(serviceInfo);
			}
		}
		if (updateInfos.size() > 0) {
			Information[] infos = new Information[updateInfos.size()];
			for (int i = 0; i < updateInfos.size(); i++) {
				infos[i] = updateInfos.get(i);
			}
			updateInfos.clear();
			PhotoTalkDatabaseFactory.getDatabase().updateInformationState(infos);
		}
		if (newNotices.size() > 0)
			PhotoTalkDatabaseFactory.getDatabase().saveRecordInfos(newNotices);
		return newNotices;
	}

	/**
	 * 判断当前用户是不是发送者
	 * 
	 * @param context
	 * @param record
	 * @return true表示当前用户是发送者，false表示当前用户是接受者
	 */
	public static boolean isSender(Context context, Information record) {

		if (((MenueApplication) context.getApplicationContext()).getCurrentUser().getSuid().equals(record.getSender().getSuid()) && !record.getReceiver().getSuid().equals(record.getSender().getSuid()))
			return true;
		return false;
	}
}
