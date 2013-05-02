package com.rcplatform.phototalk.listener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.assist.ImageLoadingListener;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.api.MenueApiFactory;
import com.rcplatform.phototalk.api.MenueApiRecordType;
import com.rcplatform.phototalk.api.MenueApiUrl;
import com.rcplatform.phototalk.bean.InfoRecord;
import com.rcplatform.phototalk.bean.ServiceSimpleNotice;
import com.rcplatform.phototalk.clienservice.PhotoCharRequestService;
import com.rcplatform.phototalk.db.PhotoTalkDao;
import com.rcplatform.phototalk.utils.Utils;

public class HomeRecordLoadPicListener implements ImageLoadingListener {

    private final ListView listView;

    private ProgressBar bar;

    private TextView statu;

    private final Context context;

    private final InfoRecord record;

    public HomeRecordLoadPicListener(ListView listView, ProgressBar bar, TextView textView, Context context, InfoRecord record) {
        super();
        this.listView = listView;
        this.bar = bar;
        this.statu = textView;
        this.context = context;
        this.record = record;
    }

    @Override
    public void onLoadingStarted(String imageUri, View view) {
        record.setStatu(MenueApiRecordType.STATU_NOTICE_LOADING);
        updateView(View.VISIBLE, context.getResources().getString(R.string.home_record_pic_loading));
    }

    @Override
    public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
        record.setStatu(MenueApiRecordType.STATU_NOTICE_LOAD_FAIL);
        PhotoTalkDao.getInstance().updateRecordStatu(context,record);
        updateView(View.GONE, context.getResources().getString(R.string.home_record_load_fail));
    }

    @Override
    public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
        record.setStatu(MenueApiRecordType.STATU_NOTICE_DELIVERED_OR_LOADED);
        record.setLastUpdateTime(System.currentTimeMillis());
        // 把本地数据的这条记录改为2
        PhotoTalkDao.getInstance().updateRecordStatu(context,record);
        // 通知服务器该表状态
        notifyServer(context, record);
        String text = Utils.getStatuTime(context.getResources().getString(R.string.statu_received),
                                         context.getResources().getString(R.string.statu_press_to_show), record.getLastUpdateTime());
        updateView(View.GONE, text);
    }

    @Override
    public void onLoadingCancelled(String imageUri, View view) {

    }

    private static void notifyServer(Context context, InfoRecord record) {
        Gson gson = new Gson();
        ServiceSimpleNotice notice = new ServiceSimpleNotice(record.getStatu() + "", record.getRecordId() + "", record.getType() + "");
        List<ServiceSimpleNotice> list = new ArrayList<ServiceSimpleNotice>();
        list.add(notice);
        String s = gson.toJson(list, new TypeToken<List<ServiceSimpleNotice>>() {
        }.getType());

        Map<String, String> params = new HashMap<String, String>();
        params.put(MenueApiFactory.NOTICES, s);
        // 此处之所以不传callback 是因为不管能不能通知成功服务器，本地的数据都回改变状态，
        PhotoCharRequestService.getInstence().postRequest(context, null, params, MenueApiUrl.HOME_USER_NOTICE_CHANGE);

    }

    private void updateView(int visibitity, String text) {
        if (bar == null) {
            if (listView != null) {
                bar = (ProgressBar) listView.findViewWithTag(record.getRecordId() + ProgressBar.class.getName());
            }
        }
        if (bar != null) {
            String barTag = (String) bar.getTag();
            if (barTag.equals(record.getRecordId() + ProgressBar.class.getName())) {
                bar.setVisibility(visibitity);
            }
        }

        if (statu == null) {
            if (listView != null) {
                statu = (TextView) listView.findViewWithTag(record.getRecordId() + TextView.class.getName());
            }
        }

        if (statu != null) {
            String statuTag = (String) statu.getTag();
            if (statuTag.equals(record.getRecordId() + TextView.class.getName())) {
                statu.setText(text);
            }
        }
    }
}
