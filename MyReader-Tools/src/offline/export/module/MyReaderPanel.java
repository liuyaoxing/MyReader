package offline.export.module;

import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTable;

import offline.export.IConstants;
import offline.export.dialog.InfiniteProgressPanel;
import offline.export.utils.EventDispatcher;

public abstract class MyReaderPanel extends JPanel implements IConstants, PropertyChangeListener {

	public static final String TITLE = "读乐乐备份工具 v3.31";

	public AtomicLong total = new AtomicLong(0);

	public AtomicLong counter = new AtomicLong(0);

	protected File currentDirectory;

	/** 序列号 */
	private static final long serialVersionUID = -4577948865550707034L;

	public MyReaderPanel() {
		EventDispatcher.addPropertyChangeListener(this);
	}

	public void deriveFontStyleSize(Component component, float fontSize, int style) {
		if (component == null || component.getFont() == null)
			return;
		Font newFont = component.getFont();
		if (fontSize != 0)
			newFont = newFont.deriveFont(newFont.getSize() + fontSize);
		if (style >= 0)
			newFont = newFont.deriveFont(style);
		component.setFont(newFont);
		if (component instanceof JTable) {
			setTableFont((JTable) component, newFont);
		}
	}

	public void setTableFont(JTable table, Font font) {
		Font currentFont = table.getFont();
		if (currentFont == null)
			currentFont = font;
		table.setFont(font);
		FontMetrics fontMetrics = table.getFontMetrics(font);
		int rowHeight = fontMetrics.getHeight();
		table.setRowHeight(rowHeight);
	}

	public void addItemsToCombo(JComboBox<String> urlCombo, String[] items, int index) {
		DefaultComboBoxModel<String> d = (DefaultComboBoxModel<String>) urlCombo.getModel();
		d.removeAllElements();
		for (String item : items) {
			d.addElement(item);
		}
		d.setSelectedItem(items[index]);
	}

	/**
	 * 将字符串复制到剪切板。
	 */
	public static void setSysClipboardText(String writeMe) {
		Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable tText = new StringSelection(writeMe);
		clip.setContents(tText, null);
	}

	protected void pasteToCombo(JComboBox<String> combo) {
		// 取得系统剪贴板里可传输的数据构造的Java对象
		Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);
		try {
			if (t != null && t.isDataFlavorSupported(DataFlavor.stringFlavor)) {
				// 因为原系的剪贴板里有多种信息, 如文字, 图片, 文件等
				// 先判断开始取得的可传输的数据是不是文字, 如果是, 取得这些文字
				System.out.println((String) t.getTransferData(DataFlavor.stringFlavor));
				// 同样, 因为Transferable中的DataFlavor是多种类型的,
				// 所以传入DataFlavor这个参数, 指定要取得哪种类型的Data.
				setComboBox(combo, (String) t.getTransferData(DataFlavor.stringFlavor));
			} else if (t != null && t.isDataFlavorSupported(DataFlavor.imageFlavor)) {
				System.out.println(t.getTransferData(DataFlavor.imageFlavor));
			}
		} catch (UnsupportedFlavorException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	protected void setComboBox(JComboBox<String> comboBox, String newItem) {
		DefaultComboBoxModel<String> d = (DefaultComboBoxModel<String>) comboBox.getModel();
		d.addElement(newItem);
		d.setSelectedItem(newItem);
	}

	protected String getInputHostUrl(JComboBox<String> urlCombo) {
		try {
			String newItem = String.valueOf(urlCombo.getEditor().getItem());
			URL url = new URL(newItem);
			return "http://" + String.format("%s:%s", url.getHost(), url.getPort());
		} catch (Exception ex) {
			return null;
		}
	}

	public String getComboText(JComboBox<String> comboBox) {
		return String.valueOf(comboBox.getEditor().getItem());
	}

	public String toSizeStr(long fileLen) {
		DecimalFormat df = new DecimalFormat("0.00");
		if (fileLen > 1024 * 1024) {
			return df.format(fileLen * 1f / 1024 / 1024) + "MB";
		} else if (fileLen > 1024) {
			return df.format(fileLen * 1f / 1024) + "KB";
		} else {
			return fileLen + "B";
		}
	}

	public void dispatchMessage(String propName, Object newValue, Object oldValue) {
		EventDispatcher.dispatchMessage(propName, newValue, oldValue);
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
	}
}
