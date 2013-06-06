package com.rcplatform.videotalk.bean;

import java.io.Serializable;

public class FriendSourse implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String name;
	private int attrType;
	private String value;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getAttrType() {
		return attrType;
	}

	public void setAttrType(int attrType) {
		this.attrType = attrType;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
