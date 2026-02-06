import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import offline.export.utils.FileUtils;

public class FileComparePanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1670728073039192883L;
	private JTree leftTree, rightTree;
	private JScrollPane leftScrollPane, rightScrollPane;
	private DefaultTreeModel leftModel, rightModel;
	private DefaultMutableTreeNode leftRoot, rightRoot;
	private JTextArea leftTextArea, rightTextArea;
	private JLabel statusLabel;

	private final Map<String, File> leftFileMap = new HashMap<>();
	private final Map<String, File> rightFileMap = new HashMap<>();

	public FileComparePanel() {
		setLayout(new BorderLayout());
		initUI();
	}

	private void initUI() {
		// === 左右树结构 ===
		leftTree = new JTree();
		rightTree = new JTree();
		leftScrollPane = new JScrollPane(leftTree);
		rightScrollPane = new JScrollPane(rightTree);

		leftModel = new DefaultTreeModel(new DefaultMutableTreeNode("Left"));
		rightModel = new DefaultTreeModel(new DefaultMutableTreeNode("Right"));
		leftRoot = (DefaultMutableTreeNode) leftModel.getRoot();
		rightRoot = (DefaultMutableTreeNode) rightModel.getRoot();

		leftTree.setModel(leftModel);
		rightTree.setModel(rightModel);

		leftTree.setRootVisible(true);
		rightTree.setRootVisible(true);

		// === 内容区域 ===
		leftTextArea = new JTextArea();
		rightTextArea = new JTextArea();
		leftTextArea.setEditable(false);
		rightTextArea.setEditable(false);

		JSplitPane contentSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(leftTextArea), new JScrollPane(rightTextArea));
		contentSplitPane.setDividerLocation(500);

		// === 状态栏 ===
		statusLabel = new JLabel("请选择目录进行比对");
		statusLabel.setBorder(BorderFactory.createLoweredBevelBorder());

		// === 布局 ===
		JSplitPane treeSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftScrollPane, rightScrollPane);
		treeSplitPane.setDividerLocation(300);

		add(treeSplitPane, BorderLayout.CENTER);
		add(contentSplitPane, BorderLayout.SOUTH);
		add(statusLabel, BorderLayout.NORTH);

		// === 右键菜单 ===
		setupRightClickMenu();

		// === 事件监听 ===
		leftTree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					showFileContent(leftTree, e.getPoint());
				}
			}
		});

		rightTree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					showFileContent(rightTree, e.getPoint());
				}
			}
		});

		// === 滚动对齐监听 ===
		leftTree.addTreeSelectionListener(e -> {
			showInOtherTree(leftTree, rightTree);
		});

		rightTree.addTreeSelectionListener(e -> {
			showInOtherTree(rightTree, leftTree);
		});
	}

	private void setupRightClickMenu() {
		JPopupMenu popup = new JPopupMenu();
		JMenuItem openItem = new JMenuItem("打开文件");
		JMenuItem compareItem = new JMenuItem("对比内容");
		openItem.addActionListener(e -> {
			JTree tree = (JTree) popup.getInvoker();
			TreePath path = tree.getSelectionPath();
			if (path == null)
				return;
			Object node = path.getLastPathComponent();
			if (node instanceof DefaultMutableTreeNode) {
				File file = (File) ((DefaultMutableTreeNode) node).getUserObject();
				openFile(file);
			}
		});
		compareItem.addActionListener(e -> {
			JTree tree = (JTree) popup.getInvoker();
			TreePath path = tree.getSelectionPath();
			if (path == null)
				return;
			Object node = path.getLastPathComponent();
			if (node instanceof DefaultMutableTreeNode) {
				File file = (File) ((DefaultMutableTreeNode) node).getUserObject();
				showFileContent(tree, null);
			}
		});
		popup.add(openItem);
		popup.add(compareItem);

		leftTree.setComponentPopupMenu(popup);
		rightTree.setComponentPopupMenu(popup);
	}

	public void loadDirectory(String leftPath, String rightPath) {
		leftRoot.removeAllChildren();
		rightRoot.removeAllChildren();
		leftFileMap.clear();
		rightFileMap.clear();

		File leftDir = new File(leftPath);
		File rightDir = new File(rightPath);

		if (!leftDir.exists() || !leftDir.isDirectory()) {
			JOptionPane.showMessageDialog(this, "左目录不存在：" + leftPath);
			return;
		}
		if (!rightDir.exists() || !rightDir.isDirectory()) {
			JOptionPane.showMessageDialog(this, "右目录不存在：" + rightPath);
			return;
		}

		buildTree(leftDir, leftRoot, leftFileMap);
		buildTree(rightDir, rightRoot, rightFileMap);

		leftModel.reload();
		rightModel.reload();

		statusLabel.setText("已加载：" + leftPath + " vs " + rightPath);
	}

	private void buildTree(File dir, DefaultMutableTreeNode parent, Map<String, File> fileMap) {
		File[] files = dir.listFiles();
		if (files == null)
			return;

		for (File file : files) {
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(file);
			parent.add(node);

			fileMap.put(file.getAbsolutePath(), file);

			if (file.isDirectory()) {
				buildTree(file, node, fileMap);
			}
		}
	}

	private void showFileContent(JTree tree, Point point) {
		TreePath path = tree.getSelectionPath();
		if (path == null)
			return;

		Object node = path.getLastPathComponent();
		if (!(node instanceof DefaultMutableTreeNode))
			return;

		File file = (File) ((DefaultMutableTreeNode) node).getUserObject();
		if (!file.isFile())
			return;

		try {
			String content = FileUtils.readFileToString(file, "utf-8");// Files.readString(Paths.get(file.getAbsolutePath()),
																		// StandardCharsets.UTF_8);
			JTextArea textArea = (tree == leftTree) ? leftTextArea : rightTextArea;
			textArea.setText(content);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "读取文件失败：" + file.getName());
		}
	}

	private void showInOtherTree(JTree tree1, JTree tree2) {
		TreePath selectedPath = tree1.getSelectionPath();
		if (selectedPath == null)
			return;

		Object node = selectedPath.getLastPathComponent();
		if (!(node instanceof DefaultMutableTreeNode))
			return;

		File selectedFile = (File) ((DefaultMutableTreeNode) node).getUserObject();
		if (!selectedFile.isFile())
			return;

		String relativePath = selectedFile.getAbsolutePath();
		if (tree1 == leftTree) {
			// 左 -> 右：相对路径
			String leftRootPath = ((File) leftRoot.getUserObject()).getAbsolutePath();
			relativePath = relativePath.substring(leftRootPath.length());
		} else {
			// 右 -> 左
			String rightRootPath = ((File) rightRoot.getUserObject()).getAbsolutePath();
			relativePath = relativePath.substring(rightRootPath.length());
		}

		// 查找对应文件
		File otherFile = new File((tree1 == leftTree ? rightRoot.getUserObject() : leftRoot.getUserObject()).toString() + relativePath);
		TreePath otherPath = findPathByFile(tree2, otherFile);
		if (otherPath != null) {
			tree2.setSelectionPath(otherPath);
			tree2.scrollPathToVisible(otherPath);
		}
	}

	private TreePath findPathByFile(JTree tree, File target) {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
		return findPathByFile(root, target);
	}

	private TreePath findPathByFile(DefaultMutableTreeNode parent, File target) {
		if (parent == null || target == null)
			return null;
		if (Objects.equals(parent.getUserObject(), target)) {
			return new TreePath(parent.getPath());
		}
		for (int i = 0; i < parent.getChildCount(); i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);
			TreePath path = findPathByFile(child, target);
			if (path != null)
				return path;
		}
		return null;
	}

	private void openFile(File file) {
		try {
			Desktop.getDesktop().open(file);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(this, "无法打开文件：" + file.getName());
		}
	}

	// === 测试入口 ===
	public static void main(String[] args) {
		JFrame frame = new JFrame("文件结构与内容比对工具");
		FileComparePanel panel = new FileComparePanel();
		frame.add(panel, BorderLayout.CENTER);

		JButton loadBtn = new JButton("加载目录");
		loadBtn.addActionListener(e -> {
			JFileChooser fc = new JFileChooser();
			fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			int result = fc.showOpenDialog(frame);
			if (result == JFileChooser.APPROVE_OPTION) {
				File leftDir = fc.getSelectedFile();
				JFileChooser fc2 = new JFileChooser();
				fc2.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int result2 = fc2.showOpenDialog(frame);
				if (result2 == JFileChooser.APPROVE_OPTION) {
					File rightDir = fc2.getSelectedFile();
					panel.loadDirectory(leftDir.getAbsolutePath(), rightDir.getAbsolutePath());
				}
			}
		});

		frame.add(loadBtn, BorderLayout.NORTH);
		frame.setSize(1200, 700);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}