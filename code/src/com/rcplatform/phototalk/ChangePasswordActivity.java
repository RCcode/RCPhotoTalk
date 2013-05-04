package com.rcplatform.phototalk;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.rcplatform.phototalk.activity.BaseActivity;

public class ChangePasswordActivity extends BaseActivity implements OnClickListener {

	private ViewSwitcher mViewSwitcher;
	private EditText etPassword;
	private EditText etNewPassword;
	private EditText etConfirmPassword;
	private Button btnConfirm;
	private Button btnConfirmPassword;

	private TextView tvForget;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.change_password);
		initView();
	}

	private void initView() {
		mViewSwitcher = (ViewSwitcher) findViewById(R.id.vs_change);
		etPassword = (EditText) findViewById(R.id.et_password_current);
		etNewPassword = (EditText) findViewById(R.id.et_new_password);
		etConfirmPassword = (EditText) findViewById(R.id.et_confirm_password);
		btnConfirm = (Button) findViewById(R.id.btn_current_confirm);
		btnConfirmPassword = (Button) findViewById(R.id.btn_confirm_password);
		tvForget = (TextView) findViewById(R.id.tv_forget_password);
		tvForget.setOnClickListener(this);
		btnConfirmPassword.setOnClickListener(this);
		btnConfirm.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_confirm_password:
			
			break;
		case R.id.tv_forget_password:
			startActivity(ForgetPasswordActivity.class);
			break;
		case R.id.btn_current_confirm:
			break;
		}
	}
}
