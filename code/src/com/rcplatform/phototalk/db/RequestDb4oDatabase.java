package com.rcplatform.phototalk.db;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.rcplatform.phototalk.request.Request;
import com.rcplatform.phototalk.request.RequestCache;

public class RequestDb4oDatabase implements RequestDatabase {

	private static final RequestDb4oDatabase instance = new RequestDb4oDatabase();
	private static ObjectContainer db;

	private RequestDb4oDatabase() {
		db = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), DatabaseUtils.getRequestDatabasePath());
	}

	public synchronized static RequestDb4oDatabase getInstance() {
		return instance;
	}

	@Override
	public void saveRequest(Request request) {
		RequestCache requestCache = new RequestCache(request);
		db.store(requestCache);
		db.commit();
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
