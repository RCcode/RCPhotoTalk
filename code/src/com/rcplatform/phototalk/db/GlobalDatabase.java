package com.rcplatform.phototalk.db;

import java.util.List;

import com.rcplatform.phototalk.bean.AppInfo;
import com.rcplatform.phototalk.bean.Contacts;

public interface GlobalDatabase {
	
	public void saveContacts(List<Contacts> contacts);

	public List<Contacts> getContacts();

	public void deleteContacts();

	public void savePlatformAppInfos(List<AppInfo> appInfos);

	public List<AppInfo> getPlatformAppInfos();
}
