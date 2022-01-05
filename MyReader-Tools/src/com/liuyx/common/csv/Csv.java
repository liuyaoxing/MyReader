package com.liuyx.common.csv;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;
import java.util.Set;

public class Csv {

	static public char FIELD_DELIMITER = ',';

	static public char BLOCK_DELIMITER = '\n';

	private LinkedList<String[]> list = new LinkedList<String[]>();

	private String[] headers;

	public Csv(CsvReader reader) throws IOException {
		this.headers = reader.readLine();

		String[] line = null;

		while ((line = reader.readLine()) != null) {
			list.add(line);
		}
	}

	public String[] getAll(String header) {
		HashSet<String> values = new HashSet<String>();
		int idx = getHeaderIndex(header);
		Iterator<String[]> iterator = list.iterator();
		while (iterator.hasNext()) {
			values.add(((String[]) iterator.next())[idx]);
		}
		return (String[]) values.toArray(new String[0]);
	}

	private int getHeaderIndex(String header) {
		for (int i = 0; i < this.headers.length; i++) {
			if (header.equals(this.headers[i])) {
				return i;
			}
		}
		return -1;
	}

	public String[] get(String header, String subheader, String value) {
		HashSet<String> values = new HashSet<String>();
		int idx = getHeaderIndex(header);
		int subidx = getHeaderIndex(subheader);
		Iterator<String[]> iterator = list.iterator();
		while (iterator.hasNext()) {
			String[] strs = (String[]) iterator.next();
			if (value.equals(strs[idx])) {
				values.add(strs[subidx]);
			}
		}
		return (String[]) values.toArray(new String[0]);
	}

	public String[] get(String header, Properties context) {
		// optimisation
		if (context.contains(header)) {
			String[] ret = new String[1];
			ret[1] = context.getProperty(header);
			return ret;
		}

		HashSet<String> values = new HashSet<String>();
		int idx = getHeaderIndex(header);
		Iterator<String[]> iterator = list.iterator();
		Set<?> keys = context.keySet();
		LABEL: while (iterator.hasNext()) {
			// get next csv row
			String[] strs = (String[]) iterator.next();

			// check that this row is in context
			Iterator<?> keysIterator = keys.iterator();
			while (keysIterator.hasNext()) {
				String key = (String) keysIterator.next();
				String value = context.getProperty(key);
				int hdrIndex = getHeaderIndex(key);
				if (!value.equals(strs[hdrIndex])) {
					continue LABEL;
				}
			}

			values.add(strs[idx]);
		}

		return (String[]) values.toArray(new String[0]);
	}

}
