package offline.export.utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternUtils {

	public static final String URL_REGEX = "^(http|https|ftp)\\://([a-zA-Z0-9\\.\\-]+(\\:[a-zA-Z0-9\\.&amp;%\\$\\-]+)*@)?((25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])|([a-zA-Z0-9\\-]+\\.)*[a-zA-Z0-9\\-]+\\.[a-zA-Z]{2,4})(\\:[0-9]+)?(/[^/][a-zA-Z0-9\\.\\,\\?\\'\\\\/\\+&amp;%\\$#\\=~_\\-@]*)*$";

	public static String trimHttpPrefix(String url) {
		return url == null ? "" : url.replaceAll("^(https|http)://", "");
	}

	public static String trimHttpWWWPrefix(String url) {
		return url == null ? "" : url.replaceAll("^(https|http)://(www.)?", "");
	}

	public static boolean matchesUrl(String url) {
		if (url == null)
			return false;
		if (url.startsWith("http") || url.matches(URL_REGEX))
			return true;
		return ("http://" + url).matches(URL_REGEX);
	}

	/**
	 * 替换所有标点符号
	 * 
	 * @param str
	 * @return
	 */
	public static String replaceInvalidPath(String srcStr, String symbol) {
		String str = srcStr.replaceAll("[\"'`^=|<>·|｜《》/\\\\:%#!！~&*?$@]", symbol);
		return str.replaceAll("[^(\\u4e00-\\u9fa5)(0-9)(a-z)(A-Z)]", symbol);

	}

	/**
	 * 替换所有标点符号
	 * 
	 * @param str
	 * @return
	 */
	public static String replacePunctuation(String srcStr, String symbol) {
		return srcStr.replaceAll("[\\p{P}+~$`^=|<>·|｜《》]", symbol);
	}

	/**
	 * 是不是 www.zhangzisi.com，www.zhangzis.cc
	 * 
	 * @param url
	 * @return
	 */

	/**
	 * 是否是微信文章。
	 * 
	 * @param url
	 * @return
	 */
	public static boolean isMpWeixin(String url) {
		return url != null && url.matches("^(http|https)\\://mp.weixin.qq.com/.*$");
	}

	/**
	 * 比较两个URL是否一样。
	 * 
	 * @param url1
	 * @param url2
	 * @return
	 */
	public static boolean isSameUrl(String url1, String url2) {
		if (url1 == null || url2 == null)
			return false;
		return (url1.replaceAll("^(https|http)", "").equals(url2.replaceAll("^(https|http)", "")));
	}

	/**
	 * 是否被大括号包含,"{123}":true, "{456":false
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isBetweenBraces(String str) {
		return str != null && str.matches("^\\{.*\\}$");
	}

	/**
	 * 提取大括号内容,extractBraces("{123}")="123"
	 * 
	 * @param str
	 * @return
	 */
	public static String extractBraces(String str) {
		Pattern pattern = Pattern.compile("(?<=^\\{)(.+?)(?=\\}$)");
		Matcher matcher = pattern.matcher(str);

		while (matcher.find()) {
			return matcher.group();
		}
		return str;
	}

	/**
	 * 获得页面字符
	 */
	public static String matchHtmlCharset(String content) {
		String chs = "";
		Pattern p = Pattern.compile("(?<=charset=)(.+)(?=\")");
		Matcher m = p.matcher(content);
		if (m.find())
			return m.group();
		return chs;
	}

	/**
	 * 获得页面字符
	 */
	public static String replaceHtmlCharset(String content, String charset) {
		String regex = "(?<=charset=)(.+)(?=\")";
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(content);
		if (m.find())
			return content.replaceAll(regex, charset);
		return content;
	}

	public static String getHostDomain(String url) {
		if (url == null || url.trim().isEmpty())
			return "";
		if (PatternUtils.isMpWeixin(url)) {
			return "mp.weixin.qq.com";
		}
		url = url.startsWith("http://") ? url : "http://" + PatternUtils.trimHttpPrefix(url);
		Pattern p = Pattern.compile("(?<=https://|\\.)[^.]*?\\.(com|cn|net|org|biz|info|cc|tv|me|sb)", Pattern.CASE_INSENSITIVE);
		Matcher matcher = p.matcher(url);
		if (matcher.find()) {
			return matcher.group(0);
		}
		try {
			return new URL(url).getHost();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return url;
	}
}
