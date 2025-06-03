package offline.export.module;

import java.awt.Desktop;
import java.awt.Rectangle;
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
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import offline.export.FileUtils;
import offline.export.log.LogHandler;
import offline.export.utils.NetworkUtils;
import offline.export.utils.ProgressRequestBody;
import offline.export.utils.ProgressRequestListener;
import okhttp3.Call;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class UploadJPanel extends UploadJPanelUI {

	/** 序列号 */
	private static final long serialVersionUID = -5952898543215887697L;

	protected JFrame frame;

	ThreadPoolExecutor tastListExecutor = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());

	OkHttpClient uploadOkHttpClient = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS).writeTimeout(30, TimeUnit.SECONDS)
			.readTimeout(30, TimeUnit.SECONDS).build();

	private AtomicInteger uploadCount = new AtomicInteger(0);

	public UploadJPanel(JFrame frame) {
		this.frame = frame;

		createPopupMenu();
		addListeners();
		initDnd();

		addItemsToCombo(urlCombo2, new String[] { "http://192.168.43.1:61666" + FOLDER_UPLOAD }, 0);
	}

	protected void createPopupMenu() {
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
					try {
						Object td = transfer.getTransferData(DataFlavor.javaFileListFlavor);
						tastListExecutor.submit(new Runnable() {
							@Override
							public void run() {
								uploadTableModel.setRowCount(0);
								uploadCount.set(0);
								try {
									if (td instanceof List) {
										List<?> tdFileList = (List<?>) td;
										File baseDir = null;
										List<File> fileList = new ArrayList<>();
										for (Object value : tdFileList) {
											if (!(value instanceof File))
												continue;
											File srcFile = (File) value;
											if (srcFile.isDirectory()) {
												File[] allFiles = FileUtils.listFiles(srcFile).toArray(new File[0]);
												for (int i = 0; i < allFiles.length; i++) {
													addToUploadTable(allFiles[i]);
													fileList.add(allFiles[i]);
												}
											} else {
												addToUploadTable(srcFile);
												fileList.add(srcFile);
											}
											baseDir = srcFile.getParentFile();
										}

										if (tdFileList.size() == 1 && tdFileList.get(0) instanceof File && ((File) tdFileList.get(0)).isDirectory())
											baseDir = (File) tdFileList.get(0);

										doUploadFolder(baseDir, fileList.toArray(new File[0]));
									}
								} catch (Exception ex) {
									ex.printStackTrace();
								}
							}
						});
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		});
	}

	private void addListeners() {
		uploadFolderBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser();
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int v = chooser.showOpenDialog(null);
				if (v == JFileChooser.APPROVE_OPTION) {
					File currentUploadFolder = chooser.getSelectedFile();
					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								File[] allFiles = doPreUploadFolder(currentUploadFolder);
								doUploadFolder(currentUploadFolder, allFiles);
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					}).start();
				}
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
//					itemSet.add(String.valueOf(newUrl + FOLDER_UPLOAD + FLAG_DELETE_ON_SUCCESS));
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

						JMenuItem deleteFileItem = new JMenuItem("删除本地文件");
						popup.add(deleteFileItem);
						deleteFileItem.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								int column = uploadTable.getColumnModel().getColumnIndex(KEY_FILEPATH);
								Object value = uploadTable.getValueAt(row, column);
								if (value != null && new File(value.toString()).exists()) {
									String msg = String.format("确认是否要删除本地文件: %s?", value);
									int opt = JOptionPane.showConfirmDialog(null, msg, "确认删除", JOptionPane.YES_NO_OPTION);
									if (opt == JOptionPane.YES_OPTION) {
										if (new File(value.toString()).delete()) {
											JOptionPane.showMessageDialog(null, "删除成功:" + value);
										} else {
											JOptionPane.showMessageDialog(null, "删除失败:" + value);
										}
									}
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
	}

	protected File[] doPreUploadFolder(File file) throws IOException {
		uploadTableModel.setRowCount(0);
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

	private void doUploadFolder(File baseDir, final File[] allFiles) throws IOException, InterruptedException {
		doUploadFolder(baseDir, allFiles, false);
	}

	private void doUploadFolder(File baseDir, final File[] allFiles, boolean onlyFile) throws IOException, InterruptedException {
		if (allFiles == null || allFiles.length == 0 || baseDir == null)
			return;

		String uploadUrl = getComboText(urlCombo2);

		uploadUrl += uploadUrl.endsWith(FOLDER_UPLOAD) ? "" : FOLDER_UPLOAD;

		if (!NetworkUtils.isNetworkAvailable(uploadUrl)) {
			JOptionPane.showMessageDialog(null, "无法连接到服务地址: " + uploadUrl);
			Thread.sleep(15000);
			doUploadFolder(baseDir, allFiles);
			return;
		}

		total.set(allFiles.length);
		counter.set(0);

		final int updateColumn = uploadTable.getColumnModel().getColumnIndex(COL_PROGRESS);

		final ExecutorService es = Executors.newFixedThreadPool(1);

		for (int i = 0; i < allFiles.length; i++) {
			if (allFiles[i].isHidden())
				continue;
			boolean deleteOnSuccess = deleteFileAfterUploadBtn.isSelected();
			final String uploadUrl0 = uploadUrl;
			String folderPath = allFiles[i].getParentFile().getCanonicalPath().substring(baseDir.getParentFile().getCanonicalPath().length());
			if (folderPath.startsWith(File.separator))
				folderPath = folderPath.substring(File.separator.length());
			final int index = i;
			final String finalFolderPath = folderPath;
			es.submit(new Runnable() {
				@Override
				public void run() {
					try {
						boolean result = uploadFile(finalFolderPath, uploadOkHttpClient, uploadUrl0, allFiles[index], updateColumn, index, deleteOnSuccess);
						if (!result)
							es.submit(this);
					} catch (Exception e) {
						e.printStackTrace();
						es.submit(this);
					}
				}
			});
		}
	}

	protected boolean uploadFile(String path, OkHttpClient mOkHttpClient, String uploadUrl, File subFile, final int updateCol, final int currentRow)
			throws IOException {
		return uploadFile(path, mOkHttpClient, uploadUrl, subFile, updateCol, currentRow, false);
	}

	protected boolean uploadFile(String folderPath, OkHttpClient mOkHttpClient, String uploadUrl, final File subFile, final int updateCol,
			final int currentRow, final boolean deleteOnSuccess) throws IOException {
		return uploadFile(folderPath, mOkHttpClient, uploadUrl, subFile, updateCol, currentRow, deleteOnSuccess, false);
	}

	protected boolean uploadFile(String folderPath, OkHttpClient mOkHttpClient, String uploadUrl, final File subFile, final int updateCol,
			final int currentRow, final boolean deleteOnSuccess, boolean isAsync) throws IOException {
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
				.url(uploadUrl + "?path=" + URLEncoder.encode(folderPath, "utf-8")//
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

	Set<String> itemSet = new HashSet<String>();

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String propertyName = event.getPropertyName();
		if (PROP_LANPORT_SCAN_IP.equals(propertyName)) {
			itemSet.add((String) event.getNewValue() + FOLDER_UPLOAD);
			addItemsToCombo(urlCombo2, itemSet.toArray(new String[0]), 0);
		}
		super.propertyChange(event);
	}
}
