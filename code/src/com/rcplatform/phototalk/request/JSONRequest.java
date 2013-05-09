package com.rcplatform.phototalk.request;

import java.util.Map;

public class JSONRequest extends Request{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Map<String, String> params;

	public Map<String, String> getParams() {
		return params;
	}

	public void setParams(Map<String, String> params) {
		this.params = params;
	}

}
