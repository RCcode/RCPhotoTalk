package com.rcplatform.phototalk.views;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ListView;

public class SnapListView extends ListView {

	private boolean willShowSnap = false;

	private boolean showSnap = false;


	private float startTapPointY = 0;

	private final long TAP_LONG_TIME = 1l;

	private final float TAP_POINT_MAX_Y_LEN = 10;

	private SnapShowListener snapListener = null;

	private final int SHOW_SNAP_FLAG = 1;

	private final Handler snapHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			switch (msg.what) {
				case SHOW_SNAP_FLAG:
					if (null != snapListener) {
						snapListener.snapShow();
					}
					break;
			}
		}
	};

	private Timer snapTimer;

	public SnapListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public SnapListView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void setSnapListener(SnapShowListener listener) {
		this.snapListener = listener;
	}
	public boolean onTouchEvent(MotionEvent event) {
		super.onTouchEvent(event);
		final int action = event.getAction();
		switch (action) {
			case MotionEvent.ACTION_DOWN:
				if (!willShowSnap) {
					willShowSnap = true;
					startTapPointY = event.getY();
					if (null != snapTimer) {
						snapTimer.cancel();
					}
					snapTimer = new Timer();
					TimerTask sanpTask = new TimerTask() {

						public void run() {
							showSnap = true;
							Message message = new Message();
							message.what = SHOW_SNAP_FLAG;
							snapHandler.sendMessage(message);
						}
					};

					snapTimer.schedule(sanpTask, TAP_LONG_TIME);
				}

				Log.e("snap list", "down");
				break;

			case MotionEvent.ACTION_MOVE:
				Log.e("snap list", "move");
				float currentY = event.getY();
				float len = currentY - startTapPointY;
				if ((len > TAP_POINT_MAX_Y_LEN || len < -TAP_POINT_MAX_Y_LEN) && !showSnap) {
					willShowSnap = false;
					if (null != snapTimer) {
						snapTimer.cancel();
					}
				}
				break;

			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_CANCEL:
				Log.e("snap list", "up");
				willShowSnap = false;
				showSnap = false;
				snapListener.snapHide();
				if (null != snapTimer) {
					snapTimer.cancel();
				}
				Log.e("snap list", "cancel");
				break;
			default:
				break;
		}
		return true;
	}

}
