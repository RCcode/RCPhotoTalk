package com.rcplatform.phototalk.views;

import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.HomeActivity;
import com.rcplatform.phototalk.MenueApplication;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.adapter.AppsAdapter;
import com.rcplatform.phototalk.api.MenueApiFactory;
import com.rcplatform.phototalk.api.MenueApiRecordType;
import com.rcplatform.phototalk.api.MenueApiUrl;
import com.rcplatform.phototalk.bean.DetailFriend;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.InfoRecord;
import com.rcplatform.phototalk.db.PhotoTalkDao;
import com.rcplatform.phototalk.galhttprequest.GalHttpRequest;
import com.rcplatform.phototalk.galhttprequest.GalHttpRequest.GalHttpLoadTextCallBack;
import com.rcplatform.phototalk.image.downloader.RCPlatformImageLoader;
import com.rcplatform.phototalk.utils.AppSelfInfo;
import com.rcplatform.phototalk.utils.Contract;

public class PVPopupWindow {

	public static PopupWindow show(final Context context, View v,
			DetailFriend detailFriend,final InfoRecord record) {
		View detailsView = LayoutInflater.from(context).inflate(
				R.layout.my_friend_details_layout, null, false);

		HorizontalListView appListView = (HorizontalListView) detailsView
				.findViewById(R.id.my_friend_details_apps_listview);
		AppsAdapter mAdapter = new AppsAdapter(context,
				detailFriend.getAppBeans());
		appListView.setAdapter(mAdapter);
		final PopupWindow popupWindow = new PopupWindow(detailsView,
				((Activity) context).getWindow().getWindowManager()
						.getDefaultDisplay().getWidth(), ((Activity) context)
						.getWindow().getWindowManager().getDefaultDisplay()
						.getHeight());

		popupWindow.setFocusable(true);
		popupWindow.setOutsideTouchable(true);
		popupWindow.setBackgroundDrawable(new BitmapDrawable());

		int[] location = new int[2];
		v.getLocationOnScreen(location);

		popupWindow.showAtLocation(v, Gravity.BOTTOM, location[0], location[1]
				- popupWindow.getHeight());

		// icon
		ImageView headIcon = (ImageView) detailsView
				.findViewById(R.id.friend_detail_head_portrait);
		RCPlatformImageLoader.loadImage(context, ImageLoader.getInstance(),
				detailFriend.getHeadUrl(),
				AppSelfInfo.ImageScaleInfo.thumbnailImageWidthPx, headIcon,
				R.drawable.default_head);
		// nick
		TextView textView = (TextView) detailsView
				.findViewById(R.id.friend_detail_nick);
		textView.setText(detailFriend.getNick());
		// action.

		Button actionBtn = (Button) detailsView
				.findViewById(R.id.friend_detail_edit_action);
		actionBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (popupWindow.isShowing()) {
					popupWindow.dismiss();
				}
				View editView = LayoutInflater.from(context).inflate(
						R.layout.my_friend_details_layout_edit, null, false);
				PopupWindow editNickPop = new PopupWindow(editView,
						((Activity) context).getWindow().getWindowManager()
								.getDefaultDisplay().getWidth(),
						((Activity) context).getWindow().getWindowManager()
								.getDefaultDisplay().getHeight());

				editNickPop.setFocusable(true);
				editNickPop.setOutsideTouchable(true);
				editNickPop.setBackgroundDrawable(new BitmapDrawable());

				int[] location = new int[2];
				v.getLocationOnScreen(location);

				editNickPop.showAtLocation(v, Gravity.BOTTOM, location[0],
						location[1] - editNickPop.getHeight());
			}
		});

		// tacotyId 是好友,隐藏它。
		TextView tacotyIdView = (TextView) detailsView
				.findViewById(R.id.friend_detail_tacoty);
		tacotyIdView.setText(detailFriend.getRcId());
		// mark
		TextView markView = (TextView) detailsView
				.findViewById(R.id.friend_detail_mark);
		markView.setText(detailFriend.getMark());
		// from source action.
		// 通过搜索添加的好友，需要判断对方是否在任何我的好友列表中（通讯录，facebook好友，其它应用好友，如在的话显示来源，如不在则不显示蓝框部分）
		TextView tvFrom = (TextView) detailsView
				.findViewById(R.id.friend_detail_from_contact_btn);
		// tvFrom.setText(detailFriend.getUserFrom());
		// forward.
		Button forwardBtn = (Button) detailsView
				.findViewById(R.id.friend_detail_forward_send_btn);
		forwardBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				context.startActivity(new Intent(context, HomeActivity.class));
			}
		});
		Button btnAdd = (Button) detailsView
				.findViewById(R.id.friend_detail_forward_add);
		btnAdd.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				addAsFriend(context, record);
			}
		});
		if (Friend.USER_STATUS_NOT_FRIEND == detailFriend.getStatus()) {
			actionBtn.setVisibility(View.GONE);
			forwardBtn.setVisibility(View.GONE);
			tvFrom.setVisibility(View.GONE);
		} else {
			btnAdd.setVisibility(View.GONE);
		}
		return popupWindow;
	}

	private static void addAsFriend(final Context context, final InfoRecord record) {
		GalHttpRequest request = GalHttpRequest.requestWithURL(context,
				MenueApiUrl.HOME_USER_NOTICE_ADD_FRIEND);
		// request.setPostValueForKey(MenueApiFactory.USER, email);
		request.setPostValueForKey(MenueApiFactory.TOKEN, MenueApplication
				.getUserInfoInstall(context).getToken());
		request.setPostValueForKey(MenueApiFactory.USERID, MenueApplication
				.getUserInfoInstall(context).getSuid());
		request.setPostValueForKey(MenueApiFactory.LANGUAGE, Locale
				.getDefault().getLanguage());
		request.setPostValueForKey(MenueApiFactory.DEVICE_ID,
				android.os.Build.DEVICE);
		request.setPostValueForKey(MenueApiFactory.APP_ID, Contract.APP_ID);

		request.setPostValueForKey(MenueApiFactory.RECORD_NOTICE_FRIEND_ID,
				record.getSender().getSuUserId());
		request.setPostValueForKey(
				MenueApiFactory.RECORD_NOTICE_NOTICE_NOTICEID,
				record.getNoticeId());
		request.setPostValueForKey(MenueApiFactory.RECORD_NOTICE_STATU,
				record.getStatu() + "");
		request.setPostValueForKey(MenueApiFactory.RECORD_NOTICE_TYPE,
				record.getType() + "");
		request.getPostData().toString();
		request.startAsynRequestString(new GalHttpLoadTextCallBack() {

			@Override
			public void textLoaded(String text) {
				record.setStatu(MenueApiRecordType.STATU_QEQUEST_ADDED);
				PhotoTalkDao.getInstance().updateRecordStatu(context,record);
			}

			@Override
			public void loadFail() {
				Log.i("AAA", "add friend fail");
			}
		});

	}
}
