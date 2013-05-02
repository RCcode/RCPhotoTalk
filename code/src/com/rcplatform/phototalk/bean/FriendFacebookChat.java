package com.rcplatform.phototalk.bean;

public class FriendFacebookChat extends Friend {

	public FriendFacebookChat(String uName, String uHeadUrl, String fType, String fAccount, String fname) {
		super();
		this.uName = uName;
		this.uHeadUrl = uHeadUrl;
		this.fType = fType;
		this.fAccount = fAccount;
		this.fname = fname;
	}

	/** 自己的facebook昵称uName */
	private String uName;

	private String uHeadUrl;

	private String fType;

	private String fAccount;

	/** facebook的好友的昵称 */
	private String fname;

	public String getuName() {
		return uName;
	}

	public void setuName(String uName) {
		this.uName = uName;
	}

	public String getuHeadUrl() {
		return uHeadUrl;
	}

	public void setuHeadUrl(String uHeadUrl) {
		this.uHeadUrl = uHeadUrl;
	}

	public String getfType() {
		return fType;
	}

	public void setfType(String fType) {
		this.fType = fType;
	}

	public String getfAccount() {
		return fAccount;
	}

	public void setfAccount(String fAccount) {
		this.fAccount = fAccount;
	}

	public String getFname() {
		return fname;
	}

	public void setFname(String fname) {
		this.fname = fname;
	}

	@Override
	public String toString() {
		return "FriendFacebook [uName=" + uName + ", uHeadUrl=" + uHeadUrl + ", fType=" + fType + ", fAccount=" + fAccount + ", fname=" + fname + "]";
	}

}
