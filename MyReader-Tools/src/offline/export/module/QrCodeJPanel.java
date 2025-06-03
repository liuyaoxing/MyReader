package offline.export.module;

import java.awt.Component;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import offline.export.FileUtils;
import offline.export.pictureViewer.ViewerFrame;
import offline.export.utils.Base64FileUtil;
import offline.export.utils.ComparatorFactory;
import offline.export.utils.EventDispatcher;

public class QrCodeJPanel extends QrCodeJPanelUI {

	/** 序列号 */
	private static final long serialVersionUID = -8097572405188262778L;

	protected ViewerFrame viewerFrame;

	private JFrame frame;

	private JPopupMenu popupMenu;

	public QrCodeJPanel(JFrame frame) {
		this.frame = frame;

		initMenus();
		initDnd();
		addListeners();
	}

	private void addListeners() {
		qrCodeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Point location = qrCodeButton.getLocationOnScreen();
					popupMenu.setLocation(location.x, location.y + qrCodeButton.getSize().height);
					popupMenu.setVisible(true);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

		qrCodeTable.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent me) {
				if (me.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(me)) {
					final int row = qrCodeTable.rowAtPoint(me.getPoint());
					final int col = qrCodeTable.columnAtPoint(me.getPoint());
					String selectedText = (String) qrCodeTableModel.getValueAt(row, col);
					if (viewerFrame == null) {
						viewerFrame = new ViewerFrame();
						viewerFrame.setLocationRelativeTo(null);
					}
					System.out.println(selectedText);
					viewerFrame.setVisible(true);
					viewerFrame.openFile(new File(qrCodeFileTitle.getText(), selectedText));
				}
			}
		});
	}

	private void initMenus() {
		popupMenu = new JPopupMenu();
		JMenuItem qrCodeTransferMenuItem = new JMenuItem("码云传");
		qrCodeTransferMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					popupMenu.setVisible(false);
//					FileDialog fd = new FileDialog(frame);
					FileDialog fd = new FileDialog(frame);
//					fd.setMultipleMode(false);
					fd.setTitle("请选择文件");
					fd.setVisible(true);

					File getFile = new File(fd.getDirectory(), fd.getFile());

					parseFile2QrCodes(getFile);

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		popupMenu.add(qrCodeTransferMenuItem);

		JMenuItem photoViewerMenuItem = new JMenuItem("图片浏览器");
		photoViewerMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					popupMenu.setVisible(false);
					frame.setExtendedState(Frame.ICONIFIED);
					ViewerFrame viewerFrame = new ViewerFrame();
					viewerFrame.setLocationRelativeTo(null);
					viewerFrame.openFile();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		popupMenu.add(photoViewerMenuItem);
		addPopupMenu(qrCodeButton, popupMenu);
	}

	private void initDnd() {
		new DropTarget(qrCodeTable, DnDConstants.ACTION_COPY_OR_MOVE, new DropTargetAdapter() {
			@Override
			public void dragEnter(DropTargetDragEvent evt) {
				Transferable t = evt.getTransferable();
				if (!t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					evt.rejectDrag(); // 没有需要的类型，拒绝进入
				}
//				evt.acceptDrag(DnDConstants.ACTION_COPY);
			}

			@Override
			public void drop(DropTargetDropEvent dtde) {
				// 检测拖放进来的数据类型
				Transferable transfer = dtde.getTransferable();
				if (transfer.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					// 必须先调用acceptDrop
					dtde.acceptDrop(DnDConstants.ACTION_COPY);
					qrCodeTableModel.setRowCount(0);
					try {
						Object td = transfer.getTransferData(DataFlavor.javaFileListFlavor);
						if (td instanceof List) {
							for (Object value : ((List<?>) td)) {
								if (value instanceof File) {
									File file = (File) value;
									parseFile2QrCodes(file);
								}
							}
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		});
	}

	private void parseFile2QrCodes(final File getFile) {
		if (getFile.length() > FileUtils.ONE_MB) {
			JOptionPane.showMessageDialog(null, getFile.getPath(), String.format("文件大小[%s]超过1M！不允许使用!", FileUtils.getFileSize(getFile.length())),
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		EventDispatcher.dispatchMessage(PROP_GLASSPANE_START, "", "");
//		glassPane.start();// 开始动画加载效果
//		frame.setVisible(true);

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String fileStr = Base64FileUtil.getFileStr(getFile.getCanonicalPath());
					String generateFile = Base64FileUtil.generateFile(getFile, fileStr);

//					glassPane.stop();
					EventDispatcher.dispatchMessage(PROP_GLASSPANE_STOP, "", "");

//					Desktop.getDesktop().open(new File(generateFile));

					File[] qrCodeFiles = new File(generateFile).listFiles();
					Arrays.sort(qrCodeFiles, new ComparatorFactory.WindowsExplorerComparator());

					for (int i = 0; i < qrCodeFiles.length; i += qrCodeTable.getColumnCount()) {
						Vector<String> rowData = new Vector<String>(qrCodeTable.getColumnCount());
						for (int j = 0; j < Math.min(qrCodeTable.getColumnCount(), qrCodeFiles.length - i); j++) {
							rowData.add(qrCodeFiles[i + j].getName());
						}
						qrCodeTableModel.addRow(rowData);
					}
					qrCodeFileTitle.setText(generateFile);

					if (viewerFrame == null) {
						viewerFrame = new ViewerFrame();
						viewerFrame.setLocationRelativeTo(null);
					}
					viewerFrame.openFile(qrCodeFiles[0]);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void addPopupMenu(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
}
