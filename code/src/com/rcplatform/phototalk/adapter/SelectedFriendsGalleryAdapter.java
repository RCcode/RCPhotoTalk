package com.rcplatform.phototalk.adapter;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.FriendChat;
import com.rcplatform.phototalk.image.downloader.ImageOptionsFactory;
import com.rcplatform.phototalk.image.downloader.RCPlatformImageLoader;
import com.rcplatform.phototalk.utils.AppSelfInfo;

public class SelectedFriendsGalleryAdapter extends BaseAdapter {

    private final List<FriendChat> data;

    private final Context context;

    private ViewHolder holder;

    private final ImageLoader mImageLoader;

    private OnGalleryItemClickListener clickListener;

    public interface OnGalleryItemClickListener {

        void onClick(int positon);
    }

    public SelectedFriendsGalleryAdapter(Context context, List<FriendChat> data) {
        this.data = data;
        this.context = context;
        this.mImageLoader = ImageLoader.getInstance();
    }

    @Override
    public int getCount() {
        return data != null ? data.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return (data != null && data.size() > 0) ? data.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.i("MENUE", "GET VIEW" + System.currentTimeMillis());
        Friend friend = data.get(position);
        final int index = position;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.selected_friends_galleryt_item, null);
            holder = new ViewHolder();
            holder.head = (ImageView) convertView.findViewById(R.id.iv_sfgi_head);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        //
        // holder.head.setOnClickListener(new OnClickListener() {
        //
        // @Override
        // public void onClick(View v) {
        // Toast.makeText(context, "conclick", 1).show();
        // }
        // });
        RCPlatformImageLoader.loadImage(context, mImageLoader, ImageOptionsFactory.getPublishImageOptions(), friend.getHeadUrl(),
                                   AppSelfInfo.ImageScaleInfo.thumbnailImageWidthPx, holder.head, R.drawable.default_head);
        return convertView;
    }

    class ViewHolder {

        ImageView head;

    }

    public List<FriendChat> getData() {
        return data;
    }

    public void setOnGalleryItemClickListener(OnGalleryItemClickListener clickListener) {
        this.clickListener = clickListener;
    }

}
