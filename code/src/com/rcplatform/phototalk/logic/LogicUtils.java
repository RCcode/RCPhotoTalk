package com.rcplatform.phototalk.logic;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.rcplatform.phototalk.HomeActivity;
import com.rcplatform.phototalk.PhotoTalkApplication;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.InformationState;
import com.rcplatform.phototalk.bean.InformationType;
import com.rcplatform.phototalk.bean.RecordUser;
import com.rcplatform.phototalk.bean.ServiceSimpleNotice;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.clienservice.InformationStateChangeService;
import com.rcplatform.phototalk.clienservice.InviteFriendUploadService;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.logic.controller.InformationPageController;
import com.rcplatform.phototalk.request.Request;
import com.rcplatform.phototalk.request.inf.PhotoSendListener;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.Constants.Action;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.FacebookUtil;
import com.rcplatform.phototalk.utils.PrefsUtils;

public class LogicUtils {
	public static void updateInformationState(Context context, String action, Information... infos) {
		Intent intent = new Intent(context, InformationStateChangeService.class);
		if (infos.length > 0) {
			ServiceSimpleNotice[] ssns = new ServiceSimpleNotice[infos.length];
			for (int i = 0; i < infos.length; i++) {
				Information info = infos[i];
				ssns[i] = new ServiceSimpleNotice(info.getStatu() + "", "", info.getType() + "", info.getLastUpdateTime());
			}
			intent.putExtra(InformationStateChangeService.PARAM_KEY_INFORMATION, ssns);
		}
		intent.setAction(action);
		context.startService(intent);
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

	public static List<Information> informationFilter(Context context, List<Information> informations, List<Information> localData) {
		List<Information> newNotices = new ArrayList<Information>();
		List<Information> updateInfos = new ArrayList<Information>();
		Iterator<Information> iterator = informations.iterator();
		while (iterator.hasNext()) {
			Information serviceInfo = iterator.next();
			// 如果是通知
			if (localData != null && localData.contains(serviceInfo)) {
				Information localInfo = localData.get(localData.indexOf(serviceInfo));
				if (serviceInfo.getType() == localInfo.getType() && serviceInfo.getStatu() == localInfo.getStatu()) {
					// 状态没有改变
					iterator.remove();
				} else {
					// 状态改变
					if (serviceInfo.getType() == InformationType.TYPE_FRIEND_REQUEST_NOTICE) {
						// 好友请求信息
						if (localInfo.getStatu() < serviceInfo.getStatu()) {
							localInfo.setStatu(serviceInfo.getStatu());
							updateInfos.add(localInfo);
						}
					} else if (serviceInfo.getType() == InformationType.TYPE_PICTURE_OR_VIDEO) {
						// 图片信息
						if (localInfo.getStatu() < serviceInfo.getStatu()) {
							localInfo.setStatu(serviceInfo.getStatu());
							updateInfos.add(localInfo);
						}
					}
				}
			} else {
				serviceInfo.setReceiveTime(System.currentTimeMillis());
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

		if (((PhotoTalkApplication) context.getApplicationContext()).getCurrentUser().getRcId().equals(record.getSender().getRcId())
				&& !record.getReceiver().getRcId().equals(record.getSender().getRcId()))
			return true;
		return false;
	}

	public static void informationFriendAdded(Context context, Information information, Friend friend) {
		if (information.getType() == InformationType.TYPE_FRIEND_REQUEST_NOTICE) {
			PhotoTalkDatabaseFactory.getDatabase().updateInformationState(information);
//			MessageSender.sendInformation(context, friend.getTigaseId(), friend.getRcId(), information);
			MessageSender.getInstance().sendInformation(context, friend.getTigaseId(), friend.getRcId(), information);
		}
	}

	public static void friendAdded(Context context, Friend friend, int addType) {
		UserInfo currentUser = ((PhotoTalkApplication) context.getApplicationContext()).getCurrentUser();
		long createTime = System.currentTimeMillis();
		Information information = null;
		if (addType == Constants.FriendAddType.ADD_FRIEND_PASSIVE) {
			RecordUser sender = new RecordUser(friend.getRcId(), friend.getNickName(), friend.getHeadUrl(), friend.getTigaseId());
			RecordUser receiver = new RecordUser(currentUser.getRcId(), currentUser.getNickName(), currentUser.getHeadUrl(), currentUser.getTigaseId());
			information = MessageSender.createInformation(InformationType.TYPE_FRIEND_REQUEST_NOTICE,
					InformationState.FriendRequestInformationState.STATU_QEQUEST_ADD_CONFIRM, sender, receiver, createTime);
			PhotoTalkDatabaseFactory.getDatabase().updateFriendRequestInformationByFriend(friend);
		} else if (addType == Constants.FriendAddType.ADD_FRIEND_ACTIVE) {
			RecordUser receiver = new RecordUser(friend.getRcId(), friend.getNickName(), friend.getHeadUrl(), friend.getTigaseId());
			RecordUser sender = new RecordUser(currentUser.getRcId(), currentUser.getNickName(), currentUser.getHeadUrl(), currentUser.getTigaseId());
			information = MessageSender.createInformation(InformationType.TYPE_FRIEND_REQUEST_NOTICE,
					InformationState.FriendRequestInformationState.STATU_QEQUEST_ADD_REQUEST, sender, receiver, createTime);
			information.setReceiveTime(createTime);
			List<Information> infos = new ArrayList<Information>();
			infos.add(information);
			PhotoTalkDatabaseFactory.getDatabase().saveRecordInfos(infos);
		}
		if (information != null) {
			MessageSender.getInstance().sendInformation(context, friend.getTigaseId(), friend.getRcId(), information);
//			MessageSender.sendInformation(context, friend.getTigaseId(), friend.getRcId(), information);
			InformationPageController.getInstance().friendAdded(information, addType);
		}
	}

	public static void clearInformations(Context context) {
		PhotoTalkDatabaseFactory.getDatabase().clearInformation();
		InformationPageController.getInstance().clearInformations();
	}

	public static void startShowPhotoInformation(Information information) {
		PhotoInformationCountDownService.getInstance().addInformation(information);
	}

	public static void showInformationClearDialog(final Context context) {
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					clearInformations(context);
					break;
				}
				dialog.dismiss();
			}
		};
		DialogUtil.showInformationClearConfirmDialog(context, R.string.clear_infos_message, R.string.cancel, R.string.confirm, listener);
	}

	public static void deleteInformation(Context context, Information information) {
		PhotoTalkDatabaseFactory.getDatabase().deleteInformation(information);
		updateInformationState(context, Action.ACTION_INFORMATION_DELETE, information);
	}

	public static void logout(Context context) {
		doLogoutOperation(context);
		Intent intent = new Intent(context, HomeActivity.class);
		intent.setAction(Action.ACTION_LOGOUT);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(intent);
	}

	private static void doLogoutOperation(Context context) {
		PrefsUtils.LoginState.clearLoginInfo(context);
		FacebookUtil.clearFacebookVlidated(context);
		Request.executeLogoutAsync(context);
	}

	public static void relogin(Context context) {
		doLogoutOperation(context);
		Intent intent = new Intent(context, HomeActivity.class);
		intent.setAction(Action.ACTION_RELOGIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(intent);
	}

	public static void sendPhoto(final Context context, String timeLimit, List<Friend> friends, File file) {
		long flag = System.currentTimeMillis();
		try {
			UserInfo currentUser = ((PhotoTalkApplication) context.getApplicationContext()).getCurrentUser();
			List<String> friendIds = buildSendPhotoTempInformations(currentUser, friends, flag, Integer.parseInt(timeLimit), file);
			Request.sendPhoto(context, flag, file, timeLimit, new PhotoSendListener() {

				@Override
				public void onSendSuccess(long flag) {
					InformationPageController.getInstance().onPhotoSendSuccess(flag);
				}

				@Override
				public void onFail(long flag, int errorCode, String content) {
					InformationPageController.getInstance().onPhotoSendFail(flag);
				}

			}, friendIds);
		} catch (Exception e) {
			e.printStackTrace();
			InformationPageController.getInstance().onPhotoSendFail(flag);
		}
	}

	private static List<String> buildSendPhotoTempInformations(UserInfo currentUser, List<Friend> friends, long flag, int timeLimit, File file)
			throws JSONException {
		List<String> friendIds = new ArrayList<String>();
		List<Information> infoRecords = new ArrayList<Information>();
		for (Friend f : friends) {
			friendIds.add(f.getRcId());
			if (f.getRcId().equals(currentUser.getRcId()))
				continue;
			Information record = new Information();
			record.setCreatetime(flag);
			record.setReceiveTime(flag);
			// 发送者信息
			RecordUser user = new RecordUser();
			user.setHeadUrl(currentUser.getHeadUrl());
			user.setNick(currentUser.getNickName());
			user.setRcId(currentUser.getRcId());
			user.setTigaseId(currentUser.getTigaseId());
			record.setSender(user);
			// 接受者信息
			user = new RecordUser();
			user.setNick(f.getNickName());
			user.setHeadUrl(f.getHeadUrl());
			user.setRcId(f.getRcId());
			user.setTigaseId(f.getTigaseId());
			record.setReceiver(user);
			// 信息类型为发图，状态正在发送
			record.setType(InformationType.TYPE_PICTURE_OR_VIDEO);
			record.setStatu(InformationState.PhotoInformationState.STATU_NOTICE_SENDING_OR_LOADING);
			record.setTotleLength(timeLimit);
			record.setLimitTime(timeLimit);
			record.setUrl(file.getPath());
			infoRecords.add(record);
		}
		PhotoTalkDatabaseFactory.getDatabase().saveRecordInfos(infoRecords);
		InformationPageController.getInstance().sendPhotos(infoRecords);
		return friendIds;
	}

}
