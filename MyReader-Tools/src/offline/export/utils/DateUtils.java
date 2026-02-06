package offline.export.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public class DateUtils {

	private final static int SECONDS = 1;
	private final static int MINUTES = 60 * SECONDS;
	public final static int HOURS = 60 * MINUTES;
	public final static int DAYS = 24 * HOURS;
	private final static int WEEKS = 7 * DAYS;
	private final static int MONTHS = 4 * WEEKS;
	public final static int YEARS = 12 * MONTHS;

	public static final String YYYYMMDD = "yyyyMMdd";

	/**
	 * 时间格式：yyyy-MM-dd HH:mm:ss
	 */
	public static final String YYYY_MM_dd_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

	/**
	 * 时间格式：yyyy-MM-dd HH:mm:ss
	 */
	public static final String YYYY_MM_dd = "yyyy-MM-dd";

	public static Map<String, String> dateFormatPattern = new HashMap<String, String>();

	static {
		dateFormatPattern.put("[0-9]{4}-[0-9]{2}-[0-9]{2}", "yyyy-MM-dd"); // 1987-06-11
		dateFormatPattern.put("[0-9]{4}-[0-9]{1}-[0-9]{2}", "yyyy-M-dd"); // 1987-6-11
		dateFormatPattern.put("[0-9]{4}/[0-9]{2}/[0-9]{2}", "yyyy/MM/dd"); // 1987/06/11
		dateFormatPattern.put("[0-9]{4}/[0-9]{1}/[0-9]{2}", "yyyy/M/dd"); // 1987/6/11
		dateFormatPattern.put("[0-9]{4}/[0-9]{2}/[0-9]{1}", "yyyy/M/dd"); // 1987/06/1
		dateFormatPattern.put("[0-9]{4}/[0-9]{1}/[0-9]{1}", "yyyy/M/d"); // 1987/6/1
		dateFormatPattern.put("[0-9]{4}[0-9]{2}[0-9]{2}", "yyyyMMdd"); // 19870611
		dateFormatPattern.put("[0-9]{4}[0-9]{1}[0-9]{2}", "yyyyMdd"); // 19870611
		dateFormatPattern.put("[0-9]{4}[0-9]{2}", "yyyyMM"); // 198706
		dateFormatPattern.put("[0-9]{4}/[0-9]{2}", "yyyy/MM"); // 1987/06
		dateFormatPattern.put("[0-9]{4}-[0-9]{2}", "yyyy-MM"); // 1987/06
		dateFormatPattern.put("[0-9]{4}[0-9]{1}", "yyyyM"); // 19876
		dateFormatPattern.put("[0-9]{4}/[0-9]{1}", "yyyy/M"); // 1987/6
		dateFormatPattern.put("[0-9]{4}-[0-9]{1}", "yyyy-M"); // 1987-6
	}

	/**
	 * 获取当前时间
	 *
	 * @return
	 */
	public static String getCurrentTime() {
		Date date = Calendar.getInstance().getTime();
		SimpleDateFormat sf = new SimpleDateFormat(YYYY_MM_dd_HH_MM_SS, Locale.getDefault());
		return sf.format(date);
	}

	public static String matchDateFormat(String dateStr) {
		Iterator<String> iter = dateFormatPattern.keySet().iterator();
		while (iter.hasNext()) {
			String key = iter.next();
			Pattern pattern = Pattern.compile(key);
			if (pattern.matcher(dateStr).matches())
				return dateFormatPattern.get(key);
		}
		return "yyyyMMdd";
	}

	public static String format(Date date, String format) {
		SimpleDateFormat sf = new SimpleDateFormat(format, Locale.getDefault());
		return sf.format(date);
	}

	public static String formatLocal(Date date) {
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
		return sf.format(date);
	}

	public static String formatDateLong(long lastModified) {
		return formatDate(lastModified, "yyyy-MM-dd HH:mm:ss");
	}

	public static String formatDate(long lastModified, String format) {
		java.text.SimpleDateFormat df = new java.text.SimpleDateFormat(format);
		String dateTime = df.format(new Date(lastModified));
		return dateTime;
	}

	public static String format(String dateStr, String oldFormat, String newFormat) {
		try {
			SimpleDateFormat oldDF = new SimpleDateFormat(oldFormat, Locale.getDefault());
			SimpleDateFormat newDF = new SimpleDateFormat(newFormat, Locale.getDefault());
			return newDF.format(oldDF.parse(dateStr));
		} catch (ParseException ex) {
			ex.printStackTrace();
		}
		return dateStr;
	}

	public static Date parser(String dateStr) {
		try {
			SimpleDateFormat df = new SimpleDateFormat(matchDateFormat(dateStr), Locale.getDefault());
			return df.parse(dateStr);
		} catch (ParseException ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * 算自然天数
	 *
	 * @param date1
	 * @param date2
	 * @return double
	 */
	public static double diff(Date date1, Date date2) {
		if (date1 == null || date2 == null)
			return 0;
		return Math.floor((date1.getTime() - date2.getTime()) / (1000 * 3600 * 24));
	}

	/**
	 * 算自然小时
	 *
	 * @param date1
	 * @param date2
	 * @return double
	 */
	public static double diffHours(Date date1, Date date2) {
		if (date1 == null || date2 == null)
			return 0;
		return Math.floor((date1.getTime() - date2.getTime()) / (1000 * 3600));
	}

	public static String toDateStr(long lastModified) {
		return toDateStr("yyyy-MM-dd HH:mm:ss", lastModified);
	}

	public static String toDateStr(String format, long lastModified) {
		return new SimpleDateFormat(format, Locale.CHINA).format(new Date(lastModified));
	}

	public static String getFuzzy(String dateTime) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
			Calendar cal = Calendar.getInstance();
			cal.setTime(sdf.parse(dateTime));
			return timeAgo(cal);
		} catch (Exception ex) {
			return dateTime;
		}
	}

	public static String getFuzzy2(Date dateTime) {
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTime(dateTime);
			return timeAgo(cal);
		} catch (Exception ex) {
			return "";
		}
	}

	public static String getFuzzy2(String dateTime) {
		try {
			if (dateTime == null || dateTime.length() == 0)
				return "";
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);
			Calendar cal = Calendar.getInstance();
			cal.setTime(sdf.parse(dateTime));
			return timeAgo(cal);
		} catch (Exception ex) {
			return dateTime;
		}
	}

	/**
	 * 获取当前时间
	 *
	 * @param format
	 *            "yyyy-MM-dd'T'HH:mm:ss.SSSZ"
	 * @return
	 */
	public static String getCurrentDate(String format) {
		Date date = Calendar.getInstance().getTime();
		SimpleDateFormat sf = new SimpleDateFormat(format, Locale.getDefault());
		return sf.format(date);
	}

	public static String getDate(String format, int dateValue) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, dateValue);
		Date date = cal.getTime();
		SimpleDateFormat sf = new SimpleDateFormat(format, Locale.getDefault());
		return sf.format(date);
	}

	public static String getDateMinute(String format, int minuteValue) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MINUTE, minuteValue);
		Date date = cal.getTime();
		SimpleDateFormat sf = new SimpleDateFormat(format, Locale.getDefault());
		return sf.format(date);
	}

	public static String getMonthMinute(String format, int minuteValue) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, minuteValue);
		Date date = cal.getTime();
		SimpleDateFormat sf = new SimpleDateFormat(format, Locale.getDefault());
		return sf.format(date);
	}

	public static String getYearMinute(String format, int minuteValue) {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.YEAR, minuteValue);
		Date date = cal.getTime();
		SimpleDateFormat sf = new SimpleDateFormat(format, Locale.getDefault());
		return sf.format(date);
	}

	/**
	 * 获取前一天传-1, 前两天传-2
	 *
	 * @param date
	 * @param amount
	 * @return
	 */
	public static Date addDays(Date date, int amount) {
		return add(date, Calendar.DAY_OF_MONTH, amount);
	}

	private static Date add(Date date, int calendarField, int amount) {
		if (date == null)
			throw new IllegalArgumentException("The date must not be null");
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(calendarField, amount);
		return c.getTime();
	}

	/**
	 * 将几天前，几小时前转换成实际的时间。
	 *
	 * @param format
	 *            "yyyy-MM-dd HH:mm:ss"
	 * @param minuteValue
	 *            xx天前,xx小时xx分钟前,xx小时前
	 * @return
	 * @throws Exception
	 */
	public static String formatDateStr(String format, String minuteValue) throws Exception {
		if (minuteValue.equals("刚刚")) {
			return getCurrentDate(format);
		}
		if (minuteValue.equals("今天")) {
			return getCurrentDate("yyyy-MM-dd");
		}
		if (minuteValue.matches("^[0-9]+天前$")) {// xx天前
			int result = Integer.valueOf(minuteValue.replaceAll("[^0-9]", "")) * 60 * 24;
			return getDateMinute("yyyy-MM-dd HH:mm:ss", -result);
		}
		if (minuteValue.matches("^[0-9]+小时[0-9]+分钟前$")) {// xx小时xx分钟前
//			String str = minuteValue.replace("小时", "*60+").replace("分钟前", "*1");
//			Evaluator evaluator = new Evaluator();
//			Double result = Double.valueOf(evaluator.evaluate(str));
//			return getDateMinute("yyyy-MM-dd HH:mm:ss", -result.intValue());
			return minuteValue;
		}
		if (minuteValue.matches("^[0-9]+小时前$")) {// xx小时前
			int result = Integer.valueOf(minuteValue.replaceAll("[^0-9]", "")) * 60;
			return getDateMinute("yyyy-MM-dd HH:mm:ss", -result);
		}
		if (minuteValue.matches("^[0-9]+分钟前$")) {// xx分钟前
			int result = Integer.valueOf(minuteValue.replaceAll("[^0-9]", ""));
			return getDateMinute("yyyy-MM-dd HH:mm:ss", -result);
		}
		if (minuteValue.matches("^[0-9]+月前$")) {// 2月前
			int result = Integer.valueOf(minuteValue.replaceAll("[^0-9]", ""));
			return getMonthMinute("yyyy-MM-dd HH:mm:ss", -result);
		}
		if (minuteValue.matches("^[0-9]+年前$")) {// 2年前
			int result = Integer.valueOf(minuteValue.replaceAll("[^0-9]", ""));
			return getYearMinute("yyyy-MM-dd HH:mm:ss", -result);
		}
		// 2017年01月03日
		if (minuteValue.matches("^[0-9]+年[0-9]+月[0-9]+日$")) {
			return format(minuteValue, "yyyy年MM月dd日", "yyyy-MM-dd HH:mm:ss");
		}
		// 2017-07-19 22:07
		if (minuteValue.matches("^[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}")) {
			return format(minuteValue, "yyyy-MM-dd HH:mm", "yyyy-MM-dd HH:mm:ss");
		}
		// 12-21 20:48
		if (minuteValue.matches("^[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}")) {
			return DateUtils.getCurrentDate("yyyy") + "-" + format(minuteValue, "MM-dd HH:mm", "MM-dd HH:mm:ss");
		}
		// 2017-12-16
		if (minuteValue.matches("^[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
			return minuteValue + " " + DateUtils.getCurrentDate("HH:mm:ss");
		}
		return minuteValue;
	}

	public static String week2DateTime(String format, String date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(new Date());
		int today = calendar.get(Calendar.DAY_OF_WEEK) + 7;
		int day = 0;
		if (date.contains("小时前") || date.equals("今天") || date.equals("刚刚")) {
			day = 0;
		} else if ("昨天".equals(date)) {
			day = 1;
		} else if ("前天".equals(date)) {
			day = 2;
		} else if ("星期日".equals(date) || "星期天".equals(date)) {
			day = today - 1;
		} else if ("星期一".equals(date)) {
			day = today - 2;
		} else if ("星期二".equals(date)) {
			day = today - 3;
		} else if ("星期三".equals(date)) {
			day = today - 4;
		} else if ("星期四".equals(date)) {
			day = today - 5;
		} else if ("星期五".equals(date)) {
			day = today - 6;
		} else if ("星期六".equals(date)) {
			day = today - 7;
		} else {
			return date;
		}
		return getDate(format, -day);
	}

	public static int getIndexOfWeek(String date) {
		if ("星期日".equals(date) || "星期天".equals(date))
			return 1;
		if ("星期一".equals(date))
			return 2;
		if ("星期二".equals(date))
			return 3;
		if ("星期三".equals(date))
			return 4;
		if ("星期四".equals(date))
			return 5;
		if ("星期五".equals(date))
			return 6;
		if ("星期六".equals(date))
			return 7;
		return 0;
	}

	public static String getDate(int dateValue) {
		return getDate(YYYY_MM_dd_HH_MM_SS, dateValue);
	}

	/**
	 * @param lastModified
	 * @param dateFormat
	 *            "yyyy-MM-dd HH:mm:ss"
	 * @return
	 */
	public static String formatLastModified(long lastModified, String dateFormat) {
		java.text.SimpleDateFormat df = new java.text.SimpleDateFormat(dateFormat, Locale.getDefault());
		String dateTime = df.format(new Date(lastModified));
		return dateTime;
	}

	public static double hourAgo(long lastModified) {
		Date date1 = new Date(lastModified);
		Date date2 = new Date(System.currentTimeMillis());
		return diffHours(date2, date1);
	}

	public static String timeAgo(Calendar before) {
		Calendar currentTime = Calendar.getInstance(Locale.CHINA);
		int beforeSeconds = (int) (before.getTimeInMillis() / 1000);
		int nowSeconds = (int) (currentTime.getTimeInMillis() / 1000);
		int timeDifference = nowSeconds - beforeSeconds;

		int unit;

		if (timeDifference < MINUTES) {
			unit = SECONDS;
		} else if (timeDifference < HOURS) {
			unit = MINUTES;
		} else if (timeDifference < DAYS) {
			unit = HOURS;
		} else if (timeDifference < WEEKS) {
			unit = DAYS;
		} else if (timeDifference < MONTHS) {
			unit = WEEKS;
		} else if (timeDifference < YEARS) {
			unit = MONTHS;
		} else {
			unit = YEARS;
		}

		return callUnit(before, unit, timeDifference);
	}

	private static String callUnit(Calendar before, int unit, int difference) {
		difference = difference / unit;

		if (difference == 1) {
			return callSingleUnit(before, unit);
		} else {
			return callMultiUnit(before, unit, difference);
		}
	}

	private static String callMultiUnit(Calendar before, int unit, int difference) {
		switch (unit) {
			case SECONDS :
				return FuzzyMessages.someSecondsAgo(difference);
			case MINUTES :
				return FuzzyMessages.someMinutesAgo(difference);
			case HOURS :
				return FuzzyMessages.someHoursAgo(difference);
			case DAYS :
				return FuzzyMessages.someDaysAgo(difference);
			case WEEKS :
				return FuzzyMessages.someWeeksAgo(difference);
			case MONTHS :
				return FuzzyMessages.someMonthsAgo(difference);
			case YEARS :
				return FuzzyMessages.someYearsAgo(difference);
			default :
				throw new RuntimeException("Unknown multi unit");
		}
	}

	private static String callSingleUnit(Calendar before, int unit) {
		switch (unit) {
			case SECONDS :
				return FuzzyMessages.oneSecondAgo();
			case MINUTES :
				return FuzzyMessages.oneMinuteAgo();
			case HOURS :
				return FuzzyMessages.oneHourAgo();
			case DAYS :
				return FuzzyMessages.oneDayAgo();
			case WEEKS :
				return FuzzyMessages.oneWeekAgo();
			case MONTHS :
				return FuzzyMessages.oneMonthAgo();
			case YEARS :
				return FuzzyMessages.oneYearAgo();
			default :
				throw new RuntimeException("Unknown single unit");
		}
	}

	private static class FuzzyMessages {

		public static String oneSecondAgo() {
			return "刚刚";
		}

		public static String someSecondsAgo(int numberOfSeconds) {
			return "刚刚";
		}

		public static String oneMinuteAgo() {
			return "刚刚";
		}

		public static String someMinutesAgo(int numberOfMinutes) {
			if (numberOfMinutes <= 9) {
				return "刚刚";
			} else {
				return String.format("%s分钟前", numberOfMinutes);
			}
		}

		public static String oneHourAgo() {
			return "1小时前";
		}

		public static String someHoursAgo(int numberOfHours) {
			if (numberOfHours <= 23) {
				return String.format("%s小时前", numberOfHours);
			}
			return "今天";
		}

		public static String oneDayAgo() {
			return "昨天";
		}

		public static String someDaysAgo(int numberOfDays) {
			if (numberOfDays <= 5) {
				return String.format("%s天前", numberOfDays);
			}
			return "这周";
		}

		public static String oneWeekAgo() {
			return "上周";
		}

		public static String someWeeksAgo(int numberOfWeeks) {
			return numberOfWeeks + "周前";
		}

		public static String oneMonthAgo() {
			return "1 个月前";
		}

		public static String someMonthsAgo(int numberOfMonths) {
			return numberOfMonths + "月前";
		}

		public static String oneYearAgo() {
			return "去年";
		}

		public static String someYearsAgo(int numberOfYears) {
			return numberOfYears + "年前";
		}
	}

}
