package com.rcplatform.videotalk.bean;

import java.io.Serializable;

public class ServiceCensus implements Serializable {
	private static final long serialVersionUID = 1L;

	private String rcId;
	private String picUrl;
	public ServiceCensus(String rcId, String picUrl) {
		super();
		this.rcId = rcId;
		this.picUrl = picUrl;
	}

	

	public String getRcId() {
		return rcId;
	}

	public void setRcId(String rcId) {
		this.rcId = rcId;
	}

	public String getPicurl() {
		return picUrl;
	}

	public void setPicurl(String picurl) {
		this.picUrl = picurl;
	}

}
