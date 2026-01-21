package offline.export.module;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.util.Enumeration;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import offline.export.module.compare.CompareTreeNode;

/**
 * 上传到手机面板
 * 
 * @author liuyaoxing
 */
public class FileCompareJPanel extends FileCompareJPanelUI {

	/** 序列号 */
	private static final long serialVersionUID = -5952898543215887697L;

	public FileCompareJPanel(JFrame frame) {
//		createPopupMenu();
		addListeners();
//		initDnd();

	}

	private void addListeners() {
		leftBtn.addActionListener(e -> selectFolder(leftCombo));
		rightBtn.addActionListener(e -> selectFolder(rightCombo));
		refreshBtn.addActionListener(e -> performCompare());
	}

	private void selectFolder(JComboBox<String> field) {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			addItemsToCombo(field, new String[] { chooser.getSelectedFile().getAbsolutePath() }, 0);
		}
	}

	// 执行比对的核心方法
	private void performCompare() {
		splitPane.setDividerLocation(0.5);

		String path1 = getComboText(leftCombo);
		String path2 = getComboText(rightCombo);

		if (path1.isEmpty() || path2.isEmpty()) {
			JOptionPane.showMessageDialog(this, "请填写两个文件夹路径");
			return;
		}

		File folder1 = new File(path1);
		File folder2 = new File(path2);

		if (!folder1.exists() || !folder2.exists()) {
			JOptionPane.showMessageDialog(this, "路径不存在");
			return;
		}

		// 构建左右两棵树的根节点
		CompareTreeNode root1 = new CompareTreeNode(folder1, folder1.getName(), CompareTreeNode.Status.MATCHED);
		CompareTreeNode root2 = new CompareTreeNode(folder2, folder2.getName(), CompareTreeNode.Status.MATCHED);

		// 执行递归比对
		compareNodes(root1, root2, folder1, folder2);

		// 更新树模型
		leftModel.setRoot(root1);
		rightModel.setRoot(root2);

		// 展开所有节点
		expandAll(leftTree);
		expandAll(rightTree);
	}

	// 递归比对逻辑
	private void compareNodes(CompareTreeNode node1, CompareTreeNode node2, File file1, File file2) {
		// 获取两个目录下的子项
		File[] children1 = file1.isDirectory() ? file1.listFiles() : new File[0];
		File[] children2 = file2.isDirectory() ? file2.listFiles() : new File[0];

		if (children1 == null)
			children1 = new File[0];
		if (children2 == null)
			children2 = new File[0];

		// 将右边的子项放入Map，便于查找
		java.util.Map<String, File> rightMap = new java.util.HashMap<>();
		for (File f : children2) {
			rightMap.put(f.getName(), f);
		}

		// 遍历左边
		for (File f1 : children1) {
			CompareTreeNode.Status status;
			File f2 = rightMap.get(f1.getName());

			if (f2 == null) {
				// 仅在左边存在
				status = CompareTreeNode.Status.DELETED;
			} else {
				// 两边都存在
				if (f1.isDirectory() && f2.isDirectory()) {
					// 如果是目录，递归比对
					status = CompareTreeNode.Status.MATCHED;
				} else {
					// 如果是文件，检查是否相同
					status = isFileEqual(f1, f2) ? CompareTreeNode.Status.MATCHED : CompareTreeNode.Status.MODIFIED;
				}
			}

			CompareTreeNode n1 = new CompareTreeNode(f1, f1.getName(), status);
			node1.add(n1);

			// 为右边的树创建对应节点
			File targetFile = (f2 != null) ? f2 : f1; // 如果右边没有，就用左边的文件（用于显示）
			CompareTreeNode n2 = new CompareTreeNode(targetFile, f1.getName(), status);
			node2.add(n2);

			// 如果是目录且两边都存在，递归
			if (f1.isDirectory() && f2 != null && f2.isDirectory()) {
				compareNodes(n1, n2, f1, f2);
			}
		}

		// 处理仅在右边存在的文件 (补充到右边的树上)
		for (File f2 : children2) {
			if (rightMap.containsKey(f2.getName())) {
				// 检查这个文件是否已经在上面的循环中处理过了
				boolean alreadyExists = false;
				for (int i = 0; i < node2.getChildCount(); i++) {
					if (node2.getChildAt(i).toString().equals(f2.getName())) {
						alreadyExists = true;
						break;
					}
				}
				if (!alreadyExists) {
					CompareTreeNode n2 = new CompareTreeNode(f2, f2.getName(), CompareTreeNode.Status.ADDED);
					node2.add(n2);
					// 注意：左边树这里不需要添加，保持为空或者添加一个占位符（这里选择不添加）
				}
			}
		}
	}

	// 展开所有树节点
	private void expandAll(JTree tree) {
		TreeNode root = (TreeNode) tree.getModel().getRoot();
		expandNode(tree, new TreePath(root));
	}

	private void expandNode(JTree tree, TreePath parent) {
		TreeNode node = (TreeNode) parent.getLastPathComponent();
		if (node.getChildCount() >= 0) {
			for (Enumeration<?> e = node.children(); e.hasMoreElements();) {
				TreeNode n = (TreeNode) e.nextElement();
				TreePath path = parent.pathByAddingChild(n);
				tree.expandPath(path);
				expandNode(tree, path);
			}
		}
	}

	// 简单的文件比较 (大小+修改时间)
	private boolean isFileEqual(File f1, File f2) {
		return f1.length() == f2.length() && Math.abs(f1.lastModified() - f2.lastModified()) < 2000;
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String propertyName = event.getPropertyName();
		if (PROP_LANPORT_SCAN_IP.equals(propertyName)) {
//			itemSet.add((String) event.getNewValue() + FOLDER_UPLOAD);
//			addItemsToCombo(urlCombo2, itemSet.toArray(new String[0]), 0);
		}
		super.propertyChange(event);
	}
}
