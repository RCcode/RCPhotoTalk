package com.rcplatform.phototalk.adapter;

import java.util.ArrayList;
import java.util.List;

import com.google.android.gms.internal.bi;
import com.rcplatform.phototalk.R;
import com.rcplatform.phototalk.EditUserCountryActivity.CountryHolder;
import com.rcplatform.phototalk.utils.Utils;
import com.rcplatform.phototalk.views.HeadImageView;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class SeachCountryAdapter extends BaseAdapter {
	Context context;
	private ViewHodler holder;
	private List<CountryHolder> listCountry = new ArrayList<CountryHolder>();

	public SeachCountryAdapter(Context context, List<CountryHolder> listCountry) {
		// TODO Auto-generated constructor stub
		this.listCountry = listCountry;
		this.context = context;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return listCountry.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		String flagName = listCountry.get(position).getCode();
		if (convertView == null) {
			convertView = LayoutInflater.from(context).inflate(
					R.layout.country_list_item, null);
			holder = new ViewHodler();
			holder.countryFlag = (ImageView) convertView
					.findViewById(R.id.country_flag_view);
			holder.countryName = (TextView) convertView
					.findViewById(R.id.country_name);
			holder.selectBtn = (Button) convertView
					.findViewById(R.id.select_btn);
			convertView.setTag(holder);
		} else {
			holder = (ViewHodler) convertView.getTag();
		}
		Bitmap bitmap = Utils.getAssetCountryFlag(context, flagName);
		if (bitmap != null) {
			holder.countryFlag.setImageBitmap(bitmap);
		}
		holder.countryName.setText(listCountry.get(position).getName());
		if (listCountry.get(position).isSelect()) {
			holder.selectBtn.setVisibility(View.VISIBLE);
		} else {
			holder.selectBtn.setVisibility(View.INVISIBLE);
		}

		return convertView;
	}

	class ViewHodler {
		ImageView countryFlag;
		TextView countryName;
		Button selectBtn;

		public ImageView getCountryFlag() {
			return countryFlag;
		}

		public void setCountryFlag(ImageView countryFlag) {
			this.countryFlag = countryFlag;
		}

		public TextView getCountryName() {
			return countryName;
		}

		public void setCountryName(TextView countryName) {
			this.countryName = countryName;
		}

		public Button getSelectBtn() {
			return selectBtn;
		}

		public void setSelectBtn(Button selectBtn) {
			this.selectBtn = selectBtn;
		}

	}
}
