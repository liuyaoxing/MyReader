import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

public class FolderComparatorApp extends JFrame {

	private JTextField leftPathField;
	private JTextField rightPathField;
	private JTable resultTable;
	private DefaultTableModel tableModel;
	private JButton compareButton;

	// 表示比对结果的实体类
	static class DiffResult {
		String fileName;
		String fullPath;
		String status; // "相同", "仅在左边", "仅在右边", "不同"

		public DiffResult(String fileName, String fullPath, String status) {
			this.fileName = fileName;
			this.fullPath = fullPath;
			this.status = status;
		}
	}

	public FolderComparatorApp() {
		setTitle("文件夹比对工具");
		setSize(900, 600);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		initUI();
	}

	private void initUI() {
		setLayout(new BorderLayout());

		// 1. 顶部面板：用于选择路径
		JPanel topPanel = new JPanel(new GridLayout(2, 1));

		// 左边路径选择
		JPanel leftPanel = new JPanel(new FlowLayout());
		leftPanel.add(new JLabel("左边文件夹:"));
		leftPathField = new JTextField(30);
		leftPanel.add(leftPathField);
		JButton leftBtn = new JButton("浏览...");
		leftBtn.addActionListener((ActionEvent e) -> browseFolder(leftPathField));
		leftPanel.add(leftBtn);

		// 右边路径选择
		JPanel rightPanel = new JPanel(new FlowLayout());
		rightPanel.add(new JLabel("右边文件夹:"));
		rightPathField = new JTextField(30);
		rightPanel.add(rightPathField);
		JButton rightBtn = new JButton("浏览...");
		rightBtn.addActionListener((ActionEvent e) -> browseFolder(rightPathField));
		rightPanel.add(rightBtn);

		topPanel.add(leftPanel);
		topPanel.add(rightPanel);
		add(topPanel, BorderLayout.NORTH);

		// 2. 中间面板：结果显示表格
		String[] columns = {"文件名", "完整路径", "状态"};
		tableModel = new DefaultTableModel(columns, 0) {
			// 禁止用户编辑单元格
			@Override
			public boolean isCellEditable(int row, int column) {
				return false;
			}
		};

		resultTable = new JTable(tableModel);
		resultTable.setRowHeight(25); // 设置行高便于阅读
		add(new JScrollPane(resultTable), BorderLayout.CENTER);

		// 3. 底部面板：比对按钮
		JPanel bottomPanel = new JPanel(new FlowLayout());
		compareButton = new JButton("开始比对");
		compareButton.addActionListener((ActionEvent e) -> startComparison());
		bottomPanel.add(compareButton);
		add(bottomPanel, BorderLayout.SOUTH);

		// 初始化表格渲染器（用于给状态上色）
		resultTable.setDefaultRenderer(Object.class, new StatusCellRenderer());
	}

	// 浏览文件夹对话框
	private void browseFolder(JTextField field) {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int result = chooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			field.setText(chooser.getSelectedFile().getAbsolutePath());
		}
	}

	// 启动比对任务
	private void startComparison() {
		String path1 = leftPathField.getText().trim();
		String path2 = rightPathField.getText().trim();

		if (path1.isEmpty() || path2.isEmpty()) {
			JOptionPane.showMessageDialog(this, "请先选择两个文件夹路径！");
			return;
		}

		File folder1 = new File(path1);
		File folder2 = new File(path2);

		if (!folder1.exists() || !folder1.isDirectory()) {
			JOptionPane.showMessageDialog(this, "左边路径无效或不是文件夹！");
			return;
		}
		if (!folder2.exists() || !folder2.isDirectory()) {
			JOptionPane.showMessageDialog(this, "右边路径无效或不是文件夹！");
			return;
		}

		// 清空旧数据
		tableModel.setRowCount(0);
		compareButton.setText("比对中...");
		compareButton.setEnabled(false);

		// 使用 SwingWorker 防止界面卡死
		SwingWorker<List<DiffResult>, Void> worker = new SwingWorker<List<DiffResult>, Void>() {
			@Override
			protected List<DiffResult> doInBackground() throws Exception {
				return compareFolders(folder1, folder2);
			}

			@Override
			protected void done() {
				try {
					List<DiffResult> results = get();
					// 填充数据
					for (DiffResult res : results) {
						tableModel.addRow(new Object[]{res.fileName, res.fullPath, res.status});
					}
					JOptionPane.showMessageDialog(FolderComparatorApp.this, "比对完成，共找到 " + results.size() + " 项。");
				} catch (Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(FolderComparatorApp.this, "比对出错: " + ex.getMessage());
				} finally {
					compareButton.setText("开始比对");
					compareButton.setEnabled(true);
				}
			}
		};
		worker.execute();
	}

	// 核心比对逻辑
	private List<DiffResult> compareFolders(File leftRoot, File rightRoot) throws IOException {
		List<DiffResult> results = new ArrayList<>();

		// 获取两个文件夹下的所有文件（包含子目录）
		List<File> leftFiles = getAllFiles(leftRoot);
		List<File> rightFiles = getAllFiles(rightRoot);

		// 将右边的文件列表构建成一个 Map，以相对于根目录的路径为 key，便于快速查找
		// 例如：子文件夹/文件.txt
		java.util.Map<String, File> rightFileMap = new java.util.HashMap<>();
		for (File f : rightFiles) {
			String relativePath = getRelativePath(rightRoot, f);
			rightFileMap.put(relativePath, f);
		}

		// 遍历左边文件夹
		for (File leftFile : leftFiles) {
			String relativePath = getRelativePath(leftRoot, leftFile);

			if (rightFileMap.containsKey(relativePath)) {
				// 文件在两边都存在，检查内容是否相同
				File rightFile = rightFileMap.get(relativePath);
				if (isFileContentSame(leftFile, rightFile)) {
					results.add(new DiffResult(leftFile.getName(), leftFile.getAbsolutePath(), "相同"));
				} else {
					results.add(new DiffResult(leftFile.getName(), leftFile.getAbsolutePath(), "不同"));
				}
				// 从 Map 中移除，剩下的就是右边独有的
				rightFileMap.remove(relativePath);
			} else {
				// 仅在左边存在
				results.add(new DiffResult(leftFile.getName(), leftFile.getAbsolutePath(), "仅在左边"));
			}
		}

		// 剩下的 rightFileMap 中的文件就是仅在右边存在的
		for (File rightFile : rightFileMap.values()) {
			results.add(new DiffResult(rightFile.getName(), rightFile.getAbsolutePath(), "仅在右边"));
		}

		return results;
	}

	// 获取文件夹下所有文件（递归）
	private List<File> getAllFiles(File folder) {
		List<File> files = new ArrayList<>();
		File[] entries = folder.listFiles();
		if (entries != null) {
			for (File entry : entries) {
				if (entry.isDirectory()) {
					files.addAll(getAllFiles(entry)); // 递归
				} else {
					files.add(entry);
				}
			}
		}
		return files;
	}

	// 获取文件相对于根目录的路径（用于跨平台比较）
	private String getRelativePath(File root, File file) {
		return root.toURI().relativize(file.toURI()).getPath();
	}

	// 简单判断文件内容是否相同（比较大小和最后修改时间）
	// 如果需要绝对准确，可以计算 MD5，但会很慢
	private boolean isFileContentSame(File f1, File f2) {
		return f1.length() == f2.length() && Math.abs(f1.lastModified() - f2.lastModified()) < 1000; // 允许1秒误差
	}

	// 自定义表格渲染器，根据状态显示不同颜色
	class StatusCellRenderer extends JLabel implements TableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			setText(value != null ? value.toString() : "");

			String status = (String) table.getValueAt(row, 2);
			if (isSelected) {
				setBackground(table.getSelectionBackground());
				setForeground(table.getSelectionForeground());
			} else {
				setBackground(Color.WHITE); // 默认背景
				switch (status) {
					case "相同" :
						setForeground(Color.BLACK);
						break;
					case "不同" :
						setForeground(Color.ORANGE.darker());
						break;
					case "仅在左边" :
						setForeground(Color.RED);
						break;
					case "仅在右边" :
						setForeground(Color.BLUE);
						break;
					default :
						setForeground(Color.GRAY);
				}
			}
			setOpaque(true); // 必须设置为 true 才能显示背景色
			return this;
		}
	}

	// 主函数
	public static void main(String[] args) {
		// 设置系统外观风格（让界面看起来更像原生应用）
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}

		SwingUtilities.invokeLater(() -> {
			new FolderComparatorApp().setVisible(true);
		});
	}
}