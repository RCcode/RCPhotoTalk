package com.rcplatform.phototalk;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.rcplatform.phototalk.adapter.SelectedFriendsGalleryAdapter;
import com.rcplatform.phototalk.adapter.SelectedFriendsListAdapter;
import com.rcplatform.phototalk.adapter.SelectedFriendsListAdapter.OnCheckBoxChangedListener;
import com.rcplatform.phototalk.api.MenueApiFactory;
import com.rcplatform.phototalk.api.MenueApiRecordType;
import com.rcplatform.phototalk.api.MenueApiUrl;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.FriendChat;
import com.rcplatform.phototalk.bean.InfoRecord;
import com.rcplatform.phototalk.bean.RecordUser;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.clienservice.PhotoCharRequestService;
import com.rcplatform.phototalk.galhttprequest.GalHttpRequest.GalHttpLoadTextCallBack;
import com.rcplatform.phototalk.galhttprequest.GalHttpRequest.PhotoChatHttpLoadTextCallBack;
import com.rcplatform.phototalk.utils.DialogUtil;
import com.rcplatform.phototalk.utils.DisplayUtil;
import com.rcplatform.phototalk.utils.PinyinComparator;

public class SelectFriendsActivity extends Activity implements OnClickListener {

    private ListView mFriendListView;

    private Gallery mGallery;

    private Button mButtonSend;

    // density 为1.5的手机上的px
    private int galleryLeftPaddingPx = 20;

    // density 为1.5的手机上的px
    private int gallerySpacePx = 25;

    private int mDisplayableCount;

    private UserInfo mUserInfo;

    private final int MSG_WHAT_ERROR = 100;

    private final int MSG_CACHE_FINISH = 200;

    private final int MSG_CACHE_FAIL = 300;

    private String tempFilePath;

    private MenueApplication app;

    private String timeLimit;

    private ProgressBar progressBar;

    private TextView mTvContentTitle;

    private ImageButton mBtBack;

    private TextView mBtAddFriend;

    private boolean isCached;

    private List<FriendChat> data;

    protected boolean needRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        app = (MenueApplication) getApplication();
        timeLimit = getIntent().getStringExtra("timeLimit");
        if (timeLimit == null) {
            timeLimit = "10";
        }
        setContentView(R.layout.select_friends_list_view);
        // 缓存要发送的图片
        catchBitampOnSDC(app.getEditeBitmap());
        // 初始化view 和 listener
        initViewOrListener();

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        progressBar.setVisibility(View.VISIBLE);
        loadFriends();
    }

    private void initViewOrListener() {
        mFriendListView = (ListView) findViewById(R.id.lv_sfl_friends);
        mGallery = (Gallery) findViewById(R.id.g_sfl_added_friends);
        mButtonSend = (Button) findViewById(R.id.btn_sfl_send);
        progressBar = (ProgressBar) findViewById(R.id.pb_select_friend);
        mTvContentTitle = (TextView) findViewById(R.id.titleContent);

        mBtBack = (ImageButton) findViewById(R.id.back);
        mGallery.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                final List<FriendChat> list = ((SelectedFriendsGalleryAdapter) mGallery.getAdapter()).getData();
                FriendChat friendChat = list.get(position);
                list.remove(friendChat);
                ((SelectedFriendsGalleryAdapter) mGallery.getAdapter()).notifyDataSetChanged();
                ((SelectedFriendsListAdapter) mFriendListView.getAdapter()).getStatu().put(friendChat.getPostion(), false);
                ((SelectedFriendsListAdapter) mFriendListView.getAdapter()).notifyDataSetChanged();
                mGallery.setNextFocusRightId(mGallery.getNextFocusLeftId());
            }
        });
        SelectedFriendsGalleryAdapter adapter = new SelectedFriendsGalleryAdapter(this, new ArrayList<FriendChat>());
        mGallery.setAdapter(adapter);
        alignGalleryToLeft(mGallery);

        mButtonSend.setOnClickListener(this);
        mTvContentTitle.setVisibility(View.VISIBLE);
        mTvContentTitle.setText(R.string.select_friend_title);

        mBtBack.setVisibility(View.VISIBLE);
        mBtBack.setBackgroundResource(R.drawable.base_back_arrow);
        mBtBack.setOnClickListener(this);

        mBtAddFriend = (TextView) findViewById(R.id.choosebutton);
        mBtAddFriend.setVisibility(View.GONE);
        mBtAddFriend.setBackgroundResource(R.drawable.select_add);
        mBtAddFriend.setOnClickListener(this);
    }

    private void catchBitampOnSDC(final Bitmap bitmap) {
        // 创建一个临时的隐藏文件夹
        new Thread(new Runnable() {

            @Override
            public void run() {
                File file = new File(app.getSendFileCachePath(), "/" + System.currentTimeMillis() + ".jpg");
                FileOutputStream os = null;
                try {

                    if (!file.exists())
                        file.createNewFile();

                    os = openFileOutput(file.getName(), MODE_WORLD_WRITEABLE);
                    tempFilePath = file.getName();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
                    os.flush();
                    os.close();
                    isCached = true;
                    sendStringMessage(MSG_CACHE_FINISH, "");
                    Log.i("MENUE", "cache " + tempFilePath);
                }
                catch (Exception e) {
                    isCached = true;
                    sendStringMessage(MSG_CACHE_FINISH, e.getMessage());
                    e.printStackTrace();
                }
                finally {
                    if (os != null)
                        try {
                            os.close();
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                }
            }
        }).start();
    }

    private void loadFriends() {
        PhotoCharRequestService.getInstence().postRequest(this, mLoadFriendCallBack, null, MenueApiUrl.GET_FRIENDS_URL);
    }

    private final GalHttpLoadTextCallBack mLoadFriendCallBack = new GalHttpLoadTextCallBack() {

        @Override
        public void textLoaded(String text) {
            try {
                List<FriendChat> friends = jsonToFriends(text);
                if (friends != null && friends.size() > 0) {
                    data = friends;
                    if (isCached) {
                        initFriendListView(data);
                        data = null;
                        needRefresh = false;
                    } else {
                        needRefresh = true;
                    }
                }
            }
            catch (JSONException e) {
                e.printStackTrace();
                sendStringMessage(MSG_WHAT_ERROR, getString(R.string.receive_data_error));
            }

        }

        @Override
        public void loadFail() {
            sendStringMessage(MSG_WHAT_ERROR, getString(R.string.net_error));
        }
    };

    private void initFriendListView(List<FriendChat> list) {
        progressBar.setVisibility(View.GONE);
        SelectedFriendsListAdapter adapter = (SelectedFriendsListAdapter) mFriendListView.getAdapter();
        if (adapter == null) {
            initFriendListAdapter(list);
        } else {
            if (adapter.getData().size() != list.size()) {
                initFriendListAdapter(list);
                Map<Integer, Boolean> statu = ((SelectedFriendsListAdapter) mFriendListView.getAdapter()).getStatu();
                // 获取已经选择了的联系人
                List<FriendChat> selectedFriends = ((SelectedFriendsGalleryAdapter) mGallery.getAdapter()).getData();
                for (Friend friend : selectedFriends) {
                    for (int i = 0; i < list.size(); i++) {
                        Friend chat = list.get(i);
                        if (chat.getSuid().equals(friend.getSuid())) {
                            statu.put(i, true);
                        }
                    }
                }
            }
        }
    }

    private void initFriendListAdapter(List<FriendChat> list) {
        SelectedFriendsListAdapter adapter = new SelectedFriendsListAdapter(this, list);
        mFriendListView.setAdapter(adapter);
        adapter.setOnCheckBoxChangedListener(new OnCheckBoxChangedListener() {

            @Override
            public void onChange(FriendChat friend, boolean isChecked) {
                List<FriendChat> list = ((SelectedFriendsGalleryAdapter) mGallery.getAdapter()).getData();
                if (isChecked) {
                    if (!list.contains(friend))
                        list.add(friend);
                } else
                    list.remove(friend);

                ((SelectedFriendsGalleryAdapter) mGallery.getAdapter()).notifyDataSetChanged();
                if (list.size() > mDisplayableCount)
                    mGallery.setSelection(list.size() - mDisplayableCount);
                else {
                    mGallery.setSelection(0);
                }
            }
        });
    }

    private List<FriendChat> jsonToFriends(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);
        if (isRequestStatusOK(jsonObject)) {
            JSONArray myFriendsArray = jsonObject.getJSONArray("myUsers");
            Gson gson = new Gson();
            List<FriendChat> friends = gson.fromJson(myFriendsArray.toString(), new com.google.gson.reflect.TypeToken<ArrayList<FriendChat>>() {
            }.getType());
            TreeSet<FriendChat> fs = new TreeSet<FriendChat>(new PinyinComparator());
            fs.addAll(friends);
            friends.clear();
            friends.addAll(fs);
            fs.clear();
            return friends;
        } else {
            sendStringMessage(MSG_WHAT_ERROR, jsonObject.getString(MenueApiFactory.RESPONSE_KEY_MESSAGE));
            return null;
        }
    }

    private void sendStringMessage(int what, String content) {
        Message msg = mHandler.obtainMessage();
        msg.what = what;
        msg.obj = content;
        mHandler.sendMessage(msg);
    }

    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_WHAT_ERROR:
                    progressBar.setVisibility(View.GONE);
                    DialogUtil.showToast(getApplicationContext(), (String) msg.obj, Toast.LENGTH_SHORT);
                    break;

                case MSG_CACHE_FINISH:
                    if (needRefresh) {
                        initFriendListView(data);
                        data = null;
                        needRefresh = false;
                    }
                    break;

                case MSG_CACHE_FAIL:
                    if (needRefresh) {
                        initFriendListView(data);
                        data = null;
                        needRefresh = false;
                    }
                    DialogUtil.showToast(getApplicationContext(), (String) msg.obj, Toast.LENGTH_SHORT);
                    break;
            }
        };
    };

    private long timeSnap;

    private void sendPicture(String desc, String imagePath, int timeLimit, List<FriendChat> friends) {
        timeSnap = System.currentTimeMillis();
        Map<String, String> params = new HashMap<String, String>();
        params.put(MenueApiFactory.COUNTRY, Locale.getDefault().getCountry());
        params.put(MenueApiFactory.HEAD_URL, MenueApplication.getUserInfoInstall(this).getHeadUrl());
        params.put(MenueApiFactory.TIME, String.valueOf(timeSnap));
        params.put(MenueApiFactory.NICK, MenueApplication.getUserInfoInstall(this).getNick());
        params.put(MenueApiFactory.IMAGE_TYPE, "jpg");
        params.put(MenueApiFactory.DESC, desc);
        params.put(MenueApiFactory.TIME_LIMIT, this.timeLimit);
        params.put(MenueApiFactory.USER_ARRAY, buildUserArray(friends, timeSnap));
        params.put(MenueApiFactory.FILE, imagePath);
        PhotoCharRequestService.getInstence().postRequestByTimestamp(this, new PhotoChatHttpLoadTextCallBack() {

            @Override
            public void textLoaded(String text, long time) {
                HomeActivity activity = (HomeActivity) app.getActivity(HomeActivity.class.getName());
                if (!activity.isFinishing()) {
                    activity.callBackForSend(time, text);
                }
            }

            @Override
            public void loadFail(long time) {
                HomeActivity activity = (HomeActivity) app.getActivity(HomeActivity.class.getName());
                if (!activity.isFinishing()) {
                    activity.callBackForSend(time, "");
                }
                sendStringMessage(MSG_WHAT_ERROR, getString(R.string.net_error));
            }
        }, params, MenueApiUrl.SEND_PICTURE_URL, timeSnap, null);

    }

    private boolean isRequestStatusOK(JSONObject jsonObject) throws JSONException {
        return jsonObject.getInt(MenueApiFactory.RESPONSE_KEY_STATUS) == MenueApiFactory.RESPONSE_STATE_SUCCESS;
    }

    private String buildUserArray(List<FriendChat> friends, long time) {
        try {
            JSONArray array = new JSONArray();
            List<InfoRecord> infoRecords = new ArrayList<InfoRecord>();
            InfoRecord record;
            for (Friend f : friends) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("userId", f.getSuid());
                jsonObject.put("headUrl", f.getHeadUrl());
                jsonObject.put("nick", f.getNick());
                array.put(jsonObject);

                record = new InfoRecord();
                record.setRecordId(record.hashCode() + "");
                record.setNoticeId(MenueApiFactory.ERROR_NOTICE);
                record.setCreatetime(time);
                RecordUser user = new RecordUser();
                user.setSuUserId(MenueApplication.getUserInfoInstall(this).getSuid());
                record.setSender(user);
                user = new RecordUser();
                user.setSuUserId(f.getSuid());
                user.setNick(f.getNick());
                user.setHeadUrl(f.getHeadUrl());
                record.setReceiver(user);
                record.setUrl(tempFilePath);
                record.setLimitTime(Integer.parseInt(this.timeLimit));
                record.setType(MenueApiRecordType.TYPE_PICTURE_OR_VIDEO);
                record.setStatu(MenueApiRecordType.STATU_NOTICE_SENDING);
                infoRecords.add(record);

            }
            app.addSendRecords(time, infoRecords);
            // Log.i("MENUE", "add send records" + time + " //" + timeSnap);
            return array.toString();
        }
        catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.btn_sfl_send:
                List<FriendChat> data = ((SelectedFriendsGalleryAdapter) mGallery.getAdapter()).getData();
                if (data == null || data.size() <= 0) {
                    Toast.makeText(SelectFriendsActivity.this, R.string.please_select_contact, 1).show();
                    return;
                } else {
                    sendPicture("123", tempFilePath, 10, data);
                    Intent intent = new Intent(SelectFriendsActivity.this, HomeActivity.class);
                    intent.putExtra("from", this.getClass().getName());
                    intent.putExtra("time", this.timeSnap);
                    startActivity(intent);
                }
                break;
            case R.id.back:
                app.deleteSendFileCache(tempFilePath);
                finish();
                break;

            case R.id.choosebutton:
                startActivity(new Intent(SelectFriendsActivity.this, AddFriendActivity.class));
                break;
        }
    }

    private void alignGalleryToLeft(Gallery gallery) {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        measureView(mButtonSend, params);

        int rightMargin = ((MarginLayoutParams) mButtonSend.getLayoutParams()).rightMargin;
        int leftMargin = ((MarginLayoutParams) mButtonSend.getLayoutParams()).leftMargin;

        int gallerLeftPaddingDip = DisplayUtil.px2dip(galleryLeftPaddingPx, 1.5f);
        int gallerySpaceDip = DisplayUtil.px2dip(gallerySpacePx, 1.5f);

        galleryLeftPaddingPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, gallerLeftPaddingDip, metrics);
        gallerySpacePx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, gallerySpaceDip, metrics);

        mGallery.setSpacing(gallerySpacePx);
        int w = getResources().getDisplayMetrics().widthPixels - mButtonSend.getMeasuredWidth() - rightMargin - leftMargin - galleryLeftPaddingPx;

        View itemView = LayoutInflater.from(this).inflate(R.layout.selected_friends_galleryt_item, null);
        measureView(itemView, params);

        int itemWidth = itemView.getMeasuredWidth();

        mDisplayableCount = (w) / (itemWidth + gallerySpacePx);

        MarginLayoutParams layoutParams = (MarginLayoutParams) mGallery.getLayoutParams();
        layoutParams.setMargins(-(w - itemWidth), layoutParams.topMargin, layoutParams.rightMargin, layoutParams.bottomMargin);
    }

    public void measureView(View child, ViewGroup.LayoutParams params) {
        ViewGroup.LayoutParams p = child.getLayoutParams();
        if (p == null) {
            p = params;
        }
        int width = p.width;
        if (width > 0) {
            width = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        } else {
            width = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }

        child.measure(width, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //
            app.deleteSendFileCache(tempFilePath);
        }
        return super.onKeyDown(keyCode, event);
    }
}
