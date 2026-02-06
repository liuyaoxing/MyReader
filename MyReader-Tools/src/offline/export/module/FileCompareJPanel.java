package offline.export.module;

import java.awt.Desktop;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import offline.export.dialog.DeleteFileConfirmDialog;
import offline.export.module.compare.CompareTreeNode;
import offline.export.module.compare.FileTreeCellRenderer;
import offline.export.utils.FileUtils;
import offline.export.utils.PathUtils;

/**
 * 文件夹比较面板
 * 
 * @author liuyaoxing
 */
public class FileCompareJPanel extends FileCompareJPanelUI {

	/** 序列号 */
	private static final long serialVersionUID = -5952898543215887697L;

	private JPopupMenu leftPopupMenu, rightPopupMenu;

	private Set<File> ignoreFiles = new HashSet<>();

	public FileCompareJPanel(JFrame frame) {
		createPopupMenu();
		addListeners();
		syncVerticalScrolling();
		// initDnd();

		// 设置渲染器
		leftTree.setCellRenderer(new FileTreeCellRenderer());
		rightTree.setCellRenderer(new FileTreeCellRenderer());
	}

	private void createPopupMenu() {
		{
			leftPopupMenu = new JPopupMenu("操作");

			JMenuItem ignoreItem = new JMenuItem("忽略文件");
			ignoreItem.addActionListener(e -> ignoreSelectedNode(leftTree));
			leftPopupMenu.add(ignoreItem);

			JMenuItem copyToItem = new JMenuItem("复制到右侧");
			copyToItem.addActionListener(e -> copySelectedTo(leftTree, rightTree));
			leftPopupMenu.add(copyToItem);

			JMenuItem moveToItem = new JMenuItem("移动到右侧");
			moveToItem.addActionListener(e -> moveSelectedTo(leftTree, rightTree));
			leftPopupMenu.add(moveToItem);

			// 删除节点
			JMenuItem deleteItem = new JMenuItem("删除文件");
			deleteItem.addActionListener(e -> deleteSelectedNode(leftTree));
			leftPopupMenu.add(deleteItem);

			JMenuItem clearItem = new JMenuItem("删除空文件夹");
			clearItem.addActionListener(e -> deleteBlankDir(leftTree));
			leftPopupMenu.add(clearItem);

			JMenuItem deleteEqualItem = new JMenuItem("删除右侧相同文件");
			deleteEqualItem.addActionListener(e -> deleteEqualFiles(leftTree, rightTree));
			leftPopupMenu.add(deleteEqualItem);

			JMenuItem deleteInputFileItem = new JMenuItem("删除指定文件");
			deleteInputFileItem.addActionListener(e -> deleteInputFiles(leftTree));
			leftPopupMenu.add(deleteInputFileItem);

			JMenuItem explorerItem = new JMenuItem("查看文件");
			explorerItem.addActionListener(e -> showFileInExplorer(leftTree));
			leftPopupMenu.add(explorerItem);

			JMenuItem showInRightItem = new JMenuItem("显示在右侧");
			showInRightItem.addActionListener(e -> showInOtherTree(leftTree, rightTree));
			leftPopupMenu.add(showInRightItem);
		}

		{
			rightPopupMenu = new JPopupMenu("操作");

			JMenuItem ignoreItem = new JMenuItem("忽略文件");
			ignoreItem.addActionListener(e -> ignoreSelectedNode(leftTree));
			rightPopupMenu.add(ignoreItem);

			JMenuItem copyToItem = new JMenuItem("复制到左侧");
			copyToItem.addActionListener(e -> copySelectedTo(rightTree, leftTree));
			rightPopupMenu.add(copyToItem);

			JMenuItem moveToItem = new JMenuItem("移动到左侧");
			moveToItem.addActionListener(e -> moveSelectedTo(rightTree, leftTree));
			rightPopupMenu.add(moveToItem);

			// 删除节点
			JMenuItem deleteItem = new JMenuItem("删除文件");
			deleteItem.addActionListener(e -> deleteSelectedNode(rightTree));
			rightPopupMenu.add(deleteItem);

			JMenuItem clearItem = new JMenuItem("删除空文件夹");
			clearItem.addActionListener(e -> deleteBlankDir(rightTree));
			rightPopupMenu.add(clearItem);

			JMenuItem deleteEqualItem = new JMenuItem("删除左侧相同文件");
			deleteEqualItem.addActionListener(e -> deleteEqualFiles(rightTree, leftTree));
			rightPopupMenu.add(deleteEqualItem);

			JMenuItem deleteInputFileItem = new JMenuItem("删除指定文件");
			deleteInputFileItem.addActionListener(e -> deleteInputFiles(rightTree));
			rightPopupMenu.add(deleteInputFileItem);

			JMenuItem explorerItem = new JMenuItem("查看文件");
			explorerItem.addActionListener(e -> showFileInExplorer(rightTree));
			rightPopupMenu.add(explorerItem);

			JMenuItem showInRightItem = new JMenuItem("显示在左侧");
			showInRightItem.addActionListener(e -> showInOtherTree(rightTree, leftTree));
			rightPopupMenu.add(showInRightItem);
		}
	}

	private void addListeners() {
		leftBtn.addActionListener(e -> selectFolder(leftCombo));
		rightBtn.addActionListener(e -> selectFolder(rightCombo));
		refreshBtn.addActionListener(e -> performCompare());
		md5CompBtn.addActionListener(e -> performCompare());
		sizeDateCompBtn.addActionListener(e -> performCompare());
		showEqualsBtn.addActionListener(e -> performCompare());
		deleteEqualBtn.addActionListener(e -> deleteAllEqualFiles());
		expandAllBtn.addActionListener(e -> {
			expandAll(leftTree);
			expandAll(rightTree);
		});
		collapseAllBtn.addActionListener(e -> {
			collapseAll(leftTree);
			collapseAll(rightTree);
		});

		// 添加鼠标监听器，处理右键点击
		leftTree.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					TreePath path = leftTree.getPathForLocation(e.getX(), e.getY());
					if (path != null) {
						leftTree.setSelectionPath(path);
						leftPopupMenu.show(leftTree, e.getX(), e.getY());
					}
				}
			}
		});
		// 添加 TreeExpansionListener 监听展开/收缩事件
		leftTree.addTreeExpansionListener(new TreeExpansionListener() {
			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				if (synActionBtn.isSelected()) {
					TreePath path = event.getPath();
					Object lastPathComponent = path.getLastPathComponent();
					if (lastPathComponent instanceof CompareTreeNode) {
						TreePath rightPath = getRightPath(lastPathComponent);
						rightTree.expandPath(rightPath); // 展开
					}
				}
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
				if (synActionBtn.isSelected()) {
					TreePath path = event.getPath();
					Object lastPathComponent = path.getLastPathComponent();
					if (lastPathComponent instanceof CompareTreeNode) {
						TreePath rightPath = getRightPath(lastPathComponent);
						rightTree.collapsePath(rightPath); // 展开
					}
				}
			}

			private TreePath getRightPath(Object lastPathComponent) {
				CompareTreeNode treeNode = (CompareTreeNode) lastPathComponent;
				File rightFile = mapFile(treeNode.getFile(), new File(getComboText(leftCombo)), new File(getComboText(rightCombo)));
				TreePath rightPath = findPathByFile((CompareTreeNode) rightModel.getRoot(), rightFile);
				return rightPath;
			}
		});
		// 添加鼠标监听器，处理右键点击
		rightTree.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					TreePath path = rightTree.getPathForLocation(e.getX(), e.getY());
					if (path != null) {
						rightTree.setSelectionPath(path);
						rightPopupMenu.show(rightTree, e.getX(), e.getY());
					}
				}
			}
		});

		// 添加 TreeExpansionListener 监听展开/收缩事件
		rightTree.addTreeExpansionListener(new TreeExpansionListener() {
			@Override
			public void treeExpanded(TreeExpansionEvent event) {
				if (synActionBtn.isSelected()) {
					TreePath path = event.getPath();
					Object lastPathComponent = path.getLastPathComponent();
					if (lastPathComponent instanceof CompareTreeNode) {
						TreePath rightPath = getLeftPath(lastPathComponent);
						leftTree.expandPath(rightPath); // 展开
					}
				}
			}

			@Override
			public void treeCollapsed(TreeExpansionEvent event) {
				if (synActionBtn.isSelected()) {
					TreePath path = event.getPath();
					Object lastPathComponent = path.getLastPathComponent();
					if (lastPathComponent instanceof CompareTreeNode) {
						TreePath rightPath = getLeftPath(lastPathComponent);
						leftTree.collapsePath(rightPath); // 展开
					}
				}
			}

			private TreePath getLeftPath(Object lastPathComponent) {
				CompareTreeNode treeNode = (CompareTreeNode) lastPathComponent;
				File rightFile = mapFile(treeNode.getFile(), new File(getComboText(rightCombo)), new File(getComboText(rightCombo)));
				TreePath rightPath = findPathByFile((CompareTreeNode) leftModel.getRoot(), rightFile);
				return rightPath;
			}
		});
	}

	private File mapFile(File srcFile, String srcParent, String targetParent) {
		return mapFile(srcFile, new File(srcParent), new File(targetParent));
	}

	private File mapFile(File srcFile, File srcParent, File targetParent) {
		String relativize = PathUtils.relativize(srcParent, srcFile);
		return new File(targetParent, relativize);
	}

	/**
	 * 同步两个 JTree 的垂直滚动条
	 */
	private void syncVerticalScrolling() {
		// JScrollBar bar1 = leftJScrollPane.getVerticalScrollBar();
		// JScrollBar bar2 = rightJScrollPane.getVerticalScrollBar();

		// // 监听 bar1 的滚动变化
		// bar1.addAdjustmentListener(e -> {
		// if (!bar1.getValueIsAdjusting()) {
		// // 用户停止滚动时，同步到 bar2
		// bar2.setValue(bar1.getValue());
		// }
		// });
		//
		// // 监听 bar2 的滚动变化
		// bar2.addAdjustmentListener(e -> {
		// if (!bar2.getValueIsAdjusting()) {
		// bar1.setValue(bar2.getValue());
		// }
		// });
	}

	/**
	 * 递归查找指定节点对应的 TreePath
	 * 
	 * @param parent
	 *            当前父节点
	 * @param target
	 *            目标节点
	 * @return TreePath 或 null（未找到）
	 */
	public TreePath findPathByFile(CompareTreeNode parent, File target) {
		if (parent == null || target == null)
			return null;

		// 如果当前节点就是目标节点
		if (target.equals(parent.getFile())) {
			return new TreePath(parent);
		}

		// 遍历子节点
		Enumeration<TreeNode> children = parent.children();
		while (children.hasMoreElements()) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
			if (!(child instanceof CompareTreeNode))
				continue;
			TreePath path = findPathByFile((CompareTreeNode) child, target);
			if (path != null) {
				// 将当前节点加入路径
				Object[] pathArray = new Object[path.getPathCount() + 1];
				System.arraycopy(path.getPath(), 0, pathArray, 1, path.getPathCount());
				pathArray[0] = parent;
				return new TreePath(pathArray);
			}
		}

		return null;
	}

	private void deleteEqualFiles(JTree tree1, JTree tree2) {
		String srcParent = (tree1 == this.leftTree ? getComboText(leftCombo) : getComboText(rightCombo));
		String targetParent = (tree1 == this.leftTree ? getComboText(rightCombo) : getComboText(leftCombo));
		File selectedFile = getSelectedFile(tree1);
		Set<File> toDeletedFiles = new HashSet<>();
		if (selectedFile.isDirectory()) {
			Set<File> fileSet;
			try {
				fileSet = FileUtils.listFiles(selectedFile);
				for (File file : fileSet) {
					File targetFile = mapFile(file, srcParent, targetParent);
					if (isFileEqual(file, targetFile))
						toDeletedFiles.add(file);
				}
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, "比对文件失败:" + e.getMessage());
			}
		} else {
			File targetFile = mapFile(selectedFile, srcParent, targetParent);
			if (isFileEqual(selectedFile, targetFile))
				toDeletedFiles.add(targetFile);
		}
		if (toDeletedFiles.isEmpty()) {
			System.out.println("无需删除");
			return;
		}
		boolean confirmed = DeleteFileConfirmDialog.showConfirmDialog(null, toDeletedFiles);
		if (confirmed) {
			toDeletedFiles.forEach(f -> {
				if (f != null && f.isFile()) {
					if (!f.delete())
						System.err.println("Delete failed: " + f.getAbsolutePath());
				}
			});
			performCompare(false);
		} else {
			System.out.println("用户取消删除");
		}
	}

	private void deleteInputFiles(JTree tree) {
		String input = (String) JOptionPane.showInputDialog(null, "请输入要删除的文件或文件夹", "文件", JOptionPane.QUESTION_MESSAGE, null, null, "bin");
		if (input == null || input.trim().isEmpty())
			return;
		PathMatcher matcher = Paths.get("").getFileSystem().getPathMatcher("glob:" + input);
		File selectedFile = getSelectedFile(tree);
		Set<File> toDeletedFiles = new HashSet<>();
		if (selectedFile.isDirectory()) {
			Set<File> fileSet;
			try {
				fileSet = FileUtils.listFiles(selectedFile);
				for (File file : fileSet) {
					if (matcher.matches(Paths.get(file.getName())))
						toDeletedFiles.add(file);
				}
			} catch (IOException e) {
				e.printStackTrace();
				JOptionPane.showMessageDialog(this, "比对文件失败:" + e.getMessage());
			}
		} else {
			if (matcher.matches(selectedFile.toPath()))
				toDeletedFiles.add(selectedFile);
		}
		if (toDeletedFiles.isEmpty()) {
			System.out.println("无需删除");
			return;
		}
		boolean confirmed = DeleteFileConfirmDialog.showConfirmDialog(null, toDeletedFiles);
		if (confirmed) {
			toDeletedFiles.forEach(f -> {
				if (f != null && f.isFile()) {
					if (!f.delete())
						System.err.println("Delete failed: " + f.getAbsolutePath());
				}
			});
			performCompare(false);
		} else {
			System.out.println("用户取消删除");
		}
	}

	private void deleteAllEqualFiles() {
		if (!(leftModel.getRoot() instanceof CompareTreeNode))
			return;
		CompareTreeNode root = (CompareTreeNode) leftModel.getRoot();
		Set<File> toDeletedFiles = new HashSet<>();
		removeAllSubNodes(root, toDeletedFiles);
		boolean confirmed = DeleteFileConfirmDialog.showConfirmDialog(null, toDeletedFiles);
		if (confirmed) {
			toDeletedFiles.forEach(f -> {
				if (f != null && f.isFile()) {
					if (!f.delete())
						System.err.println("Delete failed: " + f.getAbsolutePath());
				}
			});
		} else {
			System.out.println("用户取消删除");
		}
	}

	private void removeAllSubNodes(DefaultMutableTreeNode parent, Set<File> toDeletedFiles) {
		for (Enumeration<? extends TreeNode> e = parent.children(); e.hasMoreElements();) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
			if (!(node instanceof CompareTreeNode))
				continue;
			CompareTreeNode treeNode = (CompareTreeNode) node;
			if (treeNode.getFile().isDirectory()) {
				removeAllSubNodes(treeNode, toDeletedFiles);
			} else {
				if (treeNode.getStatus() == CompareTreeNode.Status.MATCHED) {
					File rightFile = mapFile(treeNode.getFile(), new File(getComboText(leftCombo)), new File(getComboText(rightCombo)));
					System.out.println("删除左侧文件:" + treeNode.getFile() + ", 删除右侧文件:" + rightFile);
					toDeletedFiles.add(treeNode.getFile());
					toDeletedFiles.add(rightFile);
				}
			}
		}
	}

	private void selectFolder(JComboBox<String> field) {
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File(getComboText(field)));
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			addItemsToCombo(field, new String[]{chooser.getSelectedFile().getAbsolutePath()}, 0);
		}
	}

	private void performCompare() {
		performCompare(true);
	}

	private void performCompare(boolean showMsg) {
		new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				performCompare0(); // 耗时操作放后台
				return null;
			}

			@Override
			protected void done() {
				if (showMsg) {
					SwingUtilities.invokeLater(() -> {
						JOptionPane.showMessageDialog(null, "比较完成！");
					});
				}
			}
		}.execute();
	}

	// 执行比对的核心方法
	private void performCompare0() {
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

		if (isFileEqual(file1, file2) && file1.isFile() && file2.isFile()) {
			if (!showEqualsBtn.isSelected())
				return;
		}

		// 将右边的子项放入Map，便于查找
		java.util.Map<String, File> leftMap = new java.util.HashMap<>();
		for (File f : children1) {
			leftMap.put(f.getName(), f);
		}
		java.util.Map<String, File> rightMap = new java.util.HashMap<>();
		for (File f : children2) {
			rightMap.put(f.getName(), f);
		}

		// 遍历左边
		for (File f1 : children1) {
			CompareTreeNode.Status status;
			File f2 = rightMap.get(f1.getName());

			if (ignoreFiles.contains(f1))
				continue;

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

			if (status == CompareTreeNode.Status.MATCHED && f1.isFile() && f2.isFile()) {
				if (!showEqualsBtn.isSelected())
					continue;
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
					if (leftMap.containsKey(f2.getName())) {// 如果在左边也有
						File f1 = leftMap.get(f2.getName());
						if (isFileEqual(f1, f2) && f1.isFile() && f2.isFile()) {
							if (!showEqualsBtn.isSelected())
								continue;
						}
					}
					CompareTreeNode n2 = new CompareTreeNode(f2, f2.getName(), CompareTreeNode.Status.ADDED);
					node2.add(n2);
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

	// 折叠所有树节点
	private void collapseAll(JTree tree) {
		int row = 0;
		while (row < tree.getRowCount()) {
			TreePath path = tree.getPathForRow(row);
			if (tree.isExpanded(row)) {
				tree.collapsePath(path);
			}
			row++;
		}
	}

	// 简单的文件比较 (大小+修改时间)
	private boolean isFileEqual(File f1, File f2) {
		if (f1.length() != f2.length())
			return false;
		if (md5CompBtn.isSelected()) {
			try {
				if (FileUtils.contentEquals(f1, f2))
					return true;
			} catch (Exception e) {
			}
		}
		return Math.abs(f1.lastModified() - f2.lastModified()) < 2000;
	}

	public Set<File> listFiles(File srcFile) throws IOException {
		if (srcFile.isDirectory())
			return FileUtils.listFiles(srcFile);
		Set<File> fileSet = new HashSet<>();
		fileSet.add(srcFile);
		return fileSet;
	}

	private void ignoreSelectedNode(JTree tree) {
		ignoreFiles.add(getSelectedFile(tree));
	}

	private File copySelectedTo(JTree srcTree, JTree targetTree) {
		File selectedFile = getSelectedFile(srcTree);
		File targetFile = null;
		if (srcTree == this.leftTree)
			targetFile = mapFile(selectedFile, getComboText(leftCombo), getComboText(rightCombo));
		else
			targetFile = mapFile(selectedFile, getComboText(rightCombo), getComboText(leftCombo));
		try {
			if (selectedFile.isDirectory()) {
				FileUtils.copyDirectoryToDirectory(selectedFile, targetFile.getParentFile());
			} else {
				FileUtils.copyFile(selectedFile, targetFile);
			}
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "文件复制失败:" + e.getMessage());
		}
		performCompare(false);
		return targetFile;
	}

	private void moveSelectedTo(JTree srcTree, JTree targetTree) {
		try {
			File selectedFile = getSelectedFile(srcTree);
			copySelectedTo(srcTree, targetTree);
			FileUtils.forceDelete(selectedFile);
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "文件移动失败:" + e.getMessage());
		}
		performCompare(false);
	}

	private void deleteSelectedNode(JTree tree) {
		File selectedFile = getSelectedFile(tree);
		if (selectedFile == null) {
			JOptionPane.showMessageDialog(this, "请选中要删除的文件!");
			return;
		}
		try {
			Set<File> fileSet = listFiles(selectedFile);
			if (DeleteFileConfirmDialog.showConfirmDialog(this, fileSet)) {
				FileUtils.forceDelete(selectedFile);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "文件删除失败:" + ex.getMessage());
		}
	}

	private File getSelectedFile(JTree tree) {
		TreePath treePath = tree.getSelectionPath();
		if (treePath == null || treePath.getLastPathComponent() == null)
			return null;
		if (!(treePath.getLastPathComponent() instanceof CompareTreeNode))
			return null;
		CompareTreeNode compareTreeNode = (CompareTreeNode) treePath.getLastPathComponent();
		return compareTreeNode.getFile();
	}

	private void deleteBlankDir(JTree tree) {
		try {
			File selectedFile = getSelectedFile(tree);
			if (selectedFile != null)
				FileUtils.deleteBlankPath(selectedFile.getCanonicalPath());
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "删除空文件夹失败:" + ex.getMessage());
		}
	}

	private void showFileInExplorer(JTree tree) {
		try {
			File selectedFile = getSelectedFile(tree);
			if (selectedFile != null && selectedFile.exists()) {
				Desktop.getDesktop().open(selectedFile.getParentFile());
				if (Desktop.getDesktop().isSupported(Desktop.Action.EDIT))
					Desktop.getDesktop().edit(selectedFile);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			JOptionPane.showMessageDialog(this, "查看文件失败:" + ex.getMessage());
		}
	}

	private void showInOtherTree(JTree tree1, JTree tree2) {
		File selectedFile = getSelectedFile(tree1);
		if (selectedFile == null) {
			JOptionPane.showMessageDialog(this, "请选择要查看的文件");
			return;
		}
		if (tree1 == leftTree) {
			TreePath leftTreePath = findPathByFile((CompareTreeNode) leftModel.getRoot(), selectedFile);
			File rightFile = mapFile(selectedFile, new File(getComboText(leftCombo)), new File(getComboText(rightCombo)));
			TreePath rightTreePath = findPathByFile((CompareTreeNode) rightModel.getRoot(), rightFile);
			// 获取节点在树中的矩形位置
			Rectangle rectLeft = leftTree.getPathBounds(leftTreePath);
			Rectangle rectRight = rightTree.getPathBounds(rightTreePath);

			// 高亮匹配节点
			leftTree.setSelectionPath(leftTreePath);
			rightTree.setSelectionPath(rightTreePath);
			leftTree.scrollRectToVisible(rectLeft);
			rightTree.scrollRectToVisible(rectRight);
		} else if (tree1 == rightTree) {
			TreePath rightTreePath = findPathByFile((CompareTreeNode) rightModel.getRoot(), selectedFile);
			File leftFile = mapFile(selectedFile, new File(getComboText(rightCombo)), new File(getComboText(leftCombo)));
			TreePath leftTreePath = findPathByFile((CompareTreeNode) leftModel.getRoot(), leftFile);
			// 获取节点在树中的矩形位置
			Rectangle rectLeft = leftTree.getPathBounds(leftTreePath);
			Rectangle rectRight = rightTree.getPathBounds(rightTreePath);

			// 高亮匹配节点
			leftTree.setSelectionPath(leftTreePath);
			rightTree.setSelectionPath(rightTreePath);
			leftTree.scrollRectToVisible(rectLeft);
			rightTree.scrollRectToVisible(rectRight);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String propertyName = event.getPropertyName();
		if (PROP_LANPORT_SCAN_IP.equals(propertyName)) {
			// itemSet.add((String) event.getNewValue() + FOLDER_UPLOAD);
			// addItemsToCombo(urlCombo2, itemSet.toArray(new String[0]), 0);
		}
		super.propertyChange(event);
	}
}
