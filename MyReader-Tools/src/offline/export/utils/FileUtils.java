package offline.export.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class FileUtils {

	public static final long ONE_KB = 1024;
	public static final long ONE_MB = ONE_KB * ONE_KB;
	public static final long ONE_GB = ONE_KB * ONE_MB;
	public static final long ONE_TB = ONE_KB * ONE_GB;
	public static final long ONE_PB = ONE_KB * ONE_TB;

	/**
	 * Delete a file. If file is a directory, delete it and all sub-directories.
	 * <p>
	 * The difference between File.delete() and this method are:
	 * <ul>
	 * <li>A directory to be deleted does not have to be empty.</li>
	 * <li>You get exceptions when a file or directory cannot be deleted.
	 * (java.io.File methods returns a boolean)</li>
	 * </ul>
	 *
	 * @param file
	 *            file or directory to delete, must not be <code>null</code>
	 * @throws NullPointerException
	 *             if the directory is <code>null</code>
	 * @throws IOException
	 *             in case deletion is unsuccessful
	 */
	public static void forceDelete(File file) throws IOException {
		if (file.isDirectory()) {
			deleteDirectory(file);
		} else {
			if (!file.exists()) {
				return;
			}
			if (!file.delete()) {
				String message = "Unable to delete file: " + file;
				throw new IOException(message);
			}
		}
	}

	/**
	 * Recursively delete a directory.
	 *
	 * @param directory
	 *            directory to delete
	 * @throws IOException
	 *             in case deletion is unsuccessful
	 */
	public static void deleteDirectory(File directory) throws IOException {
		if (!directory.exists())
			return;

		cleanDirectory(directory);
		if (!directory.delete()) {
			String message = "Unable to delete directory " + directory + ".";
			throw new IOException(message);
		}
	}

	/**
	 * Clean a directory without deleting it.
	 *
	 * @param directory
	 *            directory to clean
	 * @throws IOException
	 *             in case cleaning is unsuccessful
	 */
	public static void cleanDirectory(File directory) throws IOException {
		if (!directory.exists()) {
			String message = directory + " does not exist";
			throw new IllegalArgumentException(message);
		}

		if (!directory.isDirectory()) {
			String message = directory + " is not a directory";
			throw new IllegalArgumentException(message);
		}

		File[] files = directory.listFiles();
		if (files == null) { // null if security restricted
			throw new IOException("Failed to list contents of " + directory);
		}

		IOException exception = null;
		for (int i = 0; i < files.length; i++) {
			File file = files[i];
			try {
				forceDelete(file);
			} catch (IOException ioe) {
				exception = ioe;
			}
		}

		if (null != exception) {
			throw exception;
		}
	}

	/**
	 * 根据文件的绝对路径获取文件名但不包含扩展名
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
	 * 获取文件扩展名
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
	 * 获取文件大小
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
	 * 获取文件大小
	 *
	 * @param destFolder
	 * @return
	 */
	public static String getFileSize(File destFolder) {
		return getFileSize(getDirSize(destFolder));
	}

	/**
	 * 获取文件大小 10.24M
	 *
	 * @param size
	 *            字节
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
	 * 转换文件大小
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
	 * 获取目录文件大小
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
	 * 获取目录文件大小
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
				dirSize += getDirSize(file); // 递归调用继续统计
			}
		}
		return dirSize;
	}

	/**
	 * 获取目录文件个数
	 */
	public static long getFileList(File dir) {
		long count = 0;
		File[] files = dir.listFiles();
		if (files == null)
			return 0;
		count = files.length;
		for (File file : files) {
			if (file.isDirectory()) {
				count = count + getFileList(file);// 递归
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
	 * 删除空目录
	 * <p/>
	 * 返回 0代表成功 ,1 代表没有删除权限, 2代表不是空目录,3 代表未知错误
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
	 * 重命名
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
	 * 清空一个文件夹
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
	 * 列出root目录下所有子目录
	 */
	public static List<String> listPath(String root) {
		List<String> allDir = new ArrayList<String>();
		// SecurityManager checker = new SecurityManager();
		File path = new File(root);
		// checker.checkRead(root);
		// 过滤掉以.开始的文件夹
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
	 * 获取一个文件夹下的所有文件
	 *
	 * @param root
	 * @return
	 */
	public static List<File> listPathFiles(String root) {
		List<File> allDir = new ArrayList<File>();
		// SecurityManager checker = new SecurityManager();
		File path = new File(root);
		// checker.checkRead(root);
		File[] files = path.listFiles();
		for (File f : files) {
			if (f.isFile())
				allDir.add(f);
			else
				allDir.addAll(listPathFiles(f.getAbsolutePath()));
		}
		return allDir;
	}

	public enum PathStatus {
		SUCCESS, EXITS, ERROR
	}

	/**
	 * 创建目录
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
	 * 截取路径名
	 *
	 * @return
	 */
	public static String getPathName(String absolutePath) {
		int start = absolutePath.lastIndexOf(File.separator) + 1;
		int end = absolutePath.length();
		return absolutePath.substring(start, end);
	}

	public static Set<File> listFiles(File srcDir) throws IOException {
		Set<File> fileSet = new LinkedHashSet<File>();
		listFiles(srcDir, fileSet);
		return fileSet;
	}

	public static void listFiles(File srcDir, Set<File> fileSet) throws IOException {
		File[] files = srcDir.listFiles();
		if (files == null)
			return;
		for (int i = 0; i < files.length; i++) {
			if (files[i].isHidden())
				continue;
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
		return fileFormat.toUpperCase(Locale.getDefault()).matches("^[(JPG)|(JPGX)|(PNG)|(PNGX)|(GIF)|(GIFX)|(JPEG)|(JPEGX)|(BMP)|(BMPX)]+$");
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

	/**
	 * Compare the contents of two files to determine if they are equal or not.
	 * <p>
	 * This method checks to see if the two files are different lengths or if
	 * they point to the same file, before resorting to byte-by-byte comparison
	 * of the contents.
	 * <p>
	 * Code origin: Avalon
	 *
	 * @param file1
	 *            the first file
	 * @param file2
	 *            the second file
	 * @return true if the content of the files are equal or they both don't
	 *         exist, false otherwise
	 * @throws IOException
	 *             in case of an I/O error
	 */
	public static boolean contentEquals(File file1, File file2) throws IOException {
		boolean file1Exists = file1.exists();
		if (file1Exists != file2.exists()) {
			return false;
		}

		if (!file1Exists) {
			// two not existing files are equal
			return true;
		}

		if (file1.isDirectory() || file2.isDirectory()) {
			// don't want to compare directory contents
			throw new IOException("Can't compare directories, only files");
		}

		if (file1.length() != file2.length()) {
			// lengths differ, cannot be equal
			return false;
		}

		if (file1.getCanonicalFile().equals(file2.getCanonicalFile())) {
			// same file
			return true;
		}

		InputStream input1 = null;
		InputStream input2 = null;
		try {
			input1 = new FileInputStream(file1);
			input2 = new FileInputStream(file2);
			return IOUtils.contentEquals(input1, input2);

		} finally {
			IOUtils.closeQuietly(input1);
			IOUtils.closeQuietly(input2);
		}
	}

	/**
	 * Copies a directory to within another directory preserving the file dates.
	 * <p>
	 * This method copies the source directory and all its contents to a
	 * directory of the same name in the specified destination directory.
	 * <p>
	 * The destination directory is created if it does not exist. If the
	 * destination directory did exist, then this method merges the source with
	 * the destination, with the source taking precedence.
	 *
	 * @param srcDir
	 *            an existing directory to copy, must not be <code>null</code>
	 * @param destDir
	 *            the directory to place the copy in, must not be
	 *            <code>null</code>
	 *
	 * @throws NullPointerException
	 *             if source or destination is <code>null</code>
	 * @throws IOException
	 *             if source or destination is invalid
	 * @throws IOException
	 *             if an IO error occurs during copying
	 * @since Commons IO 1.2
	 */
	public static void copyDirectoryToDirectory(File srcDir, File destDir) throws IOException {
		if (srcDir == null) {
			throw new NullPointerException("Source must not be null");
		}
		if (srcDir.exists() && srcDir.isDirectory() == false) {
			throw new IllegalArgumentException("Source '" + destDir + "' is not a directory");
		}
		if (destDir == null) {
			throw new NullPointerException("Destination must not be null");
		}
		if (destDir.exists() && destDir.isDirectory() == false) {
			throw new IllegalArgumentException("Destination '" + destDir + "' is not a directory");
		}
		copyDirectory(srcDir, new File(destDir, srcDir.getName()), true);
	}

	/**
	 * Copies a whole directory to a new location preserving the file dates.
	 * <p>
	 * This method copies the specified directory and all its child directories
	 * and files to the specified destination. The destination is the new
	 * location and name of the directory.
	 * <p>
	 * The destination directory is created if it does not exist. If the
	 * destination directory did exist, then this method merges the source with
	 * the destination, with the source taking precedence.
	 *
	 * @param srcDir
	 *            an existing directory to copy, must not be <code>null</code>
	 * @param destDir
	 *            the new directory, must not be <code>null</code>
	 *
	 * @throws NullPointerException
	 *             if source or destination is <code>null</code>
	 * @throws IOException
	 *             if source or destination is invalid
	 * @throws IOException
	 *             if an IO error occurs during copying
	 * @since Commons IO 1.1
	 */
	public static void copyDirectory(File srcDir, File destDir) throws IOException {
		copyDirectory(srcDir, destDir, true);
	}

	/**
	 * Copies a whole directory to a new location.
	 * <p>
	 * This method copies the contents of the specified source directory to
	 * within the specified destination directory.
	 * <p>
	 * The destination directory is created if it does not exist. If the
	 * destination directory did exist, then this method merges the source with
	 * the destination, with the source taking precedence.
	 *
	 * @param srcDir
	 *            an existing directory to copy, must not be <code>null</code>
	 * @param destDir
	 *            the new directory, must not be <code>null</code>
	 * @param preserveFileDate
	 *            true if the file date of the copy should be the same as the
	 *            original
	 *
	 * @throws NullPointerException
	 *             if source or destination is <code>null</code>
	 * @throws IOException
	 *             if source or destination is invalid
	 * @throws IOException
	 *             if an IO error occurs during copying
	 * @since Commons IO 1.1
	 */
	public static void copyDirectory(File srcDir, File destDir, boolean preserveFileDate) throws IOException {
		if (srcDir == null) {
			throw new NullPointerException("Source must not be null");
		}
		if (destDir == null) {
			throw new NullPointerException("Destination must not be null");
		}
		if (srcDir.exists() == false) {
			throw new FileNotFoundException("Source '" + srcDir + "' does not exist");
		}
		if (srcDir.isDirectory() == false) {
			throw new IOException("Source '" + srcDir + "' exists but is not a directory");
		}
		if (srcDir.getCanonicalPath().equals(destDir.getCanonicalPath())) {
			throw new IOException("Source '" + srcDir + "' and destination '" + destDir + "' are the same");
		}
		doCopyDirectory(srcDir, destDir, preserveFileDate);
	}

	/**
	 * Internal copy directory method.
	 * 
	 * @param srcDir
	 *            the validated source directory, must not be <code>null</code>
	 * @param destDir
	 *            the validated destination directory, must not be
	 *            <code>null</code>
	 * @param preserveFileDate
	 *            whether to preserve the file date
	 * @throws IOException
	 *             if an error occurs
	 * @since Commons IO 1.1
	 */
	private static void doCopyDirectory(File srcDir, File destDir, boolean preserveFileDate) throws IOException {
		if (destDir.exists()) {
			if (destDir.isDirectory() == false) {
				throw new IOException("Destination '" + destDir + "' exists but is not a directory");
			}
		} else {
			if (destDir.mkdirs() == false) {
				throw new IOException("Destination '" + destDir + "' directory cannot be created");
			}
			if (preserveFileDate) {
				destDir.setLastModified(srcDir.lastModified());
			}
		}
		if (destDir.canWrite() == false) {
			throw new IOException("Destination '" + destDir + "' cannot be written to");
		}
		// recurse
		File[] files = srcDir.listFiles();
		if (files == null) { // null if security restricted
			throw new IOException("Failed to list contents of " + srcDir);
		}
		for (int i = 0; i < files.length; i++) {
			File copiedFile = new File(destDir, files[i].getName());
			if (files[i].isDirectory()) {
				doCopyDirectory(files[i], copiedFile, preserveFileDate);
			} else {
				doCopyFile(files[i], copiedFile, preserveFileDate);
			}
		}
	}

	/**
	 * Internal copy file method.
	 * 
	 * @param srcFile
	 *            the validated source file, must not be <code>null</code>
	 * @param destFile
	 *            the validated destination file, must not be <code>null</code>
	 * @param preserveFileDate
	 *            whether to preserve the file date
	 * @throws IOException
	 *             if an error occurs
	 */
	private static void doCopyFile(File srcFile, File destFile, boolean preserveFileDate) throws IOException {
		if (destFile.exists() && destFile.isDirectory()) {
			throw new IOException("Destination '" + destFile + "' exists but is a directory");
		}

		FileInputStream input = new FileInputStream(srcFile);
		try {
			FileOutputStream output = new FileOutputStream(destFile);
			try {
				IOUtils.copy(input, output);
			} finally {
				IOUtils.closeQuietly(output);
			}
		} finally {
			IOUtils.closeQuietly(input);
		}

		if (srcFile.length() != destFile.length()) {
			throw new IOException("Failed to copy full contents from '" + srcFile + "' to '" + destFile + "'");
		}
		if (preserveFileDate) {
			destFile.setLastModified(srcFile.lastModified());
		}
	}

	/**
	 * Copies a file to a new location preserving the file date.
	 * <p>
	 * This method copies the contents of the specified source file to the
	 * specified destination file. The directory holding the destination file is
	 * created if it does not exist. If the destination file exists, then this
	 * method will overwrite it.
	 * 
	 * @param srcFile
	 *            an existing file to copy, must not be <code>null</code>
	 * @param destFile
	 *            the new file, must not be <code>null</code>
	 * 
	 * @throws NullPointerException
	 *             if source or destination is <code>null</code>
	 * @throws IOException
	 *             if source or destination is invalid
	 * @throws IOException
	 *             if an IO error occurs during copying
	 * @see #copyFileToDirectory(File, File)
	 */
	public static void copyFile(File srcFile, File destFile) throws IOException {
		copyFile(srcFile, destFile, true);
	}

	/**
	 * Copies a file to a new location.
	 * <p>
	 * This method copies the contents of the specified source file to the
	 * specified destination file. The directory holding the destination file is
	 * created if it does not exist. If the destination file exists, then this
	 * method will overwrite it.
	 *
	 * @param srcFile
	 *            an existing file to copy, must not be <code>null</code>
	 * @param destFile
	 *            the new file, must not be <code>null</code>
	 * @param preserveFileDate
	 *            true if the file date of the copy should be the same as the
	 *            original
	 *
	 * @throws NullPointerException
	 *             if source or destination is <code>null</code>
	 * @throws IOException
	 *             if source or destination is invalid
	 * @throws IOException
	 *             if an IO error occurs during copying
	 * @see #copyFileToDirectory(File, File, boolean)
	 */
	public static void copyFile(File srcFile, File destFile, boolean preserveFileDate) throws IOException {
		if (srcFile == null) {
			throw new NullPointerException("Source must not be null");
		}
		if (destFile == null) {
			throw new NullPointerException("Destination must not be null");
		}
		if (srcFile.exists() == false) {
			throw new FileNotFoundException("Source '" + srcFile + "' does not exist");
		}
		if (srcFile.isDirectory()) {
			throw new IOException("Source '" + srcFile + "' exists but is a directory");
		}
		if (srcFile.getCanonicalPath().equals(destFile.getCanonicalPath())) {
			throw new IOException("Source '" + srcFile + "' and destination '" + destFile + "' are the same");
		}
		if (destFile.getParentFile() != null && destFile.getParentFile().exists() == false) {
			if (destFile.getParentFile().mkdirs() == false) {
				throw new IOException("Destination '" + destFile + "' directory cannot be created");
			}
		}
		if (destFile.exists() && destFile.canWrite() == false) {
			throw new IOException("Destination '" + destFile + "' exists but is read-only");
		}
		doCopyFile(srcFile, destFile, preserveFileDate);
	}

	/**
	 * Reads the contents of a file into a String. The file is always closed.
	 *
	 * @param file
	 *            the file to read, must not be <code>null</code>
	 * @param encoding
	 *            the encoding to use, <code>null</code> means platform default
	 * @return the file contents, never <code>null</code>
	 * @throws IOException
	 *             in case of an I/O error
	 * @throws java.io.UnsupportedEncodingException
	 *             if the encoding is not supported by the VM
	 */
	public static String readFileToString(File file, String encoding) throws IOException {
		InputStream in = null;
		try {
			in = openInputStream(file);
			return IOUtils.toString(in, encoding);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	/**
	 * Reads the contents of a file into a String using the default encoding for
	 * the VM. The file is always closed.
	 *
	 * @param file
	 *            the file to read, must not be <code>null</code>
	 * @return the file contents, never <code>null</code>
	 * @throws IOException
	 *             in case of an I/O error
	 * @since Commons IO 1.3
	 */
	public static String readFileToString(File file) throws IOException {
		return readFileToString(file, null);
	}

	/**
	 * Reads the contents of a file into a byte array. The file is always
	 * closed.
	 *
	 * @param file
	 *            the file to read, must not be <code>null</code>
	 * @return the file contents, never <code>null</code>
	 * @throws IOException
	 *             in case of an I/O error
	 * @since Commons IO 1.1
	 */
	public static byte[] readFileToByteArray(File file) throws IOException {
		InputStream in = null;
		try {
			in = openInputStream(file);
			return IOUtils.toByteArray(in);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	/**
	 * Opens a {@link FileInputStream} for the specified file, providing better
	 * error messages than simply calling
	 * <code>new FileInputStream(file)</code>.
	 * <p>
	 * At the end of the method either the stream will be successfully opened,
	 * or an exception will have been thrown.
	 * <p>
	 * An exception is thrown if the file does not exist. An exception is thrown
	 * if the file object exists but is a directory. An exception is thrown if
	 * the file exists but cannot be read.
	 * 
	 * @param file
	 *            the file to open for input, must not be <code>null</code>
	 * @return a new {@link FileInputStream} for the specified file
	 * @throws FileNotFoundException
	 *             if the file does not exist
	 * @throws IOException
	 *             if the file object is a directory
	 * @throws IOException
	 *             if the file cannot be read
	 * @since Commons IO 1.3
	 */
	public static FileInputStream openInputStream(File file) throws IOException {
		if (file.exists()) {
			if (file.isDirectory()) {
				throw new IOException("File '" + file + "' exists but is a directory");
			}
			if (file.canRead() == false) {
				throw new IOException("File '" + file + "' cannot be read");
			}
		} else {
			throw new FileNotFoundException("File '" + file + "' does not exist");
		}
		return new FileInputStream(file);
	}
}
