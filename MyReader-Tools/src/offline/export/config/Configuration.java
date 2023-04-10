package offline.export.config;

public class Configuration {

	private static Configuration instance = new Configuration();

	private int qrCodeCapacity = 1234;

	// 二维码尺寸
	private int qrCodeSize = 800;
	// LOGO宽度
	private int qrCodeLogoWidth = 60;
	// LOGO高度
	private int qrCodeLogoHeight = 60;

	public static Configuration getInstance() {
		return instance;
	}

	public void setQrCodeCapacity(int qrCodeCapacity) {
		this.qrCodeCapacity = qrCodeCapacity;
	}

	public int getQrCodeCapacity() {
		return this.qrCodeCapacity;
	}

	public void setQrCodeSize(int qrCodeSize) {
		this.qrCodeSize = qrCodeSize;
	}

	public int getQrCodeSize() {
		return this.qrCodeSize;
	}

	public void setQrCodeLogoWidth(int qrCodeLogoWidth) {
		this.qrCodeLogoWidth = qrCodeLogoWidth;
	}

	public int getQrCodeLogoWidth() {
		return this.qrCodeLogoWidth;
	}

	public void setQrCodeLogoHeight(int qrCodeLogoHeight) {
		this.qrCodeLogoHeight = qrCodeLogoHeight;
	}

	public int getQrCodeLogoHeight() {
		return this.qrCodeLogoHeight;
	}
}
