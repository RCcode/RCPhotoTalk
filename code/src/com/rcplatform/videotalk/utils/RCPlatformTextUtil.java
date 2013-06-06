package com.rcplatform.videotalk.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;

import com.rcplatform.videotalk.R;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

public class RCPlatformTextUtil {

	private static SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd");

	private static final long A_SECOND = 1000;
	private static final long A_MINUTE = 60 * A_SECOND;
	private static final long A_HOUR = 60 * A_MINUTE;
	private static final long A_DAY = 24 * A_HOUR;

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

	public static String getTextFromTimeToNow(Context context, long time) {
		long timeToNow = System.currentTimeMillis() - time;
		String result = null;
		if (timeToNow < A_MINUTE) {
			int timeNumber = (int) (timeToNow / A_SECOND);
			if (timeNumber < 0)
				timeNumber = 0;
			result = context.getString(R.string.ago, context.getString(R.string.second, timeNumber));
		} else if (timeToNow < A_HOUR) {
			int timeNumber = (int) (timeToNow / A_MINUTE);
			result = context.getString(R.string.ago, context.getString(R.string.minute, timeNumber));
		} else if (timeToNow < A_DAY) {
			int timeNumber = (int) (timeToNow / A_HOUR);
			result = context.getString(R.string.ago, context.getString(R.string.hour, timeNumber));
		} else {
			int timeNumber = (int) (timeToNow / A_DAY);
			result = context.getString(R.string.ago, context.getString(R.string.day, timeNumber));
		}
		return result;
	}

	public static String getLetter(String inputString) {
		if (inputString == null) {
			new IllegalArgumentException("inputString is NULL");
			return null;
		}
		HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
		format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
		format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
		format.setVCharType(HanyuPinyinVCharType.WITH_V);

		char[] input = inputString.trim().toCharArray();// 把字符串转化成字符数组
		String output = "";

		try {
			for (int i = 0; i < input.length; i++) {
				// \\u4E00是unicode编码，判断是不是中文
				if (java.lang.Character.toString(input[i]).matches("[\\u4E00-\\u9FA5]+")) {
					// 将汉语拼音的全拼存到temp数组
					String[] temp = PinyinHelper.toHanyuPinyinStringArray(input[i], format);
					// 取拼音的第一个读音
					output += temp[0];
				}
				// 大写字母转化成小写字母
				else if (input[i] >= 'A' && input[i] <= 'Z') {
					output += java.lang.Character.toString(input[i]);
					output = output.toLowerCase();
				}
				output += java.lang.Character.toString(input[i]);
			}
		} catch (Exception e) {
			Log.e("Exception", e.toString());
		}
		if (!TextUtils.isEmpty(output)) {
			char letter = output.charAt(0);
			if (letter >= 'a' && letter <= 'z')
				return letter + "";
		}
		return "#";
	}

	public static int getAgeByBirthday(String birthday) {
		try {
			long age = (System.currentTimeMillis() - mDateFormat.parse(birthday).getTime());
			return (int) (age / (1000L * 60 * 60 * 24 * 365));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public static boolean isEmpty(String text) {
		if (TextUtils.isEmpty(text) || text.equals("null"))
			return true;
		return false;
	}
}
