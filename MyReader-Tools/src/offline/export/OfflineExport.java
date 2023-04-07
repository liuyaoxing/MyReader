package offline.export;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
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
import offline.export.db.BackupTask;
import offline.export.db.DataBaseProxy;
import offline.export.dialog.InfiniteProgressPanel;
import offline.export.log.LogHandler;
import offline.export.utils.Base64FileUtil;
import okhttp3.Request;
import okhttp3.Response;

public class OfflineExport {

	public static final String FILESERVER_NAME = "name";
	public static final String FILESERVER_SIZE = "size";
	public static final String FILESERVER_PATH = "path";
	public static final String FILESERVER_LENGTH = "length";
	public static final String FILESERVER_MD5 = "md5";

	private static final String FOLDER_LIST = "/folder/list";
	private static final String FOLDER_LIST_MD5 = "/folder/list/md5/";
	private static final String FOLDER_DOWNLOAD_MD5 = "/folder/download/md5/";

	private JFrame frame;
	private JComboBox<String> urlCombo;
	private JTable table;
	private DefaultTableModel tableModel;

	private String[] columnNames = new String[] { "ID", "标题", "URL", "大小", "状态", "保存路径" };
	private int[] columnWidths = new int[] { 50, 250, 50, 50, 50, 250 };
	protected BackupTask backupTask;
	protected DataBaseProxy database;

	protected Thread startThread, startFolderThread;

	private String frameTitle;

	private AtomicLong counter = new AtomicLong(0);

	private File currentDirectory;
	private JButton btnNewButton;
	private JPopupMenu popupMenu;
	private JMenuItem mntmNewMenuItem;

	private InfiniteProgressPanel glassPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		String name = ManagementFactory.getRuntimeMXBean().getName();
		System.out.println("ManagementFactory.getRuntimeMXBean().getName():" + name);
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

	/**
	 * Create the application.
	 */
	public OfflineExport() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setTitle("读乐乐备份工具 v3.26");
		frame.setSize(888, 666);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		glassPane = new InfiniteProgressPanel();
		Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
		glassPane.setBounds(100, 100, (dimension.width) / 2, (dimension.height) / 2);
		frame.setGlassPane(glassPane);

		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.NORTH);
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		urlCombo = new JComboBox<String>();
		urlCombo.setEditable(true);
		urlCombo.setToolTipText("请输入读乐乐服务URL");
		panel.add(urlCombo);
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
				addItemsToCombo(itemSet.toArray(new String[0]), 0);
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
			}
		});
		final JButton backupBtn = new JButton("开始备份");
		backupBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				backupBtn.setText(backupBtn.getText().equals("开始备份") ? "停止备份" : "开始备份");
				final String comboText = getComboText(urlCombo);
				if (comboText.endsWith(FOLDER_LIST)) {
					backupBtn.setText("开始备份");
					tableModel.getDataVector().clear();
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
							tableModel.insertRow(0, new Object[] { id, title, url, size, "", "" });
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
		backupBtn.setHorizontalAlignment(SwingConstants.RIGHT);
		panel.add(backupBtn);

		btnNewButton = new JButton("菜单栏");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
//					Robot robot = new Robot();
//					robot.mousePress(InputEvent.BUTTON3_MASK);
//					robot.mouseRelease(InputEvent.BUTTON3_MASK);

					Point location = btnNewButton.getLocationOnScreen();
					popupMenu.setLocation(location.x, location.y + btnNewButton.getSize().height);
					popupMenu.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		panel.add(btnNewButton);

		popupMenu = new JPopupMenu();
		addPopup(btnNewButton, popupMenu);

		mntmNewMenuItem = new JMenuItem("码云传");
		mntmNewMenuItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					popupMenu.setVisible(false);
					FileDialog fd = new FileDialog(frame);
//					fd.setMultipleMode(false);
					fd.setTitle("请选择文件");
					fd.setVisible(true);
					final File getFile = new File(fd.getDirectory(), fd.getFile());
					if (getFile.length() > FileUtils.ONE_MB) {
						JOptionPane.showMessageDialog(null, "文件大小超过1M！不允许使用：" + getFile.length() + "," + getFile.getPath());
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
								JOptionPane.showMessageDialog(null, "文件生成成功:" + generateFile);
								Desktop.getDesktop().open(new File(generateFile));
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}).start();

				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});
		popupMenu.add(mntmNewMenuItem);

		tableModel = new DefaultTableModel(null, columnNames);
		table = new JTable(tableModel);
		for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
		}
		table.getTableHeader().setVisible(true);
		table.setShowGrid(true);
		table.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent me) {
				frameTitle = frame.getTitle();
				if (SwingUtilities.isRightMouseButton(me)) {
					final int row = table.rowAtPoint(me.getPoint());
					table.setRowSelectionInterval(row, row);
					System.out.println("row:" + row);
					if (row != -1) {
						final int column = table.columnAtPoint(me.getPoint());

						final JPopupMenu popup = new JPopupMenu();
						JMenuItem copyItem = new JMenuItem("复制");
						popup.add(copyItem);
						copyItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								Object value = table.getValueAt(row, column);
								if (value != null)
									setSysClipboardText(String.valueOf(value));
							}
						});

						if (getComboText(urlCombo).contains(FOLDER_LIST)) {
							JMenuItem syncFolderItem = new JMenuItem("下载文件夹");
							popup.add(syncFolderItem);
							syncFolderItem.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent e) {
									Object title = table.getValueAt(row, 1);
									if (title != null) {
										try {
											currentDirectory = new File(title.toString().replace("[文件夹]", "")).getCanonicalFile();
										} catch (IOException e1) {
											e1.printStackTrace();
										}
									}

									Object value = table.getValueAt(row, 0);
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

		JScrollPane scrollPane = new JScrollPane(table); // 支持滚动

		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);

		initDatas();
	}

	private void initDatas() {
		addItemsToCombo(new String[] { "http://192.168.43.1:61666" }, 0);
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
		tableModel.getDataVector().clear();
		for (int i = 0; i < jsonArray.size(); i++) {
			JsonObject element = jsonArray.get(i).getAsJsonObject();
			final String id = element.get(FILESERVER_MD5).getAsString();
			final String title = element.get(FILESERVER_NAME).getAsString();
			final String url = element.get(FILESERVER_PATH).getAsString();
			final String size = element.get(FILESERVER_SIZE).getAsString();

			if (title.startsWith(".") || (title.contains("[文件夹]")) || "0B".equals(size))
				continue;

			tableModel.insertRow(0, new Object[] { id, title, url, size, "", "" });
			table.setRowSelectionInterval(0, 0);

			final int row = 0, col = tableModel.getColumnCount() - 1;

			File destFile = new File(toFile, title);
			if (destFile.exists() && toSizeStr(destFile.length()).equals(size)) {
				tableModel.setValueAt("文件已存在!", row, col - 1);
				tableModel.setValueAt(destFile.getCanonicalFile(), row, col);
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
					tableModel.setValueAt("100%", row, col - 1);
					tableModel.setValueAt(file.getCanonicalFile(), row, col);
				} catch (Exception ex) {
					LogHandler.error(ex);
					ex.printStackTrace();
				}
			}

			@Override
			public void onDownloading(int progress) {
				tableModel.setValueAt(progress + "%", row, col - 1);
			}

			@Override
			public void onDownloadFailed(Exception e) {
				tableModel.setValueAt("0%", row, col - 1);
				tableModel.setValueAt("下载失败" + e.getMessage(), row, col);
			}

			@Override
			public boolean isFileExists(File srcFile) {
				return srcFile.exists() && srcFile.length() > 0;
			}

			@Override
			public void onFileExists(File file) {
				tableModel.setValueAt("文件已存在:", row, col - 1);
				tableModel.setValueAt(file.getPath(), row, col);
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
				tableModel.insertRow(0, new Object[] { id, title, url, FileUtils.getFileSize(length), "文件已存在！" });
//				continue;
			}
			tableModel.insertRow(0, new Object[] { id, title, url, FileUtils.getFileSize(length), "0%" });

			table.scrollRectToVisible(table.getCellRect(0, 0, true));
			table.setRowSelectionInterval(0, 0);
			table.setSelectionBackground(Color.LIGHT_GRAY);// 选中行设置背景色

			final int row = 0, col = tableModel.getColumnCount() - 1;
			final String getUrl = String.format("%s/dll/export/%s", getComboText(urlCombo), id);

			DownloadUtil.get().download(getUrl, id, "backup", new OnDownloadListener() {
				@Override
				public boolean isFileExists(File srcFile) {
					return srcFile.exists() && srcFile.length() > 0;
				}

				@Override
				public void onDownloadSuccess(File file) {
					try {
						tableModel.setValueAt("100%", row, col - 1);
						tableModel.setValueAt(file.getCanonicalFile(), row, col);
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
					tableModel.setValueAt(progress + "%", row, col - 1);
				}

				@Override
				public void onDownloadFailed(Exception e) {
					tableModel.setValueAt("0%", row, col - 1);
					tableModel.setValueAt("下载失败" + e.getMessage(), row, col);
				}

				@Override
				public void onFileExists(File file) {
					tableModel.setValueAt("文件已存在:", row, col - 1);
					tableModel.setValueAt(file.getPath(), row, col);
				}
			});

			if (tableModel.getDataVector().size() > 5000) {
				tableModel.getDataVector().clear();
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

	protected void addItemsToCombo(String[] items, int index) {
		DefaultComboBoxModel<String> d = (DefaultComboBoxModel<String>) urlCombo.getModel();
		d.removeAllElements();
		for (String item : items) {
			d.addElement(item);
		}
		d.setSelectedItem(items[index]);
	}

	private static void addPopup(Component component, final JPopupMenu popup) {
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
