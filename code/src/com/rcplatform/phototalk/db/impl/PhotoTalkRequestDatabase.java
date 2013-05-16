package com.rcplatform.phototalk.db.impl;

import java.util.List;

import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.db.RequestDatabase;
import com.rcplatform.phototalk.request.Request;

public abstract class PhotoTalkRequestDatabase implements RequestDatabase {

	@Override
	public abstract void saveRequest(Request request);

	@Override
	public abstract List<Request> getRequests();

	@Override
	public abstract void deleteRequest(Request request);


}
