package com.rcplatform.phototalk.umeng;

import android.content.Context;

import com.umeng.analytics.MobclickAgent;

public class EventUtil {

	public static class Register_Login_Invite {

		static public void rcpt_registerbutton(Context ctx) {
			MobclickAgent.onEvent(ctx, "Register_Login_Invite", "rcpt_registerbutton");
		}

		static public void rcpt_register(Context ctx) {
			MobclickAgent.onEvent(ctx, "Register_Login_Invite", "rcpt_register");
		}

		static public void rcpt_smsinvite(Context ctx) {
			MobclickAgent.onEvent(ctx, "Register_Login_Invite", "rcpt_smsinvite");
		}

		static public void rcpt_success_smsinvite(Context ctx) {
			MobclickAgent.onEvent(ctx, "Register_Login_Invite", "rcpt_success_smsinvite");
		}

		static public void rcpt_facebooklink(Context ctx) {
			MobclickAgent.onEvent(ctx, "Register_Login_Invite", "rcpt_facebooklink");
		}

		static public void rcpt_facebookinvite(Context ctx) {
			MobclickAgent.onEvent(ctx, "Register_Login_Invite", "rcpt_facebookinvite");
		}

		static public void rcpt_vklinklink(Context ctx) {
			MobclickAgent.onEvent(ctx, "Register_Login_Invite", "rcpt_vklinklink");
		}

		static public void rcpt_vkinvite(Context ctx) {
			MobclickAgent.onEvent(ctx, "Register_Login_Invite", "rcpt_vkinvite");
		}

		static public void rcpt_success_vkinvite(Context ctx) {
			MobclickAgent.onEvent(ctx, "Register_Login_Invite", "rcpt_success_vkinvite");
		}

		static public void rcpt_success_search(Context ctx) {
			MobclickAgent.onEvent(ctx, "Register_Login_Invite", "rcpt_success_search");
		}

		static public void rcpt_loginbutton(Context ctx) {
			MobclickAgent.onEvent(ctx, "Register_Login_Invite", "rcpt_loginbutton");
		}

		static public void rcpt_login(Context ctx) {
			MobclickAgent.onEvent(ctx, "Register_Login_Invite", "rcpt_login");
		}

		static public void rcpt_forgetpasswordbutton(Context ctx) {
			MobclickAgent.onEvent(ctx, "Register_Login_Invite", "rcpt_forgetpasswordbutton");
		}

		static public void rcpt_resetpassword(Context ctx) {
			MobclickAgent.onEvent(ctx, "Register_Login_Invite", "rcpt_resetpassword");
		}
	}
}
