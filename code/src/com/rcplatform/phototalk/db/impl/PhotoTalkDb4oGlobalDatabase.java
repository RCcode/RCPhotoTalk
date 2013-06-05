package com.rcplatform.phototalk.db.impl;

import java.util.ArrayList;
import java.util.List;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.AndroidSupport;
import com.db4o.config.EmbeddedConfiguration;
import com.rcplatform.phototalk.bean.AppInfo;
import com.rcplatform.phototalk.bean.Contacts;
import com.rcplatform.phototalk.db.GlobalDatabase;
import com.rcplatform.phototalk.db.DatabaseUtils;

public class PhotoTalkDb4oGlobalDatabase implements GlobalDatabase {

	private static final PhotoTalkDb4oGlobalDatabase instance = new PhotoTalkDb4oGlobalDatabase();

	private static ObjectContainer db;

	public static synchronized PhotoTalkDb4oGlobalDatabase getInstance() {
		return instance;
	}

	private PhotoTalkDb4oGlobalDatabase() {
		EmbeddedConfiguration config = Db4oEmbedded.newConfiguration();
		config.common().add(new AndroidSupport());
		db = Db4oEmbedded.openFile(config, DatabaseUtils.getGlobalDatabasePath());
	}

	@Override
	public void saveContacts(List<Contacts> contacts) {
		deleteContacts();
		db.store(contacts);
		db.commit();
	}

	@Override
	public List<Contacts> getContacts() {
		ObjectSet<Contacts> result = db.query(Contacts.class);
		List<Contacts> contacts = new ArrayList<Contacts>();
		contacts.addAll(result);
		return contacts;
	}

	@Override
	public void deleteContacts() {
		ObjectSet<Contacts> result = db.query(Contacts.class);
		while (result.hasNext())
			db.delete(result.next());
		db.commit();
	}

	@Override
	public void savePlatformAppInfos(List<AppInfo> appInfos) {
		ObjectSet<AppInfo> localApps = db.query(AppInfo.class);
		for (AppInfo info : localApps)
			db.delete(info);
		db.store(appInfos);
		db.commit();
	}

	@Override
	public List<AppInfo> getPlatformAppInfos() {
		ObjectSet<AppInfo> result = db.query(AppInfo.class);
		List<AppInfo> apps = new ArrayList<AppInfo>();
		apps.addAll(result);
		return apps;
	}

}
