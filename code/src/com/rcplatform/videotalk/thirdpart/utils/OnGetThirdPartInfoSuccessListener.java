package com.rcplatform.videotalk.thirdpart.utils;

import java.util.List;

import com.rcplatform.videotalk.thirdpart.bean.ThirdPartUser;

public interface OnGetThirdPartInfoSuccessListener {
	public void onGetInfoSuccess(ThirdPartUser user, List<ThirdPartUser> friends);

	public void onGetFail();
}
