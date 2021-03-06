package offline.export.log;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComparatorFactory {

	public static class WindowsExplorerComparator implements Comparator<File> {

		private static final Pattern splitPattern = Pattern.compile("\\d+|\\.|\\s");

		@Override
		public int compare(File f1, File f2) {
			Iterator<String> iter1 = splitStringPreserveDelimiter(f1.getName()).iterator();
			Iterator<String> iter2 = splitStringPreserveDelimiter(f2.getName()).iterator();
			while (true) {
				if (!iter1.hasNext() && !iter2.hasNext()) {
					return 0;
				}
				if (!iter1.hasNext() && iter2.hasNext()) {
					return -1;
				}
				if (iter1.hasNext() && !iter2.hasNext()) {
					return 1;
				}

				String data1 = iter1.next();
				String data2 = iter2.next();
				int result;
				try {
					result = compareLong(Long.valueOf(data1), Long.valueOf(data2));
					if (result == 0) {
						result = -compareInteger(data1.length(), data2.length());
					}
				} catch (NumberFormatException ex) {
					result = data1.compareToIgnoreCase(data2);
				}

				if (result != 0) {
					return result;
				}
			}
		}

		public static int compareLong(long x, long y) {
			return (x < y) ? -1 : ((x == y) ? 0 : 1);
		}

		public static int compareInteger(int x, int y) {
			return (x < y) ? -1 : ((x == y) ? 0 : 1);
		}

		private List<String> splitStringPreserveDelimiter(String str) {
			Matcher matcher = splitPattern.matcher(str);
			List<String> list = new ArrayList<String>();
			int pos = 0;
			while (matcher.find()) {
				list.add(str.substring(pos, matcher.start()));
				list.add(matcher.group());
				pos = matcher.end();
			}
			list.add(str.substring(pos));
			return list;
		}
	}

	/**
	 * 按 文件修改时间排序（从新到旧）
	 */
	public static class CompratorByFileTime implements Comparator<File> {
		@Override
		public int compare(File file1, File file2) {
			int diff = 0;
			try {
				diff = Long.valueOf(file1.lastModified()).compareTo(file2.lastModified());
			} catch (NullPointerException e) {
				diff = -1;
			}
			if (diff < 0) {
				return 1;
			} else if (diff == 0) {
				return 0;
			} else {
				return -1;
			}
		}
	}
}
