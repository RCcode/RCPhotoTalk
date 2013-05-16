package com.rcplatform.phototalk.db.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.AndroidSupport;
import com.db4o.config.EmbeddedConfiguration;
import com.rcplatform.phototalk.api.MenueApiUrl;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.db.DatabaseUtils;
import com.rcplatform.phototalk.request.PhotoTalkParams;
import com.rcplatform.phototalk.request.Request;
import com.rcplatform.phototalk.request.RequestCache;

public class RequestDb4oDatabase extends PhotoTalkRequestDatabase {

	private static final RequestDb4oDatabase instance = new RequestDb4oDatabase();
	private static ObjectContainer db;

	private RequestDb4oDatabase() {
		EmbeddedConfiguration config = Db4oEmbedded.newConfiguration();
		config.common().add(new AndroidSupport());
		db = Db4oEmbedded.openFile(config, DatabaseUtils.getRequestDatabasePath());
	}

	public synchronized static RequestDb4oDatabase getInstance() {
		return instance;
	}

	@Override
	public void saveRequest(Request request) {
		if (request.getUrl().equals(MenueApiUrl.SEND_PICTURE_URL)) {
			List<RequestCache> caches = slipRequest(request);
			saveRequest(caches);
		} else {
			RequestCache requestCache = new RequestCache(request);
			saveRequest(requestCache);
		}
	}

	private void saveRequest(RequestCache requestCache) {
		RequestCache reqExample = new RequestCache();
		reqExample.setCreateTime(requestCache.getCreateTime());
		reqExample.setRequestUrl(requestCache.getRequestUrl());
		ObjectSet<RequestCache> result = db.queryByExample(reqExample);
		if (result.size() > 0)
			return;
		db.store(requestCache);
		db.commit();
	}

	private void saveRequest(List<RequestCache> requestCaches) {
		for (RequestCache cache : requestCaches) {
			saveRequest(cache);
		}
	}

	private List<RequestCache> slipRequest(Request request) {
		String targets = request.getParams().get(PhotoTalkParams.SendPhoto.PARAM_KEY_USERS);
		List<RequestCache> cacheRequests = new ArrayList<RequestCache>();
		try {
			JSONArray jsonArray = new JSONArray(targets);
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				JSONArray array = new JSONArray();
				array.put(jsonObject);
				RequestCache cache = new RequestCache();
				Map<String, String> cacheParams = new HashMap<String, String>();
				cacheParams.putAll(request.getParams());
				cacheParams.put(PhotoTalkParams.SendPhoto.PARAM_KEY_USERS, array.toString());
				cache.setCreateTime(request.getCreateTime());
				cache.setAllParams(cacheParams);
				cache.setRequestUrl(request.getUrl());
				cache.setFilePath(request.getFile().getPath());
				cacheRequests.add(cache);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return cacheRequests;
	}

	@Override
	public List<Request> getRequests() {
		ObjectSet<RequestCache> result = db.query(RequestCache.class);
		List<Request> requests = new ArrayList<Request>();
		for (RequestCache requestCache : result) {
			Request request = createRequest(requestCache);
			requests.add(request);
		}
		return requests;
	}

	@Override
	public void deleteRequest(Request request) {
		RequestCache reqExample = new RequestCache();
		reqExample.setCreateTime(request.getCreateTime());
		reqExample.setRequestUrl(request.getUrl());
		ObjectSet<RequestCache> result = db.queryByExample(reqExample);
		for (RequestCache cache : result) {
			db.delete(cache);
		}
		db.commit();
	}

	private Request createRequest(RequestCache cache) {
		Request request = new Request();
		request.setCreateTime(cache.getCreateTime());
		if (cache.getFilePath() != null)
			request.setFile(new File(cache.getFilePath()));
		request.setParams(cache.getAllParams());
		request.setUrl(cache.getRequestUrl());
		return request;
	}

}
