package com.rcplatform.videotalk.db;

import java.util.List;

import com.rcplatform.videotalk.bean.CountryCode;

public interface CountryCodeDatabase {
	public List<CountryCode> getAllCountry();
	public void close();
}
