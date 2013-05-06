package com.rcplatform.phototalk.api;

public class MenueApiUrl {

	private static final String BASE_URL = "http://192.168.0.86:8083/";
	// private static final String BASE_URL = "http://103.247.169.150:8083/";
	// private static final String BASE_URL = "http://192.168.0.116/";

	private static final String BASE_URL_FOR_PHOTOTALK = BASE_URL + "photochat/";

	private static final String USER_URL = BASE_URL_FOR_PHOTOTALK + "user/";

	private static final String THIRD_URL = BASE_URL_FOR_PHOTOTALK + "third/";

	private static final String FILE_URL = BASE_URL_FOR_PHOTOTALK + "file/";

	private static final String NOTICE_URL = BASE_URL_FOR_PHOTOTALK + "notice/";

	private static final String SETTING_URL = BASE_URL_FOR_PHOTOTALK + "setting/";
	
	private static final String APP_URL=BASE_URL_FOR_PHOTOTALK+"app/";

	public static final String REMOVE_NOTICE_ITEM = NOTICE_URL + "lookedTheNotice.do";

	public static final String FRIEND_QUERY_BY_PHONENUM_URL = USER_URL + "queryUsersByPhones.do";

	public static final String CLEAN_HISTORY_URL = NOTICE_URL + "clearAllNotice.do";

	public static final String DELETE_FRIEND_URL = USER_URL + "delUserFriend.do";

	public static final String UPDATE_FRIEND_REMARK_URL = USER_URL + "updFriendMark.do";

	public static final String GET_MY_FRIENDS_URL = USER_URL + "queryAllUsers.do";

	public static final String CREATE_TACOTY_ID_URL = USER_URL + "saveTacoty.do";

	public static final String USER_NOTICES_URL = NOTICE_URL + "mainUserNoticeList.do";

	public static final String NOTICE_STATE_CHANGE_URL = NOTICE_URL + "downLoadOrLookNotice.do";

	public static final String HOME_USER_NOTICE_ADD_FRIEND = USER_URL + "addFriendFromMain.do";
	public static final String CONTACT_RECOMMEND_URL = USER_URL + "queryUsersByPhones.do";
	public static final String NOTICE_DELETE_URL = NOTICE_URL + "deleteTheNotice.do";

	public static final String NOTICE_CLEAR_URL = NOTICE_URL + "clearAllNotice.do";

	public static final String FRIEND_QUERY_BY_FACEBOOK_URL = THIRD_URL + "thirdBind.do";

	public static final String FRIEND_FACEBOOK_USER_LIST_BY_URL = THIRD_URL + "getUserFriendList.do";

	public static final String FRIEND_ADD_URL = USER_URL + "addFriend.do";

	public static final String SEARCH_FRIENDS_URL = USER_URL + "findUserList.do";

	public static final String USER_INFO_URL = USER_URL + "getPUser.do";

	public static final String USER_INFO_UPDATE_URL = FILE_URL + "userUpload.do";

	public static final String FACEBOOK_RECOMMENDS_URL = USER_URL + "queryThirdUsers.do";

	public static final String ADD_FRIEND_URL = USER_URL + "addFriend.do";

	public static final String ASYNC_INVITE_URL = USER_URL + "inviteUserLog.do";
	public static final String FRIEND_DETAIL_URL = USER_URL + "getUserInfo.do";

	public static final String ADD_FRIEND_FROM_INFORMATION = USER_URL + "addFriendFromMain.do";

	public static final String USER_SETTING_URL = SETTING_URL + "submitConfig.do";
	
	public static final String CHECK_UPATE_URL=APP_URL+"getAppConfig.do";

	// -----------------------------------------------------------------------------------------------------------------------------------
	private static final String BASE_URL_FOR_USER = BASE_URL + "useraccess/";

	public static final String RCPLATFORM_ACCOUNT_LOGIN_URL = BASE_URL_FOR_USER + "login/createUserInfo.do";
	private static final String COMM_URL = BASE_URL_FOR_USER + "comm/";
	public static final String SYNC_CONTACT_URL = COMM_URL + "synchroContacts.do";

	public static final String GET_FRIENDS_URL = USER_URL + "queryMyFriends.do";

	public static final String SEND_PICTURE_URL = FILE_URL + "upload.do";
	// 2.3 用户登录
	public static final String LOGIN_URL = BASE_URL_FOR_USER + "login/userLogin.do";

	// 2.1 用户注册
	public static final String SIGNUP_URL = BASE_URL_FOR_USER + "login/regist.do";
	public static final String CHECK_USER_URL = BASE_URL_FOR_USER + "login/hasUserInfo.do";
	// 2.25 清空所有记录

	// 2.22 删除好友（单方向删除）

	// 2.4 忘记密码
	public static final String FORGET_PASSWORD_URL = BASE_URL_FOR_USER + "user/getUserPass.do";

	// 2.13 好友列表--上半部推荐好友（点击主界面的好友进入）
	// 2.14 好友列表--下半部自己在此应用下的好友（点击主界面的好友进入）

	// 2.16 通讯录好友查找（同2.6添加好友的列表）

	// 2.26 验证登录密码
	public static final String CHECK_LOGIN_PASSWORD_URL = BASE_URL_FOR_USER + "login/checkPwdForUpdate.do";

	// 2.27 修改登录密码
	public static final String UPDATE_LOGIN_PASSWORD_URL = BASE_URL_FOR_USER + "login/updUserPass.do";

	// 2.28 存储tacotyId

	public static final String CHECK_USER_PHONEBIND_URL = BASE_URL_FOR_USER + "user/checkPhone.do";
	public static final String UPDATE_PHONE_BIND_STATE_URL = COMM_URL + "phoneLog.do";
	public static final String SYNCHRO_THIRD_URL = THIRD_URL + "thirdBind.do";
	public static final String GET_USER_INFO = USER_URL + "getUserSelfInfo.do";

}
