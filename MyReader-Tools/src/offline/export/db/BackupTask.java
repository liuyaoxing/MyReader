package offline.export.db;

import java.util.LinkedHashMap;
import java.util.Map;

public class BackupTask extends BaseDo {

	private Map<String, String> attributeMap = new LinkedHashMap<String, String>();

	public static final String KEY_ID = "id";
	public static final String KEY_TITLE = "title";
	public static final String KEY_URL = "url";
	public static final String KEY_LENGTH = "length";

	@Override
	public String getTableName() {
		return BackupTask.class.getSimpleName();
	}

	@Override
	public String getCreateSql() {
		return "CREATE TABLE BackupTask (id VARCHAR(16), title VARCHAR(256), url VARCHAR(256),  length VARCHAR(16))";
	}

	@Override
	public Map<String, String> getAttributeMaps() {
		return attributeMap;
	}

	public String getId() {
		return attributeMap.get(KEY_ID);
	}

	public void setId(String id) {
		attributeMap.put(KEY_ID, id);
	}

	public String getTitle() {
		return attributeMap.get(KEY_TITLE);
	}

	public void setTitle(String title) {
		attributeMap.put(KEY_TITLE, title);
	}

	public String getUrl() {
		return attributeMap.get(KEY_URL);
	}

	public void setUrl(String url) {
		attributeMap.put(KEY_URL, url);
	}

	public long getLength() {
		try {
			return Long.parseLong(attributeMap.get(KEY_LENGTH));
		} catch (Exception ex) {
			return 0;
		}
	}

	public void setLength(String length) {
		attributeMap.put(KEY_LENGTH, length);
	}
}
