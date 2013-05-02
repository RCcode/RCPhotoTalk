package com.rcplatform.phototalk.bean;

public class RecordUser {

    private String suid;

    private String suUserId;

    private String nick;

    private String headUrl;

    public RecordUser() {
        // TODO Auto-generated constructor stub
    }

    public RecordUser(String suid, String suUserId, String nick, String headUrl) {
        super();
        this.suid = suid;
        this.suUserId = suUserId;
        this.nick = nick;
        this.headUrl = headUrl;
    }


    public String getSuid() {
		return suid;
	}

	public void setSuid(String suid) {
		this.suid = suid;
	}

	public String getSuUserId() {
        return suUserId;
    }

    public void setSuUserId(String suUserId) {
        this.suUserId = suUserId;
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
