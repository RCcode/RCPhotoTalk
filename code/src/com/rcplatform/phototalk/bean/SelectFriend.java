package com.rcplatform.phototalk.bean;

public class SelectFriend extends Friend {
	private boolean isChosed;

	public void setIsChosed(boolean isChosed) {
		this.isChosed = isChosed;
	}

	public boolean getIsChosed() {
		return isChosed;
	}

	private int choseposition;

	public void setChoseposition(int choseposition) {
		this.choseposition = choseposition;
	}

	public int getChoseposition() {
		return choseposition;
	}

	private int position;

	public void setPosition(int position) {
		this.position = position;
	}

	public int getPosition() {
		return position;
	}

	public static SelectFriend parseSelectFriend(Friend friend) {
		SelectFriend result = new SelectFriend();
		result.setAdded(friend.getAdded());
		result.setAppId(friend.getAppId());
		result.setAppList(friend.getAppList());
		result.setBackground(friend.getBackground());
		result.setBirthday(friend.getBirthday());
		result.setCellPhone(friend.getCellPhone());
		result.setCountry(friend.getCountry());
		result.setFriend(friend.isFriend());
		result.setGender(friend.getGender());
		result.setHeadUrl(friend.getHeadUrl());
		result.setLetter(friend.getLetter());
		result.setLocalName(friend.getLocalName());
		result.setNickName(friend.getNickName());
		result.setRcId(friend.getRcId());
		result.setSource(friend.getSource());
		result.setTigaseId(friend.getTigaseId());
		return result;
	}
}
