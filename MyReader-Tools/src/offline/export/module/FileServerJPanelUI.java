package offline.export.module;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.JTextField;

public class FileServerJPanelUI extends MyReaderPanel {

	/** * 序列号 */
	private static final long serialVersionUID = 3088458191004290208L;

	protected JComboBox<String> ipCombo;

	protected JButton startStopBtn;

	protected JButton refreshUrlBtn;

	protected DefaultTableModel fileServerTableModel;

	protected String[] columnNames = new String[]{KEY_ID, KEY_FILENAME, KEY_URL, KEY_LENGTH, KEY_STATUS, KEY_FILEPATH, "个 数"};
	protected int[] columnWidths = new int[]{50, 250, 50, 50, 50, 250, 50};

	protected JTable fileServerTable;

	private JTextField ipTxt;

	/**
	 * Create the panel.
	 */
	public FileServerJPanelUI() {
		setLayout(new BorderLayout(0, 0));

		JPanel firstPanel_1 = new JPanel();
		add(firstPanel_1, BorderLayout.NORTH);
		firstPanel_1.setLayout(new BoxLayout(firstPanel_1, BoxLayout.X_AXIS));

		JLabel lblNewLabel = new JLabel("当前IP:");
		firstPanel_1.add(lblNewLabel);
		
		ipCombo = new JComboBox<String>();
		// urlCombo.setFont(new Font("宋体", Font.PLAIN, 16));
		ipCombo.setEditable(true);
		ipCombo.setPreferredSize(new Dimension(400, ipCombo.getPreferredSize().height));
		ipCombo.setMinimumSize(new Dimension(400, ipCombo.getPreferredSize().height));
		ipCombo.setMaximumSize(new Dimension(400, ipCombo.getPreferredSize().height));
		ipCombo.setToolTipText("读乐乐服务IP");
		firstPanel_1.add(ipCombo);
		
		JLabel lblNewLabel2 = new JLabel("端口:");
		firstPanel_1.add(lblNewLabel2);
		
		ipTxt = new JTextField();
		ipTxt.setPreferredSize(new Dimension(100, ipTxt.getPreferredSize().height));
		ipTxt.setMinimumSize(new Dimension(100, ipTxt.getPreferredSize().height));
		ipTxt.setMaximumSize(new Dimension(100, ipTxt.getPreferredSize().height));
		firstPanel_1.add(ipTxt);

		startStopBtn = new JButton("开启服务");
		startStopBtn.setHorizontalAlignment(SwingConstants.RIGHT);
		firstPanel_1.add(startStopBtn);

		refreshUrlBtn = new JButton("刷新网址");
		refreshUrlBtn.setHorizontalAlignment(SwingConstants.RIGHT);
		firstPanel_1.add(refreshUrlBtn);

		// 创建水平方向胶水，填充水平剩余空间
		Component hGlue = Box.createHorizontalGlue();
		firstPanel_1.add(hGlue);
		
		fileServerTableModel = new DefaultTableModel(null, columnNames);
		fileServerTable = new JTable(fileServerTableModel);
		fileServerTable.setFillsViewportHeight(true);
		for (int i = 0; i < fileServerTable.getColumnModel().getColumnCount(); i++) {
			fileServerTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
		}
		fileServerTable.getTableHeader().setVisible(true);
		fileServerTable.setShowGrid(true);
		fileServerTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			/*** 序列号 */
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				label.setToolTipText(String.valueOf(table.getValueAt(row, column)));
				return label;
			}
		});

		deriveFontStyleSize(fileServerTable, -1, 0);

		// setTableFont(backupTable, new Font("宋体", Font.PLAIN, 12));

		add(new JScrollPane(fileServerTable)); // 支持滚动
	}

}
