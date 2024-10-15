package db;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test {
	public static void main(String[] args) {
		String str = "中华人民共和国，简称（中国）。";
		Matcher mat = Pattern.compile("(?<=\\（)(\\S+)(?=\\）)").matcher(str);// 此处是中文输入的（）
		while (mat.find()) {
			System.out.println(mat.group());
		}

		String str2 = "中华人民共和国，简称_(中国)";
		Matcher mat2 = Pattern.compile("\\(([^}]*)\\)").matcher(str2);// 此处是中文输入的（）
		while (mat2.find()) {
			System.out.println(mat2.group(1));
		}

		System.out.println(str2.replaceAll("\\(([^}]*)\\)", "(香港)"));

		String str3 = "中华人民共和国，简称_(中国)(1).txt";
		for (int i = 0; i < 100; i++) {
			str3 = getNextFileName(str3);
			System.out.println(str3);
		}
	}

	public static String getNextFileName(String fileName) {
		String noFormat = FileUtils.getFileNameNoFormat(fileName);
		String format = fileName.contains(".") ? ("." + FileUtils.getFileFormat(fileName)) : "";
		if (noFormat.contains("(") && noFormat.endsWith(")")) {
			int lastIndexOf = noFormat.lastIndexOf("(");
			int index = Integer.valueOf(noFormat.substring(lastIndexOf + 1, noFormat.length() - 1));
			return String.format("%s(%s)%s", noFormat.substring(0, lastIndexOf), index + 1, format);
		} else {
			return noFormat + "(1)." + format;
		}
	}
}
