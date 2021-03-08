package com.liuyx.common.csv;

import java.io.IOException;
import java.io.Reader;
import java.util.LinkedList;
import java.util.List;

public class CsvReader {

	private char field_delim = Csv.FIELD_DELIMITER;

	private char block_delim = Csv.BLOCK_DELIMITER;

	private Reader reader;

	private boolean newline;

	private boolean consume = false;

	public CsvReader(Reader rdr) {
		this.reader = rdr;
	}

	public void setFieldDelimiter(char ch) {
		field_delim = ch;
	}

	public void setBlockDelimiter(char ch) {
		block_delim = ch;
	}

	public void setConsuming(boolean b) {
		this.consume = b;
	}

	public boolean isConsuming() {
		return this.consume;
	}

	public String[] readLine() throws IOException {
		List<String> list = new LinkedList<String>();
		String str;

		while (true) {
			str = readField();
			if (str == null) {
				break;
			}
			if (consume && str.length() == 0) {
				continue;
			}
			list.add(str);
		}
		if (list.isEmpty()) {
			return null;
		}
		return (String[]) list.toArray(new String[list.size()]);
	}

	public String readField() throws IOException {
		if (this.newline) {
			this.newline = false;
			return null;
		}

		StringBuffer buffer = new StringBuffer();
		boolean quoted = false;
		int last = -1;
		int ch = this.reader.read();

		if (ch == -1) {
			return null;
		}

		if (ch == '"') {
			quoted = true;
		} else if (ch == block_delim) {
			return null;
		} else if (ch == field_delim) {
			return "";
		} else {
			buffer.append((char) ch);
		}

		while ((ch = this.reader.read()) != -1) {
			if (ch == block_delim) {
				if ((quoted && last == '"') || !quoted) {
					this.newline = true;
					break;
				}
			} else if (ch == field_delim) {
				if ((quoted && last == '"') || !quoted) {
					break;
				}
			} else if (ch == '"') {
				if (quoted) {
					if (last == '"') {
						last = -1;
					} else {
						last = '"';
						continue;
					}
				}
			}
			buffer.append((char) ch);
		}

		return buffer.toString();
	}

	public void close() throws IOException {
		this.reader.close();
	}

}
