package com.rcplatform.phototalk;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.rcplatform.phototalk.activity.BaseActivity;

public class InviteActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.invite_friends);
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
				initInviteList(result);
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

		public abstract void invite(Context paramContext);

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

		public void invite(Context paramContext) {
			Intent localIntent = getTargetedSendIntent();
			localIntent.setType("text/plain");
			localIntent.putExtra("android.intent.extra.TEXT", getInviteText(paramContext));
			paramContext.startActivity(localIntent);
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

}
