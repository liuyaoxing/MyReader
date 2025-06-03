package offline.export.module;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.liuyx.common.csv.CsvUtil;

import offline.export.DownloadUtil;
import offline.export.DownloadUtil.OnDownloadListener;
import offline.export.FileUtils;
import offline.export.db.BackupTask;
import offline.export.log.LogHandler;
import offline.export.utils.EventDispatcher;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class BackupJPanel extends BackupJPanelUI {

	/** 序列号 */
	private static final long serialVersionUID = 3088458191004290208L;

	ThreadPoolExecutor tastListExecutor = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());

	protected BackupTask backupTask;

	private JFrame frame;

	protected Thread startThread, startFolderThread;

	public BackupJPanel(JFrame frame) {
		this.frame = frame;

		createPopupMenu();
		addListeners();

		addItemsToCombo(urlCombo, new String[] { "http://192.168.43.1:61666" }, 0);
	}

	private void addListeners() {
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

		backupTable.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent me) {
				if (SwingUtilities.isRightMouseButton(me)) {
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
							int row = backupTable.rowAtPoint(me.getPoint());
							int column = backupTable.columnAtPoint(me.getPoint());
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
							int[] getSelectedRows = backupTable.getSelectedRows();
							if (getSelectedRows == null || getSelectedRows.length == 1) {
								final int row = backupTable.rowAtPoint(me.getPoint());
								backupTable.setRowSelectionInterval(row, row);
								getSelectedRows = new int[] { row };
							}

							if (getSelectedRows == null || getSelectedRows.length == 0)
								return;

							final JFileChooser fileChooser = new JFileChooser();// 文件选择器
							fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);// 设定只能选择到文件夹
							if (currentDirectory != null)
								fileChooser.setCurrentDirectory(currentDirectory);
							int state = fileChooser.showSaveDialog(null);// 此句是打开文件选择器界面的触发语句
							if (state != JFileChooser.APPROVE_OPTION)
								return;
							currentDirectory = fileChooser.getSelectedFile();// toFile为选择到的目录
							System.out.println("CurrentDirectory:" + currentDirectory);

							for (int row : getSelectedRows) {
								tastListExecutor.submit(new Runnable() {
									@Override
									public void run() {
										Object value = backupTable.getValueAt(row, 0);
										Object folderName = backupTable.getValueAt(row, 1);
										if (value != null && folderName != null) {
											String newUrl = getInputHostUrl() + FOLDER_LIST_MD5 + value;
//											taskListTitle.setText(folderName + ": " + newUrl);
											EventDispatcher.dispatchMessage(PROP_TASLKIST_TEXT, folderName + ": " + newUrl, "");
											String newFolderName = folderName.toString().replace("[文件夹]", "");
											doDownloadFolder(newUrl, new File(currentDirectory, newFolderName));
										}
									}
								});
							}
						}
					});
					popup.add(new JSeparator());
					JMenuItem cleanFolderItem = new JMenuItem("清空文件夹");
					popup.add(cleanFolderItem);
					cleanFolderItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							int row = backupTable.rowAtPoint(me.getPoint());
//								int column = backupTable.columnAtPoint(me.getPoint());
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

	protected void createPopupMenu() {
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
		JMenuItem refreshItem = new JMenuItem("刷新网址");
		popupMenu.add(refreshItem);
		refreshItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
//				startScanPorts();
				EventDispatcher.dispatchMessage(PROP_LANPORT_SCAN, BackupJPanel.class.getSimpleName(), "");
			}
		});
		urlCombo.setComponentPopupMenu(popupMenu);
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

//			List<Map<String, Object>> resList = database.dbQuery(backupTask.getTableName(), whereMap);
//			if (resList != null && resList.size() > 0) {
//				backupTableModel.insertRow(0, new Object[] { id, title, url, FileUtils.getFileSize(length), "文件已存在！" });
////				continue;
//			}
			backupTableModel.insertRow(0, new Object[] { id, title, url, FileUtils.getFileSize(length), "0%" });

			backupTable.scrollRectToVisible(backupTable.getCellRect(0, 0, true));
			backupTable.setRowSelectionInterval(0, 0);
			backupTable.setSelectionBackground(Color.LIGHT_GRAY);// 选中行设置背景色

			final int row = 0;
			int statusCol = backupTable.getColumnModel().getColumnIndex(KEY_STATUS);
			int pathCol = backupTable.getColumnModel().getColumnIndex(KEY_FILEPATH);
			final String getUrl = String.format("%s/dll/export/%s", getComboText(urlCombo), id);

			DownloadUtil.get().download(getUrl, id, "backup", new OnDownloadListener() {
				@Override
				public boolean isFileExists(File srcFile) {
					return srcFile.exists() && srcFile.length() > 0;
				}

				@Override
				public void onDownloadSuccess(File file) {
					try {
						backupTableModel.setValueAt("100%", row, statusCol);
						backupTableModel.setValueAt(file.getCanonicalFile(), row, pathCol);
//						backupTask.setId(id);
//						backupTask.setTitle(title);
//						backupTask.setUrl(url);
//						backupTask.setLength(String.valueOf(length));
//						database.dbInsert(backupTask);
					} catch (Exception ex) {
						LogHandler.error(ex);
						ex.printStackTrace();
					}
				}

				@Override
				public void onDownloading(int progress) {
					backupTableModel.setValueAt(progress + "%", row, statusCol);
				}

				@Override
				public void onDownloadFailed(Exception e) {
					backupTableModel.setValueAt("0%", row, statusCol);
					backupTableModel.setValueAt("下载失败" + e.getMessage(), row, pathCol);
				}

				@Override
				public void onFileExists(File file) {
					backupTableModel.setValueAt("文件已存在:", row, statusCol);
					backupTableModel.setValueAt(file.getPath(), row, pathCol);
				}
			});

			if (backupTableModel.getDataVector().size() > 5000) {
				backupTableModel.setRowCount(0);
			}
			frame.setTitle(String.format("%s (已处理: %s项)", TITLE, counter.incrementAndGet()));
		}
		return jsonArray.size() > 0;
	}

	protected void doStartBackup(final JButton backupBtn) {
		LogHandler.debug("开始备份...");
		backupTask = new BackupTask();
		try {
//			database = new DataBaseProxy();
			frame.getTitle();
//			int res = database.dbCreate(backupTask);
//			System.out.println(res);

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
						counter.set(0);
						total.set(0);
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

	protected String getInputHostUrl() {
		return getInputHostUrl(urlCombo);
	}

	public void refreshServerIp() {
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

	private void doDownloadFolder(final String fileUrl, final File toFile) {
		try {
			EventDispatcher.dispatchMessage(PROP_DOWNLOAD_FOLDER,
					CsvUtil.stringArrayToCsv(new String[] { fileUrl, toFile.getCanonicalPath(), getInputHostUrl() }), "");
		} catch (IOException ex) {
			LogHandler.debug("下载文件夹失败:" + ex.getMessage());
		}
	}

	Set<String> itemSet = new HashSet<String>();

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String propertyName = event.getPropertyName();
		if (PROP_LANPORT_SCAN_IP.equals(propertyName)) {
			itemSet.add((String) event.getNewValue());
			addItemsToCombo(urlCombo, itemSet.toArray(new String[0]), 0);
		}
		super.propertyChange(event);
	}
}
