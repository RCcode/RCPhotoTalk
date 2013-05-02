package com.rcplatform.phototalk.utils;

public class RCPlatformTextUtil {
	private static final String EMAIL_REGEX = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
	private static final String NICK_REGEX = "[a-zA-Z0-9]{1,20}";
	private static final String RCID_REGEX = "(?!^[0-9]*$)(?!^[a-zA-Z]*$)^([a-zA-Z0-9]{2,})$";
	public static final int RCID_MIN_LENGTH = 4;
	public static final int RCID_MAX_LENGTH = 20;
	public static final int SIGNTURE_MAX_LENGTH = 60;
	public static final int NICK_MAX_LENGTH = 20;
	public static final int PASSWORD_MIN_LENGTH = 6;
	public static final int PASSWORD_MAX_LENGTH = 20;

	public static boolean isEmailMatches(String email) {
		email = email.trim();
		return email.matches(EMAIL_REGEX);
	}

	public static boolean isPasswordMatches(String password) {
		int length = password.trim().length();
		return length >= PASSWORD_MIN_LENGTH && length <= PASSWORD_MAX_LENGTH;
	}

	public static boolean isRCIDMatches(String rcId) {
		rcId = rcId.trim();
		int textLength = rcId.length();
		return rcId.matches(RCID_REGEX) && textLength >= RCID_MIN_LENGTH && textLength <= RCID_MAX_LENGTH;
	}

	public static boolean isNickMatches(String nick) {
		nick = nick.trim();
		return nick.length() > 0 && nick.length() <= NICK_MAX_LENGTH;
	}

	public static boolean isSigntureMatches(String signture) {
		signture = signture.trim();
		return signture.length() <= SIGNTURE_MAX_LENGTH;
	}

	public static String getSMSMessage(String suid, String appId) {
		StringBuilder sb = new StringBuilder();
		sb.append(suid).append(",").append(appId);
		return sb.toString();
	}
}
