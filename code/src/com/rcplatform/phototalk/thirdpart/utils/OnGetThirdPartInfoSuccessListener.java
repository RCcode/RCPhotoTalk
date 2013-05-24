package com.rcplatform.phototalk.thirdpart.utils;

import java.util.List;

import com.rcplatform.phototalk.thirdpart.bean.ThirdPartUser;

public interface OnGetThirdPartInfoSuccessListener {
	public void onGetInfoSuccess(ThirdPartUser user, List<ThirdPartUser> friends);

	public void onGetFail();
}
