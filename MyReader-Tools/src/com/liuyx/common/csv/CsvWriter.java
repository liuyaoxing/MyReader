package com.liuyx.common.csv;

import java.io.IOException;
import java.io.Writer;

public class CsvWriter {

	private char field_delim = Csv.FIELD_DELIMITER;

	private char block_delim = Csv.BLOCK_DELIMITER;

	private Writer writer;

	private boolean needFieldDelim;

	public CsvWriter(Writer wtr) {
		this.writer = wtr;
	}

	public Writer getWriter() {
		return this.writer;
	}

	public void setFieldDelimiter(char ch) {
		field_delim = ch;
	}

	public void setBlockDelimiter(char ch) {
		block_delim = ch;
	}

	public void writeField(String field) throws IOException {
		if (field == null) {
			field = "";
		}
		if (needFieldDelim) {
			writer.write(field_delim);
			needFieldDelim = false;
		}

		if (field.indexOf(field_delim) != -1 || field.indexOf('"') == 0 || field.indexOf(block_delim) != -1) {
			field = "\"" + replace(field, "\"", "\"\"") + "\"";
		}

		writer.write(field);
		if (field.length() > 0) {
			needFieldDelim = true;
		} else {
			writer.write(field_delim);
			needFieldDelim = false;
		}
	}

	public void endBlock() throws IOException {
		writer.write(block_delim);
		needFieldDelim = false;
	}

	public void writeLine(String[] strs) throws IOException {
		int sz = strs.length;
		for (int i = 0; i < sz; i++) {
			writeField(strs[i]);
		}
		endBlock();
	}

	public void close() throws IOException {
		this.writer.close();
	}

	private static String replace(String text, String repl, String with) {
		int max = -1;
		if (text == null || repl == null || with == null || repl.length() == 0 || max == 0) {
			return text;
		}

		StringBuffer buf = new StringBuffer(text.length());
		int start = 0, end = 0;
		while ((end = text.indexOf(repl, start)) != -1) {
			buf.append(text.substring(start, end)).append(with);
			start = end + repl.length();

			if (--max == 0) {
				break;
			}
		}
		buf.append(text.substring(start));
		return buf.toString();
	}

}
