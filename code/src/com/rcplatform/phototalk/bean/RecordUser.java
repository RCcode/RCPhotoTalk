package com.rcplatform.phototalk.bean;

public class RecordUser {

    private String suid;

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

	public RecordUser(String suid,String nick, String headUrl,String tigaseId) {
        this.suid = suid;
        this.nick = nick;
        this.headUrl = headUrl;
        this.tigaseId=tigaseId;
    }


    public String getSuid() {
		return suid;
	}

	public void setSuid(String suid) {
		this.suid = suid;
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
