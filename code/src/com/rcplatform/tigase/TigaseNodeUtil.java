package com.rcplatform.tigase;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TigaseNodeUtil {

	public static TigaseNode getTigaseNode() {
		List<TigaseNode> list = new ArrayList<TigaseNode>();
		InputStream is = null;
		HttpURLConnection conn = null;
		String content = null;
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
		} catch (ConnectException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (Exception e) {
			}
			if (conn != null)
				conn.disconnect();

		}

		int allWeight = 0;

		try {
			JSONObject resultJSON;
			resultJSON = new JSONObject(content);
			if (null == resultJSON) {
				return null;
			}
			if (!resultJSON.has(TigaseConfig.TIGASE_STATUS)) {
				return null;
			}

			if (resultJSON.getInt(TigaseConfig.TIGASE_STATUS) != 0) {
				return null;
			}

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

					list.add(node);
					allWeight += node.getWeight();
				}
			}
			// 根据权重获取连接节点
			TigaseNode selectTigaseNode = list.get(0);

			int random = (int) (Math.random() * ((float) allWeight));
			int testWeight = 0;
			for (int i = 0; i < list.size(); i++) {
				if (testWeight > random) {
					break;
				}
				selectTigaseNode = list.get(i);
				testWeight += list.get(i).getWeight();
			}
			return selectTigaseNode;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
