package offline.export;

import java.awt.BorderLayout;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class JTableTest extends JFrame {
	public JTableTest() {
		super();
		setTitle("���");
		setBounds(100, 100, 240, 150);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		String[] columnNames = { "A", "B" }; // ����
		String[][] tableVales = { { "A1", "B1" }, { "A2", "B2" }, { "A3", "B3" }, { "A4", "B4" }, { "A5", "B5" } }; // ����
		JTable table = new JTable(tableVales, columnNames); // ����һ��JTable
		JScrollPane scrollPane = new JScrollPane(table); // ֧�ֹ���
		getContentPane().add(scrollPane, BorderLayout.CENTER);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JTableTest jTableTest = new JTableTest();
		jTableTest.setVisible(true);
	}

}