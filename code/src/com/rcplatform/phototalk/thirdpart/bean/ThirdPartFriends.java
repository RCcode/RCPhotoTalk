package com.rcplatform.phototalk.thirdpart.bean;

import java.util.List;

public class ThirdPartFriends {

	private List<User> users;

	private int next_cursor;

	private int previous_cursor;

	private int total_number;

	public int getTotal_number() {
		return total_number;
	}

	public void setTotal_number(int total_number) {
		this.total_number = total_number;
	}

	public int getNext_cursor() {
		return next_cursor;
	}

	public void setNext_cursor(int next_cursor) {
		this.next_cursor = next_cursor;
	}

	public int getPrevious_cursor() {
		return previous_cursor;
	}

	public void setPrevious_cursor(int previous_cursor) {
		this.previous_cursor = previous_cursor;
	}

	public List<User> getUsers() {
		return users;
	}

	public void setUsers(List<User> users) {
		this.users = users;
	}

	public static class User {

		public User() {
		}

		public User(String friendAccount, String friendName) {
			this.friendAccount = friendAccount;
			this.friendName = friendName;
		}

		// private String id;
		private String friendAccount;

		private String headUrl;

		// private String screen_name;

		// private String name;
		private String friendName;

		public String getFriendAccount() {
			return friendAccount;
		}

		public void setFriendAccount(String friendAccount) {
			this.friendAccount = friendAccount;
		}

		public String getHeadUrl() {
			return headUrl;
		}

		public void setHeadUrl(String headUrl) {
			this.headUrl = headUrl;
		}

		public String getFriendName() {
			return friendName;
		}

		public void setFriendName(String friendName) {
			this.friendName = friendName;
		}

		@Override
		public String toString() {
			return "User [friendAccount=" + friendAccount + ", headUrl=" + headUrl + ", friendName=" + friendName + "]";
		}

	}
}
