package com.rcplatform.phototalk.api;

public class PhotoTalkApiUrl {
//	private static final String BASE_URL = "http://pt.rcplatformhk.net/";
	private static final String BASE_URL = "http://192.168.0.86:8083/";
//	 private static final String BASE_URL = "http://192.168.0.119/";
	// private static final String BASE_URL = "http://192.168.0.86:8083/";
	// private static final String BASE_URL = "http://pt.rcplatformhk.net/";
	// private static final String BASE_URL = "http://103.247.169.150:8083/";
	// private static final String BASE_URL = "http://192.168.0.106/";

	private static final String BASE_URL_FOR_PHOTOTALK = BASE_URL + "phototalk/";

	private static final String USER_URL = BASE_URL_FOR_PHOTOTALK + "user/";

	private static final String THIRD_URL = BASE_URL_FOR_PHOTOTALK + "third/";

	private static final String FILE_URL = BASE_URL_FOR_PHOTOTALK + "file/";

	private static final String NOTICE_URL = BASE_URL_FOR_PHOTOTALK + "notice/";

	private static final String SETTING_URL = BASE_URL_FOR_PHOTOTALK + "setting/";

	private static final String APP_URL = BASE_URL_FOR_PHOTOTALK + "app/";

	public static final String NOTICE_OVER_URL = NOTICE_URL + "lookedTheNotice.do";

	public static final String FRIEND_QUERY_BY_PHONENUM_URL = USER_URL + "queryUsersByPhones.do";

	public static final String CLEAN_HISTORY_URL = NOTICE_URL + "clearAllNotice.do";

	public static final String DELETE_FRIEND_URL = USER_URL + "delUserFriend.do";

	public static final String UPDATE_FRIEND_REMARK_URL = USER_URL + "updFriendMark.do";

	public static final String GET_MY_FRIENDS_URL = USER_URL + "queryAllUsers.do";

	public static final String GET_ALL_RECOMMENDS_URL = USER_URL + "queryRecommendUsers.do";

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
	public static final String USER_BACKGROUND_UPDATE_URL = FILE_URL + "uploadBackground.do";
	public static final String USER_INFO_BACKGROUND_URL = FILE_URL + "uploadBackground.do";

	public static final String USER_INFO_HEAD_IMAGE_URL = FILE_URL + "uploadHead.do";

	public static final String THIRDPART_RECOMMENDS_URL = USER_URL + "queryThirdUsers.do";

	public static final String ADD_FRIEND_URL = USER_URL + "addFriend.do";

	public static final String FRIEND_DETAIL_URL = USER_URL + "queryUserDetail.do";

	public static final String ADD_FRIEND_FROM_INFORMATION = USER_URL + "tigaseAddFriendFromMain.do";

	public static final String USER_SETTING_URL = SETTING_URL + "submitConfig.do";

	public static final String CHECK_UPATE_URL = APP_URL + "getAppConfig.do";

	public static final String DELETE_RECOMMEND_URL = USER_URL + "delRecommendFriend.do";
	public static final String GET_FRIENDS_URL = USER_URL + "queryMyFriends.do";
	public static final String GET_FRIENDS_DYNAMIC_URL = SETTING_URL + "queryUserTrends.do";

	public static final String SEND_PICTURE_URL = FILE_URL + "upload.do";

	public static final String GET_USER_INFO = USER_URL + "getUserSelfInfo.do";

	public static final String CHECK_TRENDS_URL = SETTING_URL + "checkHasTrend.do";

	public static final String INFORMATION_CENSUS_URL = FILE_URL + "receivedPics.do";

	// -----------------------------------------------------------------------------------------------------------------------------------

//	private static final String RCBOSS_BASE_URL = "http://rc.rcplatformhk.net/";
	private static final String RCBOSS_BASE_URL = "http://192.168.0.86:8083/";
//	private static final String RCBOSS_BASE_URL = "http://192.168.0.119/";
	// private static final String RCBOSS_BASE_URL =
	// "http://192.168.0.86:8083/";

	private static final String BASE_URL_FOR_RCBOSS = RCBOSS_BASE_URL + "rcboss/";

	private static final String RCBOSS_USER_URL = BASE_URL_FOR_RCBOSS + "user/";

	private static final String RCBOSS_COMM_URL = BASE_URL_FOR_RCBOSS + "comm/";

	// private static final String RCBOSS_LONIN_URL = BASE_URL_FOR_RCBOSS +
	// "login/";

	private static final String RCBOSS_THIRD_URL = BASE_URL_FOR_RCBOSS + "third/";

	public static final String SYNC_CONTACT_URL = RCBOSS_COMM_URL + "synchroContacts.do";

	public static final String RCPLATFORM_ACCOUNT_LOGIN_URL = RCBOSS_USER_URL + "userLoginByOther.do";

	public static final String UPDATE_PHONE_BIND_STATE_URL = RCBOSS_COMM_URL + "phoneLog.do";
	public static final String LOGIN_URL = RCBOSS_USER_URL + "userLogin.do";
	public static final String SIGNUP_URL = RCBOSS_USER_URL + "regist.do";
	public static final String FORGET_PASSWORD_URL = RCBOSS_USER_URL + "getUserPass.do";

	public static final String CHECK_LOGIN_PASSWORD_URL = RCBOSS_USER_URL + "checkPwdForUpdate.do";

	public static final String UPDATE_LOGIN_PASSWORD_URL = RCBOSS_USER_URL + "updUserPass.do";

	public static final String CHECK_USER_PHONEBIND_URL = RCBOSS_USER_URL + "checkPhone.do";
	public static final String ASYNC_INVITE_URL = RCBOSS_COMM_URL + "inviteUserLog.do";
	public static final String SYNCHRO_THIRD_URL = RCBOSS_THIRD_URL + "thirdBind.do";
	public static final String GET_ALL_APPS_URL = RCBOSS_USER_URL + "getAllAppInfo.do";
	public static final String REQUEST_SMS_URL = RCBOSS_USER_URL + "downSms.do";
	public static final String BIND_PHONE_URL = RCBOSS_USER_URL + "checkSms.do";
	public static final String LOGOUT_URL = RCBOSS_USER_URL + "userLogout.do";
	public static final String RCPLATFORM_ACCTION_CREATE_USERINFO = RCBOSS_USER_URL + "createUserInfo.do";
	public static final String CLIENT_LOG_URL = BASE_URL_FOR_RCBOSS + "config/insClientLog.do";
}
