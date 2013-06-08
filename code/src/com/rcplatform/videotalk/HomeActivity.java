package com.rcplatform.videotalk;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.google.android.gcm.ServerUtilities;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;
import com.rcplatform.videotalk.R;
import com.rcplatform.videotalk.activity.MenuBaseActivity;
import com.rcplatform.videotalk.adapter.PhotoTalkMessageAdapter;
import com.rcplatform.videotalk.bean.AppInfo;
import com.rcplatform.videotalk.bean.Friend;
import com.rcplatform.videotalk.bean.Information;
import com.rcplatform.videotalk.bean.InformationState;
import com.rcplatform.videotalk.bean.InformationType;
import com.rcplatform.videotalk.bean.UserInfo;
import com.rcplatform.videotalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.videotalk.galhttprequest.LogUtil;
import com.rcplatform.videotalk.image.downloader.RCPlatformImageLoader;
import com.rcplatform.videotalk.logic.LogicUtils;
import com.rcplatform.videotalk.logic.MessageSender;
import com.rcplatform.videotalk.logic.PhotoInformationCountDownService;
import com.rcplatform.videotalk.logic.controller.InformationPageController;
import com.rcplatform.videotalk.logic.controller.SettingPageController;
import com.rcplatform.videotalk.proxy.UserSettingProxy;
import com.rcplatform.videotalk.request.JSONConver;
import com.rcplatform.videotalk.request.RCPlatformResponseHandler;
import com.rcplatform.videotalk.request.Request;
import com.rcplatform.videotalk.request.inf.FriendDetailListener;
import com.rcplatform.videotalk.request.inf.PhotoSendListener;
import com.rcplatform.videotalk.task.CheckUpdateTask;
import com.rcplatform.videotalk.umeng.EventUtil;
import com.rcplatform.videotalk.utils.Constants;
import com.rcplatform.videotalk.utils.PhotoTalkUtils;
import com.rcplatform.videotalk.utils.PrefsUtils;
import com.rcplatform.videotalk.utils.RCPlatformTextUtil;
import com.rcplatform.videotalk.utils.RCThreadPool;
import com.rcplatform.videotalk.utils.Constants.Action;
import com.rcplatform.videotalk.views.LongClickShowView;
import com.rcplatform.videotalk.views.LongPressDialog;
import com.rcplatform.videotalk.views.RecordTimerLimitView;
import com.rcplatform.videotalk.views.SnapListView;
import com.rcplatform.videotalk.views.SnapShowListener;
import com.rcplatform.videotalk.views.LongPressDialog.OnLongPressItemClickListener;
import com.rcplatform.tigase.TigaseMessageBinderService;
import com.rcplatform.tigase.TigaseMessageBinderService.LocalBinder;
import com.rcplatform.tigase.TigaseMessageReceiver;

/**
 * 主界面. <br>
 * 主界面记录了用户最近的一些记录，包括给哪些好友发送了图片，收到了哪些好友的发送的图片或者好友添加通知，通过主界面可以进入拍照界面，
 * 拍照完成后后可以进行编辑和发送给好友
 * 
 * @version 1.0.0
 */
public class HomeActivity extends MenuBaseActivity implements SnapShowListener, TigaseMessageReceiver {

	private static final int MSG_WHAT_INFORMATION_LOADED = 1;

	private static final int MSG_WHAT_LOCAL_INFORMATION_LOADED = 4;

	protected static final int MSG_TIGASE_NEW_INFORMATION = 3;

	private SnapListView mInformationList;

	private Button mTakePhoto;

	protected LongClickShowView mShowDialog;

	private boolean isShow;

	private TextView mTvContentTitle;

	private TextView mBtFriendList;

	private LongPressDialog mLongPressDialog;

	private PhotoTalkMessageAdapter adapter;

	private CheckUpdateTask mCheckUpdateTask;

	private Request checkTrendRequest;

	private ImageView iconTrendNew;

	private ImageLoader mImageLoader;

	private boolean hasNextPage = true;

	private View loadingFooter;

	public static final String INTENT_KEY_STATE = "state";

	private TextView tvTigaseState;

	private boolean isLoading = false;

	private boolean willQuit = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_view);
		registeTigaseStateChangeReceiver();
		InformationPageController.getInstance().setupController(this);
		mImageLoader = ImageLoader.getInstance();
		initViewAndListener();
		loadLocalInformation();
		getAllPlatformApps();
		onNewTrends();
		checkUpdate();
		checkTrends();
		tvTigaseState = (TextView) findViewById(R.id.tv_test);
		bindTigaseService();
		checkBindPhone();
		//注册GCM
		UserInfo userInfo = PrefsUtils.LoginState.getLoginUser(getApplicationContext());
		if (userInfo != null) {
			try {
				ServerUtilities.register(this, userInfo.getRcId(), userInfo.getToken());
			}
			catch (Exception e) {

			}
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		FlurryAgent.onStartSession(this, Constants.FLURRY_KEY);
	}

	@Override
	protected void onStop() {
		super.onStop();
		FlurryAgent.onEndSession(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		willQuit = false;
	}

	private void checkBindPhone() {
		String rcId = getCurrentUser().getRcId();
		if (TextUtils.isEmpty(getCurrentUser().getCellPhone()) && !PrefsUtils.User.MobilePhoneBind.willTryToBindPhone(this, rcId)
		        && !PrefsUtils.User.MobilePhoneBind.hasAttentionToBindPhone(this, rcId)) {
			PrefsUtils.User.MobilePhoneBind.setAttentionToBindPhone(this, rcId);
			DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
						case DialogInterface.BUTTON_NEGATIVE:
							EventUtil.Main_Photo.rcpt_phonepop_register(baseContext);
							startActivity(RequestSMSActivity.class);
							break;
						case DialogInterface.BUTTON_POSITIVE:
							EventUtil.Main_Photo.rcpt_phonepop_later(baseContext);
							dialog.dismiss();
							break;
					}
				}
			};
			AlertDialog dialog = new AlertDialog.Builder(this).setMessage(R.string.bind_phone_attention)
			        .setNegativeButton(R.string.bind_now, listener).setPositiveButton(R.string.bind_later, listener).create();
			dialog.show();
		}
	}

	private void getAllPlatformApps() {
		UserSettingProxy.getAllAppInfo(this, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, final String content) {

				RCThreadPool.getInstance().addTask(new Runnable() {

					@Override
					public void run() {
						try {
							List<AppInfo> apps = JSONConver.jsonToAppInfos(new JSONObject(content).getJSONArray("allApps").toString());
							PhotoTalkDatabaseFactory.getGlobalDatabase().savePlatformAppInfos(apps);
						}
						catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});

			}

			@Override
			public void onFailure(int errorCode, String content) {
			}
		});
	}

	private void checkTrends() {
		checkTrendRequest = UserSettingProxy.checkTrends(this, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				try {
					JSONObject jsonObject = new JSONObject(content);
					String url = jsonObject.getString("headUrl");
					int trendId = jsonObject.getInt("trendId");
					PrefsUtils.User.saveMaxTrendsId(getApplicationContext(), getCurrentUser().getRcId(), trendId);
					PrefsUtils.User.saveMaxTrendUrl(getApplicationContext(), getCurrentUser().getRcId(), url);
					onNewTrends();
				}
				catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(int errorCode, String content) {
			}
		}, PrefsUtils.User.getShowedMaxTrendsId(this, getCurrentUser().getRcId()));
	}

	private void searchFriend(Friend friend) {
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		Request.executeGetFriendDetailAsync(this, friend, new FriendDetailListener() {

			@Override
			public void onSuccess(Friend friend) {
				dismissLoadingDialog();
				startFriendDetailActivity(friend);
			}

			@Override
			public void onError(int errorCode, String content) {
				dismissLoadingDialog();
				showErrorConfirmDialog(content);
			}
		}, false);
	}

	private void startFriendDetailActivity(Friend friend) {
		Intent intent = new Intent(this, FriendDetailActivity.class);
		intent.putExtra(FriendDetailActivity.PARAM_FRIEND, friend);
		if (!friend.isFriend()) {
			intent.setAction(Constants.Action.ACTION_RECOMMEND_DETAIL);
		} else {
			intent.setAction(Constants.Action.ACTION_FRIEND_DETAIL);
		}
		startActivity(intent);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		String action = intent.getAction();
		if (Action.ACTION_LOGOUT.equals(action)) {
			logout();
			return;
		} else if (Action.ACTION_RELOGIN.equals(action)) {
			relogin();
			return;
		}
	}

	private void logout() {
		startActivity(InitPageActivity.class);
		checkTrendRequest.cancel();
		finish();
	}

	private void relogin() {
		Intent loginIntent = new Intent(this, InitPageActivity.class);
		loginIntent.putExtra(InitPageActivity.REQUEST_PARAM_RELOGIN, true);
		startActivity(loginIntent);
		checkTrendRequest.cancel();
		finish();
	}

	private void sendDataLoadedMessage(List<Information> infos, int what) {
		Message msg = myHandler.obtainMessage();
		msg.what = what;
		msg.obj = infos;
		myHandler.sendMessage(msg);
	}

	private void initViewAndListener() {
		iconTrendNew = (ImageView) findViewById(R.id.iv_trend_new);
		mInformationList = (SnapListView) findViewById(R.id.lv_home);
		mInformationList.setSnapListener(this);
		mTakePhoto = (Button) findViewById(R.id.btn_home_take_photo);

		mTvContentTitle = (TextView) findViewById(R.id.titleContent);
		mTvContentTitle.setVisibility(View.VISIBLE);
		mTvContentTitle.setBackgroundResource(R.drawable.app_title);
		mBtFriendList = (TextView) findViewById(R.id.choosebutton0);
		mBtFriendList.setVisibility(View.VISIBLE);
		mBtFriendList.setBackgroundResource(R.drawable.friendlist_btn);
		mBtFriendList.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				EventUtil.Main_Photo.rcpt_friends(baseContext);
				startActivity(new Intent(HomeActivity.this, MyFriendsActivity.class));
			}
		});
		initForwordButton(R.drawable.more_btn, new OnClickListener() {

			@Override
			public void onClick(View v) {
				EventUtil.Main_Photo.rcpt_more(baseContext);
				startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
			}
		});
		mTakePhoto.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				EventUtil.Main_Photo.rcpt_takephotobutton(baseContext);
				startActivity(new Intent(HomeActivity.this, TakePhotoActivity.class));
			}
		});
		mInformationList.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				EventUtil.Main_Photo.rcpt_main_longpress(baseContext);
				showLongClickDialog(arg2);
				return false;
			}
		});
		mInformationList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Information information = (Information) adapter.getItem(arg2);
				if (information.getType() == InformationType.TYPE_PICTURE_OR_VIDEO
				        && information.getStatu() == InformationState.PhotoInformationState.STATU_NOTICE_SHOWING)
					return;
				if (information.getType() == InformationType.TYPE_PICTURE_OR_VIDEO
				        && information.getStatu() == InformationState.PhotoInformationState.STATU_NOTICE_SEND_OR_LOAD_FAIL) {
					if (LogicUtils.isSender(HomeActivity.this, information)) {
						reSendPhoto(information);
					} else {
						reLoadPictrue(information);
					}
				} else {
					EventUtil.Main_Photo.rcpt_main_profileview(baseContext);
					showFriendDetail(information);
				}
			}
		});
	}

	private void showFriendDetail(Information information) {
		if (information.getSender().getRcId().equals(information.getReceiver().getRcId())) {
			Friend friend = PhotoTalkUtils.userToFriend(getCurrentUser());
			startFriendDetailActivity(friend);
			return;
		}
		Friend friend = new Friend();
		if (LogicUtils.isSender(HomeActivity.this, information)) {
			friend.setRcId(information.getReceiver().getRcId());
		} else {
			friend.setRcId(information.getSender().getRcId());
		}
		searchFriend(friend);
	}

	protected void showLongClickDialog(int position) {
		if (adapter != null) {
			Information record = adapter.getData().get(position);
			if (record != null) {
				if (record.getType() == InformationType.TYPE_PICTURE_OR_VIDEO
				        && (record.getStatu() == InformationState.PhotoInformationState.STATU_NOTICE_SENDING_OR_LOADING || record.getStatu() == InformationState.PhotoInformationState.STATU_NOTICE_SHOWING)) {
					return;
				} else {
					if (mLongPressDialog == null) {
						mLongPressDialog = new LongPressDialog(this, new String[] { getString(R.string.resend), getString(R.string.reload),
						        getString(R.string.delete) }, new OnLongPressItemClickListener() {

							@Override
							public void onClick(int listPostion, int itemIndex) {
								Information record = adapter.getData().get(listPostion);
								switch (itemIndex) {
									case 0:
										reSendPhoto(record);
										mLongPressDialog.hide();
										break;
									// 重新下载
									case 1:
										reLoadPictrue(record);
										mLongPressDialog.hide();
										break;

									case 2:
										deleteInformation(record);
										mLongPressDialog.hide();
										break;
								}
							}
						});
					}

					if (record.getStatu() == InformationState.PhotoInformationState.STATU_NOTICE_SEND_OR_LOAD_FAIL) {
						if (LogicUtils.isSender(this, record))
							mLongPressDialog.show(position, 1);
						else
							mLongPressDialog.show(position, 0);
					} else {
						mLongPressDialog.show(position, 0, 1);
					}
				}
			}
		}
	}

	private void reSendPhoto(final Information information) {
		List<String> friendIds = new ArrayList<String>();
		friendIds.add(information.getReceiver().getRcId());
		information.setStatu(InformationState.PhotoInformationState.STATU_NOTICE_SENDING_OR_LOADING);
		PhotoTalkDatabaseFactory.getDatabase().updateInformationState(information);
		adapter.notifyDataSetChanged();
		Request.sendPhoto(this, information.getCreatetime(), new File(information.getUrl()), information.getTotleLength() + "",
		                  new PhotoSendListener() {

			                  @Override
			                  public void onSendSuccess(long flag) {
				                  InformationPageController.getInstance().onPhotoResendSuccess(information);
			                  }

			                  @Override
			                  public void onFail(long flag, int errorCode, String content) {
				                  InformationPageController.getInstance().onPhotoResendFail(information);
			                  }
		                  }, friendIds, information.isHasVoice(), information.isHasGraf());
	}

	private void deleteInformation(Information information) {
		adapter.getData().remove(information);
		adapter.notifyDataSetChanged();
		LogicUtils.deleteInformation(this, information);
	}

	protected void reLoadPictrue(Information record) {
		RCPlatformImageLoader.LoadPictureForList(this, mInformationList, record);
	}

	private void show(int position) {
		EventUtil.Main_Photo.rcpt_photoview(baseContext);
		Information infoRecord = (Information) adapter.getItem(position);
		if (infoRecord.getType() == InformationType.TYPE_PICTURE_OR_VIDEO && !LogicUtils.isSender(this, infoRecord)) {
			// 表示还未查看
			if (infoRecord.getStatu() == InformationState.PhotoInformationState.STATU_NOTICE_DELIVERED_OR_LOADED
			        || infoRecord.getStatu() == InformationState.PhotoInformationState.STATU_NOTICE_SHOWING) {
				if (mShowDialog == null) {
					LongClickShowView.Builder builder = new LongClickShowView.Builder(HomeActivity.this, R.layout.receice_to_show_view);
					mShowDialog = builder.create();
				}
				RecordTimerLimitView limitView = (RecordTimerLimitView) mInformationList.findViewWithTag(PhotoTalkUtils
				        .getInformationTagBase(infoRecord) + Button.class.getName());
				limitView.setVisibility(View.VISIBLE);
				// limitView.setBackgroundDrawable(null);
				limitView.setBackgroundResource(R.drawable.item_time_bg);
				mShowDialog.ShowDialog(infoRecord);
				(limitView).scheuleTask(infoRecord);
				LogicUtils.startShowPhotoInformation(infoRecord);
				isShow = true;
				// 把数据里面的状态更改为3，已查看
			}
		}
	}

	public int getPostionFromTouch(MotionEvent ev, ListView listview) {
		float eY = ev.getRawY();
		Rect firstR = new Rect();
		Rect LastR = new Rect();
		/*
		 * 获取第一个可见item相对于listview的可见区域,
		 * 注意getChildAt(0)得到的是listview这个ViewGroup中的第一个子view,
		 * 如果listview的adapter中getView进行了优化， 则listview的子view个数是屏幕可最多显示的item个数，
		 * 此时得到的子view并不一定对应adapter数据的第一项。
		 */
		/* 这里得到的position是和list数据列表真实对应的 */
		int first = listview.getFirstVisiblePosition();
		int last = listview.getLastVisiblePosition();

		listview.getChildAt(0).getGlobalVisibleRect(firstR);
		listview.getChildAt(last - first).getGlobalVisibleRect(LastR);

		if (eY < firstR.top || eY > LastR.bottom) {
			return -1;
		}
		int index_in_adapter = first;
		int count = 0;

		/* 第一个可见项是listview header */
		if (0 == first) {
			if (eY > firstR.bottom) {
				/* 触摸不在listview header上，根据触摸的Y坐标和listitem的高度计算索引 */
				count = (int) ((eY - firstR.bottom) / listview.getChildAt(0).getHeight());
				count++;
				index_in_adapter += count;
			} else {
				/* 触摸在listview header上 */
				return 0;
			}
		}
		/* 第一个可见项不是listview header */
		else {
			if (eY > firstR.bottom) {
				/* 用触摸点坐标和item高度相除来计算索引 */
				count = (int) ((eY - firstR.bottom) / listview.getChildAt(0).getHeight());
				count++;
				index_in_adapter += count;
			} else {
			}
		}
		return index_in_adapter;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		int action = event.getAction();
		switch (action) {
			case MotionEvent.ACTION_UP:
				if (isShow) {
					mShowDialog.hideDialog();
					isShow = false;
				}
				break;
		}
		if (isShow) {
			return true;
		}
		return super.dispatchTouchEvent(event);
	}

	private final Handler myHandler = new Handler() {

		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
				case MSG_WHAT_INFORMATION_LOADED:
					initOrRefreshListView((List<Information>) msg.obj);
					break;
				case MSG_TIGASE_NEW_INFORMATION:
					InformationPageController.getInstance().onNewInformation((Map<Integer, List<Information>>) msg.obj);
					break;
				case MSG_WHAT_LOCAL_INFORMATION_LOADED:
					addListData((List<Information>) msg.obj);
					break;
			}
		}
	};

	private void addListData(List<Information> localInformations) {
		if (adapter == null) {
			adapter = new PhotoTalkMessageAdapter(this, localInformations, mInformationList, mImageLoader);
			loadingFooter = getLayoutInflater().inflate(R.layout.information_loading_item, null);
			mInformationList.addFooterView(loadingFooter);
			mInformationList.setAdapter(adapter);
			mInformationList.setOnScrollListener(mScrollListener);
		} else {
			adapter.addDataAtLast(localInformations);
			adapter.notifyDataSetChanged();
		}
		if (localInformations.size() < Constants.INFORMATION_PAGE_SIZE) {
			hasNextPage = false;
			mInformationList.removeFooterView(loadingFooter);
		}
		isLoading = false;
	};

	private void initOrRefreshListView(List<Information> data) {
		if (adapter == null) {
			adapter = new PhotoTalkMessageAdapter(this, data, mInformationList, mImageLoader);
			mInformationList.setAdapter(adapter);
		} else {
			adapter.addData(data);
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onDestroy() {
		unBindTigaseService();
		PhotoInformationCountDownService.getInstance().finishAllShowingMessage();
		unregisterReceiver(mTigaseStateChangeReceiver);
		InformationPageController.getInstance().destroy();
		if (mCheckUpdateTask != null)
			mCheckUpdateTask.cancel();
		if (mShowDialog != null && mShowDialog.isShowing())
			mShowDialog.dismiss();
		if (mLongPressDialog != null && mLongPressDialog.isShowing())
			mLongPressDialog.dismiss();
		ImageLoader.getInstance().stop();
		super.onDestroy();
	}

	private List<Information> getAdapterData() {
		if (adapter == null)
			return null;
		return adapter.getData();
	}

	@Override
	public void snapShow() {
		if (adapter != null) {
			int pos = adapter.getPressedPosition();
			if (pos >= 0) {
				show(pos);
			}
		}
	}

	@Override
	public void snapHide() {
		if (adapter != null)
			adapter.resetPressedInformation();
	}

	private void checkUpdate() {
		mCheckUpdateTask = new CheckUpdateTask(this, true);
		mCheckUpdateTask.start();
	}

	public void onInformationShowEnd(Information information) {
		String tagBase = PhotoTalkUtils.getInformationTagBase(information);
		String buttonTag = tagBase + Button.class.getName();
		String statuTag = tagBase + TextView.class.getName();
		String newTag = tagBase + ImageView.class.getName();
		RecordTimerLimitView timerLimitView = (RecordTimerLimitView) mInformationList.findViewWithTag(buttonTag);
		if (timerLimitView != null) {
			timerLimitView.setVisibility(View.GONE);
		}
		TextView statu = ((TextView) mInformationList.findViewWithTag(statuTag));
		if (statu != null) {
			statu.setText(getString(R.string.receive_looked, RCPlatformTextUtil.getTextFromTimeToNow(this, information.getReceiveTime())));
		}
		View newView = mInformationList.findViewWithTag(newTag);
		if (newView != null)
			newView.setVisibility(View.GONE);
	}

	public void onFriendAdded(Information friend, int addType) {
		if (addType == Constants.FriendAddType.ADD_FRIEND_PASSIVE) {
			List<Information> infos = getAdapterData();
			if (infos != null && infos.size() > 0) {
				for (Information information : infos) {
					if (information.getType() == InformationType.TYPE_FRIEND_REQUEST_NOTICE
					        && information.getSender().getRcId().equals(friend.getSender().getRcId())) {
						information.setStatu(InformationState.FriendRequestInformationState.STATU_QEQUEST_ADD_CONFIRM);
					}
				}
				adapter.notifyDataSetChanged();
			}
		} else if (addType == Constants.FriendAddType.ADD_FRIEND_ACTIVE) {
			friend.setReceiveTime(System.currentTimeMillis());
			List<Information> infos = new ArrayList<Information>();
			infos.add(friend);
			sendDataLoadedMessage(infos, MSG_WHAT_INFORMATION_LOADED);
		}
	}

	public void clearInformation() {
		if (adapter != null) {
			adapter.getData().clear();
			adapter.notifyDataSetChanged();
		}
	}

	public void onPhotoSending(List<Information> informations) {
		sendDataLoadedMessage(informations, MSG_WHAT_INFORMATION_LOADED);
	}

	public void onPhotoSendSuccess(long flag) {
		List<Information> localInfos = getAdapterData();
		if (localInfos != null) {
			for (Information info : localInfos) {
				if (info.getType() == InformationType.TYPE_PICTURE_OR_VIDEO
				        && info.getStatu() == InformationState.PhotoInformationState.STATU_NOTICE_SENDING_OR_LOADING && info.getCreatetime() == flag) {
					info.setStatu(InformationState.PhotoInformationState.STATU_NOTICE_SENDED_OR_NEED_LOADD);
				}
			}
			adapter.notifyDataSetChanged();
		}
	}

	public void onPhotoResendSuccess(Information information) {
		List<Information> localInfos = getAdapterData();
		if (localInfos != null) {
			int index = localInfos.indexOf(information);
			if (index != -1) {
				localInfos.get(index).setStatu(InformationState.PhotoInformationState.STATU_NOTICE_SENDED_OR_NEED_LOADD);
				adapter.notifyDataSetChanged();
			}
		}
	}

	public void onPhotoSendFail(long flag) {
		List<Information> localInfos = getAdapterData();
		if (localInfos != null) {
			for (Information info : localInfos) {
				if (info.getType() == InformationType.TYPE_PICTURE_OR_VIDEO
				        && info.getStatu() == InformationState.PhotoInformationState.STATU_NOTICE_SENDING_OR_LOADING && info.getCreatetime() == flag) {
					info.setStatu(InformationState.PhotoInformationState.STATU_NOTICE_SEND_OR_LOAD_FAIL);
				}
			}
			adapter.notifyDataSetChanged();
		}
	}

	public void onPhotoResendFail(Information information) {
		List<Information> localInfos = getAdapterData();
		if (localInfos != null) {
			int index = localInfos.indexOf(information);
			if (index != -1) {
				localInfos.get(index).setStatu(InformationState.PhotoInformationState.STATU_NOTICE_SEND_OR_LOAD_FAIL);
				adapter.notifyDataSetChanged();
			}
		}
	}

	private void filteInformations(final List<Information> infos) {
		Thread th = new Thread() {

			public void run() {
				Map<Integer, List<Information>> result = PhotoTalkDatabaseFactory.getDatabase().filterNewInformations(infos, getCurrentUser());
				Message msg = myHandler.obtainMessage();
				msg.what = MSG_TIGASE_NEW_INFORMATION;
				msg.obj = result;
				myHandler.sendMessage(msg);
			};
		};
		th.start();
	}

	public void onNewInformation(List<Information> infosNeedUpdate, List<Information> infosNew) {
		List<Information> localInformation = getAdapterData();
		for (Information info : infosNeedUpdate) {
			if (localInformation != null && localInformation.contains(info)) {
				localInformation.get(localInformation.indexOf(info)).setStatu(info.getStatu());
			}
		}
		sendDataLoadedMessage(infosNew, MSG_WHAT_INFORMATION_LOADED);
	}

	public void onNewTrends() {
		int maxTrendId = PrefsUtils.User.getMaxTrendsId(this, getCurrentUser().getRcId());
		int showedMaxTrendId = PrefsUtils.User.getShowedMaxTrendsId(this, getCurrentUser().getRcId());
		if (showedMaxTrendId < maxTrendId) {
			iconTrendNew.setVisibility(View.VISIBLE);
			String url = PrefsUtils.User.getMaxTrendUrl(this, getCurrentUser().getRcId());
			SettingPageController.getInstance().onNewTrends(true, url);
		} else {
			iconTrendNew.setVisibility(View.GONE);
			SettingPageController.getInstance().onNewTrends(false, null);
		}
	}

	private OnScrollListener mScrollListener = new PauseOnScrollListener(mImageLoader, false, true) {

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			if (mInformationList.getAdapter().getCount() - view.getLastVisiblePosition() <= 2) {
				loadLocalInformation();
			}
		}
	};

	private void loadLocalInformation() {
		if (hasNextPage && !isLoading) {
			isLoading = true;
			loadLocalInformations();
		}
	}

	private void loadLocalInformations() {
		Thread thread = new Thread() {

			public void run() {
				int start = 0;
				if (mInformationList.getAdapter() != null)
					start = mInformationList.getAdapter().getCount();
				List<Information> informations = PhotoTalkDatabaseFactory.getDatabase().getInformationByPage(start, Constants.INFORMATION_PAGE_SIZE);
				Message msg = myHandler.obtainMessage();
				msg.what = MSG_WHAT_LOCAL_INFORMATION_LOADED;
				msg.obj = informations;
				myHandler.sendMessage(msg);
			};
		};
		thread.start();
	}

	private void registeTigaseStateChangeReceiver() {
		IntentFilter filter = new IntentFilter(Action.ACTION_TIGASE_STATE_CHANGE);
		registerReceiver(mTigaseStateChangeReceiver, filter);
	}

	private BroadcastReceiver mTigaseStateChangeReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String newState = intent.getStringExtra(INTENT_KEY_STATE);
			tvTigaseState.setText(newState + "");
		}
	};

	private void bindTigaseService() {
		Intent service = new Intent(this, TigaseMessageBinderService.class);
		bindService(service, tigaseServiceConnection, Context.BIND_AUTO_CREATE);
	}

	private void unBindTigaseService() {
		MessageSender.getInstance().stop();
		unbindService(tigaseServiceConnection);
	}

	@Override
	public boolean onMessageHandle(String msg, String from) {
		LogUtil.e("tigase receive informations...");
		List<Information> informations = JSONConver.jsonToInformations(msg);
		filteInformations(informations);
		return false;
	}

	private ServiceConnection tigaseServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			LocalBinder binder = (LocalBinder) service;
			binder.getService().setOnMessageReciver(HomeActivity.this);
			binder.getService().tigaseLogin(getCurrentUser().getTigaseId(), getCurrentUser().getTigasePwd());
			MessageSender.getInstance().setTigaseService(binder.getService());
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (!willQuit) {
				willQuit = true;
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.will_quit), Toast.LENGTH_SHORT).show();
				Timer quitTimer = new Timer();
				TimerTask task = new TimerTask() {

					@Override
					public void run() {
						// TODO Auto-generated method stub
						willQuit = false;
					}

				};
				quitTimer.schedule(task, 2000);
				return false;
			} else {
				finish();
				return true;
			}

		}
		return false;
	}
}