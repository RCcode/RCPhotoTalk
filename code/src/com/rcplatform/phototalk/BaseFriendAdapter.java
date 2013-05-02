package com.rcplatform.phototalk;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.rcplatform.phototalk.bean.Friend;

/**
 * <br>
 * 基本适配器.
 * <p>
 * Copyright: Menue,Inc Copyright (c) 2013-3-4 下午04:49:05
 * <p>
 * Team:Menue Beijing
 * <p>
 * 
 * @author jelly.xiong@menue.com.cn
 * @version 1.0.0
 */
public abstract class BaseFriendAdapter<T extends Friend> extends BaseAdapter {

	protected Context mContext;

	private List<?> mFriendList;

	public BaseFriendAdapter(Context mContext, List<?> mFriendList) {
		super();
		this.mContext = mContext;
		this.mFriendList = mFriendList;
	}

	@Override
	public int getCount() {
		return mFriendList.size();
	}

	@Override
	public Object getItem(int position) {
		return mFriendList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view;
		if (convertView == null) {
			view = newView(mContext, mFriendList, parent);
		} else {
			view = convertView;
		}
		bindView(mContext, view, mFriendList, position);
		return view;
	}

	public abstract View newView(Context context, List<?> friendLists, ViewGroup parent);

	public abstract void bindView(Context context, View view, List<?> friendLists, int position);

}
