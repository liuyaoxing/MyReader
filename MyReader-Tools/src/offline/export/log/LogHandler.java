package offline.export.log;

import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

/**
 *
 * @author 刘尧兴 (https://github.com/liuyaoxing)
 *
 */
public class LogHandler {

	public static String TAG = "LogHandler";

	static {
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(getLogFile(), true);
			PrintStream printStream = new PrintStream(fileOutputStream, false, "GBK");
			System.setOut(printStream);
			System.setErr(printStream);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static File getLogFile() {
		String fileName = String.format("MyReader.console.%s.log", new SimpleDateFormat("yyyyMMdd").format(new Date()));
		File f = new File(fileName);
		try {
			if (!f.exists())
				f.createNewFile();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		File[] listFiles = f.getParentFile().listFiles(new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.getName().startsWith("MyReader.console.");
			}
		});
		if (listFiles != null && listFiles.length > 10) {
			Arrays.sort(listFiles, new ComparatorFactory.CompratorByFileTime());
			for (int i = 3; i < listFiles.length; i++) {
				listFiles[i].delete();
			}
		}
		return f;
	}

	/**
	 * 保存错误信息到文件中
	 * 
	 * @param message
	 * @return 返回文件名称,便于将文件传送到服务器
	 * @throws Exception
	 */
	public static void debug(String message) {
		StringBuffer sb = new StringBuffer();
		try {
			SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
			String date = sDateFormat.format(new java.util.Date());
			sb.append("\r\n" + date + " == ");
			sb.append(message);
			writeFile(sb.toString());
		} catch (Exception e) {
			sb.append("an error occured while writing file...\r\n");
			writeFile(sb.toString());
		}
	}

	private static void writeFile(String sb) {
		try {
			String fileName = "myreader.log";
			File logsFile = getLogFile();
			if (!logsFile.exists()) {
				logsFile.mkdirs();
			}
			FileOutputStream fos = new FileOutputStream(new File(logsFile, fileName), true);
			fos.write(sb.getBytes("GBK"));
			fos.flush();
			fos.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
}
