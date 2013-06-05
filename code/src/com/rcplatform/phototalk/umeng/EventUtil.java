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

	public static class Main_Photo {

		private static final String EVENT_NAME = "Main_Photo";

		private static void onEvent(Context ctx, String label) {
			MobclickAgent.onEvent(ctx, EVENT_NAME, label);
		}

		static public void rcpt_photoview(Context ctx) {
			onEvent(ctx, "rcpt_photoview");
		}

		static public void rcpt_main_profileview(Context ctx) {
			onEvent(ctx, "rcpt_main_profileview");
		}

		static public void rcpt_main_longpress(Context ctx) {
			onEvent(ctx, "rcpt_main_longpress");
		}

		static public void rcpt_takephotobutton(Context ctx) {
			onEvent(ctx, "rcpt_takephotobutton");
		}

		static public void rcpt_takephoto(Context ctx) {
			onEvent(ctx, "rcpt_takephoto");
		}

		static public void rcpt_graffiti(Context ctx) {
			onEvent(ctx, "rcpt_graffiti");
		}

		static public void rcpt_photosave(Context ctx) {
			onEvent(ctx, "rcpt_photosave");
		}

		static public void rcpt_text(Context ctx) {
			onEvent(ctx, "rcpt_text");
		}

		static public void rcpt_timer(Context ctx) {
			onEvent(ctx, "rcpt_timer");
		}

		static public void rcpt_timer(Context ctx, int second) {
			String label = String.format("rcpt_timer%d", second);
			onEvent(ctx, label);
		}

		static public void rcpt_record(Context ctx) {
			onEvent(ctx, "rcpt_record");
		}

		static public void rcpt_recorddelete(Context ctx) {
			onEvent(ctx, "rcpt_recorddelete");
		}

		static public void rcpt_sendbutton(Context ctx) {
			onEvent(ctx, "rcpt_sendbutton");
		}

		static public void rcpt_choosefriends(Context ctx) {
			onEvent(ctx, "rcpt_choosefriends");
		}

		static public void rcpt_success_send(Context ctx) {
			onEvent(ctx, "rcpt_success_send");
		}

		static public void rcpt_menu(Context ctx) {
			onEvent(ctx, "rcpt_menu");
		}

		static public void rcpt_menu_addfriends(Context ctx) {
			onEvent(ctx, "rcpt_menu_addfriends");
		}

		static public void rcpt_menu_feedback(Context ctx) {
			onEvent(ctx, "rcpt_menu_feedback");
		}

		static public void rcpt_menu_rate(Context ctx) {
			onEvent(ctx, "rcpt_menu_rate");
		}

		static public void rcpt_friends(Context ctx) {
			onEvent(ctx, "rcpt_friends");
		}

		static public void rcpt_more(Context ctx) {
			onEvent(ctx, "rcpt_more");
		}

		static public void rcpt_updatepop_update(Context ctx) {
			onEvent(ctx, "rcpt_updatepop_update");
		}

		static public void rcpt_updatepop_later(Context ctx) {
			onEvent(ctx, "rcpt_updatepop_later");
		}

		static public void rcpt_phonepop_register(Context ctx) {
			onEvent(ctx, "rcpt_phonepop_register");
		}

		static public void rcpt_phonepop_later(Context ctx) {
			onEvent(ctx, "rcpt_phonepop_later");
		}

	}
	
	public static class Friends_Addfriends {

		private static final String EVENT_NAME = "Friends_Addfriends";

		private static void onEvent(Context ctx, String label) {
			MobclickAgent.onEvent(ctx, EVENT_NAME, label);
		}

		static public void rcpt_friends_profileview(Context ctx) {
			onEvent(ctx, "rcpt_friends_profileview");
		}
		
		static public void rcpt_friends_longpress(Context ctx) {
			onEvent(ctx, "rcpt_friends_longpress");
		}
		
		static public void rcpt_friendsdelete(Context ctx) {
			onEvent(ctx, "rcpt_friendsdelete");
		}
		
		static public void rcpt_addfriends(Context ctx) {
			onEvent(ctx, "rcpt_addfriends");
		}
		
		
		static public void rcpt_facebok(Context ctx) {
			onEvent(ctx, "rcpt_facebok");
		}
		
		static public void rcpt_vk(Context ctx) {
			onEvent(ctx, "rcpt_vk");
		}
		static public void rcpt_search(Context ctx) {
			onEvent(ctx, "rcpt_search");
		}
		static public void rcpt_profile_takephotobutton(Context ctx) {
			onEvent(ctx, "rcpt_friends_profileview");
		}
		static public void rcpt_profile_rcapp(Context ctx) {
			onEvent(ctx, "rcpt_friends_profileview");
		}

	}
}
