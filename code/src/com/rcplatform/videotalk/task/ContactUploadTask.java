package com.rcplatform.videotalk.task;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;

import com.rcplatform.videotalk.api.PhotoTalkApiUrl;
import com.rcplatform.videotalk.bean.Contacts;
import com.rcplatform.videotalk.db.PhotoTalkDatabaseFactory;
import com.rcplatform.videotalk.request.PhotoTalkParams;
import com.rcplatform.videotalk.request.RCPlatformResponse;
import com.rcplatform.videotalk.utils.ContactUtil;
import com.rcplatform.videotalk.utils.PrefsUtils;

public class ContactUploadTask {

	private static final int MAX_RETRY_TIME = 3;
	private static final int MSG_KEY_UPLOAD_SUCCESS = 100;
	private static final int MSG_KEY_UPLOAD_FAIL = 101;

	private Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_KEY_UPLOAD_SUCCESS:
				uploadResult = true;
				if (!PrefsUtils.AppInfo.hasUploadContacts(mContext))
					PrefsUtils.AppInfo.setContactsUploaded(mContext);
				else
					PrefsUtils.AppInfo.setLastContactUploadTime(mContext);
				if (!PrefsUtils.LoginState.hasAppUsed(mContext))
					PrefsUtils.LoginState.setAppUsed(mContext);
				break;
			case MSG_KEY_UPLOAD_FAIL:
				mTask = new ContactUploadTask(mContext);
				uploadResult = false;
				break;
			}
			if (mListener != null) {
				mListener.onUploadOver(uploadResult);
			}
			status = Status.STATUS_FINISH;

		};
	};
	private int status = Status.STATUS_PENDING;
	private OnUploadOverListener mListener;
	private int mCurrentTime;
	private Context mContext;
	private Thread mUploadTask;
	private boolean uploadResult = false;
	private static ContactUploadTask mTask;

	private ContactUploadTask(Context context) {
		this.mContext = context.getApplicationContext();
		mUploadTask = new UploadThread();
	};

	public synchronized static ContactUploadTask getInstance(Context context) {
		if (mTask == null) {
			mTask = new ContactUploadTask(context);
		}
		return mTask;
	}

	public synchronized static ContactUploadTask createNewTask(Context context) {
		mTask = new ContactUploadTask(context);
		return mTask;
	}

	private class UploadThread extends Thread {
		@Override
		public void run() {
			boolean hasUpdate = false;
			List<Contacts> contacts = ContactUtil.getContacts(mContext);
			if (!PrefsUtils.LoginState.hasAppUsed(mContext)) {
				hasUpdate = true;
			} else {
				List<Contacts> contactsLocal = PhotoTalkDatabaseFactory.getGlobalDatabase().getContacts();
				for (Contacts contact : contacts) {
					if (!contactsLocal.contains(contact)) {
						hasUpdate = true;
						break;
					}
				}
			}

			if (hasUpdate) {
				PhotoTalkDatabaseFactory.getGlobalDatabase().saveContacts(contacts);
				if (contacts.size() > 0) {
					String entity = getEntity(contacts);
					while (mCurrentTime <= MAX_RETRY_TIME) {
						mCurrentTime++;
						try {
							HttpClient client = new DefaultHttpClient();
							HttpPost post = new HttpPost(PhotoTalkApiUrl.SYNC_CONTACT_URL);
							post.setEntity(new StringEntity(entity, "UTF-8"));
							HttpResponse response = client.execute(post);
							if (response.getStatusLine().getStatusCode() == 200) {
								String result = readStream(response.getEntity().getContent());
								if (result != null) {
									if (RCPlatformResponse.ResponseStatus.isRequestSuccess(result)) {
										mHandler.sendEmptyMessage(MSG_KEY_UPLOAD_SUCCESS);
										return;
									}
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					mHandler.sendEmptyMessage(MSG_KEY_UPLOAD_FAIL);
				} else {
					mHandler.sendEmptyMessage(MSG_KEY_UPLOAD_SUCCESS);
				}
			} else {
				mHandler.sendEmptyMessage(MSG_KEY_UPLOAD_SUCCESS);
			}
		}
	}

	private String getEntity(Collection<Contacts> contacts) {
		try {
			JSONObject entity = new JSONObject();
			entity.put(PhotoTalkParams.PARAM_KEY_TOKEN, PhotoTalkParams.PARAM_VALUE_TOKEN_DEFAULT);
			entity.put(PhotoTalkParams.PARAM_KEY_LANGUAGE, PhotoTalkParams.PARAM_VALUE_LANGUAGE);
			entity.put(PhotoTalkParams.PARAM_KEY_DEVICE_ID, PhotoTalkParams.PARAM_VALUE_DEVICE_ID);
			entity.put(PhotoTalkParams.PARAM_KEY_APP_ID, PhotoTalkParams.PARAM_VALUE_APP_ID);
			JSONArray arrayContacts = new JSONArray();
			for (Contacts contact : contacts) {
				JSONObject jsonContact = new JSONObject();
				jsonContact.put(PhotoTalkParams.UploadContacts.PARAM_KEY_NAME, contact.getName());
				jsonContact.put(PhotoTalkParams.UploadContacts.PARAM_KEY_PHONE_NUMBER, contact.getMobilePhoneNumber());
				arrayContacts.put(jsonContact);
			}
			entity.put(PhotoTalkParams.UploadContacts.PARAM_KEY_CONTACT_LIST, arrayContacts);
			return entity.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void startUpload() {
		status = Status.STATUS_RUNNING;
		mUploadTask.start();
	}

	public int getStatus() {
		return status;
	}

	public static interface OnUploadOverListener {
		public void onUploadOver(boolean isSuccess);
	}

	private String readStream(InputStream is) {
		StringBuilder sb = new StringBuilder();
		int len = 0;
		byte[] buffer = new byte[1024];
		try {
			while ((len = is.read(buffer)) != -1) {
				sb.append(new String(buffer, 0, len));
			}
			return sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public static class Status {
		public static int STATUS_PENDING = 1;
		public static int STATUS_FINISH = 3;
		public static int STATUS_RUNNING = 2;
	}

	public void setOnUploadOverListener(OnUploadOverListener listener) {
		this.mListener = listener;
	}
}
