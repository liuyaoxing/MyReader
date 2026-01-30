package offline.export.module;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class TaskListJPanelUI extends MyReaderPanel {

	/** 序列号 */
	private static final long serialVersionUID = -7164145189781853270L;

	protected String[] taskListColumnNames = new String[]{KEY_ID, KEY_FILEID, KEY_FILENAME, KEY_URL, KEY_LENGTH, KEY_STATUS, KEY_FILEPATH, "个 数"};
	protected int[] taskListcolumnWidths = new int[]{10, 50, 250, 50, 50, 50, 250, 50};

	protected JTable taskListTable;
	protected DefaultTableModel taskListTableModel;
	protected JLabel taskListTitle;

	public TaskListJPanelUI() {
		setLayout(new BorderLayout(0, 0));

		JPanel titlePanel = new JPanel();
		add(titlePanel, BorderLayout.NORTH);
		titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));

		taskListTitle = new JLabel();
		taskListTitle.setAlignmentX(0.5f);
		taskListTitle.setText("                              ");
		// taskListTitle.setFont(new Font("宋体", Font.PLAIN, 12));
		deriveFontStyleSize(taskListTitle, -4, 0);
		titlePanel.add(taskListTitle);

		taskListTableModel = new DefaultTableModel(null, taskListColumnNames);
		taskListTable = new JTable(taskListTableModel);
		taskListTable.setFillsViewportHeight(true);
		taskListTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		for (int i = 0; i < taskListTable.getColumnModel().getColumnCount(); i++) {
			taskListTable.getColumnModel().getColumn(i).setPreferredWidth(taskListcolumnWidths[i]);
		}
		taskListTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			/*** 序列号 */
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				label.setToolTipText(String.valueOf(table.getValueAt(row, column)));
				return label;
			}
		});

		deriveFontStyleSize(taskListTable, -1, 0);

		add(new JScrollPane(taskListTable));// 支持滚动
	}
}
