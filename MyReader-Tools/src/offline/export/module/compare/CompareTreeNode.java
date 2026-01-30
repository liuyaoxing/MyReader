package offline.export.module.compare;

import java.io.File;

import javax.swing.tree.DefaultMutableTreeNode;

// 自定义树节点类
public class CompareTreeNode extends DefaultMutableTreeNode {
	
	/** 序列号 */
	private static final long serialVersionUID = -683870836107718279L;

	public enum Status {
		MATCHED, MODIFIED, ADDED, DELETED, UNKNOWN
	}

	private Status status;
	private File file; // 关联的文件对象

	public CompareTreeNode(File file, Object userObject, Status status) {
		super(userObject);
		this.file = file;
		this.status = status;
	}

	public Status getStatus() {
		return status;
	}

	public File getFile() {
		return file;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
}
