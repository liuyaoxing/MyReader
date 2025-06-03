package offline.export;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

import offline.export.config.Configuration;
import offline.export.dialog.InfiniteProgressPanel;
import offline.export.log.LogHandler;
import offline.export.module.BackupJPanel;
import offline.export.module.QrCodeJPanel;
import offline.export.module.TaskListJPanel;
import offline.export.module.UploadJPanel;
import offline.export.utils.EventDispatcher;
import offline.export.utils.LanPortScanner;
import offline.export.utils.LanPortScanner.ScannerResultCallback;

public class OfflineExport implements IConstants, PropertyChangeListener {

	public static final String FILESERVER_NAME = "name";
	public static final String FILESERVER_SIZE = "size";
	public static final String FILESERVER_ABSPATH = "absPath";
	public static final String FILESERVER_FILELIST = "fileList";
	public static final String FILESERVER_PATH = "path";
	public static final String FILESERVER_LENGTH = "length";
	public static final String FILESERVER_MD5 = "md5";

	private static final String TITLE = "读乐乐备份工具 v3.31";

	private JFrame frame;

	private InfiniteProgressPanel glassPane;

	private JTabbedPane tabbedPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		onBeforeCommandLineProcessing(args);
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					setupUIManager();

					OfflineExport window = new OfflineExport();
					window.frame.setVisible(true);

					String exeName = new java.io.File(OfflineExport.class.getProtectionDomain()//
							.getCodeSource()//
							.getLocation()//
							.getPath()).getName();
					LogHandler.debug("当前exe名称:" + exeName);
				} catch (Exception ex) {
					LogHandler.error(ex);
					ex.printStackTrace();
				}
			}
		});
	}

	public static void setupUIManager() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

			Font tableFont = (Font) UIManager.get("Table.font");
			LineMetrics lineMetrics = tableFont.getLineMetrics("读乐乐", new FontRenderContext(null, false, false));
			UIManager.put("Table.rowHeight", lineMetrics.getHeight());
		} catch (Exception ex) {

		}
	}

	public static void onBeforeCommandLineProcessing(String[] args) {
		StringBuffer sb = new StringBuffer("用法：java -jar MyReader.Backup.jar --capacity=1234 --qrCodeSize=800");
		System.out.println(sb.toString());
		LogHandler.debug(sb.toString());

		for (String arg : args) {
			int switchCnt = arg.startsWith("--") ? 2 : arg.startsWith("/") ? 1 : arg.startsWith("-") ? 1 : 0;
			switch (switchCnt) {
			case 2:
				if (arg.length() == 2) {
					continue;
				}
			case 1: {
				String[] switchVals = arg.substring(switchCnt).split("=");
				if (switchVals.length == 2) {
					String key = switchVals[0], value = switchVals[1];
					if ("CAPACITY".equalsIgnoreCase(key) || "QRCODECAPACITY".equalsIgnoreCase(key)) {
						Configuration.getInstance().setQrCodeCapacity(Integer.valueOf(value));
					}
					if ("qrCodeSize".equalsIgnoreCase(key)) {
						Configuration.getInstance().setQrCodeSize(Integer.valueOf(value));
					}
					if ("qrCodeLogoWidth".equalsIgnoreCase(key)) {
						Configuration.getInstance().setQrCodeLogoWidth(Integer.valueOf(value));
					}
					if ("qrCodeLogoHeight".equalsIgnoreCase(key)) {
						Configuration.getInstance().setQrCodeLogoHeight(Integer.valueOf(value));
					}
				} else {
				}
				break;
			}
			case 0:
				break;
			}
		}
	}

	/**
	 * Create the application.
	 */
	public OfflineExport() {
		initialize();

		startScanPorts();

		EventDispatcher.addPropertyChangeListener(this);
	}

	private void startScanPorts() {
		new Thread(new Runnable() {
			public void run() {
				try {
					LanPortScanner.scan(new ScannerResultCallback() {
						@Override
						public void onSuccess(String ip) {
							EventDispatcher.dispatchMessage(PROP_LANPORT_SCAN_IP, "http://" + ip, "");
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	/**
	 * Initialize the contents of the frame.
	 * 
	 * @param tabbedPane
	 * 
	 * @param uploadTableModel
	 * @param uploadTable
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setTitle(TITLE);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setSize((int) (screenSize.width * 0.618), (int) (screenSize.height * 0.618));
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		glassPane = new InfiniteProgressPanel();
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		glassPane.setBounds(100, 100, (dimension.width) / 2, (dimension.height) / 2);
		frame.setGlassPane(glassPane);

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);

		tabbedPane.addTab(NAME_BACKUP, new BackupJPanel(frame));
		tabbedPane.addTab(NAME_UPLOAD, new UploadJPanel(frame));
		tabbedPane.addTab(NAME_QRCODE, new QrCodeJPanel(frame));
		tabbedPane.addTab(NAME_TASKLIST, new TaskListJPanel(frame));
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String propertyName = event.getPropertyName();
		if (event.getPropertyName().equals(PROP_GLASSPANE_START) && glassPane != null) {
			glassPane.start();// 开始动画加载效果
			frame.setVisible(true);
		} else if (event.getPropertyName().equals(PROP_GLASSPANE_STOP) && glassPane != null) {
			glassPane.stop();
		}
		if (propertyName.equals(PROP_LANPORT_SCAN)) {
			startScanPorts();
		}
		if (PROP_SET_WINDOW_TITLE.equals(propertyName)) {
			frame.setTitle((String) event.getNewValue());
		}
		if (PROP_TABBEDPANE_SELECTED_NAME.equals(propertyName)) {
			for (int i = 0; i < tabbedPane.getTabCount(); i++) {
				if (tabbedPane.getTitleAt(i).equals(NAME_TASKLIST) && event.getNewValue().equals(TAB_TASKLIST)) {
					tabbedPane.setSelectedIndex(i);
					break;
				}
				if (tabbedPane.getTitleAt(i).equals(NAME_BACKUP) && event.getNewValue().equals(TAB_BACKUP)) {
					tabbedPane.setSelectedIndex(i);
					break;
				}
				if (tabbedPane.getTitleAt(i).equals(NAME_UPLOAD) && event.getNewValue().equals(TAB_UPLOAD)) {
					tabbedPane.setSelectedIndex(i);
					break;
				}
				if (tabbedPane.getTitleAt(i).equals(NAME_QRCODE) && event.getNewValue().equals(TAB_QRCODE)) {
					tabbedPane.setSelectedIndex(i);
					break;
				}
			}
		}
	}
}
