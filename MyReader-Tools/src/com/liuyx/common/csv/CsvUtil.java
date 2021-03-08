package com.liuyx.common.csv;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class CsvUtil {

	public static Map<String, String> csvToMap(String csv) {
		if (csv == null) {
			return new LinkedHashMap<String, String>(1);
		}
		Map<String, String> resultMap = new LinkedHashMap<String, String>();
		try {
			CsvReader reader = new CsvReader(new StringReader(csv));
			String[] line;
			while ((line = reader.readLine()) != null && line.length > 1) {
				resultMap.put(line[0], line[1]);
			}
		} catch (IOException e) {
		}
		return resultMap;
	}

	public static String[] csvToStringArray(String csv) {
		if (csv == null || csv.length() == 0) {
			return new String[0];
		}
		CsvReader reader = new CsvReader(new StringReader(csv));
		try {
			return reader.readLine();
		} catch (IOException e) {
			return null;
		}
	}

	@SuppressWarnings("rawtypes")
	public static String mapToCsv(Map map) {
		if (map == null) {
			return "";
		}
		StringWriter inner = new StringWriter();
		CsvWriter writer = new CsvWriter(inner);
		boolean multi = false;
		try {
			for (Iterator<?> it = map.entrySet().iterator(); it.hasNext();) {
				if (multi) {
					writer.endBlock();
				}
				Map.Entry entry = (Map.Entry) it.next();
				writer.writeField(entry.getKey().toString());
				writer.writeField(entry.getValue() == null ? "" : entry.getValue().toString());
				multi = true;
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return inner.getBuffer().toString();
	}

	public static String stringArrayToCsv(String[] strArray) {
		if (strArray == null) {
			return "";
		}
		return stringArrayToCsv(strArray, 0, strArray.length);
	}

	public static String stringArrayToCsv(String[] strArray, int offset, int length) {
		if (strArray == null) {
			return "";
		}

		offset = Math.max(0, offset);

		int end = Math.min(strArray.length, offset + length);

		StringWriter inner = new StringWriter();
		CsvWriter writer = new CsvWriter(inner);
		try {
			for (int i = offset; i < end; i++) {
				writer.writeField(strArray[i]);
			}
			writer.close();
		} catch (IOException e) {
		}
		return inner.getBuffer().toString();
	}

	private CsvUtil() {
	}
}
