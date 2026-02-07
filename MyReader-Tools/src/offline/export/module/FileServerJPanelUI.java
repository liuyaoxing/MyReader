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
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

public class FileServerJPanelUI extends MyReaderPanel {

	/** * 序列号 */
	private static final long serialVersionUID = 3088458191004290208L;

	protected JComboBox<String> ipCombo;

	protected JButton startStopBtn, serverQRCodeBtn;

	protected JButton addFileBtn, addFolderBtn, clearBtn;

	protected DefaultTableModel fileServerTableModel;

	protected String[] columnNames = new String[]{KEY_ID, KEY_FILENAME, KEY_FILEPATH, KEY_LENGTH, KEY_URL, "个 数"};
	protected int[] columnWidths = new int[]{10, 166, 150, 40, 250, 10};

	protected JTable fileServerTable;

	protected JTextField portTxt;

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
		ipCombo.setPreferredSize(new Dimension(200, ipCombo.getPreferredSize().height));
		ipCombo.setMinimumSize(new Dimension(200, ipCombo.getPreferredSize().height));
		ipCombo.setMaximumSize(new Dimension(200, ipCombo.getPreferredSize().height));
		ipCombo.setToolTipText("读乐乐服务IP");
		firstPanel_1.add(ipCombo);

		JLabel lblNewLabel2 = new JLabel("端口:");
		firstPanel_1.add(lblNewLabel2);

		portTxt = new JTextField();
		portTxt.setPreferredSize(new Dimension(77, portTxt.getPreferredSize().height));
		portTxt.setMinimumSize(new Dimension(77, portTxt.getPreferredSize().height));
		portTxt.setMaximumSize(new Dimension(77, portTxt.getPreferredSize().height));
		portTxt.setText("61666");
		firstPanel_1.add(portTxt);

		startStopBtn = new JButton("开启服务");
		startStopBtn.setHorizontalAlignment(SwingConstants.RIGHT);
		firstPanel_1.add(startStopBtn);

		addFileBtn = new JButton("添加文件");
		addFileBtn.setHorizontalAlignment(SwingConstants.RIGHT);
		firstPanel_1.add(addFileBtn);

		addFolderBtn = new JButton("添加文件夹");
		addFolderBtn.setHorizontalAlignment(SwingConstants.RIGHT);
		firstPanel_1.add(addFolderBtn);

		clearBtn = new JButton("清空共享");
		clearBtn.setHorizontalAlignment(SwingConstants.RIGHT);
		firstPanel_1.add(clearBtn);

		serverQRCodeBtn = new JButton("连接二维码");
		serverQRCodeBtn.setHorizontalAlignment(SwingConstants.RIGHT);
		firstPanel_1.add(serverQRCodeBtn);

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
