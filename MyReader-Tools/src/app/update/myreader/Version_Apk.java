package app.update.myreader;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

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

	public static void main(String[] args) {
		File srcFile = new File("D:\\Developer\\DeveloperWorks\\Android\\MyReaderv33\\app\\release\\MyReader-release-3.30.v20240111.apk");
		Version_Apk version = new Version_Apk();
		version.setTitle("读乐乐v3.30");
		version.setFileName(srcFile.getName());
		version.setChangeLog("01、增加文件夹上传功能; 02、修复保存长图片失败; 03、修复一些已知的问题");
		version.setUrl("https://github.com/liuyaoxing/MyReader/releases/download/v3.30/MyReader-release-3.30.v20240420.apk");
		version.setFileSize(srcFile.length());
		version.setVcode(76);
		version.setVersion(202404200900L);
		version.setForceUpdate(true);

		Gson gson = new Gson();
		String toJson = gson.toJson(version);
		System.out.println("Json = " + toJson);

//		Version_Apk version0 = gson.fromJson(toJson, Version_Apk.class);
//		System.out.println(version0);
	}

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
		return "Version_Apk [fileName=" + fileName + ", changelog=" + changelog + ", version=" + version + ", fileSize=" + fileSize + ", forceUpdate="
				+ forceUpdate + "]";
	}

}
