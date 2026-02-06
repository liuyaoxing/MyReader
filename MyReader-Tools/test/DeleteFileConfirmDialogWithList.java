import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;

public class DeleteFileConfirmDialogWithList {

	/**
	 * 显示删除文件确认对话框，使用 JList 列出要删除的文件
	 *
	 * @param parent
	 *            父组件（可为 null）
	 * @param files
	 *            要删除的文件列表
	 * @return true 表示用户确认删除，false 表示取消
	 */
	public static boolean showConfirmDialog(Component parent, List<File> files) {
		if (files == null || files.isEmpty()) {
			return true; // 无文件直接返回 true
		}

		// 创建 JList，使用 File 对象作为元素
		DefaultListModel<File> model = new DefaultListModel<>();
		for (File file : files) {
			model.addElement(file);
		}

		JList<File> fileList = new JList<>(model);
		fileList.setCellRenderer(new FileListCellRenderer());
		fileList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		fileList.setVisibleRowCount(6); // 显示 6 行，自动触发滚动条

		// 包装在滚动面板中
		JScrollPane scrollPane = new JScrollPane(fileList);
		scrollPane.setPreferredSize(new Dimension(600, 200));

		// 构建对话框内容面板
		JPanel panel = new JPanel(new BorderLayout());
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		JLabel label = new JLabel("即将删除以下 " + files.size() + " 个文件：");
		label.setFont(new Font("宋体", Font.BOLD, 12));
		label.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(label, BorderLayout.NORTH);
		panel.add(scrollPane, BorderLayout.CENTER);

		// 设置图标
		Icon warningIcon = UIManager.getIcon("OptionPane.warningIcon");

		// 弹出确认对话框
		int result = JOptionPane.showConfirmDialog(parent, panel, "确认删除文件", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE, warningIcon);

		return result == JOptionPane.OK_OPTION;
	}

	/**
	 * 自定义 JList 单元格渲染器，用于显示文件路径
	 */
	private static class FileListCellRenderer extends JLabel implements ListCellRenderer<File> {
		public FileListCellRenderer() {
			setOpaque(true);
			setFont(new Font("宋体", Font.PLAIN, 12));
			setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
		}

		@Override
		public Component getListCellRendererComponent(JList<? extends File> list, File value, int index, boolean isSelected, boolean cellHasFocus) {
			setText(value.getAbsolutePath());
			if (isSelected) {
				setBackground(list.getSelectionBackground());
				setForeground(list.getSelectionForeground());
			} else {
				setBackground(list.getBackground());
				setForeground(list.getForeground());
			}
			return this;
		}
	}

	// === 测试主方法 ===
	public static void main(String[] args) {
		List<File> filesToDelete = Arrays.asList(new File("/adhome/ex-liuraoxing135/Documents/afa-pdfwithpdffox/test1.pdf"),
				new File("/adhome/ex-liuraoxing135/Documents/afa-pdfwithpdffox/test2.jpg"),
				new File("/adhome/ex-liuraoxing135/Documents/afa-pdfwithpdffox/temp.log"),
				new File("/adhome/ex-liuraoxing135/Documents/afa-pdfwithpdffox/log_2025_01_01.txt"),
				new File("/adhome/ex-liuraoxing135/Documents/afa-pdfwithpdffox/report.xlsx"));

		boolean confirmed = showConfirmDialog(null, filesToDelete);

		if (confirmed) {
			System.out.println("✅ 用户确认删除文件");
			// 可在此处执行删除逻辑
			// filesToDelete.forEach(f -> f.delete());
		} else {
			System.out.println("❌ 用户取消删除");
		}
	}
}