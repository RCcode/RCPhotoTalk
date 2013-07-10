package com.rcplatform.phototalk.clienservice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.telephony.SmsManager;
import android.widget.RemoteViews;

import com.facebook.Request;
import com.facebook.Request.GraphUserCallback;
import com.facebook.Request.GraphUserListCallback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;
import com.perm.kate.api.Api;
import com.perm.kate.api.User;
import com.rcplatform.phototalk.PhotoTalkApplication;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.WelcomeActivity;
import com.rcplatform.phototalk.api.PhotoTalkApiUrl;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.FriendType;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.galhttprequest.LogUtil;
import com.rcplatform.phototalk.logic.controller.InformationPageController;
import com.rcplatform.phototalk.proxy.FriendsProxy;
import com.rcplatform.phototalk.request.JSONConver;
import com.rcplatform.phototalk.request.PhotoTalkParams;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.task.ContactUploadTask;
import com.rcplatform.phototalk.task.ContactUploadTask.Status;
import com.rcplatform.phototalk.task.GetBindPhoneTask;
import com.rcplatform.phototalk.task.GetBindPhoneTask.OnBindSuccessListener;
import com.rcplatform.phototalk.task.ThirdPartInfoUploadTask;
import com.rcplatform.phototalk.thirdpart.bean.ThirdPartUser;
import com.rcplatform.phototalk.thirdpart.utils.ThirdPartUtils;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.Constants.Action;
import com.rcplatform.phototalk.utils.Constants.ApplicationStartMode;
import com.rcplatform.phototalk.utils.PhotoTalkUtils;
import com.rcplatform.phototalk.utils.PrefsUtils;
import com.rcplatform.phototalk.utils.RCPlatformTextUtil;
import com.rcplatform.phototalk.utils.RCThreadPool;
import com.rcplatform.phototalk.utils.Utils;

public class PTBackgroundService extends Service {

	private static final int MSG_WHAT_NEWINFOS = 20000;
	private static final int MSG_WHAT_NEW_RECOMMENDS = 20001;

	private static final long BIND_STATE_CHECK_DELAY_TIME = 1000 * 60;
	private static final long BIND_STATE_CHECK_SPACING_TIME = 1000 * 60 * 30;
	private static final long MAX_BIND_WAITING_TIME = 1000 * 60 * 60 * 2;

	private static final long MAX_THIRD_PART_SYNC_SPACING_TIME = 1000 * 60 * 60 * 24 * 7;

	private static final long RETRY_SEND_SMS_WATTING_TIME = 1000 * 60 * 5;

	private static final String INTENT_PARAM_KEY_THIRD_PART = "thirdparttype";

	private static final String ACTION_CHECK_BIND_PHONE = "com.androidlord.phototalk.phonebindstate";
	private static final String ACTION_SMS_SEND = "com.rcplatform.sms.bind.send";

	private BroadcastReceiver mSMSSendReceiver;
	private BroadcastReceiver mConnectivityReceiver;
	private BroadcastReceiver mBindPhoneStateReceiver;

	private PendingIntent mCheckBindStatePI;

	private GetBindPhoneTask mGetBindPhoneTask;
	private ThirdPartInfoUploadTask mFacebookAsyncTask;
	private UserInfo mCurrentUser;

	public UserInfo getCurrentUser() {
		return mCurrentUser;
	}

	public void setCurrentUser(UserInfo currentUser) {
		if (mCurrentUser == null || (!currentUser.getRcId().equals(mCurrentUser.getRcId()))) {
			cancelCurrentBindCheckTask();
			this.mCurrentUser = currentUser;
			checkPhoneBindState();
			PhotoTalkDatabaseFactory.getDatabase().updateTempInformationFail();
		} else {
			this.mCurrentUser = currentUser;
		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		((PhotoTalkApplication) getApplication()).setService(this);
		registeTimeTickReceiver();
		registeGCMReceiver();
		LogUtil.e("background service oncreate over");
	}

	private void registeTimeTickReceiver() {
		IntentFilter filter = new IntentFilter(Intent.ACTION_TIME_TICK);
		registerReceiver(mTimeTickReceiver, filter);
	}

	private void checkPhoneBindState() {

		if (isUserNeedToBindPhone(mCurrentUser)) {
			// 如果号码没有绑定，而且绑定短信发送成功过
			boolean willTryToBind = PrefsUtils.User.MobilePhoneBind.willTryToBindPhone(getApplicationContext(), mCurrentUser.getRcId());
			long lastBindTime = PrefsUtils.User.MobilePhoneBind.getLastBindPhoneTryTime(getApplicationContext(), mCurrentUser.getRcId());
			if (lastBindTime == 0) {
				// 一次都没有发送成功过
				bindSuidByPhone(Constants.BIND_PHONE_NUMBER, mCurrentUser);
			} else if (isOverBindWaitingTime() && willTryToBind) {
				// 如果上次绑定的时间大于一天
				bindSuidByPhone(Constants.BIND_PHONE_NUMBER_BACKUP, mCurrentUser);
			} else if (willTryToBind) {
				// 上次绑定时间少于一天
				getBindPhoneNumberFromService(0, mCurrentUser);
			}
		} else {
			LogUtil.e("~~~~~~~~~~~~~~not need to bind phone~~~~~~~~~~~~~~");
		}
	}

	private boolean isUserNeedToBindPhone(UserInfo userInfo) {
		return PhotoTalkUtils.isUserNeedToBindPhone(getApplicationContext(), userInfo);
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

	private void registeGCMReceiver() {
		IntentFilter filter = new IntentFilter(Action.ACTION_GCM_MESSAGE);
		registerReceiver(mGCMReceiver, filter);
	}

	private void uploadContact() {
		if (mCurrentUser != null) {
			ContactUploadTask task = ContactUploadTask.getInstance(getApplicationContext());
			if (task.getStatus() == Status.STATUS_FINISH) {
				task = ContactUploadTask.createNewTask(getApplicationContext());
				task.startUpload();
			} else if (task.getStatus() == Status.STATUS_PENDING) {
				task.startUpload();
			}
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
		LogUtil.e("destroy service ~~~~~~~~~~~~~~~~phototalk background service");
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	private void bindSuidByPhone(String number, UserInfo userInfo) {
		registeBindPhoneReceiver();
		sendSMS(number, userInfo.getRcId());
	}

	/**
	 * 发送短信给指定号码
	 * 
	 * @param number
	 */
	private void sendSMS(String number, String rcId) {
		if (mCurrentUser != null && mCurrentUser.getRcId().equals(rcId)) {
			// LogUtil.e("~~~~~~~~~~~~~~~~~~~~~~~send msm to number " + number +
			// "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
			try {
				Intent deliveryIntent = new Intent(ACTION_SMS_SEND);
				PendingIntent sendPI = PendingIntent.getBroadcast(this, 0, deliveryIntent, 0);
				SmsManager smsManager = SmsManager.getDefault();
				smsManager.sendTextMessage(number, null, RCPlatformTextUtil.getSMSMessage(mCurrentUser.getRcId(), Constants.APP_ID), sendPI, null);
				PrefsUtils.User.MobilePhoneBind.setLastBindNumber(getApplicationContext(), mCurrentUser.getRcId(), number);
				PrefsUtils.User.MobilePhoneBind.setFirstBindPhoneTime(getApplicationContext(), mCurrentUser.getRcId(), System.currentTimeMillis());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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

	class SMSStateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String actionName = intent.getAction();
			int resultCode = getResultCode();

			if (isUserNeedToBindPhone(mCurrentUser) && actionName.equals(ACTION_SMS_SEND)) {
				switch (resultCode) {
				case Activity.RESULT_OK:
					if (mCurrentUser != null) {
						LogUtil.i("sms send success");
						long time = System.currentTimeMillis();
						PrefsUtils.User.MobilePhoneBind.setLastBindPhoneTime(getApplicationContext(), time, mCurrentUser.getRcId());
						getBindPhoneNumberFromService(BIND_STATE_CHECK_DELAY_TIME, mCurrentUser);
						unregisterReceiver(this);
						sendSMSStateToService(PrefsUtils.User.MobilePhoneBind.getLastBindNumber(getApplicationContext(), mCurrentUser.getRcId()), time);
					}
					break;
				case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
				case SmsManager.RESULT_ERROR_NO_SERVICE:
				case SmsManager.RESULT_ERROR_NULL_PDU:
				case SmsManager.RESULT_ERROR_RADIO_OFF:
					LogUtil.e("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~send sms error ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
					if (isUserNeedToBindPhone(mCurrentUser)) {
						final String rcId = mCurrentUser.getRcId();
						newInformationHandler.postDelayed(new Runnable() {

							@Override
							public void run() {
								sendSMS(PrefsUtils.User.MobilePhoneBind.getLastBindNumber(getApplicationContext(), rcId), rcId);
							}
						}, RETRY_SEND_SMS_WATTING_TIME);

					}
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
		boolean willTryToBind = PrefsUtils.User.MobilePhoneBind.willTryToBindPhone(getApplicationContext(), userInfo.getRcId());
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
						PrefsUtils.User.MobilePhoneBind.saveBindedPhoneNumber(getApplicationContext(), phoneNumber, mCurrentUser.getRcId());
						mCurrentUser.setCellPhone(phoneNumber);
					}
					cancelCurrentBindCheckTask();
				}

				@Override
				public void onBindFail() {
					LogUtil.e("~~~~~~~~~~~~~~haven't binded phone number~~~~~~~~~~~~~~");
					boolean willTry = PrefsUtils.User.MobilePhoneBind.willTryToBindPhone(getApplicationContext(), mCurrentUser.getRcId());
					if (isUserNeedToBindPhone(mCurrentUser) && isOverBindWaitingTime() && willTry) {
						// 判断，如果距离上次发短信时间已经大于一天，而且仍要进行绑定，同时当前有登陆的用户，同时该用户需要绑定手机号，则使用备用短信平台发送短信
						LogUtil.e("~~~~~~~~~~~~~~over max wait time try second sms number~~~~~~~~~~~~~~");
						cancelCurrentBindCheckTask();
						bindSuidByPhone(Constants.BIND_PHONE_NUMBER_BACKUP, mCurrentUser);
					} else if (!willTry) {
						cancelCurrentBindCheckTask();
					}
				}
			});
			mGetBindPhoneTask.start();
		}
	};

	private boolean isOverBindWaitingTime() {
		long lastSendTime = PrefsUtils.User.MobilePhoneBind.getLastBindPhoneTryTime(getApplicationContext(), mCurrentUser.getRcId());
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
				PhotoTalkApiUrl.UPDATE_PHONE_BIND_STATE_URL, new RCPlatformResponseHandler() {

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

	private List<ThirdPartUser> mFriends;
	private GraphUser mUser;
	private boolean hasUserInfoLoaed = false;
	private boolean hasFriendsLoaded = false;

	public void uploadFacebookInfo() {
		boolean vlidate = ThirdPartUtils.isFacebookVlidate(getApplicationContext());
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
					mFriends = ThirdPartUtils.parserFacebookUserToThirdPartUser(users);
					if (hasUserInfoLoaed) {
						hasFriendsLoaded = true;
						onFacebookInfoloaded(mUser, mFriends);
					}
				}
			});
			request.executeAsync();
		}
	}

	private void onFacebookInfoloaded(GraphUser user, List<ThirdPartUser> friends) {
		if (mCurrentUser != null && ThirdPartUtils.isFacebookVlidate(getApplicationContext())) {
			PhotoTalkDatabaseFactory.getDatabase().saveThirdPartFriends(friends, FriendType.FACEBOOK);
			PrefsUtils.User.ThirdPart.setFacebookUserName(this, getCurrentUser().getRcId(), ThirdPartUtils.parserFacebookUserToThirdPartUser(user).getNick());
			PrefsUtils.User.ThirdPart.refreshFacebookAsyncTime(getApplicationContext(), mCurrentUser.getRcId());
			mFacebookAsyncTask = new ThirdPartInfoUploadTask(getApplicationContext(), friends, ThirdPartUtils.parserFacebookUserToThirdPartUser(user),
					FriendType.FACEBOOK, new RCPlatformResponseHandler() {

						@Override
						public void onSuccess(int statusCode, String content) {
							PrefsUtils.User.ThirdPart.refreshFacebookAsyncTime(getApplicationContext(), mCurrentUser.getRcId());
							LogUtil.e("upload facebook success");
						}

						@Override
						public void onFailure(int errorCode, String content) {

						}
					});
			mFacebookAsyncTask.start();
		}
	}

	private BroadcastReceiver mTimeTickReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			LogUtil.e("time change a minute");
			long currentTime = System.currentTimeMillis();
			if (mCurrentUser != null) {
				if (ThirdPartUtils.isVKVlidated(getApplicationContext(), mCurrentUser.getRcId())
						&& (currentTime - PrefsUtils.User.ThirdPart.getVKSyncTime(getApplicationContext(), mCurrentUser.getRcId())) > MAX_THIRD_PART_SYNC_SPACING_TIME) {
					LogUtil.e("need to update vk info");
					updateVKInfo();
				}
				if (ThirdPartUtils.isFacebookVlidate(getApplicationContext())
						&& (currentTime - PrefsUtils.User.ThirdPart.getFacebookLastAsyncTime(getApplicationContext(), mCurrentUser.getRcId())) > MAX_THIRD_PART_SYNC_SPACING_TIME) {
					LogUtil.e("need to update facebook info");
					uploadFacebookInfo();
				}
				tryToResetDriftFishTime();
			}
		}
	};

	private void updateVKInfo() {
		new Thread() {
			@Override
			public void run() {
				Object[] vkAccount = PrefsUtils.User.ThirdPart.getVKAccount(getApplicationContext(), getCurrentUser().getRcId());
				if (vkAccount != null) {
					try {
						Api api = new Api((String) vkAccount[0], com.rcplatform.phototalk.utils.Constants.VK_API_ID);
						long userId = (Long) vkAccount[1];
						ArrayList<User> users = api.getFriends(userId, null, null, null, null);
						List<ThirdPartUser> vkFriends = ThirdPartUtils.parserVKUserToThirdPartUser(users);
						PhotoTalkDatabaseFactory.getDatabase().saveThirdPartFriends(vkFriends, FriendType.VK);
						List<User> userProfiles = api.getProfiles(Arrays.asList(userId), null, null, null, null, null);
						ThirdPartUser user = ThirdPartUtils.parserVKUserToThirdPartUser(userProfiles.get(0));
						PrefsUtils.User.ThirdPart.refreshVKSyncTime(getApplicationContext(), mCurrentUser.getRcId());
						new ThirdPartInfoUploadTask(getBaseContext(), vkFriends, user, FriendType.VK, new RCPlatformResponseHandler() {

							@Override
							public void onSuccess(int statusCode, String content) {
							}

							@Override
							public void onFailure(int errorCode, String content) {
							}
						}).start();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}

	protected void tryToResetDriftFishTime() {
		long currentTime = System.currentTimeMillis();
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(currentTime);
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		int minute = calendar.get(Calendar.MINUTE);
		if (hour == 0 && minute == 0) {
			PrefsUtils.User.setFishLeaveTime(getApplicationContext(), mCurrentUser.getRcId(), PrefsUtils.AppInfo.getMaxFishTime(getApplicationContext()));
		}
	}

	private BroadcastReceiver mGCMReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (mCurrentUser != null) {
				String type = intent.getStringExtra(Constants.Message.MESSAGE_TYPE_KEY);
				if (type != null) {
					if (type.equals(Constants.Message.MESSAGE_TYPE_NEW_INFORMATIONS)) {
						handlerNewInformation(intent);
					} else if (type.equals(Constants.Message.MESSAGE_TYPE_NEW_RECOMMENDS)) {
						handlerNewRecommends(intent, mCurrentUser.getRcId());
					}
				}
			}
		};
	};

	private void handlerNewInformation(final Intent data) {
		LogUtil.e("gcm receive informations....");
		RCThreadPool.getInstance().addTask(new Runnable() {

			@Override
			public void run() {
				List<Information> gcms = JSONConver.jsonToInformations(data.getStringExtra(Constants.Message.MESSAGE_CONTENT_KEY));
				Map<Integer, List<Information>> result = PhotoTalkDatabaseFactory.getDatabase().filterNewInformations(gcms, mCurrentUser);
				Message msg = newInformationHandler.obtainMessage();
				msg.what = MSG_WHAT_NEWINFOS;
				msg.obj = result;
				newInformationHandler.sendMessage(msg);
			}
		});
	}

	private void handlerNewRecommends(final Intent data, final String rcId) {
		LogUtil.e("gcm receive new recommends....");
		final String msg = data.getStringExtra(Constants.Message.MESSAGE_CONTENT_KEY);
		FriendsProxy.getAllRecommends(PTBackgroundService.this, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, final String content) {
				if (mCurrentUser != null && mCurrentUser.getRcId().equals(rcId)) {
					RCThreadPool.getInstance().addTask(new Runnable() {

						@Override
						public void run() {
							try {
								JSONObject jsonObject = new JSONObject(content);
								List<Friend> recommends = JSONConver.jsonToFriends(jsonObject.getJSONArray("recommendUsers").toString());
								if (recommends != null && recommends.size() > 0) {
									PhotoTalkDatabaseFactory.getDatabase().saveRecommends(recommends);
									PrefsUtils.User.setNewRecommends(getApplicationContext(), mCurrentUser.getRcId(), true);
									Message handlerMsg = newInformationHandler.obtainMessage();
									handlerMsg.what = MSG_WHAT_NEW_RECOMMENDS;
									handlerMsg.obj = msg;
									newInformationHandler.sendMessage(handlerMsg);
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					});
				}
			}

			@Override
			public void onFailure(int errorCode, String content) {

			}
		});
	}

	private static void notifyNewRecommends(Context context, String msg) {
		try {

			Notification notification = new Notification();
			notification.icon = R.drawable.ic_launcher;

			notification.contentView = new RemoteViews(context.getPackageName(), R.layout.gcm_notification);
			notification.contentView.setImageViewResource(R.id.gcm_image, R.drawable.ic_launcher);
			notification.contentView.setTextViewText(R.id.gcm_title, context.getText(R.string.app_name));
			notification.contentView.setTextViewText(R.id.gcm_decs, msg);
			notification.when = System.currentTimeMillis();
			notification.defaults |= Notification.DEFAULT_SOUND;
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
			NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
			;
			Intent notificationIntent = new Intent(context, WelcomeActivity.class);
			notificationIntent.putExtra(Constants.ApplicationStartMode.APPLICATION_START_KEY, ApplicationStartMode.APPLICATION_START_RECOMMENDS);
			// set intent so it does not start a new activity
			PendingIntent intent = PendingIntent.getActivity(context, 0, notificationIntent, 0);
			notification.contentIntent = intent;
			notificationManager.notify(0, notification);
		} catch (Exception e) {

		}
	}

	private Handler newInformationHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == MSG_WHAT_NEWINFOS) {
				InformationPageController.getInstance().onNewInformation((Map<Integer, List<Information>>) msg.obj);
			} else if (msg.what == MSG_WHAT_NEW_RECOMMENDS) {
				String notifyMessage = (String) msg.obj;
				InformationPageController.getInstance().onNewRecommends();
				if (!Utils.isRunningForeground(PTBackgroundService.this))
					notifyNewRecommends(getApplicationContext(), notifyMessage);
			}
		};
	};
}
