package offline.export.module.compare;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

public class FileTreeCellRenderer extends DefaultTreeCellRenderer {

	/** 序列号 */
	private static final long serialVersionUID = -3976277669289358061L;

	private final Color COLOR_SAME = new Color(0x4CAF50); // 绿色
	private final Color COLOR_DIFF = new Color(0xFFC107); // 黄色
	private final Color COLOR_MISSING = new Color(0xF44336); // 红色
	private final Color COLOR_DEFAULT = Color.BLACK;

	private final Icon ICON_DIR = UIManager.getIcon("Tree.openIcon"); // 默认目录图标
	// private final Icon ICON_FILE = UIManager.getIcon("FileView.fileIcon");

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);

		if (value instanceof DefaultMutableTreeNode) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			if (node instanceof CompareTreeNode) {
				CompareTreeNode data = (CompareTreeNode) node;
				if (data.getStatus() == CompareTreeNode.Status.MATCHED) {
					setForeground(COLOR_SAME);
					setText("✅ " + data.getFile().getName());
				} else if (data.getStatus() == CompareTreeNode.Status.MODIFIED) {
					setForeground(COLOR_DIFF);
					setText("⚠️ " + data.getFile().getName());
				} else if (data.getStatus() == CompareTreeNode.Status.ADDED) {
					setForeground(COLOR_MISSING);
					setText("◑(仅在右侧) " + data.getFile().getName());
				} else if (data.getStatus() == CompareTreeNode.Status.DELETED) {
					setForeground(COLOR_MISSING);
					setText("◐(仅在左侧) " + data.getFile().getName());
				} else {
					setForeground(COLOR_DEFAULT);
				}

				// ✅ 设置图标：目录 or 文件
				if (data.getFile() != null && data.getFile().isDirectory()) {
					setIcon(ICON_DIR);
				} else {
					// setIcon(ICON_FILE);
				}
			} else {
				setText(value == null ? "" : value.toString());
			}
		} else {
			setText(value == null ? "" : value.toString());
			setForeground(COLOR_DEFAULT);
		}

		// 设置最小宽度
		// setMinimumSize(new Dimension(250, getPreferredSize().height));
		setPreferredSize(new Dimension(300, getPreferredSize().height));
		return this;
	}
}