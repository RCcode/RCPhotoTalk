package com.rcplatform.phototalk.bean;

import java.util.ArrayList;
import java.util.List;

public class InformationState {

	public static final int STATU_QEQUEST_ADD_NO_CONFIRM = 1;

	public static final int STATU_QEQUEST_ADD_NEED_CONFIRM = 2;

	public static final int STATU_QEQUEST_ADDED = 3;

	// 发送方 表示已经发送，接收方去下载图片（正在下载）
	public static final int STATU_NOTICE_SENDED_OR_NEED_LOADD = 1;

	// 发送方表示已送达,接收方表示已下载
	public static final int STATU_NOTICE_DELIVERED_OR_LOADED = 2;

	// 发送方表示已查看，接收方已打开
	public static final int STATU_NOTICE_OPENED = 3;

	// 表示正在查看（此状态和服务器和数据库都没关系，只是一个用来临时记录正在查看中的一个变量）
	public static final int STATU_NOTICE_SHOWING = -2;

	// 表示正在下载（此状态和服务器和数据库都没关系，只是一个用来临时记录正在查看中的一个变量）
	public static final int STATU_NOTICE_LOADING = 0;

	// 表示正在发送（此状态和服务器和数据库都没关系，只是一个用来临时记录正在查看中的一个变量）
	public static final int STATU_NOTICE_SENDING = 0;

	public static final int STATU_NOTICE_OVER = 8;

	public static final int STATU_NOTICE_SEND_FAIL = -1;

	public static final int STATU_NOTICE_LOAD_FAIL = -1;

	private static List<Integer> servicePhotoStates;
	static {
		servicePhotoStates = new ArrayList<Integer>();
		servicePhotoStates.add(STATU_NOTICE_SENDED_OR_NEED_LOADD);
		servicePhotoStates.add(STATU_NOTICE_DELIVERED_OR_LOADED);
		servicePhotoStates.add(STATU_NOTICE_OPENED);
	}

	public static boolean isServiceState(Integer state) {
		return servicePhotoStates.contains(state);
	}
}
