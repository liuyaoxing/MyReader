package ui;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

public class FilterableJTreeExample extends JFrame {

	private JTree tree;
	private JTextField filterField;
	private DefaultTreeModel treeModel;
	private DefaultMutableTreeNode root;
	private List<DefaultMutableTreeNode> allNodes; // 保存所有节点，用于还原

	public FilterableJTreeExample() {
		setTitle("动态过滤 JTree 节点");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(500, 600);
		setLocationRelativeTo(null);

		// 初始化根节点
		root = new DefaultMutableTreeNode("根目录");
		allNodes = new ArrayList<>();

		// 添加测试数据
		addTestNodes(root);

		// 创建树模型
		treeModel = new DefaultTreeModel(root);
		tree = new JTree(treeModel);

		// 设置节点可展开
		tree.setRootVisible(true);
		tree.setShowsRootHandles(true);

		// 搜索框
		filterField = new JTextField(20);
		filterField.getDocument().addDocumentListener(new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				filter();
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				filter();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				filter();
			}
		});

		// 布局
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JLabel("过滤关键词："), BorderLayout.WEST);
		panel.add(filterField, BorderLayout.CENTER);

		JScrollPane scrollPane = new JScrollPane(tree);

		setLayout(new BorderLayout());
		add(panel, BorderLayout.NORTH);
		add(scrollPane, BorderLayout.CENTER);

		// 启动
		setVisible(true);
	}

	private void addTestNodes(DefaultMutableTreeNode parent) {
		DefaultMutableTreeNode node1 = new DefaultMutableTreeNode("用户管理");
		DefaultMutableTreeNode node2 = new DefaultMutableTreeNode("订单系统");
		DefaultMutableTreeNode node3 = new DefaultMutableTreeNode("日志审计");

		parent.add(node1);
		parent.add(node2);
		parent.add(node3);

		// 子节点
		node1.add(new DefaultMutableTreeNode("用户查询"));
		node1.add(new DefaultMutableTreeNode("用户新增"));
		node1.add(new DefaultMutableTreeNode("权限配置"));

		node2.add(new DefaultMutableTreeNode("订单查询"));
		node2.add(new DefaultMutableTreeNode("订单支付"));
		node2.add(new DefaultMutableTreeNode("订单退款"));

		node3.add(new DefaultMutableTreeNode("登录日志"));
		node3.add(new DefaultMutableTreeNode("操作日志"));
		node3.add(new DefaultMutableTreeNode("异常日志"));

		// 收集所有节点
		collectAllNodes(parent, allNodes);
	}

	private void collectAllNodes(DefaultMutableTreeNode node, List<DefaultMutableTreeNode> list) {
		list.add(node);
		for (int i = 0; i < node.getChildCount(); i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
			collectAllNodes(child, list);
		}
	}

	private void filter() {
		String keyword = filterField.getText().trim().toLowerCase();
		if (keyword.isEmpty()) {
			// 清空过滤，还原原始树
			treeModel.setRoot(root);
			treeModel.reload();
			return;
		}

		// 创建临时根节点，用于存储匹配的节点
		DefaultMutableTreeNode filteredRoot = new DefaultMutableTreeNode("过滤结果");

		// 递归查找匹配节点
		List<DefaultMutableTreeNode> matches = new ArrayList<>();
		findMatchingNodes(root, keyword, matches);

		// 重建树结构：将匹配的节点及其祖先路径重建
		rebuildTreeFromMatches(filteredRoot, matches);

		// 更新树模型
		treeModel.setRoot(filteredRoot);
		treeModel.reload();
	}

	private void findMatchingNodes(DefaultMutableTreeNode node, String keyword, List<DefaultMutableTreeNode> matches) {
		if (node.getUserObject() != null && node.getUserObject().toString().toLowerCase().contains(keyword)) {
			matches.add(node);
		}

		for (int i = 0; i < node.getChildCount(); i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
			findMatchingNodes(child, keyword, matches);
		}
	}

	private void rebuildTreeFromMatches(DefaultMutableTreeNode filteredRoot, List<DefaultMutableTreeNode> matches) {
		// 为每个匹配节点，向上追溯其祖先，确保路径完整
		for (DefaultMutableTreeNode match : matches) {
			DefaultMutableTreeNode current = match;
			List<DefaultMutableTreeNode> path = new ArrayList<>();

			// 收集从匹配节点到根的路径
			while (current != null) {
				path.add(0, current); // 逆序插入
				current = (DefaultMutableTreeNode) current.getParent();
			}

			// 从根开始，逐级添加路径
			DefaultMutableTreeNode parent = filteredRoot;
			for (int i = 1; i < path.size(); i++) { // 忽略根节点（已存在）
				DefaultMutableTreeNode node = path.get(i);
				boolean found = false;
				for (int j = 0; j < parent.getChildCount(); j++) {
					DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(j);
					if (Objects.equals(child.getUserObject(), node.getUserObject())) {
						parent = child;
						found = true;
						break;
					}
				}
				if (!found) {
					parent.add(node);
					parent = node;
				}
			}
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception e) {
				e.printStackTrace();
			}
			new FilterableJTreeExample();
		});
	}
}