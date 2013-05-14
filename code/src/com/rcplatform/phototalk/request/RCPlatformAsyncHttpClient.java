package com.rcplatform.phototalk.request;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Map;

import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.galhttprequest.LogUtil;
import com.rcplatform.phototalk.galhttprequest.RCPlatformServiceError;
import com.rcplatform.phototalk.utils.Contract.Action;

public class RCPlatformAsyncHttpClient {
	private static final String CONTENT_TYPE_JSON = "application/json";
	private static final int TIME_OUT = 30 * 1000;
	private AsyncHttpClient mClient;
	private RequestParams mRequestParams;
	private boolean isCancel = false;

	public static enum RequestAction {
		FILE, JSON
	}

	public RCPlatformAsyncHttpClient() {
		mClient = new AsyncHttpClient();
		mClient.setTimeout(TIME_OUT);
		mRequestParams = new RequestParams();
	}

	public void clearParams() {
		mRequestParams = new RequestParams();
	}

	public void cancel() {
		isCancel = true;
	}

	public void post(final Context context, String url, final RCPlatformResponseHandler responseHandler) {
		try {
			mClient.post(context, url, getEntityFromParams(), CONTENT_TYPE_JSON, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(int statusCode, String content) {
					super.onSuccess(statusCode, content);
					LogUtil.e("response is " + content);
					if (responseHandler != null && !isCancel) {
						try {
							JSONObject jsonObject = new JSONObject(content);
							int state = jsonObject.getInt(RCPlatformResponse.ResponseStatus.RESPONSE_KEY_STATUS);
							if (state == RCPlatformResponse.ResponseStatus.RESPONSE_VALUE_SUCCESS) {
								responseHandler.onSuccess(state, content);
							} else if (state == RCPlatformResponse.ResponseStatus.RESPONSE_NEED_LOGIN) {
								context.sendBroadcast(new Intent(Action.ACTION_OTHER_DEVICE_LOGIN));
							} else {
								responseHandler.onFailure(state, jsonObject.getString(RCPlatformResponse.ResponseStatus.RESPONSE_KEY_MESSAGE));
							}
						} catch (JSONException e) {
							e.printStackTrace();
							onIOException(context, responseHandler);
						}
					}
				}

				@Override
				public void onFailure(Throwable error, String content) {
					super.onFailure(error, content);
					LogUtil.e("response is " + content);
					if (responseHandler != null) {
						onIOException(context, responseHandler);
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			if (responseHandler != null)
				responseHandler.onFailure(RCPlatformServiceError.ERROR_CODE_REQUEST_FAIL, context.getString(R.string.net_error));
		}
		clearParams();
	}

	private void onIOException(Context context, RCPlatformResponseHandler responseHandler) {
		responseHandler.onFailure(RCPlatformServiceError.ERROR_CODE_REQUEST_FAIL, context.getString(R.string.net_error));
	}

	public void postFile(final Context context, String url, File file, final RCPlatformResponseHandler responseHandler) {
		if (file != null && file.exists()) {
			try {
				mRequestParams.put("file", file);
				mRequestParams.put("imgType", "jpg");
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		mClient.post(context, url, mRequestParams, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, String content) {
				super.onSuccess(statusCode, content);
				LogUtil.e(content);
				if (responseHandler != null && !isCancel) {
					try {
						JSONObject jsonObject = new JSONObject(content);
						int state = jsonObject.getInt(RCPlatformResponse.ResponseStatus.RESPONSE_KEY_STATUS);
						if (state == RCPlatformResponse.ResponseStatus.RESPONSE_VALUE_SUCCESS) {
							responseHandler.onSuccess(state, content);
						} else {
							responseHandler.onFailure(state, null);
						}
					} catch (JSONException e) {
						e.printStackTrace();
						onIOException(context, responseHandler);
					}
				}
			}

			@Override
			public void onFailure(Throwable error, String content) {
				super.onFailure(error, content);
				LogUtil.e(content);
				if (responseHandler != null) {
					onIOException(context, responseHandler);
				}
			}
		});

	}

	public void putRequestParam(String key, String value) {
		mRequestParams.put(key, value);
	}

	public void putAllRequestParams(Map<String, String> params) {
		for (String key : params.keySet()) {
			mRequestParams.put(key, params.get(key));
		}
	}

	public void cancel(Context context) {
		mClient.cancelRequests(context, true);
	}

	private StringEntity getEntityFromParams() throws Exception {
		JSONObject jsonObject = new JSONObject();
		List<BasicNameValuePair> params = mRequestParams.getParamsList();
		for (BasicNameValuePair pair : params) {
			jsonObject.put(pair.getName(), pair.getValue());
		}
		LogUtil.e("request params is :" + jsonObject.toString());
		return new StringEntity(jsonObject.toString(), "UTF-8");
	}
}
