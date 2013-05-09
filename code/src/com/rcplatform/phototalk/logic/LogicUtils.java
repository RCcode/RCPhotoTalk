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
import com.rcplatform.phototalk.request.FileRequest;
import com.rcplatform.phototalk.request.JSONConver;
import com.rcplatform.phototalk.request.PhotoTalkParams;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
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
				ssns[i] = new ServiceSimpleNotice(info.getStatu() + "", info.getRecordId(), info.getType() + "", info.getLastUpdateTime());
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
			// if (isNeedToNotifyServiceOverInformation(context, serviceInfo)) {
			// updateInformationState(context, Action.ACTION_INFORMATION_OVER,
			// serviceInfo);
			// }
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

	public static void informationFriendAdded(Information information) {
		if (information.getType() == InformationType.TYPE_FRIEND_REQUEST_NOTICE) {
			information.setStatu(InformationState.STATU_QEQUEST_ADDED);
			PhotoTalkDatabaseFactory.getDatabase().updateInformationState(information);
		}
	}

	public static void friendAdded(Friend friend) {
		PhotoTalkDatabaseFactory.getDatabase().updateFriendRequestInformationByFriend(friend);
		InformationPageController.getInstance().friendAdded(friend);
	}

	public static void clearInformations(Context context) {
		updateInformationState(context, Action.ACTION_INFORMATION_DELETE);
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

	public static void sendPhoto(Context context, String timeLimit, List<Friend> friends, File file) {
		try {
			long flag = System.currentTimeMillis();
			UserInfo currentUser = ((MenueApplication) context.getApplicationContext()).getCurrentUser();
			String userArray = buildSendPhotoTempInformations(currentUser, friends, flag);
			FileRequest request = new FileRequest();
			request.setFile(file);
			PhotoTalkParams.buildBasicParams(context, request);
			request.putParam(PhotoTalkParams.SendPhoto.PARAM_KEY_FLAG, flag + "");
			request.putParam(PhotoTalkParams.SendPhoto.PARAM_KEY_TIME_LIMIT, timeLimit);
			request.putParam(PhotoTalkParams.SendPhoto.PARAM_KEY_USERS, userArray);
			request.setResponseHandler(new RCPlatformResponseHandler() {

				@Override
				public void onSuccess(int statusCode, String content) {
					try {
						JSONObject jsonObject = new JSONObject(content);
						List<Information> infos = JSONConver.jsonToInformations(jsonObject.getJSONArray("noticeList").toString());
						Map<String, Information> serviceInformations = new HashMap<String, Information>();
						long flag = jsonObject.getLong("time");
						Information infoSelf = null;
						for (Information info : infos) {
							if (info.getReceiver().getSuid().equals(info.getSender().getSuid())) {
								infoSelf = info;
								continue;
							}
							info.setReceiveTime(flag);
							serviceInformations.put(info.getReceiver().getSuid(), info);
						}
						if (infoSelf != null)
							infos.remove(infoSelf);
						PhotoTalkDatabaseFactory.getDatabase().updateTempInformations(infos, flag);
						InformationPageController.getInstance().photosSendSuccess(serviceInformations, flag);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}

				@Override
				public void onFailure(int errorCode, String content) {

				}
			});
			request.setUrl(MenueApiUrl.SEND_PICTURE_URL);
			((MenueApplication) context.getApplicationContext()).getWebService().postRequest(request);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static String buildSendPhotoTempInformations(UserInfo currentUser, List<Friend> friends, long flag) throws JSONException {
		JSONArray array = new JSONArray();
		List<Information> infoRecords = new ArrayList<Information>();
		for (Friend f : friends) {

			JSONObject jsonObject = new JSONObject();
			jsonObject.put(PhotoTalkParams.SendPhoto.PARAM_KEY_RECEIVER_ID, f.getSuid());
			array.put(jsonObject);
			if (f.getSuid().equals(currentUser.getSuid()))
				continue;
			Information record = new Information();
			record.setRecordId(Contract.TEMP_INFORMATION_ID + record.hashCode());
			record.setCreatetime(flag);
			record.setReceiveTime(flag);
			// 发送者信息
			RecordUser user = new RecordUser();
			user.setHeadUrl(currentUser.getHeadUrl());
			user.setNick(currentUser.getNick());
			user.setSuid(currentUser.getSuid());
			record.setSender(user);
			// 接受者信息
			user = new RecordUser();
			user.setNick(f.getNick());
			user.setHeadUrl(f.getHeadUrl());
			user.setSuid(f.getSuid());
			record.setReceiver(user);
			// 信息类型为发图，状态正在发送
			record.setType(InformationType.TYPE_PICTURE_OR_VIDEO);
			record.setStatu(InformationState.STATU_NOTICE_SENDING);
			infoRecords.add(record);
		}
		PhotoTalkDatabaseFactory.getDatabase().saveRecordInfos(infoRecords);
		InformationPageController.getInstance().sendPhotos(infoRecords);
		return array.toString();
	}
}
