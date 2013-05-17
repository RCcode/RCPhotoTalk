package com.rcplatform.phototalk.clienservice;

import java.util.List;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.IBinder;
import android.telephony.SmsManager;

import com.facebook.Request;
import com.facebook.Request.GraphUserCallback;
import com.facebook.Request.GraphUserListCallback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.rcplatform.phototalk.MenueApplication;
import com.rcplatform.phototalk.api.MenueApiUrl;
import com.rcplatform.phototalk.bean.FriendType;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.galhttprequest.LogUtil;
import com.rcplatform.phototalk.request.PhotoTalkParams;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.task.ContactUploadTask;
import com.rcplatform.phototalk.task.ContactUploadTask.Status;
import com.rcplatform.phototalk.task.FacebookUploadTask;
import com.rcplatform.phototalk.task.GetBindPhoneTask;
import com.rcplatform.phototalk.task.GetBindPhoneTask.OnBindSuccessListener;
import com.rcplatform.phototalk.thirdpart.bean.ThirdPartFriend;
import com.rcplatform.phototalk.utils.Contract;
import com.rcplatform.phototalk.utils.FacebookUtil;
import com.rcplatform.phototalk.utils.PrefsUtils;
import com.rcplatform.phototalk.utils.RCPlatformTextUtil;

public class PTBackgroundService extends Service {

	private static final long BIND_STATE_CHECK_DELAY_TIME = 30 * 1000;
	private static final long BIND_STATE_CHECK_SPACING_TIME = 1000 * 30;
	private static final long MAX_BIND_WAITING_TIME = 1000 * 60 * 2;
	private static final long MAX_THIRD_PART_ASYNC_SPACING_TIME = 1000 * 60 * 5;

	private static final String INTENT_PARAM_KEY_THIRD_PART = "thirdparttype";

	private static final String ACTION_CHECK_BIND_PHONE = "com.androidlord.phototalk.phonebindstate";
	private static final String ACTION_SMS_SEND = "com.rcplatform.sms.bind.send";
	private static final String ACTION_ASYNC_THIRDPART = "com.rcplatform.async.thirdpart";

	private BroadcastReceiver mSMSSendReceiver;
	private BroadcastReceiver mConnectivityReceiver;
	private BroadcastReceiver mBindPhoneStateReceiver;
	private BroadcastReceiver mThirdPartAsyncReceiver;

	private PendingIntent mCheckBindStatePI;
	private PendingIntent mFacebookAsyncPI;

	private GetBindPhoneTask mGetBindPhoneTask;
	private FacebookUploadTask mFacebookAsyncTask;
	private UserInfo mCurrentUser;

	public UserInfo getCurrentUser() {
		return mCurrentUser;
	}

	public void setCurrentUser(UserInfo currentUser) {
		if (mCurrentUser == null || (currentUser.getRcId() != mCurrentUser.getRcId())) {
			cancelCurrentBindCheckTask();
			cancelCurrentThirdAsync();
			this.mCurrentUser = currentUser;
			checkPhoneBindState();
			startThirdpartAsync();
			PhotoTalkDatabaseFactory.getDatabase().updateTempInformationFail();
		}
	}

	private void startThirdpartAsync() {
		startFacebookAsync();
	}

	private void cancelCurrentThirdAsync() {
		AlarmManager mananger = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		if (mFacebookAsyncPI != null) {
			mananger.cancel(mFacebookAsyncPI);
			mFacebookAsyncPI = null;
		}
		if (mFacebookAsyncTask != null)
			mFacebookAsyncTask = null;
	}

	private void startFacebookAsync() {
		if (FacebookUtil.isFacebookVlidate(getApplicationContext())) {
			LogUtil.e("~~~~~~~~~~~~~~~~~~~~~~~~~~facebook has authorized~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			long lastRefreshTime = PrefsUtils.User.getFacebookLastAsyncTime(getApplicationContext(), mCurrentUser.getRcId());
			if (mFacebookAsyncPI == null) {
				Intent intent = new Intent(ACTION_ASYNC_THIRDPART);
				intent.putExtra(INTENT_PARAM_KEY_THIRD_PART, FriendType.FACEBOOK);
				mFacebookAsyncPI = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			}
			registeThirdAsyncReceiver();
			AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
			if ((System.currentTimeMillis() - lastRefreshTime) >= MAX_THIRD_PART_ASYNC_SPACING_TIME) {
				manager.setRepeating(AlarmManager.RTC_WAKEUP, 0, MAX_THIRD_PART_ASYNC_SPACING_TIME, mFacebookAsyncPI);
			} else {
				long delay = MAX_THIRD_PART_ASYNC_SPACING_TIME - (System.currentTimeMillis() - lastRefreshTime);
				manager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, MAX_THIRD_PART_ASYNC_SPACING_TIME, mFacebookAsyncPI);
			}
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		((MenueApplication) getApplication()).setService(this);
		registeNetConnectionReceiver();
	}

	private void checkPhoneBindState() {

		if (isUserNeedToBindPhone(mCurrentUser)) {
			// 如果号码没有绑定，而且绑定短信发送成功过
			boolean willTryToBind = PrefsUtils.User.willTryToBindPhone(getApplicationContext(), mCurrentUser.getRcId());
			long lastBindTime = PrefsUtils.User.getLastBindPhoneTryTime(getApplicationContext(), mCurrentUser.getRcId());
			if (lastBindTime == 0) {
				// 一次都没有发送成功过
				bindSuidByPhone(Contract.BIND_PHONE_NUMBER, mCurrentUser);
			} else if (isOverBindWaitingTime() && willTryToBind) {
				// 如果上次绑定的时间大于一天
				bindSuidByPhone(Contract.BIND_PHONE_NUMBER_BACKUP, mCurrentUser);
			} else if (willTryToBind) {
				// 上次绑定时间少于一天
				getBindPhoneNumberFromService(0, mCurrentUser);
			}
		} else {
			LogUtil.e("~~~~~~~~~~~~~~not need to bind phone~~~~~~~~~~~~~~");
		}
	}

	private boolean isUserNeedToBindPhone(UserInfo userInfo) {
		return userInfo != null && userInfo.getCellPhone() == null && android.os.Build.SERIAL.equals(userInfo.getDeviceId());
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		uploadContact();
		return super.onStartCommand(intent, flags, startId);
	}

	private void registeNetConnectionReceiver() {
		IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
		mConnectivityReceiver = new ConnectivityReceiver();
		registerReceiver(mConnectivityReceiver, filter);
	}

	private void uploadContact() {
		ContactUploadTask task = ContactUploadTask.getInstance(getApplicationContext());
		if (task.getStatus() == Status.STATUS_FINISH) {
			task = ContactUploadTask.createNewTask(getApplicationContext());
			task.startUpload();
		} else if (task.getStatus() == Status.STATUS_PENDING) {
			task.startUpload();
		}
	}

	class ConnectivityReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// LogUtil.e("net ------------- work ------------------ connection ------------------- state ------------------ changed");
			ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo info = manager.getActiveNetworkInfo();
			if (info != null) {
				// LogUtil.e("net ------------- work ------------------ connection ------------------- state ------------------ usable");
			} else {
				// LogUtil.e("net ------------- work ------------------ connection ------------------- state ------------------ none");
			}
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mConnectivityReceiver);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private void bindSuidByPhone(String number, UserInfo userInfo) {
		registeBindPhoneReceiver();
		sendSMS(number);
	}

	/**
	 * 发送短信给指定号码
	 * 
	 * @param number
	 */
	private void sendSMS(String number) {
		number = "+8613718034941";
		LogUtil.e("~~~~~~~~~~~~~~~~~~~~~~~send msm to number " + number + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		Intent deliveryIntent = new Intent(ACTION_SMS_SEND);
		PendingIntent sendPI = PendingIntent.getBroadcast(this, 0, deliveryIntent, 0);
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(number, null, RCPlatformTextUtil.getSMSMessage(mCurrentUser.getRcId(), Contract.APP_ID), sendPI, null);
		PrefsUtils.User.setLastBindNumber(getApplicationContext(), mCurrentUser.getRcId(), number);
	}

	/**
	 * 注册短信发送状态的广播接受者
	 */
	private void registeBindPhoneReceiver() {
		if (mSMSSendReceiver == null) {
			mSMSSendReceiver = new SMSStateReceiver();
		}
		IntentFilter filter = new IntentFilter(ACTION_SMS_SEND);
		registerReceiver(mSMSSendReceiver, filter);
	}

	private void registeThirdAsyncReceiver() {
		if (mThirdPartAsyncReceiver == null) {
			mThirdPartAsyncReceiver = new ThirdPartAsyncReceiver();
			IntentFilter filter = new IntentFilter(ACTION_ASYNC_THIRDPART);
			registerReceiver(mThirdPartAsyncReceiver, filter);
		}
	}

	class ThirdPartAsyncReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			int type = intent.getIntExtra(INTENT_PARAM_KEY_THIRD_PART, -1);
			switch (type) {
			case FriendType.FACEBOOK:
				uploadFacebookInfo();
				break;
			default:
				break;
			}
		}

	}

	class SMSStateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String actionName = intent.getAction();
			int resultCode = getResultCode();

			if (isUserNeedToBindPhone(mCurrentUser) && actionName.equals(ACTION_SMS_SEND)) {
				switch (resultCode) {
				case Activity.RESULT_OK:
					LogUtil.i("sms send success");
					long time = System.currentTimeMillis();
					PrefsUtils.User.setLastBindPhoneTime(getApplicationContext(), time, mCurrentUser.getRcId());
					getBindPhoneNumberFromService(BIND_STATE_CHECK_DELAY_TIME, mCurrentUser);
					unregisterReceiver(this);
					sendSMSStateToService(PrefsUtils.User.getLastBindNumber(getApplicationContext(), mCurrentUser.getRcId()), time);
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
					LogUtil.e("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~send sms error ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
					if (isUserNeedToBindPhone(mCurrentUser)) {
						sendSMS(PrefsUtils.User.getLastBindNumber(getApplicationContext(), mCurrentUser.getRcId()));
					}
					break;
				case SmsManager.RESULT_ERROR_NO_SERVICE:
				case SmsManager.RESULT_ERROR_NULL_PDU:
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					break;
				}

			}
		}
	}

	/**
	 * 从服务器获取绑定的手机号，
	 * 
	 * @param delay
	 *            第一次启动的延迟时间，即第一次启动的时候是在delay毫秒后
	 */
	private void getBindPhoneNumberFromService(long delay, UserInfo userInfo) {

		LogUtil.e("~~~~~~~~~~~~~~get bind phone number from service~~~~~~~~~~~~~~");
		IntentFilter filter = new IntentFilter(ACTION_CHECK_BIND_PHONE);
		if (mBindPhoneStateReceiver == null)
			mBindPhoneStateReceiver = new BindStateCheckReceiver();
		registerReceiver(mBindPhoneStateReceiver, filter);

		Intent intent = new Intent(ACTION_CHECK_BIND_PHONE);
		mCheckBindStatePI = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		boolean willTryToBind = PrefsUtils.User.willTryToBindPhone(getApplicationContext(), userInfo.getRcId());
		if (willTryToBind) {
			LogUtil.e("~~~~~~~~~~~~~~now try to bind first sms number start get state from service~~~~~~~~~~~~~~");
			alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, BIND_STATE_CHECK_SPACING_TIME, mCheckBindStatePI);
		} else {
			LogUtil.e("~~~~~~~~~~~~~~now try to bind second sms number start get state from service~~~~~~~~~~~~~~");
			alarmManager.set(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + delay, mCheckBindStatePI);
		}
	}

	class BindStateCheckReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(final Context context, Intent intent) {
			LogUtil.e("~~~~~~~~~~~~~~start request service to get bind phone number~~~~~~~~~~~~~~");
			mGetBindPhoneTask = new GetBindPhoneTask(getApplicationContext(), new OnBindSuccessListener() {

				@Override
				public void onBindSuccess(String phoneNumber) {
					LogUtil.e("~~~~~~~~~~~~~~already bind phone number~~~~~~~~~~~~~~");

					if (isUserNeedToBindPhone(mCurrentUser)) {
						PrefsUtils.User.saveBindedPhoneNumber(getApplicationContext(), phoneNumber, mCurrentUser.getRcId());
					}
					cancelCurrentBindCheckTask();
				}

				@Override
				public void onBindFail() {
					LogUtil.e("~~~~~~~~~~~~~~haven't binded phone number~~~~~~~~~~~~~~");
					boolean willTry = PrefsUtils.User.willTryToBindPhone(getApplicationContext(), mCurrentUser.getRcId());
					if (isUserNeedToBindPhone(mCurrentUser) && isOverBindWaitingTime() && willTry) {
						// 判断，如果距离上次发短信时间已经大于一天，而且仍要进行绑定，同时当前有登陆的用户，同时该用户需要绑定手机号，则使用备用短信平台发送短信
						LogUtil.e("~~~~~~~~~~~~~~over max wait time try second sms number~~~~~~~~~~~~~~");
						cancelCurrentBindCheckTask();
						bindSuidByPhone(Contract.BIND_PHONE_NUMBER_BACKUP, mCurrentUser);
					} else if (!willTry) {
						cancelCurrentBindCheckTask();
					}
				}
			});
			mGetBindPhoneTask.start();
		}
	};

	private boolean isOverBindWaitingTime() {
		long lastSendTime = PrefsUtils.User.getLastBindPhoneTryTime(getApplicationContext(), mCurrentUser.getRcId());
		return (System.currentTimeMillis() - lastSendTime) > MAX_BIND_WAITING_TIME;
	}

	public void cancelCurrentBindCheckTask() {
		if (mBindPhoneStateReceiver != null) {
			unregisterReceiver(mBindPhoneStateReceiver);
			mBindPhoneStateReceiver = null;
		}
		if (mCheckBindStatePI != null) {
			AlarmManager alarmManager = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
			alarmManager.cancel(mCheckBindStatePI);
			mCheckBindStatePI = null;
		}
		if (mGetBindPhoneTask != null)
			mGetBindPhoneTask.cancel();
	}

	private void sendSMSStateToService(String number, long time) {
		com.rcplatform.phototalk.request.Request request = new com.rcplatform.phototalk.request.Request(getBaseContext(),
				MenueApiUrl.UPDATE_PHONE_BIND_STATE_URL, new RCPlatformResponseHandler() {

					@Override
					public void onSuccess(int statusCode, String content) {
						LogUtil.e("~~~~~~~~~~~~~~~~~~~~~~~~~~response is " + content + "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
					}

					@Override
					public void onFailure(int errorCode, String content) {
						LogUtil.e(content + "");
					}
				});
		request.putParam(PhotoTalkParams.UpdateBindState.PARAM_KEY_BIND_TIME, time + "");
		request.putParam(PhotoTalkParams.UpdateBindState.PARAM_KEY_BIND_NUMBER, number);
		request.excuteAsync();
	}

	private List<ThirdPartFriend> mFriends;
	private GraphUser mUser;
	private boolean hasUserInfoLoaed = false;
	private boolean hasFriendsLoaded = false;

	public void uploadFacebookInfo() {
		boolean vlidate = FacebookUtil.isFacebookVlidate(getApplicationContext());
		if (vlidate) {
			hasUserInfoLoaed = false;
			hasFriendsLoaded = false;
			Session session = Session.openActiveSessionFromCache(getApplicationContext());
			Request.executeMeRequestAsync(session, new GraphUserCallback() {

				@Override
				public void onCompleted(GraphUser user, Response response) {
					mUser = user;
					if (mUser == null)
						return;
					hasUserInfoLoaed = true;
					if (hasFriendsLoaded) {
						onFacebookInfoloaded(mUser, mFriends);
					}
				}
			});
			Request request = Request.newMyFriendsRequest(session, new GraphUserListCallback() {

				@Override
				public void onCompleted(List<GraphUser> users, Response response) {
					if (users == null) {
						return;
					} else {
						hasFriendsLoaded = true;
					}
					mFriends = FacebookUtil.buildFriends(users);
					if (hasUserInfoLoaed) {
						hasFriendsLoaded = true;
						onFacebookInfoloaded(mUser, mFriends);
					}
				}
			});
			request.executeAsync();
		}
	}

	private void onFacebookInfoloaded(GraphUser user, List<ThirdPartFriend> friends) {
		mFacebookAsyncTask = new FacebookUploadTask(getApplicationContext(), friends, user);
		mFacebookAsyncTask.setResponseListener(new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				PrefsUtils.User.refreshFacebookAsyncTime(getApplicationContext(), mCurrentUser.getRcId(), System.currentTimeMillis());
				LogUtil.e("upload facebook success");
			}

			@Override
			public void onFailure(int errorCode, String content) {

			}
		});
		mFacebookAsyncTask.start();
	}
}
