package com.rcplatform.videotalk.bean;

public class RecordUser {

	private String rcId;

	private String nick;

	private String headUrl;

	private String tigaseId;

	public RecordUser() {
	}

	public String getTigaseId() {
		return tigaseId;
	}

	public void setTigaseId(String tigaseId) {
		this.tigaseId = tigaseId;
	}

	public RecordUser(String rcId, String nick, String headUrl, String tigaseId) {
		this.rcId = rcId;
		this.nick = nick;
		this.headUrl = headUrl;
		this.tigaseId = tigaseId;
	}

	public String getRcId() {
		return rcId;
	}

	public void setRcId(String rcId) {
		this.rcId = rcId;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getHeadUrl() {
		return headUrl;
	}

	public void setHeadUrl(String headUrl) {
		this.headUrl = headUrl;
	}

}
