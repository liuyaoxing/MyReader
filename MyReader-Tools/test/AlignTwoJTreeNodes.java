import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

public class AlignTwoJTreeNodes {
	public static void main(String[] args) {
		JFrame frame = new JFrame("对齐两个 JTree 的指定节点");
		frame.setLayout(new BorderLayout());

		// 创建两个 JTree
		JTree tree1 = new JTree();
		JTree tree2 = new JTree();

		// 构建树结构
		DefaultMutableTreeNode root1 = new DefaultMutableTreeNode("根节点1");
		root1.add(new DefaultMutableTreeNode("节点 A"));
		root1.add(new DefaultMutableTreeNode("节点 B"));
		root1.add(new DefaultMutableTreeNode("节点 C")); // 要对齐的节点
		root1.add(new DefaultMutableTreeNode("节点 X1"));
		root1.add(new DefaultMutableTreeNode("节点 Y1"));
		root1.add(new DefaultMutableTreeNode("节点 Z1")); // 要对齐的节点
		root1.add(new DefaultMutableTreeNode("节点 X2"));
		root1.add(new DefaultMutableTreeNode("节点 Y2"));
		root1.add(new DefaultMutableTreeNode("节点 Z2")); // 要对齐的节点
		root1.add(new DefaultMutableTreeNode("节点 X3"));
		root1.add(new DefaultMutableTreeNode("节点 Y3"));
		root1.add(new DefaultMutableTreeNode("节点 Z3")); // 要对齐的节点
		tree1.setModel(new DefaultTreeModel(root1));

		DefaultMutableTreeNode root2 = new DefaultMutableTreeNode("根节点2");
		root2.add(new DefaultMutableTreeNode("节点 X"));
		root2.add(new DefaultMutableTreeNode("节点 Y"));
		root2.add(new DefaultMutableTreeNode("节点 Z")); // 要对齐的节点
		root2.add(new DefaultMutableTreeNode("节点 X1"));
		root2.add(new DefaultMutableTreeNode("节点 Y1"));
		root2.add(new DefaultMutableTreeNode("节点 Z1")); // 要对齐的节点
		root2.add(new DefaultMutableTreeNode("节点 X2"));
		root2.add(new DefaultMutableTreeNode("节点 Y2"));
		root2.add(new DefaultMutableTreeNode("节点 Z2")); // 要对齐的节点
		root2.add(new DefaultMutableTreeNode("节点 X3"));
		root2.add(new DefaultMutableTreeNode("节点 Y3"));
		root2.add(new DefaultMutableTreeNode("节点 Z3")); // 要对齐的节点
		tree2.setModel(new DefaultTreeModel(root2));

		// 设置滚动面板
		JScrollPane scrollPane1 = new JScrollPane(tree1);
		JScrollPane scrollPane2 = new JScrollPane(tree2);

		// 保证两个面板高度一致
		scrollPane1.setPreferredSize(new Dimension(300, 300));
		scrollPane2.setPreferredSize(new Dimension(300, 300));

		// 添加到界面
		frame.add(scrollPane1, BorderLayout.WEST);
		frame.add(scrollPane2, BorderLayout.EAST);

		// 按钮：对齐指定节点
		JButton alignBtn = new JButton("对齐节点 C 和 Z");
		alignBtn.addActionListener(e -> {
			alignNodesVertically(tree1, tree2, "节点 C", "节点 Z");
		});

		frame.add(alignBtn, BorderLayout.SOUTH);
		frame.setSize(800, 400);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	/**
	 * 将两个 JTree 中的指定节点垂直对齐（Y 坐标一致）
	 *
	 * @param tree1
	 *            第一个 JTree
	 * @param tree2
	 *            第二个 JTree
	 * @param text1
	 *            要对齐的节点文本（在 tree1 中）
	 * @param text2
	 *            要对齐的节点文本（在 tree2 中）
	 */
	public static void alignNodesVertically(JTree tree1, JTree tree2, String text1, String text2) {
		// 1. 查找节点路径
		TreePath path1 = findNodePath(tree1, text1);
		TreePath path2 = findNodePath(tree2, text2);

		if (path1 == null || path2 == null) {
			JOptionPane.showMessageDialog(null, "未找到指定节点");
			return;
		}

		// 2. 获取节点在 JTree 中的显示位置（相对于 JTree 组件）
		Rectangle rect1 = tree1.getVisibleRect();
		Rectangle rect2 = tree2.getVisibleRect();

		// 获取节点在 JTree 中的绝对 Y 坐标（相对于 JTree 的起点）
		int y1 = tree1.getRowForPath(path1);
		int y2 = tree2.getRowForPath(path2);

		// 获取行高（用于计算实际 Y 坐标）
		int rowHeight = tree1.getRowHeight();
		int y1Real = y1 * rowHeight;
		int y2Real = y2 * rowHeight;

		// 3. 计算需要滚动的差值
		int offset = y1Real - y2Real;

		// 4. 滚动第二个 JTree，使其与第一个对齐
		JScrollPane scrollPane2 = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, tree2);
		JScrollBar vsb2 = scrollPane2.getVerticalScrollBar();

		// 获取当前滚动位置
		int currentScroll = vsb2.getValue();

		// 设置新滚动位置
		int newScroll = currentScroll + offset;

		// 限制滚动范围
		int maxScroll = vsb2.getMaximum() - vsb2.getVisibleAmount();
		newScroll = Math.max(0, Math.min(newScroll, maxScroll));

		vsb2.setValue(newScroll);

		// 5. 选中节点（可选）
		tree1.setSelectionPath(path1);
		tree2.setSelectionPath(path2);
	}

	/**
	 * 在 JTree 中查找指定文本的节点路径
	 */
	private static TreePath findNodePath(JTree tree, String text) {
		DefaultMutableTreeNode root = (DefaultMutableTreeNode) tree.getModel().getRoot();
		return findNodePathRecursive(root, text);
	}

	private static TreePath findNodePathRecursive(DefaultMutableTreeNode node, String text) {
		if (node.toString().equals(text)) {
			return new TreePath(node.getPath());
		}
		for (int i = 0; i < node.getChildCount(); i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
			TreePath path = findNodePathRecursive(child, text);
			if (path != null)
				return path;
		}
		return null;
	}
}