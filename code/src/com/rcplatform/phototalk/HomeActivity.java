package com.rcplatform.phototalk;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.adapter.PhotoTalkMessageAdapter;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.InformationState;
import com.rcplatform.phototalk.bean.InformationType;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.image.downloader.ImageOptionsFactory;
import com.rcplatform.phototalk.image.downloader.RCPlatformImageLoader;
import com.rcplatform.phototalk.logic.InformationPageController;
import com.rcplatform.phototalk.logic.LogicUtils;
import com.rcplatform.phototalk.proxy.FriendsProxy;
import com.rcplatform.phototalk.proxy.RecordInfoProxy;
import com.rcplatform.phototalk.request.JSONConver;
import com.rcplatform.phototalk.request.RCPlatformResponseHandler;
import com.rcplatform.phototalk.task.CheckUpdateTask;
import com.rcplatform.phototalk.utils.Contract;
import com.rcplatform.phototalk.utils.Contract.Action;
import com.rcplatform.phototalk.utils.PrefsUtils;
import com.rcplatform.phototalk.utils.RCPlatformTextUtil;
import com.rcplatform.phototalk.views.LongClickShowView;
import com.rcplatform.phototalk.views.LongPressDialog;
import com.rcplatform.phototalk.views.LongPressDialog.OnLongPressItemClickListener;
import com.rcplatform.phototalk.views.PullToRefreshView;
import com.rcplatform.phototalk.views.PullToRefreshView.OnHeaderRefreshListener;
import com.rcplatform.phototalk.views.RecordTimerLimitView;
import com.rcplatform.phototalk.views.SnapListView;
import com.rcplatform.phototalk.views.SnapShowListener;

/**
 * 主界面. <br>
 * 主界面记录了用户最近的一些记录，包括给哪些好友发送了图片，收到了哪些好友的发送的图片或者好友添加通知，通过主界面可以进入拍照界面，
 * 拍照完成后后可以进行编辑和发送给好友
 * 
 * @version 1.0.0
 */
public class HomeActivity extends BaseActivity implements SnapShowListener {

	private static final int MSG_WHAT_GET_SERVICE_RECORD_SUCCESS = 1;

	private static final int LOAD_MORE_FAIL = 4;

	private static final int LOAD_MORE_SUCCESS = 5;

	private static final int REFRESH_FAIL = 6;

	private PullToRefreshView mPtrView;

	private SnapListView mInformationList;

	private Button mTakePhoto;

	private Button mRecord;

	protected LongClickShowView mShowDialog;

	private boolean isShow;

	private TextView mTvContentTitle;

	private TextView mBtFriendList;

	private TextView mBtMore;
	private ImageView title_line;

	private LongPressDialog mLongPressDialog;

	private PhotoTalkMessageAdapter adapter;

	private CheckUpdateTask mCheckUpdateTask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_view);
		InformationPageController.getInstance().setupController(this);
		initViewAndListener();
		loadDataFromDataBase();
		loadRecords();
		checkUpdate();
	}

	private void loadRecords() {
		// mRefreshView.setRefreshing();
		RecordInfoProxy.getAllRecordInfos(this, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				handleServiceRecordInfo(content);
			}

			@Override
			public void onFailure(int errorCode, String content) {
				showErrorConfirmDialog(content);
				// mRefreshView.onHeaderRefreshComplete();
			}
		});
	}

	private void handleServiceRecordInfo(final String serviceContent) {
		Thread thread = new Thread() {

			@Override
			public void run() {
				try {
					JSONObject obj = new JSONObject(serviceContent);
					int noticeId = obj.getInt("noticeId");
					PrefsUtils.User.setUserMaxRecordInfoId(HomeActivity.this, getPhotoTalkApplication().getCurrentUser().getEmail(), noticeId);
					List<Information> listinfo = JSONConver.jsonToInformations(obj.getJSONArray("mainUserNotice").toString());
					filterList(listinfo);
				} catch (Exception e) {
					e.printStackTrace();
					sendDataLoadedMessage(null);
				}
			}
		};
		thread.start();
	}

	private void searchFriendDetailById(String atUserId, final Information record) {
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		FriendsProxy.getFriendDetail(this, new RCPlatformResponseHandler() {

			@Override
			public void onSuccess(int statusCode, String content) {
				try {
					JSONObject jObj = new JSONObject(content);
					Friend friend = JSONConver.jsonToFriend(jObj.getJSONObject("friendInfo").toString());
					startFriendDetailActivity(friend);
				} catch (JSONException e) {
					showErrorConfirmDialog(R.string.net_error);
				}
				dismissLoadingDialog();
			}

			@Override
			public void onFailure(int errorCode, String content) {
				dismissLoadingDialog();
				showErrorConfirmDialog(content);
			}
		}, atUserId);
	}

	private void startFriendDetailActivity(Friend friend) {
		Intent intent = new Intent(this, FriendDetailActivity.class);
		intent.putExtra(FriendDetailActivity.PARAM_FRIEND, friend);
		if (friend.getStatus() == Friend.USER_STATUS_NOT_FRIEND) {
			intent.setAction(Contract.Action.ACTION_RECOMMEND_DETAIL);
		} else {
			intent.setAction(Contract.Action.ACTION_FRIEND_DETAIL);
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
		// if
		// (SelectFriendsActivity.class.getName().equals(intent.getStringExtra("from")))
		// {
		// long time = intent.getLongExtra("time", 0);
		// if (app.getSendRecordsList(time) != null) {
		// initOrRefreshListView(app.getSendRecordsList(time));
		// }
		// }
	}

	private void logout() {
		startActivity(InitPageActivity.class);
		finish();
	}

	private void relogin() {
		Intent loginIntent = new Intent(this, LoginActivity.class);
		loginIntent.putExtra(Contract.KEY_LOGIN_PAGE, true);
		startActivity(loginIntent);
		finish();
	}

	// private void reSendNotifyToService(Information record) {
	// //
	// List<Information> sendList =
	// app.getSendRecordsList(record.getCreatetime());
	// if (sendList != null) {
	// sendList.remove(record);
	// if (sendList.size() == 0)
	// app.getSendRecords().remove(record.getCreatetime());
	// }
	//
	// long time = System.currentTimeMillis();
	// record.setCreatetime(time);
	// record.setStatu(InformationState.STATU_NOTICE_SENDING);
	// List<Information> newSendlist = new ArrayList<Information>();
	// newSendlist.add(record);
	// app.addSendRecords(time, newSendlist);
	// Map<String, String> params = new HashMap<String, String>();
	// params.put(MenueApiFactory.COUNTRY, Locale.getDefault().getCountry());
	// params.put(MenueApiFactory.HEAD_URL,
	// getPhotoTalkApplication().getCurrentUser().getHeadUrl());
	// params.put(MenueApiFactory.TIME, String.valueOf(record.getCreatetime()));
	// params.put(MenueApiFactory.NICK,
	// getPhotoTalkApplication().getCurrentUser().getNick());
	// params.put(MenueApiFactory.IMAGE_TYPE, "jpg");
	// params.put(MenueApiFactory.DESC, "");
	// params.put(MenueApiFactory.TIME_LIMIT,
	// String.valueOf(record.getLimitTime()));
	// params.put(MenueApiFactory.USER_ARRAY,
	// buildUserArray(record.getReceiver()));
	// params.put(MenueApiFactory.FILE, record.getUrl());
	//
	// PhotoTalkInformationStateService.getInstence().postRequestByTimestamp(this,
	// new PhotoChatHttpLoadTextCallBack() {
	//
	// @Override
	// public void textLoaded(String text, long time) {
	// callBackForSend(time, text);
	// }
	//
	// @Override
	// public void loadFail(long time) {
	// callBackForSend(time, "");
	// }
	// }, params, MenueApiUrl.SEND_PICTURE_URL, record.getCreatetime(), null);
	//
	// }

	private void loadDataFromDataBase() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				List<Information> data = PhotoTalkDatabaseFactory.getDatabase().getRecordInfos();
				sendDataLoadedMessage(data);
			}
		}).start();
	}

	private void sendDataLoadedMessage(List<Information> infos) {
		if (infos != null && infos.size() > 0) {
			Message msg = myHandler.obtainMessage();
			msg.what = MSG_WHAT_GET_SERVICE_RECORD_SUCCESS;
			msg.obj = infos;
			myHandler.sendMessage(msg);
		} else {
			myHandler.sendEmptyMessage(MSG_WHAT_GET_SERVICE_RECORD_SUCCESS);
		}
	}

	private void initViewAndListener() {
		mPtrView = (PullToRefreshView) findViewById(R.id.ptr_home);
		mPtrView.setOnHeaderRefreshListener(new OnHeaderRefreshListener() {

			@Override
			public void onHeaderRefresh(PullToRefreshView view) {
				loadRecords();
			}
		});
		mInformationList = (SnapListView) findViewById(R.id.lv_home);
		mInformationList.setSnapListener(this);
		mTakePhoto = (Button) findViewById(R.id.btn_home_take_photo);
		mRecord = (Button) findViewById(R.id.btn_home_record);

		mTvContentTitle = (TextView) findViewById(R.id.titleContent);
		mTvContentTitle.setVisibility(View.VISIBLE);
		mTvContentTitle.setBackgroundResource(R.drawable.app_title);
		title_line = (ImageView) findViewById(R.id.title_line);
		title_line.setVisibility(View.VISIBLE);
		mBtFriendList = (TextView) findViewById(R.id.choosebutton0);
		mBtFriendList.setVisibility(View.VISIBLE);
		mBtFriendList.setBackgroundResource(R.drawable.friendlist_btn);
		mBtFriendList.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(HomeActivity.this, MyFriendsActivity.class));
			}
		});

		mBtMore = (TextView) findViewById(R.id.choosebutton);
		mBtMore.setVisibility(View.VISIBLE);
		mBtMore.setBackgroundResource(R.drawable.more_btn);
		mBtMore.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(HomeActivity.this, SettingsActivity.class));
			}
		});
		mTakePhoto.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(HomeActivity.this, TakePhotoActivity.class));
			}
		});
		mRecord.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(HomeActivity.this, VideoRecordActivity.class));
			}
		});
		mInformationList.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				showLongClickDialog(arg2 - 1);
				return false;
			}
		});
		mInformationList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Information information = (Information) adapter.getItem(arg2 - 1);
				showFriendDetail(information);

			}
		});
		// mRefreshView.setOnHeaderRefreshListener(new OnHeaderRefreshListener()
		// {
		//
		// @Override
		// public void onHeaderRefresh(PullToRefreshView view) {
		// loadRecords();
		// }
		// });
	}

	private void showFriendDetail(Information information) {
		if (information.getSender().getSuid().equals(information.getReceiver().getSuid())) {
			startActivity(SettingsActivity.class);
			return;
		}
		String friendSuid = null;
		if (LogicUtils.isSender(HomeActivity.this, information)) {
			friendSuid = information.getReceiver().getSuid();
		} else {
			friendSuid = information.getSender().getSuid();
		}
		searchFriendDetailById(friendSuid, information);
	}

	protected void showLongClickDialog(int position) {
		if (adapter != null) {
			Information record = adapter.getData().get(position);
			if (record != null) {
				if (record.getStatu() == InformationState.STATU_NOTICE_LOADING || record.getStatu() == InformationState.STATU_NOTICE_SENDING || record.getStatu() == InformationState.STATU_NOTICE_SHOWING) {
					return;
				} else {
					if (mLongPressDialog == null) {
						mLongPressDialog = new LongPressDialog(this, new String[] { getString(R.string.resend), getString(R.string.reload), getString(R.string.delete) }, new OnLongPressItemClickListener() {

							@Override
							public void onClick(int listPostion, int itemIndex) {
								Information record = adapter.getData().get(listPostion);
								switch (itemIndex) {
								case 0:
									// reSendNotifyToService(record);
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

					if (record.getStatu() == InformationState.STATU_NOTICE_LOAD_FAIL) {
						mLongPressDialog.show(position, 0);
					} else if (record.getStatu() == InformationState.STATU_NOTICE_SEND_FAIL) {
						mLongPressDialog.show(position, 1);
					} else {
						mLongPressDialog.show(position, 0, 1);
					}
				}
			}
		}
	}

	private void deleteInformation(Information information) {
		adapter.getData().remove(information);
		adapter.notifyDataSetChanged();
		LogicUtils.deleteInformation(this, information);
	}

	protected void reLoadPictrue(Information record) {
		RCPlatformImageLoader.LoadPictureForList(this, null, null, mInformationList, ImageLoader.getInstance(), ImageOptionsFactory.getReceiveImageOption(), record);
	}

	private void show(int position) {

		Information infoRecord = (Information) adapter.getItem(position);
		if (infoRecord.getType() == InformationType.TYPE_PICTURE_OR_VIDEO && !LogicUtils.isSender(this, infoRecord)) {
			// 表示还未查看
			if (infoRecord.getStatu() == InformationState.STATU_NOTICE_DELIVERED_OR_LOADED) {
				RecordTimerLimitView limitView = (RecordTimerLimitView) mInformationList.findViewWithTag(infoRecord.getRecordId() + Button.class.getName());
				limitView.setBackgroundDrawable(null);
				(limitView).scheuleTask(infoRecord);
				LogicUtils.startShowPhotoInformation(infoRecord);
				if (mShowDialog == null) {
					LongClickShowView.Builder builder = new LongClickShowView.Builder(HomeActivity.this, R.layout.receice_to_show_view);
					mShowDialog = builder.create();
				}
				mShowDialog.ShowDialog(infoRecord);
				isShow = true;
				// 把数据里面的状态更改为3，已查看
			} else if (infoRecord.getStatu() == InformationState.STATU_NOTICE_SHOWING) {
				if (mShowDialog == null) {
					LongClickShowView.Builder builder = new LongClickShowView.Builder(HomeActivity.this, R.layout.receice_to_show_view);
					mShowDialog = builder.create();
				}
				mShowDialog.ShowDialog(infoRecord);
				isShow = true;
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

		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_WHAT_GET_SERVICE_RECORD_SUCCESS:
				if (msg.obj != null) {
					initOrRefreshListView((List<Information>) msg.obj);
				}
				mPtrView.onHeaderRefreshComplete();
				break;
			case LOAD_MORE_SUCCESS:
				mPtrView.onHeaderRefreshComplete();
				break;
			case LOAD_MORE_FAIL:
				mPtrView.onHeaderRefreshComplete();
				break;
			case REFRESH_FAIL:
				mPtrView.onHeaderRefreshComplete();
				break;
			}
		};
	};

	private void initOrRefreshListView(List<Information> data) {
		if (data == null || data.isEmpty()) {
			adapter.notifyDataSetChanged();
			return;
		}
		if (adapter == null) {
			sortList(data);
			adapter = new PhotoTalkMessageAdapter(this, data, mInformationList);
			mInformationList.setAdapter(adapter);
		} else {
			adapter.addData(data);
			adapter.notifyDataSetChanged();
		}
	}

	/**
	 * Method description 过滤从服务器取回的数据，删除不需要的，更新更新的，插入需要插入数据库的
	 * 
	 * @param listinfo
	 *            ： 服务器取回的数据
	 */
	protected void filterList(List<Information> listinfo) {
		List<Information> newNotices = LogicUtils.informationFilter(this, listinfo, getAdapterData());
		sendDataLoadedMessage(newNotices);
	}

	private void sortList(List<Information> list) {
		Collections.sort(list, comparator);
	}

	Comparator<Information> comparator = new Comparator<Information>() {

		@Override
		public int compare(Information lhs, Information rhs) {
			// LogUtil.e(lhs.getCreatetime() + "......is lhs create time");
			// LogUtil.e(rhs.getCreatetime() + "......is rhs create time");
			if (rhs.getReceiveTime() > lhs.getReceiveTime())
				return 1;
			else if (rhs.getReceiveTime() < lhs.getReceiveTime())
				return -1;
			return 0;
		}
	};

	@Override
	protected void onDestroy() {
		InformationPageController.getInstance().destroy();
		if (mCheckUpdateTask != null)
			mCheckUpdateTask.cancel();
		ImageLoader.getInstance().clearMemoryCache();
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(R.string.home_clear_all_record);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		LogicUtils.showInformationClearDialog(this);
		return super.onOptionsItemSelected(item);
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
		String buttonTag = information.getRecordId() + Button.class.getName();
		String statuTag = information.getRecordId() + TextView.class.getName();
		String newTag = information.getRecordId() + ImageView.class.getName();
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

	public void onFriendAdded(Friend friend) {
		List<Information> infos = getAdapterData();
		if (infos != null && infos.size() > 0) {
			for (Information information : infos) {
				if (information.getType() == InformationType.TYPE_FRIEND_REQUEST_NOTICE && information.getSender().getSuid().equals(friend.getSuid())) {
					information.setStatu(InformationState.STATU_QEQUEST_ADDED);
				}
			}
			adapter.notifyDataSetChanged();
		}

	}

	public void clearInformation() {
		if (adapter != null) {
			adapter.getData().clear();
			adapter.notifyDataSetChanged();
		}
	}

	public void onPhotoSending(List<Information> informations) {
		sendDataLoadedMessage(informations);
	}

	public void onPhotoSendSuccess(Map<String, Information> informations, long flag) {
		List<Information> localInfos = getAdapterData();
		if (localInfos != null) {
			for (Information info : localInfos) {
				if (info.getType() == InformationType.TYPE_PICTURE_OR_VIDEO && info.getStatu() == InformationState.STATU_NOTICE_SENDING && info.getCreatetime() == flag && info.getRecordId().startsWith(Contract.TEMP_INFORMATION_ID)) {
					Information serviceInformation = informations.get(info.getReceiver().getSuid());
					info.setCreatetime(serviceInformation.getCreatetime());
					info.setStatu(serviceInformation.getStatu());
					info.setRecordId(serviceInformation.getRecordId());
				}
			}
			adapter.notifyDataSetChanged();
		}
	}
}
