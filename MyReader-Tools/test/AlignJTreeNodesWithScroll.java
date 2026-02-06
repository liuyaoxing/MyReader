import java.awt.BorderLayout;
import java.awt.Color;
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

public class AlignJTreeNodesWithScroll {
	public static void main(String[] args) {
		JFrame frame = new JFrame("两个 JTree 节点对齐并滚动到同一水平");
		frame.setLayout(new BorderLayout());

		// 创建两个 JTree
		JTree tree1 = new JTree();
		JTree tree2 = new JTree();

		// 构建树结构
		DefaultMutableTreeNode root1 = new DefaultMutableTreeNode("根节点1");
		root1.add(new DefaultMutableTreeNode("节点 A"));
		root1.add(new DefaultMutableTreeNode("节点 B"));
		root1.add(new DefaultMutableTreeNode("节点 C（对齐目标）")); // 要对齐的节点
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
		root2.add(new DefaultMutableTreeNode("节点 Z（对齐目标）")); // 要对齐的节点
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

		// 设置行高一致
		tree1.setRowHeight(24);
		tree2.setRowHeight(24);

		// 滚动面板
		JScrollPane scrollPane1 = new JScrollPane(tree1);
		JScrollPane scrollPane2 = new JScrollPane(tree2);

		// 设置固定宽度和高度
		scrollPane1.setPreferredSize(new Dimension(300, 300));
		scrollPane2.setPreferredSize(new Dimension(300, 300));

		// 添加到界面
		frame.add(scrollPane1, BorderLayout.WEST);
		frame.add(scrollPane2, BorderLayout.EAST);

		// 按钮：对齐并滚动到同一水平
		JButton alignBtn = new JButton("对齐节点 C 和 Z 并滚动");
		alignBtn.addActionListener(e -> {
			alignNodesAndScroll(tree1, tree2, "节点 C（对齐目标）", "节点 Z（对齐目标）");
		});

		frame.add(alignBtn, BorderLayout.SOUTH);
		frame.setSize(800, 450);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}

	/**
	 * 将两个 JTree 的指定节点对齐，并滚动到同一水平位置
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
	public static void alignNodesAndScroll(JTree tree1, JTree tree2, String text1, String text2) {
		// 1. 查找两个节点的路径
		TreePath path1 = findNodePath(tree1, text1);
		TreePath path2 = findNodePath(tree2, text2);

		if (path1 == null || path2 == null) {
			JOptionPane.showMessageDialog(null, "未找到指定节点，请检查文本是否匹配");
			return;
		}

		// 2. 获取节点在 JTree 中的显示区域（Rectangle）
		Rectangle rect1 = tree1.getPathBounds(path1);
		Rectangle rect2 = tree2.getPathBounds(path2);

		if (rect1 == null || rect2 == null) {
			JOptionPane.showMessageDialog(null, "节点未渲染，无法获取位置");
			return;
		}

		// 3. 计算目标 Y 坐标（取两个节点 Y 的平均值或取其中一个）
		// 推荐：以第一个节点为基准，滚动第二个节点对齐
		int y1 = rect1.y;
		int y2 = rect2.y;

		// 4. 滚动两个 JTree，使两个节点在同一水平线
		// 方法：先滚动 tree1 到 rect1 可见
		tree1.scrollRectToVisible(rect1);

		// 方法：再滚动 tree2 到 rect2 可见
		tree2.scrollRectToVisible(rect2);

		// ✅ 进阶：强制两个节点 Y 坐标一致（例如都对齐到 Y1）
		// 仅在需要“完全对齐”时使用
		int targetY = y1; // 可改为 (y1 + y2) / 2 实现居中对齐

		// 重新计算 tree2 的滚动位置，使其 Y 坐标与 tree1 一致
		// 计算滚动条新值
		JScrollPane scrollPane2 = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, tree2);
		JScrollBar vsb2 = scrollPane2.getVerticalScrollBar();

		int currentScroll = vsb2.getValue();
		int visibleHeight = vsb2.getVisibleAmount();
		int maxScroll = vsb2.getMaximum() - visibleHeight;

		// 目标滚动位置：使 rect2 的 Y 坐标 = targetY
		int newScroll = currentScroll + (y2 - targetY);

		// 限制滚动范围
		newScroll = Math.max(0, Math.min(newScroll, maxScroll));

		vsb2.setValue(newScroll);

		// 5. 选中节点（可选）
		tree1.setSelectionPath(path1);
		tree2.setSelectionPath(path2);

		// 6. 可选：高亮节点（加颜色）
		tree1.setBackground(Color.LIGHT_GRAY);
		tree2.setBackground(Color.LIGHT_GRAY);
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