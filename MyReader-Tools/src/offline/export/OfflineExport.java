package offline.export;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.TimeUnit;
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
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
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
import offline.export.utils.ProgressRequestBody;
import offline.export.utils.ProgressRequestListener;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import java.awt.Component;

public class OfflineExport {

	public static final String FILESERVER_NAME = "name";
	public static final String FILESERVER_SIZE = "size";
	public static final String FILESERVER_PATH = "path";
	public static final String FILESERVER_LENGTH = "length";
	public static final String FILESERVER_MD5 = "md5";

	private static final String FOLDER_LIST = "/folder/list";
	private static final String FOLDER_LIST_MD5 = "/folder/list/md5/";
	private static final String FOLDER_DOWNLOAD_MD5 = "/folder/download/md5/";
	private static final String FOLDER_UPLOAD = "/folderUpload";

	private JFrame frame;
	private JComboBox<String> urlCombo, urlCombo2;
	private JTable backupTable;
	private DefaultTableModel backupTableModel;

	private String[] columnNames = new String[] { "ID", "标题", "URL", "大小", "状态", "保存路径" };
	private int[] columnWidths = new int[] { 50, 250, 50, 50, 50, 250 };

	private String[] uploadColumnNames = new String[] { "ID", "文件名", "进度", "路径", "大小", "修改时间" };
	private int[] uploadColumnWidths = new int[] { 6, 100, 6, 360, 10, 88 };

	protected BackupTask backupTask;
	protected DataBaseProxy database;

	protected Thread startThread, startFolderThread;

	private String frameTitle;

	private AtomicLong counter = new AtomicLong(0);

	private File currentDirectory;

	private InfiniteProgressPanel glassPane;
	private JButton backupBtn;
	private JTable qrCodeTable;
	private DefaultTableModel qrCodeTableModel;
	private JLabel qrCodeFileTitle;

	protected File[] qrCodeFiles;
	protected ViewerFrame viewerFrame;
	private JButton uploadFolderBtn;
	protected File currentUploadFolder;

	protected DefaultTableModel uploadTableModel;
	protected JTable uploadTable;
	private JButton qrCodeButton;
	private JPopupMenu popupMenu;

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
					try {
						doPreUploadFolder(currentUploadFolder);

						OkHttpClient.Builder builder = new OkHttpClient.Builder();
						builder.connectTimeout(10, TimeUnit.SECONDS);
						builder.writeTimeout(30, TimeUnit.SECONDS);
						builder.readTimeout(30, TimeUnit.SECONDS);
						doUploadFolder(builder.build(), currentUploadFolder);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}

			}
		});
		backupTable.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent me) {
				frameTitle = frame.getTitle();
				if (SwingUtilities.isRightMouseButton(me)) {
					final int row = backupTable.rowAtPoint(me.getPoint());
					backupTable.setRowSelectionInterval(row, row);
					System.out.println("row:" + row);
					if (row != -1) {
						final int column = backupTable.columnAtPoint(me.getPoint());

						final JPopupMenu popup = new JPopupMenu();
						JMenuItem copyItem = new JMenuItem("复制");
						popup.add(copyItem);
						copyItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								Object value = backupTable.getValueAt(row, column);
								if (value != null)
									setSysClipboardText(String.valueOf(value));
							}
						});

						if (getComboText(urlCombo).contains(FOLDER_LIST)) {
							JMenuItem syncFolderItem = new JMenuItem("下载文件夹");
							popup.add(syncFolderItem);
							syncFolderItem.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									Object title = backupTable.getValueAt(row, 1);
									if (title != null) {
										try {
											currentDirectory = new File(title.toString().replace("[文件夹]", ""))
													.getCanonicalFile();
										} catch (IOException e1) {
											e1.printStackTrace();
										}
									}

									Object value = backupTable.getValueAt(row, 0);
									if (value != null) {
										String newUrl = getInputHostUrl() + FOLDER_LIST_MD5 + value;
										setComboBox(urlCombo, newUrl);
										backupBtn.doClick();
									}
									setSysClipboardText(String.valueOf(value));
								}
							});
						}

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
				String newItem = String.valueOf(urlCombo.getEditor().getItem());

				Set<String> itemSet = new HashSet<String>();
				DefaultComboBoxModel<String> d = (DefaultComboBoxModel<String>) urlCombo.getModel();
				d.removeAllElements();
				try {
					itemSet.add(String.valueOf(newItem));
					URL url = new URL(newItem);
					String newUrl = "http://" + String.format("%s:%s", url.getHost(), url.getPort());
					itemSet.add(newUrl);
					itemSet.add(String.valueOf(newUrl + FOLDER_LIST));
					itemSet.add(String.valueOf(newUrl + FOLDER_LIST_MD5));
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				}
				addItemsToCombo(urlCombo, itemSet.toArray(new String[0]), 0);
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

		backupBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				backupBtn.setText(backupBtn.getText().equals("开始备份") ? "停止备份" : "开始备份");
				final String comboText = getComboText(urlCombo);
				if (comboText.endsWith(FOLDER_LIST)) {
					backupBtn.setText("开始备份");
					backupTableModel.getDataVector().clear();
					Request request = new Request.Builder().addHeader("x-header", "dll")//
							.header("sort", "_id")//
							.url(comboText).build();
					try {
						Response response = DownloadUtil.get().newCall(request);
						if (!response.isSuccessful()) {
							JOptionPane.showMessageDialog(null, response.message());
							return;
						}
						String body = response.body().string();

						Map<String, JsonObject> toMap = new Gson().fromJson(body,
								new TypeToken<Map<String, JsonObject>>() {
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
							backupTableModel.insertRow(0, new Object[] { id, title, url, size, "", "" });
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				} else if (comboText.contains(FOLDER_LIST_MD5)) {
					try {
						if (startFolderThread != null) {
							startFolderThread.interrupt();
							startFolderThread = null;
							return;
						}
						final JFileChooser fileChooser = new JFileChooser();// 文件选择器
						if (currentDirectory != null)
							fileChooser.setSelectedFile(new File(currentDirectory, "tmp"));
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
												.url(comboText).build();
										Response response = DownloadUtil.get().newCall(request);
										if (!response.isSuccessful()) {
											JOptionPane.showMessageDialog(null, response.message());
											throw new IOException("Unexpected code " + response);
										}
										String body = response.body().string();
										final JsonArray jsonArray = new Gson().fromJson(body, JsonArray.class);
										final File toFile = fileChooser.getSelectedFile();// toFile为选择到的目录
										glassPane.stop();
										doSyncFolder(jsonArray, toFile);
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
				} else {
					doStartBackup(backupBtn);
				}
			}
		});
	}

	/**
	 * Initialize the contents of the frame.
	 * 
	 * @param uploadTableModel
	 * @param uploadTable
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setTitle("读乐乐备份工具 v3.30");
		frame.setSize(888, 666);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		glassPane = new InfiniteProgressPanel();
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		glassPane.setBounds(100, 100, (dimension.width) / 2, (dimension.height) / 2);
		frame.setGlassPane(glassPane);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);

		JPanel firstPanel = new JPanel();
		tabbedPane.addTab("文件备份", firstPanel);
		firstPanel.setLayout(new BorderLayout(0, 0));

		JPanel firstPanel_1 = new JPanel();
		firstPanel.add(firstPanel_1, BorderLayout.NORTH);
		firstPanel_1.setLayout(new BoxLayout(firstPanel_1, BoxLayout.X_AXIS));

		urlCombo = new JComboBox<String>();
		urlCombo.setEditable(true);
		urlCombo.setToolTipText("请输入读乐乐服务URL");
		firstPanel_1.add(urlCombo);

		backupBtn = new JButton("开始备份");
		backupBtn.setHorizontalAlignment(SwingConstants.RIGHT);
		firstPanel_1.add(backupBtn);

		backupTableModel = new DefaultTableModel(null, columnNames);
		backupTable = new JTable(backupTableModel);
		backupTable.setFillsViewportHeight(true);
		for (int i = 0; i < backupTable.getColumnModel().getColumnCount(); i++) {
			backupTable.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
		}
		backupTable.getTableHeader().setVisible(true);
		backupTable.setShowGrid(true);

		JScrollPane backupscrollPane = new JScrollPane(backupTable); // 支持滚动
		firstPanel.add(backupscrollPane);

		JPanel secondPanel = new JPanel();
		tabbedPane.addTab("文件上传", secondPanel);
		secondPanel.setLayout(new BorderLayout(0, 0));

		JPanel secondPanel_1 = new JPanel();
		secondPanel.add(secondPanel_1, BorderLayout.NORTH);
		secondPanel_1.setLayout(new BoxLayout(secondPanel_1, BoxLayout.X_AXIS));

		urlCombo2 = new JComboBox<String>();
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
		uploadTable.getTableHeader().setVisible(true);
		uploadTable.setShowGrid(true);

		JScrollPane uploadscrollPane = new JScrollPane(uploadTable); // 支持滚动
		secondPanel.add(uploadscrollPane);

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

		JScrollPane uploadscrollPane2 = new JScrollPane(qrCodeTable); // 支持滚动
		qrCodePanel.add(uploadscrollPane2);

//		qrCodePanel.add(qrCodeTable, BorderLayout.CENTER);

		initMenus();
		initDatas();
		initDnd();
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
	}

	private void initDnd() {
		new DropTarget(urlCombo, DnDConstants.ACTION_COPY_OR_MOVE, new DropTargetAdapter() {
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

					try {
						Object td = transfer.getTransferData(DataFlavor.javaFileListFlavor);
						if (td instanceof List) {
							for (Object value : ((List<?>) td)) {
								if (value instanceof File) {
									File file = (File) value;
									if (file.isDirectory()) {
										currentUploadFolder = file;
										doPreUploadFolder(file);
										doUploadFolder(new okhttp3.OkHttpClient(), file);
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
		new DropTarget(uploadTable, DnDConstants.ACTION_COPY_OR_MOVE, new DropTargetAdapter() {
			@Override
			public void dragEnter(DropTargetDragEvent evt) {
				Transferable t = evt.getTransferable();
				if (!t.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					evt.rejectDrag(); // 没有需要的类型，拒绝进入
				}
				evt.acceptDrag(DnDConstants.ACTION_COPY);
			}

			@Override
			public void drop(DropTargetDropEvent dtde) {
				// 检测拖放进来的数据类型
				Transferable transfer = dtde.getTransferable();
				if (transfer.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					// 必须先调用acceptDrop
					dtde.acceptDrop(DnDConstants.ACTION_COPY);

					try {
						Object td = transfer.getTransferData(DataFlavor.javaFileListFlavor);
						if (td instanceof List) {
							for (Object value : ((List<?>) td)) {
								if (value instanceof File) {
									File file = (File) value;
									if (file.isDirectory()) {
										currentUploadFolder = file;
										doPreUploadFolder(file);
										doUploadFolder(new okhttp3.OkHttpClient(), file);
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

	protected void doPreUploadFolder(File file) throws IOException {
		uploadTableModel.getDataVector().clear();
		File[] allFiles = FileUtils.listFiles(file).toArray(new File[0]);
		for (int i = 0; i < allFiles.length; i++) {
			File subFile = allFiles[i];
			// new String[] { "文件名", "路径", "大小", "修改时间" };
			uploadTableModel.addRow(new String[] { String.valueOf(uploadTableModel.getRowCount() + 1), //
					subFile.getName(), //
					"0%", //
					subFile.getCanonicalPath(), //
					FileUtils.getFileSize(subFile.length()), //
					new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(subFile.lastModified())) });
		}
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
		try {
			String newItem = String.valueOf(urlCombo.getEditor().getItem());
			URL url = new URL(newItem);
			return "http://" + String.format("%s:%s", url.getHost(), url.getPort());
		} catch (Exception ex) {
			return null;
		}
	}

	protected void doSyncFolder(JsonArray jsonArray, File toFile) throws IOException {
		String inputHostUrl = getInputHostUrl();
		if (inputHostUrl == null || inputHostUrl.isEmpty()) {
			JOptionPane.showMessageDialog(null, "请输入正确的服务器地址！");
			return;
		}
		backupTableModel.getDataVector().clear();
		for (int i = 0; i < jsonArray.size(); i++) {
			JsonObject element = jsonArray.get(i).getAsJsonObject();
			final String id = element.get(FILESERVER_MD5).getAsString();
			final String title = element.get(FILESERVER_NAME).getAsString();
			final String url = element.get(FILESERVER_PATH).getAsString();
			final String size = element.get(FILESERVER_SIZE).getAsString();

			if (title.startsWith(".") || (title.contains("[文件夹]")) || "0B".equals(size))
				continue;

			backupTableModel.insertRow(0, new Object[] { id, title, url, size, "", "" });
			backupTable.setRowSelectionInterval(0, 0);

			final int row = 0, col = backupTableModel.getColumnCount() - 1;

			File destFile = new File(toFile, title);
			if (destFile.exists() && toSizeStr(destFile.length()).equals(size)) {
				backupTableModel.setValueAt("文件已存在!", row, col - 1);
				backupTableModel.setValueAt(destFile.getCanonicalFile(), row, col);
				continue;
			}

			try {
				frame.setTitle(String.format("%s (已处理: %s项)", frameTitle, counter.incrementAndGet()));
				String getUrl = downloadFile(id, row, col, destFile);
				System.out.println(getUrl);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	protected String downloadFile(final String id, final int row, final int col, File destFile) throws IOException {
		String getUrl = getInputHostUrl() + FOLDER_DOWNLOAD_MD5 + id;
		DownloadUtil.get().download(getUrl, id, destFile, new OnDownloadListener() {
			@Override
			public void onDownloadSuccess(File file) {
				try {
					backupTableModel.setValueAt("100%", row, col - 1);
					backupTableModel.setValueAt(file.getCanonicalFile(), row, col);
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
			public boolean isFileExists(File srcFile) {
				return srcFile.exists() && srcFile.length() > 0;
			}

			@Override
			public void onFileExists(File file) {
				backupTableModel.setValueAt("文件已存在:", row, col - 1);
				backupTableModel.setValueAt(file.getPath(), row, col);
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
		DefaultComboBoxModel<String> d = (DefaultComboBoxModel<String>) urlCombo.getModel();
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
			frame.setTitle(String.format("%s (已处理: %s项)", frameTitle, counter.incrementAndGet()));
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
				backupTableModel.getDataVector().clear();
			}

		}
		return jsonArray.size() > 0;
	}

	protected void doStartBackup(final JButton backupBtn) {
		LogHandler.debug("开始备份...");
		backupTask = new BackupTask();
		try {
			database = new DataBaseProxy();
			frameTitle = frame.getTitle();
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
			JOptionPane.showMessageDialog(null, getFile.getPath(),
					String.format("文件大小[%s]超过1M！不允许使用!", FileUtils.getFileSize(getFile.length())),
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

	private void doUploadFolder(okhttp3.OkHttpClient mOkHttpClient, File file) throws IOException {
		File[] subFiles = file.listFiles();
		if (subFiles == null || subFiles.length == 0)
			return;

		String uploadUrl = String.valueOf(urlCombo.getSelectedItem()) + FOLDER_UPLOAD;

		for (File subFile : subFiles) {
			if (subFile.isDirectory()) {
				doUploadFolder(mOkHttpClient, subFile);
			} else {
				String path = file.getCanonicalPath()
						.substring(currentUploadFolder.getParentFile().getCanonicalPath().length() + 1);
				uploadFile(path, mOkHttpClient, uploadUrl, subFile);
			}
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

	private int findRowByColValue(int col, String value) {
		for (int i = 0; i < uploadTableModel.getRowCount(); i++) {
			if (Objects.equals(uploadTableModel.getValueAt(i, col), value))
				return i;
		}
		return -1;
	}

	private void uploadFile(String path, okhttp3.OkHttpClient mOkHttpClient, String uploadUrl, File subFile)
			throws IOException {
		MultipartBody.Builder builder = new MultipartBody.Builder();
		builder.setType(MultipartBody.FORM);
		builder.addPart(RequestBody.create(MediaType.parse("application/octet-stream"), subFile));

		final int col = 3;
		final int row = findRowByColValue(col, subFile.getCanonicalPath());

		RequestBody requestBody = new ProgressRequestBody(builder.build(), new ProgressRequestListener() {
			@Override
			public void onRequestProgress(long bytesWrite, long contentLength, boolean done) {
				try {
					String progress = (100 * bytesWrite) / contentLength + "%";
					uploadTableModel.setValueAt(progress, row, col - 1);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		Request request = new Request.Builder()//
				.url(uploadUrl + "?path=" + path + "&fileName=" + subFile.getName())//
				.post(requestBody).build();
		Call call = mOkHttpClient.newCall(request);
		call.enqueue(new okhttp3.Callback() {
			@Override
			public void onFailure(okhttp3.Call call, final IOException e) {
//				uploadTableModel.setValueAt("上传失败", row, col - 1);
				System.err.println("上传失败:" + e.getMessage());
			}

			@Override
			public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
				uploadTableModel.setValueAt("100%", row, col - 1);

				uploadTable.setRowSelectionInterval(row, row);
				uploadTable.scrollRectToVisible(uploadTable.getCellRect(0, 0, true));
				uploadTable.setSelectionBackground(Color.LIGHT_GRAY);// 选中行设置背景色
			}
		});
	}
}
