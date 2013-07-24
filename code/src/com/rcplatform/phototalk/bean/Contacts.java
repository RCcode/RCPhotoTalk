package com.rcplatform.phototalk.bean;

import java.io.Serializable;

public class Contacts implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public Contacts(String name, String mobilePhoneNumber) {
		this.name = name;
		this.mobilePhoneNumber = mobilePhoneNumber;
	}

	public Contacts() {
		// TODO Auto-generated constructor stub
	}

	private String name;
	private String mobilePhoneNumber;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getMobilePhoneNumber() {
		return mobilePhoneNumber;
	}

	public void setMobilePhoneNumber(String mobilePhoneNumber) {
		this.mobilePhoneNumber = mobilePhoneNumber;
	}

	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		if (o == null || mobilePhoneNumber == null)
			return false;
		return mobilePhoneNumber.equals(((Contacts) o).getMobilePhoneNumber());
	}

}
