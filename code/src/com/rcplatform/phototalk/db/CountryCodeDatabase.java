package com.rcplatform.phototalk.db;

import java.util.List;

import com.rcplatform.phototalk.bean.CountryCode;

public interface CountryCodeDatabase {
	public List<CountryCode> getAllCountry();
	public void close();
}
