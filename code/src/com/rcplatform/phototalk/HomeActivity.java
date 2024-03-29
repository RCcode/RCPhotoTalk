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

import android.content.Intent;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.adapter.HomeUserRecordAdapter;
import com.rcplatform.phototalk.api.MenueApiFactory;
import com.rcplatform.phototalk.api.MenueApiRecordType;
import com.rcplatform.phototalk.api.MenueApiUrl;
import com.rcplatform.phototalk.bean.DetailFriend;
import com.rcplatform.phototalk.bean.InfoRecord;
import com.rcplatform.phototalk.bean.RecordUser;
import com.rcplatform.phototalk.bean.ServiceRecordInfo;
import com.rcplatform.phototalk.bean.ServiceSimpleNotice;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.clienservice.PhotoCharRequestService;
import com.rcplatform.phototalk.db.DatabaseFactory;
import com.rcplatform.phototalk.db.PhotoTalkDao;
import com.rcplatform.phototalk.galhttprequest.GalHttpRequest;
import com.rcplatform.phototalk.galhttprequest.GalHttpRequest.GalHttpLoadTextCallBack;
import com.rcplatform.phototalk.galhttprequest.GalHttpRequest.PhotoChatHttpLoadTextCallBack;
import com.rcplatform.phototalk.galhttprequest.RCLoadTextCallBack;
import com.rcplatform.phototalk.image.downloader.ImageOptionsFactory;
import com.rcplatform.phototalk.image.downloader.RCPlatformImageLoader;
import com.rcplatform.phototalk.utils.Contract;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.PhotoTalkUtils;
import com.rcplatform.phototalk.utils.PrefsUtils;
import com.rcplatform.phototalk.utils.TimerLimitUtil;
import com.rcplatform.phototalk.views.LongClickShowView;
import com.rcplatform.phototalk.views.LongPressDialog;
import com.rcplatform.phototalk.views.LongPressDialog.OnLongPressItemClickListener;
import com.rcplatform.phototalk.views.PVPopupWindow;
import com.rcplatform.phototalk.views.PullToRefreshView;
import com.rcplatform.phototalk.views.PullToRefreshView.OnFooterRefreshListener;
import com.rcplatform.phototalk.views.PullToRefreshView.OnHeaderRefreshListener;
import com.rcplatform.phototalk.views.RecordTimerLimitView;
import com.rcplatform.phototalk.views.RecordTimerLimitView.OnTimeEndListener;

/**
 * 主界面. <br>
 * 主界面记录了用户最近的一些记录，包括给哪些好友发送了图片，收到了哪些好友的发送的图片或者好友添加通知，通过主界面可以进入拍照界面，
 * 拍照完成后后可以进行编辑和发送给好友
 * <p>
 * Copyright: Menue,Inc Copyright (c) 2013-3-13 下午2:09:14
 * <p>
 * Team:Menue Beijing
 * <p>
 * 
 * @author tao.fu@menue.com.cn
 * @version 1.0.0
 */
public class HomeActivity extends BaseActivity {

	private static final int OPENDE = 0;

	private static final int ENSURE_ADD = 1;

	private static final int LOAD_SUCCESS = 2;

	private static final int LOAD_FAIL = 3;

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

	private HomeUserRecordAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home_view);
		// insertData();

		app = (MenueApplication) getApplication();
		app.addActivity(this.getClass().getName(), this);
		// 如果当前账号是第一次登陆就会去为当前用户创建相关表
		DatabaseFactory.getInstance(this).createTables(this);
		// 检查当前用户是是否有请求失败的记录，有的话就会向服务器提交
		checkFialRequest();
		initViewAndListener();
		// 进来后首先从数据库里面查找记录
		loadDataFromDataBase();
		// 查出记录后，开始去服务器去最新消息和状态
		mRefreshView.setRefreshing();
		loadDataFromService();

	}

	private void searchFriendDetailById(String atUserId, final InfoRecord record) {
		/*
		 * {"token":"000000","userId":"DcZTw+RZVA8=","keyword":"xxxx","language":
		 * "zh_CN","deviceId":"123456"}
		 */
		showLoadingDialog(LOADING_NO_MSG, LOADING_NO_MSG, false);
		GalHttpRequest request = GalHttpRequest.requestWithURL(this,
				MenueApiUrl.FRIEND_DETAIL_URL);
		UserInfo userInfo = PrefsUtils.LoginState.getLoginUser(this);
		request.setPostValueForKey(MenueApiFactory.USERID, userInfo.getSuid());
		request.setPostValueForKey(MenueApiFactory.TOKEN, userInfo.getToken());
		request.setPostValueForKey(MenueApiFactory.ATUSERID, atUserId);
		request.setPostValueForKey(MenueApiFactory.LANGUAGE, Locale
				.getDefault().getLanguage());
		request.setPostValueForKey(MenueApiFactory.DEVICE_ID,
				android.os.Build.DEVICE);
		request.setPostValueForKey(MenueApiFactory.APP_ID, Contract.APP_ID);
		request.startAsynRequestString(new RCLoadTextCallBack(this) {

			@Override
			public void onSuccess(int statusCode, String text) {
				// TODO Auto-generated method stub
				try {
					JSONObject obj = new JSONObject(text);
					DetailFriend detailFriend = new Gson().fromJson(obj
							.getJSONObject("userDetail").toString(),
							new TypeToken<DetailFriend>() {
							}.getType());
					PopupWindow window = PVPopupWindow.show(HomeActivity.this,
							mRecordListView, detailFriend, record);
					window.setOnDismissListener(new OnDismissListener() {

						@Override
						public void onDismiss() {
							// TODO Auto-generated method stub
							adapter.notifyDataSetChanged();
						}
					});
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					DialogUtil.createMsgDialog(HomeActivity.this,
							getString(R.string.net_error),
							getString(android.R.string.ok)).show();
				}
			}

			@Override
			public void onError(int errorCode, String error) {
				// TODO Auto-generated method stub
				DialogUtil.createMsgDialog(HomeActivity.this, error,
						getString(android.R.string.ok)).show();
			}
		});
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (SelectFriendsActivity.class.getName().equals(
				intent.getStringExtra("from"))) {
			long time = intent.getLongExtra("time", 0);
			if (app.getSendRecordsList(time) != null) {
				initOrRefreshListView(app.getSendRecordsList(time));
			}
		}
	}

	private void loadDataFromService() {
		Map<String, String> params = new HashMap<String, String>();
		params.put(MenueApiFactory.PAGE, "1");
		params.put(MenueApiFactory.SIZE, "20");
		params.put(MenueApiFactory.SORT, "1");
		params.put(MenueApiFactory.RECEIVESET, "0");
		params.put(MenueApiFactory.NOTICE_ID, PrefsUtils.getNoticeId(this));
		PhotoCharRequestService service = PhotoCharRequestService.getInstence();
		service.postRequest(this, loadFromServiceCallBack, params,
				MenueApiUrl.HOME_USER_NOTICE);
	}

	private void reSendNotifyToService(InfoRecord record) {
		//
		List<InfoRecord> sendList = app.getSendRecordsList(record
				.getCreatetime());
		if (sendList != null) {
			sendList.remove(record);
			if (sendList.size() == 0)
				app.getSendRecords().remove(record.getCreatetime());
		}

		long time = System.currentTimeMillis();
		record.setCreatetime(time);
		record.setStatu(MenueApiRecordType.STATU_NOTICE_SENDING);
		PhotoTalkDao.getInstance().deleteRecordById(this, record.getRecordId());
		List<InfoRecord> newSendlist = new ArrayList<InfoRecord>();
		newSendlist.add(record);
		app.addSendRecords(time, newSendlist);
		Map<String, String> params = new HashMap<String, String>();
		params.put(MenueApiFactory.COUNTRY, Locale.getDefault().getCountry());
		params.put(MenueApiFactory.HEAD_URL, MenueApplication
				.getUserInfoInstall(this).getHeadUrl());
		params.put(MenueApiFactory.TIME, String.valueOf(record.getCreatetime()));
		params.put(MenueApiFactory.NICK,
				MenueApplication.getUserInfoInstall(this).getNick());
		params.put(MenueApiFactory.IMAGE_TYPE, "jpg");
		params.put(MenueApiFactory.DESC, "");
		params.put(MenueApiFactory.TIME_LIMIT,
				String.valueOf(record.getLimitTime()));
		params.put(MenueApiFactory.USER_ARRAY,
				buildUserArray(record.getReceiver()));
		params.put(MenueApiFactory.FILE, record.getUrl());

		PhotoCharRequestService.getInstence().postRequestByTimestamp(this,
				new PhotoChatHttpLoadTextCallBack() {

					@Override
					public void textLoaded(String text, long time) {
						callBackForSend(time, text);
					}

					@Override
					public void loadFail(long time) {
						callBackForSend(time, "");
					}
				}, params, MenueApiUrl.SEND_PICTURE_URL,
				record.getCreatetime(), null);

	}

	private String buildUserArray(RecordUser receiver) {
		try {
			JSONArray array = new JSONArray();
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("userId", receiver.getSuUserId());
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
				List<InfoRecord> data = PhotoTalkDao.getInstance()
						.loadTopCountInfoRecord(HomeActivity.this, 20);
				if (data != null && data.size() > 0) {
					initOrRefreshListView(data);
				}

			}
		}).start();
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
				startActivity(new Intent(HomeActivity.this,
						MyFriendsActivity.class));
			}
		});

		mBtMore = (TextView) findViewById(R.id.choosebutton);
		mBtMore.setVisibility(View.VISIBLE);
		mBtMore.setBackgroundResource(R.drawable.more_icon);
		mBtMore.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(HomeActivity.this,
						SettingsActivity.class));
			}
		});
		mTakePhoto.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(HomeActivity.this,
						TakePhotoActivity.class));
			}
		});
		mRecord.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(new Intent(HomeActivity.this,
						VideoRecordActivity.class));
			}
		});
		mRecordListView
				.setOnItemLongClickListener(new OnItemLongClickListener() {

					@Override
					public boolean onItemLongClick(AdapterView<?> parent,
							View view, int position, long id) {
						// show(position);
						showLongClickDialog(position);
						return false;
					}
				});
		final GestureDetector gestureDetector = new GestureDetector(
				new HomeGestureListener());
		mRecordListView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return gestureDetector.onTouchEvent(event);
			}
		});
		mRefreshView.setOnHeaderRefreshListener(new OnHeaderRefreshListener() {

			@Override
			public void onHeaderRefresh(PullToRefreshView view) {
				loadDataFromService();
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
				if (position != -1)
					show(position);
			}
			return super.onDown(e);
		}

		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			if (mRecordListView.getChildCount() > 0) {
				int position = getPostionFromTouch(e, mRecordListView);
				InfoRecord record = (InfoRecord) adapter.getItem(position);
				if (record != null
						&& (record.getStatu() != MenueApiRecordType.STATU_NOTICE_SHOWING && record
								.getStatu() != MenueApiRecordType.STATU_NOTICE_DELIVERED_OR_LOADED)) {
					String sUid = null;
					if (PhotoTalkUtils.isOwnerForReocrd(HomeActivity.this,
							record)) {
						sUid = record.getReceiver().getSuUserId();
					} else {
						sUid = record.getSender().getSuUserId();
					}
					searchFriendDetailById(sUid, record);
				}
			}
			return super.onSingleTapUp(e);
		}
	}

	protected void showLongClickDialog(int position) {
		if (adapter != null) {
			InfoRecord record = adapter.getData().get(position);
			if (record != null) {
				if (record.getStatu() == MenueApiRecordType.STATU_NOTICE_LOADING
						|| record.getStatu() == MenueApiRecordType.STATU_NOTICE_SENDING
						|| record.getStatu() == MenueApiRecordType.STATU_NOTICE_SHOWING) {
					return;
				} else {
					if (mLongPressDialog == null) {
						mLongPressDialog = new LongPressDialog(this,
								new String[] { getString(R.string.resend),
										getString(R.string.reload),
										getString(R.string.delete) },
								new OnLongPressItemClickListener() {

									@Override
									public void onClick(int listPostion,
											int itemIndex) {
										InfoRecord record = adapter.getData()
												.get(listPostion);
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
											PhotoTalkDao.getInstance()
													.deleteRecordById(
															HomeActivity.this,
															record.getRecordId());
											adapter.getData().remove(record);
											adapter.notifyDataSetChanged();
											notifyServiceDelete(record);
											mLongPressDialog.hide();
											break;
										}

									}
								});
					}

					if (record.getStatu() == MenueApiRecordType.STATU_NOTICE_LOAD_FAIL) {
						mLongPressDialog.show(position, 0);
					} else if (record.getStatu() == MenueApiRecordType.STATU_NOTICE_SEND_FAIL) {
						mLongPressDialog.show(position, 1);
					} else {
						mLongPressDialog.show(position, 0, 1);
					}
				}
				// if (record.getStatu() ==
				// MenueApiRecordType.STATU_NOTICE_OPENED || record.getStatu()
				// == MenueApiRecordType.STATU_NOTICE_LOAD_FAIL
				// || record.getStatu() ==
				// MenueApiRecordType.STATU_NOTICE_SEND_FAIL) {
				// if (mLongPressDialog == null) {
				// mLongPressDialog = new LongPressDialog(this, new String[] {
				// "重新发送", "重新下载", "删除" }, new OnLongPressItemClickListener() {
				//
				// @Override
				// public void onClick(int listPostion, int itemIndex) {
				// InfoRecord record = adapter.getData().get(listPostion);
				// switch (itemIndex) {
				// case 0:
				// reSendNotifyToService(record);
				// mLongPressDialog.hide();
				// break;
				// // 重新下载
				// case 1:
				// reLoadPictrue(record);
				// mLongPressDialog.hide();
				// break;
				//
				// case 2:
				// GalDBHelper.getInstance(HomeActivity.this).deleteRecordById(record.getRecordId());
				// adapter.getData().remove(record);
				// adapter.notifyDataSetChanged();
				// notifyServiceDelete(record);
				// mLongPressDialog.hide();
				// break;
				// }
				//
				// }
				// });
				// }
				//
				// if (record.getStatu() ==
				// MenueApiRecordType.STATU_NOTICE_OPENED) {
				// mLongPressDialog.show(position, 0, 1);
				// } else if (record.getStatu() ==
				// MenueApiRecordType.STATU_NOTICE_LOAD_FAIL) {
				// mLongPressDialog.show(position, 0);
				// } else {
				// mLongPressDialog.show(position, 1);
				// }
				// }
			}
		}
	}

	protected void reLoadPictrue(InfoRecord record) {
		RCPlatformImageLoader.LoadPictureForList(this, null, null, mRecordListView,
				ImageLoader.getInstance(),
				ImageOptionsFactory.getReceiveImageOption(), record);
	}

	protected void loadMoreFromDatabase(final int i) {

		new AsyncTask<Integer, Integer, List<InfoRecord>>() {

			@Override
			protected List<InfoRecord> doInBackground(Integer... params) {
				List<InfoRecord> list = null;
				if (getAdapterData() != null && getAdapterData().size() > 0) {
					list = PhotoTalkDao.getInstance().loadTMoreInfoRecord(
							HomeActivity.this, i, String
									.valueOf(getAdapterData().get(
											getAdapterData().size() - 1)
											.getCreatetime()));
				} else {
					loadDataFromDataBase();
				}
				return list;
			}

			@Override
			protected void onPostExecute(List<InfoRecord> result) {
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
		final InfoRecord infoRecord = ((InfoRecord) mRecordListView
				.getAdapter().getItem(position));

		if (infoRecord.getType() == MenueApiRecordType.TYPE_PICTURE_OR_VIDEO
				&& infoRecord
						.getReceiver()
						.getSuid()
						.equals(String.valueOf(MenueApplication
								.getUserInfoInstall(this).getSuid()))) {
			// 表示还未查看
			if (infoRecord.getStatu() == MenueApiRecordType.STATU_NOTICE_DELIVERED_OR_LOADED) {
				// 这句还给View里面联合起作用，这边加入任务每一秒钟去setLimitTime改变值，然后RecordTimerLimitView在每秒钟显示下新的值
				TimerLimitUtil.getInstence().addTask(infoRecord);
				RecordTimerLimitView limitView = (RecordTimerLimitView) mRecordListView
						.findViewWithTag(infoRecord.getRecordId()
								+ Button.class.getName());
				limitView.setBackgroundDrawable(null);
				(limitView).scheuleTask(infoRecord);
				limitView.setOnTimeEndListener(
						new OnTimeEndListener() {

							@Override
							public void onEnd(Object statuTag, Object buttonTag) {

								RecordTimerLimitView timerLimitView = (RecordTimerLimitView) mRecordListView
										.findViewWithTag(buttonTag);
								if (timerLimitView != null) {
									timerLimitView
											.setBackgroundResource(R.drawable.receive_arrows_opened);
									timerLimitView.setText("");
								}
								TextView statu = ((TextView) mRecordListView
										.findViewWithTag(statuTag));
								if (statu != null) {
									statu.setText(R.string.statu_opened_1s_ago);
								}
								// 通知服务器改变状态为3，表示已经查看
								notifyService(infoRecord);

							}
						}, infoRecord.getRecordId() + TextView.class.getName(),
						infoRecord.getRecordId() + Button.class.getName());

				if (mShowDialog == null) {
					LongClickShowView.Builder builder = new LongClickShowView.Builder(
							HomeActivity.this, R.layout.receice_to_show_view);
					mShowDialog = builder.create();
				}
				mShowDialog.ShowDialog(infoRecord);
				isShow = true;
				// 把数据里面的状态更改为3，已查看
			} else if (infoRecord.getStatu() == MenueApiRecordType.STATU_NOTICE_SHOWING) {
				if (mShowDialog == null) {
					LongClickShowView.Builder builder = new LongClickShowView.Builder(
							HomeActivity.this, R.layout.receice_to_show_view);
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
				count = (int) ((eY - firstR.bottom) / listview.getChildAt(0)
						.getHeight());
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
				count = (int) ((eY - firstR.bottom) / listview.getChildAt(0)
						.getHeight());
				count++;
				index_of_childview = count;
				index_in_adapter += count;
			} else {
				index_of_childview = 0;
			}
		}

		/* 这即是触摸的那个list item view. */
		// ViewGroup child_view = (ViewGroup)
		// listview.getChildAt(index_of_childview);
		/* index_in_adapter即是触摸的子item view在adapter中对应的数据项 */
		/* index_of_childview即是触摸的item view在listview中的索引 */
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
			case LOAD_MORE_SUCCESS:
				mRefreshView.onFooterRefreshComplete();
				break;
			case LOAD_MORE_FAIL:
				mRefreshView.onFooterRefreshComplete();
				break;
			case REFRESH_FAIL:
				Toast.makeText(HomeActivity.this, "加载失败..", 1).show();
				mRefreshView.onHeaderRefreshComplete();
				break;
			}
		};
	};

	private void initOrRefreshListView(List<InfoRecord> data) {
		if (data == null || data.isEmpty()) {
			adapter.notifyDataSetChanged();
			return;
		}
		if (adapter == null) {
			adapter = new HomeUserRecordAdapter(this, data, mRecordListView);
			sortList(getAdapterData());
			mRecordListView.setAdapter(adapter);

		} else {
			getAdapterData().addAll(data);
			sortList(getAdapterData());
			adapter.notifyDataSetChanged();
		}
		// adapter = new HomeRecordAdapter(this, data);
	}

	private void notifyService(InfoRecord record) {
		if (record.getType() == MenueApiRecordType.TYPE_PICTURE_OR_VIDEO) {
			record.setStatu(MenueApiRecordType.STATU_NOTICE_OPENED);
			notifyServiceOpenedPic(record);
			PhotoTalkDao.getInstance().updateRecordStatu(this, record);
		}

	}

	private void notifyServiceOpenedPic(InfoRecord record) {
		Gson gson = new Gson();
		ServiceSimpleNotice notice = new ServiceSimpleNotice(record.getStatu()
				+ "", record.getRecordId() + "", record.getType() + "");
		List<ServiceSimpleNotice> list = new ArrayList<ServiceSimpleNotice>();
		list.add(notice);
		String s = gson.toJson(list,
				new TypeToken<List<ServiceSimpleNotice>>() {
				}.getType());
		Map<String, String> params = new HashMap<String, String>();
		params.put(MenueApiFactory.NOTICES, s);
		PhotoCharRequestService.getInstence().postRequest(this,
				notifyRecordOpenedCallBack, params,
				MenueApiUrl.HOME_USER_NOTICE_CHANGE);
	}

	private void notifyServiceDelete(InfoRecord record) {
		Gson gson = new Gson();
		ServiceSimpleNotice notice = new ServiceSimpleNotice(record.getStatu()
				+ "", record.getRecordId() + "", record.getType() + "");
		List<ServiceSimpleNotice> list = new ArrayList<ServiceSimpleNotice>();
		list.add(notice);
		String s = gson.toJson(list,
				new TypeToken<List<ServiceSimpleNotice>>() {
				}.getType());
		Map<String, String> params = new HashMap<String, String>();
		params.put(MenueApiFactory.NOTICES, s);
		PhotoCharRequestService.getInstence().postRequest(this,
				notifyRecordOpenedCallBack, params,
				MenueApiUrl.HOME_USER_NOTICE_DELETE);
	}

	// private void openedNotice(InfoRecord record) {
	// Gson gson = new Gson();
	// ServiceSimpleNotice notice = new
	// ServiceSimpleNotice(record.getNoticeId(), record.getStatu() + "",
	// record.getRecordId() + "",
	// record.getType() + "");
	// List<ServiceSimpleNotice> list = new ArrayList<ServiceSimpleNotice>();
	// list.add(notice);
	// String s = gson.toJson(list, new TypeToken<List<ServiceSimpleNotice>>() {
	// }.getType());
	//
	// GalHttpRequest request = GalHttpRequest.requestWithURL(this,
	// MenueApiUrl.HOME_USER_NOTICE_DELETE);
	// // request.setPostValueForKey(MenueApiFactory.USER, email);
	// request.setPostValueForKey(MenueApiFactory.TOKEN,
	// MenueApplication.getUserInfoInstall(this).getToken());
	// request.setPostValueForKey(MenueApiFactory.USERID,
	// MenueApplication.getUserInfoInstall(this).getSuid());
	// request.setPostValueForKey(MenueApiFactory.LANGUAGE,
	// Locale.getDefault().getLanguage());
	// request.setPostValueForKey(MenueApiFactory.DEVICE_ID,
	// android.os.Build.DEVICE);
	// request.setPostValueForKey(MenueApiFactory.APP_ID, Contract.APP_ID);
	//
	// request.setPostValueForKey(MenueApiFactory.NOTICES, s);
	//
	// request.startAsynRequestString(new GalHttpLoadTextCallBack() {
	//
	// @Override
	// public void textLoaded(String text) {
	// Log.i("AAA", "openedNotice" + text.toString());
	// }
	//
	// @Override
	// public void loadFail() {
	// Log.i("AAA", "openedNotice fail");
	// }
	// });
	//
	// }

	/**
	 * 把服务器返回的数据list 转换为本地需要的list
	 * 
	 * @param list
	 *            服务器的list
	 * @return 本地需要的list
	 */
	private List<InfoRecord> convertData(List<ServiceRecordInfo> list) {
		List<InfoRecord> data = new ArrayList<InfoRecord>();
		InfoRecord infoRecord;
		for (ServiceRecordInfo info : list) {
			infoRecord = new InfoRecord();
			infoRecord.setRecordId(info.getId().trim());
			infoRecord.setNoticeId(info.getNoticeId());
			infoRecord.setCreatetime(info.getCrtTime());
			infoRecord.setLimitTime(info.getTime());
			infoRecord.setSender(new RecordUser(info.getSeUserId(), info
					.getSeSuid(), info.getSnick(), info.getShead()));
			infoRecord.setReceiver(new RecordUser(info.getReUserId(), info
					.getReSuid(), info.getRnick(), info.getRhead()));
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
				for (InfoRecord info : app.getSendRecordsList(time)) {
					info.setStatu(MenueApiRecordType.STATU_NOTICE_SEND_FAIL);
				}
				// 把当前这次发送错误的信息写入数据库保存，并删除app中的引用
				PhotoTalkDao.getInstance().insertInfoRecord(this,
						app.getSendRecordsList(time));
			}
			initOrRefreshListView(null);
			return;
		}
		try {
			JSONObject jsonObject = null;
			jsonObject = new JSONObject(text);
			int state = jsonObject.getInt(MenueApiFactory.RESPONSE_KEY_STATUS);
			if (state == MenueApiFactory.RESPONSE_STATE_SUCCESS) {
				List<ServiceRecordInfo> recordInfo = (List<ServiceRecordInfo>) new Gson()
						.fromJson(jsonObject.getJSONArray("noticeList")
								.toString(),
								new TypeToken<List<ServiceRecordInfo>>() {
								}.getType());
				if (recordInfo != null && recordInfo.size() > 0) {
					if (app.getSendRecordsList(time) != null
							&& app.getSendRecordsList(time).size() > 0) {
						// 删除 本地缓存的图片
						String fileName = app.getSendRecordsList(time).get(0)
								.getUrl();
						app.deleteSendFileCache(fileName);
						// 删除app 中的记录
						app.getSendRecords().remove(time);
						// 删除 list列表里面的
						clearListDataByTime(getAdapterData(), time);

						List<InfoRecord> listinfo = convertData(recordInfo);

						filterList(listinfo);
					}
				}
			} else {
				if (app.getSendRecordsList(time) != null) {
					for (InfoRecord info : app.getSendRecordsList(time)) {
						if (info.getCreatetime() == time) {
							info.setStatu(MenueApiRecordType.STATU_NOTICE_SEND_FAIL);
						}
					}

					PhotoTalkDao.getInstance().insertInfoRecord(this,
							app.getSendRecordsList(time));
					// app.getSendRecords().remove(time);
				}
				initOrRefreshListView(null);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private void clearListDataByTime(List<InfoRecord> data2, long time) {
		Iterator<InfoRecord> iterator = data2.iterator();
		while (iterator.hasNext()) {
			InfoRecord infoRecord = iterator.next();
			if (infoRecord.getCreatetime() == time
					&& (infoRecord.getNoticeId()
							.equals(MenueApiFactory.ERROR_NOTICE))) {
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
	protected void filterList(List<InfoRecord> listinfo) {
		boolean isNeedRefreshAdpter = false;
		List<InfoRecord> newNotices = new ArrayList<InfoRecord>();
		Iterator<InfoRecord> iterator = listinfo.iterator();
		List<InfoRecord> data = getAdapterData();
		while (iterator.hasNext()) {
			InfoRecord record = iterator.next();
			// 如果是通知
			if (data != null && data.contains(record)) {
				// 如果当前list包含了这个notcie 即 record_id ，type 一样

				// 那么判断当前这条notice的statu 和集合里面有同样 record_id 和type的 statu
				// 是否一样

				// 如果一样，说明这条notice没有发生任何变化，就删除，不做任何操作
				if (record.getStatu() == data.get(data.indexOf(record))
						.getStatu()) {
					iterator.remove();
				}
				// 如果不一样,说明当前list 里面 的这条notice
				else {

					if (record.getType() == MenueApiRecordType.TYPE_FRIEND_REQUEST_NOTICE) {
						data.get(data.indexOf(record)).setStatu(
								record.getStatu());
						isNeedRefreshAdpter = true;
					} else if (record.getType() == MenueApiRecordType.TYPE_PICTURE_OR_VIDEO) {

						// 判断 当前本地notice的状态是否等于其他几种特殊状态（0：正在发送，4，正在查看，5
						// 正在下载）,由于0是本地临时的，在服务器是没有这种状态的，所以比考虑

						if (record.getStatu() == MenueApiRecordType.STATU_NOTICE_SENDED_OR_NEED_LOADD) {
							// 如果状态为1，表示需要去下载，但是我们得去判断是否正在下载，
							// 对于发送者来说什么都不做，只有接受者会有操作
							if (data.get(data.indexOf(record)).getStatu() == MenueApiRecordType.STATU_NOTICE_LOADING
									|| data.get(data.indexOf(record))
											.getStatu() == MenueApiRecordType.STATU_NOTICE_SHOWING) {
								// 状态为5或者4
								// 表示是正在下载或者正在查看，说明已经下载下来了，那么就不需要去下载了
							} else if (data.get(data.indexOf(record))
									.getStatu() == MenueApiRecordType.STATU_NOTICE_DELIVERED_OR_LOADED
									|| data.get(data.indexOf(record))
											.getStatu() == MenueApiRecordType.STATU_NOTICE_OPENED) {
								// 如果本地的notice
								// 是2或者3了，表示已经下载下来了，但是服务器还是一，说明通知服务器改状态没有成功，那么的通知服务器改状态，本地什么都不做
								notifyService(data.get(data.indexOf(record)));
							} else if (record.getStatu() == MenueApiRecordType.STATU_NOTICE_LOAD_FAIL) {
								data.get(data.indexOf(record)).setStatu(
										record.getStatu());
								isNeedRefreshAdpter = true;
							}
						} else if (record.getStatu() == MenueApiRecordType.STATU_NOTICE_DELIVERED_OR_LOADED) {
							// 对发送者来说，只需要改变状态，并通知界面刷新就OK
							if (String.valueOf(
									MenueApplication.getUserInfoInstall(
											HomeActivity.this).getSuid())
									.equals(record.getSender().getSuid())) {
								data.get(data.indexOf(record)).setStatu(
										record.getStatu());
								isNeedRefreshAdpter = true;
							} else {
								// 对于接收者来说，图片一已经下载完成了，那就要判断本地的状态时候为3，如果为3，那说明本地查看后
								// 只改了本地的状态，通知服务器改状态失败了，得再次通知，其他的就不用做任何操作
								if (data.get(data.indexOf(record)).getStatu() == MenueApiRecordType.STATU_NOTICE_OPENED) {
									notifyService(data
											.get(data.indexOf(record)));
								}
							}

						} else if (record.getStatu() == MenueApiRecordType.STATU_NOTICE_OPENED) {
							if (String.valueOf(
									MenueApplication.getUserInfoInstall(
											HomeActivity.this).getSuid())
									.equals(record.getSender().getSuid())) {
								data.get(data.indexOf(record)).setStatu(
										record.getStatu());
								isNeedRefreshAdpter = true;
							}

						}

						// 为什么没有状态为3的情况，因为状态为3，不管是发送者还是接收者，这条记录生命线已经完了，不需要做任何操作了
					} else if (record.getType() == MenueApiRecordType.TYPE_SYSTEM_NOTICE) {
						// 什么都不做，没有任何操作
					}
				}
			} else {
				// 如果当前list里面不包含这个notice的话 那么又分为以下几种情况
				// 1,判断数据库里面是否有这条notice
				InfoRecord dbInfoRecord = PhotoTalkDao.getInstance()
						.findRecordByRecordId(this, record.getRecordId());
				// 如果有，
				if (dbInfoRecord != null) {
					// 判断状态是否发生变化
					if (dbInfoRecord.getStatu() != record.getStatu()) {
						// 发生了变化
						dbInfoRecord.setStatu(record.getStatu());
						PhotoTalkDao.getInstance()
								.updateRecordStatu(this, dbInfoRecord);
					}
					// 相等则什么都不做，
				} else {
					// 如果数据里面 没有,假如缓存list，和插入数据库
					newNotices.add(record);
				}

				// 如果没有，说明是新的notice ，那么添加当一个缓存list里面 ，并插入数据库
			}

		}

		if (isNeedRefreshAdpter == true || newNotices.size() > 0) {
			initOrRefreshListView(newNotices);
			// 并且把新数据插入数据库
			PhotoTalkDao.getInstance().insertInfoRecord(this, newNotices);
		}
		mRefreshView.onHeaderRefreshComplete();

	}

	private void sortList(List<InfoRecord> list) {
		Collections.sort(list, comparator);
	}

	Comparator<InfoRecord> comparator = new Comparator<InfoRecord>() {

		@Override
		public int compare(InfoRecord lhs, InfoRecord rhs) {

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

	private final GalHttpLoadTextCallBack loadFromServiceCallBack = new GalHttpLoadTextCallBack() {

		@Override
		public void textLoaded(String text) {
			JSONObject obj;
			try {
				obj = new JSONObject(text);
				int state = obj.getInt(MenueApiFactory.RESPONSE_KEY_STATUS);
				if (state == MenueApiFactory.RESPONSE_STATE_SUCCESS) {
					int newNoticeId = obj.getInt(MenueApiFactory.NOTICE_ID);
					int oldNoticeId = Integer.parseInt(PrefsUtils
							.getNoticeId(HomeActivity.this));
					if (newNoticeId > oldNoticeId) {
						PrefsUtils.savaNoticeId(HomeActivity.this,
								String.valueOf(newNoticeId));
					}

					@SuppressWarnings("unchecked")
					List<ServiceRecordInfo> recordInfo = (List<ServiceRecordInfo>) new Gson()
							.fromJson(obj.getJSONArray("mainUserNotice")
									.toString(),
									new TypeToken<List<ServiceRecordInfo>>() {
									}.getType());

					List<InfoRecord> listinfo = convertData(recordInfo);
					filterList(listinfo);
				} else {
					myHandler.sendEmptyMessage(REFRESH_FAIL);
				}
			} catch (Exception e) {
				myHandler.sendEmptyMessage(REFRESH_FAIL);
				e.printStackTrace();
			}
		}

		@Override
		public void loadFail() {
			myHandler.sendEmptyMessage(REFRESH_FAIL);
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
		params.put(MenueApiFactory.NOTICE_ID, PrefsUtils.getNoticeId(this));
		notifyServiceDeleteAll(params);
		PhotoTalkDao.getInstance().deleteCurrentUserTable(this);
		((HomeUserRecordAdapter) mRecordListView.getAdapter()).getData()
				.clear();
		((HomeUserRecordAdapter) mRecordListView.getAdapter())
				.notifyDataSetChanged();
		return super.onOptionsItemSelected(item);
	}

	private void notifyServiceDeleteAll(final Map<String, String> params) {
		PhotoCharRequestService.getInstence().postRequest(this,
				new GalHttpLoadTextCallBack() {

					@Override
					public void textLoaded(String text) {
						JSONObject obj;
						try {
							obj = new JSONObject(text);
							int state = obj
									.getInt(MenueApiFactory.RESPONSE_KEY_STATUS);
							if (state != MenueApiFactory.RESPONSE_STATE_SUCCESS) {
								String jsonParams = new Gson().toJson(params,
										new TypeToken<Map<String, String>>() {
										}.getType());
								PhotoTalkDao.getInstance()
										.insertFailRequestInfo(
												HomeActivity.this,
												MenueApiUrl.HOME_USER_NOTICE_DELETE_ALL,
												jsonParams);
							} else {
								String request_id = params.get("id");
								if (request_id != null
										&& request_id.length() > 0) {
									PhotoTalkDao.getInstance()
											.deleteFailRequestInfoById(
													HomeActivity.this,
													request_id);
								}
							}
						} catch (Exception ex) {

						}
					}

					@Override
					public void loadFail() {
						String jsonParams = new Gson().toJson(params,
								new TypeToken<Map<String, String>>() {
								}.getType());
						PhotoTalkDao.getInstance().insertFailRequestInfo(
								HomeActivity.this,
								MenueApiUrl.HOME_USER_NOTICE_DELETE_ALL,
								jsonParams);
					}
				}, params, MenueApiUrl.HOME_USER_NOTICE_DELETE_ALL);
	}

	private void checkFialRequest() {
		List<Map<String, String>> requests = PhotoTalkDao.getInstance()
				.findAllFailRequestInfo(this);
		if (requests != null && requests.size() > 0) {
			Map<String, String> params;
			for (Map<String, String> info : requests) {
				params = new Gson().fromJson(info.get("params"),
						new TypeToken<Map<String, String>>() {
						}.getType());
				params.put("id", info.get("id"));
				notifyServiceDeleteAll(params);
			}
		}
	}

	private List<InfoRecord> getAdapterData() {
		if (adapter == null)
			return null;
		return adapter.getData();
	}
}