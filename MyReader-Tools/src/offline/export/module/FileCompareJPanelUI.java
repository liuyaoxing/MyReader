package offline.export.module;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultTreeModel;

import offline.export.module.compare.CompareTreeCellRenderer;
import offline.export.module.compare.CompareTreeNode;

public class FileCompareJPanelUI extends MyReaderPanel {

	/** 序列号 */
	private static final long serialVersionUID = 1431561609475052749L;

	protected String[] uploadColumnNames = new String[] { KEY_ID, KEY_FILENAME, COL_PROGRESS, KEY_FILEPATH, KEY_LENGTH, "修改时间" };
	protected int[] uploadColumnWidths = new int[] { 6, 100, 6, 360, 10, 88 };

	protected DefaultTreeModel uploadTableModel;
	private JPanel panel1;
	private JPanel panel2;
	protected JComboBox<String> leftCombo;
	protected JButton leftBtn;
	protected JComboBox<String> rightCombo;
	protected JButton rightBtn;
	protected JCheckBox showEqualsBtn;
	protected JButton deleteEqulasBtn;
	protected JButton refreshBtn;
	protected JSplitPane splitPane;

	protected JTree leftTree;
	protected JTree rightTree;
	protected DefaultTreeModel leftModel;
	protected DefaultTreeModel rightModel;

	protected CompareTreeNode leftRoot, rightRoot;

	public FileCompareJPanelUI() {
		setLayout(new BorderLayout(0, 0));

		JPanel titlePanel = new JPanel();
		add(titlePanel, BorderLayout.NORTH);
		titlePanel.setLayout(new GridLayout(0, 1, 0, 0));

		panel1 = new JPanel();
		titlePanel.add(panel1);
		GridBagLayout gbl_panel1 = new GridBagLayout();
		gbl_panel1.columnWidths = new int[] { 0, 112, 0, 0, 112, 0, 0 };
		gbl_panel1.rowHeights = new int[] { 23 };
		gbl_panel1.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		gbl_panel1.rowWeights = new double[] { 0.0 };
		panel1.setLayout(gbl_panel1);

		JLabel lblNewLabel = new JLabel("文件夹1");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel.gridx = 0;
		gbc_lblNewLabel.gridy = 0;
		panel1.add(lblNewLabel, gbc_lblNewLabel);

		leftCombo = new JComboBox<String>();
		leftCombo.setToolTipText("请输入读乐乐服务URL");
		leftCombo.setEditable(true);
		GridBagConstraints gbc_leftCombo = new GridBagConstraints();
		gbc_leftCombo.weightx = 1.0;
		gbc_leftCombo.fill = GridBagConstraints.BOTH;
		gbc_leftCombo.insets = new Insets(0, 0, 0, 5);
		gbc_leftCombo.gridx = 1;
		gbc_leftCombo.gridy = 0;
		panel1.add(leftCombo, gbc_leftCombo);

		leftBtn = new JButton("选择");
		GridBagConstraints gbc_leftBtn = new GridBagConstraints();
		gbc_leftBtn.insets = new Insets(0, 0, 0, 5);
		gbc_leftBtn.gridx = 2;
		gbc_leftBtn.gridy = 0;
		panel1.add(leftBtn, gbc_leftBtn);

		JLabel lblNewLabel_1 = new JLabel("文件夹2");
		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
		gbc_lblNewLabel_1.insets = new Insets(0, 0, 0, 5);
		gbc_lblNewLabel_1.gridx = 3;
		gbc_lblNewLabel_1.gridy = 0;
		panel1.add(lblNewLabel_1, gbc_lblNewLabel_1);

		rightCombo = new JComboBox<String>();
		rightCombo.setToolTipText("请输入读乐乐服务URL");
		rightCombo.setEditable(true);
		GridBagConstraints gbc_rightCombo = new GridBagConstraints();
		gbc_rightCombo.weightx = 1.0;
		gbc_rightCombo.fill = GridBagConstraints.BOTH;
		gbc_rightCombo.insets = new Insets(0, 0, 0, 5);
		gbc_rightCombo.gridx = 4;
		gbc_rightCombo.gridy = 0;
		panel1.add(rightCombo, gbc_rightCombo);

		rightBtn = new JButton("选择");
		GridBagConstraints gbc_rightBtn = new GridBagConstraints();
		gbc_rightBtn.insets = new Insets(0, 0, 0, 5);
		gbc_rightBtn.gridx = 5;
		gbc_rightBtn.gridy = 0;
		panel1.add(rightBtn, gbc_rightBtn);

		refreshBtn = new JButton("刷新");
		GridBagConstraints gbc_refreshBtn = new GridBagConstraints();
		gbc_refreshBtn.gridx = 6;
		gbc_refreshBtn.gridy = 0;
		panel1.add(refreshBtn, gbc_refreshBtn);

		panel2 = new JPanel();
		titlePanel.add(panel2);
		panel2.setLayout(new BoxLayout(panel2, BoxLayout.X_AXIS));

		showEqualsBtn = new JCheckBox("查看相同");
		panel2.add(showEqualsBtn);

		deleteEqulasBtn = new JButton("删除相同");
		panel2.add(deleteEqulasBtn);

		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		add(splitPane);

		// 初始化空的树模型
		leftRoot = new CompareTreeNode(null, "源目录", CompareTreeNode.Status.UNKNOWN);
		leftModel = new DefaultTreeModel(leftRoot);
		leftTree = new JTree(leftModel);
		leftTree.setCellRenderer(new CompareTreeCellRenderer());
		leftTree.setRootVisible(true);
		splitPane.setLeftComponent(new JScrollPane(leftTree));

		rightRoot = new CompareTreeNode(null, "目标目录", CompareTreeNode.Status.UNKNOWN);
		rightModel = new DefaultTreeModel(rightRoot);
		rightTree = new JTree(rightModel);
		rightTree.setCellRenderer(new CompareTreeCellRenderer());
		rightTree.setRootVisible(true);
		splitPane.setRightComponent(new JScrollPane(rightTree));

		add(splitPane, BorderLayout.CENTER);

		deriveFontStyleSize(leftTree, -1, 0);
		deriveFontStyleSize(rightTree, -1, 0);

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				splitPane.setDividerLocation(0.5);
			}
		});
	}
}
