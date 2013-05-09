package com.rcplatform.phototalk.bean;

import java.io.Serializable;

public class ServiceSimpleNotice implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String state;

	private String id;

	private String type;

	private long time;

	public ServiceSimpleNotice(String state, String id, String type, long time) {
		super();
		this.state = state;
		this.id = id;
		this.type = type;
		this.time = time;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public ServiceSimpleNotice() {
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
