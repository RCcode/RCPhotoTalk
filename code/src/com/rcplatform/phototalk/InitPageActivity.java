package com.rcplatform.phototalk;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.utils.Contract;
import com.rcplatform.phototalk.views.PageIndicator;

public class InitPageActivity extends BaseActivity {

	private PageIndicator mPageIndicator;
	private final int REQUEST_CODE_LOGIN = 100;
	private final int GUIDE_PAGE_COUNT = 10;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.init_page);
		initView();
	}

	private void initView() {
		Button mLoginButton = (Button) findViewById(R.id.init_page_login_button);
		mLoginButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent loginIntent = new Intent(InitPageActivity.this, LoginActivity.class);
				loginIntent.putExtra(Contract.KEY_LOGIN_PAGE, true);
				startActivityForResult(loginIntent, REQUEST_CODE_LOGIN);
			}
		});
		Button mSignupButton = (Button) findViewById(R.id.init_page_signup_button);
		mSignupButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent loginIntent = new Intent(InitPageActivity.this, LoginActivity.class);
				loginIntent.putExtra(Contract.KEY_LOGIN_PAGE, false);
				startActivityForResult(loginIntent, REQUEST_CODE_LOGIN);
			}
		});
		ViewPager pager = (ViewPager) findViewById(R.id.intro_pager);
		pager.setAdapter(new IntroAdapter());
		mPageIndicator = (PageIndicator) findViewById(R.id.page_indicator_other);
		mPageIndicator.setDotCount(GUIDE_PAGE_COUNT);
		pager.setCurrentItem(0);
		pager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int arg0) {
				// TODO Auto-generated method stub
				setPageIndicator(arg0);
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	private void setPageIndicator(int position) {
		mPageIndicator.setActiveDot(position);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		// TODO Auto-generated method stub
		super.onNewIntent(intent);
		UserInfo userInfo = (UserInfo) intent.getSerializableExtra(PlatformEditActivity.PARAM_USER);
		getPhotoTalkApplication().setCurrentUser(userInfo);
		startActivity(AddFriendsActivity.class);
		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			UserInfo userInfo = (UserInfo) data.getSerializableExtra(LoginActivity.RESULT_KEY_USERINFO);
			getPhotoTalkApplication().setCurrentUser(userInfo);
			if (userInfo.getShowRecommends() == 1) {
				startActivity(HomeActivity.class);
			} else {
				startActivity(AddFriendsActivity.class);
			}
			finish();
		}
	}

	class IntroAdapter extends PagerAdapter {
		private List<View> views = new ArrayList<View>();

		public IntroAdapter() {
			// TODO Auto-generated constructor stub
			initPager();
		}

		private void initPager() {
			for (int i = 0; i < GUIDE_PAGE_COUNT; i++) {
				ImageView iv = new ImageView(InitPageActivity.this);
				iv.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
				iv.setScaleType(ScaleType.FIT_XY);
				iv.setImageResource(R.drawable.login_background);
				views.add(iv);
			}
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return views.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			// TODO Auto-generated method stub
			container.removeView(views.get(position));
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			// TODO Auto-generated method stub
			container.addView(views.get(position));
			return views.get(position);
		}
	}
}
