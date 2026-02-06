package offline.export.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

	public static final String[] EMPTY_STRING = new String[0];

	/**
	 * 判断给定字符串是否空白串。<br>
	 * 空白串是指由空格、制表符、回车符、换行符组成的字符串<br>
	 * 若输入字符串为null或空字符串，返回true
	 *
	 * @param input
	 * @return boolean
	 */
	public static boolean isBlank(String input) {
		if (input == null || "".equals(input))
			return true;

		for (int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
				return false;
			}
		}
		return true;
	}

	/**
	 * 函数名称：填充字符串
	 * <p>
	 * 函数功能：用指定字符串填充另外一个字符串，用字符iChar填充满字符串sBufData到isBufLen位，isBufLen为负数左靠齐，
	 * isBufLen为正数右靠齐
	 *
	 * @param sBufData
	 *            被填充的字符串
	 * @param iChar
	 *            填充字符
	 * @param isBufLen
	 *            填充长度，为负数左靠齐，为正数右靠齐
	 * @return 填充后的字符串
	 *         <p>
	 *         编写时间：2010-8-25 上午09:13:36 <br>
	 *         修改人： (函数的修改者) <br>
	 *         修改时间：(函数的修改时间，与上面的修改人相对应。) <br>
	 *         函数备注：
	 */
	public static String fixFill(String sBufData, String iChar, int isBufLen) {
		assert (sBufData != null);
		assert (iChar != null);

		String sRetMsg;
		byte bObjData[];
		byte bBufData[];
		try {
			bBufData = sBufData.getBytes("GBK");
		} catch (UnsupportedEncodingException e) {
			bBufData = sBufData.getBytes();
		}
		byte bCharData[];
		try {
			bCharData = iChar.getBytes("GBK");
		} catch (UnsupportedEncodingException e) {
			bCharData = iChar.getBytes();
		}
		int iLen;

		int sBufDataLen = bBufData.length;
		if (isBufLen < 0) {
			iLen = 0 - isBufLen;
			bObjData = new byte[iLen];
			if (sBufDataLen > iLen) {
				sBufDataLen = iLen;
			}
		} else {
			iLen = isBufLen;
			bObjData = new byte[isBufLen];
			if (sBufDataLen > iLen) {
				int iStart = sBufDataLen - iLen;
				for (int i = 0; i < iLen; i++) {
					bBufData[i] = bBufData[i + iStart];
				}
				sBufDataLen = iLen;
			}
		}
		if (isBufLen < 0) {
			for (int i = 0; i < sBufDataLen; i++) {
				bObjData[i] = bBufData[i];
			}
			for (int i = sBufDataLen; i < iLen; i++) {
				bObjData[i] = bCharData[0];
			}
		} else {
			int iStart = isBufLen - sBufDataLen;
			for (int i = 0; i < iStart; i++) {
				bObjData[i] = bCharData[0];
			}
			for (int i = 0; i < sBufDataLen; i++) {
				bObjData[iStart + i] = bBufData[i];
			}
		}
		/*
		 * if (isBufLen < 0) { bObjData = hzFixFill(bObjData, iLen); }
		 */
		sRetMsg = new String(bObjData);
		return sRetMsg;
	}

	public static String ifBlank(String input, String def) {
		return isBlank(input) ? def : input;
	}

	/**
	 * 去除字符串中所有全角字符。
	 *
	 * @param str
	 * @return
	 */
	public static String trimBlankSpaces(String str) {
		return str == null ? "" : str.replaceAll("([\\s\\p{Zs}]*)+", "");
	}

	/**
	 * @param url
	 *            "http://www.lizclimo.tumblr.com/api/read?type=photo&num=50&start=0"
	 * @return "tumblr.com"
	 */
	public static String getHostDomain(String url) {
		Pattern p = Pattern.compile("(?<=http://|\\.)[^.]*?\\.(com|cn|net|org|biz|info|cc|tv)", Pattern.CASE_INSENSITIVE);
		Matcher matcher = p.matcher(url);
		matcher.find();
		return matcher.group(0);
	}

	/**
	 * 判断字符串是否是乱码
	 *
	 * @param strName
	 *            字符串
	 * @return 是否是乱码
	 */
	public static boolean isMessyCode(String strName) {
		Pattern p = Pattern.compile("\\s*|t*|r*|n*");
		Matcher m = p.matcher(strName);
		String after = m.replaceAll("");
		String temp = after.replaceAll("\\p{P}", "");
		char[] ch = temp.trim().toCharArray();
		float chLength = ch.length;
		float count = 0;
		for (int i = 0; i < ch.length; i++) {
			char c = ch[i];
			if (!Character.isLetterOrDigit(c)) {
				if (!isChinese(c)) {
					count = count + 1;
				}
			}
		}
		float result = count / chLength;
		if (result > 0.4) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 判断字符是否是中文
	 *
	 * @param c
	 *            字符
	 * @return 是否是中文
	 */
	public static boolean isChinese(char c) {
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
				|| ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A || ub == Character.UnicodeBlock.GENERAL_PUNCTUATION
				|| ub == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION || ub == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS) {
			return true;
		}
		return false;
	}

	/**
	 * 若输入为null，则返回空字符串；否则返回字符串自身
	 *
	 * @param
	 * @return
	 */
	public static String getNotNull(String input) {
		return (input == null ? "" : input);
	}

	public static String getNotNull(String input, String defaultValue) {
		return (isBlank(input) ? defaultValue : input);
	}

	/**
	 * 截取字符串末尾空格
	 *
	 * @param input
	 * @return
	 */
	public static String trim(String input) {
		if (isBlank(input))
			return "";
		return input.trim();
	}

	/**
	 * 根据指定分隔符分割字符串，返回结果数组。<br>
	 * 处理规则：<br>
	 * 若输入为null，则返回null；<br>
	 * 否则若输入为空字符串，则返回空数组；<br>
	 * 否则若分隔符为null或空字符串，则返回包含原字符串本身的数组；<br>
	 *
	 * @param input
	 *            输入字符串
	 * @param separator
	 *            分隔符
	 * @return 结果数组（注意：包括空字符串）
	 */
	public static String[] split(String input, String separator) {
		if (input == null)
			return null;
		if (input.equals(""))
			return EMPTY_STRING;
		if (separator == null || "".equals(separator))
			return new String[]{input};

		int cursor = 0; // 游标
		int lastPos = 0; // 指向上一个分隔符后第一个字符
		ArrayList<String> list = new ArrayList<String>();

		while ((cursor = input.indexOf(separator, cursor)) != -1) {

			String token = input.substring(lastPos, cursor);
			list.add(token);

			lastPos = cursor + separator.length();

			cursor = lastPos;
		}

		if (lastPos < input.length())
			list.add(input.substring(lastPos));

		return (String[]) list.toArray(new String[list.size()]);
	}

	/**
	 * 字符串替换
	 *
	 * @param source
	 * @param oldstring
	 * @param newstring
	 * @param caseInsensive
	 * @return
	 */
	public static String replaceString(String source, String oldstring, String newstring, boolean caseInsensive) {
		Matcher matcher = null;

		// 区分大小写
		if (caseInsensive) {
			matcher = Pattern.compile(oldstring, Pattern.CASE_INSENSITIVE).matcher(source);
		} else {
			matcher = Pattern.compile(oldstring).matcher(source);
		}

		return matcher.replaceAll(newstring);
	}

	/**
	 * 清除字符串末尾的特定字符<br>
	 * 若字符串末尾并非给定字符，则什么都不做<br>
	 * 注意：该方法改变了传入的StringBuffer参数的值
	 *
	 * @param sb
	 *            字符串缓存
	 * @param tail
	 *            用户给定字符
	 * @return 字符串缓存对象的字符串表示
	 */
	public static String trimTail(StringBuffer sb, char tail) {
		if (sb.length() > 0 && sb.charAt(sb.length() - 1) == tail)
			sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	/**
	 * 根据指定分隔符分割字符串，返回结果数组。<br>
	 * 处理规则：<br>
	 * 若输入为null，则返回null；<br>
	 * 否则若输入为空字符串，则返回空数组；<br>
	 * 否则若分隔符为null或空字符串，则返回包含原字符串本身的数组；<br>
	 * 注意：返回结果中过滤掉空字符串
	 *
	 * @param input
	 *            输入字符串(数字字符串)
	 * @param separator
	 *            分隔符
	 * @return 结果数组（注意：不包括空字符串）
	 */
	public static Integer[] splitInt(String input, String separator) {
		if (input == null)
			return null;
		if (input.equals(""))
			return null;
		if (separator == null || "".equals(separator))
			return null;

		int cursor = 0; // 游标
		int lastPos = 0; // 指向上一个分隔符后第一个字符
		ArrayList<Integer> list = new ArrayList<Integer>();

		while ((cursor = input.indexOf(separator, cursor)) != -1) {

			if (cursor > lastPos) {// 滤掉空字符串
				int token = Integer.parseInt(input.substring(lastPos, cursor));
				list.add(token);
			}

			lastPos = cursor + separator.length();

			cursor = lastPos;
		}

		if (lastPos < input.length())
			list.add(Integer.parseInt(input.substring(lastPos)));

		Integer[] iStrToI = new Integer[list.size()];
		for (int i = 0; i < list.size(); i++) {
			iStrToI[i] = Integer.parseInt(list.get(i).toString());
		}
		return iStrToI;
	}

	/**
	 * 字符串的转义(处理特殊字符)
	 *
	 * @param input
	 * @return
	 */
	public static String StringToString(String input) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < input.length(); i++) {
			char c = input.toCharArray()[i];
			switch (c) {
				case '\'' :
					sb.append("\\\'");
					break;
				case '\"' :
					sb.append("\\\"");
					break;
				case '\\' :
					sb.append("\\\\");
					break;
				case '/' :
					sb.append("\\/");
					break;
				case '\b' :
					sb.append("\\b");
					break;
				case '\f' :
					sb.append("\\f");
					break;
				case '\n' :
					sb.append("\\n");
					break;
				case '\r' :
					sb.append("\\r");
					break;
				case '\t' :
					sb.append("\\t");
					break;
				default :
					sb.append(c);
					break;
			}
		}
		return sb.toString();
	}

	/**
	 * list 转换为 string
	 *
	 * @param list
	 * @param flag
	 * @return
	 */
	public static String listToString(ArrayList<String> list, String flag) {
		String strMsg = "";
		int listSize = list.size();
		if (listSize > 0) {
			for (int i = 0; i < listSize; i++) {
				if (i == listSize - 1) {
					strMsg = strMsg + list.get(i).toString();
				} else {
					strMsg = strMsg + list.get(i).toString() + flag;
				}
			}
		} else {
			strMsg = "";
		}
		return strMsg;
	}

	/**
	 * InputStream转为字符串
	 *
	 * @param in
	 * @return
	 */
	public static String toString(InputStream in) throws IOException {
		StringBuffer out = new StringBuffer();
		byte[] buffer = new byte[1024];
		for (int i; (i = in.read(buffer)) != -1;) {
			out.append(new String(buffer, 0, i));
		}
		return out.toString();
	}

	/**
	 * 对字符串中的中文进行编码
	 *
	 * @param inputUrl
	 * @return
	 */
	public static String encodeUrl(String inputUrl) {
		if (isBlank(inputUrl))
			return inputUrl;

		char[] charArray = inputUrl.toCharArray();
		for (int i = 0; i < charArray.length; i++) {
			if ((charArray[i] >= 0x4e00) && (charArray[i] <= 0x9fbb)) {
				inputUrl = inputUrl.replaceFirst(String.valueOf(charArray[i]), URLEncoder.encode(String.valueOf(charArray[i])));
			}
		}
		return inputUrl;
	}

	public static boolean isNumeric(String str) {
		if (isBlank(str))
			return false;
		int sz = str.length();
		for (int i = 0; i < sz; i++) {
			if (Character.isDigit(str.charAt(i)) == false)
				return false;
		}
		return true;
	}

	/**
	 * 截取字符串
	 *
	 * @param inputUrl
	 * @param length
	 * @return
	 */
	public static String subString(String inputUrl, int length) {
		if (isBlank(inputUrl))
			return inputUrl;

		if (inputUrl.length() > length) {
			return inputUrl.substring(0, length);
		} else {
			return inputUrl;
		}
	}

	public static void main(String[] args) {
		System.out.println(isMessyCode("你好"));
	}

	public static boolean isEmpty(CharSequence cs) {
		return cs == null || cs.length() == 0;
	}

	// 判断浮点数（double和float）
	public static boolean isDouble(String str) {
		if (null == str || "".equals(str)) {
			return false;
		}
		Pattern pattern = Pattern.compile("^[-\\+]?[.\\d]*$");
		return pattern.matcher(str).matches();
	}
}