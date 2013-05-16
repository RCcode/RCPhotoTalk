package com.rcplatform.phototalk.db;

import java.util.List;

import com.rcplatform.phototalk.bean.Contacts;

public interface ContactDatabase {
	public void saveContacts(List<Contacts> contacts);

	public List<Contacts> getContacts();

	public void deleteContacts();
}
