package com.rcplatform.phototalk.logic;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.json.JSONException;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.google.android.gcm.GCMRegistrar;
import com.rcplatform.phototalk.HomeActivity;
import com.rcplatform.phototalk.PhotoTalkApplication;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.InformationCategory;
import com.rcplatform.phototalk.bean.InformationState;
import com.rcplatform.phototalk.bean.InformationType;
import com.rcplatform.phototalk.bean.RecordUser;
import com.rcplatform.phototalk.bean.ServiceCensus;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.clienservice.CensusService;
import com.rcplatform.phototalk.clienservice.InviteFriendUploadService;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.db.impl.FriendDynamicDatabase;
import com.rcplatform.phototalk.drift.DriftInformation;
import com.rcplatform.phototalk.drift.DriftSender;
import com.rcplatform.phototalk.logic.controller.DriftInformationPageController;
import com.rcplatform.phototalk.logic.controller.InformationPageController;
import com.rcplatform.phototalk.proxy.DriftProxy;
import com.rcplatform.phototalk.proxy.InformationProxy;
import com.rcplatform.phototalk.request.PhotoTalkParams;
import com.rcplatform.phototalk.request.Request;
import com.rcplatform.phototalk.request.handler.ThrowDriftResponseHandler;
import com.rcplatform.phototalk.request.inf.PhotoSendListener;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.Constants.Action;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.NotificationSender;
import com.rcplatform.phototalk.utils.PhotoTalkUtils;
import com.rcplatform.phototalk.utils.PrefsUtils;
import com.rcplatform.phototalk.utils.Utils;

public class LogicUtils {

	public static void serviceCensus(Context context, Information... infos) {
		Intent intent = new Intent(context, CensusService.class);
		if (infos.length > 0) {
			ServiceCensus[] census = new ServiceCensus[infos.length];
			for (int i = 0; i < infos.length; i++) {
				Information info = infos[i];
				census[i] = new ServiceCensus(info.getSender().getRcId(), info.getUrl());
			}
			intent.putExtra(CensusService.PARAM_KEY_INFORMATION, census);
		}
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

	public static boolean isSender(Context context, DriftInformation record) {

		return ((PhotoTalkApplication) context.getApplicationContext()).getCurrentUser().getRcId().equals(record.getSender().getRcId());
	}

	public static void informationFriendAdded(Context context, Information information, Friend friend) {
		if (information.getType() == InformationType.TYPE_FRIEND_REQUEST_NOTICE) {
			PhotoTalkDatabaseFactory.getDatabase().updateInformationState(information);
			// MessageSender.sendInformation(context, friend.getTigaseId(),
			// friend.getRcId(), information);
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
			InformationPageController.getInstance().friendAdded(information, addType);
		}
	}

	public static void friendAlreadyAdded(Information information) {
		information.setStatu(InformationState.FriendRequestInformationState.STATU_QEQUEST_ADD_CONFIRM);
		PhotoTalkDatabaseFactory.getDatabase().updateFriendInformationState(information);
		InformationPageController.getInstance().onFriendAlreadyAdded(information);
	}

	public static void clearInformations(Context context) {
		PhotoTalkDatabaseFactory.getDatabase().clearInformation();
		InformationPageController.getInstance().clearInformations();
	}

	public static void startShowPhotoInformation(Information information) {
		PhotoInformationCountDownService.getInstance().addInformation(information);
	}

	public static void startShowPhotoInformation(DriftInformation information) {
		DriftInformationCountDownService.getInstance().addInformation(information);
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
		DialogUtil.showInformationClearConfirmDialog(context, R.string.clear_infos_message, R.string.cancel, R.string.ok, listener);
	}

	public static void deleteInformation(Context context, Information information) {
		PhotoTalkDatabaseFactory.getDatabase().deleteInformation(information);
	}

	public static void deleteInformation(Context context, DriftInformation information) {
		PhotoTalkDatabaseFactory.getDatabase().deleteDriftInformation(information);
	}

	public static void logout(Context context) {
		// 清空好友动态
		FriendDynamicDatabase.getInstance().clearAll();
		// 本机注销gcm user key
		UserInfo userInfo = PrefsUtils.LoginState.getLoginUser(context.getApplicationContext());

		if (userInfo != null) {
			GCMRegistrar.setRegisteredOnServer(context, false, userInfo.getRcId());
		}
		doLogoutOperation(context);
		Intent intent = new Intent(context, HomeActivity.class);
		intent.setAction(Action.ACTION_LOGOUT);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(intent);
	}

	private static void doLogoutOperation(Context context) {
		PrefsUtils.LoginState.clearLoginInfo(context);
		Request.executeLogoutAsync(context);
	}

	public static void relogin(Context context) {
		doLogoutOperation(context);
		Intent intent = new Intent(context, HomeActivity.class);
		intent.setAction(Action.ACTION_RELOGIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(intent);
	}

	private static void showInformationStateNofitication(Context context, String notifyTitle, String notifyText, long flag, int notificationId, Intent intent,
			boolean cancelAble) {
		SendingInformationManager.getInstance().addSendingInformation(flag, notificationId);
		NotificationSender.getInstance(context).sendNotification(notifyTitle, notifyText, R.drawable.notification_icon, intent, notificationId, cancelAble);
	}

	public static void sendPhoto(final Context context, final String timeLimit, List<Friend> friends, final File file, final boolean hasVoice,
			final boolean hasGraf, boolean hasText, int photoType, final int informationCate) {
		long flag = System.currentTimeMillis();
		UserInfo currentUser = ((PhotoTalkApplication) context.getApplicationContext()).getCurrentUser();
		final boolean sendToStranges = friends.contains(PhotoTalkUtils.getDriftFriend());
		if (sendToStranges) {
			DriftInformation tempDriftInformation = buildDriftTempInformation(currentUser, flag, Integer.parseInt(timeLimit), file, hasVoice, hasGraf,
					informationCate);
			PhotoTalkDatabaseFactory.getDatabase().saveDriftInformation(tempDriftInformation);
			DriftInformationPageController.getInstance().onDriftInformationSending(Arrays.asList(new DriftInformation[] { tempDriftInformation }));
			if (informationCate == InformationCategory.VIDEO)
				showInformationStateNofitication(context, context.getString(R.string.sending_to_stranger), context.getString(R.string.send_sending), flag,
						SendingInformationManager.getInstance().getDriftNotificationId(), PhotoTalkUtils.getNotificationDriftInformationIntent(context), false);
		}
		if (friends.size() == 1 && sendToStranges) {
			// 只是扔漂流瓶
			DriftProxy.throwDriftInformation(context, new ThrowDriftResponseHandler(context, flag, file.getPath(), informationCate), currentUser, null,
					timeLimit, hasGraf, hasVoice, file.getPath(), flag, informationCate);
			return;
		}
		final StringBuilder sbNotifyTitle = new StringBuilder();
		for (Friend friend : friends) {
			if (Friend.DRIFT_FRIEND_RCID.equals(friend.getRcId()) || currentUser.getRcId().equals(friend.getRcId()))
				continue;
			sbNotifyTitle.append(friend.getNickName()).append(",");
		}
		// 发送给好友并扔漂流瓶
		String title = null;
		if (sbNotifyTitle.length() > 0)
			title = context.getString(R.string.sending_video_to, sbNotifyTitle.substring(0, sbNotifyTitle.length() - 1));
		final String notificationTitle = title;
		try {
			List<String> friendIds = buildSendPhotoTempInformations(currentUser, friends, flag, Integer.parseInt(timeLimit), file, hasVoice, hasGraf, hasText,
					photoType, informationCate);
			int notificationId = 0;
			if (informationCate == InformationCategory.VIDEO && notificationTitle != null) {
				notificationId = SendingInformationManager.getInstance().getNotificationId(flag);
				showInformationStateNofitication(context, notificationTitle, context.getString(R.string.send_sending), flag, notificationId,
						PhotoTalkUtils.getNotificationNormalIntent(context), false);
			}
			InformationProxy.sendInformation(context, flag, file, timeLimit, friendIds, hasVoice, hasGraf, hasText, informationCate, new PhotoSendListener() {

				@Override
				public void onSendSuccess(long flag, String url) {
					if (informationCate == InformationCategory.VIDEO && notificationTitle != null) {
						int notificationId = SendingInformationManager.getInstance().getNotificationId(flag);
						videoInformationSendSuccess(context, notificationTitle, flag, notificationId);
					}
					InformationPageController.getInstance().onPhotoSendSuccess(flag);
					if (sendToStranges) {
						// 发送给好友后判断是否需要扔漂流瓶
						DriftProxy.throwDriftInformation(context, new ThrowDriftResponseHandler(context, flag, file.getPath(), informationCate),
								((PhotoTalkApplication) context.getApplicationContext()).getCurrentUser(), null, timeLimit, hasGraf, hasVoice, file.getPath(),
								flag, informationCate);
					}
				}

				@Override
				public void onFail(long flag, int errorCode, String content) {
					InformationPageController.getInstance().onPhotoSendFail(flag);
					if (informationCate == InformationCategory.VIDEO && notificationTitle != null) {
						int notificationId = SendingInformationManager.getInstance().getNotificationId(flag);
						showInformationStateNofitication(context, notificationTitle, context.getString(R.string.send_video_fail), flag, notificationId,
								PhotoTalkUtils.getNotificationNormalIntent(context), true);
					}
					if (sendToStranges) {
						// 发送给好友失败后判断是否需要扔漂流瓶，如果需要扔，也尝试下
						DriftProxy.throwDriftInformation(context, new ThrowDriftResponseHandler(context, flag, file.getPath(), informationCate),
								((PhotoTalkApplication) context.getApplicationContext()).getCurrentUser(), null, timeLimit, hasGraf, hasVoice, file.getPath(),
								flag, informationCate);
					}
				}

			});
		} catch (Exception e) {
			e.printStackTrace();
			InformationPageController.getInstance().onPhotoSendFail(flag);
			if (sendToStranges)
				DriftInformationPageController.getInstance().onDriftInformationSendFail(flag);
		}
	}

	private static DriftInformation buildDriftTempInformation(UserInfo currentUser, long flag, int timeLimit, File file, boolean hasVoice, boolean hasGraf,
			int informationCate) {
		DriftInformation tempDriftInformation = new DriftInformation();
		tempDriftInformation.setFlag(flag);
		if (hasVoice) {
			tempDriftInformation.setHasVoice(Integer.parseInt(PhotoTalkParams.PARAM_VALUE_CONFIRM));
		} else {
			tempDriftInformation.setHasVoice(Integer.parseInt(PhotoTalkParams.PARAM_VALUE_NEGATE));
		}
		if (hasGraf) {
			tempDriftInformation.setHasGraf(Integer.parseInt(PhotoTalkParams.PARAM_VALUE_CONFIRM));
		} else {
			tempDriftInformation.setHasGraf(Integer.parseInt(PhotoTalkParams.PARAM_VALUE_NEGATE));
		}
		tempDriftInformation.setLimitTime(timeLimit);
		tempDriftInformation.setReceiveTime(flag);
		DriftSender sender = new DriftSender();
		sender.setAppId(Integer.parseInt(Constants.APP_ID));
		sender.setCountry(currentUser.getCountry());
		sender.setGender(currentUser.getGender());
		sender.setHeadUrl(currentUser.getHeadUrl());
		sender.setNick(currentUser.getNickName());
		sender.setRcId(currentUser.getRcId());
		sender.setTigaseId(currentUser.getTigaseId());
		tempDriftInformation.setSender(sender);
		tempDriftInformation.setReceiveTime(flag);
		tempDriftInformation.setState(InformationState.PhotoInformationState.STATU_NOTICE_SENDING_OR_LOADING);
		tempDriftInformation.setUrl(file.getPath());
		tempDriftInformation.setType(informationCate);
		return tempDriftInformation;
	}

	private static List<String> buildSendPhotoTempInformations(UserInfo currentUser, List<Friend> friends, long flag, int timeLimit, File file,
			boolean hasVoice, boolean hasGraf, boolean hasText, int photoType, int informationCate) throws JSONException {
		List<String> friendIds = new ArrayList<String>();
		List<Information> infoRecords = new ArrayList<Information>();
		for (Friend f : friends) {
			if (f.equals(PhotoTalkUtils.getDriftFriend()))
				continue;
			friendIds.add(f.getRcId());
			if (f.getRcId().equals(currentUser.getRcId()))
				continue;
			Information record = new Information();
			record.setInformationCate(informationCate);
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
			record.setHasVoice(hasVoice);
			record.setHasGraf(hasGraf);
			record.setHasText(hasText);
			record.setPhotoType(photoType);
			infoRecords.add(record);
		}
		PhotoTalkDatabaseFactory.getDatabase().saveRecordInfos(infoRecords);
		InformationPageController.getInstance().sendPhotos(infoRecords);
		return friendIds;
	}

	public static boolean isAttentionThrowDrift(Context context, String rcId) {
		long lastFishTime = PrefsUtils.User.getLastFishTime(context, rcId);
		long currentTime = System.currentTimeMillis();
		if (!Utils.isSameDay(currentTime, lastFishTime)) {
			PrefsUtils.User.setTodayFishTime(context, rcId, 0);
			return false;
		}
		return !PrefsUtils.User.isThrowToday(context, rcId) && PrefsUtils.User.getTodayFishTime(context, rcId) >= Constants.UNTHROW_FISH_ALLOW_TIME;
	}

	public static void resendInformation(final Context context, final Information information) {
		List<String> friendIds = new ArrayList<String>();
		friendIds.add(information.getReceiver().getRcId());
		final int informationCate = information.getInformationCate();
		final String notificationTitle = context.getString(R.string.sending_video_to, information.getReceiver().getNick());
		InformationProxy.sendInformation(context, information.getCreatetime(), new File(information.getUrl()), Integer.toString(information.getTotleLength()),
				friendIds, information.isHasVoice(), information.isHasGraf(), information.isHasText(), informationCate, new PhotoSendListener() {

					@Override
					public void onSendSuccess(long flag, String url) {
						InformationPageController.getInstance().onPhotoResendSuccess(information);
						videoInformationSendSuccess(context, notificationTitle, flag, SendingInformationManager.getInstance().getNotificationId(flag));
					}

					@Override
					public void onFail(long flag, int errorCode, String content) {
						InformationPageController.getInstance().onPhotoResendFail(information);
						if (informationCate == InformationCategory.VIDEO) {
							int notificationId = SendingInformationManager.getInstance().getNotificationId(flag);
							showInformationStateNofitication(context, notificationTitle, context.getString(R.string.send_fail), flag, notificationId,
									PhotoTalkUtils.getNotificationNormalIntent(context), true);
						}
					}
				});
		if (information.getInformationCate() == InformationCategory.VIDEO) {
			showInformationStateNofitication(context, notificationTitle, context.getString(R.string.send_sending), information.getCreatetime(),
					SendingInformationManager.getInstance().getNotificationId(information.getCreatetime()),
					PhotoTalkUtils.getNotificationNormalIntent(context), false);
		}
	}

	private static void videoInformationSendSuccess(Context context, String notificationTitle, long flag, int notificationId) {
		Intent intent = null;
		if (notificationId == SendingInformationManager.getInstance().getDriftNotificationId()) {
			intent = PhotoTalkUtils.getNotificationDriftInformationIntent(context);
		} else {
			intent = PhotoTalkUtils.getNotificationNormalIntent(context);
		}
		showInformationStateNofitication(context, notificationTitle, context.getString(R.string.send_success), flag, notificationId, intent, false);
		NotificationSender.getInstance(context).cancelNotification(notificationId, Constants.TimeMillins.SEND_SUCCESS_NOTIFICATION_SHOW_TIME);
	}
}
