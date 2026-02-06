import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;

public class FileNameMatcher {
	public static void main(String[] args) {
		// 定义通配符模式
		String pattern = "*.jpg"; // 匹配所有 .jpg 文件
		PathMatcher matcher = Paths.get("").getFileSystem().getPathMatcher("glob:" + pattern);

		// 测试文件名
		String[] files = {"photo.jpg", "image.JPG", // 注意：大小写不敏感？看需求
				"image.png", "test.jpg.bak", "abc.jpg"};

		for (String file : files) {
			Path path = Paths.get(file);
			boolean matches = matcher.matches(path);
			System.out.printf("%-15s -> %s%n", file, matches ? "✅ 匹配" : "❌ 不匹配");
		}
	}
}