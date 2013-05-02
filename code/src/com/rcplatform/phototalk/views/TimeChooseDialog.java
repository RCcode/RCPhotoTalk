package com.rcplatform.phototalk.views;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.views.wheel.OnWheelClickedListener;
import com.rcplatform.phototalk.views.wheel.WheelView;
import com.rcplatform.phototalk.views.wheel.adapter.AbstractWheelTextAdapter;

public class TimeChooseDialog {
	private Context mContext;
	private String[] mTimes;
	private PopupWindow mPopupWindow;
	private WheelView mWheel;

	public TimeChooseDialog(Context context, String[] times) {
		// TODO Auto-generated constructor stub
		this.mContext = context;
		this.mTimes = times;
		init();
	}

	private void init() {
		// TODO Auto-generated method stub
		View view = LayoutInflater.from(mContext).inflate(
				R.layout.time_choose_dialog, null);
		mWheel = (WheelView) view.findViewById(R.id.wv_hours);
		mWheel.addClickingListener(new OnWheelClickedListener() {

			@Override
			public void onItemClicked(WheelView wheel, int itemIndex) {
				// TODO Auto-generated method stub
				if (itemIndex == getCurrentItem())
					mPopupWindow.dismiss();
				else
					mWheel.setCurrentItem(itemIndex);
			}
		});
		TimeChooseAdapter adapter = new TimeChooseAdapter(mContext, mTimes);
		adapter.setTextSize(20);
		mWheel.setVisibleItems(4);
		mWheel.setViewAdapter(adapter);
		mPopupWindow = new PopupWindow(mContext);
		mPopupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
		mPopupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
		mPopupWindow.setContentView(view);
		mPopupWindow.setAnimationStyle(R.style.dialogWindowAnim);
		mPopupWindow.setTouchable(true);
		mPopupWindow.setFocusable(true);
		mPopupWindow.setOutsideTouchable(true);

	}

	public void setSelection(int selected) {
		mWheel.setCurrentItem(selected);
	}

	public int getCurrentItem() {
		return mWheel.getCurrentItem();
	}

	private class TimeChooseAdapter extends AbstractWheelTextAdapter {

		private String[] mTimes;

		protected TimeChooseAdapter(Context context, String[] timeArray) {
			super(context);
			// TODO Auto-generated constructor stub
			this.mTimes = timeArray;
		}

		@Override
		public int getItemsCount() {
			// TODO Auto-generated method stub
			return mTimes.length;
		}

		@Override
		protected CharSequence getItemText(int index) {
			// TODO Auto-generated method stub
			return mTimes[index];
		}

	}

	public void showTimeChooseDialog(View targetView) {
		mPopupWindow.showAtLocation(targetView, Gravity.BOTTOM, 0, 0);
	}

	public void dismissTimeChooseDialog() {
		if (mPopupWindow.isShowing())
			mPopupWindow.dismiss();
	}

	public void setOnDissmissListener(OnDissmissListener listener) {
		mPopupWindow.setOnDismissListener(listener);
	}

	public static abstract class OnDissmissListener implements
			PopupWindow.OnDismissListener {

		private TimeChooseDialog dialog;

		public OnDissmissListener(TimeChooseDialog dialog) {
			// TODO Auto-generated constructor stub
			this.dialog = dialog;
		}

		@Override
		public void onDismiss() {
			// TODO Auto-generated method stub
			onDismiss(dialog.getCurrentItem());
		}

		public abstract void onDismiss(int lastSelectItem);
	}
}
