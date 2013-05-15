package com.rcplatform.phototalk;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.adapter.FriendDynamicListAdpter;
import com.rcplatform.phototalk.views.HeadImageView;

public class FriendDynamicActivity extends BaseActivity {
	private ListView friendDynameicList;
	// 好友动态列表使用此adpter
	private FriendDynamicListAdpter adpter;
	private ImageButton back_btn;
	private TextView titleContent;
	private PopupWindow firendMsgPop;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.friend_dynamic);
		back_btn = (ImageButton) findViewById(R.id.back);
		back_btn.setVisibility(View.VISIBLE);
		titleContent = (TextView) findViewById(R.id.titleContent);
		titleContent.setText(R.string.friend_dynamic);
		titleContent.setVisibility(View.VISIBLE);
		friendDynameicList = (ListView) findViewById(R.id.friend_dynamic_list);
		friendDynameicList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
//				弹出popup 显示好友信息缺少添加参数 和设置信息
//				showPop()
			}
		});

	}

	public void showPop(View view) {
		if (firendMsgPop != null && firendMsgPop.isShowing()) {
			firendMsgPop.dismiss();
		}

		View detailsView = LayoutInflater.from(this).inflate(
				R.layout.friend_dynamic_pop, null, false);
		firendMsgPop = new PopupWindow(detailsView, getWindow()
				.getWindowManager().getDefaultDisplay().getWidth(),
				((Activity) this).getWindow().getWindowManager()
						.getDefaultDisplay().getHeight());
		firendMsgPop.setFocusable(true);
		firendMsgPop.setOutsideTouchable(true);
		HeadImageView headView = (HeadImageView) findViewById(R.id.friend_head);
		// 昵称
		TextView friend_nick = (TextView) detailsView
				.findViewById(R.id.friend_nick);
		// Rc Id
		TextView friend_rc_id = (TextView) detailsView
				.findViewById(R.id.friend_rc_id);

		Button friend_photo = (Button) detailsView
				.findViewById(R.id.friend_photo);
		friend_photo.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (firendMsgPop.isShowing()) {
					firendMsgPop.dismiss();
				}
			}
		});
		Button add_or_send = (Button) detailsView
				.findViewById(R.id.add_or_send);
		add_or_send.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (firendMsgPop.isShowing()) {
					firendMsgPop.dismiss();
				}
			}
		});
		firendMsgPop.showAtLocation(view, Gravity.BOTTOM, 0, 0);
	}
}
