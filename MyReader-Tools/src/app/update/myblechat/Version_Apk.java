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
		File srcFile = new File("D:\\Developer\\DeveloperWorks\\Android\\MyBleChatv33\\app\\release\\MyBleChat-release-3.21.apk");
		Version_Apk version = new Version_Apk();
		version.setTitle("左右手v3.21");
		version.setFileName(srcFile.getName());
		version.setChangeLog("00、增加二维码传文件的功能：手机开启文件共享服务，在电脑浏览器打开文件服务管理页面，在左下方下载【备份工具专业版】后，用备份工具生产文件的二维码，在手机App上用码云传功能可以识别并合并文件。\r\n01、修复更新失败问题。\r\n02、适配Android13。\r\n03、修复了一些体验的问题");
		version.setUrl("https://github.com/liuyaoxing/MyBLEChat/releases/download/v3.21/MyBleChat-release-3.21.apk");
		version.setFileSize(srcFile.length());
		version.setVcode(29);
		version.setVersion(202304240900L);
		version.setForceUpdate(true);

		Gson gson = new Gson();
		String toJson = gson.toJson(version);
		System.out.println("Json = " + toJson);

//		Version_Apk version0 = gson.fromJson(toJson, Version_Apk.class);
//		System.out.println(version0);
	}
}
