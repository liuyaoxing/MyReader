package offline.export;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * @author vwpolo
 */
public class FileUtils {

	public static final long ONE_KB = 1024;
	public static final long ONE_MB = ONE_KB * ONE_KB;
	public static final long ONE_GB = ONE_KB * ONE_MB;
	public static final long ONE_TB = ONE_KB * ONE_GB;
	public static final long ONE_PB = ONE_KB * ONE_TB;

	/**
	 * �����ļ��ľ���·����ȡ�ļ�������������չ��
	 *
	 * @param filePath
	 * @return
	 */
	public static String getFileNameNoFormat(String filePath) {
		if (filePath == null || filePath.isEmpty()) {
			return "";
		}
		int point = filePath.lastIndexOf('.');
		if (point == -1)
			return filePath.substring(filePath.lastIndexOf(File.separator) + 1);
		return filePath.substring(filePath.lastIndexOf(File.separator) + 1, point);
	}

	/**
	 * ��ȡ�ļ���չ��
	 *
	 * @param fileName
	 * @return
	 */
	public static String getFileFormat(String fileName) {
		if (fileName == null || fileName.isEmpty())
			return "";

		int point = fileName.lastIndexOf('.');
		return fileName.substring(point + 1);
	}

	/**
	 * ��ȡ�ļ���С
	 *
	 * @param filePath
	 * @return
	 */
	public static long getFileSize(String filePath) {
		long size = 0;

		File file = new File(filePath);
		if (file != null && file.exists()) {
			size = file.length();
		}
		return size;
	}

	/**
	 * ��ȡ�ļ���С
	 *
	 * @param destFolder
	 * @return
	 */
	public static String getFileSize(File destFolder) {
		return getFileSize(getDirSize(destFolder));
	}

	/**
	 * ��ȡ�ļ���С 10.24M
	 *
	 * @param size �ֽ�
	 * @return
	 */
	public static String getFileSize(long size) {
		if (size <= 0)
			return "0";
		java.text.DecimalFormat df = new java.text.DecimalFormat("##.##");
		float temp = (float) size / 1024;
		if (temp >= 1024) {
			return df.format(temp / 1024) + "M";
		} else {
			return df.format(temp) + "K";
		}
	}

	/**
	 * ת���ļ���С
	 *
	 * @param fileS
	 * @return B/KB/MB/GB
	 */
	public static String formatFileSize(long fileS) {
		java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");
		String fileSizeString = "";
		if (fileS < 1024) {
			fileSizeString = df.format((double) fileS) + "B";
		} else if (fileS < 1048576) {
			fileSizeString = df.format((double) fileS / 1024) + "KB";
		} else if (fileS < 1073741824) {
			fileSizeString = df.format((double) fileS / 1048576) + "MB";
		} else {
			fileSizeString = df.format((double) fileS / 1073741824) + "G";
		}
		return fileSizeString;
	}

	/**
	 * ��ȡĿ¼�ļ���С
	 *
	 * @param dir
	 * @return
	 */
	public static long getFileDirSize(File dir) {
		if (dir == null)
			return 0;
		return dir.isDirectory() ? getDirSize(dir) : dir.length();
	}

	/**
	 * ��ȡĿ¼�ļ���С
	 *
	 * @param dir
	 * @return
	 */
	public static long getDirSize(File dir) {
		if (dir == null) {
			return 0;
		}
		if (!dir.isDirectory()) {
			return dir.length();
		}
		long dirSize = 0;
		File[] files = dir.listFiles();
		for (File file : files) {
			if (file.isFile()) {
				dirSize += file.length();
			} else if (file.isDirectory()) {
				dirSize += file.length();
				dirSize += getDirSize(file); // �ݹ���ü���ͳ��
			}
		}
		return dirSize;
	}

	/**
	 * ��ȡĿ¼�ļ�����
	 */
	public static long getFileList(File dir) {
		long count = 0;
		File[] files = dir.listFiles();
		if (files == null)
			return 0;
		count = files.length;
		for (File file : files) {
			if (file.isDirectory()) {
				count = count + getFileList(file);// �ݹ�
				count--;
			}
		}
		return count;
	}

	public static byte[] toBytes(InputStream in) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		int ch;
		while ((ch = in.read()) != -1) {
			out.write(ch);
		}
		byte buffer[] = out.toByteArray();
		out.close();
		return buffer;
	}

	/**
	 * ɾ����Ŀ¼
	 * <p/>
	 * ���� 0����ɹ� ,1 ����û��ɾ��Ȩ��, 2�����ǿ�Ŀ¼,3 ����δ֪����
	 *
	 * @return
	 */
	public static int deleteBlankPath(String path) {
		File f = new File(path);
		if (!f.canWrite()) {
			return 1;
		}
		if (f.list() != null && f.list().length > 0) {
			return 2;
		}
		if (f.delete()) {
			return 0;
		}
		return 3;
	}

	/**
	 * ������
	 *
	 * @param oldName
	 * @param newName
	 * @return
	 */
	public static boolean reNamePath(String oldName, String newName) {
		File f = new File(oldName);
		return f.renameTo(new File(newName));
	}

	/**
	 * ���һ���ļ���
	 */
	public static void clearFileWithPath(String filePath) {
		List<File> files = FileUtils.listPathFiles(filePath);
		if (files.isEmpty()) {
			return;
		}
		for (File f : files) {
			if (f.isDirectory()) {
				clearFileWithPath(f.getAbsolutePath());
			} else {
				f.delete();
			}
		}
	}

	/**
	 * �г�rootĿ¼��������Ŀ¼
	 */
	public static List<String> listPath(String root) {
		List<String> allDir = new ArrayList<String>();
		SecurityManager checker = new SecurityManager();
		File path = new File(root);
		checker.checkRead(root);
		// ���˵���.��ʼ���ļ���
		if (path.isDirectory()) {
			for (File f : path.listFiles()) {
				if (f.isDirectory() && !f.getName().startsWith(".")) {
					allDir.add(f.getAbsolutePath());
				}
			}
		}
		return allDir;
	}

	/**
	 * ��ȡһ���ļ����µ������ļ�
	 *
	 * @param root
	 * @return
	 */
	public static List<File> listPathFiles(String root) {
		List<File> allDir = new ArrayList<File>();
		SecurityManager checker = new SecurityManager();
		File path = new File(root);
		checker.checkRead(root);
		File[] files = path.listFiles();
		for (File f : files) {
			if (f.isFile())
				allDir.add(f);
			else
				listPath(f.getAbsolutePath());
		}
		return allDir;
	}

	public enum PathStatus {
		SUCCESS, EXITS, ERROR
	}

	/**
	 * ����Ŀ¼
	 */
	public static PathStatus createPath(String newPath) {
		File path = new File(newPath);
		if (path.exists()) {
			return PathStatus.EXITS;
		}
		if (path.mkdir()) {
			return PathStatus.SUCCESS;
		} else {
			return PathStatus.ERROR;
		}
	}

	/**
	 * ��ȡ·����
	 *
	 * @return
	 */
	public static String getPathName(String absolutePath) {
		int start = absolutePath.lastIndexOf(File.separator) + 1;
		int end = absolutePath.length();
		return absolutePath.substring(start, end);
	}

	public static Set<File> listFiles(File srcDir) throws IOException {
		Set<File> fileSet = new LinkedHashSet<>();
		listFiles(srcDir, fileSet);
		return fileSet;
	}

	public static void listFiles(File srcDir, Set<File> fileSet) throws IOException {
		File[] files = srcDir.listFiles();
		if (files == null)
			return;
		for (int i = 0; i < files.length; i++) {
			if (files[i].isDirectory())
				listFiles(files[i], fileSet);
			else
				fileSet.add(files[i]);
		}
	}

	public static boolean isImageFile(File srcFile) {
		if (srcFile == null || !srcFile.isFile())
			return false;
		String fileFormat = getFileFormat(srcFile.getName());
		if (fileFormat == null || fileFormat.length() == 0)
			return false;
		return fileFormat.toUpperCase(Locale.getDefault())
				.matches("^[(JPG)|(JPGX)|(PNG)|(PNGX)|(GIF)|(GIFX)|(JPEG)|(JPEGX)|(BMP)|(BMPX)]+$");
	}

	public static boolean isVideoFile(File srcFile) {
		if (srcFile == null || !srcFile.isFile())
			return false;
		String fileFormat = getFileFormat(srcFile.getName());
		if (fileFormat == null || fileFormat.length() == 0)
			return false;
		return fileFormat.toUpperCase(Locale.getDefault()).matches("^[(MP4)|(AVI)]+$");
	}

	public static boolean isMusicFile(File srcFile) {
		if (srcFile == null || !srcFile.isFile())
			return false;
		String fileFormat = getFileFormat(srcFile.getName());
		if (fileFormat == null || fileFormat.length() == 0)
			return false;
		return fileFormat.toUpperCase(Locale.getDefault()).matches("^[(MP3)|(FLAC)]+$");
	}

	public static boolean isApkFile(File srcFile) {
		if (srcFile == null || !srcFile.isFile())
			return false;
		String fileFormat = getFileFormat(srcFile.getName());
		if (fileFormat == null || fileFormat.length() == 0)
			return false;
		return fileFormat.toUpperCase(Locale.getDefault()).matches("^[(APK)]+$");
	}
}