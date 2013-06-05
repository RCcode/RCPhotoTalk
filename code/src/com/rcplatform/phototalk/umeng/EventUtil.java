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

	public static class More_Setting {

		private static final String EVENT_NAME = "More_Setting";

		private static void onEvent(Context ctx, String label) {
			MobclickAgent.onEvent(ctx, EVENT_NAME, label);
		}

		static public void rcpt_clear(Context ctx) {
			onEvent(ctx, "rcpt_clear");
		}

		static public void rcpt_avataredit(Context ctx) {
			onEvent(ctx, "rcpt_avataredit");
		}

		static public void rcpt_backgroundedit(Context ctx) {
			onEvent(ctx, "rcpt_backgroundedit");
		}

		static public void rcpt_nameedit(Context ctx) {
			onEvent(ctx, "rcpt_nameedit");
		}

		static public void rcpt_genderedit(Context ctx) {
			onEvent(ctx, "rcpt_genderedit");
		}

		static public void rcpt_ageedit(Context ctx) {
			onEvent(ctx, "rcpt_ageedit");
		}

		static public void rcpt_friendsupdate(Context ctx) {
			onEvent(ctx, "rcpt_friendsupdate");
		}

		static public void rcpt_friendsupdate_profileview(Context ctx) {
			onEvent(ctx, "rcpt_friendsupdate_profileview");
		}

		static public void rcpt_myaccount(Context ctx) {
			onEvent(ctx, "rcpt_myaccount");
		}

		static public void rcpt_success_changepassword(Context ctx) {
			onEvent(ctx, "rcpt_success_changepassword");
		}
		
		static public void rcpt_changepasswordsbutton(Context ctx) {
			onEvent(ctx, "rcpt_changepasswordsbutton");
		}

		static public void rcpt_facebookunlink(Context ctx) {
			onEvent(ctx, "rcpt_facebookunlink");
		}

		static public void rcpt_vkunlink(Context ctx) {
			onEvent(ctx, "rcpt_vkunlink");
		}

		static public void rcpt_phonenumber(Context ctx) {
			onEvent(ctx, "rcpt_phonenumber");
		}

		static public void rcpt_getcode(Context ctx) {
			onEvent(ctx, "rcpt_getcode");
		}

		static public void rcpt_success_phonenumber(Context ctx) {
			onEvent(ctx, "rcpt_success_phonenumber");
		}

		static public void rcpt_logout(Context ctx) {
			onEvent(ctx, "rcpt_logout");
		}

		static public void rcpt_setting(Context ctx) {
			onEvent(ctx, "rcpt_setting");
		}

		static public void rcpt_onlyfirends(Context ctx) {
			onEvent(ctx, "rcpt_onlyfirends");
		}

		static public void rcpt_anyone(Context ctx) {
			onEvent(ctx, "rcpt_anyone");
		}

		static public void rcpt_share(Context ctx) {
			onEvent(ctx, "rcpt_share");
		}

		static public void rcpt_notshare(Context ctx) {
			onEvent(ctx, "rcpt_notshare");
		}

		static public void rcpt_about(Context ctx) {
			onEvent(ctx, "rcpt_about");
		}

		static public void rcpt_checkupdate(Context ctx) {
			onEvent(ctx, "rcpt_checkupdate");
		}

		static public void rcpt_feedback(Context ctx) {
			onEvent(ctx, "rcpt_feedback");
		}

	}
}
