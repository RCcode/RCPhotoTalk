package com.rcplatform.phototalk.db.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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
import com.rcplatform.phototalk.bean.FriendSourse;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.InformationState;
import com.rcplatform.phototalk.bean.InformationType;
import com.rcplatform.phototalk.bean.RecordUser;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.db.DatabaseUtils;
import com.rcplatform.phototalk.db.PhotoTalkDatabase;
import com.rcplatform.phototalk.drift.DriftInformation;
import com.rcplatform.phototalk.galhttprequest.LogUtil;
import com.rcplatform.phototalk.logic.MessageSender;
import com.rcplatform.phototalk.thirdpart.bean.ThirdPartUser;
import com.rcplatform.phototalk.thirdpart.utils.ThirdPartUtils;
import com.rcplatform.phototalk.utils.RCPlatformTextUtil;

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
		ObjectSet<Information> infos = queryInformations();
		List<Information> result = new ArrayList<Information>();
		if (infos != null)
			result.addAll(infos);
		return result;
	}

	private ObjectSet<Information> queryInformations() {
		try {
			Query query = db.query();
			query.constrain(Information.class);
			query.descend("receiveTime").orderDescending();
			ObjectSet<Information> infos = query.execute();
			return infos;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

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
			List<String> receivableUserIds, final List<String> allReceiverIds, int state, int totleLength, boolean hasVoice) {
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
			information.setTotleLength(totleLength);
			information.setLimitTime(totleLength);
			information.setUrl(picUrl);
			information.setHasVoice(hasVoice);
			result.put(user.getRcId(), information);
		}
		db.commit();
		return result;
	}

	@Override
	public synchronized void saveFriends(List<Friend> friends) {
		for (Friend friend : friends)
			friend.setLetter(RCPlatformTextUtil.getLetter(friend.getNickName()));
		ObjectSet<Friend> localCache = db.query(Friend.class);
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
				checkLetter(rhs);
				checkLetter(lhs);
				return lhs.getLetter().compareTo(rhs.getLetter());
			}
		});
		List<Friend> friends = new ArrayList<Friend>();
		friends.addAll(result);
		return friends;
	}

	private void checkLetter(Friend friend) {
		if (friend.getLetter() == null)
			friend.setLetter("#");
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
		for (Friend recommend : recommends) {
			recommend.setLetter(RCPlatformTextUtil.getLetter(recommend.getNickName()));
		}
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
			f.setFriend(false);
			db.store(f);
		}
		db.commit();
	}

	@Override
	public synchronized void updateTempInformationFail() {
		Information infoExample = new Information();
		infoExample.setType(InformationType.TYPE_PICTURE_OR_VIDEO);
		infoExample.setStatu(InformationState.PhotoInformationState.STATU_NOTICE_SENDING_OR_LOADING);
		ObjectSet<Information> result = db.query(new Predicate<Information>() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean match(Information arg0) {
				return arg0.getType() == InformationType.TYPE_PICTURE_OR_VIDEO
						&& InformationState.PhotoInformationState.STATU_NOTICE_SENDING_OR_LOADING == arg0.getStatu();
			}
		});
		while (result.hasNext()) {
			Information info = result.next();
			info.setStatu(InformationState.PhotoInformationState.STATU_NOTICE_SEND_OR_LOAD_FAIL);
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
	public synchronized void saveRecommends(List<Friend> recommends, final int friendType) {
		for (Friend recommend : recommends) {
			recommend.setLetter(RCPlatformTextUtil.getLetter(recommend.getNickName()));
		}
		ObjectSet<Friend> result = db.query(Friend.class);
		updateFriendsAndStore(result, recommends);
		db.commit();
	}

	private synchronized void updateFriendsAndStore(ObjectSet<Friend> caches, List<Friend> serviceFriends) {
		List<Friend> localFriends = new ArrayList<Friend>();
		localFriends.addAll(caches);
		for (Friend fService : serviceFriends) {
			if (localFriends.contains(fService)) {
				Friend fLocal = localFriends.get(localFriends.indexOf(fService));
				fLocal.setNickName(fService.getNickName());
				fLocal.setSource(fService.getSource());
				fLocal.setHeadUrl(fService.getHeadUrl());
				fLocal.setLetter(fService.getLetter());
				fLocal.setHiden(false);
				db.store(fLocal);
			} else {
				db.store(fService);
			}
		}
	}

	private Friend cloneFriend(Friend friend, Friend friendNew) {
		friendNew.setFriend(friend.isFriend());
		friendNew.setAppId(friend.getAppId());
		friendNew.setAppList(friend.getAppList());
		friendNew.setBackground(friend.getBackground());
		friendNew.setBirthday(friend.getBirthday());
		friendNew.setCellPhone(friend.getCellPhone());
		friendNew.setCountry(friend.getCountry());
		friendNew.setGender(friend.getGender());
		friendNew.setHeadUrl(friend.getHeadUrl());
		friendNew.setHiden(friend.isHiden());
		friendNew.setLetter(friend.getLetter());
		friendNew.setNickName(friend.getNickName());
		friendNew.setRcId(friend.getRcId());
		friendNew.setSource(friend.getSource());
		friendNew.setTigaseId(friend.getTigaseId());
		return friendNew;

	}

	@Override
	public synchronized List<Friend> getHidenFriends() {
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
	public synchronized void updateFriend(final Friend friend) {
		ObjectSet<Friend> result = db.query(new Predicate<Friend>() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean match(Friend arg0) {
				return friend.getRcId().equals(arg0.getRcId());
			}
		});
		LogUtil.e(result.size() + " is result size");
		boolean isHiden = true;
		boolean isFriend = false;
		FriendSourse source = null;
		for (Friend f : result) {
			isHiden = f.isHiden();
			isFriend = f.isFriend();
			source = f.getSource();
			db.delete(f);
		}
		friend.setSource(source);
		friend.setHiden(isHiden);
		friend.setFriend(isFriend);
		db.store(friend);
		db.commit();
	}

	@Override
	public synchronized Map<Integer, List<Information>> filterNewInformations(Collection<Information> newInformations, UserInfo currentUser) {
		long receiveTime = System.currentTimeMillis();
		List<Information> updatedInfos = new ArrayList<Information>();
		List<Information> newInfos = new ArrayList<Information>();
		List<Information> localInformations = new ArrayList<Information>();
		localInformations.addAll(db.query(Information.class));
		for (Information newInfo : newInformations) {
			newInfo.setReceiveTime(receiveTime);
			if (localInformations.contains(newInfo)) {
				List<Information> localInfos = updateInformation(currentUser, localInformations, newInfo);
				if (localInfos.size() > 0)
					db.store(localInfos);
				updatedInfos.addAll(localInfos);
			} else {
				db.store(newInfo);
				newInfos.add(newInfo);
			}
		}
		db.commit();
		Map<Integer, List<Information>> result = new HashMap<Integer, List<Information>>();
		result.put(NEW_INFORMATION, newInfos);
		result.put(UPDATED_INFORMATION, updatedInfos);
		return result;
	}

	private List<Information> updateInformation(UserInfo userInfo, List<Information> localInformations, Information newInformation) {
		Information localInformation = localInformations.get(localInformations.indexOf(newInformation));
		List<Information> informationUpdateds = new ArrayList<Information>();
		if (newInformation.getType() == InformationType.TYPE_FRIEND_REQUEST_NOTICE) {
			informationUpdateds.addAll(updateFriendRequestInformation(localInformations, newInformation));
		} else if (newInformation.getType() == InformationType.TYPE_PICTURE_OR_VIDEO) {
			if (userInfo.getRcId().equals(newInformation.getSender().getRcId())
					&& !newInformation.getReceiver().getRcId().equals(newInformation.getSender().getRcId())) {
				informationUpdateds.add(updateInformationSended(localInformation, newInformation));
			}
		}
		return informationUpdateds;
	}

	private Information updateInformationSended(Information localInformation, Information newInformation) {
		switch (localInformation.getStatu()) {
		case InformationState.PhotoInformationState.STATU_NOTICE_SENDING_OR_LOADING:
			localInformation.setStatu(newInformation.getStatu());
			break;
		case InformationState.PhotoInformationState.STATU_NOTICE_SENDED_OR_NEED_LOADD: {
			int newState = newInformation.getStatu();
			if (newState == InformationState.PhotoInformationState.STATU_NOTICE_DELIVERED_OR_LOADED
					|| newState == InformationState.PhotoInformationState.STATU_NOTICE_OPENED)
				localInformation.setStatu(newState);
		}
			break;
		case InformationState.PhotoInformationState.STATU_NOTICE_DELIVERED_OR_LOADED: {
			int newState = newInformation.getStatu();
			if (newState == InformationState.PhotoInformationState.STATU_NOTICE_OPENED)
				localInformation.setStatu(newState);
		}
			break;
		default:
			break;
		}
		return localInformation;
	}

	private List<Information> updateFriendRequestInformation(List<Information> localInformations, Information newInformation) {
		List<Information> updateInformations = new ArrayList<Information>();
		for (Information localInfo : localInformations) {
			if (localInfo.equals(newInformation)) {
				if (newInformation.getStatu() == InformationState.FriendRequestInformationState.STATU_QEQUEST_ADD_CONFIRM) {
					localInfo.setStatu(newInformation.getStatu());
					updateInformations.add(localInfo);
				}
			}
		}
		return updateInformations;
	}

	@Override
	public synchronized List<Information> getInformationByPage(int start, int pageSize) {
		ObjectSet<Information> localInformations = queryInformations();
		List<Information> result = new ArrayList<Information>();
		if (localInformations != null) {
			int end = start + pageSize;
			for (int i = start; i < end; i++) {
				if (i >= localInformations.size())
					break;
				Information information = localInformations.get(i);
				result.add(information);
			}
		}
		return result;
	}

	@Override
	public synchronized int getUnSendInformationCountByUrl(final String url) {
		ObjectSet<Information> result = db.query(new Predicate<Information>() {

			private static final long serialVersionUID = 1L;

			@Override
			public boolean match(Information arg0) {
				return url.equals(arg0.getUrl())
						&& arg0.getType() == InformationType.TYPE_PICTURE_OR_VIDEO
						&& (arg0.getStatu() == InformationState.PhotoInformationState.STATU_NOTICE_SENDING_OR_LOADING || arg0.getStatu() == InformationState.PhotoInformationState.STATU_NOTICE_SEND_OR_LOAD_FAIL);
			}
		});
		return result.size();
	}

	@Override
	public void handAddedFriendInformation(boolean hasLoadedFriends, String currentUserRcId, final Information information) {
		ObjectSet<Information> infosLocal = db.query(new Predicate<Information>() {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean match(Information arg0) {
				return ((arg0.getSender().getRcId().equals(information.getSender().getRcId()) && arg0.getReceiver().getRcId()
						.equals(information.getReceiver().getRcId())) || (arg0.getSender().getRcId().equals(information.getReceiver().getRcId()) && arg0
						.getReceiver().getRcId().equals(information.getSender().getRcId())))
						&& arg0.getType() == InformationType.TYPE_FRIEND_REQUEST_NOTICE;
			}
		});
		if (infosLocal.size() == 0) {
			if (hasLoadedFriends) {
				Friend friend = new Friend();
				saveRecordInfos(Arrays.asList(new Information[] { information }));
			} else {

			}

		} else {
		}
	}

	@Override
	public synchronized void updateFriendInformationState(Information information) {
		Information informationExample = new Information();
		informationExample.setSender(new RecordUser(information.getSender().getRcId(), null, null, null));
		informationExample.setReceiver(new RecordUser(information.getReceiver().getRcId(), null, null, null));
		informationExample.setType(InformationType.TYPE_FRIEND_REQUEST_NOTICE);
		ObjectSet<Information> localInfos = db.queryByExample(informationExample);
		for (Information info : localInfos) {
			info.setStatu(InformationState.FriendRequestInformationState.STATU_QEQUEST_ADD_CONFIRM);
			db.store(info);
		}
		db.commit();
	}

	@Override
	public void saveDriftInformation(DriftInformation information) {
		db.store(information);
	}

	@Override
	public List<DriftInformation> getDriftInformations(int start, int pageSize) {
		ObjectSet<DriftInformation> localInformations = queryDriftInformations();
		List<DriftInformation> result = new ArrayList<DriftInformation>();
		if (localInformations != null) {
			int end = start + pageSize;
			for (int i = start; i < end; i++) {
				if (i >= localInformations.size())
					break;
				DriftInformation information = localInformations.get(i);
				result.add(information);
			}
		}
		return result;
	}

	private ObjectSet<DriftInformation> queryDriftInformations() {
		try {
			Query query = db.query();
			query.constrain(DriftInformation.class);
			query.descend("receiveTime").orderDescending();
			ObjectSet<DriftInformation> infos = query.execute();
			return infos;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
