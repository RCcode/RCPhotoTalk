package com.rcplatform.phototalk.galhttprequest;

/**
 * @Title: MD5.java
 * @Package com.galeapp.push.xmpp.util
 * @Description: 为String生成MD5码
 * @author 林秋明
 * @date 2012-2-14 上午11:51:48
 * @version V1.0
 */
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5 {

	/**
	 * @param key
	 *            需要生成的key的源字符串
	 * @return 生成的32位key
	 */
	public static String md5Hash(String key) {
		String cacheKey;
		try {
			final MessageDigest mDigest = MessageDigest.getInstance("MD5");
			mDigest.update(key.getBytes());
			cacheKey = bytesToHexString(mDigest.digest());
		}
		catch (NoSuchAlgorithmException e) {
			cacheKey = String.valueOf(key.hashCode());
		}
		return cacheKey;
	}

	private static String bytesToHexString(byte[] bytes) {
		// http://stackoverflow.com/questions/332079
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
			String hex = Integer.toHexString(0xFF & bytes[i]);
			if (hex.length() == 1) {
				sb.append('0');
			}
			sb.append(hex);
		}
		return sb.toString();
	}

	/**
	 * 获取某个key的md5值(利用facebook的util类提供的函数)
	 * 
	 * @param key
	 *            需要生成的key的源字符串
	 * @return 生成的32位key
	 */
	public static String MD5WithFacebook(String key) {
		MessageDigest hash = null;
		try {
			hash = MessageDigest.getInstance("MD5");
		}
		catch (NoSuchAlgorithmException e) {
			return null;
		}
		hash.update(key.getBytes());
		byte[] digest = hash.digest();
		StringBuilder builder = new StringBuilder();
		for (int b : digest) {
			builder.append(Integer.toHexString((b >> 4) & 0xf));
			builder.append(Integer.toHexString((b >> 0) & 0xf));
		}
		return builder.toString();
	}

	public static String encodeMD5String(String str) {
		return encode(str, "MD5");
	}

	private static String encode(String str, String method) {
		MessageDigest md = null;
		String dstr = null;
		try {
			md = MessageDigest.getInstance(method);
			md.update(str.getBytes());
			dstr = new BigInteger(1, md.digest()).toString(16);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return dstr;
	}

}
