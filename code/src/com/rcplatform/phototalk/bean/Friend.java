package com.rcplatform.phototalk.bean;

import java.io.Serializable;
import java.util.List;

import com.rcplatform.phototalk.utils.RCPlatformTextUtil;

public class Friend implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int FRIEND_ADDED = 0;
	public static final int FRIEND_NOT_ADDED = 1;

	public Friend(String nick, String cellPhone, String headUrl) {
		this.nickName = nick;
		this.cellPhone = cellPhone;
		this.headUrl = headUrl;
	}

	private FriendSourse source;

	private int appId;

	private int added;

	private String nickName;

	private String headUrl;

	private String background;

	private String rcId;

	private String cellPhone;

	private List<AppInfo> appList;

	private String localName;

	private int gender;

	private String letter;

	private boolean isNew = false;

	private String tigaseId;

	private String birthday;

	private String country;


	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getTigaseId() {
		return tigaseId;
	}

	public void setTigaseId(String tigaseId) {
		this.tigaseId = tigaseId;
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

	public int getGender() {
		return gender;
	}

	public void setGender(int gender) {
		this.gender = gender;
	}

	public String getLocalName() {
		return localName;
	}

	public void setLocalName(String localName) {
		this.localName = localName;
	}

	public Friend() {
		super();
	}

	public String getBackground() {
		return background;
	}

	public void setBackground(String background) {
		this.background = background;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public int getAppId() {
		return appId;
	}

	public void setAppId(int appId) {
		this.appId = appId;
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

	public String getCellPhone() {
		return cellPhone;
	}

	public void setCellPhone(String cellPhone) {
		this.cellPhone = cellPhone;
	}

	public boolean isFriend() {
		return added == FRIEND_ADDED;
//		return false;
	}

	public void setFriend(boolean isFriend) {
		if (isFriend)
			added = FRIEND_ADDED;
		else
			added = FRIEND_NOT_ADDED;
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

	@Override
	public boolean equals(Object o) {
		return rcId.equals(((Friend) o).getRcId());
	}

	public String getBirthday() {
		return birthday;
	}

	public void setBirthday(String birthday) {
		this.birthday = birthday;
	}

	public int getAdded() {
		return added;
	}

	public void setAdded(int added) {
		this.added = added;
	}

	public int getAge() {
		if (birthday == null)
			return 0;
		return RCPlatformTextUtil.getAgeByBirthday(birthday);
	}
}
