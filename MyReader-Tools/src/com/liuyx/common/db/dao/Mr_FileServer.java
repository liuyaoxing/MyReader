package com.liuyx.common.db.dao;

import java.util.HashMap;
import java.util.Map;

/**
 * 网页离线对象。
 *
 * @author 刘绕兴
 */
public class Mr_FileServer {

	protected Map<String, String> attributeMap = new HashMap<String, String>();

	public static final String TITLE = "title";
	public static final String FILE_LOCATION = "file_location";
	public static final String WEBSRC = "websrc";
	public static final String FOLDER_SIZE = "foldersize";
	public static final String MD5 = "md5";
	public static final String CONTENT_TYPE = "content_type";
	public static final String UPDATETIME = "updatetime";
	public static final String TIMESTAMP = "timestamp";
	public static final String REQ_TYPE = "req_type";
	public static final String STATE = "state";

	public Mr_FileServer() {
	}

	public Mr_FileServer(Map<String, String> attributeMap) {
		this.attributeMap = attributeMap;
	}

	public Map<String, String> getAttributeMap() {
		return attributeMap;
	}

	public String get(String key) {
		return attributeMap.get(key);
	}

	public String get(String key, String defValue) {
		String value = get(key);
		if (value != null && !value.isEmpty())
			return value;
		return defValue;
	}

	public void put(String key, String value) {
		if (value != null) {
			attributeMap.put(key, value);
		} else {
			attributeMap.remove(key);
		}
	}

	public String getTitle() {
		return get(TITLE);
	}

	public void setTitle(String title) {
		put(TITLE, title);
	}

	public String getLocation() {
		return get(FILE_LOCATION);
	}

	public void setLocation(String location) {
		put(FILE_LOCATION, location);
	}

	public void setWebSrc(String author) {
		put(WEBSRC, author);
	}

	public String getWebSrc() {
		return get(WEBSRC);
	}

	public long getFolderSize() {
		return Long.parseLong(get(FOLDER_SIZE, "0"));
	}

	public void setFolderSize(long size) {
		put(FOLDER_SIZE, String.valueOf(size));
	}

	public void setFileMD5(String md5) {
		put(MD5, md5);
	}

	public String getFileMD5() {
		return get(MD5);
	}

	public void setContentType(String contentType) {
		put(CONTENT_TYPE, contentType);
	}

	public String getContentType() {
		return get(CONTENT_TYPE);
	}

	public void setUpdateTime(String updateTime) {
		put(UPDATETIME, updateTime);
	}

	public String getUpdateTime() {
		return get(UPDATETIME);
	}
	public String getTimestamp() {
		return get(TIMESTAMP);
	}

	public void setTimestamp(String timestamp) {
		put(TIMESTAMP, timestamp);
	}

	/**
	 * @param reqType
	 *            0-下载，1-上传
	 */
	public void setReqType(int reqType) {
		put(REQ_TYPE, String.valueOf(reqType));
	}

	/**
	 * @return reqType 0-下载，1-上传
	 */
	public int getReqType() {
		return Integer.valueOf(get(REQ_TYPE));
	}

	public int getState() {
		return Integer.valueOf(get(STATE, "0"));
	}

	public void setState(int state) {
		put(STATE, String.valueOf(state));
	}
}
