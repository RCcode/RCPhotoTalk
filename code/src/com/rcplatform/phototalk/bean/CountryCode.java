package com.rcplatform.phototalk.bean;

public class CountryCode {
	private String countryShort;
	private String countryName;
	private String countryCode;

	public String getCountryShort() {
		return countryShort;
	}

	public void setCountryShort(String countryShort) {
		this.countryShort = countryShort;
	}

	public String getCountryName() {
		return countryName;
	}

	public void setCountryName(String countryName) {
		this.countryName = countryName;
	}

	public String getCountryCode() {
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof CountryCode))
			return false;
		return ((CountryCode) o).getCountryShort().toLowerCase().equals(countryShort.toLowerCase());
	}
}
