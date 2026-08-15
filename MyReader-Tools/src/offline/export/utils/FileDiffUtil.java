package offline.export.utils;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

import offline.export.utils.ProcessUtils.ProcessStatus;

public class FileDiffUtil {

	/**
	 * 使用指定的外部工具比较两个文件
	 *
	 * @param toolName
	 *            工具名称 (例如: "winmerge", "bcompare", "tortoisemerge")
	 * @param file1
	 *            第一个文件
	 * @param file2
	 *            第二个文件
	 */
	public static void openDiffTool(String toolName, File file1, File file2) {
		if (file1 == null || !file1.exists()) {
			throw new IllegalArgumentException("File 1 does not exist: " + file1);
		}
		if (file2 == null || !file2.exists()) {
			throw new IllegalArgumentException("File 2 does not exist: " + file2);
		}

		List<String> command;

		switch (toolName.toLowerCase()) {
			case "winmerge" :
				String WINMERGE_EXE_PATH = "C:\\Program Files\\WinMerge\\WinMergeU.exe";
				if (!new File(WINMERGE_EXE_PATH).exists())
					WINMERGE_EXE_PATH = "C:\\Program Files (x86)\\WinMerge\\WinMergeU.exe";
				// WinMergeU.exe /u /dl "Left" /dr "Right" "File1" "File2"
				command = Arrays.asList(WINMERGE_EXE_PATH, "/u", "/dl", "Left", "/dr", "Right", file1.getAbsolutePath(), file2.getAbsolutePath());
				break;
			case "bcompare" :
				// bcompare.exe "File1" "File2"
				command = Arrays.asList("bcompare.exe", file1.getAbsolutePath(), file2.getAbsolutePath());
				break;
			case "tortoisemerge" :
				// TortoiseMerge.exe /base:"File1" /mine:"File2"
				command = Arrays.asList("TortoiseMerge.exe", "/base:" + file1.getAbsolutePath(), "/mine:" + file2.getAbsolutePath());
				break;
			case "notepadpp" :
				// 如果安装了 NppFTP 或类似插件，可能需要特定参数，这里仅作为示例
				// 通常 Notepad++ 本身不是文件比较工具，而是编辑器
				command = Arrays.asList("notepad++.exe", file1.getAbsolutePath(), file2.getAbsolutePath());
				break;
			default :
				throw new IllegalArgumentException("Unsupported diff tool: " + toolName);
		}

		try {
			ProcessStatus processStatus = ProcessUtils.execute(0, command.toArray(new String[0]));
			System.out.println(processStatus);
		} catch (TimeoutException e) {
			e.printStackTrace();
		} catch (Exception e) {
			throw new RuntimeException("Failed to start diff tool: " + toolName, e);
		}
	}

	/**
	 * 默认使用 WinMerge
	 */
	public static void openWinMerge(File file1, File file2) {
		openDiffTool("winmerge", file1, file2);
	}

	/**
	 * 默认使用 Beyond Compare
	 */
	public static void openBeyondCompare(File file1, File file2) {
		openDiffTool("bcompare", file1, file2);
	}
}