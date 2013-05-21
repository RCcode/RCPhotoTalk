package com.rcplatform.phototalk;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ViewFlipper;

import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.utils.Constants;
import com.rcplatform.phototalk.views.PageIndicator;

public class InitPageActivity extends BaseActivity implements
		OnGestureListener, OnTouchListener {

	private PageIndicator mPageIndicator;
	public static final int REQUEST_CODE_LOGIN = 100;
	private final int GUIDE_PAGE_COUNT = 10;
	private AlphaAnimation inAnimation_Alpha;
	private AlphaAnimation outAnimation_Alpha;
	private GestureDetector mGestureDetector;
	private static final int FLING_MIN_DISTANCE = 100;
	private static final int FLING_MIN_VELOCITY = 200;
	private int numView = 0;
	private ViewFlipper pager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.init_page);
		initView();
	}

	private void initView() {

		mGestureDetector = new GestureDetector(this);
		mGestureDetector.setIsLongpressEnabled(true);
		inAnimation_Alpha = new AlphaAnimation(0.0f, 1.0f);
		inAnimation_Alpha.setDuration(100);
		outAnimation_Alpha = new AlphaAnimation(1.0f, 0.0f);
		outAnimation_Alpha.setDuration(100);

		Button mLoginButton = (Button) findViewById(R.id.init_page_login_button);
		mLoginButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				Intent loginIntent = new Intent(InitPageActivity.this,
						LoginActivity.class);
				loginIntent.putExtra(Constants.KEY_LOGIN_PAGE, true);
				startActivityForResult(loginIntent, REQUEST_CODE_LOGIN);
			}
		});
		Button mSignupButton = (Button) findViewById(R.id.init_page_signup_button);
		mSignupButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent loginIntent = new Intent(InitPageActivity.this,
						LoginActivity.class);
				loginIntent.putExtra(Constants.KEY_LOGIN_PAGE, false);
				startActivityForResult(loginIntent, REQUEST_CODE_LOGIN);
			}
		});
		pager = (ViewFlipper) findViewById(R.id.intro_pager);
		pager.setOnTouchListener(this);
		pager.setLongClickable(true);
		// pager.setAdapter(new IntroAdapter());
		mPageIndicator = (PageIndicator) findViewById(R.id.page_indicator_other);
		mPageIndicator.setDotCount(pager.getChildCount());
		// pager.setCurrentItem(0);
		// pager.setOnPageChangeListener(new OnPageChangeListener() {
		//
		// @Override
		// public void onPageSelected(int arg0) {
		// setPageIndicator(arg0);
		// }
		//
		// @Override
		// public void onPageScrolled(int arg0, float arg1, int arg2) {
		// }
		//
		// @Override
		// public void onPageScrollStateChanged(int arg0) {
		// }
		// });
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		startActivity(AddFriendsActivity.class);
		finish();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			UserInfo userInfo = (UserInfo) data
					.getSerializableExtra(LoginActivity.RESULT_KEY_USERINFO);
			if (userInfo.getShowRecommends() == 1) {
				startActivity(HomeActivity.class);
			} else {
				startActivity(AddFriendsActivity.class);
			}
			finish();
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return mGestureDetector.onTouchEvent(event);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE
				&& Math.abs(velocityX) > FLING_MIN_VELOCITY) {
			pager.setInAnimation(inAnimation_Alpha);
			pager.setOutAnimation(outAnimation_Alpha);
			if (numView < pager.getChildCount()-1) {
				numView++;
				onCheckMain(numView);
			}
		} else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE
				&& Math.abs(velocityX) > FLING_MIN_VELOCITY) {
			pager.setInAnimation(inAnimation_Alpha);
			pager.setOutAnimation(outAnimation_Alpha);
			if (numView > 0) {
				numView--;
				onCheckMain(numView);
			}
		}
//		上下滑动判断
//		if (e1.getY() - e2.getY() > FLING_MIN_DISTANCE
//				&& Math.abs(velocityY) > FLING_MIN_VELOCITY) {
//		} else if (e2.getY() - e1.getY() > FLING_MIN_DISTANCE
//				&& Math.abs(velocityY) > FLING_MIN_VELOCITY) {
//		}
		
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}
	public void onCheckMain(int n) {
		pager.setDisplayedChild(n);
		mPageIndicator.setActiveDot(n);
	}
	// class IntroAdapter extends PagerAdapter {
	// private List<View> views = new ArrayList<View>();
	//
	// public IntroAdapter() {
	// initPager();
	// }
	//
	// private void initPager() {
	// for (int i = 0; i < GUIDE_PAGE_COUNT; i++) {
	// ImageView iv = new ImageView(InitPageActivity.this);
	// iv.setLayoutParams(new
	// ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
	// ViewGroup.LayoutParams.MATCH_PARENT));
	// iv.setScaleType(ScaleType.FIT_XY);
	// iv.setImageResource(R.drawable.login_background);
	// views.add(iv);
	// }
	// }
	//
	// @Override
	// public int getCount() {
	// return views.size();
	// }
	//
	// @Override
	// public boolean isViewFromObject(View arg0, Object arg1) {
	// return arg0 == arg1;
	// }
	//
	// @Override
	// public void destroyItem(ViewGroup container, int position, Object object)
	// {
	// container.removeView(views.get(position));
	// }
	//
	// @Override
	// public Object instantiateItem(ViewGroup container, int position) {
	// container.addView(views.get(position));
	// return views.get(position);
	// }
	// }
}
