package offline.export.module;

import java.awt.Desktop;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.liuyx.common.csv.CsvUtil;

import offline.export.DownloadUtil;
import offline.export.DownloadUtil.OnDownloadListener;
import offline.export.log.LogHandler;
import offline.export.utils.EventDispatcher;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 任务中心面板
 * 
 * @author liuyaoxing
 */
public class TaskListJPanel extends TaskListJPanelUI {

	/** 序列号 */
	private static final long serialVersionUID = 6597532229772678135L;

	protected String inputHostUrl;

	public TaskListJPanel(JFrame frame) {
		addListeners();
	}

	private void addListeners() {
		taskListTable.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent me) {
				if (SwingUtilities.isRightMouseButton(me)) {
					final JPopupMenu popup = new JPopupMenu();
					JMenuItem clearItem = new JMenuItem("清空");
					popup.add(clearItem);
					clearItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							taskListTableModel.setRowCount(0);
						}
					});
					final int row = taskListTable.rowAtPoint(me.getPoint());
					if (row != -1) {
						taskListTable.setRowSelectionInterval(row, row);
						SwingUtilities.invokeLater(() -> taskListTable.repaint());
						int column = taskListTable.getColumnModel().getColumnIndex(KEY_FILEPATH);
						final File srcFile = new File(String.valueOf(taskListTable.getValueAt(row, column)));
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
						JMenuItem openDirFile = new JMenuItem("打开本地目录");
						popup.add(openDirFile);
						openDirFile.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								try {
									if (srcFile.getParentFile().exists()) {
										Desktop.getDesktop().open(srcFile.getParentFile());
									} else if (currentDirectory != null && currentDirectory.exists()) {
										Desktop.getDesktop().open(currentDirectory);
									}
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							}
						});
					}
					popup.show(me.getComponent(), me.getX(), me.getY());
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
	}

	private void doDownloadFolder(final String fileUrl, final File toFile) {
		try {
			EventDispatcher.dispatchMessage(PROP_GLASSPANE_START, fileUrl, null);// 开始动画加载效果

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
			EventDispatcher.dispatchMessage(PROP_GLASSPANE_STOP, fileUrl, "");// 停止动画加载效果
			counter.set(0);
			total.set(jsonArray.size());
			doSyncFolder(jsonArray, toFile);
		} catch (Exception ex) {
			LogHandler.error(ex);
			JOptionPane.showMessageDialog(null, "下载失败:" + ex.getMessage());
			EventDispatcher.dispatchMessage(PROP_GLASSPANE_STOP, fileUrl, null);// 开始动画加载效果
		}
	}

	protected void doSyncFolder(List<Map<String, Object>> jsonArray, File toFile) throws IOException {
		String inputHostUrl = getInputHostUrl();
		if (inputHostUrl == null || inputHostUrl.isEmpty()) {
			JOptionPane.showMessageDialog(null, "请输入正确的服务器地址！");
			return;
		}
		EventDispatcher.dispatchMessage(PROP_TABBEDPANE_SELECTED_NAME, TAB_TASKLIST, null);

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

			taskListTableModel.addRow(new Object[]{taskListTableModel.getRowCount() + 1, id, title, url, size, "", absPath});
		}

		total.set(taskListTableModel.getRowCount());

		for (int row = 0; row < taskListTableModel.getRowCount(); row++) {
			final String id = (String) taskListTableModel.getValueAt(row, taskListTable.getColumnModel().getColumnIndex(KEY_FILEID));
			final String title = (String) taskListTableModel.getValueAt(row, taskListTable.getColumnModel().getColumnIndex(KEY_FILENAME));
			final String url = (String) taskListTableModel.getValueAt(row, taskListTable.getColumnModel().getColumnIndex(KEY_URL));
			final String size = (String) taskListTableModel.getValueAt(row, taskListTable.getColumnModel().getColumnIndex(KEY_LENGTH));
			final String absPath = (String) taskListTableModel.getValueAt(row, taskListTable.getColumnModel().getColumnIndex(KEY_FILEPATH));

			taskListTable.setRowSelectionInterval(row, row);
			taskListTable.scrollRectToVisible(new Rectangle(taskListTable.getCellRect(row + 10, 0, true)));
			SwingUtilities.invokeLater(() -> taskListTable.repaint());

			int col = taskListTable.getColumnModel().getColumnIndex(KEY_STATUS);

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
					taskListTableModel.setValueAt("文件已存在!" + destFile.getCanonicalPath(), row, taskListTable.getColumnModel().getColumnIndex(KEY_STATUS));
					taskListTableModel.setValueAt(destFile.getCanonicalFile(), row, taskListTable.getColumnModel().getColumnIndex(KEY_FILEPATH));
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
				// frame.setTitle(String.format("%s (已处理: %s/%s项)", TITLE,
				// counter.incrementAndGet(), total.get()));
				dispatchMessage(PROP_SET_WINDOW_TITLE, String.format("%s (已处理: %s/%s项)", TITLE, counter.incrementAndGet(), total.get()), "");
			}
		}
	}

	public String getInputHostUrl() {
		return inputHostUrl;
	}

	protected String downloadFile(final String id, final int row, final int col, File destFile) throws IOException {
		String getUrl = getInputHostUrl() + FOLDER_DOWNLOAD_MD5 + id;
		File tmpFile = new File(destFile.getParent(), destFile.getName() + ".dulele");
		DownloadUtil.get().download(getUrl, id, tmpFile, new OnDownloadListener() {
			@Override
			public void onDownloadSuccess(File file) {
				try {
					taskListTableModel.setValueAt("100%", row, taskListTable.getColumnModel().getColumnIndex(KEY_STATUS));
					taskListTableModel.setValueAt(destFile.getCanonicalFile(), row, taskListTable.getColumnModel().getColumnIndex(KEY_FILEPATH));
					Files.move(file.toPath(), destFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				} catch (Exception ex) {
					LogHandler.error(ex);
					ex.printStackTrace();
				}
			}

			@Override
			public void onDownloading(int progress) {
				taskListTableModel.setValueAt(progress + "%", row, taskListTable.getColumnModel().getColumnIndex(KEY_STATUS));
			}

			@Override
			public void onDownloadFailed(Exception e) {
				taskListTableModel.setValueAt("0%", row, taskListTable.getColumnModel().getColumnIndex(KEY_STATUS));
				taskListTableModel.setValueAt("下载失败" + e.getMessage(), row, taskListTable.getColumnModel().getColumnIndex(KEY_FILEPATH));
				try {
					if (e instanceof FileNotFoundException)
						return;
					Thread.sleep(666);
					downloadFile(id, row, col, destFile);
				} catch (Exception ex) {
					LogHandler.debug("下载失败:" + ex.getMessage());
					ex.printStackTrace();
				}
			}

			@Override
			public boolean isFileExists(File srcFile) {
				return srcFile.exists() && srcFile.length() > 0;
			}

			@Override
			public void onFileExists(File file) {
				try {
					taskListTableModel.setValueAt("文件已存在!" + file.getCanonicalPath(), row, taskListTable.getColumnModel().getColumnIndex(KEY_STATUS));
					taskListTableModel.setValueAt(file.getPath(), row, taskListTable.getColumnModel().getColumnIndex(KEY_FILEPATH));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		return getUrl;
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String propertyName = event.getPropertyName();
		if (PROP_SET_ROW_COUNT.equals(propertyName)) {
			if (TAB_TASKLIST.equalsIgnoreCase((String) event.getOldValue()))
				taskListTableModel.setRowCount((int) event.getNewValue());
		}
		if (PROP_TASLKIST_TEXT.equals(propertyName)) {
			taskListTitle.setText((String) event.getNewValue());
		}
		if (PROP_TASKLIST_ADD.equals(propertyName)) {
			Map<String, String> element = CsvUtil.csvToMap((String) event.getNewValue());
			final String id = (String) element.get(FILESERVER_MD5);
			final String title = (String) element.get(FILESERVER_NAME);
			final String url = (String) element.get(FILESERVER_PATH);
			final String size = (String) element.get(FILESERVER_SIZE);
			final String absPath = (String) element.get(FILESERVER_ABSPATH);
			taskListTableModel.addRow(new Object[]{taskListTableModel.getRowCount() + 1, id, title, url, size, "", absPath});
		}
		if (PROP_DOWNLOAD_FOLDER.equals(propertyName)) {
			String[] commands = CsvUtil.csvToStringArray((String) event.getNewValue());
			this.inputHostUrl = commands[2];
			doDownloadFolder(commands[0], new File(commands[1]));
		}
		super.propertyChange(event);
	}
}
