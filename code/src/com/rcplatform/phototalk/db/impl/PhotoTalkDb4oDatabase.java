package com.rcplatform.phototalk.db.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.config.AndroidSupport;
import com.db4o.config.EmbeddedConfiguration;
import com.db4o.query.Predicate;
import com.db4o.query.Query;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.InformationState;
import com.rcplatform.phototalk.bean.InformationType;
import com.rcplatform.phototalk.bean.RecordUser;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.db.DatabaseUtils;
import com.rcplatform.phototalk.db.PhotoTalkDatabase;
import com.rcplatform.phototalk.galhttprequest.LogUtil;
import com.rcplatform.phototalk.logic.MessageSender;
import com.rcplatform.phototalk.thirdpart.bean.ThirdPartUser;
import com.rcplatform.phototalk.thirdpart.utils.ThirdPartUtils;

public class PhotoTalkDb4oDatabase implements PhotoTalkDatabase {

	private static ObjectContainer db;

	public PhotoTalkDb4oDatabase(UserInfo userInfo) {
		EmbeddedConfiguration config = Db4oEmbedded.newConfiguration();
		config.common().add(new AndroidSupport());
		db = Db4oEmbedded.openFile(config, DatabaseUtils.getDatabasePath(userInfo));
	}

	@Override
	public synchronized void saveThirdPartFriends(List<ThirdPartUser> thirdPartFriends, int type) {
		ThirdPartUser friendExample = new ThirdPartUser();
		friendExample.setType(type);
		ObjectSet<ThirdPartUser> result = db.queryByExample(friendExample);
		for (ThirdPartUser f : result)
			db.delete(f);
		db.store(thirdPartFriends);
		db.commit();
	}

	@Override
	public synchronized void close() {
		db.close();
	}

	@Override
	public synchronized List<Friend> getThirdPartFriends(int type) {
		ThirdPartUser example = new ThirdPartUser();
		example.setType(type);
		ObjectSet<ThirdPartUser> friends = db.queryByExample(example);
		return ThirdPartUtils.parserToFriends(friends, type);
	}

	@Override
	public synchronized void saveRecordInfos(List<Information> recordInfos) {
		db.store(recordInfos);
		db.commit();
	}

	@Override
	public synchronized List<Information> getRecordInfos() {

		Query query = db.query();
		query.constrain(Information.class);
		ObjectSet<Information> infos = query.sortBy(new Comparator<Information>() {

			@Override
			public int compare(Information lhs, Information rhs) {
				if (rhs.getReceiveTime() > lhs.getReceiveTime())
					return 1;
				else if (rhs.getReceiveTime() < lhs.getReceiveTime())
					return -1;
				return 0;
			}
		}).execute();
		List<Information> result = new ArrayList<Information>();
		result.addAll(infos);
		return result;
	}

	@Override
	public synchronized boolean hasFriend(String suid) {
		Friend example = new Friend();
		example.setRcId(suid);
		ObjectSet<Friend> result = db.queryByExample(example);
		if (result.size() > 0) {
			return true;
		}
		return false;
	}

	private synchronized void updateInformationState(Information information) {
		Information infoExample = getInformationExample(information);
		ObjectSet<Information> result = db.queryByExample(infoExample);
		if (result.size() > 0) {
			Information infoLocal = result.next();
			infoLocal.setStatu(information.getStatu());
			db.store(infoLocal);
		}
	}

	@Override
	public synchronized void updateInformationState(Information... informations) {
		for (Information info : informations) {
			updateInformationState(info);
		}
		if (informations.length > 0)
			db.commit();
	}

	private Information getInformationExample(Information information) {
		Information infoExample = new Information();
		infoExample.setCreatetime(information.getCreatetime());
		infoExample.setSender(new RecordUser(information.getSender().getRcId(), null, null, null));
		infoExample.setReceiver(new RecordUser(information.getReceiver().getRcId(), null, null, null));
		infoExample.setType(information.getType());
		return infoExample;
	}

	@Override
	public synchronized void deleteInformation(Information information) {
		Information infoExample = new Information();
		infoExample.setCreatetime(information.getCreatetime());
		infoExample.setReceiver(information.getReceiver());
		infoExample.setSender(information.getSender());
		infoExample.setType(information.getType());
		ObjectSet<Information> result = db.queryByExample(infoExample);
		if (result.size() > 0) {
			Information info = result.next();
			db.delete(info);
			db.commit();
		}
	}

	@Override
	public synchronized void clearInformation() {
		ObjectSet<Information> result = db.query(Information.class);
		for (Information info : result) {
			db.delete(info);
		}
		db.commit();
	}

	@Override
	public synchronized void updateFriendRequestInformationByFriend(Friend friend) {
		Information infoExample = new Information();
		infoExample.setSender(new RecordUser(friend.getRcId(), null, null, null));
		infoExample.setType(InformationType.TYPE_FRIEND_REQUEST_NOTICE);
		ObjectSet<Information> infos = db.queryByExample(infoExample);
		if (infos.size() > 0) {
			List<Information> infoCaches = new ArrayList<Information>();
			for (Information infoCache : infos) {
				infoCache.setStatu(InformationState.FriendRequestInformationState.STATU_QEQUEST_ADD_CONFIRM);
				infoCaches.add(infoCache);
			}
			db.store(infoCaches);
		}
	}

	@Override
	public synchronized Map<String, Information> updateTempInformations(final UserInfo senderInfo, String picUrl, final long createTime,
			List<String> receivableUserIds, final List<String> allReceiverIds, int state) {
		ObjectSet<Information> infoLocals = db.query(new Predicate<Information>() {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean match(Information arg0) {
				return allReceiverIds.contains(arg0.getReceiver().getRcId()) && arg0.getCreatetime() == createTime
						&& arg0.getSender().getRcId().equals(senderInfo.getRcId());
			}
		});
		Map<String, Information> result = new HashMap<String, Information>();
		for (Information info : infoLocals) {
			if (state == InformationState.PhotoInformationState.STATU_NOTICE_SENDED_OR_NEED_LOADD) {
				info.setUrl(picUrl);
				if (receivableUserIds.contains(info.getReceiver().getRcId())) {
					result.put(info.getReceiver().getRcId(), info);
				}
			}
			info.setStatu(state);
			db.store(info);
		}
		if (receivableUserIds != null && receivableUserIds.contains(senderInfo.getRcId())) {
			RecordUser user = new RecordUser(senderInfo.getRcId(), senderInfo.getNickName(), senderInfo.getHeadUrl(), senderInfo.getTigaseId());
			Information information = MessageSender.createInformation(InformationType.TYPE_PICTURE_OR_VIDEO,
					InformationState.PhotoInformationState.STATU_NOTICE_SENDED_OR_NEED_LOADD, user, user, createTime);
			result.put(user.getRcId(), information);
		}
		db.commit();
		return result;
	}

	@Override
	public synchronized void saveFriends(List<Friend> friends) {
		ObjectSet<Friend> localCache = db.query(new Predicate<Friend>() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean match(Friend arg0) {
				return arg0.isFriend();
			}
		});
		updateFriendsAndStore(localCache, friends);
		db.commit();
	}

	@Override
	public synchronized List<Friend> getFriends() {
		ObjectSet<Friend> result = db.query(new Predicate<Friend>() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean match(Friend arg0) {
				return arg0.isFriend() && !arg0.isHiden();
			}
		}, new Comparator<Friend>() {

			@Override
			public int compare(Friend lhs, Friend rhs) {
				return lhs.getLetter().compareTo(rhs.getLetter());
			}
		});
		List<Friend> friends = new ArrayList<Friend>();
		friends.addAll(result);
		return friends;
	}

	@Override
	public synchronized List<Friend> getRecommends(final int type) {
		ObjectSet<Friend> result = db.query(new Predicate<Friend>() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean match(Friend arg0) {
				return !arg0.isFriend() && arg0.getSource().getAttrType() == type && !arg0.isHiden();
			}
		});
		List<Friend> friends = new ArrayList<Friend>();
		friends.addAll(result);
		return friends;
	}

	@Override
	public synchronized List<Friend> getRecommends() {
		ObjectSet<Friend> result = db.query(new Predicate<Friend>() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean match(Friend arg0) {
				return !arg0.isFriend() && !arg0.isHiden();
			}
		});
		List<Friend> friends = new ArrayList<Friend>();
		friends.addAll(result);
		return friends;
	}

	@Override
	public synchronized void saveRecommends(List<Friend> recommends) {
		ObjectSet<Friend> localCache = db.query(Friend.class);
		updateFriendsAndStore(localCache, recommends);
		db.commit();
	}

	@Override
	public synchronized void addFriend(final Friend friend) {
		ObjectSet<Friend> result = db.query(new Predicate<Friend>() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean match(Friend arg0) {
				return friend.getRcId().equals(arg0.getRcId());
			}
		});
		LogUtil.e(result.size() + " is result size");
		for (Friend f : result) {
			db.delete(f);
		}
		db.store(friend);
		db.commit();
	}

	@Override
	public synchronized void deleteFriend(Friend friend) {
		Friend friendExample = new Friend();
		friendExample.setRcId(friend.getRcId());
		ObjectSet<Friend> result = db.queryByExample(friendExample);
		LogUtil.e(result.size() + " is result size");
		for (Friend f : result) {
			f.setHiden(true);
			db.store(f);
		}
		db.commit();
	}

	@Override
	public synchronized void updateTempInformationFail() {
		Information infoExample = new Information();
		infoExample.setType(InformationType.TYPE_PICTURE_OR_VIDEO);
		infoExample.setStatu(InformationState.PhotoInformationState.STATU_NOTICE_SENDING);
		ObjectSet<Information> result = db.query(new Predicate<Information>() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean match(Information arg0) {
				return arg0.getType() == InformationType.TYPE_PICTURE_OR_VIDEO
						&& InformationState.PhotoInformationState.STATU_NOTICE_SENDING == arg0.getStatu();
			}
		});
		while (result.hasNext()) {
			Information info = result.next();
			info.setStatu(InformationState.PhotoInformationState.STATU_NOTICE_SEND_FAIL);
			db.store(info);
		}
		db.commit();
	}

	@Override
	public synchronized Friend getFriendById(String rcId) {
		Friend friendExample = new Friend();
		friendExample.setRcId(rcId);
		ObjectSet<Friend> result = db.queryByExample(friendExample);
		if (result.hasNext()) {
			return result.next();
		}
		return null;
	}

	@Override
	public void saveRecommends(List<Friend> recommends, final int friendType) {
		ObjectSet<Friend> result = db.query(Friend.class);
		updateFriendsAndStore(result, recommends);
		db.commit();
	}

	private void updateFriendsAndStore(ObjectSet<Friend> caches, List<Friend> serviceFriends) {
		List<Friend> localFriends = new ArrayList<Friend>();
		localFriends.addAll(caches);
		for (Friend fService : serviceFriends) {
			if (localFriends.contains(fService)) {
				Friend fLocal = localFriends.get(localFriends.indexOf(fService));
				fLocal.setNickName(fService.getNickName());
				fLocal.setSource(fService.getSource());
				fLocal.setHeadUrl(fService.getHeadUrl());
				fLocal.setLetter(fService.getLetter());
				db.store(fLocal);
			} else {
				db.store(fService);
			}
		}
	}

	@Override
	public List<Friend> getHidenFriends() {
		ObjectSet<Friend> result = db.query(new Predicate<Friend>() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean match(Friend arg0) {
				return arg0.isHiden();
			}
		});
		List<Friend> delFriends = new ArrayList<Friend>();
		delFriends.addAll(result);
		return delFriends;
	}

	@Override
	public void updateFriend(final Friend friend) {
		ObjectSet<Friend> result = db.query(new Predicate<Friend>() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean match(Friend arg0) {
				return friend.getRcId().equals(arg0.getRcId());
			}
		});
		LogUtil.e(result.size() + " is result size");
		boolean isHiden = false;
		for (Friend f : result) {
			isHiden = f.isHiden();
			db.delete(f);
		}
		friend.setHiden(isHiden);
		db.store(friend);
		db.commit();
	}
}
