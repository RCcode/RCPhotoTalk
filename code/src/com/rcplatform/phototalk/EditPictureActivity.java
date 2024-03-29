package com.rcplatform.phototalk;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.rcplatform.phototalk.views.ColorPicker.OnColorChangeListener;
import com.rcplatform.phototalk.views.ColorPickerDialog;
import com.rcplatform.phototalk.views.EditPictureView;
import com.rcplatform.phototalk.views.EditableViewGroup;
import com.rcplatform.phototalk.views.TimeChooseDialog;
import com.rcplatform.phototalk.views.wheel.OnWheelClickedListener;
import com.rcplatform.phototalk.views.wheel.WheelView;
import com.rcplatform.phototalk.views.wheel.adapter.AbstractWheelTextAdapter;

/**
 * 标题、简要说明. <br>
 * 类详细说明.
 * <p>
 * Copyright: Menue,Inc Copyright (c) 2013-2-27 上午11:36:04
 * <p>
 * Team:Menue Beijing
 * <p>
 * 
 * @author tao.fu@menue.com.cn
 * @version 1.0.0
 */
public class EditPictureActivity extends Activity {

	private static final int UNDO_ON_CLICK = 0;

	private static final int REDO_ON_CLICK = 1;

	private static final int ADDTEXT_ON_CLICK = 2;

	private static final int TUYA_ON_CLICK = 3;

	private static final int TIMELIMIT_ON_CLICK = 4;

	private static final int SAVE_PICTURE_ON_CLICK = 5;

	private static final int SEND_ON_CLICK = 6;

	private static final int CLOSE_ON_CLICK = 7;

	private static final int SAVE_SUCCESS = 100;

	private static final int SAVE_FAIL = 200;

	private static final int NO_SDC = 300;
	private static final int SET_LIMIT = 400;

	private EditPictureView mEditePicView;

	private Button mButtonUndo;

	// private Button mButtonRedo;

	// private Button mButtonAddText;

	private ImageView mButtonTuya;

	private Button mButtonTimeLimit;

	private Button mButtonSend;

	private Button mButtonSave;

	private Button mButtonClose;

	private EditableViewGroup mEditableViewGroup;

	private LinearLayout mEditText;

	private boolean isShowTimeLimit;

	private MenueApplication app;

	private TimeChooseDialog timeChooseDialog;

	private ColorPickerDialog colorPickerDialog;

	private int softInputHight;

	private String tempFilePath;

	private Dialog waitDialog;

	private boolean enableSave = true;
	private WheelView mWheel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.edit_picture_view2);
		mEditableViewGroup = (EditableViewGroup) findViewById(R.id.edit_group);
		mEditableViewGroup.setDrawingCacheEnabled(true);
		app = (MenueApplication) getApplication();
		// mEditePicView = (EditPictureView) findViewById(R.id.sf_edite_pic);
		mEditePicView = new EditPictureView(this);
		mEditableViewGroup.addView(mEditePicView, LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);
		// mButtonRedo = (Button) findViewById(R.id.btn_edit_pic_redo);
		mButtonUndo = (Button) findViewById(R.id.btn_edit_pic_undo);
		// mButtonAddText = (Button) findViewById(R.id.btn_edit_pic_addText);
		mButtonTuya = (ImageView) findViewById(R.id.btn_edit_pic_tuya);
		mButtonTimeLimit = (Button) findViewById(R.id.btn_edit_pic_timelimit);
		mButtonSend = (Button) findViewById(R.id.btn_edit_pic_send);
		mButtonSave = (Button) findViewById(R.id.btn_edit_pic_save);
		mButtonClose = (Button) findViewById(R.id.btn_edit_pic_close);
		mButtonUndo.setVisibility(View.GONE);
		mWheel = (WheelView) findViewById(R.id.wv_hours);
		mWheel.setVisibility(View.GONE);
		mWheel.addClickingListener(new OnWheelClickedListener() {

			@Override
			public void onItemClicked(WheelView wheel, int itemIndex) {
				// TODO Auto-generated method stub
				if (itemIndex == getCurrentItem()) {
					handler.obtainMessage(SET_LIMIT,itemIndex+1).sendToTarget();
				} else {
					mWheel.setCurrentItem(itemIndex);
				}
			}
		});
		mButtonUndo.setTag(UNDO_ON_CLICK);
		mButtonTimeLimit.setText("10");
		mButtonTuya.setTag(TUYA_ON_CLICK);
		mButtonTimeLimit.setTag(TIMELIMIT_ON_CLICK);
		mButtonSave.setTag(SAVE_PICTURE_ON_CLICK);
		mButtonSend.setTag(SEND_ON_CLICK);
		mButtonClose.setTag(CLOSE_ON_CLICK);
		// mButtonRedo.setOnClickListener(clickListener);
		mButtonUndo.setOnClickListener(clickListener);
		// mButtonAddText.setOnClickListener(clickListener);
		mButtonTuya.setOnClickListener(clickListener);
		mButtonTimeLimit.setOnClickListener(clickListener);
		mButtonSave.setOnClickListener(clickListener);
		mButtonSend.setOnClickListener(clickListener);
		mButtonClose.setOnClickListener(clickListener);
		mEditableViewGroup.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {

					@Override
					public void onGlobalLayout() {
						// TODO Auto-generated method stub
						Rect r = new Rect();
						mEditableViewGroup.getWindowVisibleDisplayFrame(r);

						int screenHeight = mEditableViewGroup.getRootView()
								.getHeight();
						softInputHight = screenHeight - (r.bottom - r.top);
						if (softInputHight != 0) {

							mEditableViewGroup.setPopupSoftInput(true);
							mEditableViewGroup
									.updateTextViewLoation(screenHeight
											- softInputHight);
						} else {
							mEditableViewGroup.setPopupSoftInput(false);
							if (mEditText != null
									&& (mEditText.getVisibility() == View.VISIBLE)) {
								mEditText.getChildAt(0)
										.setFocusableInTouchMode(false);
								mEditText.getChildAt(0).setFocusable(false);
								mEditText.getChildAt(0).clearFocus();
							}
						}
						// boolean visible = heightDiff > screenHeight / 3;
					}
				});
	}

	private final OnClickListener clickListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			int tag = (Integer) v.getTag();
			switch (tag) {
			case UNDO_ON_CLICK:
				setSaveable(true);
				mEditePicView.undo();
				break;
			case TUYA_ON_CLICK:

				if (mEditePicView.openOrCloseTuya()) {
					mButtonTuya.setBackgroundResource(R.drawable.scrawl_press);
					showColorPickerDialog();
					mEditableViewGroup.setTuyaMode(true);
					mButtonUndo.setVisibility(View.VISIBLE);
					setSaveable(true);
				} else {
					mButtonTuya.setBackgroundResource(R.drawable.scrawl_normal);
					mButtonUndo.setVisibility(View.GONE);
					mEditableViewGroup.setTuyaMode(false);
					if (colorPickerDialog.isShowing())
						colorPickerDialog.dismiss();
				}

				break;
			case TIMELIMIT_ON_CLICK:
				showTimeLimitView();
				break;

			case SAVE_PICTURE_ON_CLICK:
				if (enableSave) {
					mEditableViewGroup.setDrawingCacheEnabled(true);
					mEditableViewGroup.buildDrawingCache();
					saveEditedPictrue(mEditableViewGroup.getDrawingCache());
				}
				break;
			case SEND_ON_CLICK:
				mEditableViewGroup.setDrawingCacheEnabled(true);
				mEditableViewGroup.buildDrawingCache();
				app.setEditeBitmap(mEditableViewGroup.getDrawingCache());
				startSelectFriendActivity();
				break;
			case CLOSE_ON_CLICK:
				mEditePicView.recyle();
				finish();
				break;
			}
		}
	};

	private void startSelectFriendActivity() {
		Intent intent = new Intent(this, SelectFriendsActivity.class);
		intent.putExtra("timeLimit", mButtonTimeLimit.getText().toString());
		startActivity(intent);
	}

	@Override
	public boolean onTouchEvent(android.view.MotionEvent event) {

		int action = event.getAction();
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if (mEditText == null) {
				mEditText = (LinearLayout) LayoutInflater.from(
						EditPictureActivity.this).inflate(
						R.layout.edittext_view, null);
				EditText editText = (EditText) mEditText
						.findViewById(R.id.et_editText_view);
				final Paint paint = editText.getPaint();
				editText.addTextChangedListener(new TextWatcher() {

					@Override
					public void onTextChanged(CharSequence s, int start,
							int before, int count) {
						setSaveable(true);
					}

					@Override
					public void beforeTextChanged(CharSequence s, int start,
							int count, int after) {

					}

					@Override
					public void afterTextChanged(Editable s) {
						float length = paint.measureText(s.toString());
						if (length > app.getScreenWidth()) {
							s.delete(s.length() - 1, s.length());
						}
					}
				});
				mEditableViewGroup.addEditeTextView(mEditText);
				setSaveable(true);
			}
			break;

		default:
			break;
		}

		return super.onTouchEvent(event);
	};

	protected void showColorPickerDialog() {
		if (colorPickerDialog == null) {
			colorPickerDialog = new ColorPickerDialog(this);
			colorPickerDialog
					.setOnColorChangeListener(new OnColorChangeListener() {

						@Override
						public void onColorChange(int color) {
							mEditePicView.setColor(color);
							Log.i("ABC", "COLOR" + color);
						}
					});
		}
		colorPickerDialog.showDialog(mButtonTuya);
	}

	private PopupWindow mPopuTimeLimit;

	private void showTimeLimitView() {
		// if (timeChooseDialog == null) {
		final String timers[] = new String[] { "1s", "2s", "3s", "4s", "5s", "6s",
				"7s", "8s", "9s", "10s" };
		TimeChooseAdapter adapter = new TimeChooseAdapter(this, timers);
		adapter.setTextSize(20);
		mWheel.setVisibleItems(3);
		mWheel.setViewAdapter(adapter);
		mWheel.setVisibility(View.VISIBLE);
		// timeChooseDialog = new TimeChooseDialog(this, timers);
		// timeChooseDialog.showTimeChooseDialog(mEditableViewGroup);
		// timeChooseDialog.setOnDissmissListener(new
		// TimeChooseDialog.OnDissmissListener(timeChooseDialog) {
		//
		// @Override
		// public void onDismiss(int lastSelectItem) {
		// mButtonTimeLimit.setText(timers[lastSelectItem]);
		// mEditePicView.setTimeLimit(timers[lastSelectItem]);
		// }
		// });
		// }
		// timeChooseDialog.showTimeChooseDialog(mEditableViewGroup);

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
	
	public int getCurrentItem() {
		return mWheel.getCurrentItem();
	}
	public void saveEditedPictrue(final Bitmap bitmap) {
		showDialog();
		new Thread(new Runnable() {

			@Override
			public void run() {
				if (!Environment.getExternalStorageState().equals(
						Environment.MEDIA_MOUNTED)) {
					handler.sendEmptyMessage(NO_SDC);
					return;
				}
				tempFilePath = app.getCacheFilePath() + "/Photochat_"
						+ System.currentTimeMillis() + ".jpg";
				File file = new File(tempFilePath);
				try {

					if (!file.exists())
						file.createNewFile();
					BufferedOutputStream os = new BufferedOutputStream(
							new FileOutputStream(file)); //
					// b.compress(Bitmap.CompressFormat.JPEG, 100, os);
					bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
					os.flush();
					os.close();
					handler.sendEmptyMessage(SAVE_SUCCESS);
				} catch (Exception e) {
					handler.sendEmptyMessage(NO_SDC);
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void showDialog() {

		if (waitDialog == null) {
			waitDialog = new Dialog(this, R.style.waiting_dialog);
			waitDialog.setContentView(R.layout.reader_progress_wait_view);
			waitDialog.getWindow().setBackgroundDrawableResource(
					R.color.TRANSPARENT);
			waitDialog.setCancelable(false);
		}
		waitDialog.show();
	}

	Handler handler = new Handler() {

		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case SAVE_SUCCESS:
				if (waitDialog != null && waitDialog.isShowing())
					waitDialog.hide();
				setSaveable(false);
				Toast.makeText(EditPictureActivity.this, R.string.save_success,
						Toast.LENGTH_SHORT).show();
				break;
			case SAVE_FAIL:
				if (waitDialog != null && waitDialog.isShowing())
					waitDialog.hide();
				Toast.makeText(EditPictureActivity.this, R.string.save_fail,
						Toast.LENGTH_SHORT).show();
				break;
			case NO_SDC:
				if (waitDialog != null && waitDialog.isShowing())
					waitDialog.hide();
				Toast.makeText(EditPictureActivity.this, R.string.no_sdc,
						Toast.LENGTH_SHORT).show();
				break;
			case SET_LIMIT:
				int n = (Integer) msg.obj;
				mButtonTimeLimit.setText(n+"");
				mEditePicView.setTimeLimit(n);
				mWheel.setVisibility(View.GONE);
				break;
			}

		};
	};

	@Override
	protected void onDestroy() {
		mEditePicView.recyle();
		super.onDestroy();
	}

	public void setSaveable(boolean enableSave) {
		this.enableSave = enableSave;
		if (enableSave) {
			mButtonSave.setBackgroundResource(R.drawable.save_normal);
		} else {
			mButtonSave.setBackgroundResource(R.drawable.save_press);
		}
	}

	public boolean getSaveable() {
		return this.enableSave;
	}
}
