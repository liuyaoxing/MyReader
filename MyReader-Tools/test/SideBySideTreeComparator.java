import javax.swing.*;
import javax.swing.tree.*;
import java.awt.*;
import java.util.*;

public class SideBySideTreeComparator extends JFrame {
    private JTree treeLeft, treeRight;
    private JScrollPane scrollPaneLeft, scrollPaneRight;
    private Map<String, TreePath> nodeMapLeft, nodeMapRight;
    private Set<String> matchedNodes;

    public SideBySideTreeComparator() {
        super("双侧 JTree 比较工具（像 Beyond Compare）");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // 初始化两个树
        treeLeft = new JTree();
        treeRight = new JTree();

        // 设置行高一致
        treeLeft.setRowHeight(24);
        treeRight.setRowHeight(24);

        // 构建左侧树（原始）
        DefaultMutableTreeNode rootLeft = new DefaultMutableTreeNode("Root");
        rootLeft.add(new DefaultMutableTreeNode("A"));
        DefaultMutableTreeNode nodeB = new DefaultMutableTreeNode("B");
        nodeB.add(new DefaultMutableTreeNode("C"));
        nodeB.add(new DefaultMutableTreeNode("D"));
        rootLeft.add(nodeB);
        rootLeft.add(new DefaultMutableTreeNode("E"));
        treeLeft.setModel(new DefaultTreeModel(rootLeft));

        // 构建右侧树（修改后）
        DefaultMutableTreeNode rootRight = new DefaultMutableTreeNode("Root");
        rootRight.add(new DefaultMutableTreeNode("A"));
        DefaultMutableTreeNode nodeB2 = new DefaultMutableTreeNode("B");
        nodeB2.add(new DefaultMutableTreeNode("C"));
        nodeB2.add(new DefaultMutableTreeNode("X")); // 不同
        rootRight.add(nodeB2);
        rootRight.add(new DefaultMutableTreeNode("F")); // 不同
        treeRight.setModel(new DefaultTreeModel(rootRight));

        // 构建节点映射（用于匹配）
        nodeMapLeft = buildNodeMap(rootLeft);
        nodeMapRight = buildNodeMap(rootRight);

        // 找出相同节点
        matchedNodes = findMatchedNodes();

        // 设置滚动面板
        scrollPaneLeft = new JScrollPane(treeLeft);
        scrollPaneRight = new JScrollPane(treeRight);

        scrollPaneLeft.setPreferredSize(new Dimension(350, 400));
        scrollPaneRight.setPreferredSize(new Dimension(350, 400));

        // 添加到界面
        add(scrollPaneLeft, BorderLayout.WEST);
        add(scrollPaneRight, BorderLayout.EAST);

        // 按钮：对齐相同节点
        JButton alignBtn = new JButton("对齐相同节点（自动滚动）");
        alignBtn.addActionListener(e -> alignMatchedNodes());

        add(alignBtn, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // 构建节点路径映射：文本 -> TreePath
    private Map<String, TreePath> buildNodeMap(DefaultMutableTreeNode root) {
        Map<String, TreePath> map = new HashMap<>();
        buildNodeMapRecursive(root, new TreePath(root), map);
        return map;
    }

    private void buildNodeMapRecursive(DefaultMutableTreeNode node, TreePath path, Map<String, TreePath> map) {
        String text = node.toString();
        map.put(text, path);
        for (int i = 0; i < node.getChildCount(); i++) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            TreePath childPath = path.pathByAddingChild(child);
            buildNodeMapRecursive(child, childPath, map);
        }
    }

    // 查找相同节点（文本相同）
    private Set<String> findMatchedNodes() {
        Set<String> matched = new HashSet<>();
        for (String key : nodeMapLeft.keySet()) {
            if (nodeMapRight.containsKey(key)) {
                matched.add(key);
            }
        }
        return matched;
    }

    // 对齐所有匹配节点（滚动到同一水平线）
    public void alignMatchedNodes() {
        // 重置颜色
        clearHighlight();

        // 获取滚动面板
        JScrollPane scrollPaneLeft = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, treeLeft);
        JScrollPane scrollPaneRight = (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, treeRight);

        JScrollBar vsbLeft = scrollPaneLeft.getVerticalScrollBar();
        JScrollBar vsbRight = scrollPaneRight.getVerticalScrollBar();

        // 找出所有匹配节点，对齐它们
        int maxScroll = 0;

        for (String nodeName : matchedNodes) {
            TreePath pathLeft = nodeMapLeft.get(nodeName);
            TreePath pathRight = nodeMapRight.get(nodeName);

            if (pathLeft == null || pathRight == null) continue;

            // 获取节点在树中的矩形位置
            Rectangle rectLeft = treeLeft.getPathBounds(pathLeft);
            Rectangle rectRight = treeRight.getPathBounds(pathRight);

            if (rectLeft == null || rectRight == null) continue;

            // 以左侧为基准，滚动右侧对齐
            int yLeft = rectLeft.y;
            int yRight = rectRight.y;

            // 滚动右侧到与左侧对齐
            int currentScroll = vsbRight.getValue();
            int visibleHeight = vsbRight.getVisibleAmount();
            int maxScrollRight = vsbRight.getMaximum() - visibleHeight;

            int newScroll = currentScroll + (yRight - yLeft);
            newScroll = Math.max(0, Math.min(newScroll, maxScrollRight));

            vsbRight.setValue(newScroll);

            // 高亮匹配节点
            treeLeft.setSelectionPath(pathLeft);
            treeRight.setSelectionPath(pathRight);
            treeLeft.setBackground(Color.LIGHT_GRAY);
            treeRight.setBackground(Color.LIGHT_GRAY);

            // 保存最大滚动值（用于同步）
            maxScroll = Math.max(maxScroll, newScroll);
        }

        // 可选：同步左侧滚动（如果需要双侧联动）
        vsbLeft.setValue(maxScroll);

        // 重绘
        treeLeft.repaint();
        treeRight.repaint();
    }

    // 清除高亮
    private void clearHighlight() {
        treeLeft.setBackground(null);
        treeRight.setBackground(null);
        treeLeft.setSelectionPath(null);
        treeRight.setSelectionPath(null);
    }

    // 主入口
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new SideBySideTreeComparator();
        });
    }
}