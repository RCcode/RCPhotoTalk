package com.rcplatform.phototalk.bean;

/**
 * 标题、简要说明. <br>
 * 类详细说明.
 * <p>
 * Copyright: Menue,Inc Copyright (c) 2013-3-1 下午05:41:22
 * <p>
 * Team:Menue Beijing
 * <p>
 * 
 * @author jelly.xiong@menue.com.cn
 * @version 1.0.0
 */
public class SignupBean {

	private String nick;

	private String token;

	private int status;

	private int userId;

	public SignupBean(String nick, String token, int status, int userId) {
		this.nick = nick;
		this.token = token;
		this.status = status;
		this.userId = userId;
	}

	public String getNick() {
		return nick;
	}

	public void setNick(String nick) {
		this.nick = nick;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	@Override
	public String toString() {
		return "SignupBean [nick=" + nick + ", token=" + token + ", status=" + status + ", userId=" + userId + "]";
	}

}
