package offline.export.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * 对话框帮助类
 * 
 * @author 刘绕兴
 */
public class DialogHelper {

	/**
	 * 弹出多行文本框输入框。
	 * 
	 * @return
	 */
	public static String showMultiInputDialog(Frame parent, String title) {
		JDialog dialog = new JDialog(parent, title, true);
		dialog.setSize(333, 222);
		dialog.setLocationRelativeTo(parent);

		JTextArea inputArea = new JTextArea(4, 10);
		inputArea.setLineWrap(true);
		inputArea.setWrapStyleWord(true);
		inputArea.setOpaque(true);
		inputArea.setBackground(Color.WHITE);
		inputArea.setFont(new Font("Monespaced", Font.PLAIN, 12));

		JScrollPane scrollPane = new JScrollPane(inputArea);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

		JPanel buttonPanel = new JPanel();
		JButton okButton = new JButton("确定");
		JButton cancelButton = new JButton("取消");

		okButton.setPreferredSize(new Dimension(80, 25));
		cancelButton.setPreferredSize(new Dimension(80, 25));

		AtomicBoolean confirmed = new AtomicBoolean(false);

		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				confirmed.set(true);
				dialog.dispose();
			}
		});
		cancelButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				confirmed.set(false);
				inputArea.setText("");
				dialog.dispose();
			}
		});

		buttonPanel.add(cancelButton);
		buttonPanel.add(okButton);

		JPanel contentPanel = new JPanel(new BorderLayout());
		contentPanel.add(scrollPane, BorderLayout.CENTER);
		contentPanel.add(buttonPanel, BorderLayout.SOUTH);
		contentPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		dialog.setContentPane(contentPanel);

		dialog.setVisible(true);

		if (confirmed.get())
			return inputArea.getText().trim();

		return null;
	}
}
