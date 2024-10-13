package offline.export;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import offline.export.DownloadUtil.OnDownloadListener;
import offline.export.config.Configuration;
import offline.export.db.BackupTask;
import offline.export.db.DataBaseProxy;
import offline.export.dialog.InfiniteProgressPanel;
import offline.export.log.LogHandler;
import offline.export.pictureViewer.ViewerFrame;
import offline.export.utils.Base64FileUtil;
import offline.export.utils.ComparatorFactory;
import offline.export.utils.LanPortScanner;
import offline.export.utils.LanPortScanner.ScannerResultCallback;
import offline.export.utils.NetworkUtils;
import offline.export.utils.ProgressRequestBody;
import offline.export.utils.ProgressRequestListener;
import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OfflineExport {

	private static final String KEY_ID = "ID";
	private static final String KEY_FILENAME = "文件名";
	private static final String KEY_URL = "URL";
	private static final String KEY_LENGTH = "大小";
	private static final String KEY_FILEPATH = "保存路径";

	public static final String FILESERVER_NAME = "name";
	public static final String FILESERVER_SIZE = "size";
	public static final String FILESERVER_ABSPATH = "absPath";
	public static final String FILESERVER_FILELIST = "fileList";
	public static final String FILESERVER_PATH = "path";
	public static final String FILESERVER_LENGTH = "length";
	public static final String FILESERVER_MD5 = "md5";

	private static final String FOLDER_LIST = "/folder/list";
	private static final String FOLDER_LIST_MD5 = "/folder/list/md5/";
	private static final String FOLDER_DOWNLOAD_MD5 = "/folder/download/md5/";
	private static final String FOLDER_UPLOAD = "/folderUpload";

	private static final String FLAG_DELETE_ON_SUCCESS = "#--delete-on-success";

	private static final String TITLE = "读乐乐备份工具 v3.31";

	private JFrame frame;
	private JComboBox<String> urlCombo, urlCombo2;
	private JTable backupTable;
	private DefaultTableModel backupTableModel;

	private String[] columnNames = new String[] { KEY_ID, "标题", KEY_URL, KEY_LENGTH, "状态", KEY_FILEPATH, "个 数" };
	private int[] columnWidths = new int[] { 50, 250, 50, 50, 50, 250, 50 };

	private static final String COL_PROGRESS = "进度";

	private String[] uploadColumnNames = new String[] { KEY_ID, KEY_FILENAME, COL_PROGRESS, KEY_FILEPATH, KEY_LENGTH, "修改时间" };
	private int[] uploadColumnWidths = new int[] { 6, 100, 6, 360, 10, 88 };

	protected BackupTask backupTask;
	protected DataBaseProxy database;

	protected Thread startThread, startFolderThread;

	private AtomicLong total = new AtomicLong(0);
	private AtomicLong counter = new AtomicLong(0);

	private File currentDirectory;

	private InfiniteProgressPanel glassPane;
	private JButton backupBtn;
	private JButton folderListBtn;
	private JTable qrCodeTable;
	private DefaultTableModel qrCodeTableModel;
	private JLabel qrCodeFileTitle;

	private JTable taskListTable;
	private DefaultTableModel taskListTableModel;
	private JLabel taskListTitle;

	protected File[] qrCodeFiles;
	protected ViewerFrame viewerFrame;
	private JButton uploadFolderBtn;
	protected File currentUploadFolder;

	protected DefaultTableModel uploadTableModel;
	protected JTable uploadTable;
	private JButton qrCodeButton;
	private JPopupMenu popupMenu;

	private AtomicInteger uploadCount = new AtomicInteger(0);

	OkHttpClient uploadOkHttpClient = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS)
			.readTimeout(30, TimeUnit.SECONDS).build();

	private JTabbedPane tabbedPane;
	private JPanel taskContailerPanel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		onBeforeCommandLineProcessing(args);
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
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
		addListeners();
		startScanPorts();
	}

	private void startScanPorts() {
		Set<String> itemSet = new HashSet<String>();
		Set<String> itemSet2 = new HashSet<String>();
		new Thread(new Runnable() {
			public void run() {
				try {
					LanPortScanner.scan(new ScannerResultCallback() {
						@Override
						public void onSuccess(String ip) {
							itemSet.add("http://" + ip);
							itemSet2.add("http://" + ip + FOLDER_UPLOAD);
							addItemsToCombo(urlCombo, itemSet.toArray(new String[0]), 0);
							addItemsToCombo(urlCombo2, itemSet2.toArray(new String[0]), 0);
						}
					});
				} catch (SocketException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private void addListeners() {
		uploadFolderBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int v = chooser.showOpenDialog(null);
				if (v == JFileChooser.APPROVE_OPTION) {
					currentUploadFolder = chooser.getSelectedFile();
					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								File[] allFiles = doPreUploadFolder(currentUploadFolder);
								doUploadFolder(allFiles);
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					}).start();
				}
			}
		});
		backupTable.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent me) {
				if (SwingUtilities.isRightMouseButton(me)) {
					final int row = backupTable.rowAtPoint(me.getPoint());
					backupTable.setRowSelectionInterval(row, row);
					System.out.println("row:" + row);
					if (row != -1) {
						final int column = backupTable.columnAtPoint(me.getPoint());

						final JPopupMenu popup = new JPopupMenu();
						JMenuItem clearItem = new JMenuItem("清空");
						popup.add(clearItem);
						clearItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								backupTableModel.setRowCount(0);
							}
						});
						JMenuItem copyItem = new JMenuItem("复制");
						popup.add(copyItem);
						copyItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								Object value = backupTable.getValueAt(row, column);
								if (value != null)
									setSysClipboardText(String.valueOf(value));
							}
						});
						popup.add(new JSeparator());
						JMenuItem syncFolderItem = new JMenuItem("下载文件夹");
						popup.add(syncFolderItem);
						syncFolderItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								Object title = backupTable.getValueAt(row, 1);
								if (title != null) {
									try {
										currentDirectory = new File(title.toString().replace("[文件夹]", "")).getCanonicalFile();
									} catch (IOException e1) {
										e1.printStackTrace();
									}
								}

								Object value = backupTable.getValueAt(row, 0);
								Object folderName = backupTable.getValueAt(row, 1);
								if (value != null) {
									String newUrl = getInputHostUrl() + FOLDER_LIST_MD5 + value;
//									setComboBox(urlCombo, newUrl);
									taskListTitle.setText(newUrl);
									doDownloadFolder(newUrl, String.valueOf(folderName));
								}
								setSysClipboardText(String.valueOf(value));
							}
						});
						popup.add(new JSeparator());
						JMenuItem cleanFolderItem = new JMenuItem("清空文件夹");
						popup.add(cleanFolderItem);
						cleanFolderItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								Object id = backupTable.getValueAt(row, 0);
								Object fileName = backupTable.getValueAt(row, 1);
								if (id != null) {
									String msg = String.format("确认是否要删除文件服务器上的%s?", fileName);
									int opt = JOptionPane.showConfirmDialog(null, msg, "确认删除", JOptionPane.YES_NO_OPTION);
									if (opt == JOptionPane.YES_OPTION) {
										// 确认继续操作
//										http://192.168.133.65:61666/files/ad60dc86d022c92f66715899686753e2
										String hostUrl = getInputHostUrl(urlCombo);
										String deleteUrl = hostUrl + "/files/" + id;
										try {
											LinkedHashMap<String, String> urlParams = new LinkedHashMap<>();
											urlParams.put("_method", "delete");
											boolean isSuccess = doPostFormData(deleteUrl, urlParams);
											JOptionPane.showMessageDialog(null, fileName + (isSuccess ? "删除成功!" : "删除失败!"));
										} catch (Exception e1) {
											e1.printStackTrace();
										}
									}
								}
							}
						});

						JMenuItem calcel = new JMenuItem("取消");
						calcel.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								popup.setVisible(false);
							}
						});

						popup.add(new JSeparator());
						popup.add(calcel);
						popup.show(me.getComponent(), me.getX(), me.getY());
					}
				}
			}
		});

		uploadTable.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent me) {
				if (SwingUtilities.isRightMouseButton(me)) {
					final int row = uploadTable.rowAtPoint(me.getPoint());
					uploadTable.setRowSelectionInterval(row, row);
					if (row != -1) {
						final int column = uploadTable.columnAtPoint(me.getPoint());

						final JPopupMenu popup = new JPopupMenu();
						JMenuItem clearItem = new JMenuItem("清空");
						popup.add(clearItem);
						clearItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								uploadTableModel.setRowCount(0);
							}
						});
						JMenuItem copyItem = new JMenuItem("复制");
						popup.add(copyItem);
						copyItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								Object value = uploadTable.getValueAt(row, column);
								if (value != null)
									setSysClipboardText(String.valueOf(value));
							}
						});

						JMenuItem openFolderItem = new JMenuItem("打开文件夹");
						popup.add(openFolderItem);
						openFolderItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								int column = uploadTable.getColumnModel().getColumnIndex(KEY_FILEPATH);
								Object value = uploadTable.getValueAt(row, column);
								if (value != null && new File(value.toString()).exists()) {
									try {
										Desktop.getDesktop().open(new File(value.toString()).getParentFile());
									} catch (Exception ex) {
										LogHandler.debug("打开文件夹失败：" + ex.getMessage());
									}
								}
							}
						});

						JMenuItem calcel = new JMenuItem("取消");
						calcel.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								popup.setVisible(false);
							}
						});

						popup.add(new JSeparator());
						popup.add(calcel);
						popup.show(me.getComponent(), me.getX(), me.getY());
					}
				}
			}

			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					// 双击事件的处理逻辑
					int row = uploadTable.rowAtPoint(e.getPoint());
					int column = uploadTable.columnAtPoint(e.getPoint());
					// 可以在这里添加你的代码，例如显示所选单元格的数据
					System.out.println("Double clicked on row " + row + ", column " + column);
				}
			}
		});

		taskListTable.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent me) {
				if (SwingUtilities.isRightMouseButton(me)) {
					final int row = taskListTable.rowAtPoint(me.getPoint());
					taskListTable.setRowSelectionInterval(row, row);
					int column = taskListTable.getColumnModel().getColumnIndex(KEY_FILEPATH);
					final File srcFile = new File(String.valueOf(taskListTable.getValueAt(row, column)));
					if (row != -1) {
						final JPopupMenu popup = new JPopupMenu();
						JMenuItem clearItem = new JMenuItem("清空");
						popup.add(clearItem);
						clearItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								taskListTableModel.setRowCount(0);
							}
						});
						if (srcFile.exists()) {
							JMenuItem openFile = new JMenuItem("打开文件");
							popup.add(openFile);
							openFile.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									try {
										Desktop.getDesktop().open(srcFile);
									} catch (IOException e1) {
										e1.printStackTrace();
									}
								}
							});
						}
						if (srcFile.getParentFile().exists()) {
							JMenuItem openDirFile = new JMenuItem("打开本地目录");
							popup.add(openDirFile);
							openDirFile.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									try {
										Desktop.getDesktop().open(srcFile.getParentFile());
									} catch (IOException e1) {
										e1.printStackTrace();
									}
								}
							});
						}

						popup.show(me.getComponent(), me.getX(), me.getY());
					}
				}
			}

			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					int row = taskListTable.rowAtPoint(e.getPoint());
					int column = taskListTable.getColumnModel().getColumnIndex(KEY_FILEPATH);
					final File srcFile = new File(String.valueOf(taskListTable.getValueAt(row, column)));
					try {
						Desktop.getDesktop().open(srcFile);
					} catch (IOException e1) {
						e1.printStackTrace();
					}
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

		urlCombo.addPopupMenuListener(new PopupMenuListener() {
			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				refreshServerIp();
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
		});

		urlCombo2.addPopupMenuListener(new PopupMenuListener() {

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				String newItem = String.valueOf(urlCombo2.getEditor().getItem());

				Set<String> itemSet = new HashSet<String>();
				DefaultComboBoxModel<String> d = (DefaultComboBoxModel<String>) urlCombo2.getModel();
				d.removeAllElements();
				try {
					itemSet.add(String.valueOf(newItem));
					URL url = new URL(newItem);
					String newUrl = "http://" + String.format("%s:%s", url.getHost(), url.getPort());
					itemSet.add(String.valueOf(newUrl + FOLDER_UPLOAD + FLAG_DELETE_ON_SUCCESS));
					itemSet.add(String.valueOf(newUrl + FOLDER_UPLOAD));
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				}
				addItemsToCombo(urlCombo2, itemSet.toArray(new String[0]), 0);
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {

			}
		});

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

		folderListBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshServerIp();

				String comboText = getComboText(urlCombo);
				String folderList = comboText + (comboText.endsWith(FOLDER_LIST) ? "" : FOLDER_LIST);

				addItemsToCombo(urlCombo, new String[] { comboText, folderList }, 1);

				Request request = new Request.Builder().addHeader("x-header", "dll")//
						.header("sort", "_id")//
						.url(folderList).build();
				try {
					Response response = DownloadUtil.get().newCall(request);
					if (!response.isSuccessful()) {
						JOptionPane.showMessageDialog(null, response.message());
						return;
					}
					String body = response.body().string();
					backupTableModel.setRowCount(0);
					Map<String, JsonObject> toMap = new Gson().fromJson(body, new TypeToken<Map<String, JsonObject>>() {
					}.getType());
					Iterator<Entry<String, JsonObject>> iter = toMap.entrySet().iterator();
					while (iter.hasNext()) {
						Entry<String, JsonObject> entry = iter.next();
						JsonObject element = entry.getValue();
						JsonObject jsonObject = element.get("nameValuePairs").getAsJsonObject();
						final String id = jsonObject.get(FILESERVER_MD5).getAsString();
						final String title = jsonObject.get(FILESERVER_NAME).getAsString();
						final String url = jsonObject.get(FILESERVER_PATH).getAsString();
						final String size = jsonObject.get(FILESERVER_SIZE).getAsString();
						final int fileList = jsonObject.get(FILESERVER_FILELIST).getAsInt();
						backupTableModel.insertRow(0, new Object[] { id, title, url, size, "", "", fileList });
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});

		backupBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				backupTableModel.setRowCount(0);
				counter.set(0);
				doStartBackup(backupBtn);
			}
		});
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
		frame.setSize(888, 666);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		glassPane = new InfiniteProgressPanel();
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		glassPane.setBounds(100, 100, (dimension.width) / 2, (dimension.height) / 2);
		frame.setGlassPane(glassPane);

		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);

		createBackupPanel(tabbedPane);

		createUploadPanel(tabbedPane);

		createQrCodePanel(tabbedPane);

		createTasklistPanel(tabbedPane);

		initMenus();
		initDatas();
		initDnd();
	}

	private void createBackupPanel(JTabbedPane tabbedPane) {
		JPanel firstPanel = new JPanel();
		tabbedPane.addTab("文件备份", firstPanel);
		firstPanel.setLayout(new BorderLayout(0, 0));

		JPanel firstPanel_1 = new JPanel();
		firstPanel.add(firstPanel_1, BorderLayout.NORTH);
		firstPanel_1.setLayout(new BoxLayout(firstPanel_1, BoxLayout.X_AXIS));

		urlCombo = new JComboBox<String>();
		urlCombo.setFont(new Font("宋体", Font.PLAIN, 16));
		urlCombo.setEditable(true);
		urlCombo.setToolTipText("请输入读乐乐服务URL");
		firstPanel_1.add(urlCombo);

		backupBtn = new JButton("开始备份");
		backupBtn.setHorizontalAlignment(SwingConstants.RIGHT);
		firstPanel_1.add(backupBtn);

		folderListBtn = new JButton("刷新文件夹");
		folderListBtn.setHorizontalAlignment(SwingConstants.RIGHT);
		firstPanel_1.add(folderListBtn);

		backupTableModel = new DefaultTableModel(null, columnNames);
		backupTable = new JTable(backupTableModel);
		backupTable.setFillsViewportHeight(true);
		for (int i = 0; i < backupTable.getColumnModel().getColumnCount(); i++) {
			backupTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
		}
		backupTable.getTableHeader().setVisible(true);
		backupTable.setShowGrid(true);
		backupTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			/*** 序列号 */
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				label.setToolTipText(String.valueOf(table.getValueAt(row, column)));
				return label;
			}
		});

		JScrollPane backupscrollPane = new JScrollPane(backupTable); // 支持滚动

//		backupTablePopupMenu = new JPopupMenu();
//		addPopup(backupTable, backupTablePopupMenu);
//
//		backupTableCopyMenu = new JMenuItem("复制");
//		backupTablePopupMenu.add(backupTableCopyMenu);
//
//		backupTableClearMenu = new JMenuItem("清空");
//		backupTablePopupMenu.add(backupTableClearMenu);
		firstPanel.add(backupscrollPane);
	}

	private void createUploadPanel(JTabbedPane tabbedPane) {
		JPanel secondPanel = new JPanel();
		tabbedPane.addTab("文件上传", secondPanel);
		secondPanel.setLayout(new BorderLayout(0, 0));

		JPanel secondPanel_1 = new JPanel();
		secondPanel.add(secondPanel_1, BorderLayout.NORTH);
		secondPanel_1.setLayout(new BoxLayout(secondPanel_1, BoxLayout.X_AXIS));

		urlCombo2 = new JComboBox<String>();
		urlCombo2.setFont(new Font("宋体", Font.PLAIN, 16));
		urlCombo2.setEditable(true);
		urlCombo2.setToolTipText("请输入读乐乐服务URL");
		secondPanel_1.add(urlCombo2);

		uploadFolderBtn = new JButton("上传文件夹");
		uploadFolderBtn.setHorizontalAlignment(SwingConstants.RIGHT);
		secondPanel_1.add(uploadFolderBtn);

		uploadTableModel = new DefaultTableModel(null, uploadColumnNames);
		uploadTable = new JTable(uploadTableModel);
		uploadTable.setFillsViewportHeight(true);
		for (int i = 0; i < uploadTable.getColumnModel().getColumnCount(); i++) {
			uploadTable.getColumnModel().getColumn(i).setPreferredWidth(uploadColumnWidths[i]);
		}
		uploadTable.setCellEditor(null);
		uploadTable.setCellSelectionEnabled(false);
		uploadTable.getTableHeader().setVisible(true);
		uploadTable.setShowGrid(true);
		uploadTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			/*** 序列号 */
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				label.setToolTipText(String.valueOf(table.getValueAt(row, column)));
				return label;
			}
		});

		JScrollPane uploadscrollPane = new JScrollPane(uploadTable); // 支持滚动
		uploadscrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

//		uploadTablePopupMenu = new JPopupMenu();
//		addPopup(uploadTable, uploadTablePopupMenu);
//
//		updateTableCopyMenu = new JMenuItem("复制");
//		uploadTablePopupMenu.add(updateTableCopyMenu);
//
//		updateTableClearMenu = new JMenuItem("清空");
//		uploadTablePopupMenu.add(updateTableClearMenu);
		secondPanel.add(uploadscrollPane);
	}

	private void createQrCodePanel(JTabbedPane tabbedPane) {
		JPanel qrCodePanel = new JPanel();
		tabbedPane.addTab("码云传", qrCodePanel);
		qrCodePanel.setLayout(new BorderLayout(0, 0));

		JPanel qrCodePanel_1 = new JPanel();
		qrCodePanel.add(qrCodePanel_1, BorderLayout.NORTH);
		qrCodePanel_1.setLayout(new BoxLayout(qrCodePanel_1, BoxLayout.X_AXIS));

		qrCodeButton = new JButton("菜单栏");
		qrCodeButton.setHorizontalAlignment(SwingConstants.RIGHT);
		qrCodePanel_1.add(qrCodeButton);

		qrCodeFileTitle = new JLabel();
		qrCodeFileTitle.setAlignmentX(0.5f);
		qrCodeFileTitle.setText("请将文件拖入到下方                              ");
		qrCodeFileTitle.setFont(new Font("宋体", Font.PLAIN, 12));
		qrCodePanel_1.add(qrCodeFileTitle);

		qrCodeTableModel = new DefaultTableModel(null, new String[] { "0", "1", "2", "4", "5" });
		qrCodeTable = new JTable(qrCodeTableModel);
		qrCodeTable.setFillsViewportHeight(true);
		qrCodeTable.setRowHeight(30);
		qrCodeTable.setFont(new Font("宋体", Font.PLAIN, 12));
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

		JScrollPane tableScrollPanel = new JScrollPane(qrCodeTable); // 支持滚动
		qrCodePanel.add(tableScrollPanel);
	}

	private void createTasklistPanel(JTabbedPane tabbedPane) {
		taskContailerPanel = new JPanel();
		tabbedPane.addTab("任务列表", taskContailerPanel);
		taskContailerPanel.setLayout(new BorderLayout(0, 0));

		JPanel titlePanel = new JPanel();
		taskContailerPanel.add(titlePanel, BorderLayout.NORTH);
		titlePanel.setLayout(new BoxLayout(titlePanel, BoxLayout.X_AXIS));

		taskListTitle = new JLabel();
		taskListTitle.setAlignmentX(0.5f);
		taskListTitle.setText("                              ");
		taskListTitle.setFont(new Font("宋体", Font.PLAIN, 12));
		titlePanel.add(taskListTitle);

		taskListTableModel = new DefaultTableModel(null, columnNames);
		taskListTable = new JTable(taskListTableModel);
		taskListTable.setFillsViewportHeight(true);
		for (int i = 0; i < taskListTable.getColumnModel().getColumnCount(); i++) {
			taskListTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
		}
		taskListTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
			/*** 序列号 */
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
				JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
				label.setToolTipText(String.valueOf(table.getValueAt(row, column)));
				return label;
			}
		});
		taskListTable.setFont(new Font("宋体", Font.PLAIN, 12));
		taskListTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		JScrollPane tableScrollPanel = new JScrollPane(taskListTable); // 支持滚动
		taskContailerPanel.add(tableScrollPanel);
	}

	private void initMenus() {
		popupMenu = new JPopupMenu();
		addPopupMenu(qrCodeButton, popupMenu);

		JMenuItem qrCodeTransferMenuItem = new JMenuItem("码云传");
		qrCodeTransferMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					popupMenu.setVisible(false);
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

		{
			// 创建右键菜单
			JPopupMenu popupMenu = new JPopupMenu();
			JMenuItem copyItem = new JMenuItem("复制");
			popupMenu.add(copyItem);
			copyItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String selectedItem = (String) urlCombo.getSelectedItem();
					if (selectedItem != null) {
						setSysClipboardText(selectedItem);
					}
				}
			});
			JMenuItem pasteItem = new JMenuItem("粘贴");
			popupMenu.add(pasteItem);
			pasteItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					pasteToCombo(urlCombo);
				}
			});
			JMenuItem openUrlItem = new JMenuItem("打开网址");
			popupMenu.add(openUrlItem);
			openUrlItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String hostUrl = getInputHostUrl(urlCombo);
					try {
						Desktop.getDesktop().browse(URI.create(hostUrl));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			});
			urlCombo.setComponentPopupMenu(popupMenu);
		}

		{
			// 创建右键菜单
			JPopupMenu popupMenu = new JPopupMenu();
			JMenuItem copyItem = new JMenuItem("复制");
			popupMenu.add(copyItem);
			copyItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String selectedItem = (String) urlCombo2.getSelectedItem();
					if (selectedItem != null) {
						setSysClipboardText(selectedItem);
					}
				}
			});
			JMenuItem pasteItem = new JMenuItem("粘贴");
			popupMenu.add(pasteItem);
			pasteItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					pasteToCombo(urlCombo2);
				}
			});
			JMenuItem openUrlItem = new JMenuItem("打开网址");
			popupMenu.add(openUrlItem);
			openUrlItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String hostUrl = getInputHostUrl(urlCombo2);
					try {
						Desktop.getDesktop().browse(URI.create(hostUrl));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			});
			urlCombo2.setComponentPopupMenu(popupMenu);
		}
	}

	private void initDnd() {
		new DropTarget(uploadTable, DnDConstants.ACTION_COPY_OR_MOVE, new DropTargetAdapter() {
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
					uploadTableModel.setRowCount(0);
					try {
						Object td = transfer.getTransferData(DataFlavor.javaFileListFlavor);
						if (td instanceof List) {
							for (Object value : ((List<?>) td)) {
								if (value instanceof File) {
									File file = (File) value;
									if (file.isDirectory()) {
										currentUploadFolder = file;
										System.out.println("开始上传文件夹: " + currentDirectory);
										new Thread(new Runnable() {
											@Override
											public void run() {
												try {
													File[] allFiles = doPreUploadFolder(currentUploadFolder);
													doUploadFolder(allFiles);
												} catch (Exception ex) {
													ex.printStackTrace();
												}
											}
										}).start();
									} else {
										currentUploadFolder = file.getParentFile();
										addToUploadTable(file);
										doUploadFolder(new File[] { file }, true);
									}
								}
							}
						}
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		});
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

	protected File[] doPreUploadFolder(File file) throws IOException {
		uploadTableModel.getDataVector().clear();
		File[] allFiles = FileUtils.listFiles(file).toArray(new File[0]);
		for (int i = 0; i < allFiles.length; i++) {
			File subFile = allFiles[i];
			// new String[] { "文件名", "路径", "大小", "修改时间" };
			addToUploadTable(subFile);
		}
		uploadCount.set(0);
		return allFiles;
	}

	private void addToUploadTable(File srcFile) throws IOException {
		uploadTableModel.addRow(new String[] { String.valueOf(uploadTableModel.getRowCount() + 1), //
				srcFile.getName(), //
				"0%", //
				srcFile.getCanonicalPath(), //
				FileUtils.getFileSize(srcFile.length()), //
				new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(srcFile.lastModified())) });
	}

	private void initDatas() {
		addItemsToCombo(urlCombo, new String[] { "http://192.168.43.1:61666" }, 0);
		addItemsToCombo(urlCombo2, new String[] { "http://192.168.43.1:61666" + FOLDER_UPLOAD }, 0);
	}

	/**
	 * 将字符串复制到剪切板。
	 */
	public static void setSysClipboardText(String writeMe) {
		Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable tText = new StringSelection(writeMe);
		clip.setContents(tText, null);
	}

	private String getInputHostUrl() {
		return getInputHostUrl(urlCombo);
	}

	private String getInputHostUrl(JComboBox<String> urlCombo) {
		try {
			String newItem = String.valueOf(urlCombo.getEditor().getItem());
			URL url = new URL(newItem);
			return "http://" + String.format("%s:%s", url.getHost(), url.getPort());
		} catch (Exception ex) {
			return null;
		}
	}

	protected void doSyncFolder(List<Map<String, Object>> jsonArray, File toFile, final String selectedFolder) throws IOException {
		String inputHostUrl = getInputHostUrl();
		if (inputHostUrl == null || inputHostUrl.isEmpty()) {
			JOptionPane.showMessageDialog(null, "请输入正确的服务器地址！");
			return;
		}
		tabbedPane.setSelectedComponent(taskContailerPanel);
		taskListTableModel.setRowCount(0);

		for (int row = 0; row < jsonArray.size(); row++) {
			Map<String, Object> element = jsonArray.get(row);
			final String id = (String) element.get(FILESERVER_MD5);
			final String title = (String) element.get(FILESERVER_NAME);
			final String url = (String) element.get(FILESERVER_PATH);
			final String size = (String) element.get(FILESERVER_SIZE);
			final String absPath = (String) element.get(FILESERVER_ABSPATH);

			if (title.startsWith(".") || (title.contains("[文件夹]")))
				continue;

			taskListTableModel.addRow(new Object[] { id, title, url, size, "", absPath });
		}

		total.set(taskListTableModel.getRowCount());

		for (int row = 0; row < taskListTableModel.getRowCount(); row++) {
			final String id = (String) taskListTableModel.getValueAt(row, 0);
			final String title = (String) taskListTableModel.getValueAt(row, 1);
			final String url = (String) taskListTableModel.getValueAt(row, 2);
			final String size = (String) taskListTableModel.getValueAt(row, 3);
			final String absPath = (String) taskListTableModel.getValueAt(row, 5);

			taskListTable.setRowSelectionInterval(row, row);
			taskListTable.scrollRectToVisible(new Rectangle(taskListTable.getCellRect(row + 10, 0, true)));

			final int col = taskListTableModel.getColumnCount() - 2;

			String newFileName = title;
			if (absPath != null && absPath.length() > 0) {
				newFileName = absPath;
			} else {
				int firstIndexOf = url.indexOf("/" + toFile.getName() + "/");
				if (firstIndexOf != -1)
					newFileName = url.substring(firstIndexOf + 1);
				if (newFileName.startsWith(toFile.getName() + "/"))
					newFileName = newFileName.substring((toFile.getName() + "/").length());
			}

			try {
				File destFile = new File(toFile, newFileName);
				if (destFile.exists() && toSizeStr(destFile.length()).equals(size)) {
					taskListTableModel.setValueAt("文件已存在!" + destFile.getCanonicalPath(), row, col - 1);
					taskListTableModel.setValueAt(destFile.getCanonicalFile(), row, col);
					continue;
				}

				try {
					String getUrl = downloadFile(id, row, col, destFile);
					System.out.println(getUrl);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			} finally {
				frame.setTitle(String.format("%s (已处理: %s/%s项)", TITLE, counter.incrementAndGet(), total.get()));
			}
		}
	}

	protected String downloadFile(final String id, final int row, final int col, File destFile) throws IOException {
		String getUrl = getInputHostUrl() + FOLDER_DOWNLOAD_MD5 + id;
		File tmpFile = new File(destFile.getParent(), destFile.getName() + ".dulele");
		DownloadUtil.get().download(getUrl, id, tmpFile, new OnDownloadListener() {
			@Override
			public void onDownloadSuccess(File file) {
				try {
					taskListTableModel.setValueAt("100%", row, col - 1);
					taskListTableModel.setValueAt(destFile.getCanonicalFile(), row, col);
					Files.move(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} catch (Exception ex) {
					LogHandler.error(ex);
					ex.printStackTrace();
				}
			}

			@Override
			public void onDownloading(int progress) {
				taskListTableModel.setValueAt(progress + "%", row, col - 1);
			}

			@Override
			public void onDownloadFailed(Exception e) {
				taskListTableModel.setValueAt("0%", row, col - 1);
				taskListTableModel.setValueAt("下载失败" + e.getMessage(), row, col);
			}

			@Override
			public boolean isFileExists(File srcFile) {
				return srcFile.exists() && srcFile.length() > 0;
			}

			@Override
			public void onFileExists(File file) {
				try {
					taskListTableModel.setValueAt("文件已存在!" + file.getCanonicalPath(), row, col - 1);
					taskListTableModel.setValueAt(file.getPath(), row, col);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		return getUrl;
	}

	private String toSizeStr(long fileLen) {
		DecimalFormat df = new DecimalFormat("0.00");
		if (fileLen > 1024 * 1024) {
			return df.format(fileLen * 1f / 1024 / 1024) + "MB";
		} else if (fileLen > 1024) {
			return df.format(fileLen * 1f / 1024) + "KB";
		} else {
			return fileLen + "B";
		}
	}

	private String getComboText(JComboBox<String> comboBox) {
		return String.valueOf(comboBox.getEditor().getItem());
	}

	private void setComboBox(JComboBox<String> comboBox, String newItem) {
		DefaultComboBoxModel<String> d = (DefaultComboBoxModel<String>) comboBox.getModel();
		d.addElement(newItem);
		d.setSelectedItem(newItem);
	}

	protected boolean doBackFiles(int page) throws IOException, SQLException {
		urlCombo.setSelectedItem(String.valueOf(urlCombo.getSelectedItem()).replace("：", ":"));
		String backupListUrl = String.format("%s/dll/export/list", getComboText(urlCombo));
		Request request = new Request.Builder().addHeader("x-header", "dll")//
				.header("page", String.valueOf(page))//
				.header("size", String.valueOf(100))//
				.header("sort", "_id")//
				.url(backupListUrl).build();
		Response response = DownloadUtil.get().newCall(request);
		if (!response.isSuccessful()) {
			throw new UnsupportedOperationException("文件下载失败:" + response.message());
		}
		String body = response.body().string();
		JsonArray jsonArray = new Gson().fromJson(body, JsonArray.class);

		Map<String, String> whereMap = new HashMap<String, String>();
		for (int i = 0; i < jsonArray.size(); i++) {
			JsonObject element = jsonArray.get(i).getAsJsonObject();
			System.out.println("开始执行:" + element);
			final String id = element.get("_id").getAsString();
			final String title = element.get("title").getAsString();
			final String url = element.get("url").getAsString();
			final long length = element.get("foldersize").getAsLong();

			whereMap.put(BackupTask.KEY_ID, id);

			List<Map<String, Object>> resList = database.dbQuery(backupTask.getTableName(), whereMap);
			if (resList != null && resList.size() > 0) {
				backupTableModel.insertRow(0, new Object[] { id, title, url, FileUtils.getFileSize(length), "文件已存在！" });
//				continue;
			}
			backupTableModel.insertRow(0, new Object[] { id, title, url, FileUtils.getFileSize(length), "0%" });

			backupTable.scrollRectToVisible(backupTable.getCellRect(0, 0, true));
			backupTable.setRowSelectionInterval(0, 0);
			backupTable.setSelectionBackground(Color.LIGHT_GRAY);// 选中行设置背景色

			final int row = 0, col = backupTableModel.getColumnCount() - 1;
			final String getUrl = String.format("%s/dll/export/%s", getComboText(urlCombo), id);

			DownloadUtil.get().download(getUrl, id, "backup", new OnDownloadListener() {
				@Override
				public boolean isFileExists(File srcFile) {
					return srcFile.exists() && srcFile.length() > 0;
				}

				@Override
				public void onDownloadSuccess(File file) {
					try {
						backupTableModel.setValueAt("100%", row, col - 1);
						backupTableModel.setValueAt(file.getCanonicalFile(), row, col);
						backupTask.setId(id);
						backupTask.setTitle(title);
						backupTask.setUrl(url);
						backupTask.setLength(String.valueOf(length));
						database.dbInsert(backupTask);
					} catch (Exception ex) {
						LogHandler.error(ex);
						ex.printStackTrace();
					}
				}

				@Override
				public void onDownloading(int progress) {
					backupTableModel.setValueAt(progress + "%", row, col - 1);
				}

				@Override
				public void onDownloadFailed(Exception e) {
					backupTableModel.setValueAt("0%", row, col - 1);
					backupTableModel.setValueAt("下载失败" + e.getMessage(), row, col);
				}

				@Override
				public void onFileExists(File file) {
					backupTableModel.setValueAt("文件已存在:", row, col - 1);
					backupTableModel.setValueAt(file.getPath(), row, col);
				}
			});

			if (backupTableModel.getDataVector().size() > 5000) {
				backupTableModel.setRowCount(0);
			}

		}
		return jsonArray.size() > 0;
	}

	protected void doStartBackup(final JButton backupBtn) {
		LogHandler.debug("开始备份...");
		backupTask = new BackupTask();
		try {
			database = new DataBaseProxy();
			frame.getTitle();
			int res = database.dbCreate(backupTask);
			System.out.println(res);

			File toDir = new File("backup");
			if (!toDir.exists())
				toDir.mkdir();

			if (startThread != null) {
				startThread.interrupt();
				startThread = null;
			} else {
				startThread = new Thread(new Runnable() {
					@Override
					public void run() {
						for (int i = 0; i < Integer.MAX_VALUE; i++) {
							try {
								if (startThread == null || startThread.isInterrupted())
									break;
								if (!doBackFiles(i))
									break;
							} catch (UnsupportedOperationException ex) {
								JOptionPane.showMessageDialog(null, ex.getMessage());
								throw ex;
							} catch (Exception ex) {
								LogHandler.error(ex);
								ex.printStackTrace();
							}
						}
						backupBtn.setText("开始备份");
					}
				});
			}
			startThread.start();

			backupBtn.setText(startThread == null ? "开始备份" : "停止备份");
		} catch (Exception ex) {
			LogHandler.error(ex);
			ex.printStackTrace();
		}
	}

	protected void addItemsToCombo(JComboBox<String> urlCombo, String[] items, int index) {
		DefaultComboBoxModel<String> d = (DefaultComboBoxModel<String>) urlCombo.getModel();
		d.removeAllElements();
		for (String item : items) {
			d.addElement(item);
		}
		d.setSelectedItem(items[index]);
	}

	private void parseFile2QrCodes(final File getFile) {
		if (getFile.length() > FileUtils.ONE_MB) {
			JOptionPane.showMessageDialog(null, getFile.getPath(), String.format("文件大小[%s]超过1M！不允许使用!", FileUtils.getFileSize(getFile.length())),
					JOptionPane.INFORMATION_MESSAGE);
			return;
		}

		glassPane.start();// 开始动画加载效果
		frame.setVisible(true);

		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					String fileStr = Base64FileUtil.getFileStr(getFile.getCanonicalPath());
					String generateFile = Base64FileUtil.generateFile(getFile, fileStr);

					glassPane.stop();

//					Desktop.getDesktop().open(new File(generateFile));

					qrCodeFiles = new File(generateFile).listFiles();
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

	private void doUploadFolder(final File[] allFiles) throws IOException, InterruptedException {
		doUploadFolder(allFiles, false);
	}

	private void doUploadFolder(final File[] allFiles, boolean onlyFile) throws IOException, InterruptedException {
		if (allFiles == null || allFiles.length == 0 || currentUploadFolder == null)
			return;

		String uploadUrl = getComboText(urlCombo2);
		final boolean deleteOnSuccess = uploadUrl.endsWith(FLAG_DELETE_ON_SUCCESS);

		uploadUrl = uploadUrl.contains("#--") ? uploadUrl.substring(0, uploadUrl.indexOf("#--")) : uploadUrl;
		uploadUrl += uploadUrl.endsWith(FOLDER_UPLOAD) ? "" : FOLDER_UPLOAD;

		if (!NetworkUtils.isNetworkAvailable(uploadUrl)) {
			JOptionPane.showMessageDialog(null, "无法连接到服务地址: " + uploadUrl);
			Thread.sleep(15000);
			doUploadFolder(allFiles);
			return;
		}

		total.set(allFiles.length);
		counter.set(0);

		final int updateColumn = uploadTable.getColumnModel().getColumnIndex(COL_PROGRESS);

		final ExecutorService es = Executors.newFixedThreadPool(1);

		for (int i = 0; i < allFiles.length; i++) {
			if (allFiles[i].isHidden())
				continue;
			final String uploadUrl0 = uploadUrl;
			final String path = allFiles[i].getParentFile().getCanonicalPath()
					.substring(currentUploadFolder.getParentFile().getCanonicalPath().length() + 1);
			final int index = i;
			es.submit(new Runnable() {
				@Override
				public void run() {
					try {
						boolean result = uploadFile(path, uploadOkHttpClient, uploadUrl0, allFiles[index], updateColumn, index, deleteOnSuccess);
						if (!result)
							es.submit(this);
					} catch (Exception e) {
						es.submit(this);
					}
				}
			});
		}
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

	protected boolean uploadFile(String path, OkHttpClient mOkHttpClient, String uploadUrl, File subFile, final int updateCol, final int currentRow)
			throws IOException {
		return uploadFile(path, mOkHttpClient, uploadUrl, subFile, updateCol, currentRow, false);
	}

	protected boolean uploadFile(String path, OkHttpClient mOkHttpClient, String uploadUrl, final File subFile, final int updateCol, final int currentRow,
			final boolean deleteOnSuccess) throws IOException {
		return uploadFile(path, mOkHttpClient, uploadUrl, subFile, updateCol, currentRow, deleteOnSuccess, false);
	}

	protected boolean uploadFile(String path, OkHttpClient mOkHttpClient, String uploadUrl, final File subFile, final int updateCol, final int currentRow,
			final boolean deleteOnSuccess, boolean isAsync) throws IOException {
		MultipartBody.Builder builder = new MultipartBody.Builder();
		builder.setType(MultipartBody.FORM);
		builder.addPart(RequestBody.create(MediaType.parse("application/octet-stream"), subFile));

		RequestBody requestBody = new ProgressRequestBody(builder.build(), new ProgressRequestListener() {
			@Override
			public void onRequestProgress(long bytesWrite, long contentLength, boolean done) {
				try {
					String progress = (100 * bytesWrite) / contentLength + "%";
					uploadTableModel.setValueAt(progress, currentRow, updateCol);
				} catch (Exception e) {
					// e.printStackTrace();
				}
			}
		});

		Request request = new Request.Builder()//
				.url(uploadUrl + "?path=" + URLEncoder.encode(path, "utf-8")//
						+ "&fileName=" + URLEncoder.encode(subFile.getName(), "utf-8"))//
				.post(requestBody).build();
		Call call = mOkHttpClient.newCall(request);

		if (!isAsync) {
			Response response = call.execute();
			if (response.isSuccessful() && deleteOnSuccess)
				subFile.delete();
			frame.setTitle(String.format("%s (已处理: %s/%s项)", TITLE, counter.incrementAndGet(), total.get()));
			uploadTableModel.setValueAt(response.isSuccessful() ? "100%" : response.message(), currentRow, updateCol);
			uploadTable.getSelectionModel().setSelectionInterval(currentRow, currentRow);
			uploadTable.scrollRectToVisible(new Rectangle(uploadTable.getCellRect(currentRow + 10, 0, true)));
			uploadTable.updateUI();
			return response.isSuccessful();
		} else {
			call.enqueue(new okhttp3.Callback() {
				@Override
				public void onFailure(okhttp3.Call call, final IOException e) {
					System.err.println("上传失败:" + e.getMessage());
				}

				@Override
				public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
					try {
						if (response.isSuccessful() && deleteOnSuccess)
							subFile.delete();

						uploadTableModel.setValueAt(response.isSuccessful() ? "100%" : response.message(), currentRow, updateCol);
						uploadTable.getSelectionModel().setSelectionInterval(currentRow, currentRow);
						uploadTable.scrollRectToVisible(new Rectangle(uploadTable.getCellRect(currentRow + 10, 0, true)));
						uploadTable.updateUI();
					} catch (Exception e) {
						// donothing
					}
					frame.setTitle(String.format("%s (已处理: %s/%s项)", TITLE, counter.incrementAndGet(), total.get()));
				}
			});
		}
		return true;
	}

	public boolean doPostFormData(String urlStr, LinkedHashMap<String, String> urlParams) throws Exception {
		FormBody.Builder builder = new FormBody.Builder();
		Iterator<Entry<String, String>> iter = urlParams.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, String> entry = iter.next();
			builder.add(entry.getKey(), entry.getValue());
		}
		FormBody formBody = builder.build();
		Request request = new Request.Builder().url(urlStr)//
				.post(formBody)//
				.addHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")//
				.build();

		OkHttpClient client = new OkHttpClient();

		Response response = client.newCall(request).execute();
		return response.isSuccessful();
	}

	private void refreshServerIp() {
		String newItem = String.valueOf(urlCombo.getEditor().getItem());
		if (!newItem.startsWith("http://"))
			newItem = "http://" + newItem;
		if (!newItem.contains(":61666") && !newItem.contains(":61667"))
			newItem = newItem + ":61666";

		Set<String> itemSet = new HashSet<String>();
		DefaultComboBoxModel<String> d = (DefaultComboBoxModel<String>) urlCombo.getModel();
		d.removeAllElements();
		try {
			itemSet.add(String.valueOf(newItem));
			URL url = new URL(newItem);
			String newUrl = "http://" + String.format("%s:%s", url.getHost(), url.getPort());
			itemSet.add(newUrl);
//			itemSet.add(String.valueOf(newUrl + FOLDER_LIST));
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		}
		addItemsToCombo(urlCombo, itemSet.toArray(new String[0]), 0);
	}

	private void doDownloadFolder(final String fileUrl, final String selectedFolder) {
		try {
			if (startFolderThread != null) {
				startFolderThread.interrupt();
				startFolderThread = null;
			}
			final JFileChooser fileChooser = new JFileChooser();// 文件选择器
			if (currentDirectory != null)
				fileChooser.setSelectedFile(currentDirectory);
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);// 设定只能选择到文件夹
			int state = fileChooser.showOpenDialog(null);// 此句是打开文件选择器界面的触发语句
			if (state == JFileChooser.APPROVE_OPTION) {
				glassPane.start();// 开始动画加载效果
				frame.setVisible(true);

				startFolderThread = new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Request request = new Request.Builder().addHeader("x-header", "dll")//
									.header("sort", "_id")//
									.url(fileUrl).build();
							Response response = DownloadUtil.get().newCall(request);
							if (!response.isSuccessful()) {
								JOptionPane.showMessageDialog(null, response.message());
								throw new IOException("无法连接到服务器: " + response);
							}
							String body = response.body().string();
							List<Map<String, Object>> jsonArray = new Gson().fromJson(body, new TypeToken<List<Map<String, Object>>>() {
							}.getType());
							final File toFile = fileChooser.getSelectedFile();// toFile为选择到的目录
							glassPane.stop();
//										total.set(newValue);
							total.set(jsonArray.size());
							doSyncFolder(jsonArray, new File(toFile, currentDirectory.getName()), selectedFolder);
							backupBtn.setText("开始备份");
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
				startFolderThread.start();
			}
		} catch (Exception ex) {
			LogHandler.error(ex);
		}
	}

	private void pasteToCombo(JComboBox<String> combo) {
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

	protected static void addPopup(Component component, final JPopupMenu popup) {
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
