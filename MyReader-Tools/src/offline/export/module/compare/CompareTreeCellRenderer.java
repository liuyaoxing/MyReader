package offline.export.module.compare;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;

// 自定义渲染器
public class CompareTreeCellRenderer extends DefaultTreeCellRenderer {
	private final Color COLOR_ADDED = new Color(0, 150, 0); // 深绿
	private final Color COLOR_DELETED = Color.RED;
	private final Color COLOR_MODIFIED = Color.ORANGE;

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
		super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

		if (value instanceof CompareTreeNode) {
			CompareTreeNode node = (CompareTreeNode) value;
			CompareTreeNode.Status status = node.getStatus();

			// 设置图标
			switch (status) {
			case ADDED:
				setIcon(UIManager.getIcon("FileView.fileIcon"));
				setForeground(COLOR_ADDED);
				break;
			case DELETED:
				setIcon(UIManager.getIcon("FileView.computerIcon")); // 或者用一个带删除线的图标
				setForeground(COLOR_DELETED);
				break;
			case MODIFIED:
				setIcon(UIManager.getIcon("FileView.floppyDriveIcon"));
				setForeground(COLOR_MODIFIED);
				break;
			default:
				setForeground(Color.BLACK);
				// 文件夹/文件默认图标由父类处理
			}

			// 如果是文件夹，强制显示文件夹图标
			if (node.getFile() != null && node.getFile().isDirectory()) {
				setIcon(UIManager.getIcon("FileView.directoryIcon"));
			}
		}
		return this;
	}
}