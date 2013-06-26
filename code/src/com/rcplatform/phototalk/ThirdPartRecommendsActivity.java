package com.rcplatform.phototalk;

import java.util.List;

import android.content.Intent;
import android.os.Bundle;

import com.rcplatform.phototalk.activity.AddFriendBaseActivity;
import com.rcplatform.phototalk.thirdpart.bean.ThirdPartUser;
import com.rcplatform.phototalk.thirdpart.utils.OnAuthorizeSuccessListener;
import com.rcplatform.phototalk.thirdpart.utils.OnGetThirdPartInfoSuccessListener;
import com.rcplatform.phototalk.thirdpart.utils.ThirdPartClient;
import com.rcplatform.phototalk.thirdpart.utils.ThirdPartUtils;

public class ThirdPartRecommendsActivity extends AddFriendBaseActivity {
	private ThirdPartClient mClient;
	private int type;
	private boolean isTryLogin = false;

	public static final String PARAM_KEY_TYPE = "thirdparttype";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vk_recommends);
		init();
	}

	protected void onResume() {
		super.onResume();
		if (isNeedToAuthorize()) {
			authorize();
		}
	};
	private boolean isNeedToAuthorize(){
		return !mClient.isAuthorized() && !isTryLogin;
	}
	private void authorize(){
		mClient.authorize(new OnAuthorizeSuccessListener() {

			@Override
			public void onAuthorizeSuccess() {
				getThirdPartInformation();
			}
		});
	}
	@Override
	protected void onPause() {
		super.onPause();
		isTryLogin = false;
	}

	private void getThirdPartInformation() {
		mClient.getThirdPartInfo(new OnGetThirdPartInfoSuccessListener() {

			@Override
			public void onGetInfoSuccess(ThirdPartUser user, List<ThirdPartUser> friends) {

			}

			@Override
			public void onGetFail() {

			}
		});
	}

	private void init() {
		type = getIntent().getIntExtra(PARAM_KEY_TYPE, Integer.MAX_VALUE);
//		mClient = ThirdPartUtils.getThirdPartClient(this, type);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		isTryLogin = true;
		mClient.onAuthorizeInformationReceived(requestCode, resultCode, data);
	}
}
