package com.rcplatform.phototalk.bean;

public class SelectFriend extends Friend{
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
}
