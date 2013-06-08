package com.rcplatform.videotalk.clienservice;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.IntentService;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;

import com.rcplatform.videotalk.PhotoTalkApplication;
import com.rcplatform.videotalk.R;
import com.rcplatform.videotalk.api.PhotoTalkApiUrl;
import com.rcplatform.videotalk.bean.FriendType;
import com.rcplatform.videotalk.bean.UserInfo;
import com.rcplatform.videotalk.galhttprequest.LogUtil;
import com.rcplatform.videotalk.request.PhotoTalkParams;
import com.rcplatform.videotalk.utils.Constants;

public class InviteFriendUploadService extends IntentService {
	private static final String SERVICE_NAME = "invite_friend_upload";
	public static final String PARAM_FRIENDS_IDS = "third_friends_ids";
	public static final String PARAM_TYPE = "type";
	private int type;

	public InviteFriendUploadService() {
		super(SERVICE_NAME);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String action = intent.getAction();
		if (action.equals(Constants.Action.ACTION_UPLOAD_INTITE_CONTACT)) {
			type = FriendType.CONTACT;
			asyncInviteInfoContact();
		} else if (action.equals(Constants.Action.ACTION_UPLOAD_INTITE_THIRDPART)) {
			String[] ids = (String[]) intent.getSerializableExtra(PARAM_FRIENDS_IDS);
			type = intent.getIntExtra(PARAM_TYPE, 0);
			asyncInviteInfoThirdPart(ids);
		}
	}
	private void asyncInviteInfoContact() {
		List<String> numbers = new ArrayList<String>();
		Uri uri = Uri.parse("content://sms/sent");
		String[] projection = new String[] { "address", "body" };
		Cursor cursor = getContentResolver().query(uri, projection, null, null, "date desc");
		String inviteMessage = getString(R.string.invite_msg_match);
		while (cursor.moveToNext()) {
			String body = cursor.getString(cursor.getColumnIndex("body"));
			if (body.contains(inviteMessage)) {
				String number = cursor.getString(cursor.getColumnIndex("address"));
				numbers.add(number);
			}
		}
		cursor.close();
		if (numbers.size() > 0) {
			try {
				sendRequest(getEntity(numbers));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private HttpEntity getEntity(List<String> ids) throws JSONException, UnsupportedEncodingException {
		UserInfo userInfo = ((PhotoTalkApplication) getApplication()).getCurrentUser();
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(PhotoTalkParams.PARAM_KEY_TOKEN, userInfo.getToken());
		jsonObject.put(PhotoTalkParams.PARAM_KEY_APP_ID, PhotoTalkParams.PARAM_VALUE_APP_ID);
		jsonObject.put(PhotoTalkParams.PARAM_KEY_LANGUAGE, PhotoTalkParams.PARAM_VALUE_LANGUAGE);
		jsonObject.put(PhotoTalkParams.PARAM_KEY_DEVICE_ID, PhotoTalkParams.PARAM_VALUE_DEVICE_ID);
		jsonObject.put(PhotoTalkParams.PARAM_KEY_USER_ID, userInfo.getRcId());
		jsonObject.put(PhotoTalkParams.SyncInviteInfo.PARAM_KEY_TYPE, type + "");
		JSONArray array = new JSONArray();
		for (String id : ids) {
			array.put(id);
		}
		jsonObject.put(PhotoTalkParams.SyncInviteInfo.PARAM_KEY_INVITED_IDS, array);
		return new StringEntity(jsonObject.toString(), "UTF-8");
	}

	private void sendRequest(HttpEntity entity) {
		HttpPost post = new HttpPost(PhotoTalkApiUrl.ASYNC_INVITE_URL);
		post.setEntity(entity);
		try {
			HttpResponse res = new DefaultHttpClient().execute(post);
			LogUtil.e(res.getStatusLine().getStatusCode() + "");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void asyncInviteInfoThirdPart(String... ids) {
		if (ids.length > 0) {
			try {
				sendRequest(getEntity(Arrays.asList(ids)));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}