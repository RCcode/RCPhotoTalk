package com.rcplatform.phototalk.drift;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnShowListener;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.SparseIntArray;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AlphaAnimation;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.flurry.android.FlurryAgent;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;
import com.rcplatform.clientlog.ClientLogUtil;
import com.rcplatform.phototalk.EditPictureActivity;
import com.rcplatform.phototalk.EditUserCountryActivity;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.StrangerDetailActivity;
import com.rcplatform.phototalk.TakePhotoActivity;
import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.InformationState;
import com.rcplatform.phototalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.phototalk.image.downloader.RCPlatformImageLoader;
import com.rcplatform.phototalk.logic.DriftInformationCountDownService;
import com.rcplatform.phototalk.logic.LogicUtils;
import com.rcplatform.phototalk.logic.controller.DriftInformationPageController;
import com.rcplatform.phototalk.proxy.DriftProxy;
import com.rcplatform.phototalk.request.handler.DriftShowTimeResponseHandler;
import com.rcplatform.phototalk.request.handler.DriftShowTimeResponseHandler.OnDriftShowTimeListener;
import com.rcplatform.phototalk.request.handler.FishDriftResponseHandler;
import com.rcplatform.phototalk.request.handler.FishDriftResponseHandler.OnFishListener;
import com.rcplatform.phototalk.request.handler.ThrowDriftResponseHandler;
import com.rcplatform.phototalk.umeng.EventUtil;
import com.rcplatform.phototalk.utils.Constants;
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

/**
 * 主界面. <br>
 * 主界面记录了用户最近的一些记录，包括给哪些好友发送了图片，收到了哪些好友的发送的图片或者好友添加通知，通过主界面可以进入拍照界面，
 * 拍照完成后后可以进行编辑和发送给好友
 * 
 * @version 1.0.0
 */
public class DriftInformationActivity extends BaseActivity implements SnapShowListener, OnClickListener {

	private static final int MSG_WHAT_INFORMATION_LOADED = 1;
	private static final String USERDRIFT = "userDrift";
	private static final int MSG_WHAT_LOCAL_INFORMATION_LOADED = 4;
	public static final String PARAM_FRIEND = "friend";
	public static final String PARAM_INFORMATION = "information";
	public static final String PARAM_FROM_PAGE = "isFromStangerPage";

	private static final int REQUEST_KEY_COUNTRY_CHOOSE_FISH_RANGE = 100000;
	private static final int REQUEST_KEY_COUNTRY_CHOOSE_THROW = 100001;

	private SnapListView mInformationList;

	protected LongClickShowView mShowDialog;

	private boolean isShow;

	private LongPressDialog mLongPressDialog;

	private DriftInformationAdapter adapter;

	private ImageLoader mImageLoader;

	private boolean hasNextPage = true;

	private View loadingFooter;

	public static final String INTENT_KEY_STATE = "state";

	private boolean isLoading = false;

	private Button btnFish;
	private Button btnThrow;
	private ViewFlipper pager;
	private GestureDetector mGestureDetector;
	private static DriftShowMode mShowMode = DriftShowMode.ALL;

	static enum DriftShowMode {
		ALL, MY_COUNTRY;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.drift_information);
		mImageLoader = ImageLoader.getInstance();
		DriftInformationPageController.getInstance().setup(this);
		initViewAndListener();
		loadLocalInformation();
		ClientLogUtil.log(this);
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
	}

	private void sendDataLoadedMessage(List<DriftInformation> infos, int what) {
		Message msg = myHandler.obtainMessage();
		msg.what = what;
		msg.obj = infos;
		myHandler.sendMessage(msg);
	}

	@SuppressWarnings("deprecation")
	private void initViewAndListener() {
		initBackButton(R.string.know_strangers, new OnClickListener() {

			@Override
			public void onClick(View v) {
				finish();
			}
		});
		initForwordButton(R.drawable.btn_drift_filter, new OnClickListener() {

			@Override
			public void onClick(View v) {
				showFilterMenu();
			}
		});
		mInformationList = (SnapListView) findViewById(R.id.lv_drift);
		mInformationList.setSnapListener(this);
		mInformationList.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				EventUtil.Main_Photo.rcpt_main_longpress(baseContext);
				showLongClickDialog(arg2);
				return false;
			}
		});
		mInformationList.setOnItemClickListener(driftListItemClickListener);
		btnFish = (Button) findViewById(R.id.btn_get_drift);
		btnThrow = (Button) findViewById(R.id.btn_throw_drift);
		btnFish.setOnClickListener(this);
		btnThrow.setOnClickListener(this);
		pager = (ViewFlipper) findViewById(R.id.drift_view_pager);
		// 判断是否第一次进入漂流瓶
		boolean isShow = PrefsUtils.User.hasUsedDrift(this, USERDRIFT);
		if (!isShow) {
			pager.setVisibility(View.VISIBLE);
		} else {
			pager.setVisibility(View.GONE);
		}
		pager.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return mGestureDetector.onTouchEvent(event);
			}
		});
		pager.setLongClickable(true);
		mGestureDetector = new GestureDetector(new OnGestureListener() {

			@Override
			public boolean onSingleTapUp(MotionEvent e) {
				if (numView == pager.getChildCount() - 1) {
					closePaper();
				} else {
					pager.setInAnimation(DriftInformationActivity.this, R.anim.left_in);
					pager.setOutAnimation(DriftInformationActivity.this, R.anim.left_out);
					if (numView < pager.getChildCount() - 1) {
						numView++;
						pager.setDisplayedChild(numView);
					}
				}
				return false;
			}

			@Override
			public void onShowPress(MotionEvent e) {

			}

			@Override
			public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
				return false;
			}

			@Override
			public void onLongPress(MotionEvent e) {

			}

			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
				if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
					pager.setInAnimation(DriftInformationActivity.this, R.anim.left_in);
					pager.setOutAnimation(DriftInformationActivity.this, R.anim.left_out);
					if (numView < pager.getChildCount() - 1) {
						numView++;
						pager.setDisplayedChild(numView);
					} else {
						closePaper();
					}
				} else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE && Math.abs(velocityX) > FLING_MIN_VELOCITY) {
					pager.setInAnimation(DriftInformationActivity.this, R.anim.rigth_out);
					pager.setOutAnimation(DriftInformationActivity.this, R.anim.right_in);
					if (numView > 0) {
						numView--;
						pager.setDisplayedChild(numView);
					}
				}
				return false;
			}

			@Override
			public boolean onDown(MotionEvent e) {
				return false;
			}
		});
		mGestureDetector.setIsLongpressEnabled(true);
	}

	private void closePaper() {
		AlphaAnimation animation = new AlphaAnimation(1.0f, 0.0f);
		animation.setDuration(500);
		pager.setAnimation(animation);
		pager.setVisibility(View.GONE);
		PrefsUtils.User.setDriftUsed(DriftInformationActivity.this, USERDRIFT);
	}

	private OnItemClickListener driftListItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
			DriftInformation information = (DriftInformation) adapter.getItem(arg2);
			if (information.getState() == InformationState.PhotoInformationState.STATU_NOTICE_SHOWING)
				return;
			if (information.getState() == InformationState.PhotoInformationState.STATU_NOTICE_SEND_OR_LOAD_FAIL) {
				if (LogicUtils.isSender(DriftInformationActivity.this, information)) {
					reSendPicture(information);
				} else {
					reLoadPictrue(information);
				}
			} else {
				EventUtil.Main_Photo.rcpt_main_profileview(baseContext);
				showFriendDetail(information);
			}
		}
	};

	private void initMenuButton() {
		// TODO
	}

	private void showFriendDetail(DriftInformation information) {
		Friend friend = null;
		if (information.getSender().getRcId().equals(getCurrentUser().getRcId())) {
			friend = PhotoTalkUtils.userToFriend(getCurrentUser());
		} else {
			friend = new Friend();
			friend.setRcId(information.getSender().getRcId());
			friend.setAppId(information.getSender().getAppId());
			friend.setAdded(information.getSender().getIsFriend());
			friend.setGender(information.getSender().getGender());
			friend.setHeadUrl(information.getSender().getHeadUrl());
			friend.setCountry(information.getSender().getCountry());
			friend.setNickName(information.getSender().getNick());
			friend.setBackground(information.getSender().getBackUrl());
			friend.setBirthday(information.getSender().getBirthday());
			friend.setTigaseId(information.getSender().getTigaseId());
		}
		searchFriend(friend, information);
	}

	private void searchFriend(Friend friend, final DriftInformation information) {
		startFriendDetailActivity(friend, information);
	}

	private void startFriendDetailActivity(Friend friend, DriftInformation information) {
		Intent intent = new Intent(this, StrangerDetailActivity.class);
		intent.putExtra(PARAM_FRIEND, friend);
		intent.putExtra(PARAM_INFORMATION, information);
		intent.putExtra(PARAM_FROM_PAGE, true);
		intent.putExtra(StrangerDetailActivity.PARAM_BACK_PAGE, DriftInformationActivity.class);
		startActivity(intent);
	}

	protected void showLongClickDialog(int position) {
		if (adapter != null) {
			DriftInformation record = adapter.getData().get(position);
			if (record != null) {
				if (record.getState() == InformationState.PhotoInformationState.STATU_NOTICE_SHOWING) {
					return;
				} else {
					if (mLongPressDialog == null) {
						mLongPressDialog = new LongPressDialog(this, new String[] { getString(R.string.resend), getString(R.string.reload),
								getString(R.string.delete) }, new OnLongPressItemClickListener() {

							@Override
							public void onClick(int listPostion, int itemIndex) {
								DriftInformation record = adapter.getData().get(listPostion);
								switch (itemIndex) {
								case 0:
									// 重新下载
									reSendPicture(record);
									break;
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

					if (record.getState() == InformationState.PhotoInformationState.STATU_NOTICE_SEND_OR_LOAD_FAIL) {
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

	protected void reSendPicture(DriftInformation record) {
		record.setState(InformationState.PhotoInformationState.STATU_NOTICE_SENDING_OR_LOADING);
		PhotoTalkDatabaseFactory.getDatabase().resendDriftInformation(record.getFlag(), getCurrentUser().getRcId());
		adapter.notifyDataSetChanged();
		DriftProxy.throwDriftInformation(this, new ThrowDriftResponseHandler(this, record.getFlag(), record.getUrl(), record.getType()), getCurrentUser(),
				null, record.getTotleLength() + "", record.hasGraf(), record.hasVoice(), record.getUrl(), record.getFlag(), record.getType());
	}

	private void deleteInformation(DriftInformation information) {
		adapter.getData().remove(information);
		adapter.notifyDataSetChanged();
		LogicUtils.deleteInformation(this, information);
	}

	public ListView getDriftInformationList() {
		return mInformationList;
	}

	protected void reLoadPictrue(DriftInformation record) {
		RCPlatformImageLoader.loadPictureForDriftList(this, record);
	}

	private void show(int position) {
		EventUtil.Main_Photo.rcpt_photoview(baseContext);
		final DriftInformation infoRecord = (DriftInformation) adapter.getItem(position);
		if (!LogicUtils.isSender(this, infoRecord)) {
			// 表示还未查看
			if (infoRecord.getState() == InformationState.PhotoInformationState.STATU_NOTICE_DELIVERED_OR_LOADED
					|| infoRecord.getState() == InformationState.PhotoInformationState.STATU_NOTICE_SHOWING) {
				if (mShowDialog == null) {
					LongClickShowView.Builder builder = new LongClickShowView.Builder(DriftInformationActivity.this, R.layout.receice_to_show_view);
					mShowDialog = builder.create();
				}
				final RecordTimerLimitView limitView = (RecordTimerLimitView) mInformationList.findViewWithTag(infoRecord.getPicId() + Button.class.getName());
				ImageView ivCountry = (ImageView) mInformationList.findViewWithTag(infoRecord.getPicId() + ImageView.class.getName() + "country");
				ivCountry.setVisibility(View.GONE);
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
				mInformationList.setOnItemClickListener(driftListItemClickListener);
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
				addInformationAtFirst((List<DriftInformation>) msg.obj);
				break;
			case MSG_WHAT_LOCAL_INFORMATION_LOADED:
				dissmissLoadingDialog();
				addListData((List<DriftInformation>) msg.obj);
				break;
			}
		}
	};

	private void addListData(List<DriftInformation> localInformations) {
		if (adapter == null) {
			adapter = new DriftInformationAdapter(this, localInformations, mImageLoader);
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

	protected void addNewInformation(DriftInformation obj) {

	}

	private void addInformationAtFirst(List<DriftInformation> data) {
		adapter.addData(data);
		adapter.notifyDataSetChanged();
		moveToListFirst();
	}

	private void moveToListFirst() {
		mInformationList.setSelection(0);
	}

	@Override
	protected void onDestroy() {
		DriftInformationCountDownService.getInstance().finishAllShowingMessage();
		DriftInformationPageController.getInstance().destroy();
		if (mShowDialog != null && mShowDialog.isShowing())
			mShowDialog.dismiss();
		super.onDestroy();
	}

	private List<DriftInformation> getAdapterData() {
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

	public void onInformationShowEnd(DriftInformation information) {
		String tagBase = information.getPicId() + "";
		String buttonTag = tagBase + Button.class.getName();
		String statuTag = tagBase + TextView.class.getName();
		String newTag = tagBase + ImageView.class.getName();
		String countryTag = tagBase + ImageView.class.getName() + "country";
		RecordTimerLimitView timerLimitView = (RecordTimerLimitView) mInformationList.findViewWithTag(buttonTag);
		if (timerLimitView != null) {
			timerLimitView.setText(null);
			timerLimitView.setBackgroundDrawable(null);
		}
		TextView statu = ((TextView) mInformationList.findViewWithTag(statuTag));
		if (statu != null) {
			statu.setText(getString(R.string.receive_looked, RCPlatformTextUtil.getTextFromTimeToNow(this, information.getReceiveTime())));
		}
		View newView = mInformationList.findViewWithTag(newTag);
		if (newView != null)
			newView.setVisibility(View.GONE);
		ImageView ivCountry = (ImageView) mInformationList.findViewWithTag(countryTag);
		if (ivCountry != null) {
			ivCountry.setImageBitmap(Utils.getAssetCountryFlag(this, information.getSender().getCountry()));
			ivCountry.setVisibility(View.VISIBLE);
		}
	}

	public void clearInformation() {
		if (adapter != null) {
			adapter.getData().clear();
			adapter.notifyDataSetChanged();
		}
	}

	public void onPhotoSending(List<DriftInformation> informations) {
		sendDataLoadedMessage(informations, MSG_WHAT_INFORMATION_LOADED);
	}

	public void onPhotoSendSuccess(long flag, int picId) {
		List<DriftInformation> localInfos = getAdapterData();
		if (localInfos != null) {
			for (DriftInformation info : localInfos) {
				if (info.getState() == InformationState.PhotoInformationState.STATU_NOTICE_SENDING_OR_LOADING && info.getFlag() == flag) {
					info.setState(InformationState.PhotoInformationState.STATU_NOTICE_SENDED_OR_NEED_LOADD);
					info.setPicId(picId);
				}
			}
			adapter.notifyDataSetChanged();
		}
	}

	public void onPhotoResendSuccess(Information information) {
		List<DriftInformation> localInfos = getAdapterData();
		if (localInfos != null) {
			int index = localInfos.indexOf(information);
			if (index != -1) {
				localInfos.get(index).setState(InformationState.PhotoInformationState.STATU_NOTICE_SENDED_OR_NEED_LOADD);
				adapter.notifyDataSetChanged();
			}
		}
	}

	public void onPhotoSendFail(long flag) {
		List<DriftInformation> localInfos = getAdapterData();
		if (localInfos != null) {
			for (DriftInformation info : localInfos) {
				if (info.getState() == InformationState.PhotoInformationState.STATU_NOTICE_SENDING_OR_LOADING && info.getFlag() == flag) {
					info.setState(InformationState.PhotoInformationState.STATU_NOTICE_SEND_OR_LOAD_FAIL);
				}
			}
			adapter.notifyDataSetChanged();
		}
	}

	public void onPhotoResendFail(Information information) {
		List<DriftInformation> localInfos = getAdapterData();
		if (localInfos != null) {
			int index = localInfos.indexOf(information);
			if (index != -1) {
				localInfos.get(index).setState(InformationState.PhotoInformationState.STATU_NOTICE_SEND_OR_LOAD_FAIL);
				adapter.notifyDataSetChanged();
			}
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
			showLoadingDialog(false);
			isLoading = true;
			loadLocalInformationsAsync();
		}
	}

	private void loadLocalInformationsAsync() {
		RCThreadPool.getInstance().addTask(new Runnable() {

			@Override
			public void run() {
				int start = 0;
				if (mInformationList.getAdapter() != null)
					start = mInformationList.getAdapter().getCount() - 1;
				List<DriftInformation> informations = null;
				switch (mShowMode) {
				case ALL:
					informations = PhotoTalkDatabaseFactory.getDatabase().getDriftInformations(start, Constants.INFORMATION_PAGE_SIZE);
					break;
				case MY_COUNTRY:
					informations = PhotoTalkDatabaseFactory.getDatabase().getDriftInformationByCountry(start, Constants.INFORMATION_PAGE_SIZE,
							getCurrentUser().getCountry());
					break;
				}
				List<Integer> picIds = new ArrayList<Integer>();
				String currentRcId = getCurrentUser().getRcId();
				for (DriftInformation information : informations) {
					if (information.getSender().getRcId().equals(currentRcId))
						picIds.add(information.getPicId());
				}
				if (picIds.size() > 0) {
					loadSendedDriftShowTimes(picIds);
				}
				Message msg = myHandler.obtainMessage();
				msg.what = MSG_WHAT_LOCAL_INFORMATION_LOADED;
				msg.obj = informations;
				myHandler.sendMessage(msg);
			}
		});
	}

	protected void loadSendedDriftShowTimes(List<Integer> picIds) {
		DriftProxy.getDriftInformationShowTime(this, new DriftShowTimeResponseHandler(this, new OnDriftShowTimeListener() {

			@Override
			public void onGetSuccess(SparseIntArray picShowTimes) {
				adapter.addShowTimes(picShowTimes);
				adapter.notifyDataSetChanged();
			}
		}), picIds);
	}

	public ListView getInformationList() {
		return mInformationList;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_get_drift:
			fishInformation();
			break;
		case R.id.btn_throw_drift:
			if (PrefsUtils.User.isFirstTimeChooseDriftRange(this, getCurrentUserRcId())) {
				showCountryConfirmDialog(REQUEST_KEY_COUNTRY_CHOOSE_THROW);
			} else
				throwInformation();
			break;
		case R.id.btn_show_all:
			filterMenu.dismiss();
			if (mShowMode != DriftShowMode.ALL) {
				changeShowMode(DriftShowMode.ALL);
			}
			break;
		case R.id.btn_show_my_country:
			filterMenu.dismiss();
			if (mShowMode != DriftShowMode.MY_COUNTRY) {
				changeShowMode(DriftShowMode.MY_COUNTRY);
			}
			break;
		}
	}

	private void changeShowMode(DriftShowMode mode) {
		mShowMode = mode;
		reset();
		loadLocalInformation();
	}

	private void reset() {
		clearInformation();
		hasNextPage = true;
		isLoading = false;
		adapter = null;
		mInformationList.removeFooterView(loadingFooter);
		mInformationList.setAdapter(null);
		mInformationList.setOnScrollListener(null);
	}

	private void fishInformation() {
		String currentRcId = getCurrentUser().getRcId();
		if (LogicUtils.isAttentionThrowDrift(this, currentRcId)) {
			showThrowDriftDialog();
			return;
		}
		int fishLeaveTime = PrefsUtils.User.getFishLeaveTime(this, currentRcId);
		if (fishLeaveTime > 0) {
			executeFishDriftInformation();
		} else if (fishLeaveTime == 0) {
			showConfirmDialog(R.string.fish_over);
		}
	}

	private void executeFishDriftInformation() {
		showLoadingDialog(false);
		DriftProxy.fishDrift(this, new FishDriftResponseHandler(this, new OnFishListener() {

			@Override
			public void onFishSuccess(DriftInformation information) {
				PrefsUtils.User.setLastFishTime(getApplicationContext(), getCurrentUser().getRcId());
				List<DriftInformation> infos = new ArrayList<DriftInformation>();
				infos.add(information);
				sendDataLoadedMessage(infos, MSG_WHAT_INFORMATION_LOADED);
			}

			@Override
			public void onFishFail(String failReason) {
				showConfirmDialog(failReason);
			}
		}), mShowMode == DriftShowMode.MY_COUNTRY, false);
	}

	private void throwInformation() {
		Intent intent = new Intent(this, TakePhotoActivity.class);
		intent.putExtra("friend", PhotoTalkUtils.getDriftFriend());
		intent.putExtra(EditPictureActivity.PARAM_KEY_BACK_PAGE, DriftInformationActivity.class);
		startActivity(intent);
	}

	private PopupWindow filterMenu;

	private void showFilterMenu() {
		String[] items = new String[] { getString(R.string.all_world), getString(R.string.my_country) };
		AlertDialog dialog = DialogUtil.getAlertDialogBuilder(this).setTitle(R.string.set_drift_range).setItems(items, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case 0:
					changeShowMode(DriftShowMode.ALL);
					break;
				case 1:
					chooseMyCountry();
					break;
				}
			}
		}).create();
		dialog.show();
	}

	private void chooseMyCountry() {
		if (PrefsUtils.User.isFirstTimeChooseDriftRange(this, getCurrentUserRcId())) {
			showCountryConfirmDialog(REQUEST_KEY_COUNTRY_CHOOSE_FISH_RANGE);
		} else {
			changeShowMode(DriftShowMode.MY_COUNTRY);
		}

	}

	private AlertDialog throwDialog;

	private void showThrowDriftDialog() {
		if (throwDialog == null)
			throwDialog = DialogUtil.getAlertDialogBuilder(this).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					throwInformation();
				}
			}).setNegativeButton(R.string.cancel, null).setMessage(R.string.throw_dialog_msg).setTitle(R.string.throw_dialog_title).create();
		throwDialog.show();
	}

	private void showCountryConfirmDialog(final int requestCode) {
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				PrefsUtils.User.setDriftRangeCheese(DriftInformationActivity.this, getCurrentUserRcId());
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					if (requestCode == REQUEST_KEY_COUNTRY_CHOOSE_FISH_RANGE)
						changeShowMode(DriftShowMode.MY_COUNTRY);
					else
						throwInformation();
					break;
				case DialogInterface.BUTTON_NEGATIVE:
					startCountryChooseActivity(requestCode);
					break;
				}
			}
		};
		View dialogView = getLayoutInflater().inflate(R.layout.confirm_country_dialog, null);
		ImageView ivCountry = (ImageView) dialogView.findViewById(R.id.iv_country);
		ivCountry.setImageBitmap(Utils.getAssetCountryFlag(this, getCurrentUser().getCountry()));
		AlertDialog dialog = DialogUtil.getAlertDialogBuilder(this).setTitle(R.string.confirm_user_country).setPositiveButton(R.string.ok, listener)
				.setNegativeButton(R.string.change_country, listener).setView(dialogView).create();
		dialog.show();
	}

	private static final int FLING_MIN_DISTANCE = 100;
	private static final int FLING_MIN_VELOCITY = 200;
	private int numView = 0;

	private void startCountryChooseActivity(int requestCode) {
		Intent intent = new Intent(this, EditUserCountryActivity.class);
		intent.putExtra(EditUserCountryActivity.PARAM_KEY_PAGE_FROM, DriftInformationActivity.class.getName());
		startActivityForResult(intent, requestCode);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			getCurrentUser().setCountry(data.getStringExtra(EditUserCountryActivity.RESULT_KEY_COUNTRY));
			RCThreadPool.getInstance().addTask(new Runnable() {

				@Override
				public void run() {
					PrefsUtils.User.saveUserInfo(getApplicationContext(), getCurrentUserRcId(), getCurrentUser());
					PhotoTalkDatabaseFactory.getDatabase().addFriend(PhotoTalkUtils.userToFriend(getCurrentUser()));
				}
			});
			if (requestCode == REQUEST_KEY_COUNTRY_CHOOSE_FISH_RANGE) {
				changeShowMode(DriftShowMode.MY_COUNTRY);
			} else if (requestCode == REQUEST_KEY_COUNTRY_CHOOSE_THROW) {
				throwInformation();
			}

		}
	}
}
