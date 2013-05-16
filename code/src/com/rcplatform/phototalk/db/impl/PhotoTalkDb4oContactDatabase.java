package com.rcplatform.phototalk.db.impl;

import java.util.ArrayList;
import java.util.List;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.AndroidSupport;
import com.db4o.config.EmbeddedConfiguration;
import com.rcplatform.phototalk.bean.Contacts;
import com.rcplatform.phototalk.db.ContactDatabase;
import com.rcplatform.phototalk.db.DatabaseUtils;

public class PhotoTalkDb4oContactDatabase implements ContactDatabase {

	private static final PhotoTalkDb4oContactDatabase instance = new PhotoTalkDb4oContactDatabase();

	private static ObjectContainer db;

	public static synchronized PhotoTalkDb4oContactDatabase getInstance() {
		return instance;
	}

	private PhotoTalkDb4oContactDatabase() {
		EmbeddedConfiguration config = Db4oEmbedded.newConfiguration();
		config.common().add(new AndroidSupport());
		db = Db4oEmbedded.openFile(config, DatabaseUtils.getContactDatabasePath());
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

}
