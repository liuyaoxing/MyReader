package offline.export;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingConstants;
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
import offline.export.log.LogHandler;
import offline.export.utils.MD5Utils;
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

	protected Thread startThread;

	private String frameTitle;

	private AtomicLong counter = new AtomicLong(0);

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
					OfflineExport window = new OfflineExport();
					window.frame.setVisible(true);
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
		frame.setTitle("读乐乐备份工具 v3.24");
		frame.setSize(888, 666);
		frame.setLocationRelativeTo(null);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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
				for (String item : itemSet) {
					d.addElement(item);
				}
				d.setSelectedItem(newItem);
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
				String comboText = getComboText(urlCombo);
				if (comboText.endsWith(FOLDER_LIST)) {
					Request request = new Request.Builder().addHeader("x-header", "dll")//
							.header("sort", "_id")//
							.url(comboText).build();
					try {
						Response response = DownloadUtil.get().newCall(request);
						if (!response.isSuccessful()) {
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
							final String id = element.get(FILESERVER_MD5).getAsString();
							final String title = element.get(FILESERVER_NAME).getAsString();
							final String url = element.get(FILESERVER_PATH).getAsString();
							final String size = element.get(FILESERVER_SIZE).getAsString();
							tableModel.insertRow(-1, new Object[] { id, title, url, size, "" });
						}
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				} else if (comboText.contains(FOLDER_LIST_MD5)) {
					try {
						Request request = new Request.Builder().addHeader("x-header", "dll")//
								.header("sort", "_id")//
								.url(comboText).build();
						Response response = DownloadUtil.get().newCall(request);
						if (!response.isSuccessful()) {
							throw new IOException("Unexpected code " + response);
						}
						String body = response.body().string();
						JsonArray jsonArray = new Gson().fromJson(body, JsonArray.class);
						JFileChooser fileChooser = new JFileChooser();// 文件选择器
						fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);// 设定只能选择到文件夹
						int state = fileChooser.showOpenDialog(null);// 此句是打开文件选择器界面的触发语句
						if (state == JFileChooser.APPROVE_OPTION) {
							File toFile = fileChooser.getSelectedFile();// toFile为选择到的目录
							doSyncFolder(jsonArray, toFile);
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

		tableModel = new DefaultTableModel(null, columnNames);
		table = new JTable(tableModel);
		for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
			table.getColumnModel().getColumn(i).setPreferredWidth(columnWidths[i]);
		}
		table.getTableHeader().setVisible(true);
		table.setShowGrid(true);

		JScrollPane scrollPane = new JScrollPane(table); // 支持滚动

		frame.getContentPane().add(scrollPane, BorderLayout.CENTER);
	}

	private String getInputHostUrl() {
		return null;
	}

	protected void doSyncFolder(JsonArray jsonArray, File toFile) throws IOException {
		tableModel.getDataVector().clear();
		for (int i = 0; i < jsonArray.size(); i++) {
			JsonObject element = jsonArray.get(i).getAsJsonObject();
			final String id = element.get(FILESERVER_MD5).getAsString();
			final String title = element.get(FILESERVER_NAME).getAsString();
			final String url = element.get(FILESERVER_PATH).getAsString();
			final String size = element.get(FILESERVER_SIZE).getAsString();
			tableModel.insertRow(-1, new Object[] { id, title, url, size, "" });

			final int row = 0, col = tableModel.getColumnCount() - 1;

			File destFile = new File(toFile, title);
			if (destFile.exists() && id.equals(MD5Utils.encryptFileFast(destFile))) {
				tableModel.setValueAt("100%", row, col - 1);
				continue;
			}

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
			});
		}
	}

	private String getComboText(JComboBox<String> comboBox) {
		return String.valueOf(comboBox.getEditor().getItem());
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
			throw new IOException("Unexpected code " + response);
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
				continue;
			}
			tableModel.insertRow(0, new Object[] { id, title, url, FileUtils.getFileSize(length), "0%" });

			table.scrollRectToVisible(table.getCellRect(0, 0, true));
			table.setRowSelectionInterval(0, 0);
			table.setSelectionBackground(Color.LIGHT_GRAY);// 选中行设置背景色

			final int row = 0, col = tableModel.getColumnCount() - 1;
			final String getUrl = String.format("%s/dll/export/%s", getComboText(urlCombo), id);

			DownloadUtil.get().download(getUrl, id, "backup", new OnDownloadListener() {
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
							} catch (Exception ex) {
								LogHandler.error(ex);
								ex.printStackTrace();
							}
						}
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
}
