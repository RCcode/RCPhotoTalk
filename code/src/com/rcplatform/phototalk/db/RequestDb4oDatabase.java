package com.rcplatform.phototalk.db;

import java.util.ArrayList;
import java.util.List;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.rcplatform.phototalk.request.Request;

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
		db.store(request);
		db.commit();
	}

	@Override
	public List<Request> getRequests() {
		ObjectSet<Request> result = db.query(Request.class);
		List<Request> requests = new ArrayList<Request>();
		requests.addAll(result);
		return requests;
	}
}
