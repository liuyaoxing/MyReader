package com.liuyx.common.csv;

import java.io.IOException;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.List;

public class CsvParser {

	private static final int ST_FIELDCONTENT = 2;

	private static final int ST_FIELDSTART = 1;

	private static final int ST_LINESTART = 0;

	private char block_delim = '\n';

	private StringBuffer bufferedField;

	private List<String> bufferedLine;

	private boolean consume = false;

	private int fieldLengthLimit = -1;

	public int getFieldLengthLimit() {
		return fieldLengthLimit;
	}

	public void setFieldLengthLimit(int fieldLengthLimit) {
		this.fieldLengthLimit = fieldLengthLimit;
	}

	private char field_delim = ',';

	private int last = -1;

	private List<CsvListener> listeners = new ArrayList<CsvListener>();

	private boolean quoted;

	private int state = ST_LINESTART;

	public CsvParser() {
	}

	public void addListener(CsvListener l) {
		listeners.add(l);
	}

	/**
	 */
	public void finish() {
		if (bufferedLine != null) {
			notifyListeners();
			bufferedLine = null;
		}
	}

	public List<String> getBufferedLine() {
		return bufferedLine;
	}

	public void handle(char[] charArray) {
		handle(CharBuffer.wrap(charArray));
	}

	public void handle(CharBuffer charBuffer) {
		while (charBuffer.remaining() > 0) {
			int c = charBuffer.get();
			switch (state) {
				case ST_LINESTART :
					handleLineStart(c);
					break;
				case ST_FIELDSTART :
					handleFieldStart(c);
					break;
				case ST_FIELDCONTENT :
					handleFieldContent(c);
					break;
				default :
					System.out.println("bad state");
			}
			if (bufferedField != null && fieldLengthLimit > 0 && bufferedField.length() > fieldLengthLimit) {
				reset();
			}
		}
	}

	private void handleFieldContent(int ch) {
		if (ch == block_delim) {
			if ((quoted && last == '"') || !quoted) {
				bufferedLine.add(bufferedField.toString());
				notifyListeners();
				state = ST_LINESTART;
				return;
			}
		} else if (ch == field_delim) {
			if ((quoted && last == '"') || !quoted) {
				bufferedLine.add(bufferedField.toString());
				state = ST_FIELDSTART;
				return;
			}
		} else if (ch == '"') {
			if (quoted && last != '"') {
				last = '"';
				return;
			}
		}
		last = -1;
		bufferedField.append((char) ch);
	}

	private void handleFieldStart(int ch) {
		bufferedField = new StringBuffer();
		quoted = false;
		if (ch == '"') {
			quoted = true;
			state = ST_FIELDCONTENT;
		} else if (ch == block_delim) {
			bufferedField = null;
			notifyListeners();
			state = ST_LINESTART;
		} else if (ch == field_delim) {
			if (!consume) {
				bufferedLine.add("");
			}
			state = ST_FIELDSTART;
		} else {
			bufferedField.append((char) ch);
			state = ST_FIELDCONTENT;
		}

	}

	private void handleLineStart(int ch) {
		bufferedLine = new ArrayList<String>();
		bufferedField = new StringBuffer();
		quoted = false;
		if (ch == '"') {
			quoted = true;
			state = ST_FIELDCONTENT;
		} else if (ch == block_delim) {

			bufferedField = null;
			bufferedLine = null;
			notifyListeners();
			state = ST_LINESTART;
		} else if (ch == field_delim) {
			if (!consume) {
				bufferedLine.add("");
			}
			state = ST_FIELDSTART;
		} else {
			bufferedField.append((char) ch);
			state = ST_FIELDCONTENT;
		}
	}

	public boolean isConsuming() {
		return this.consume;
	}

	/**
	 * @throws IOException
	 */
	private void notifyListeners() {
		for (CsvListener l : listeners) {
			l.handleCsvLine(this, bufferedLine);
		}
	}

	public void removeListener(CsvListener l) {
		listeners.remove(l);
	}

	/**
	 */
	public void reset() {
		state = ST_LINESTART;
		if (bufferedField != null) {
			bufferedField.setLength(0);
		}
		if (bufferedLine != null) {
			bufferedLine.clear();
		}
	}

	public void setBlockDelimiter(char ch) {
		block_delim = ch;
	}

	public void setConsuming(boolean b) {
		this.consume = b;
	}

	public void setFieldDelimiter(char ch) {
		field_delim = ch;
	}

}
