package com.rcplatform.phototalk.request;

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
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.galhttprequest.LogUtil;
import com.rcplatform.phototalk.utils.Constants.Action;

public class RCPlatformAsyncHttpClient {
	private static final String CONTENT_TYPE_JSON = "application/json";
	private static final int TIME_OUT = 5 * 1000;
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

	public void post(Request request) {
		mRequestParams = new RequestParams();
		try {
			if (request.getFile() != null) {
				postFile(request);
			} else {
				postRequest(request);
			}
		} catch (Exception e) {
			e.printStackTrace();
			onRequestFailure(request.getContext(), e, e.getMessage(), request);
		}
	}

	private void postRequest(final Request request) throws Exception {
		putAllRequestParams(request.getParams());
		final Context context = request.getContext();
		final RCPlatformResponseHandler responseHandler = request.getResponseHandler();
		LogUtil.e("request url is " + request.getUrl());
		mClient.post(context, request.getUrl(), getEntityFromParams(), CONTENT_TYPE_JSON, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, String content) {
				super.onSuccess(statusCode, content);
				LogUtil.e("response is " + content + "------ from url " + request.getUrl());
				if (responseHandler != null && !isCancel) {
					try {
						JSONObject jsonObject = new JSONObject(content);
						int state = jsonObject.getInt(RCPlatformResponse.ResponseStatus.RESPONSE_KEY_STATUS);
						if (state == RCPlatformResponse.ResponseStatus.RESPONSE_VALUE_SUCCESS) {
							onRequestSuccess(context, state, content, request);
						} else if (state == RCPlatformResponse.ResponseStatus.RESPONSE_NEED_LOGIN) {
							context.sendBroadcast(new Intent(Action.ACTION_OTHER_DEVICE_LOGIN));
						} else {
							responseHandler.onFailure(state, jsonObject.getString(RCPlatformResponse.ResponseStatus.RESPONSE_KEY_MESSAGE));
						}
					} catch (JSONException e) {
						e.printStackTrace();
						onFailure(e, content);
					}
				}
			}

			@Override
			public void onFailure(Throwable error, String content) {
				super.onFailure(error, content);
				LogUtil.e("response is " + content);
				onRequestFailure(context, error, content, request);
			}
		});
	}

	private void postFile(final Request request) throws FileNotFoundException {
		putAllRequestParams(request.getParams());
		mRequestParams.put("file", request.getFile());
		postNameValuePair(request);
	}

	public void postNameValue(Request request) {
		try {
			mRequestParams = new RequestParams();
			putAllRequestParams(request.getParams());
			if (request.getFile() != null)
				mRequestParams.put("file", request.getFile());
			postNameValuePair(request);
		} catch (Exception e) {
			e.printStackTrace();
			onRequestFailure(request.getContext(), e, e.getMessage(), request);
		}
	}

	private void postNameValuePair(final Request request) {
		final RCPlatformResponseHandler responseHandler = request.getResponseHandler();
		final Context context = request.getContext();
		mClient.post(request.getUrl(), mRequestParams, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, String content) {
				super.onSuccess(statusCode, content);
				LogUtil.e(content);
				if (responseHandler != null && !isCancel) {
					try {
						JSONObject jsonObject = new JSONObject(content);
						int state = jsonObject.getInt(RCPlatformResponse.ResponseStatus.RESPONSE_KEY_STATUS);
						if (state == RCPlatformResponse.ResponseStatus.RESPONSE_VALUE_SUCCESS) {
							onRequestSuccess(context, state, content, request);
						} else if (state == RCPlatformResponse.ResponseStatus.RESPONSE_NEED_LOGIN) {
							context.sendBroadcast(new Intent(Action.ACTION_OTHER_DEVICE_LOGIN));
						} else {
							responseHandler.onFailure(state, jsonObject.getString(RCPlatformResponse.ResponseStatus.RESPONSE_KEY_MESSAGE));
						}
					} catch (JSONException e) {
						e.printStackTrace();
						onFailure(e, content);
					}
				}
			}

			@Override
			public void onFailure(Throwable error, String content) {
				super.onFailure(error, content);
				onRequestFailure(context, error, content, request);
			}
		});
	}

	private void onRequestFailure(Context context, Throwable error, String content, Request request) {
		if (request.getResponseHandler() != null) {
			request.getResponseHandler().onFailure(RCPlatformServiceError.ERROR_CODE_REQUEST_FAIL, context.getString(R.string.net_error));
		}
	}

	private void onRequestSuccess(Context context, int state, String content, Request request) {
		if (request.isCache())
			PhotoTalkDatabaseFactory.getRequestDatabase().deleteRequest(request);
		if (request.getResponseHandler() != null)
			request.getResponseHandler().onSuccess(state, content);
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
