package com.rcplatform.phototalk.bean;

import java.io.Serializable;
import java.util.List;

/**
 * 标题、简要说明. <br>
 * 类详细说明.
 * <p>
 * Copyright: Menue,Inc Copyright (c) 2013-3-4 下午03:17:42
 * <p>
 * Team:Menue Beijing
 * <p>
 * 
 * @author jelly.xiong@menue.com.cn
 * @version 1.0.0
 */
public class Friend implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int USER_STATUS_NOT_FRIEND = 0;
	public static final int USER_STATUS_FRIEND_ADDED = 1;

	private FriendSourse source;

	private String nameChat;

	private String signature;

	private int appId;

	private String nick;

	private String headUrl;

	private String background;

	private String rcId;

	private String phone;

	private int trendsSet;

	private int receiveSet;

	private String suid;

	private int userFrom;

	private List<AppInfo> appList;

	private String mark;

	private int sex;

	private int age;

	private String letter;

	private boolean isNew=false;
	
	private int isFriend;
	
	private String tigaseId;
	
	public String getTigaseId() {
		return tigaseId;
	}

	public void setTigaseId(String tigaseId) {
		this.tigaseId = tigaseId;
	}

	public int getIsFriend() {
		return isFriend;
	}

	public void setIsFriend(int isFriend) {
		this.isFriend = isFriend;
	}

	public boolean isNew() {
		return isNew;
	}

	public void setNew(boolean isNew) {
		this.isNew = isNew;
	}

	public String getLetter() {
		return letter;
	}

	public void setLetter(String letter) {
		this.letter = letter;
	}

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getMark() {
		return mark;
	}

	public void setMark(String mark) {
		this.mark = mark;
	}

	/* 朋友状态 */
	private int status;

	public Friend() {
		super();
	}

	public String getBackground() {
		return background;
	}

	public void setBackground(String background) {
		this.background = background;
	}

	public Friend(String nick, String p, String headUrl) {
		this.nick = nick;
		this.phone = p;
		this.headUrl = headUrl;
	}

	public Friend(int type, String nameChat, String name, String number, String headUri) {
		this(name, number, headUri);
		this.nameChat = nameChat;
	}

	public String getNameChat() {
		return nameChat;
	}

	public void setNameChat(String nameChat) {
		this.nameChat = nameChat;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public int getAppId() {
		return appId;
	}

	public void setAppId(int appId) {
		this.appId = appId;
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

	public String getRcId() {
		return rcId;
	}

	public void setRcId(String rcId) {
		this.rcId = rcId;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public int getUserFrom() {
		return userFrom;
	}

	public void setUserFrom(int userFrom) {
		this.userFrom = userFrom;
	}

	public String getSuid() {
		return suid;
	}

	public void setSuid(String suid) {
		this.suid = suid;
	}

	/* 获取朋友状态。1表示已添加，0表示未添加。 */
	public int getStatus() {
		return isFriend;
	}

	/*
	 * 设置朋友状态。1表示已添加，0表示未添加。
	 * 
	 * @param status
	 */
	public void setStatus(int status) {
		isFriend=status;
	}

	public FriendSourse getSource() {
		return source;
	}

	public void setSource(FriendSourse source) {
		this.source = source;
	}

	public List<AppInfo> getAppList() {
		return appList;
	}

	public void setAppList(List<AppInfo> appList) {
		this.appList = appList;
	}

	public int getTrendsSet() {
		return trendsSet;
	}

	public void setTrendsSet(int trendsSet) {
		this.trendsSet = trendsSet;
	}

	public int getReceiveSet() {
		return receiveSet;
	}

	public void setReceiveSet(int receiveSet) {
		this.receiveSet = receiveSet;
	}

	@Override
	public boolean equals(Object o) {
		// TODO Auto-generated method stub
		return suid.equals(((Friend) o).getSuid());
	}
}
