package com.rcplatform.phototalk.db;

import java.util.List;

import com.rcplatform.phototalk.request.Request;

public interface RequestDatabase {
	public void saveRequest(Request request);

	public List<Request> getRequests();

	public void deleteRequest(Request request);
}
