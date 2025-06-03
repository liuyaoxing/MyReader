package offline.export.module;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class BackupJPanelUI extends MyReaderPanel {

	/** * 序列号 */
	private static final long serialVersionUID = 3088458191004290208L;

	protected JComboBox<String> urlCombo;

	protected JButton backupBtn;

	protected JButton folderListBtn;

	protected DefaultTableModel backupTableModel;

	protected String[] columnNames = new String[] { KEY_ID, KEY_FILENAME, KEY_URL, KEY_LENGTH, KEY_STATUS, KEY_FILEPATH, "个 数" };
	protected int[] columnWidths = new int[] { 50, 250, 50, 50, 50, 250, 50 };

	protected JTable backupTable;

	/**
	 * Create the panel.
	 */
	public BackupJPanelUI() {
		setLayout(new BorderLayout(0, 0));

		JPanel firstPanel_1 = new JPanel();
		add(firstPanel_1, BorderLayout.NORTH);
		firstPanel_1.setLayout(new BoxLayout(firstPanel_1, BoxLayout.X_AXIS));

		urlCombo = new JComboBox<String>();
//		urlCombo.setFont(new Font("宋体", Font.PLAIN, 16));
		urlCombo.setEditable(true);
		urlCombo.setToolTipText("请输入读乐乐服务URL");
		firstPanel_1.add(urlCombo);

		backupBtn = new JButton("开始备份");
		backupBtn.setHorizontalAlignment(SwingConstants.RIGHT);
		firstPanel_1.add(backupBtn);

		folderListBtn = new JButton("刷新文件夹");
		folderListBtn.setHorizontalAlignment(SwingConstants.RIGHT);
		firstPanel_1.add(folderListBtn);

		backupTableModel = new DefaultTableModel(null, columnNames);
		backupTable = new JTable(backupTableModel);
		backupTable.setFillsViewportHeight(true);
		for (int i = 0; i < backupTable.getColumnModel().getColumnCount(); i++) {
			backupTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
		}
		backupTable.getTableHeader().setVisible(true);
		backupTable.setShowGrid(true);
		backupTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			/*** 序列号 */
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				label.setToolTipText(String.valueOf(table.getValueAt(row, column)));
				return label;
			}
		});

		deriveFontStyleSize(backupTable, -1, 0);

//		setTableFont(backupTable, new Font("宋体", Font.PLAIN, 12));

		JScrollPane backupscrollPane = new JScrollPane(backupTable); // 支持滚动
		add(backupscrollPane);
	}

}
