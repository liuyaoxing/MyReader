package gson.formatterentry;


import java.io.Serializable;

public class FormatterEntry implements Serializable {

	/** –Ú¡–∫≈ */
	private static final long serialVersionUID = 5587900874851982631L;

	private String[] title_select;
	private String[] date_select;
	private String[] author_select;
	private String[] content_select;
	private String[] comments_select;
	private String[] remove_tags;
	private boolean script_remove = true;

	public String[] getTitle_select() {
		return title_select;
	}

	public void setTitle_select(String[] title_select) {
		this.title_select = title_select;
	}

	public String[] getDate_select() {
		return date_select;
	}

	public void setDate_select(String[] date_select) {
		this.date_select = date_select;
	}

	public String[] getAuthor_select() {
		return author_select;
	}

	public void setAuthor_select(String[] author_select) {
		this.author_select = author_select;
	}

	public String[] getContent_select() {
		return content_select;
	}

	public void setContent_select(String[] body_select) {
		this.content_select = body_select;
	}

	public String[] getComments_select() {
		return comments_select;
	}

	public void setComments_select(String[] comments_select) {
		this.comments_select = comments_select;
	}

	public String[] getRemove_tags() {
		return remove_tags;
	}

	public void setRemove_tags(String[] remove_tags) {
		this.remove_tags = remove_tags;
	}

	public boolean isScript_remove() {
		return script_remove;
	}

	public void setScript_remove(boolean script_remove) {
		this.script_remove = script_remove;
	}

}
