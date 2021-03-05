package offline.export.db;

import java.util.Map;

public abstract class BaseDo {

	public abstract String getTableName();

	public abstract String getCreateSql();

	public abstract Map<String, String> getAttributeMaps();
}
