package com.rcplatform.videotalk.db.impl;

import java.util.List;

import com.rcplatform.videotalk.bean.Information;
import com.rcplatform.videotalk.db.RequestDatabase;
import com.rcplatform.videotalk.request.Request;

public abstract class PhotoTalkRequestDatabase implements RequestDatabase {

	@Override
	public abstract void saveRequest(Request request);

	@Override
	public abstract List<Request> getRequests();

	@Override
	public abstract void deleteRequest(Request request);


}
