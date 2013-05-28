package com.rcplatform.phototalk.db.impl;

import java.util.ArrayList;
import java.util.List;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.AndroidSupport;
import com.db4o.config.EmbeddedConfiguration;
import com.rcplatform.phototalk.bean.TigaseMassage;
import com.rcplatform.phototalk.db.DatabaseUtils;


public class TigaseDb4oDatabase {
	private static ObjectContainer db;
	public TigaseDb4oDatabase() {
		EmbeddedConfiguration config = Db4oEmbedded.newConfiguration();
		config.common().add(new AndroidSupport());
		db = Db4oEmbedded.openFile(config, DatabaseUtils.getTigaseMsgDatabasePath());
	}
	
	public void saveTigaseMassages(List<TigaseMassage> tigaseMassages) {
		db.store(tigaseMassages);
		db.commit();
	}

	public List<TigaseMassage> getTigaseMassages() {
		ObjectSet<TigaseMassage> result = db.query(TigaseMassage.class);
		List<TigaseMassage> massages = new ArrayList<TigaseMassage>();
		massages.addAll(result);
		return massages;
	}

	public void deleteTigaseMassage(TigaseMassage massage) {
		db.delete(massage);
		db.commit();
	}
}
