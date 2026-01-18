package offline.export.module;

import java.beans.PropertyChangeEvent;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

/**
 * 上传到手机面板
 * 
 * @author liuyaoxing
 */
public class FileCompareJPanel extends FileCompareJPanelUI {

	/** 序列号 */
	private static final long serialVersionUID = -5952898543215887697L;

	public FileCompareJPanel(JFrame frame) {
//		createPopupMenu();
		addListeners();
//		initDnd();

	}

	private void addListeners() {
		leftBtn.addActionListener(e -> selectFolder(leftCombo));
		rightBtn.addActionListener(e -> selectFolder(rightCombo));
	}

	private void selectFolder(JComboBox<String> field) {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			addItemsToCombo(field, new String[] { chooser.getSelectedFile().getAbsolutePath() }, 0);
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String propertyName = event.getPropertyName();
		if (PROP_LANPORT_SCAN_IP.equals(propertyName)) {
//			itemSet.add((String) event.getNewValue() + FOLDER_UPLOAD);
//			addItemsToCombo(urlCombo2, itemSet.toArray(new String[0]), 0);
		}
		super.propertyChange(event);
	}
}
