package offline.export.utils;

import java.io.File;
import java.net.URI;

public class PathUtils {

	/**
	 * 计算 file2 相对于 file1 的相对路径（使用 / 分隔符） 支持跨盘符、异常处理，返回标准化路径。
	 *
	 * @param file1
	 *            基准路径（父目录）
	 * @param file2
	 *            目标路径
	 * @return 相对路径字符串，如 "../dir/file.txt"
	 */
	public static String relativize(File file1, File file2) {
		if (file1 == null || file2 == null) {
			throw new IllegalArgumentException("File arguments cannot be null");
		}

		URI baseUri;
		URI targetUri;
		try {
			baseUri = file1.toURI();
			targetUri = file2.toURI();
		} catch (Exception e) {
			throw new IllegalArgumentException("Invalid file path: " + file1 + " or " + file2, e);
		}

		// 检查是否同协议（必须都是 file:）
		if (!"file".equals(baseUri.getScheme()) || !"file".equals(targetUri.getScheme())) {
			throw new IllegalArgumentException("Only file URIs are supported: " + baseUri + ", " + targetUri);
		}

		// 跨盘符检查（Windows）
		if (baseUri.getSchemeSpecificPart().startsWith("//") || targetUri.getSchemeSpecificPart().startsWith("//")) {
			// 一般为网络路径，不处理
			throw new IllegalArgumentException("Network paths not supported: " + baseUri + ", " + targetUri);
		}

		// 跨盘符（如 C: vs D:）
		String basePath = baseUri.getSchemeSpecificPart();
		String targetPath = targetUri.getSchemeSpecificPart();
		if (basePath.length() >= 2 && targetPath.length() >= 2 && basePath.charAt(0) == targetPath.charAt(0) && basePath.charAt(1) == ':'
				&& targetPath.charAt(1) == ':') {
			// 同盘符，可以计算
		} else if (basePath.length() >= 2 && basePath.charAt(1) == ':' && targetPath.length() >= 2 && targetPath.charAt(1) == ':') {
			// 跨盘符，无法相对化
			throw new IllegalArgumentException("Cannot compute relative path across different drives: " + baseUri + " -> " + targetUri);
		}
		try {
			// 计算相对路径
			String relPath = baseUri.relativize(targetUri).getPath();
			// 统一使用 / 分隔符（避免 Windows \ 混淆）
			return relPath.replace('\\', '/');
		} catch (Exception e) {
			throw new IllegalArgumentException("Failed to compute relative path: " + baseUri + " -> " + targetUri, e);
		}
	}

	public static void main(String[] args) {
		File base = new File("/home/user/project/src");
		File target = new File("/home/user/project/src/main/java/App.java");

		String rel = PathUtils.relativize(base, target);
		System.out.println(rel); // 输出: main/java/App.java

		// 跨盘符测试
		try {
			File cFile = new File("C:\\a\\b");
			File dFile = new File("D:\\c\\d");
			System.out.println(PathUtils.relativize(cFile, dFile));
		} catch (Exception e) {
			System.out.println("跨盘符错误：" + e.getMessage());
		}
	}
}
