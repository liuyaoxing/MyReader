package offline.export.utils;

import com.liuyx.common.csv.CsvUtil;

import org.apache.commons.codec.ext.DigestUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class MD5Utils {

	public static final long ONE_MB = 1048576L;

	public static String encrypt(String source) {
		String target = "";
		if (source == null)
			source = "";

		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(source.getBytes());
			byte b[] = md.digest();

			int i;

			StringBuffer buf = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}
			target = buf.toString();

		} catch (NoSuchAlgorithmException e) {
		}

		return target;
	}

	public static String encryptFile(File source) throws IOException {
		FileInputStream fis = null;
		try {
			return DigestUtils.md5Hex(fis = new FileInputStream(source));
		} finally {
			IOUtils.closeQuietly(fis);
		}
	}

	public static String encryptFileFast(File file) throws IOException {
		if (file.isFile() && file.length() < ONE_MB * 20)
			return encryptFile(file);
		Map<String, String> fileMap = new HashMap<>();
		fileMap.put("FILENAME", file.getName());
		fileMap.put("LENGTH", String.valueOf(file.length()));
		fileMap.put("PATH", file.getPath());
		fileMap.put("LASTMODIFIED", String.valueOf(file.lastModified()));
		return MD5Utils.encrypt(CsvUtil.mapToCsv(fileMap));
	}

	public static String encryptFile(InputStream source) throws IOException {
		return DigestUtils.md5Hex(source);
	}
}