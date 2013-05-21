package com.rcplatform.phototalk.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Request.GraphUserCallback;
import com.facebook.Request.GraphUserListCallback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.OpenRequest;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionState;
import com.facebook.SharedPreferencesTokenCachingStrategy;
import com.facebook.model.GraphUser;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.FriendType;
import com.rcplatform.phototalk.galhttprequest.LogUtil;
import com.rcplatform.phototalk.thirdpart.bean.ThirdPartUser;
import com.rcplatform.phototalk.thirdpart.utils.GetFriendsListener;
import com.rcplatform.phototalk.thirdpart.utils.ThirdPartAccessTokenKeeper;

public class FacebookUtil {



	public static void getFacebookFriends(Activity context, final GetFriendsListener listener) {
		Session session = Session.getActiveSession();
		if (session != null && !session.isClosed() && !session.isOpened()) {
			session.openForRead(new Session.OpenRequest(context).setCallback(new StatusCallback() {

				@Override
				public void call(Session session, SessionState state, Exception exception) {
					if (session != null && session.isOpened()) {
						Request request = Request.newMyFriendsRequest(session, new GraphUserListCallback() {

							@Override
							public void onCompleted(List<GraphUser> users, Response response) {
								listener.onComplete(buildFriends(users));
							}
						});
						request.executeAsync();
					}
				}
			}));
		} else {
			session = Session.openActiveSession(context, true, new StatusCallback() {

				@Override
				public void call(final Session session, SessionState state, Exception exception) {
					if (session != null && session.isOpened()) {
						Request request = Request.newMyFriendsRequest(session, new GraphUserListCallback() {

							@Override
							public void onCompleted(List<GraphUser> users, Response response) {
								listener.onComplete(buildFriends(users));
							}
						});
						request.executeAsync();

					}
				}
			});

		}
	}

	public static List<ThirdPartUser> buildFriends(List<GraphUser> users) {
		List<ThirdPartUser> friends = new ArrayList<ThirdPartUser>();
		if (users != null) {
			for (GraphUser user : users) {
				ThirdPartUser friend = new ThirdPartUser();
				friend.setId(user.getId());
				friend.setNick(user.getName());
				friend.setHeadUrl(user.getLink());
				friend.setType(FriendType.FACEBOOK);
				friends.add(friend);
			}
		}
		return friends;
	}

	public static void clearFacebookVlidated(Context context) {
		ThirdPartAccessTokenKeeper.removeFacebookAccessToekn(context);
		Session s = Session.getActiveSession();
		if (s != null) {
			s.closeAndClearTokenInformation();
		}
	}

	public static void getMeFacebookInfo(Activity context, final OnMeInfoListener onMeInfoListener) {

		Session session = Session.getActiveSession();
		if (session != null && !session.isClosed() && !session.isOpened()) {
			session.openForRead(new Session.OpenRequest(context).setCallback(new StatusCallback() {

				@Override
				public void call(Session session, SessionState state, Exception exception) {
					if (session != null && session.isOpened()) {
						Request.newMeRequest(session, new GraphUserCallback() {

							@Override
							public void onCompleted(GraphUser user, Response response) {
								if (onMeInfoListener != null) {
									onMeInfoListener.onComplete(user);
								}
							}
						}).executeAsync();
					}
				}
			}));
		} else {
			session = Session.openActiveSession(context, true, new StatusCallback() {

				@Override
				public void call(final Session session, SessionState state, Exception exception) {
					if (session != null && session.isOpened()) {
						Request.newMeRequest(session, new GraphUserCallback() {

							@Override
							public void onCompleted(GraphUser user, Response response) {
								if (onMeInfoListener != null) {
									onMeInfoListener.onComplete(user);
								}
							}
						}).executeAsync();
					}
				}
			});
		}

	}

	private static void getMeFacebookInfo(Session session, final OnMeInfoListener listener) {
		Request.executeMeRequestAsync(session, new GraphUserCallback() {

			@Override
			public void onCompleted(GraphUser user, Response response) {
				if (listener != null)
					listener.onComplete(user);
			}
		});
	}

	private static void getFacebookFriends(Session session, final GetFriendsListener listener) {
		Request request = Request.newMyFriendsRequest(session, new GraphUserListCallback() {

			@Override
			public void onCompleted(List<GraphUser> users, Response response) {
				if (listener != null)
					listener.onComplete(buildFriends(users));
			}
		});
		request.executeAsync();
	}

	public static void login(final Activity context, Runnable onError) {
	}

	public interface OnMeInfoListener {
		public void onComplete(GraphUser user);
	}

	

	public static void deAuthorize(Session session) {

	}

	public static interface OnDeAuthorizeListener {
		public void onSuccess();

		public void onFail(String error);
	}
}
