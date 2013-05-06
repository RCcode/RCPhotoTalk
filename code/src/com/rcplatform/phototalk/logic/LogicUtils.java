package com.rcplatform.phototalk.logic;

import android.content.Context;
import android.content.Intent;

import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.ServiceSimpleNotice;
import com.rcplatform.phototalk.clienservice.InformationStateChangeService;
import com.rcplatform.phototalk.clienservice.InviteFriendUploadService;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
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
}
