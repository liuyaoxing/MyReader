package offline.export.utils;

import java.awt.Component;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class DialogUtils {

	/**
	 * 在 Swing EDT (Event Dispatch Thread) 线程中安全地显示错误对话框
	 *
	 * @param parent
	 *            父组件（如果为 null，则居中屏幕显示）
	 * @param message
	 *            错误消息内容
	 * @param title
	 *            对话框标题
	 */
	public static void showErrorDialog(Component parent, Object message, String title) {
		// 确保代码在 EDT 线程中执行，保证 UI 更新的线程安全性
		if (SwingUtilities.isEventDispatchThread()) {
			JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
		} else {
			SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE));
		}
	}

	/**
	 * 在 Swing EDT (Event Dispatch Thread) 线程中安全地显示警告对话框
	 *
	 * @param parent
	 *            父组件（如果为 null，则居中屏幕显示）
	 * @param message
	 *            错误消息内容
	 * @param title
	 *            对话框标题
	 */
	public static void showWarningDialog(Component parent, Object message, String title) {
		// 确保代码在 EDT 线程中执行，保证 UI 更新的线程安全性
		if (SwingUtilities.isEventDispatchThread()) {
			JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE);
		} else {
			SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(parent, message, title, JOptionPane.WARNING_MESSAGE));
		}
	}
}
