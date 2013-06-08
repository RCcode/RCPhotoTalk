package com.rcplatform.videotalk.request;

import org.json.JSONException;
import org.json.JSONObject;

public class RCPlatformResponse {

	public static class ResponseStatus {
		public static final String RESPONSE_KEY_STATUS = "status";
		public static final String RESPONSE_KEY_MESSAGE = "message";
		public static final int RESPONSE_VALUE_SUCCESS = 10000;
		public static final int RESPONSE_NEED_LOGIN = -10003;

		public static boolean isRequestSuccess(String json) throws JSONException {
			JSONObject jsonObject = new JSONObject(json);
			return jsonObject.getInt(RESPONSE_KEY_STATUS) == RESPONSE_VALUE_SUCCESS;
		}
	}

	public static class CheckPhoneBindState {
		public static String RESPONSE_KEY_PHONE_NUMBER = "phone";
	}

	public static class Login {
		public static String RESPONSE_KEY_LAST_BIND_TIME = "time";
		public static String RESPONSE_KEY_LAST_BIND_NUMBER = "phone";
	}
}