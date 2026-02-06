import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.util.Enumeration;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public class JTreeFolderComparator extends JFrame {

	private JTextField leftPathField;
	private JTextField rightPathField;
	private JTree leftTree;
	private JTree rightTree;
	private DefaultTreeModel leftModel;
	private DefaultTreeModel rightModel;
	private JSplitPane splitPane;

	public JTreeFolderComparator() {
		setTitle("JTree 文件夹比对工具");
		setSize(1000, 700);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		initUI();
	}

	private void initUI() {
		setLayout(new BorderLayout());

		// 1. 顶部路径选择面板
		JPanel topPanel = new JPanel(new GridLayout(2, 1, 5, 5));
		topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		// 左边
		JPanel p1 = new JPanel(new FlowLayout());
		p1.add(new JLabel("源文件夹:"));
		leftPathField = new JTextField(30);
		p1.add(leftPathField);
		JButton btn1 = new JButton("浏览");
		btn1.addActionListener(e -> browseFolder(leftPathField));
		p1.add(btn1);
		topPanel.add(p1);

		// 右边
		JPanel p2 = new JPanel(new FlowLayout());
		p2.add(new JLabel("目标文件夹:"));
		rightPathField = new JTextField(30);
		p2.add(rightPathField);
		JButton btn2 = new JButton("浏览");
		btn2.addActionListener(e -> browseFolder(rightPathField));
		p2.add(btn2);
		topPanel.add(p2);

		add(topPanel, BorderLayout.NORTH);

		// 2. 中间树形展示 (使用分割窗格)
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

		// 初始化空的树模型
		CompareTreeNode leftRoot = new CompareTreeNode(null, "源目录", CompareTreeNode.Status.UNKNOWN);
		leftModel = new DefaultTreeModel(leftRoot);
		leftTree = new JTree(leftModel);
		leftTree.setCellRenderer(new CompareTreeCellRenderer());
		leftTree.setRootVisible(true);
		splitPane.setLeftComponent(new JScrollPane(leftTree));

		CompareTreeNode rightRoot = new CompareTreeNode(null, "目标目录", CompareTreeNode.Status.UNKNOWN);
		rightModel = new DefaultTreeModel(rightRoot);
		rightTree = new JTree(rightModel);
		rightTree.setCellRenderer(new CompareTreeCellRenderer());
		rightTree.setRootVisible(true);
		splitPane.setRightComponent(new JScrollPane(rightTree));

		splitPane.setDividerLocation(0.5);
		add(splitPane, BorderLayout.CENTER);

		splitPane.setDividerLocation(0.5);

		// 3. 底部按钮
		JPanel bottomPanel = new JPanel(new FlowLayout());
		JButton compareBtn = new JButton("开始比对");
		compareBtn.addActionListener(e -> performCompare());
		bottomPanel.add(compareBtn);
		add(bottomPanel, BorderLayout.SOUTH);
	}

	private void browseFolder(JTextField field) {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			field.setText(chooser.getSelectedFile().getAbsolutePath());
		}
	}

	// 执行比对的核心方法
	private void performCompare() {
		splitPane.setDividerLocation(0.5);

		String path1 = leftPathField.getText().trim();
		String path2 = rightPathField.getText().trim();

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

	// 简单的文件比较 (大小+修改时间)
	private boolean isFileEqual(File f1, File f2) {
		return f1.length() == f2.length() && Math.abs(f1.lastModified() - f2.lastModified()) < 2000;
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

	// 自定义树节点类
	static class CompareTreeNode extends DefaultMutableTreeNode {
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

	// 自定义渲染器
	static class CompareTreeCellRenderer extends DefaultTreeCellRenderer {
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
					case ADDED :
						setIcon(UIManager.getIcon("FileView.fileIcon"));
						setForeground(COLOR_ADDED);
						break;
					case DELETED :
						setIcon(UIManager.getIcon("FileView.computerIcon")); // 或者用一个带删除线的图标
						setForeground(COLOR_DELETED);
						break;
					case MODIFIED :
						setIcon(UIManager.getIcon("FileView.floppyDriveIcon"));
						setForeground(COLOR_MODIFIED);
						break;
					default :
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

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e) {
				e.printStackTrace();
			}
			new JTreeFolderComparator().setVisible(true);
		});
	}
}