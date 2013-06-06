package com.rcplatform.videotalk.db;

import java.util.List;

import com.rcplatform.videotalk.request.Request;

public interface RequestDatabase {
	public void saveRequest(Request request);

	public List<Request> getRequests();

	public void deleteRequest(Request request);
}
