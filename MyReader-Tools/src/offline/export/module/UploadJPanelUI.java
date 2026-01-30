package offline.export.module;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class UploadJPanelUI extends MyReaderPanel {

	/** 序列号 */
	private static final long serialVersionUID = 1431561609475052749L;

	protected JComboBox<String> urlCombo2;

	protected JCheckBox deleteFileAfterUploadBtn;

	protected JButton uploadFolderBtn;

	protected String[] uploadColumnNames = new String[]{KEY_ID, KEY_FILENAME, COL_PROGRESS, KEY_FILEPATH, KEY_LENGTH, "修改时间"};
	protected int[] uploadColumnWidths = new int[]{6, 100, 6, 360, 10, 88};

	protected DefaultTableModel uploadTableModel;
	protected JTable uploadTable;

	public UploadJPanelUI() {
		setLayout(new BorderLayout(0, 0));

		JPanel secondPanel_1 = new JPanel();
		add(secondPanel_1, BorderLayout.NORTH);
		secondPanel_1.setLayout(new BoxLayout(secondPanel_1, BoxLayout.X_AXIS));

		urlCombo2 = new JComboBox<String>();
		// urlCombo2.setFont(new Font("宋体", Font.PLAIN, 16));
		urlCombo2.setEditable(true);
		urlCombo2.setToolTipText("请输入读乐乐服务URL");
		secondPanel_1.add(urlCombo2);

		deleteFileAfterUploadBtn = new JCheckBox("上传完成删除本地文件");
		// deleteFileAfterUploadBtn.setFont(new Font("宋体", Font.PLAIN, 16));
		deleteFileAfterUploadBtn.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					deleteFileAfterUploadBtn.setForeground(Color.RED);
					deriveFontStyleSize(deleteFileAfterUploadBtn, 0, Font.BOLD);
				} else if (e.getStateChange() == ItemEvent.DESELECTED) {
					deleteFileAfterUploadBtn.setForeground(Color.BLACK);
					deriveFontStyleSize(deleteFileAfterUploadBtn, 0, Font.PLAIN);
				}
			}
		});
		secondPanel_1.add(deleteFileAfterUploadBtn);

		uploadFolderBtn = new JButton("上传文件夹");
		uploadFolderBtn.setHorizontalAlignment(SwingConstants.RIGHT);
		secondPanel_1.add(uploadFolderBtn);

		uploadTableModel = new DefaultTableModel(null, uploadColumnNames);
		uploadTable = new JTable(uploadTableModel);
		uploadTable.setFillsViewportHeight(true);
		for (int i = 0; i < uploadTable.getColumnModel().getColumnCount(); i++) {
			uploadTable.getColumnModel().getColumn(i).setPreferredWidth(uploadColumnWidths[i]);
		}
		uploadTable.setCellEditor(null);
		uploadTable.setCellSelectionEnabled(false);
		uploadTable.getTableHeader().setVisible(true);
		uploadTable.setShowGrid(true);
		// setTableFont(uploadTable, new Font("宋体", Font.PLAIN, 12));
		uploadTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			/*** 序列号 */
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				label.setToolTipText(String.valueOf(table.getValueAt(row, column)));
				return label;
			}
		});

		JScrollPane uploadscrollPane = new JScrollPane(uploadTable); // 支持滚动
		uploadscrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

		deriveFontStyleSize(uploadTable, -1, 0);

		// uploadTablePopupMenu = new JPopupMenu();
		// addPopup(uploadTable, uploadTablePopupMenu);
		//
		// updateTableCopyMenu = new JMenuItem("复制");
		// uploadTablePopupMenu.add(updateTableCopyMenu);
		//
		// updateTableClearMenu = new JMenuItem("清空");
		// uploadTablePopupMenu.add(updateTableClearMenu);
		add(uploadscrollPane);
	}
}
