package com.rcplatform.phototalk.api;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.entity.StringEntity;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.galhttprequest.LogUtil;
import com.rcplatform.phototalk.galhttprequest.RCPlatformServiceError;

public class RCPlatformAsyncHttpClient {
	private static final String CONTENT_TYPE_JSON = "application/json";
	private static final int TIME_OUT = 30 * 1000;
	private AsyncHttpClient mClient;
	private Map<String, String> mParams;
	private RequestParams mRequestParams;
	private RequestAction mAction;
	private boolean isCancel = false;

	public static enum RequestAction {
		FILE, JSON
	}

	public RCPlatformAsyncHttpClient(RequestAction action) {
		mAction = action;
		mClient = new AsyncHttpClient();
		mClient.setTimeout(TIME_OUT);
		if (action == RequestAction.JSON) {
			mParams = new HashMap<String, String>();
		} else if (action == RequestAction.FILE) {
			mRequestParams = new RequestParams();
		}
	}

	public void clearParams() {
		if (mAction == RequestAction.JSON)
			mParams.clear();
	}

	public void cancel() {
		isCancel = true;
	}

	public void post(final Context context, String url, final RCPlatformResponseHandler responseHandler) {
		try {
			mClient.post(context, url, getEntityFromParams(), CONTENT_TYPE_JSON, new AsyncHttpResponseHandler() {
				@Override
				public void onSuccess(int statusCode, String content) {
					// TODO Auto-generated method stub
					super.onSuccess(statusCode, content);
					LogUtil.e("response is " + content);
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
							// TODO Auto-generated catch block
							e.printStackTrace();
							onIOException(context, responseHandler);
						}
					}
				}

				@Override
				public void onFailure(Throwable error, String content) {
					// TODO Auto-generated method stub
					super.onFailure(error, content);
					LogUtil.e("response is " + content);
					if (responseHandler != null) {
						onIOException(context, responseHandler);
					}
				}
			});
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		mClient.post(context, url, mRequestParams, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, String content) {
				// TODO Auto-generated method stub
				super.onSuccess(statusCode, content);
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
						// TODO Auto-generated catch block
						e.printStackTrace();
						onIOException(context, responseHandler);
					}
				}
			}

			@Override
			public void onFailure(Throwable error, String content) {
				// TODO Auto-generated method stub
				super.onFailure(error, content);
				if (responseHandler != null) {
					onIOException(context, responseHandler);
				}
			}
		});

	}

	public void putRequestParam(String key, String value) {
		if (mAction == RequestAction.FILE)
			mRequestParams.put(key, value);
		else if (mAction == RequestAction.JSON)
			mParams.put(key, value);
	}

	public void cancel(Context context) {
		mClient.cancelRequests(context, true);
	}

	private StringEntity getEntityFromParams() throws Exception {
		JSONObject jsonObject = new JSONObject();
		for (String key : mParams.keySet()) {
			jsonObject.put(key, mParams.get(key));
		}
		LogUtil.e("request params is :" + jsonObject.toString());
		return new StringEntity(jsonObject.toString(),"UTF-8");
	}
}
