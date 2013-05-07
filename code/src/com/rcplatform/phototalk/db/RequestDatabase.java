package com.rcplatform.phototalk.db;

import java.util.List;

import com.rcplatform.phototalk.request.FileRequest;
import com.rcplatform.phototalk.request.JSONRequest;

public interface RequestDatabase {

	public void insertRequest(JSONRequest request);

	public void insertRequest(FileRequest request);

	public void deleteRequest(JSONRequest request);

	public void deleteRequest(FileRequest request);

	public List<FileRequest> getFileRequests();

	public List<JSONRequest> getJSONRequests();

}
