package com.rcplatform.phototalk;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
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
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.InformationState;
import com.rcplatform.phototalk.bean.InformationType;
import com.rcplatform.phototalk.bean.RecordUser;
import com.rcplatform.phototalk.logic.LogicUtils;
import com.rcplatform.phototalk.utils.ZipUtil;
import com.rcplatform.phototalk.views.AudioRecordButton;
import com.rcplatform.phototalk.views.AudioRecordButton.OnRecordingListener;
import com.rcplatform.phototalk.views.AudioShowView;
import com.rcplatform.phototalk.views.ColorPicker.OnColorChangeListener;
import com.rcplatform.phototalk.views.LongClickShowView.Builder;
import com.rcplatform.phototalk.views.ColorPickerDialog;
import com.rcplatform.phototalk.views.EditPictureView;
import com.rcplatform.phototalk.views.EditableViewGroup;
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

	private PhotoTalkApplication app;

	private ColorPickerDialog colorPickerDialog;

	private int softInputHight;

	private boolean enableSave = true;

	private WheelView mWheel;

	private AudioRecordButton audioBtn;

	private String voicePath;

	private boolean isSave = false;

	private Friend friend = null;

	private RelativeLayout select_layout;

	private MediaPlayer player;

	private boolean isPlayer = false;

	private RelativeLayout tooShortLayout;

	private RelativeLayout recordDisplayLayout;

	private TextView tvVoiceRecordSecond;
	private EditText editText;
	private ImageView voice_volume_bg;
	private ImageView iv_voice_volume;
	private ImageView voice_volume_gride;

	private Handler voiceRecordHandler = new Handler() {
		private boolean isRecording = false;
		private Integer recordLast;

		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case AudioRecordButton.AUDIO_RECORD_TOO_SHORT:
				tooShortLayout.setVisibility(View.VISIBLE);
				voiceRecordHandler
						.sendEmptyMessageDelayed(
								AudioRecordButton.AUDIO_RECORD_TOO_SHORT_SHOW_END,
								1000);
				break;
			case AudioRecordButton.AUDIO_RECORD_TOO_SHORT_SHOW_END:
				tooShortLayout.setVisibility(View.GONE);
				break;
			case AudioRecordButton.AUDIO_RECORD_START:
				recordDisplayLayout.setVisibility(View.VISIBLE);
				isRecording = true;
				recordLast = (Integer) msg.obj;
				voiceRecordHandler
						.sendEmptyMessage(AudioRecordButton.AUDIO_RECORDING);
				break;

			case AudioRecordButton.AUDIO_RECORDING:
				if (isRecording) {
					tvVoiceRecordSecond.setText(recordLast.toString() + "s");
					voiceRecordHandler.sendEmptyMessageDelayed(
							AudioRecordButton.AUDIO_RECORDING, 1000);
				}
				recordLast -= 1;
				break;

			case AudioRecordButton.AUDIO_RECORD_END:
				recordDisplayLayout.setVisibility(View.GONE);
				isRecording = false;
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.edit_picture_view2);
		tooShortLayout = (RelativeLayout) findViewById(R.id.layout_voice_record_too_short);
		recordDisplayLayout = (RelativeLayout) findViewById(R.id.layout_voice_record);
		voice_volume_bg = (ImageView) findViewById(R.id.voice_volume_bg);
		iv_voice_volume = (ImageView) findViewById(R.id.iv_voice_volume);
		voice_volume_gride = (ImageView) findViewById(R.id.voice_volume_gride);

		tvVoiceRecordSecond = (TextView) findViewById(R.id.tv_record_second);
		friend = (Friend) getIntent().getSerializableExtra("friend");
		audioBtn = (AudioRecordButton) findViewById(R.id.audioBtn);
		audioBtn.setVoiceHandler(voiceRecordHandler);
		audioBtn.setOnRecordingListener(new OnRecordingListener() {

			@Override
			public void onRecording(int recordedSecord, int amplitude) {
				// TODO Auto-generated method stub
				int height = voice_volume_bg.getHeight();
				if (height > 0) {
					RelativeLayout.LayoutParams layoutParams = (android.widget.RelativeLayout.LayoutParams) iv_voice_volume.getLayoutParams();
					if (amplitude > 22000) {
						iv_voice_volume
								.setBackgroundResource(R.drawable.voice_volume_full);
						layoutParams.height = LayoutParams.WRAP_CONTENT;
						voice_volume_gride.setVisibility(View.INVISIBLE);
					} else {
						iv_voice_volume
								.setBackgroundResource(R.drawable.voice_volume);
						voice_volume_gride.setVisibility(View.VISIBLE);
						if (amplitude > 17000) {
							layoutParams.height = 55;
						} else if (amplitude > 6000 && amplitude < 17000) {
							layoutParams.height = 35;
						} else if (amplitude < 6000) {
							layoutParams.height = 20;
						}
						iv_voice_volume.setLayoutParams(layoutParams);
					}
				}

			}

			@Override
			public void endRecord(String savePath, int n) {
				// TODO Auto-generated method stub
				voicePath = savePath;
				audioBtn.setVisibility(4);
				make_voice.setVisibility(0);
				voice_size.setText(n + "s");

			}
		});

		mEditableViewGroup = (EditableViewGroup) findViewById(R.id.edit_group);
		mEditableViewGroup.setDrawingCacheEnabled(true);
		app = (PhotoTalkApplication) getApplication();
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

						player = new MediaPlayer();
						FileInputStream fis = new FileInputStream(voicePath);
						player.setDataSource(fis.getFD());
						player.setAudioStreamType(AudioManager.STREAM_RING);
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
					saveEditedPictrue(mEditableViewGroup.getDrawingCache(),
							app.getCameraPath());

				}
				break;
			case SEND_ON_CLICK:
				mEditableViewGroup.setDrawingCacheEnabled(true);
				mEditableViewGroup.buildDrawingCache();
				isSave = true;
				saveEditedPictrue(mEditableViewGroup.getDrawingCache(),
						app.getSendFileCachePath() + "/Photochat.jpg");
				if (friend == null) {
					startSelectFriendActivity();
				} else {

					Intent intent = new Intent(EditPictureActivity.this,
							HomeActivity.class);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent);
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
			endplayer.setAudioStreamType(AudioManager.STREAM_RING);
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
				editText = (EditText) mEditText
						.findViewById(R.id.et_editText_view);
				final Paint paint = editText.getPaint();
				editText.setFocusable(true);
				editText.setOnFocusChangeListener(new OnFocusChangeListener() {

					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						// TODO Auto-generated method stub
						// editText.setFocusable(true);
						if (!hasFocus) {
							if (editText.getText() == null
									|| editText.getText().length() == 0) {
								mEditText.setVisibility(View.GONE);
								mEditText = null;
							}
						}
					}
				});
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
						if (length == 0) {
							mEditText.setVisibility(View.GONE);
							mEditText = null;
						}
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

	// private PopupWindow mPopuTimeLimit;

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
		mWheel.setCurrentItem(timers.length - 1);
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

	public void saveEditedPictrue(final Bitmap bitmap, final String path) {
		// showDialog();
		new Thread(new Runnable() {

			@Override
			public void run() {
				File file = new File(path);
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

	Handler handler = new Handler() {

		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case SAVE_SUCCESS:
				// setSaveable(false);
				if (isSave) {
					if (friend != null) {
						send();
					}
				} else {
					//保存成功后 刷新本地相册
					EditPictureActivity.this.getBaseContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://"+ Environment.getExternalStorageDirectory())));
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

		try {
			tempPath = app.getSendZipFileCachePath() + "/"
					+ System.currentTimeMillis() + ".zip";
			ZipUtil.ZipFolder(app.getSendFileCachePath(), tempPath);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String timelimit = (String) mButtonTimeLimit.getText();
		File file = new File(tempPath);
		if (file.exists()) {
			// 压缩成功后删除录音和照片文件
			deleteTemp();
			sendPicture("", tempPath, timelimit, friend);
		}
	}

	private void sendPicture(final String desc, String imagePath,
			final String timeLimit, Friend friend) {
		File file = new File(imagePath);
		List<Friend> friends = new ArrayList<Friend>();
		friends.add(friend);
		LogicUtils.sendPhoto(this, timeLimit, friends, file);
	}

	// private String buildUserArray(Friend friend, long time, String timeLimit)
	// {
	// try {
	// JSONArray array = new JSONArray();
	// List<Information> infoRecords = new ArrayList<Information>();
	// Information record;
	// JSONObject jsonObject = new JSONObject();
	// jsonObject.put("userId", friend.getRcId());
	// array.put(jsonObject);
	// record = new Information();
	// record.setCreatetime(time);
	// RecordUser user = new RecordUser();
	// record.setSender(user);
	// user = new RecordUser();
	// user.setNick(friend.getNickName());
	// user.setHeadUrl(friend.getHeadUrl());
	// record.setReceiver(user);
	// record.setUrl(tempFilePath);
	// record.setLimitTime(Integer.parseInt(timeLimit));
	// record.setType(InformationType.TYPE_PICTURE_OR_VIDEO);
	// record.setStatu(InformationState.PhotoInformationState.STATU_NOTICE_SENDING);
	// infoRecords.add(record);
	// app.addSendRecords(time, infoRecords);
	// return array.toString();
	// } catch (JSONException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// return null;
	// }
}
