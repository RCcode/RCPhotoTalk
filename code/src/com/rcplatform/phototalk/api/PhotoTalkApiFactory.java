package com.rcplatform.phototalk.api;

import com.rcplatform.phototalk.bean.UserInfo;

public class PhotoTalkApiFactory {

	// 返回状态。
//	public static final int RESPONSE_STATE_SUCCESS = 0;

	// 无用户信息
//	public static final int RESPONSE_STATE_SUCCESS_NO_FRIEND = 1;

	// 无用任何好友
//	public static final int RESPONSE_STATE_SUCCESS_NO_MYFRIEND = 2;

	// 电话本为空
//	public static final int RESPONSE_STATE_SUCCESS_NO_CONTACT = 3;

//	public static final String RESPONSE_KEY_STATUS = "status";
/*
	public static final String RESPONSE_KEY_MESSAGE = "message";

	public static final String RESPONSE_KEY_TIME = "time";*/

//	public static final int ORDER_BY_TIME = 2;
//
//	public static final int ORDER_BY_HOT = 1;
//
//	public static final int FOLLOW_TYPE = 1; // 关注

//	public static final int UNFOLLOW_TYPE = 0; // 取消关注
//
//	public static final String REQUEST_KEY_USER_TOKEN = "token";
//
//	public static final String REQUEST_KEY_USER_ID = "userId";
//
//	public static final String REQUEST_KEY_SORT = "sort";
//
//	public static final String REQUEST_KEY_SIZE = "size";

//	public static final String REQUEST_KEY_PAGE = "page";
//
//	public static final String REQUEST_KEY_TIME = "time";

	/*
	 * 城市信息图片的用户相关 author : jason.wu
	 * {"token":"1","userId":"0","page":"1","size":
	 * "20","sort":"0","language":"zh_CN","themeId":"1"}
	 */
//	public static final String PWD = "pwd";
//
//	public static final String NEW_PWD = "newpwd";
//
//	public static final String TACOTYID = "tacotyId";
//
//	public static final String ADDRESS = "addr";

	// 手机系统
//	public static final String SYSTEM = "system";
//
//	public static final String MODEL = "model";
//
//	public static final String USER = "user";
//
//	public static final String EMAIL = "email";
//	public static final String ACCOUNT = "account";
//	public static final String LOGIN_TYPE = "type";
//	public static final String TAGLIST = "tagList";

//	public static final String TOKEN = "token";
//
//	public static final String USERID = "userId";
//
//	public static final String SEUSERID = "seUserId";
//
//	public static final String SESUID = "seSuid";
//
//	public static final String USERID_FRIEND = "atUserId";

//	public static final String FRIENDS = "friends";
//
//	public static final String PAGE = "page";
//
//	public static final String SIZE = "size";
//
//	public static final String SORT = "sort";
//
//	public static final String RECEIVESET = "receiveSet";
//
	public static final String SIGNATURE = "signature";

	public static final String THEMEID = "themeId";

	public static final String TAGID = "tagId";

	public static final String CITY = "city";

	public static final String PICID = "picId";

	public static final String ATUSERID = "atUserId";

	public static final String HEAD_URL = "headUrl";

	public static final String SHEAD_URL = "shead";

	public static final String NICK = "nick";

	public static final String SNICK = "snick";

	public static final String CONTENT = "content";

	public static final String HIDE = "hide";

	public static final String TYPE = "type";

	public static final String TIME = "time";

	public static final String LANGUAGE = "language";

	public static final String COUNTRY = "country";

	public static final String PHONE_LIST = "phoneList";

	public static final String FACEBOOK_LIST = "friendList";

	public static final String THIRD_INFO = "thirdInfo";

	/* 图片类型 */
	public static final String IMGTYPE = "imgType";

	/* 年龄 */
	public static final String SEX = "sex";

	/* 生日 */
	public static final String BIRTHDAY = "birthday";
	// 登录类型
	public static final int LOGIN_TYPE_EMAIL = 0;
	public static final int LOGIN_TYPE_RCID = 2;

	// 登录错误处理
	public static final int LOGIN_ADMIN_ERROR = -1; // 参数异常

	public static final int LOGIN_SERVER_ERROR = -2; // 操作异常

	public static final int LOGIN_SUCCESS = 0; // 登录成功

	public static final int LOGIN_EMAIL_ERROR = 1; // 邮箱没有注册

	public static final int LOGIN_PASSWORD_ERROR = 2; // 密码错误

	public static final int LOGIN_ON_USER_INFO_ERROR = 3; // 没有用户信息

	// 登录成功过后返回的Token
	public static UserInfo UER_TOKEN;

	// 登录没有成功的时候将返回的UER_TOKEN赋值为000000
	public static final String ERROR_TOKEN = "000000";

	// 默认的token为 000000
	public static final String TOKEN_DEFAULT = "000000";
	// 没有登录状态请求数据
	public static final String ERROR_ID = "1";

	// 注册返回的错误状态码
	public static final int REGISTER_SUCCESS = 0; // 成功

	public static final int REGISTER_SERVER_ACTION_ERROR = -2; // 操作异常

	public static final int REGISTER_SERVER_ERROR = -1; // 参数异常

	//
	public static final int REGISTER_EMAIL_ERROR = 1; // 邮箱已被注册

	public static final int REGISTER_NICK_ERROR = 2; // 昵称已被注册

	public static final String APP_ID = "appId";

	public static final String DEVICE_ID = "deviceId";

	// 坐标地址
	public static final String GPS = "gps";

	// 手机品牌
	public static final String BRAND = "brand";

	// 手机平台，如iphone 或 android平台。
	public static final String PLATFORM = "platform";

	// 已安装的APP
	public static final String INSTALL_APPS = "installApps";

	// 搜索关键字
	public static final String SEARCH_KEYWORD = "keyword";

	// add futao

	public static final String NOTICES = "notices";

	public static final String RECORD_NOTICE_SEND_USER_ID = "seUserId";

	public static final String RECORD_NOTICE_SEND_USER_SUID = "seSuid";

	public static final String RECORD_NOTICE_SEND_USER_NICK = "snick";

	public static final String RECORD_NOTICE_SEND_USER_HEAD = "shead";

	public static final String RECORD_NOTICE_FRIEND_ID = "friendId";

	public static final String RECORD_NOTICE_NOTICE_NOTICEID = "noticeId";

	public static final String RECORD_NOTICE_NOTICE_ID = "id";

	public static final String RECORD_NOTICE_STATU = "state";

	public static final String RECORD_NOTICE_TYPE = "noType";

	public static final String IMAGE_TYPE = "jpg";

	public static final String DESC = "desc";

	public static final String TIME_LIMIT = "timeLimit";

	public static final String USER_ARRAY = "userArray";

	/* 文件类型 */
	public static final String FILE = "file";

	public static final String ERROR_NOTICE = "error_notice";

	// added by jelly

	public static final String NOTICE_ID = "noticeId";

}
