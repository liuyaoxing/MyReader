package offline.export.module;

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class QrCodeJPanelUI extends MyReaderPanel {

	/** 序列号 */
	private static final long serialVersionUID = 3065141250973534973L;

	protected JButton qrCodeButton;

	protected JTable qrCodeTable;
	protected DefaultTableModel qrCodeTableModel;
	protected JLabel qrCodeFileTitle;

	public QrCodeJPanelUI() {
		setLayout(new BorderLayout(0, 0));

		JPanel qrCodePanel_1 = new JPanel();
		add(qrCodePanel_1, BorderLayout.NORTH);
		qrCodePanel_1.setLayout(new BoxLayout(qrCodePanel_1, BoxLayout.X_AXIS));

		qrCodeButton = new JButton("菜单栏");
		qrCodeButton.setHorizontalAlignment(SwingConstants.RIGHT);
		qrCodePanel_1.add(qrCodeButton);

		qrCodeFileTitle = new JLabel();
		qrCodeFileTitle.setAlignmentX(0.5f);
		qrCodeFileTitle.setText("请将文件拖入到下方                              ");
//		qrCodeFileTitle.setFont(new Font("宋体", Font.PLAIN, 12));
		deriveFontStyleSize(qrCodeFileTitle, -4, 0);
		qrCodePanel_1.add(qrCodeFileTitle);

		qrCodeTableModel = new DefaultTableModel(null, new String[] { "0", "1", "2", "4", "5" }) {
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int row, int colulmn) {
				return false;
			}
		};
		qrCodeTable = new JTable(qrCodeTableModel);
		qrCodeTable.setFillsViewportHeight(true);
//		qrCodeTable.setRowHeight(30);
//		qrCodeTable.setFont(new Font("宋体", Font.PLAIN, 12));
		qrCodeTable.setCellSelectionEnabled(false);
		qrCodeTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			/*** 序列号 */
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				label.setToolTipText(String.valueOf(table.getValueAt(row, column)));
				return label;
			}
		});

		deriveFontStyleSize(qrCodeTable, -1, 0);

		add(new JScrollPane(qrCodeTable)); // 支持滚动
	}
}
