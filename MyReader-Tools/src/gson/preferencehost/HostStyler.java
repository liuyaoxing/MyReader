package gson.preferencehost;

public class HostStyler {

	private String name;

	private SaverStyle saver = new SaverStyle();

	private FormatterStyle formatter = new FormatterStyle();

	private CralwerStyle cralwer = new CralwerStyle();

	private class SaverStyle {

		private String[] images;

		private String[] others;

		private String[] scripts;

		private String[] videos;

		private String[] deleteRelates;

		private boolean useDownloadManager;
	}

	private class FormatterStyle {
		private String[] title;
		private String[] date;
		private String[] author;
		private String[] content;
		private String[] comments;
		private String[] removeTags;
	}

	private class CralwerStyle {
		private String root;
		
	}
}
