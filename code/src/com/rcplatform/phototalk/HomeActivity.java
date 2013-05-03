package com.rcplatform.phototalk;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.adapter.PhotoTalkMessageAdapter;
import com.rcplatform.phototalk.api.JSONConver;
import com.rcplatform.phototalk.api.MenueApiFactory;
import com.rcplatform.phototalk.api.MenueApiUrl;
import com.rcplatform.phototalk.api.RCPlatformResponseHandler;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.InformationState;
import com.rcplatform.phototalk.bean.InformationType;
import com.rcplatform.phototalk.bean.RecordUser;
import com.rcplatform.phototalk.bean.ServiceRecordInfo;
import com.rcplatform.phototalk.bean.ServiceSimpleNotice;
import com.rcplatform.phototalk.clienservice.PhotoCharRequestService;
import com.rcplatform.phototalk.db.DatabaseFactory;
import com.rcplatform.phototalk.db.PhotoTalkDao;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.galhttprequest.GalHttpRequest.GalHttpLoadTextCallBack;
import com.rcplatform.phototalk.galhttprequest.GalHttpRequest.PhotoChatHttpLoadTextCallBack;
import com.rcplatform.phototalk.image.downloader.ImageOptionsFactory;
import com.rcplatform.phototalk.image.downloader.RCPlatformImageLoader;
import com.rcplatform.phototalk.proxy.FriendsProxy;
import com.rcplatform.phototalk.proxy.RecordInfoProxy;
import com.rcplatform.phototalk.utils.Contract;
import com.rcplatform.phototalk.utils.PhotoTalkUtils;
import com.rcplatform.phototalk.utils.PrefsUtils;
import com.rcplatform.phototalk.utils.TimerLimitUtil;
import com.rcplatform.phototalk.views.LongClickShowView;
import com.rcplatform.phototalk.views.LongPressDialog;
import com.rcplatform.phototalk.views.LongPressDialog.OnLongPressItemClickListener;
import com.rcplatform.phototalk.views.PullToRefreshView;
import com.rcplatform.phototalk.views.PullToRefreshView.OnFooterRefreshListener;
import com.rcplatform.phototalk.views.PullToRefreshView.OnHeaderRefreshListener;
import com.rcplatform.phototalk.views.RecordTimerLimitView;
import com.rcplatform.phototalk.views.RecordTimerLimitView.OnTimeEndListener;

/**
 * 主界面. <br>
 * 主界面记录了用户最近的一些记录，包括给哪些好友发送了图片，收到了哪些好友的发送的图片或者好友添加通知，通过主界面可以进入拍照界面，
 * 拍照完成后后可以进行编辑和发送给好友
 * 
 * @version 1.0.0
 */
public class HomeActivity extends BaseActivity {
	private static final int MSG_WHAT_GET_SERVICE_RECORD_SUCCESS = 1;

	private static final int REQUEST_CODE_DETAIL = 100;

	private static final int LOAD_MORE_FAIL = 4;

	private static final int LOAD_MORE_SUCCESS = 5;

	private static final int REFRESH_FAIL = 6;

	private PullToRefreshView mRefreshView;

	private ListView mRecordListView;

	private Button mTakePhoto;

	private Button mRecord;

	protected LongClickShowView mShowDialog;

	private boolean isShow;

	private MenueApplication app;

	private TextView mTvContentTitle;

	private TextView mBtFriendList;

	private TextView mBtMore;

	private LongPressDialog mLongPressDialog;

	private PhotoTalkMessageAdapter adapter;

	private Information mShowDetailInformation;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_view);
		app = getPhotoTalkApplication();
		app.addActivity(this.getClass().getName(), this);
		DatabaseFactory.getInstance(this).createTables(this);
		checkFialRequest();
		initViewAndListener();
		loadDataFromDataBase();
		loadRecords();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == REQUEST_CODE_DETAIL) {
				mShowDetailInformation.setStatu(InformationState.STATU_QEQUEST_ADDED);
				adapter.notifyDataSetChanged();
			}
		}
	}

	private void loadRecords() {
		mRefreshView.setRefreshing();
		RecordInfoProxy.getAllRecordInfos(this, new RCPlatformResponseHandler() {
			@Override
			public void onSuccess(int statusCode, String content) {
				handleServiceRecordInfo(content);
			}

			@Override
			public void onFailure(int errorCode, String content) {
				showErrorConfirmDialog(content);
				mRefreshView.onHeaderRefreshComplete();
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
					@SuppressWarnings("unchecked")
					List<ServiceRecordInfo> recordInfo = (List<ServiceRecordInfo>) new Gson().fromJson(obj.getJSONArray("mainUserNotice").toString(), new TypeToken<List<ServiceRecordInfo>>() {
					}.getType());

					List<Information> listinfo = convertData(recordInfo);
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
					mShowDetailInformation = record;
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
		// TODO Auto-generated method stub
		Intent intent = new Intent(this, FriendDetailActivity.class);
		intent.putExtra(FriendDetailActivity.PARAM_FRIEND, friend);
		if (friend.getStatus() == Friend.USER_STATUS_NOT_FRIEND) {
			intent.setAction(Contract.Action.ACTION_RECOMMEND_DETAIL);
		} else {
			intent.setAction(Contract.Action.ACTION_FRIEND_DETAIL);
		}
		startActivityForResult(intent, REQUEST_CODE_DETAIL);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (SelectFriendsActivity.class.getName().equals(intent.getStringExtra("from"))) {
			long time = intent.getLongExtra("time", 0);
			if (app.getSendRecordsList(time) != null) {
				initOrRefreshListView(app.getSendRecordsList(time));
			}
		}
	}

	private void reSendNotifyToService(Information record) {
		//
		List<Information> sendList = app.getSendRecordsList(record.getCreatetime());
		if (sendList != null) {
			sendList.remove(record);
			if (sendList.size() == 0)
				app.getSendRecords().remove(record.getCreatetime());
		}

		long time = System.currentTimeMillis();
		record.setCreatetime(time);
		record.setStatu(InformationState.STATU_NOTICE_SENDING);
		PhotoTalkDao.getInstance().deleteRecordById(this, record.getRecordId());
		List<Information> newSendlist = new ArrayList<Information>();
		newSendlist.add(record);
		app.addSendRecords(time, newSendlist);
		Map<String, String> params = new HashMap<String, String>();
		params.put(MenueApiFactory.COUNTRY, Locale.getDefault().getCountry());
		params.put(MenueApiFactory.HEAD_URL, getPhotoTalkApplication().getCurrentUser().getHeadUrl());
		params.put(MenueApiFactory.TIME, String.valueOf(record.getCreatetime()));
		params.put(MenueApiFactory.NICK, getPhotoTalkApplication().getCurrentUser().getNick());
		params.put(MenueApiFactory.IMAGE_TYPE, "jpg");
		params.put(MenueApiFactory.DESC, "");
		params.put(MenueApiFactory.TIME_LIMIT, String.valueOf(record.getLimitTime()));
		params.put(MenueApiFactory.USER_ARRAY, buildUserArray(record.getReceiver()));
		params.put(MenueApiFactory.FILE, record.getUrl());

		PhotoCharRequestService.getInstence().postRequestByTimestamp(this, new PhotoChatHttpLoadTextCallBack() {

			@Override
			public void textLoaded(String text, long time) {
				callBackForSend(time, text);
			}

			@Override
			public void loadFail(long time) {
				callBackForSend(time, "");
			}
		}, params, MenueApiUrl.SEND_PICTURE_URL, record.getCreatetime(), null);

	}

	private String buildUserArray(RecordUser receiver) {
		try {
			JSONArray array = new JSONArray();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("userId", receiver.getSuid());
			jsonObject.put("headUrl", receiver.getHeadUrl());
			jsonObject.put("nick", receiver.getNick());
			array.put(jsonObject);
			return array.toString();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return "";
	}

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
		mRefreshView = (PullToRefreshView) findViewById(R.id.rf_home);
		mRecordListView = (ListView) findViewById(R.id.lv_home);
		mTakePhoto = (Button) findViewById(R.id.btn_home_take_photo);
		mRecord = (Button) findViewById(R.id.btn_home_record);

		mTvContentTitle = (TextView) findViewById(R.id.titleContent);
		mTvContentTitle.setVisibility(View.VISIBLE);
		mTvContentTitle.setBackgroundResource(R.drawable.app_title);

		mBtFriendList = (TextView) findViewById(R.id.choosebutton0);
		mBtFriendList.setVisibility(View.VISIBLE);
		mBtFriendList.setBackgroundResource(R.drawable.friendlist_icon);
		mBtFriendList.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(HomeActivity.this, MyFriendsActivity.class));
			}
		});

		mBtMore = (TextView) findViewById(R.id.choosebutton);
		mBtMore.setVisibility(View.VISIBLE);
		mBtMore.setBackgroundResource(R.drawable.more_icon);
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
		final GestureDetector gestureDetector = new GestureDetector(new HomeGestureListener());
		mRecordListView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		});
		mRecordListView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				showLongClickDialog(arg2);
				return false;
			}
		});
		mRefreshView.setOnHeaderRefreshListener(new OnHeaderRefreshListener() {

			@Override
			public void onHeaderRefresh(PullToRefreshView view) {
				loadRecords();
			}
		});

		mRefreshView.setOnFooterRefreshListener(new OnFooterRefreshListener() {

			@Override
			public void onFooterRefresh(PullToRefreshView view) {
				loadMoreFromDatabase(10);
			}
		});
	}

	class HomeGestureListener extends SimpleOnGestureListener {
		@Override
		public boolean onDown(MotionEvent e) {
			if (mRecordListView.getChildCount() > 0) {
				int position = getPostionFromTouch(e, mRecordListView);
				if (position != -1) {
					show(position);
				}
			}
			return super.onDown(e);
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			if (mRecordListView.getChildCount() > 0) {
				int position = getPostionFromTouch(e, mRecordListView);
				Information record = (Information) adapter.getItem(position);
				if (record != null && (record.getType() != InformationType.TYPE_SYSTEM_NOTICE && record.getStatu() != InformationState.STATU_NOTICE_SHOWING && record.getStatu() != InformationState.STATU_NOTICE_DELIVERED_OR_LOADED)) {
					String sUid = null;
					if (PhotoTalkUtils.isSender(HomeActivity.this, record)) {
						sUid = record.getReceiver().getSuid();
					} else {
						sUid = record.getSender().getSuid();
					}
					searchFriendDetailById(sUid, record);
				}
			}
			return super.onSingleTapUp(e);
		}
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
									reSendNotifyToService(record);
									mLongPressDialog.hide();
									break;
								// 重新下载
								case 1:
									reLoadPictrue(record);
									mLongPressDialog.hide();
									break;

								case 2:
									PhotoTalkDao.getInstance().deleteRecordById(HomeActivity.this, record.getRecordId());
									adapter.getData().remove(record);
									adapter.notifyDataSetChanged();
									notifyServiceDelete(record);
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

	protected void reLoadPictrue(Information record) {
		RCPlatformImageLoader.LoadPictureForList(this, null, null, mRecordListView, ImageLoader.getInstance(), ImageOptionsFactory.getReceiveImageOption(), record);
	}

	protected void loadMoreFromDatabase(final int i) {

		new AsyncTask<Integer, Integer, List<Information>>() {

			@Override
			protected List<Information> doInBackground(Integer... params) {
				List<Information> list = null;
				if (getAdapterData() != null && getAdapterData().size() > 0) {
					list = PhotoTalkDao.getInstance().loadTMoreInfoRecord(HomeActivity.this, i, String.valueOf(getAdapterData().get(getAdapterData().size() - 1).getCreatetime()));
				} else {
					loadDataFromDataBase();
				}
				return list;
			}

			@Override
			protected void onPostExecute(List<Information> result) {
				if (result != null && result.size() > 0) {
					initOrRefreshListView(result);
					myHandler.sendEmptyMessage(LOAD_MORE_SUCCESS);
				} else {
					myHandler.sendEmptyMessage(LOAD_MORE_FAIL);
				}
			}
		}.execute(i);
	}

	private void show(final int position) {
		final Information infoRecord = ((Information) mRecordListView.getAdapter().getItem(position));

		if (infoRecord.getType() == InformationType.TYPE_PICTURE_OR_VIDEO && !PhotoTalkUtils.isSender(this, infoRecord)) {
			// 表示还未查看
			if (infoRecord.getStatu() == InformationState.STATU_NOTICE_DELIVERED_OR_LOADED) {
				// 这句还给View里面联合起作用，这边加入任务每一秒钟去setLimitTime改变值，然后RecordTimerLimitView在每秒钟显示下新的值
				TimerLimitUtil.getInstence().addTask(infoRecord);
				RecordTimerLimitView limitView = (RecordTimerLimitView) mRecordListView.findViewWithTag(infoRecord.getRecordId() + Button.class.getName());
				limitView.setBackgroundDrawable(null);
				(limitView).scheuleTask(infoRecord);
				limitView.setOnTimeEndListener(new OnTimeEndListener() {

					@Override
					public void onEnd(Object statuTag, Object buttonTag) {

						RecordTimerLimitView timerLimitView = (RecordTimerLimitView) mRecordListView.findViewWithTag(buttonTag);
						if (timerLimitView != null) {
							timerLimitView.setBackgroundResource(R.drawable.receive_arrows_opened);
							timerLimitView.setText("");
						}
						TextView statu = ((TextView) mRecordListView.findViewWithTag(statuTag));
						if (statu != null) {
							statu.setText(R.string.statu_opened_1s_ago);
						}
						// 通知服务器改变状态为3，表示已经查看
						notifyService(infoRecord);

					}
				}, infoRecord.getRecordId() + TextView.class.getName(), infoRecord.getRecordId() + Button.class.getName());

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
			} else if (infoRecord.isDestroyed()) {
				// 个人资料界面
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
		int index_of_childview = 0;

		/* 第一个可见项是listview header */
		if (0 == first) {
			if (eY > firstR.bottom) {
				/* 触摸不在listview header上，根据触摸的Y坐标和listitem的高度计算索引 */
				count = (int) ((eY - firstR.bottom) / listview.getChildAt(0).getHeight());
				count++;
				index_of_childview = count;
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
				index_of_childview = count;
				index_in_adapter += count;
			} else {
				index_of_childview = 0;
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
				mRefreshView.onHeaderRefreshComplete();
				break;
			case LOAD_MORE_SUCCESS:
				mRefreshView.onFooterRefreshComplete();
				break;
			case LOAD_MORE_FAIL:
				mRefreshView.onFooterRefreshComplete();
				break;
			case REFRESH_FAIL:
				// Toast.makeText(HomeActivity.this, "加载失败..", 1).show();
				mRefreshView.onHeaderRefreshComplete();
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
			adapter = new PhotoTalkMessageAdapter(this, data, mRecordListView);
			sortList(getAdapterData());
			mRecordListView.setAdapter(adapter);

		} else {
			getAdapterData().addAll(data);
			sortList(getAdapterData());
			adapter.notifyDataSetChanged();
		}
	}

	private void notifyService(Information record) {
		if (record.getType() == InformationType.TYPE_PICTURE_OR_VIDEO) {
			record.setStatu(InformationState.STATU_NOTICE_OPENED);
			notifyServiceOpenedPic(record);
			PhotoTalkDao.getInstance().updateRecordStatu(this, record);
		}

	}

	private void notifyServiceOpenedPic(Information record) {
		Gson gson = new Gson();
		ServiceSimpleNotice notice = new ServiceSimpleNotice(record.getStatu() + "", record.getRecordId() + "", record.getType() + "");
		List<ServiceSimpleNotice> list = new ArrayList<ServiceSimpleNotice>();
		list.add(notice);
		String s = gson.toJson(list, new TypeToken<List<ServiceSimpleNotice>>() {
		}.getType());
		Map<String, String> params = new HashMap<String, String>();
		params.put(MenueApiFactory.NOTICES, s);
		PhotoCharRequestService.getInstence().postRequest(this, notifyRecordOpenedCallBack, params, MenueApiUrl.HOME_USER_NOTICE_CHANGE);
	}

	private void notifyServiceDelete(Information record) {
		Gson gson = new Gson();
		ServiceSimpleNotice notice = new ServiceSimpleNotice(record.getStatu() + "", record.getRecordId() + "", record.getType() + "");
		List<ServiceSimpleNotice> list = new ArrayList<ServiceSimpleNotice>();
		list.add(notice);
		String s = gson.toJson(list, new TypeToken<List<ServiceSimpleNotice>>() {
		}.getType());
		Map<String, String> params = new HashMap<String, String>();
		params.put(MenueApiFactory.NOTICES, s);
		PhotoCharRequestService.getInstence().postRequest(this, notifyRecordOpenedCallBack, params, MenueApiUrl.HOME_USER_NOTICE_DELETE);
	}

	/**
	 * 把服务器返回的数据list 转换为本地需要的list
	 * 
	 * @param list
	 *            服务器的list
	 * @return 本地需要的list
	 */
	private List<Information> convertData(List<ServiceRecordInfo> list) {
		List<Information> data = new ArrayList<Information>();
		Information infoRecord;
		for (ServiceRecordInfo info : list) {
			infoRecord = new Information();
			infoRecord.setRecordId(info.getId().trim());
			infoRecord.setNoticeId(info.getNoticeId());
			infoRecord.setCreatetime(info.getCrtTime());
			infoRecord.setTotleLength(info.getTime());
			infoRecord.setLimitTime(info.getTime());
			infoRecord.setSender(new RecordUser(info.getSeSuid(), info.getSnick(), info.getShead()));
			infoRecord.setReceiver(new RecordUser(info.getReSuid(), info.getRnick(), info.getRhead()));
			infoRecord.setStatu(info.getState());
			infoRecord.setType(info.getType());
			infoRecord.setUrl(info.getPicUrl());
			infoRecord.setLastUpdateTime(info.getUpdTime());
			data.add(infoRecord);
		}
		list.clear();
		list = null;
		return data;
	}

	public void callBackForSend(long time, String text) {
		if (text == null || text.length() <= 0) {
			if (app.getSendRecordsList(time) != null) {
				for (Information info : app.getSendRecordsList(time)) {
					info.setStatu(InformationState.STATU_NOTICE_SEND_FAIL);
				}
				// 把当前这次发送错误的信息写入数据库保存，并删除app中的引用
				PhotoTalkDao.getInstance().insertInfoRecord(this, app.getSendRecordsList(time));
			}
			initOrRefreshListView(null);
			return;
		}
		try {
			JSONObject jsonObject = null;
			jsonObject = new JSONObject(text);
			int state = jsonObject.getInt(MenueApiFactory.RESPONSE_KEY_STATUS);
			if (state == MenueApiFactory.RESPONSE_STATE_SUCCESS) {
				List<ServiceRecordInfo> recordInfo = (List<ServiceRecordInfo>) new Gson().fromJson(jsonObject.getJSONArray("noticeList").toString(), new TypeToken<List<ServiceRecordInfo>>() {
				}.getType());
				if (recordInfo != null && recordInfo.size() > 0) {
					if (app.getSendRecordsList(time) != null && app.getSendRecordsList(time).size() > 0) {
						// 删除 本地缓存的图片
						String fileName = app.getSendRecordsList(time).get(0).getUrl();
						app.deleteSendFileCache(fileName);
						// 删除app 中的记录
						app.getSendRecords().remove(time);
						// 删除 list列表里面的
						clearListDataByTime(getAdapterData(), time);

						List<Information> listinfo = convertData(recordInfo);

						filterList(listinfo);
					}
				}
			} else {
				if (app.getSendRecordsList(time) != null) {
					for (Information info : app.getSendRecordsList(time)) {
						if (info.getCreatetime() == time) {
							info.setStatu(InformationState.STATU_NOTICE_SEND_FAIL);
						}
					}

					PhotoTalkDao.getInstance().insertInfoRecord(this, app.getSendRecordsList(time));
					// app.getSendRecords().remove(time);
				}
				initOrRefreshListView(null);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void clearListDataByTime(List<Information> data2, long time) {
		Iterator<Information> iterator = data2.iterator();
		while (iterator.hasNext()) {
			Information infoRecord = iterator.next();
			if (infoRecord.getCreatetime() == time && (infoRecord.getNoticeId().equals(MenueApiFactory.ERROR_NOTICE))) {
				iterator.remove();
			}

		}
	}

	/**
	 * Method description 过滤从服务器取回的数据，删除不需要的，更新更新的，插入需要插入数据库的
	 * 
	 * @param listinfo
	 *            ： 服务器取回的数据
	 */
	protected void filterList(List<Information> listinfo) {
		List<Information> newNotices = new ArrayList<Information>();
		Iterator<Information> iterator = listinfo.iterator();
		List<Information> data = getAdapterData();
		while (iterator.hasNext()) {
			Information record = iterator.next();
			// 如果是通知
			if (data != null && data.contains(record)) {
				if (record.getStatu() == data.get(data.indexOf(record)).getStatu()) {
					// 状态没有改变
					iterator.remove();
				} else {
					// 状态改变
					if (record.getType() == InformationType.TYPE_FRIEND_REQUEST_NOTICE) {
						data.get(data.indexOf(record)).setStatu(record.getStatu());
					} else if (record.getType() == InformationType.TYPE_PICTURE_OR_VIDEO) {

						// 判断 当前本地notice的状态是否等于其他几种特殊状态（0：正在发送，4，正在查看，5
						// 正在下载）,由于0是本地临时的，在服务器是没有这种状态的，所以比考虑

						if (record.getStatu() == InformationState.STATU_NOTICE_SENDED_OR_NEED_LOADD) {
							// 如果状态为1，表示需要去下载，但是我们得去判断是否正在下载，
							// 对于发送者来说什么都不做，只有接受者会有操作
							if (data.get(data.indexOf(record)).getStatu() == InformationState.STATU_NOTICE_LOADING || data.get(data.indexOf(record)).getStatu() == InformationState.STATU_NOTICE_SHOWING) {
								// 状态为5或者4
								// 表示是正在下载或者正在查看，说明已经下载下来了，那么就不需要去下载了
							} else if (data.get(data.indexOf(record)).getStatu() == InformationState.STATU_NOTICE_DELIVERED_OR_LOADED || data.get(data.indexOf(record)).getStatu() == InformationState.STATU_NOTICE_OPENED) {
								// 如果本地的notice
								// 是2或者3了，表示已经下载下来了，但是服务器还是一，说明通知服务器改状态没有成功，那么的通知服务器改状态，本地什么都不做
								notifyService(data.get(data.indexOf(record)));
							} else if (record.getStatu() == InformationState.STATU_NOTICE_LOAD_FAIL) {
								data.get(data.indexOf(record)).setStatu(record.getStatu());
							}
						} else if (record.getStatu() == InformationState.STATU_NOTICE_DELIVERED_OR_LOADED) {
							// 对发送者来说，只需要改变状态，并通知界面刷新就OK
							if (String.valueOf(getPhotoTalkApplication().getCurrentUser().getSuid()).equals(record.getSender().getSuid())) {
								data.get(data.indexOf(record)).setStatu(record.getStatu());
							} else {
								// 对于接收者来说，图片一已经下载完成了，那就要判断本地的状态时候为3，如果为3，那说明本地查看后
								// 只改了本地的状态，通知服务器改状态失败了，得再次通知，其他的就不用做任何操作
								if (data.get(data.indexOf(record)).getStatu() == InformationState.STATU_NOTICE_OPENED) {
									notifyService(data.get(data.indexOf(record)));
								}
							}

						} else if (record.getStatu() == InformationState.STATU_NOTICE_OPENED) {
							if (String.valueOf(getPhotoTalkApplication().getCurrentUser().getSuid()).equals(record.getSender().getSuid())) {
								data.get(data.indexOf(record)).setStatu(record.getStatu());
							}
						}

						// 为什么没有状态为3的情况，因为状态为3，不管是发送者还是接收者，这条记录生命线已经完了，不需要做任何操作了
					} else if (record.getType() == InformationType.TYPE_SYSTEM_NOTICE) {
						// 什么都不做，没有任何操作
					}
				}
			} else {
				newNotices.add(record);
			}
		}
		if (newNotices != null && newNotices.size() > 0)
			PhotoTalkDatabaseFactory.getDatabase().saveRecordInfos(newNotices);
		sendDataLoadedMessage(newNotices);
	}

	private void sortList(List<Information> list) {
		Collections.sort(list, comparator);
	}

	Comparator<Information> comparator = new Comparator<Information>() {

		@Override
		public int compare(Information lhs, Information rhs) {

			return (int) (rhs.getCreatetime() - lhs.getCreatetime());
		}
	};

	private final GalHttpLoadTextCallBack notifyRecordOpenedCallBack = new GalHttpLoadTextCallBack() {

		@Override
		public void textLoaded(String text) {

		}

		@Override
		public void loadFail() {

		}
	};

	@Override
	protected void onDestroy() {
		app.removeActivity(this.getClass().getName());
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(R.string.home_clear_all_record);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		final Map<String, String> params = new HashMap<String, String>();
		params.put(MenueApiFactory.NOTICE_ID, PrefsUtils.User.getUserMaxRecordInfoId(this, getPhotoTalkApplication().getCurrentUser().getEmail()) + "");
		notifyServiceDeleteAll(params);
		PhotoTalkDao.getInstance().deleteCurrentUserTable(this);
		((PhotoTalkMessageAdapter) mRecordListView.getAdapter()).getData().clear();
		((PhotoTalkMessageAdapter) mRecordListView.getAdapter()).notifyDataSetChanged();
		return super.onOptionsItemSelected(item);
	}

	private void notifyServiceDeleteAll(final Map<String, String> params) {
		PhotoCharRequestService.getInstence().postRequest(this, new GalHttpLoadTextCallBack() {

			@Override
			public void textLoaded(String text) {
				JSONObject obj;
				try {
					obj = new JSONObject(text);
					int state = obj.getInt(MenueApiFactory.RESPONSE_KEY_STATUS);
					if (state != MenueApiFactory.RESPONSE_STATE_SUCCESS) {
						String jsonParams = new Gson().toJson(params, new TypeToken<Map<String, String>>() {
						}.getType());
						PhotoTalkDao.getInstance().insertFailRequestInfo(HomeActivity.this, MenueApiUrl.HOME_USER_NOTICE_DELETE_ALL, jsonParams);
					} else {
						String request_id = params.get("id");
						if (request_id != null && request_id.length() > 0) {
							PhotoTalkDao.getInstance().deleteFailRequestInfoById(HomeActivity.this, request_id);
						}
					}
				} catch (Exception ex) {

				}
			}

			@Override
			public void loadFail() {
				String jsonParams = new Gson().toJson(params, new TypeToken<Map<String, String>>() {
				}.getType());
				PhotoTalkDao.getInstance().insertFailRequestInfo(HomeActivity.this, MenueApiUrl.HOME_USER_NOTICE_DELETE_ALL, jsonParams);
			}
		}, params, MenueApiUrl.HOME_USER_NOTICE_DELETE_ALL);
	}

	private void checkFialRequest() {
		List<Map<String, String>> requests = PhotoTalkDao.getInstance().findAllFailRequestInfo(this);
		if (requests != null && requests.size() > 0) {
			Map<String, String> params;
			for (Map<String, String> info : requests) {
				params = new Gson().fromJson(info.get("params"), new TypeToken<Map<String, String>>() {
				}.getType());
				params.put("id", info.get("id"));
				notifyServiceDeleteAll(params);
			}
		}
	}

	private List<Information> getAdapterData() {
		if (adapter == null)
			return null;
		return adapter.getData();
	}
}