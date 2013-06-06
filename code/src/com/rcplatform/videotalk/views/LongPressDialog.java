package com.rcplatform.videotalk.views;

import java.util.ArrayList;
import java.util.List;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.rcplatform.videotalk.R;

public class LongPressDialog extends Dialog {

    private final OnLongPressItemClickListener mListener;

    private final String[] mContents;

    private LinearLayout.LayoutParams mParams;

    private final List<TextView> mItems = new ArrayList<TextView>();

    private final LayoutInflater mInflater;

    private int listPostion;

    public interface OnLongPressItemClickListener {

        void onClick(int listPostion, int itemIndex);
    }

    public LongPressDialog(Context context, String[] contents, OnLongPressItemClickListener listener) {
        super(context);
        // TODO Auto-generated constructor stub
        mInflater = LayoutInflater.from(context);
        this.mContents = contents;
        this.mListener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.long_press_dialog);
        createItems();
    }

    private void createItems() {
        mParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout linearItems = (LinearLayout) findViewById(R.id.linear_items);
        for (int i = 0; i < mContents.length; i++) {
            String content = mContents[i];
            TextView item = getItem(content, i);
            mItems.add(item);
            linearItems.addView(item);
        }
    }

    private TextView getItem(String content, int index) {
        TextView tv = (TextView) mInflater.inflate(R.layout.long_press_item, null);
        tv.setLayoutParams(mParams);
        tv.setText(content);
        tv.setTag(index);
        tv.setOnClickListener(mOnClickListener);
        return tv;
    }

    private final android.view.View.OnClickListener mOnClickListener = new android.view.View.OnClickListener() {

        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub.
            int index = (Integer) v.getTag();
            if (mListener != null)
                mListener.onClick(listPostion, index);
        }
    };

    public void show(int postion, int... hidingItems) {
        this.listPostion = postion;
        show();
        for (TextView tv : mItems) {
            if (tv.getVisibility() == View.GONE)
                tv.setVisibility(View.VISIBLE);
        }
        for (int index : hidingItems) {
            mItems.get(index).setVisibility(View.GONE);
        }
    }

}
