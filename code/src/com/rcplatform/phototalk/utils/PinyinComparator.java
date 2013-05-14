package com.rcplatform.phototalk.utils;

import java.util.Comparator;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import android.util.Log;

import com.rcplatform.phototalk.bean.Friend;

/**
 * <p>
 * Copyright: Menue,Inc Copyright (c) 2013-3-6 下午03:05:59
 * <p>
 * Team:Menue Beijing
 * <p>
 * 
 * @author jelly.xiong@menue.com.cn
 * @version 1.0.0
 */
@SuppressWarnings("rawtypes")
public class PinyinComparator implements Comparator {

	/**
	 * 比较两个字符串
	 */
	@Override
	public int compare(Object o1, Object o2) {
		Friend friend1 = (Friend) o1;
		Friend friend2 = (Friend) o2;
		String str1 = getPingYin(friend1.getNickName());
		String str2 = getPingYin(friend2.getNickName());
		friend1.setLetter(str1.substring(0, 1));
		friend2.setLetter(str2.substring(0, 1));
		if (str1.equals(str2))
			return 1;
		int flag = str1.compareTo(str2);
		return flag;
	}

	/**
	 * 将字符串中的中文转化为拼音,其他字符不变
	 * 
	 * @param inputString
	 * @return
	 */
	public static String getPingYin(String inputString) {
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
		return output;
	}

}
