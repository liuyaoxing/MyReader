package com.liuyx.common.csv;

import java.util.List;

public interface CsvListener {
	void handleCsvLine(CsvParser parser, List<String> line);
}
