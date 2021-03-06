package offline.export.db;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.MapListHandler;

public class DataBaseProxy {

	Connection conn;

	public DataBaseProxy() {
		this("dulele");
	}

	public DataBaseProxy(String dbName) {
		conn = getConnection(dbName, "SA", "");
	}

	public Connection getConnection(String dbName, String userName, String password) {
		try {
			Class.forName("org.hsqldb.jdbc.JDBCDriver");
			return DriverManager.getConnection("jdbc:hsqldb:" + dbName, "SA", "");
		} catch (Exception e) {
			return null;
		}
	}

	public int dbCreate(BaseDo baseDo) throws SQLException, IllegalArgumentException, IllegalAccessException {
		String createSql = "CREATE TABLE IF NOT EXISTS %s (%s)";
		StringBuffer statement = new StringBuffer();
		Field[] fields = baseDo.getClass().getFields();
		for (Field field : fields) {
			if (statement.length() > 0)
				statement.append(", ");
			statement.append(field.get(baseDo)).append(" VARCHAR(256)");
		}
		return dbUpdate(String.format(createSql, baseDo.getTableName(), statement.toString()));
	}

	public List<Map<String, Object>> dbQuery(BaseDo baseDo) throws SQLException {
		return dbQuery(baseDo.getTableName(), baseDo.getAttributeMaps());
	}

	public List<Map<String, Object>> dbQuery(String tableName, Map<String, String> whereMap) throws SQLException {
		String selectTemplate = "SELECT * FROM %s WHERE %s";
		StringBuffer where = new StringBuffer();
		Iterator<Entry<String, String>> iter = whereMap.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, String> entry = iter.next();
			where.append(entry.getKey()).append(" = '").append(entry.getValue()).append("'");
		}
		return dbQuery(String.format(selectTemplate, tableName, where.toString()));
	}

	public List<Map<String, Object>> dbQuery(final String sql) throws SQLException {
		QueryRunner runner = new QueryRunner();
		return runner.query(conn, sql, new MapListHandler());
	}

	public int dbInsert(BaseDo baseDo) throws SQLException {
		String insertTemplate = "INSERT INTO %s(%s) VALUES(%s)";
		StringBuffer columns = new StringBuffer();
		StringBuffer values = new StringBuffer();
		Iterator<Entry<String, String>> iter = baseDo.getAttributeMaps().entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, String> entry = iter.next();
			if (columns.length() > 0)
				columns.append(",");
			columns.append(entry.getKey());
			if (values.length() > 0)
				values.append(",");
			values.append("'").append(entry.getValue()).append("'");
		}
		return dbUpdate(String.format(insertTemplate, baseDo.getTableName(), columns.toString(), values.toString()));
	}

	public int dbUpdate(String sql) throws SQLException {
		Statement statement = null;
		try {
			statement = conn.createStatement(); // statements
			return statement.executeUpdate(sql); // run the query
		} finally {
			statement.close();
		}
	}
}
