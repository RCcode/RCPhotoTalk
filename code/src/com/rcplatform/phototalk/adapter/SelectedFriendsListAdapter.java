package com.rcplatform.phototalk.adapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.image.downloader.ImageOptionsFactory;
import com.rcplatform.phototalk.image.downloader.RCPlatformImageLoader;
import com.rcplatform.phototalk.utils.AppSelfInfo;

public class SelectedFriendsListAdapter extends BaseAdapter {

    private final List<Friend> data;

    private final Context context;

    private final String[] mLetters;

    private ViewHolder holder;

    private final ImageLoader mImageLoader;

    private final Map<Integer, Boolean> statu = new HashMap<Integer, Boolean>();

    private OnCheckBoxChangedListener mCheckBoxChangedListener;

    public interface OnCheckBoxChangedListener {

        void onChange(Friend friend, boolean isChecked);
    }

    public SelectedFriendsListAdapter(Context context, List<Friend> data) {
        this.data = data;
        this.context = context;
        mLetters = new String[data.size()];
        for (int i = 0; i < mLetters.length; i++) {
            mLetters[i] = data.get(i).getLetter();
        }
        this.mImageLoader = ImageLoader.getInstance();
        for (int i = 0; i < data.size(); i++) {
            statu.put(i, false);
        }
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
        Log.i("Futao", "statu =" + statu.size() + "position = " + position);
        Friend friend = data.get(position);
//        friend.setPostion(position);
        final int index = position;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.selected_friends_list_item, null);
            holder = new ViewHolder();
            holder.head = (ImageView) convertView.findViewById(R.id.iv_sfli_head);
            holder.name = (TextView) convertView.findViewById(R.id.tv_sfli_name);
            holder.checkBox = (CheckBox) convertView.findViewById(R.id.cb_sfli);
            holder.tvLetter = (TextView) convertView.findViewById(R.id.alpha);
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.checkBox.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                if (((CheckBox) v).isChecked()) {

                    statu.put(index, true);
                    mCheckBoxChangedListener.onChange(data.get(index), true);
                } else {
                    statu.put(index, false);
                    mCheckBoxChangedListener.onChange(data.get(index), false);
                }
            }
        });
        // holder.checkBox.setOnCheckedChangeListener(new
        // OnCheckedChangeListener() {
        //
        // @Override
        // public void onCheckedChanged(CompoundButton buttonView, boolean
        // isChecked) {
        // }
        // });
        holder.checkBox.setChecked(statu.get(position));
        RCPlatformImageLoader.loadImage(context, mImageLoader, ImageOptionsFactory.getPublishImageOptions(), friend.getHeadUrl(),
                                   AppSelfInfo.ImageScaleInfo.thumbnailImageWidthPx, holder.head, R.drawable.default_head);
        holder.name.setText(friend.getNick());
        String letter = friend.getLetter();
        if (!isNeedToShowLetter(position)) {
            holder.tvLetter.setVisibility(View.GONE);
        } else {
            holder.tvLetter.setVisibility(View.VISIBLE);
            holder.tvLetter.setText(letter);
        }

        return convertView;

    }

    private boolean isNeedToShowLetter(int position) {
        return position > 0 ? (!mLetters[position].equals(mLetters[position - 1])) : true;
    }

    class ViewHolder {

        ImageView head;

        TextView name;

        CheckBox checkBox;

        public TextView tvLetter;
    }

    public void setOnCheckBoxChangedListener(OnCheckBoxChangedListener mCheckBoxChangedListener) {
        this.mCheckBoxChangedListener = mCheckBoxChangedListener;
    }

    public Map<Integer, Boolean> getStatu() {
        return statu;
    }

    public List<Friend> getData() {
        return data;
    }
}
