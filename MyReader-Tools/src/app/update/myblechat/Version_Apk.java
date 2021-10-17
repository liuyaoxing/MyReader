package app.update.myblechat;

import java.io.File;

import com.google.gson.Gson;

public class Version_Apk {

	private String title;
	private String fileName;
	private String url;
	private String changelog;
	private long vcode;
	private long version;
	private long fileSize;
	private boolean forceUpdate;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getChangeLog() {
		return changelog;
	}

	public void setChangeLog(String changelog) {
		this.changelog = changelog;
	}
	public long getVcode() {
		return vcode;
	}
	public void setVcode(long vcode) {
		this.vcode = vcode;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public boolean getForceUpdate() {
		return forceUpdate;
	}

	public void setForceUpdate(boolean forceUpdate) {
		this.forceUpdate = forceUpdate;
	}

	@Override
	public String toString() {
		return "Version_Apk [fileName=" + fileName + ", changelog=" + changelog + ", version=" + version + ", fileSize=" + fileSize
				+ ", forceUpdate=" + forceUpdate + "]";
	}

	public static void main(String[] args) {
		File srcFile = new File("D:\\Developer\\DeveloperWorks\\Android\\MyBLEChatv30\\app\\release\\MyBleChat-release-3.18.apk");
		Version_Apk version = new Version_Apk();
		version.setTitle("左右手v3.18");
		version.setFileName(srcFile.getName());
		version.setChangeLog("01、增加电子卷尺功能、长度单位换算器和重量单位换算器。\r\n02、适配鸿蒙系统。\r\n03、修复了一些体验的问题");
		version.setUrl("https://github.com/liuyaoxing/repository/releases/download/v20211018/MyBleChat-release-3.18.apk");
		version.setFileSize(srcFile.length());
		version.setVcode(26);
		version.setVersion(202110180902L);
		version.setForceUpdate(true);

		Gson gson = new Gson();
		String toJson = gson.toJson(version);
		System.out.println("Json = " + toJson);

//		Version_Apk version0 = gson.fromJson(toJson, Version_Apk.class);
//		System.out.println(version0);
	}
}
