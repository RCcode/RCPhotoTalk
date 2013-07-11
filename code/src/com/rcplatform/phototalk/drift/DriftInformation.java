package com.rcplatform.phototalk.drift;

import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.request.PhotoTalkParams;

public class DriftInformation {
	private long createTime;
	private long receiveTime;
	private int hasVoice;
	private int hasGraf;
	private int totleLength;
	private int limitTime;
	private int picId;
	private String url;
	private int state = 1;
	private long flag;

	public long getFlag() {
		return flag;
	}

	public void setFlag(long flag) {
		this.flag = flag;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getReceiveTime() {
		return receiveTime;
	}

	public void setReceiveTime(long receiveTime) {
		this.receiveTime = receiveTime;
	}

	public int getHasVoice() {
		return hasVoice;
	}

	public boolean hasVoice() {
		return hasVoice == Integer.parseInt(PhotoTalkParams.SendPhoto.PARAM_VALUE_HAS_VOICE);
	}

	public boolean hasGraf() {
		return hasGraf == Integer.parseInt(PhotoTalkParams.SendPhoto.PARAM_VALUE_HAS_GRAF);
	}

	public void setHasVoice(int hasVoice) {
		this.hasVoice = hasVoice;
	}

	public int getHasGraf() {
		return hasGraf;
	}

	public void setHasGraf(int hasGraf) {
		this.hasGraf = hasGraf;
	}

	public int getTotleLength() {
		return totleLength;
	}

	public void setTotleLength(int totleLength) {
		this.totleLength = totleLength;
	}

	public int getLimitTime() {
		return limitTime;
	}

	public void setLimitTime(int limitTime) {
		this.limitTime = limitTime;
	}

	public int getPicId() {
		return picId;
	}

	public void setPicId(int picId) {
		this.picId = picId;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	private DriftSender sender;

	public DriftSender getSender() {
		return sender;
	}

	public void setSender(DriftSender sender) {
		this.sender = sender;
	}
}
