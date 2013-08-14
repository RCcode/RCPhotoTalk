package com.rcplatform.phototalk;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
import com.rcplatform.clientlog.ClientLogUtil;
import com.rcplatform.phototalk.activity.MenuBaseActivity;
import com.rcplatform.phototalk.adapter.PhotoTalkMessageAdapter;
import com.rcplatform.phototalk.bean.AppInfo;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.InformationClassification;
import com.rcplatform.phototalk.bean.InformationState;
import com.rcplatform.phototalk.bean.InformationType;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.drift.DriftInformationActivity;
import com.rcplatform.phototalk.galhttprequest.LogUtil;
import com.rcplatform.phototalk.image.downloader.RCPlatformImageLoader;
import com.rcplatform.phototalk.logic.LogicUtils;
import com.rcplatform.phototalk.logic.PhotoInformationCountDownService;
import com.rcplatform.phototalk.logic.controller.InformationPageController;
import com.rcplatform.phototalk.logic.controller.SettingPageController;
import com.rcplatform.phototalk.proxy.DriftProxy;
import com.rcplatform.phototalk.proxy.UserSettingProxy;
import com.rcplatform.phototalk.request.JSONConver;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.request.Request;
import com.rcplatform.phototalk.request.handler.MaxFishTimeResponseHandler;
import com.rcplatform.phototalk.request.inf.FriendDetailListener;
import com.rcplatform.phototalk.task.CheckUpdateTask;
import com.rcplatform.phototalk.umeng.EventUtil;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.utils.Constants.Action;
import com.rcplatform.phototalk.utils.Constants.ApplicationStartMode;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.PhotoTalkUtils;
import com.rcplatform.phototalk.utils.PrefsUtils;
import com.rcplatform.phototalk.utils.RCPlatformTextUtil;
import com.rcplatform.phototalk.utils.RCThreadPool;
import com.rcplatform.phototalk.utils.Utils;
import com.rcplatform.phototalk.views.LongClickShowView;
import com.rcplatform.phototalk.views.LongPressDialog;
import com.rcplatform.phototalk.views.LongPressDialog.OnLongPressItemClickListener;
import com.rcplatform.phototalk.views.RecordTimerLimitView;
import com.rcplatform.phototalk.views.SnapListView;
import com.rcplatform.phototalk.views.SnapShowListener;
import com.rcplatform.rcad.RcAd;
import com.rcplatform.rcad.constants.AdType;
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

	private boolean isLoading = false;

	private boolean willQuit = false;
	private RcAd popupAdlayout;
	private ImageView ivNewRecommends;
	private View knowStrangerView;
	// 引导
	private ImageView ivDriftAttention;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_view);
		InformationPageController.getInstance().setupController(this);
		mImageLoader = ImageLoader.getInstance();
		addShortCutIcon();
		checkStartMode();
		initViewAndListener();
		loadLocalInformation();
		getAllPlatformApps();
		onNewTrends();
		checkUpdate();
		checkTrends();
		checkAutoBindPhone();
		checkBindPhone();
		registeGCM();
		ClientLogUtil.log(this);
		DriftProxy.getMaxFishTime(this, new MaxFishTimeResponseHandler(this));
		addApplicationStartTime();
		showRcAd();
	}

	private void addApplicationStartTime() {
		int startTime = PrefsUtils.AppInfo.getApplicationStartTime(this);
		if (startTime <= Constants.COMMENT_ATTENTION_WAIT_MAX_TIME) {
			PrefsUtils.AppInfo.addApplicationStartTime(this);
		}
		if (startTime == Constants.COMMENT_ATTENTION_WAIT_MAX_TIME) {
			PhotoTalkUtils.showCommentAttentionDialog(this);
		}
	}

	private void registeGCM() {
		UserInfo userInfo = getCurrentUser();
		if (userInfo != null) {
			try {
				ServerUtilities.register(this, userInfo.getRcId(), userInfo.getToken());
			} catch (Exception e) {

			}
		}
	}

	private void checkAutoBindPhone() {
		if (RCPlatformTextUtil.isEmpty(getCurrentUser().getCellPhone()) && !PrefsUtils.User.hasAttentionAutoBind(this, getCurrentUserRcId())) {
			showAutoBindAttentionDialog();
		}
	}

	private void addShortCutIcon() {
		if (!PrefsUtils.AppInfo.hasAddShortCutIcon(this)) {
			PrefsUtils.AppInfo.setAddedShortCutIcon(this);
			Utils.createShortCutIcon(this, PhotoTalkUtils.getNotificationTakePhotoIntent(this), R.drawable.take_photo, R.string.icon_name);
		}
	}

	private void showAutoBindAttentionDialog() {
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (which == DialogInterface.BUTTON_POSITIVE) {
					PrefsUtils.User.setAutoBind(HomeActivity.this, getCurrentUserRcId());
					getPhotoTalkApplication().getService().startBindPhone();
				}
			}
		};
		AlertDialog dialog = DialogUtil.getAlertDialogBuilder(this).setMessage(R.string.auto_bind_dialog_msg).setNegativeButton(R.string.cancel, listener)
				.setPositiveButton(R.string.ok, listener).create();
		dialog.show();
		PrefsUtils.User.setAutoBindAttentioned(this, getCurrentUserRcId());
	}

	private void showRcAd() {
		popupAdlayout = new RcAd(this, AdType.FULLSCREEN, "1002602", true);
	}

	private void checkStartMode() {
		int startMode = getStartMode();
		if (startMode == ApplicationStartMode.APPLICATION_START_RECOMMENDS) {
			startActivity(MyFriendsActivity.class);
		} else if (startMode == ApplicationStartMode.APPLICATION_START_TAKE_PHOTO) {
			EventUtil.Main_Photo.rcpt_takephotobutton(baseContext);
			startActivity(new Intent(HomeActivity.this, TakePhotoActivity.class));
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
		if (Constants.DEVICE_ID != null) {
			if (PrefsUtils.User.MobilePhoneBind.isUserBindPhoneTimeOut(this, rcId) && RCPlatformTextUtil.isEmpty(getCurrentUser().getCellPhone())
					&& Constants.DEVICE_ID.equals(getCurrentUser().getDeviceId()) && !PrefsUtils.User.MobilePhoneBind.hasAttentionToBindPhone(this, rcId)) {
				PrefsUtils.User.MobilePhoneBind.setAttentionToBindPhone(this, rcId);
				DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						case DialogInterface.BUTTON_POSITIVE:
							EventUtil.Main_Photo.rcpt_phonepop_register(baseContext);
							startActivity(RequestSMSActivity.class);
							break;
						case DialogInterface.BUTTON_NEGATIVE:
							EventUtil.Main_Photo.rcpt_phonepop_later(baseContext);
							dialog.dismiss();
							break;
						}
					}
				};
				AlertDialog dialog = DialogUtil.getAlertDialogBuilder(this).setMessage(R.string.bind_phone_attention)
						.setNegativeButton(R.string.attention_later, listener).setPositiveButton(R.string.bind_now, listener).create();
				dialog.show();
			}
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
						} catch (JSONException e) {
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
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(int errorCode, String content) {
			}
		}, PrefsUtils.User.getShowedMaxTrendsId(this, getCurrentUser().getRcId()));
	}

	private void searchFriend(Friend friend, final int type) {
		showLoadingDialog(false);
		Request.executeGetFriendDetailAsync(this, friend, new FriendDetailListener() {

			@Override
			public void onSuccess(Friend friend) {
				dissmissLoadingDialog();
				startFriendDetailActivity(friend, type);
			}

			@Override
			public void onError(int errorCode, String content) {
				dissmissLoadingDialog();
				showConfirmDialog(content);
			}
		}, false);
	}

	private void startFriendDetailActivity(Friend friend, int type) {
		if (type == InformationClassification.TYPE_DRIFT) {
			Intent intent = new Intent(this, StrangerDetailActivity.class);
			intent.putExtra(StrangerDetailActivity.PARAM_FRIEND, friend);
			startActivity(intent);
			return;
		}
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
		ivDriftAttention = (ImageView) findViewById(R.id.iv_drift_new);
		knowStrangerView = findViewById(R.id.linear_new_friends);
		knowStrangerView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(DriftInformationActivity.class);
				resetDriftState();
				EventUtil.Main_Photo.rcpt_makenewfriends(baseContext);
			}
		});
		ivNewRecommends = (ImageView) findViewById(R.id.iv_recommends_new);
		iconTrendNew = (ImageView) findViewById(R.id.iv_trend_new);
		mInformationList = (SnapListView) findViewById(R.id.lv_home);
		mInformationList.setSnapListener(this);
		mTakePhoto = (Button) findViewById(R.id.btn_home_take_photo);

		ImageView mTvContentTitle = (ImageView) findViewById(R.id.title_image);
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
				if (Utils.isExternalStorageUsable()) {
					EventUtil.Main_Photo.rcpt_takephotobutton(baseContext);
					startActivity(new Intent(HomeActivity.this, TakePhotoActivity.class));
				} else {
					DialogUtil.showToast(HomeActivity.this, R.string.no_sdc, Toast.LENGTH_SHORT);
				}
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
		mInformationList.setOnItemClickListener(informationListItemClickListener);
		if (PrefsUtils.User.hasNewRecommends(this, getCurrentUserRcId()))
			InformationPageController.getInstance().onNewRecommends();
	}

	private OnItemClickListener informationListItemClickListener = new OnItemClickListener() {

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
	};


	private void showFriendDetail(Information information) {
		if (information.getSender().getRcId().equals(information.getReceiver().getRcId())) {
			Friend friend = PhotoTalkUtils.userToFriend(getCurrentUser());
			startFriendDetailActivity(friend, information.getPhotoType());
			return;
		}
		Friend friend = new Friend();
		if (LogicUtils.isSender(HomeActivity.this, information)) {
			friend.setRcId(information.getReceiver().getRcId());
		} else {
			friend.setRcId(information.getSender().getRcId());
		}
		searchFriend(friend, information.getPhotoType());
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
		information.setStatu(InformationState.PhotoInformationState.STATU_NOTICE_SENDING_OR_LOADING);
		PhotoTalkDatabaseFactory.getDatabase().updateInformationState(information);
		adapter.notifyDataSetChanged();
		LogicUtils.resendInformation(this, information);
	}

	private void deleteInformation(Information information) {
		adapter.getData().remove(information);
		adapter.notifyDataSetChanged();
		LogicUtils.deleteInformation(this, information);
	}

	protected void reLoadPictrue(Information record) {
		RCPlatformImageLoader.LoadPictureForList(this, record);
	}

	private void show(int position) {
		EventUtil.Main_Photo.rcpt_photoview(baseContext);

		final Information infoRecord = (Information) adapter.getItem(position);
		if (infoRecord.getType() == InformationType.TYPE_PICTURE_OR_VIDEO && !LogicUtils.isSender(this, infoRecord)) {
			// 表示还未查看
			if (infoRecord.getStatu() == InformationState.PhotoInformationState.STATU_NOTICE_DELIVERED_OR_LOADED
					|| infoRecord.getStatu() == InformationState.PhotoInformationState.STATU_NOTICE_SHOWING) {
				if (mShowDialog == null) {
					LongClickShowView.Builder builder = new LongClickShowView.Builder(HomeActivity.this, R.layout.receice_to_show_view);
					mShowDialog = builder.create();
				}
				final RecordTimerLimitView limitView = (RecordTimerLimitView) mInformationList.findViewWithTag(PhotoTalkUtils.getInformationTagBase(infoRecord)
						+ Button.class.getName());
				limitView.setVisibility(View.VISIBLE);
				// limitView.setBackgroundDrawable(null);
				limitView.setBackgroundResource(R.drawable.item_time_bg);
				mShowDialog.setOnShowListener(new OnShowListener() {

					@Override
					public void onShow(DialogInterface dialog) {
						LogicUtils.startShowPhotoInformation(infoRecord);
						limitView.scheuleTask(infoRecord);
					}
				});
				mShowDialog.ShowDialog(infoRecord);
				isShow = true;
				mInformationList.setOnItemClickListener(null);
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
				if (mShowDialog != null) {
					if (mShowDialog.isShowing()) {
						mShowDialog.dismiss();
					}
					mShowDialog = null;
				}
				isShow = false;
				mInformationList.setOnItemClickListener(informationListItemClickListener);
				return true;
			}
			break;
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
			adapter = new PhotoTalkMessageAdapter(this, localInformations, mImageLoader);
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
			adapter = new PhotoTalkMessageAdapter(this, data, mImageLoader);
			mInformationList.setAdapter(adapter);
		} else {
			adapter.addData(data);
			adapter.notifyDataSetChanged();
		}
	}

	@Override
	protected void onDestroy() {
		// unBindTigaseService();
		PhotoInformationCountDownService.getInstance().finishAllShowingMessage();
		// unregisterReceiver(mTigaseStateChangeReceiver);
		InformationPageController.getInstance().destroy();
		if (mCheckUpdateTask != null)
			mCheckUpdateTask.cancel();
		if (mShowDialog != null && mShowDialog.isShowing())
			mShowDialog.dismiss();
		if (mLongPressDialog != null && mLongPressDialog.isShowing())
			mLongPressDialog.dismiss();
		ImageLoader.getInstance().stop();

		// 广告销毁
		if (popupAdlayout != null) {
			popupAdlayout.destroyAd();
		}
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
		if (PrefsUtils.AppInfo.isMustUpdate(this)) {
			PhotoTalkUtils.showMustUpdateDialog(this, true);
			return;
		}
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

	@Override
	public boolean onMessageHandle(String msg, String from) {
		LogUtil.e("tigase receive informations...");
		List<Information> informations = JSONConver.jsonToInformations(msg);
		filteInformations(informations);
		return false;
	}

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

	public void onFriendAlreadyAdded(Information information) {
		for (Information info : getAdapterData()) {
			if (info.equals(information))
				info.setStatu(InformationState.FriendRequestInformationState.STATU_QEQUEST_ADD_CONFIRM);
		}
		adapter.notifyDataSetChanged();
	}

	public void onNewRecommends() {
		ivNewRecommends.setVisibility(View.VISIBLE);
	}

	public void onNewRecommendsShowed() {
		ivNewRecommends.setVisibility(View.GONE);
	}

	public ListView getInformationList() {
		return mInformationList;
	}

	public void onDriftThrowed() {
		ivDriftAttention.setVisibility(View.VISIBLE);
	}

	private void resetDriftState() {
		ivDriftAttention.setVisibility(View.GONE);
	}
}
