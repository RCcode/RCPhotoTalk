package com.rcplatform.phototalk;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.plus.PlusShare;
import com.rcplatform.phototalk.activity.BaseActivity;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.FriendType;
import com.rcplatform.phototalk.thirdpart.utils.OnAuthorizeSuccessListener;
import com.rcplatform.phototalk.thirdpart.utils.ThirdPartClient;
import com.rcplatform.phototalk.thirdpart.utils.TwitterClient;
import com.rcplatform.phototalk.utils.Utils;

public class InviteActivity extends BaseActivity {

	private List<InviteAction> basicInviteActions = new ArrayList<InviteActivity.InviteAction>();
	private List<ThirdPartClient> thirdPartClients = new ArrayList<ThirdPartClient>();
	private static TreeSet<Friend> friendsAdded;
	public static final String RESULT_PARAM_KEY_NEW_ADD_FRIENDS = "new_friends";
	private static final String GOOGLE_PLUS_PACKAGE = "com.google.android.apps.plus";

	private void initBasicInviteActions() {
		basicInviteActions.add(InviteActionFactory.getFriendInviteAction(this, FriendType.FACEBOOK));
		basicInviteActions.add(InviteActionFactory.getFriendInviteAction(this, FriendType.VK));
		basicInviteActions.add(InviteActionFactory.getFriendInviteAction(this, FriendType.DEFAULT));
		basicInviteActions.add(InviteActionFactory.getFriendInviteAction(this, FriendType.CONTACT));

		ThirdPartClient twitterClient = new TwitterClient(this);
		basicInviteActions.add(InviteActionFactory.getThirdPartInviteAction(this, twitterClient, FriendType.TWITTER));
		thirdPartClients.add(twitterClient);
		// GOOGLE PLUS
		if (Utils.checkApkExist(this, GOOGLE_PLUS_PACKAGE)) {
			InviteAction googlePlusAction = new GooglePlusInviteAction();
			googlePlusAction.appIcon = getResources().getDrawable(R.drawable.radio_contact_down);
			googlePlusAction.appName = getString(R.string.googleplus);
			basicInviteActions.add(googlePlusAction);
		}

	}

	// com.google.android.apps.plus
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.invite_friends);
		initBasicInviteActions();
		friendsAdded = new TreeSet<Friend>(new Comparator<Friend>() {

			@Override
			public int compare(Friend lhs, Friend rhs) {
				if (lhs.getRcId().equals(rhs.getRcId())) {
					return 0;
				}
				return 1;
			}
		});
		new AsyncTask<Void, Void, List<InviteAction>>() {
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				showLoadingDialog(false);
			}

			@Override
			protected List<InviteAction> doInBackground(Void... params) {
				return getInviteActivities();
			}

			@Override
			protected void onPostExecute(List<InviteAction> result) {
				super.onPostExecute(result);
				dissmissLoadingDialog();
				List<InviteAction> allInviteActions = new ArrayList<InviteActivity.InviteAction>();
				allInviteActions.addAll(basicInviteActions);
				allInviteActions.addAll(result);
				initInviteList(allInviteActions);
			}
		}.execute();
	}

	private void initInviteList(List<InviteAction> inviteList) {
		ListView lvInvite = (ListView) findViewById(R.id.lv_invites);
		lvInvite.setAdapter(new InviteAdapter(inviteList));
		lvInvite.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				InviteAction action = (InviteAction) arg0.getAdapter().getItem(arg2);
				action.invite(InviteActivity.this);
			}
		});
	}

	static Map<String, TargetApp> matchingPackages = new LinkedHashMap<String, TargetApp>();
	static {
		matchingPackages.put("com.whatsapp", new TargetApp("whatsapp", TextInviteAction.class));
		matchingPackages.put("jp.naver.line.android", new TargetApp("line", TextInviteAction.class));
		matchingPackages.put("com.tencent.mm", new TargetApp("wechat", TextInviteAction.class));
		matchingPackages.put("kik.android", new TargetApp("kik", TextInviteAction.class));
		matchingPackages.put("com.kakao.talk", new TargetApp("kakao", TextInviteAction.class));
		matchingPackages.put("com.sgiggle.production", new TargetApp("tanggo", TextInviteAction.class));
		matchingPackages.put("com.instagram.android", new TargetApp("instagram", InstagramInviteAction.class));
	}

	private static abstract class InviteAction {
		private Drawable appIcon;
		private String appName;
		private String className;
		private String packageName;
		private String trackFrom;

		protected String getInviteText(Context paramContext) {
			String inviteText = paramContext.getString(R.string.join_message, ((PhotoTalkApplication) paramContext.getApplicationContext()).getCurrentUser()
					.getRcId());
			return inviteText;
		}

		protected Intent getTargetedSendIntent() {
			Intent localIntent = new Intent("android.intent.action.SEND");
			localIntent.setClassName(this.packageName, this.className);
			return localIntent;
		}

		public abstract void invite(Activity paramContext);

		public void setActivityInfo(ActivityInfo paramActivityInfo) {
			this.packageName = paramActivityInfo.packageName;
			this.className = paramActivityInfo.name;
		}

		public void setApplicationInfo(Context paramContext, ApplicationInfo paramApplicationInfo) {
			PackageManager localPackageManager = paramContext.getPackageManager();
			this.appName = localPackageManager.getApplicationLabel(paramApplicationInfo).toString();
			this.appIcon = localPackageManager.getApplicationIcon(paramApplicationInfo);
		}

		public void setTargetApp(TargetApp paramTargetApp) {
			this.trackFrom = paramTargetApp.trackFrom;
		}
	}

	private static class TargetApp {
		final Class<? extends InviteAction> inviteClass;
		final String trackFrom;

		public TargetApp(String paramString2, Class<? extends InviteAction> paramClass) {
			this.trackFrom = paramString2;
			this.inviteClass = paramClass;
		}
	}

	private static class TextInviteAction extends InviteAction {
		public TextInviteAction() {
			super();
		}

		public void invite(Activity paramContext) {
			Intent localIntent = getTargetedSendIntent();
			localIntent.setType("text/plain");
			localIntent.putExtra("android.intent.extra.TEXT", getInviteText(paramContext));
			paramContext.startActivity(localIntent);
		}
	}

	private static class LocalInviteAction extends InviteAction {
		private int type;

		public LocalInviteAction(int type) {
			super();
			this.type = type;
		}

		@Override
		public void invite(Activity paramContext) {
			switch (type) {
			case FriendType.FACEBOOK:
				paramContext.startActivity(new Intent(paramContext, FacebookFriendRecommendActivity.class));
				break;
			case FriendType.CONTACT:
				paramContext.startActivity(new Intent(paramContext, ContactFriendRecommendActivity.class));
				break;
			case FriendType.VK:
				paramContext.startActivity(new Intent(paramContext, VKRecommendActivity.class));
				break;
			default:
				paramContext.startActivity(new Intent(paramContext, SearchFriendsActivity.class));
				break;
			}
		}

	}

	private static class ThirdPartInviteAction extends InviteAction {
		private ThirdPartClient client;

		public ThirdPartInviteAction(ThirdPartClient client) {
			super();
			this.client = client;
		}

		@Override
		public void invite(final Activity paramContext) {
			if (client.isAuthorized()) {
				client.sendJoinMessage(getInviteText(paramContext));
			} else {
				client.authorize(new OnAuthorizeSuccessListener() {

					@Override
					public void onAuthorizeSuccess() {
						client.sendJoinMessage(getInviteText(paramContext));
					}
				});
			}
		}
	}

	private static class InstagramInviteAction extends InviteAction {
		public InstagramInviteAction() {
			super();
		}

		public void invite(Activity paramContext) {
			File localFile = new File(Environment.getExternalStorageDirectory(), "instagram_share.jpg");
			if (localFile == null || !localFile.exists())
				return;
			Intent localIntent = getTargetedSendIntent();
			Uri localUri = Uri.fromFile(localFile);
			localIntent.setData(localUri);
			localIntent.setType("image/*");
			localIntent.putExtra("android.intent.extra.STREAM", localUri);
			paramContext.startActivity(localIntent);
		}
	}

	private static class GooglePlusInviteAction extends InviteAction {

		public GooglePlusInviteAction() {
			super();
		}

		@Override
		public void invite(final Activity paramContext) {
			Intent shareIntent = new PlusShare.Builder(paramContext).setText(getInviteText(paramContext)).setType("text/plain")
					.setContentDeepLinkId("phototalk", /**
					 * Deep-link identifier
					 */
					paramContext.getString(R.string.app_name), /** Snippet title */
					"A tasty recipe for making lemon cheesecake.", /**
					 * Snippet
					 * description
					 */
					Uri.parse("https://lh6.ggpht.com/BxPGNo_gbqq_WEN9Qvqi47JEk4VHt7Et4WDS7Nb2Mi2qzffAx_whtBVJ3Pc0xn8Vh246=w124")).getIntent();

			paramContext.startActivityForResult(shareIntent, 0);
		}
	}

	private List<InviteAction> getInviteActivities() {
		PackageManager localPackageManager = getActivity().getPackageManager();
		Intent localIntent1 = new Intent("android.intent.action.SEND");
		localIntent1.setType("text/plain");
		Intent localIntent2 = new Intent("android.intent.action.SEND");
		localIntent2.setType("image/*");
		Intent[] arrayOfIntent = { localIntent1, localIntent2 };
		HashMap<String, InviteAction> localHashMap = new HashMap<String, InviteAction>();
		ArrayList<ResolveInfo> localArrayList = new ArrayList<ResolveInfo>();
		int i = arrayOfIntent.length;
		for (int j = 0; j < i; ++j)
			localArrayList.addAll(localPackageManager.queryIntentActivities(arrayOfIntent[j], 0));
		Iterator<String> localIterator1 = matchingPackages.keySet().iterator();
		while (localIterator1.hasNext()) {
			ResolveInfo localResolveInfo = null;
			String str = localIterator1.next();
			if (localHashMap.containsKey(str))
				continue;
			Iterator<ResolveInfo> localIterator2 = localArrayList.iterator();
			while (localIterator2.hasNext()) {
				localResolveInfo = localIterator2.next();
				if (stringEquals(str, localResolveInfo.activityInfo.packageName)) {
					TargetApp localTargetApp = (TargetApp) matchingPackages.get(str);
					ApplicationInfo localApplicationInfo;
					try {
						localApplicationInfo = localPackageManager.getApplicationInfo(localResolveInfo.activityInfo.packageName, 0);
						InviteAction localInviteAction = (InviteAction) localTargetApp.inviteClass.newInstance();
						localInviteAction.setApplicationInfo(getActivity(), localApplicationInfo);
						localInviteAction.setActivityInfo(localResolveInfo.activityInfo);
						localInviteAction.setTargetApp(localTargetApp);
						localHashMap.put(str, localInviteAction);
					} catch (Exception localException) {
						localException.printStackTrace();
					}
					break;
				}
			}
		}
		return new ArrayList<InviteAction>(localHashMap.values());
	}

	public static boolean stringEquals(CharSequence paramCharSequence1, CharSequence paramCharSequence2) {
		if (paramCharSequence1 == null)
			return (paramCharSequence2 != null);
		return paramCharSequence1.equals(paramCharSequence2);
	}

	private class InviteAdapter extends BaseAdapter {
		private List<InviteAction> actions;

		public InviteAdapter(List<InviteAction> actions) {
			this.actions = actions;
		}

		@Override
		public int getCount() {
			return actions.size();
		}

		@Override
		public Object getItem(int position) {
			return actions.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			InviteAction action = actions.get(position);
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.invite_item, parent, false);
			}
			ImageView ivIcon = (ImageView) convertView.findViewById(R.id.iv_app_icon);
			TextView tvName = (TextView) convertView.findViewById(R.id.tv_app_name);
			ivIcon.setImageDrawable(action.appIcon);
			tvName.setText(action.appName);
			return convertView;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		for (ThirdPartClient client : thirdPartClients)
			client.onAuthorizeInformationReceived(requestCode, resultCode, data);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		for (ThirdPartClient client : thirdPartClients)
			client.destroy();
		friendsAdded.clear();
		friendsAdded = null;
	}

	public static void addFriend(Friend friend) {
		friendsAdded.add(friend);
	}

	@Override
	public void onBackPressed() {
		Intent intent = new Intent();
		intent.putExtra(RESULT_PARAM_KEY_NEW_ADD_FRIENDS, new ArrayList<Friend>(friendsAdded));
		setResult(Activity.RESULT_OK, intent);
		finish();
	}

	private static class InviteActionFactory {

		public static InviteAction getThirdPartInviteAction(Context context, ThirdPartClient client, int thirdPartType) {
			InviteAction action = new ThirdPartInviteAction(client);
			switch (thirdPartType) {
			case FriendType.TWITTER:
				action.appIcon = context.getResources().getDrawable(R.drawable.radio_facebook_down);
				action.appName = context.getString(R.string.twitter);
				break;
			}
			return action;
		}

		public static InviteAction getFriendInviteAction(Context context, int friendType) {
			InviteAction action = null;
			switch (friendType) {
			case FriendType.FACEBOOK:
				action = new LocalInviteAction(FriendType.FACEBOOK);
				action.appIcon = context.getResources().getDrawable(R.drawable.radio_facebook_down);
				action.appName = context.getString(R.string.facebook);
				break;
			case FriendType.CONTACT:
				action = new LocalInviteAction(FriendType.CONTACT);
				action.appIcon = context.getResources().getDrawable(R.drawable.radio_contact_down);
				action.appName = context.getString(R.string.contact);
				break;
			case FriendType.VK:
				action = new LocalInviteAction(FriendType.VK);
				action.appIcon = context.getResources().getDrawable(R.drawable.radio_vk_down);
				action.appName = context.getString(R.string.vkontakte);
				break;
			default:
				action = new LocalInviteAction(FriendType.DEFAULT);
				action.appIcon = context.getResources().getDrawable(R.drawable.radio_seach_down);
				action.appName = context.getString(R.string.search);
				break;
			}
			return action;
		}
	}
}
