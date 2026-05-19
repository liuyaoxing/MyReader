package offline.export.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileCompareUtils {

	/**
	 * 递归删除 sourceRoot 中存在于 targetRoot 的相对路径相同的文件
	 *
	 * @param sourceRoot
	 *            源根目录
	 * @param targetRoot
	 *            目标根目录
	 */
	public static void removeDuplicateFilesRecursively(Path sourceRoot, Path targetRoot, boolean fileMd5) throws IOException {
		if (!Files.exists(sourceRoot) || !Files.exists(targetRoot)) {
			throw new IOException("根目录不存在");
		}

		// 1. 构建目标目录中所有文件的相对路径集合
		Set<String> targetRelativePaths = Files.walk(targetRoot).filter(Files::isRegularFile).map(p -> targetRoot.relativize(p).toString())
				.collect(Collectors.toSet());

		if (targetRelativePaths.isEmpty()) {
			System.out.println("目标目录为空。");
			return;
		}

		// 2. 遍历源目录，删除匹配的文件
		try (Stream<Path> paths = Files.walk(sourceRoot)) {
			paths.filter(Files::isRegularFile).filter(sourcePath -> {
				String relPath = sourceRoot.relativize(sourcePath).toString();
				return targetRelativePaths.contains(relPath);
			}).forEach(sourcePath -> {
				try {
					Path targetPath = targetRoot.resolve(sourceRoot.relativize(sourcePath));
					if (isContentSameByBytes(sourcePath, targetPath, fileMd5)) {
						System.out.println(String.format("删除完全相同的两个文件:%s=%s", sourcePath, targetPath));
						Files.delete(sourcePath);
						Files.delete(targetPath);
					} else {
						System.out.println(String.format("忽略完全相同的两个文件:%s=%s", sourcePath, targetPath));
					}
				} catch (IOException e) {
					System.err.println("删除失败: " + sourcePath + " - " + e.getMessage());
				}
			});
		}

		// 3. 清理源目录中可能出现的空文件夹（可选）
		removeEmptyDirectories(sourceRoot, targetRoot);
	}

	/**
	 * 通过读取所有字节比较两个文件 缺点：大文件会占用大量内存，仅建议用于小文件
	 */
	public static boolean isContentSameByBytes(Path file1, Path file2, boolean fileMd5) throws IOException {
		if (Files.size(file1) != Files.size(file2))
			return false;

		if (fileMd5 && FileUtils.contentEquals(file1.toFile(), file2.toFile()))
			return true;

		return file1.equals(file2) && file1.toFile().lastModified() == file2.toFile().lastModified();
	}

	/**
	 * 递归删除空文件夹
	 */
	private static void removeEmptyDirectories(Path sourceRoot, Path targetRoot) throws IOException {
		Files.walk(sourceRoot).filter(Files::isDirectory).sorted((a, b) -> b.compareTo(a)) // 从最深层级开始删除
				.forEach(dir -> {
					try {
						if (!Files.list(dir).findAny().isPresent()) {
							System.out.println("已删除空文件夹: " + dir);
							Files.delete(dir);
						}
						Path targetDir = targetRoot.resolve(sourceRoot.relativize(dir));
						if (!Files.list(targetDir).findAny().isPresent()) {
							System.out.println("已删除空文件夹: " + targetDir);
							Files.delete(targetDir);
						}
					} catch (IOException e) {
						// 忽略删除失败
					}
				});
	}

	public static void main(String[] args) {
		try {
			Path source = Paths.get("/adhome/ex-liuraoxing135/Desktop/give/2026-05-18/source_folder");
			Path target = Paths.get("/adhome/ex-liuraoxing135/Desktop/give/2026-05-18/target_folder");

			removeDuplicateFilesRecursively(source, target, true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
