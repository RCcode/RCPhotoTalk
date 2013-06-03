package com.rcplatform.tigase;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class TigaseNodeManager {

	static private TigaseNodeManager instance = null;

	private final String NODE_CONFIG_SHARE_PREFERENCES_KEY = "node_config_pref";

	private final String NODE_CONFIG_STR_KEY = "node_config_str";

	private final int NODE_RETRY_MAX_COUNT = 1;

	private TigaseNode currentNode = null;

	private Context ctx = null;

	private SharedPreferences preferences = null;

	private Editor editor = null;;

	private ArrayList<TigaseNode> nodeList = null;

	private final int DEFAULT_NODE_TIMEOUT = 10;

	private TigaseNodeManager(Context ctx) {
		this.ctx = ctx;
		preferences = ctx.getSharedPreferences(NODE_CONFIG_SHARE_PREFERENCES_KEY, 0);
		editor = preferences.edit();
		String configStr = preferences.getString(NODE_CONFIG_STR_KEY, null);
		// 更新配置
		if (null != configStr) {
			Thread thread = new Thread() {

				public void run() {
					updateNodeConfig();
				}
			};

			thread.run();
		}
	}

	static public TigaseNodeManager getInstance(Context ctx) {
		if (null == instance) {
			instance = new TigaseNodeManager(ctx);
		}
		return instance;
	}

	public TigaseNode getNode() {
		// 初始化Node List.
		if (null == nodeList) {
			initNodeList();
		}
		// 查看Node List是否过期，过期直接更新config.
		if (isNodeListExpired()) {
			updateNodeConfig();
			initNodeList();
		}

		TigaseNode node = randomNode();
		if (null != node) {
			int count = node.getConnectCount() + 1;
			node.setConnectCount(count);
			currentNode = node;
		}
		return node;
	}

	public TigaseNode getCurrentNode() {
		return currentNode;
	}

	public int getNodeTimeout() {
		int timeout = DEFAULT_NODE_TIMEOUT * 1000;
		if (null != currentNode) {
			timeout = currentNode.getTimeout() * 1000;
		}
		return timeout;
	}

	private TigaseNode randomNode() {
		TigaseNode node = null;
		if (null == this.nodeList) {
			return node;
		}

		int allWeight = 0;
		for (int i = 0; i < nodeList.size(); i++) {
			TigaseNode tempNode = nodeList.get(i);
			if (NODE_RETRY_MAX_COUNT > tempNode.getConnectCount()) {
				allWeight += tempNode.getWeight();
			}
		}

		int random = (int) (Math.random() * ((float) allWeight));
		int testWeight = 0;
		for (int i = 0; i < nodeList.size(); i++) {
			TigaseNode tempNode = nodeList.get(i);
			if (NODE_RETRY_MAX_COUNT > tempNode.getConnectCount()) {
				testWeight += tempNode.getWeight();
				if (random < testWeight) {
					node = tempNode;
					break;
				}
			}

		}

		return node;
	}

	private void initNodeList() {
		String configStr = preferences.getString(NODE_CONFIG_STR_KEY, null);
		if (null == configStr) {
			configStr = getNetNodeConfigString();
		}

		try {
			JSONObject resultJSON;
			resultJSON = new JSONObject(configStr);
			if (!resultJSON.has(TigaseConfig.TIGASE_STATUS)) {
				return;
			}

			if (resultJSON.getInt(TigaseConfig.TIGASE_STATUS) != 0) {
				return;
			}

			nodeList = new ArrayList<TigaseNode>();
			if (resultJSON.has(TigaseConfig.TIGASE_NODES)) {
				JSONArray array = resultJSON.getJSONArray(TigaseConfig.TIGASE_NODES);
				for (int i = 0; i < array.length(); i++) {
					JSONObject jsonObject = array.getJSONObject(i);
					TigaseNode node = new TigaseNode();
					if (jsonObject.has(TigaseConfig.TIGASE_NODE_DOMAIN)) {
						node.setDomain(jsonObject.getString(TigaseConfig.TIGASE_NODE_DOMAIN));
					}
					if (jsonObject.has(TigaseConfig.TIGASE_NODE_IP)) {
						node.setIp(jsonObject.getString(TigaseConfig.TIGASE_NODE_IP));
					}
					if (jsonObject.has(TigaseConfig.TIGASE_NODE_PORT)) {
						node.setPort(jsonObject.getInt(TigaseConfig.TIGASE_NODE_PORT));
					}
					if (jsonObject.has(TigaseConfig.TIGASE_NODE_WEIGHT)) {
						node.setWeight(jsonObject.getInt(TigaseConfig.TIGASE_NODE_WEIGHT));
					}
					if (jsonObject.has(TigaseConfig.TIGASE_NODE_STATUS)) {
						node.setStatus(jsonObject.getInt(TigaseConfig.TIGASE_NODE_STATUS));
					}
					if (jsonObject.has(TigaseConfig.TIGASE_NODE_REMARK)) {
						node.setRemark(jsonObject.getString(TigaseConfig.TIGASE_NODE_REMARK));
					}
					if (jsonObject.has(TigaseConfig.TIGASE_NODE_TIMEOUT)) {
						node.setTimeout(jsonObject.getInt(TigaseConfig.TIGASE_NODE_TIMEOUT));
					}
					node.setConnectCount(0);
					nodeList.add(node);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}

	}

	private String getNetNodeConfigString() {
		String content = null;
		InputStream is = null;
		HttpURLConnection conn = null;
		try {
			conn = (HttpURLConnection) new URL(TigaseConfig.NODE_BANLANCE_URL).openConnection();
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(10000);
			conn.setRequestMethod("POST");
			conn.getResponseCode();
			is = conn.getInputStream();
			InputStreamReader reader = new InputStreamReader(is, "UTF-8");
			StringBuilder builder = new StringBuilder();
			char[] readChars = new char[1024];
			String temp = null;
			int result = -1;
			while ((result = reader.read(readChars, 0, 1024)) != -1) {
				temp = new String(readChars, 0, result);
				builder.append(temp);
			}
			reader.close();
			content = builder.toString();
			editor.putString(NODE_CONFIG_STR_KEY, content);
			editor.commit();
		}
		catch (ConnectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			try {
				is.close();
			}
			catch (Exception e) {
			}
			if (conn != null)
				conn.disconnect();

		}
		return content;
	}

	synchronized private void updateNodeConfig() {
		getNetNodeConfigString();

	}

	private boolean isNodeListExpired() {
		if (null == this.nodeList) {
			return true;
		}
		boolean flag = true;
		for (int i = 0; i < nodeList.size(); i++) {
			TigaseNode node = nodeList.get(i);
			if (NODE_RETRY_MAX_COUNT > node.getConnectCount()) {
				flag = false;
				break;
			}
		}
		return flag;
	}
}
