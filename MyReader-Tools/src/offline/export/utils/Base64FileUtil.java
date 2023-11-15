package offline.export.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.liuyx.common.csv.CsvUtil;

import offline.export.FileUtils;
import offline.export.config.Configuration;
import sun.misc.BASE64Decoder;

public class Base64FileUtil {

	public static void main(String[] args) throws Exception {
//		File srcFile = new File("D:\\Jobs\\市场支持\\华润银行\\AB.docx");
		File srcFile = new File("D:\\Jobs\\市场支持\\华润银行\\ABC.txt");
//		File srcFile = new File("C:\\Users\\liuyaoxing\\Desktop\\plugins\\ESB改造计划ESB改造计划ESB改造计划ESB改造计划ESB改造计划.xlsx");

		String fileStr = getFileStr(srcFile.getCanonicalPath());
		System.out.println(generateFile(srcFile, fileStr));
	}

	public static String generateFile(File srcFile, String fileStr) throws Exception {
		int CAPACITY = Configuration.getInstance().getQrCodeCapacity();
		String fileMD5 = MD5Utils.encryptFile(srcFile);
		StringBuffer source = new StringBuffer(fileStr);
		String newFileName = "temp/" + srcFile.getName() + ".v" + new SimpleDateFormat("MMddHHmm").format(new Date());
		File srcDir = new File(srcFile.getParentFile().getCanonicalPath(), newFileName);
		srcDir.mkdir();
		List<String> segmentList = new ArrayList<String>();
		while (source.length() > 0) {
			String segment = source.length() >= CAPACITY ? source.substring(0, CAPACITY) : source.toString();
			segmentList.add(segment);
			source = source.length() >= CAPACITY ? source.delete(0, CAPACITY) : source.delete(0, source.length());
		}
		for (int i = 0; i < segmentList.size(); i++) {
			Map<String, String> fileMeta = new LinkedHashMap<String, String>();
			fileMeta.put("name", FileUtils.getFileNameNoFormat(srcFile.getName()));
			fileMeta.put("md5", fileMD5);
			fileMeta.put("total", String.valueOf(segmentList.size()));
			fileMeta.put("index", String.valueOf(i));
			fileMeta.put("ext", FileUtils.getFileFormat(srcFile.getName()));
			fileMeta.put("length", String.valueOf(srcFile.length()));
			fileMeta.put("capacity", String.valueOf(segmentList.get(i).length()));
			fileMeta.put("content", segmentList.get(i));
			String fileName = String.format("%s[%s-%s]", FileUtils.getFileNameNoFormat(srcFile.getName()), segmentList.size(), i + 1);
			QRCodeUtil.encode(CsvUtil.mapToCsv(fileMeta), null, srcDir.getCanonicalPath(), fileName, true);
		}
		return srcDir.getCanonicalPath();
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
		return new String(Base64.encodeBase64(data));
//		// 对字节数组Base64编码
//		BASE64Encoder encoder = new BASE64Encoder();
//		// 返回 Base64 编码过的字节数组字符串
//		return encoder.encode(data);
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