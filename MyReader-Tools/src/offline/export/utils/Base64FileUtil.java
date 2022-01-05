package offline.export.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import com.liuyx.common.csv.CsvUtil;

import offline.export.FileUtils;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class Base64FileUtil {

	public static final int CAPACITY = 1000;

	public static void main(String[] args) throws Exception {
//		File srcFile = new File("D:\\Jobs\\市场支持\\华润银行\\AB.docx");
		File srcFile = new File("D:\\Jobs\\市场支持\\华润银行\\ABC.txt");
		
		String fileStr = getFileStr(srcFile.getCanonicalPath());
		System.out.println(generateFile(srcFile, fileStr));
	}

	private static String generateFile(File srcFile, String fileStr) throws Exception {
		int segments = (int) (srcFile.length() / CAPACITY) + (srcFile.length() % CAPACITY > 0 ? 1 : 0);
		String fileMD5 = MD5Utils.encryptFile(srcFile);
		StringBuffer source = new StringBuffer(fileStr);
		File srcDir = new File(System.getProperty("user.dir") + File.separator + "temp", FileUtils.getFileNameNoFormat(srcFile.getName()));
		srcDir.mkdir();
		for (int i = 0; i < segments; i++) {
			String segment = source.length() > CAPACITY ? source.substring(0, CAPACITY) : source.toString();
			System.out.println(segment.length() + "\n" + segment);
			String fileName = String.format("%s[%s-%s]", FileUtils.getFileNameNoFormat(srcFile.getName()), segments, i + 1);
			Map<String, String> fileMeta = new LinkedHashMap<String, String>();
			fileMeta.put("name", fileName);
			fileMeta.put("md5", fileMD5);
			fileMeta.put("total", String.valueOf(segments));
			fileMeta.put("index", String.valueOf(i));
			fileMeta.put("ext", FileUtils.getFileFormat(srcFile.getName()));
			fileMeta.put("length", String.valueOf(srcFile.length()));
			fileMeta.put("capacity", String.valueOf(segment.length()));
			fileMeta.put("content", segment);
			QRCodeUtil.encode(CsvUtil.mapToCsv(fileMeta), null, srcDir.getCanonicalPath(), fileName, true);
			source = source.length() > CAPACITY ? source.delete(0, CAPACITY) : source.delete(0, source.length());
		}
		return "";
	}

	/**
	 * * 文件转化成base64字符串 * 将文件转化为字节数组字符串，并对其进行Base64编码处理
	 */
	public static String getFileStr(String filePath) {
		InputStream in = null;
		byte[] data = null;
		// 读取文件字节数组
		try {
			in = new FileInputStream(filePath);
			data = new byte[in.available()];
			in.read(data);
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// 对字节数组Base64编码
		BASE64Encoder encoder = new BASE64Encoder();
		// 返回 Base64 编码过的字节数组字符串
		return encoder.encode(data);
	}

	/**
	 * base64字符串转化成文件，可以是JPEG、PNG、TXT和AVI等等
	 * 
	 * @param base64FileStr
	 * @param filePath
	 * @return
	 * @throws Exception
	 */
	public static boolean generateFile(String base64FileStr, String filePath) throws Exception {
		// 数据为空
		if (base64FileStr == null) {
			System.out.println(" 不行，oops！ ");
			return false;
		}
		BASE64Decoder decoder = new BASE64Decoder();

		// Base64解码,对字节数组字符串进行Base64解码并生成文件
		byte[] byt = decoder.decodeBuffer(base64FileStr);
		for (int i = 0, len = byt.length; i < len; ++i) {
			// 调整异常数据
			if (byt[i] < 0) {
				byt[i] += 256;
			}
		}
		OutputStream out = null;
		InputStream input = new ByteArrayInputStream(byt);
		try {
			// 生成指定格式的文件
			out = new FileOutputStream(filePath);
			byte[] buff = new byte[1024];
			int len = 0;
			while ((len = input.read(buff)) != -1) {
				out.write(buff, 0, len);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			out.flush();
			out.close();
		}
		return true;
	}
}