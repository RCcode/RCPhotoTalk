package com.rcplatform.phototalk.logic;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.rcplatform.phototalk.HomeActivity;
import com.rcplatform.phototalk.MenueApplication;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.api.MenueApiUrl;
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
import com.rcplatform.phototalk.request.PhotoTalkParams;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.request.Request;
import com.rcplatform.phototalk.utils.Contract;
import com.rcplatform.phototalk.utils.Contract.Action;
import com.rcplatform.phototalk.utils.DialogUtil;
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

		if (((MenueApplication) context.getApplicationContext()).getCurrentUser().getRcId().equals(record.getSender().getRcId())
				&& !record.getReceiver().getRcId().equals(record.getSender().getRcId()))
			return true;
		return false;
	}

	public static void informationFriendAdded(Context context, Information information, Friend friend) {
		if (information.getType() == InformationType.TYPE_FRIEND_REQUEST_NOTICE) {
			PhotoTalkDatabaseFactory.getDatabase().updateInformationState(information);
			MessageSender.sendInformation(context, friend.getTigaseId(), information);
		}
	}

	public static void friendAdded(Context context, Friend friend, int addType) {
		UserInfo currentUser = ((MenueApplication) context.getApplicationContext()).getCurrentUser();
		long createTime = System.currentTimeMillis();
		Information information = null;
		if (addType == Contract.FriendAddType.ADD_FRIEND_PASSIVE) {
			RecordUser sender = new RecordUser(friend.getRcId(), friend.getNickName(), friend.getHeadUrl(), friend.getTigaseId());
			RecordUser receiver = new RecordUser(currentUser.getRcId(), currentUser.getNickName(), currentUser.getHeadUrl(), currentUser.getTigaseId());
			information = MessageSender.createInformation(InformationType.TYPE_FRIEND_REQUEST_NOTICE,
					InformationState.FriendRequestInformationState.STATU_QEQUEST_ADD_CONFIRM, sender, receiver, createTime);
			PhotoTalkDatabaseFactory.getDatabase().updateFriendRequestInformationByFriend(friend);
		} else if (addType == Contract.FriendAddType.ADD_FRIEND_ACTIVE) {
			RecordUser receiver = new RecordUser(friend.getRcId(), friend.getNickName(), friend.getHeadUrl(), friend.getTigaseId());
			RecordUser sender = new RecordUser(currentUser.getRcId(), currentUser.getNickName(), currentUser.getHeadUrl(), currentUser.getTigaseId());
			information = MessageSender.createInformation(InformationType.TYPE_FRIEND_REQUEST_NOTICE,
					InformationState.FriendRequestInformationState.STATU_QEQUEST_ADD_REQUEST, sender, receiver, createTime);
			List<Information> infos = new ArrayList<Information>();
			infos.add(information);
			PhotoTalkDatabaseFactory.getDatabase().saveRecordInfos(infos);
		}
		if (information != null) {
			MessageSender.sendInformation(context, friend.getTigaseId(), information);
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
	}

	public static void relogin(Context context) {
		doLogoutOperation(context);
		Intent intent = new Intent(context, HomeActivity.class);
		intent.setAction(Action.ACTION_RELOGIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(intent);
	}

	public static void sendPhoto(final Context context, String timeLimit, List<Friend> friends, File file) {
		try {
			long flag = System.currentTimeMillis();
			UserInfo currentUser = ((MenueApplication) context.getApplicationContext()).getCurrentUser();
			String userArray = buildSendPhotoTempInformations(currentUser, friends, flag, Integer.parseInt(timeLimit));
			RCPlatformResponseHandler responseHandler = new RCPlatformResponseHandler() {

				@Override
				public void onSuccess(int statusCode, String content) {
					try {
						JSONObject jsonObject = new JSONObject(content);
						String informationUrl = jsonObject.getString("picUrl");
						Map<String, String> userIds = buildUserIds(jsonObject.getJSONArray("users"));
						long flag = jsonObject.getLong("time");
						UserInfo currentUser = ((MenueApplication) context.getApplicationContext()).getCurrentUser();
						Map<String, Information> informations = PhotoTalkDatabaseFactory.getDatabase().updateTempInformations(currentUser, informationUrl,
								flag, userIds);
						InformationPageController.getInstance().photosSendSuccess(flag);
						MessageSender.sendInformation(context, informations, userIds);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

				@Override
				public void onFailure(int errorCode, String content) {

				}
			};
			Request request = new Request(context, MenueApiUrl.SEND_PICTURE_URL, responseHandler);
			PhotoTalkParams.buildBasicParams(context, request);
			request.setFile(file);
			request.putParam(PhotoTalkParams.SendPhoto.PARAM_KEY_FLAG, flag + "");
			request.putParam(PhotoTalkParams.SendPhoto.PARAM_KEY_TIME_LIMIT, timeLimit);
			request.putParam(PhotoTalkParams.SendPhoto.PARAM_KEY_USERS, userArray);
			request.setCache(true);
			request.excuteAsync();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Map<String, String> buildUserIds(JSONArray array) throws JSONException {
		Map<String, String> ids = new HashMap<String, String>();
		if (array.length() > 0) {
			for (int i = 0; i < array.length(); i++) {
				JSONObject jsonObject = array.getJSONObject(i);
				String suid = jsonObject.getString("suid");
				String tigaseId = jsonObject.getString("tigaseId");
				ids.put(suid, tigaseId);
			}
		}
		return ids;
	}

	private static String buildSendPhotoTempInformations(UserInfo currentUser, List<Friend> friends, long flag, int timeLimit) throws JSONException {
		JSONArray array = new JSONArray();
		List<Information> infoRecords = new ArrayList<Information>();
		for (Friend f : friends) {

			JSONObject jsonObject = new JSONObject();
			jsonObject.put(PhotoTalkParams.SendPhoto.PARAM_KEY_RECEIVER_ID, f.getRcId());
			array.put(jsonObject);
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
			record.setSender(user);
			// 接受者信息
			user = new RecordUser();
			user.setNick(f.getNickName());
			user.setHeadUrl(f.getHeadUrl());
			user.setRcId(f.getRcId());
			record.setReceiver(user);
			// 信息类型为发图，状态正在发送
			record.setType(InformationType.TYPE_PICTURE_OR_VIDEO);
			record.setStatu(InformationState.PhotoInformationState.STATU_NOTICE_SENDING);
			record.setTotleLength(timeLimit);
			record.setLimitTime(timeLimit);
			infoRecords.add(record);
		}
		PhotoTalkDatabaseFactory.getDatabase().saveRecordInfos(infoRecords);
		InformationPageController.getInstance().sendPhotos(infoRecords);
		return array.toString();
	}

}
