package com.rcplatform.phototalk.db;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.db4o.ObjectSet;
import com.db4o.query.Query;
import com.rcplatform.phototalk.bean.Friend;
import com.rcplatform.phototalk.bean.Information;
import com.rcplatform.phototalk.bean.InformationState;
import com.rcplatform.phototalk.bean.InformationType;
import com.rcplatform.phototalk.bean.RecordUser;
import com.rcplatform.phototalk.bean.UserInfo;
import com.rcplatform.phototalk.logic.MessageSender;
import com.rcplatform.phototalk.thirdpart.bean.ThirdPartFriend;
import com.rcplatform.phototalk.thirdpart.utils.ThirdPartUtils;

public class PhotoTalkDb4oDatabase implements PhotoTalkDatabase {

	private static ObjectContainer db;

	public PhotoTalkDb4oDatabase(UserInfo userInfo) {
		db = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), DatabaseUtils.getDatabasePath(userInfo));
	}

	@Override
	public synchronized void saveThirdPartFriends(List<ThirdPartFriend> thirdPartFriends) {
		db.store(thirdPartFriends);
		db.commit();
	}

	@Override
	public synchronized void close() {
		db.close();
	}

	@Override
	public synchronized List<Friend> getThirdPartFriends(int type) {
		ThirdPartFriend example = new ThirdPartFriend();
		example.setType(type);
		ObjectSet<ThirdPartFriend> friends = db.queryByExample(example);
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
				if (rhs.getCreatetime() > lhs.getCreatetime())
					return 1;
				else if (rhs.getCreatetime() < lhs.getCreatetime())
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
		example.setSuid(suid);
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
		infoExample.setSender(new RecordUser(information.getSender().getSuid(), null, null,null));
		infoExample.setReceiver(new RecordUser(information.getReceiver().getSuid(), null, null,null));
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
		infoExample.setSender(new RecordUser(friend.getSuid(), null, null,null));
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
	public Map<String, Information> updateTempInformations(UserInfo senderInfo, String picUrl, long createTime, Map<String, String> userIds) {
		Information infoExample = new Information();
		infoExample.setSender(new RecordUser(senderInfo.getSuid(), null, null,null));
		infoExample.setCreatetime(createTime);
		ObjectSet<Information> infoLocals = db.queryByExample(infoExample);
		List<Information> informations = new ArrayList<Information>();
		Map<String, Information> result = new HashMap<String, Information>();
		for (Information info : infoLocals) {
			info.setUrl(picUrl);
			info.setStatu(InformationState.PhotoInformationState.STATU_NOTICE_SENDED_OR_NEED_LOADD);
			informations.add(info);
			if (userIds.containsKey(info.getReceiver().getSuid())) {
				result.put(info.getReceiver().getSuid(), info);
			}
		}
		if (userIds.containsKey(senderInfo.getSuid())) {
			RecordUser user = new RecordUser(senderInfo.getSuid(), senderInfo.getNick(), senderInfo.getHeadUrl(),senderInfo.getTigaseId());
			Information information = MessageSender.createInformation(InformationType.TYPE_PICTURE_OR_VIDEO,
					InformationState.PhotoInformationState.STATU_NOTICE_SENDED_OR_NEED_LOADD, user, user, createTime);
			result.put(user.getSuid(), information);
		}
		db.store(informations);
		db.commit();
		informations.clear();
		informations = null;
		return result;
	}
}
