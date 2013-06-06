package com.rcplatform.videotalk.db;

import java.util.List;

import com.rcplatform.videotalk.bean.AppInfo;
import com.rcplatform.videotalk.bean.Contacts;

public interface GlobalDatabase {
	
	public void saveContacts(List<Contacts> contacts);

	public List<Contacts> getContacts();

	public void deleteContacts();

	public void savePlatformAppInfos(List<AppInfo> appInfos);

	public List<AppInfo> getPlatformAppInfos();
}
