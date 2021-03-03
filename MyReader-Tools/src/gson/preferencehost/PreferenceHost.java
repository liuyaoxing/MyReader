package gson.preferencehost;

import gson.formatterentry.FormatterEntry;

public class PreferenceHost {

	private String url;

	private FormatterEntry formatterEntry;

	private String[] saveImages;

	private String[] saveOthers;

	private String[] saveScripts;

	private String[] saveVideos;

	private String[] deleteRelates;

	private boolean useDownloadManager;

	private String userAgent;

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public FormatterEntry getFormatterEntry() {
		return formatterEntry;
	}

	public void setFormatterEntry(FormatterEntry formatterEntry) {
		this.formatterEntry = formatterEntry;
	}

	public String[] getSaveImages() {
		return saveImages;
	}

	public void setSaveImages(String[] saveImages) {
		this.saveImages = saveImages;
	}

	public String[] getSaveOthers() {
		return saveOthers;
	}

	public void setSaveOthers(String[] saveOthers) {
		this.saveOthers = saveOthers;
	}

	public String[] getSaveScripts() {
		return saveScripts;
	}

	public void setSaveScripts(String[] saveScripts) {
		this.saveScripts = saveScripts;
	}

	public String[] getSaveVideos() {
		return saveVideos;
	}

	public void setSaveVideos(String[] saveVideos) {
		this.saveVideos = saveVideos;
	}

	public String[] getDeleteRelates() {
		return deleteRelates;
	}

	public void setDeleteRelates(String[] deleteRelates) {
		this.deleteRelates = deleteRelates;
	}

	public boolean isUseDownloadManager() {
		return useDownloadManager;
	}

	public void setUseDownloadManager(boolean useDownloadManager) {
		this.useDownloadManager = useDownloadManager;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

}
