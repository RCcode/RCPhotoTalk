package com.rcplatform.phototalk;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.api.MenueApiFactory;
import com.rcplatform.phototalk.api.RCPlatformResponseHandler;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.InformationState;
import com.rcplatform.phototalk.bean.InformationType;
import com.rcplatform.phototalk.bean.RecordUser;
import com.rcplatform.phototalk.proxy.FriendsProxy;
import com.rcplatform.phototalk.utils.PrefsUtils;
import com.rcplatform.phototalk.utils.ZipUtil;
import com.rcplatform.phototalk.utils.Contract.Action;
import com.rcplatform.phototalk.views.AudioRecordButton;
import com.rcplatform.phototalk.views.AudioRecordButton.OnRecordingListener;
import com.rcplatform.phototalk.views.AudioShowView;
import com.rcplatform.phototalk.views.ColorPicker.OnColorChangeListener;
import com.rcplatform.phototalk.views.ColorPickerDialog;
import com.rcplatform.phototalk.views.EditPictureView;
import com.rcplatform.phototalk.views.EditableViewGroup;
import com.rcplatform.phototalk.views.TimeChooseDialog;
import com.rcplatform.phototalk.views.wheel.OnWheelClickedListener;
import com.rcplatform.phototalk.views.wheel.WheelView;
import com.rcplatform.phototalk.views.wheel.adapter.AbstractWheelTextAdapter;

public class EditPictureActivity extends BaseActivity {

	private static final int UNDO_ON_CLICK = 0;
	private static final int PLAY_VOICE = 1;
	private static final int DELETE_VOICE = 2;
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

	private LinearLayout make_voice;

	private TextView voice_size;
	private Button play_voice;
	private Button delete_voice;

	private ImageView mButtonTuya;

	private Button mButtonTimeLimit;

	private Button mButtonSend;

	private Button mButtonSave;

	private Button mButtonClose;

	private EditableViewGroup mEditableViewGroup;

	private LinearLayout mEditText;

	private MenueApplication app;

	private ColorPickerDialog colorPickerDialog;

	private int softInputHight;

	private String tempFilePath;

	// private Dialog waitDialog;

	private boolean enableSave = true;
	private WheelView mWheel;
	private AudioRecordButton audioBtn;
	private String voicePath;
	private boolean isSave = false;
	private Friend friend = null;
	private RelativeLayout select_layout;
	private MediaPlayer player;
	private boolean isPlayer = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.edit_picture_view2);
		friend = (Friend) getIntent().getSerializableExtra("friend");
		audioBtn = (AudioRecordButton) findViewById(R.id.audioBtn);
		AudioShowView view = new AudioShowView(this);
		audioBtn.setAttentionView(getWindowManager(), view,
				new OnRecordingListener() {

					@Override
					public void onRecording(int recordedSecord, int amplitude) {
						// TODO Auto-generated method stub

					}

					@Override
					public void endRecord(String savePath, int n) {
						// TODO Auto-generated method stub
						voicePath = savePath;
						audioBtn.setVisibility(4);
						make_voice.setVisibility(0);
						voice_size.setText(n - 1 + "s");

					}
				});

		mEditableViewGroup = (EditableViewGroup) findViewById(R.id.edit_group);
		mEditableViewGroup.setDrawingCacheEnabled(true);
		app = (MenueApplication) getApplication();
		audioBtn.setSavePath(app.getSendFileCachePath());
		// mEditePicView = (EditPictureView) findViewById(R.id.sf_edite_pic);
		mEditePicView = new EditPictureView(this);
		mEditableViewGroup.addView(mEditePicView, LayoutParams.FILL_PARENT,
				LayoutParams.FILL_PARENT);

		make_voice = (LinearLayout) findViewById(R.id.make_voice);
		voice_size = (TextView) findViewById(R.id.voice_size);
		play_voice = (Button) findViewById(R.id.play_voice);
		play_voice.setTag(PLAY_VOICE);
		play_voice.setOnClickListener(clickListener);
		delete_voice = (Button) findViewById(R.id.delete_voice);
		delete_voice.setTag(DELETE_VOICE);
		delete_voice.setOnClickListener(clickListener);

		mButtonUndo = (Button) findViewById(R.id.btn_edit_pic_undo);
		mButtonTuya = (ImageView) findViewById(R.id.btn_edit_pic_tuya);
		mButtonTimeLimit = (Button) findViewById(R.id.btn_edit_pic_timelimit);
		mButtonSend = (Button) findViewById(R.id.btn_edit_pic_send);
		mButtonSave = (Button) findViewById(R.id.btn_edit_pic_save);
		mButtonClose = (Button) findViewById(R.id.btn_edit_pic_close);
		mButtonUndo.setVisibility(View.GONE);
		select_layout = (RelativeLayout) findViewById(R.id.select_layout);
		mWheel = (WheelView) findViewById(R.id.wv_hours);
		select_layout.setVisibility(View.GONE);
		mWheel.addClickingListener(new OnWheelClickedListener() {

			@Override
			public void onItemClicked(WheelView wheel, int itemIndex) {
				// TODO Auto-generated method stub
				if (itemIndex == getCurrentItem()) {
					handler.obtainMessage(SET_LIMIT, itemIndex + 1)
							.sendToTarget();
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
		mButtonUndo.setOnClickListener(clickListener);
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

			case PLAY_VOICE:

				if (player == null) {
					try {
						File file = new File(voicePath);
						System.out.println("---voicePath---->" + voicePath);
						if (file.exists()) {
							System.out.println("asdasd录音文件存在" + file.length()
									/ 1024 + "kb");
						} else {
							System.out.println("asdasd录音文件不存在");

						}

						player = new MediaPlayer();
						player.setDataSource(voicePath);
						player.prepare();
						player.setOnCompletionListener(new OnCompletionListener() {

							@Override
							public void onCompletion(MediaPlayer mp) {
								// TODO Auto-generated method stub
								player.release();
								player = null;
								isPlayer = false;
								playEndMusic();
							}
						});
					} catch (Exception e) {
						// TODO: handle exception
					}
				}

				if (!isPlayer) {
					player.start();
					play_voice.setBackgroundResource(R.drawable.voice_pause);
					isPlayer = true;
				} else {
					player.pause();
					play_voice.setBackgroundResource(R.drawable.play_voice);
					isPlayer = false;
				}

				break;
			case DELETE_VOICE:
				File file = new File(voicePath);
				if (file.exists()) {
					file.delete();
				}
				audioBtn.setVisibility(0);
				make_voice.setVisibility(4);
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
				isSave = true;
				saveEditedPictrue(mEditableViewGroup.getDrawingCache());
				// mEditableViewGroup.setDrawingCacheEnabled(true);
				// mEditableViewGroup.buildDrawingCache();
				// app.setEditeBitmap(mEditableViewGroup.getDrawingCache());
				// // 点击发送后实时保存
				// // mEditableViewGroup.setDrawingCacheEnabled(true);
				// // mEditableViewGroup.buildDrawingCache();
				// // saveEditedPictrue(mEditableViewGroup.getDrawingCache());
				if (friend == null) {
					startSelectFriendActivity();
				}
				break;
			case CLOSE_ON_CLICK:
				mEditePicView.recyle();
				finish();
				break;
			}
		}
	};

	private void playEndMusic() {
		MediaPlayer endplayer = new MediaPlayer();
		try {
			AssetFileDescriptor fileDescriptor = this.getAssets().openFd(
					"end.mp3");
			endplayer
					.setDataSource(fileDescriptor.getFileDescriptor(),
							fileDescriptor.getStartOffset(),
							fileDescriptor.getLength());
			endplayer.prepare();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		endplayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				// TODO Auto-generated method stub
				try {
					mp.release();
					play_voice.setBackgroundResource(R.drawable.play_voice);
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
		});
		endplayer.start();
	}

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
		final String timers[] = new String[] { "1", "2", "3", "4", "5", "6",
				"7", "8", "9", "10" };
		TimeChooseAdapter adapter = new TimeChooseAdapter(this, timers);
		adapter.setTextColor(Color.WHITE);
		DisplayMetrics dm = new DisplayMetrics();
		dm = this.getResources().getDisplayMetrics();
		int screenWidth = dm.widthPixels;
		int screenHeight = dm.heightPixels;
		if (screenWidth > 480 && screenHeight > 800) {
			adapter.setTextSize(22);
		} else {
			adapter.setTextSize(19);
		}
		mWheel.setVisibleItems(3);
		mWheel.setViewAdapter(adapter);
		select_layout.setVisibility(View.VISIBLE);

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
		// showDialog();
		new Thread(new Runnable() {

			@Override
			public void run() {
				// if (!Environment.getExternalStorageState().equals(
				// Environment.MEDIA_MOUNTED)) {
				// handler.sendEmptyMessage(NO_SDC);
				// return;
				// }
				tempFilePath = app.getSendFileCachePath() + "/Photochat.jpg";
				File file = new File(tempFilePath);
				try {
					if (file.exists()) {
						file.delete();
					}
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

	// public void showDialog() {
	//
	// if (waitDialog == null) {
	// waitDialog = new Dialog(this, R.style.waiting_dialog);
	// waitDialog.setContentView(R.layout.reader_progress_wait_view);
	// waitDialog.getWindow().setBackgroundDrawableResource(
	// R.color.TRANSPARENT);
	// waitDialog.setCancelable(false);
	// }
	// waitDialog.show();
	// }

	Handler handler = new Handler() {

		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case SAVE_SUCCESS:
				// if (waitDialog != null && waitDialog.isShowing())
				// waitDialog.hide();
				setSaveable(false);
				if (isSave) {
					if (friend != null) {
						send();
					}
				} else {
					Toast.makeText(EditPictureActivity.this,
							R.string.save_success, Toast.LENGTH_SHORT).show();
				}
				break;
			case SAVE_FAIL:
				// if (waitDialog != null && waitDialog.isShowing())
				// waitDialog.hide();
				Toast.makeText(EditPictureActivity.this, R.string.save_fail,
						Toast.LENGTH_SHORT).show();
				break;
			case NO_SDC:
				// if (waitDialog != null && waitDialog.isShowing())
				// waitDialog.hide();
				Toast.makeText(EditPictureActivity.this, R.string.no_sdc,
						Toast.LENGTH_SHORT).show();
				break;
			case SET_LIMIT:
				int n = (Integer) msg.obj;
				mButtonTimeLimit.setText(n + "");
				mEditePicView.setTimeLimit(n);
				audioBtn.setMaxRecoedSize(n);
				select_layout.setVisibility(View.GONE);
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

	public void send() {
		String tempPath = null;
		File file = new File(app.getSendFileCachePath() + ".zip");
		try {
			if (file.exists()) {
				file.delete();
			}
			ZipUtil.ZipFolder(app.getSendFileCachePath(),
					app.getSendFileCachePath() + ".zip");
			tempPath = app.getSendFileCachePath() + ".zip";
		} catch (Exception e) {
			e.printStackTrace();
		}
		String timelimit = (String) mButtonTimeLimit.getText();
		sendPicture("", tempPath, timelimit, friend);
	}

	private void sendPicture(final String desc, String imagePath,
			final String timeLimit, final Friend friend) {
		Long timeSnap = System.currentTimeMillis();

		final File file = new File(imagePath);
		FriendsProxy.postZip(EditPictureActivity.this, file,
				new RCPlatformResponseHandler() {

					@Override
					public void onSuccess(int statusCode, String content) {
						// TODO Auto-generated method stub
					}

					@Override
					public void onFailure(int errorCode, String content) {
						// TODO Auto-generated method stub
					}
				}, String.valueOf(timeSnap), desc, timeLimit,
				buildUserArray(friend, timeSnap, timeLimit));
		Intent intent = new Intent(this, HomeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
		this.finish();
	}

	private String buildUserArray(Friend friend, long time, String timeLimit) {
		try {
			JSONArray array = new JSONArray();
			List<Information> infoRecords = new ArrayList<Information>();
			Information record;
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("userId", friend.getSuid());
			array.put(jsonObject);

			record = new Information();
			record.setRecordId(record.hashCode() + "");
			record.setCreatetime(time);
			RecordUser user = new RecordUser();
			record.setSender(user);
			user = new RecordUser();
			user.setNick(friend.getNick());
			user.setHeadUrl(friend.getHeadUrl());
			record.setReceiver(user);
			record.setUrl(tempFilePath);
			record.setLimitTime(Integer.parseInt(timeLimit));
			record.setType(InformationType.TYPE_PICTURE_OR_VIDEO);
			record.setStatu(InformationState.STATU_NOTICE_SENDING);
			infoRecords.add(record);
			app.addSendRecords(time, infoRecords);
			return array.toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
