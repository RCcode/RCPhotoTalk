package com.rcplatform.phototalk.request.handler;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.rcplatform.phototalk.PhotoTalkApplication;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.InformationState;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.galhttprequest.LogUtil;
import com.rcplatform.phototalk.logic.MessageSender;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.request.RCPlatformServiceError;
import com.rcplatform.phototalk.request.inf.PhotoSendListener;
import com.rcplatform.phototalk.utils.RCThreadPool;

public class SendInformationResponseHandler implements RCPlatformResponseHandler {

	public SendInformationResponseHandler(UserInfo currentUser, List<String> friendIds, String timeLimit, boolean hasVoice,
			 int informationCate, Context context, File file, long flag, PhotoSendListener listener) {
		this.currentUser = currentUser;
		this.listener = listener;
		this.friendIds = friendIds;
		this.timeLimit = timeLimit;
		this.hasVoice = hasVoice;
		this.informationCate = informationCate;
		this.context = context;
		this.file = file;
		this.flag = flag;
	}

	private UserInfo currentUser;
	private PhotoSendListener listener;
	private List<String> friendIds;
	private String timeLimit;
	private boolean hasVoice;
	private int informationCate;
	private Context context;
	private File file;
	private long flag;

	@Override
	public void onFailure(int errorCode, String content) {
		listener.onFail(flag, errorCode, content);
		UserInfo currentUser = ((PhotoTalkApplication) context.getApplicationContext()).getCurrentUser();
		PhotoTalkDatabaseFactory.getDatabase().updateTempInformations(currentUser, null, flag, null, friendIds,
				InformationState.PhotoInformationState.STATU_NOTICE_SEND_OR_LOAD_FAIL, Integer.parseInt(timeLimit), hasVoice, informationCate);
	}

	@Override
	public void onSuccess(int statusCode, String content) {
		try {
			JSONObject jsonObject = new JSONObject(content);
			String informationUrl = jsonObject.getString("picUrl");
			List<String> userIds = buildUserIds(jsonObject.getJSONArray("users"));
			long flag = jsonObject.getLong("time");
			Map<String, Information> informations = PhotoTalkDatabaseFactory.getDatabase()
					.updateTempInformations(currentUser, informationUrl, flag, userIds, friendIds,
							InformationState.PhotoInformationState.STATU_NOTICE_SENDED_OR_NEED_LOADD, Integer.parseInt(timeLimit), hasVoice, informationCate);
			MessageSender.getInstance().sendInformation(context, informations, userIds);
			listener.onSendSuccess(flag, informationUrl);
			RCThreadPool.getInstance().addTask(new Runnable() {

				@Override
				public void run() {
					int count = PhotoTalkDatabaseFactory.getDatabase().getUnSendInformationCountByUrl(file.getPath());
					int driftCount = PhotoTalkDatabaseFactory.getDatabase().getUnSendDriftInformationCountByUrl(file.getPath());
					if (count == 0 && driftCount == 0) {
						LogUtil.e("delete temp files");
						file.delete();
					}
				}
			});
		} catch (JSONException e) {
			e.printStackTrace();
			onFailure(RCPlatformServiceError.ERROR_CODE_REQUEST_FAIL, content);
		}
	}

	private static List<String> buildUserIds(JSONArray array) throws JSONException {
		List<String> ids = new ArrayList<String>();
		if (array.length() > 0) {
			for (int i = 0; i < array.length(); i++) {
				String tcId = array.getString(i);
				ids.add(tcId);
			}
		}
		return ids;
	}
}
