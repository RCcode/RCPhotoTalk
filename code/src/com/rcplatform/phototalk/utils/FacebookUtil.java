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
import com.rcplatform.phototalk.thirdpart.bean.ThirdPartFriend;
import com.rcplatform.phototalk.thirdpart.utils.GetFriendsListener;
import com.rcplatform.phototalk.thirdpart.utils.ThirdPartAccessTokenKeeper;

public class FacebookUtil {

	public static boolean isFacebookVlidate(Context context) {
		SharedPreferencesTokenCachingStrategy strategy = new SharedPreferencesTokenCachingStrategy(context);
		Bundle bundle = strategy.load();
		boolean vlidated = false;
		if (bundle != null) {
			String token = bundle.getString(SharedPreferencesTokenCachingStrategy.TOKEN_KEY);
			long expirationDate = bundle.getLong(SharedPreferencesTokenCachingStrategy.EXPIRATION_DATE_KEY);
			if (token != null && !isTokenExpiration(expirationDate))
				vlidated = true;
		}
		return vlidated;
	}

	private static boolean isTokenExpiration(long expirationDate) {
		return System.currentTimeMillis() > expirationDate;
	}

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

	public static List<ThirdPartFriend> buildFriends(List<GraphUser> users) {
		List<ThirdPartFriend> friends = new ArrayList<ThirdPartFriend>();
		if (users != null) {
			for (GraphUser user : users) {
				ThirdPartFriend friend = new ThirdPartFriend();
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

	public static void deAuthorize(Activity context, final OnDeAuthorizeListener listener) {
		if (isFacebookVlidate(context)) {
			Session session = Session.getActiveSession();
			StatusCallback callback = new StatusCallback() {

				@Override
				public void call(final Session session, SessionState state, Exception exception) {
					if (state.isOpened()) {
						new Thread() {
							@Override
							public void run() {
								Request request = new Request(session, "me/permissions");
								request.setHttpMethod(HttpMethod.DELETE);
								Response response = request.executeAndWait();
								if (response.getError() == null) {
									listener.onSuccess();
								} else {
									listener.onFail(response.getError().getErrorMessage());
								}
								response.getError();
							}
						}.start();
					}
				}
			};
			if (session != null && !session.isClosed() && !session.isOpened()) {
				session.openForRead(new OpenRequest(context).setCallback(callback));
			} else {
				session = Session.openActiveSession(context, false, callback);
			}
		}
	}

	public static void deAuthorize(Session session) {

	}

	public static interface OnDeAuthorizeListener {
		public void onSuccess();

		public void onFail(String error);
	}
}
