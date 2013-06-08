package com.rcplatform.videotalk.bean;

public class Information {

	// protected String recordId;

	protected long createtime;

	protected long lastUpdateTime;

	private long receiveTime;

	protected int type; // 发送图片，接收图片，通知

	protected int statu;

	protected int limitTime;

	protected String url;

	protected int notifyStatu;

	protected RecordUser sender;

	protected RecordUser receiver;

	private int totleLength;

	private boolean hasVoice;

	private boolean hasGraf;

	public boolean isHasGraf() {
		return hasGraf;
	}

	public void setHasGraf(boolean hasGraf) {
		this.hasGraf = hasGraf;
	}

	public boolean isHasVoice() {
		return hasVoice;
	}

	public void setHasVoice(boolean hasVoice) {
		this.hasVoice = hasVoice;
	}

	public Information() {
	}

	public int getTotleLength() {
		return totleLength;
	}

	public void setTotleLength(int totleLength) {
		this.totleLength = totleLength;
	}

	public Information(int statu, long lastUpdateTime) {
		this.statu = statu;
		this.lastUpdateTime = lastUpdateTime;
	}

	public int getLimitTime() {
		return limitTime;
	}

	public void setLimitTime(int limitTime) {
		this.limitTime = limitTime;
	}

	public long getCreatetime() {
		return createtime;
	}

	public void setCreatetime(long createtime) {
		this.createtime = createtime;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getStatu() {
		return statu;
	}

	public void setStatu(int statu) {
		this.statu = statu;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getNotifyStatu() {
		return notifyStatu;
	}

	public void setNotifyStatu(int notifyStatu) {
		this.notifyStatu = notifyStatu;
	}

	public RecordUser getSender() {
		return sender;
	}

	public void setSender(RecordUser own) {
		this.sender = own;
	}

	public RecordUser getReceiver() {
		return receiver;
	}

	public void setReceiver(RecordUser receiver) {
		this.receiver = receiver;
	}

	public long getLastUpdateTime() {
		return lastUpdateTime;
	}

	public void setLastUpdateTime(long lastUpdateTime) {
		this.lastUpdateTime = lastUpdateTime;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Information))
			return false;
		Information info = (Information) o;
		if (info.getType() == InformationType.TYPE_FRIEND_REQUEST_NOTICE)
			return this.getSender().getRcId().equals(info.getSender().getRcId()) && this.getReceiver().getRcId().equals(info.getReceiver().getRcId())
					&& this.type == info.getType();
		return this.getSender().getRcId().equals(info.getSender().getRcId()) && this.getReceiver().getRcId().equals(info.getReceiver().getRcId())
				&& this.createtime == info.getCreatetime() && this.type == info.getType();
	}

	public long getReceiveTime() {
		return receiveTime;
	}

	public void setReceiveTime(long receiveTime) {
		this.receiveTime = receiveTime;
	}

}