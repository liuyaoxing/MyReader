package offline.export;

public class Test {

	public static void main(String[] args) {
		System.out.println(getFileName("/dll/export/229587"));
	}

	/**
	 * �����ļ�����·����ȡ�ļ���
	 *
	 * @param filePath
	 * @return
	 */
	public static String getFileName(String filePath) {
		if (filePath == null || filePath.isEmpty())
			return "";
		return filePath.substring(filePath.lastIndexOf("/") + 1);
	}
}
