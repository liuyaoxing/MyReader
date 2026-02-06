package offline.export.module;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URI;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import com.liuyx.common.db.dao.Mr_FileServer;
import com.sun.net.httpserver.HttpServer;

import db.FileUtils;
import offline.export.db.BackupTask;
import offline.export.dialog.QRCodeDialog;
import offline.export.log.LogHandler;
import offline.export.module.httpserver.FileServerPreferences;
import offline.export.module.httpserver.WebServerHandler;
import offline.export.utils.DateUtils;
import offline.export.utils.MD5Utils;

/**
 * 手机备份面板
 * 
 * @author liuyaoxing
 */
public class FileServerJPanel extends FileServerJPanelUI {

	/** 序列号 */
	private static final long serialVersionUID = 3088458191004290208L;

	ThreadPoolExecutor tastListExecutor = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>());

	protected BackupTask backupTask;

	private HttpServer server;

	private JFrame frame;

	public FileServerJPanel(JFrame frame) {
		this.frame = frame;
		createPopupMenu();
		addListeners();

		addItemsToCombo(ipCombo, new String[]{getHostIP(), "127.0.0.1"}, 0);
		refreshFileServerTable();
	}

	private void addListeners() {
		startStopBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (server != null) {
					server.stop(5);
					server = null;
					startStopBtn.setText("开启服务");
					startStopBtn.setForeground(Color.BLACK);
				} else {
					try {
						int port = parseInt(portTxt.getText().trim(), 61666);
						server = HttpServer.create(new InetSocketAddress(port), 0);
					} catch (Throwable ex) {
						LogHandler.error(ex);
					}
					// 使用自定义处理器处理所有请求
					server.createContext("/", new WebServerHandler());

					server.setExecutor(null); // 使用默认 executor
					server.start();

					// 获取绑定的地址信息
					InetSocketAddress address = server.getAddress();

					// 可能是 "0.0.0.0"或具体 IP
					int port = address.getPort(); // 8080
					setComboBox(ipCombo, getHostIP());
					portTxt.setText(port + "");
					System.out.println("Server started on port 8080");
					startStopBtn.setText("停止服务");
					startStopBtn.setForeground(Color.RED);
				}
			}
		});

		addFileBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				// FileDialog.LOAD 表示打开文件，FileDialog.SAVE 表示保存文件
				FileDialog fileDialog = new FileDialog(frame, "请选择文件", FileDialog.LOAD);
				fileDialog.setMultipleMode(true);
				fileDialog.setVisible(true);
				File[] selectedFiles = fileDialog.getFiles();
				if (selectedFiles != null && selectedFiles.length > 0) {
					System.out.println("您选择了 " + selectedFiles.length + " 个文件：");
					for (File filePath : selectedFiles) {
						try {
							Mr_FileServer fileServer = newFileServer(filePath);
							FileServerPreferences.addFileServer(fileServer);
						} catch (Throwable ex) {
							LogHandler.debug("添加文件失败:" + ex.getMessage());
							LogHandler.error(ex);
						}
					}
				} else {
					System.out.println("未选择任何文件或已取消。");
				}
			}
		});

		fileServerTable.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent me) {
				if (SwingUtilities.isRightMouseButton(me)) {
					final JPopupMenu popup = new JPopupMenu();

					JMenuItem copyItem = new JMenuItem("复制");
					popup.add(copyItem);
					copyItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							int row = fileServerTable.rowAtPoint(me.getPoint());
							int column = fileServerTable.columnAtPoint(me.getPoint());
							Object value = fileServerTable.getValueAt(row, column);
							if (value != null)
								setSysClipboardText(String.valueOf(value));
						}
					});
					popup.add(new JSeparator());
					JMenuItem syncFolderItem = new JMenuItem("链接二维码");
					popup.add(syncFolderItem);
					syncFolderItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							int row = fileServerTable.rowAtPoint(me.getPoint());
							String fileUrl = getTableValueAt(fileServerTable, row, KEY_URL);
							QRCodeDialog dialog = new QRCodeDialog(frame, fileUrl, "扫码打开", fileUrl);
							dialog.setVisible(true);
						}
					});
					popup.add(new JSeparator());
					JMenuItem removeItem = new JMenuItem("移除共享");
					popup.add(removeItem);
					removeItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							int opt = JOptionPane.showConfirmDialog(null, "是否要清空本地文件服务器共享列表?", "确认清空", JOptionPane.YES_NO_OPTION);
							if (opt == JOptionPane.YES_OPTION) {
								FileServerPreferences.clearFileServers();
								refreshFileServerTable();
							}
						}
					});
					JMenuItem cleanFolderItem = new JMenuItem("清空共享");
					popup.add(cleanFolderItem);
					cleanFolderItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							int opt = JOptionPane.showConfirmDialog(null, "是否要清空本地文件服务器共享列表?", "确认清空", JOptionPane.YES_NO_OPTION);
							if (opt == JOptionPane.YES_OPTION) {
								FileServerPreferences.clearFileServers();
								refreshFileServerTable();
							}
						}
					});

					JMenuItem calcel = new JMenuItem("取消");
					calcel.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							popup.setVisible(false);
						}
					});
					JMenuItem clearItem = new JMenuItem("清空");
					popup.add(clearItem);
					clearItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							fileServerTableModel.setRowCount(0);
						}
					});
					popup.add(new JSeparator());
					popup.add(calcel);
					popup.show(me.getComponent(), me.getX(), me.getY());
				}
			}
		});

		serverInfoBtn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String fileUrl = getInputHostUrl();
				QRCodeDialog dialog = new QRCodeDialog(frame, fileUrl, "扫码打开", fileUrl);
				dialog.setVisible(true);
			}
		});
	}

	protected void createPopupMenu() {
		JPopupMenu popupMenu = new JPopupMenu();
		JMenuItem copyItem = new JMenuItem("复制");
		popupMenu.add(copyItem);
		copyItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String selectedItem = (String) ipCombo.getSelectedItem();
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
				pasteToCombo(ipCombo);
			}
		});
		JMenuItem openUrlItem = new JMenuItem("打开网址");
		popupMenu.add(openUrlItem);
		openUrlItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String hostUrl = getInputHostUrl(ipCombo);
				try {
					Desktop.getDesktop().browse(URI.create(hostUrl));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		ipCombo.setComponentPopupMenu(popupMenu);
	}

	protected String getInputHostUrl(JComboBox<String> urlCombo) {
		try {
			String newItem = String.valueOf(urlCombo.getEditor().getItem());
			return "http://" + String.format("%s:%s", newItem, portTxt.getText().trim());
		} catch (Exception ex) {
			return null;
		}
	}

	protected String getInputHostUrl() {
		return getInputHostUrl(ipCombo);
	}

	private void refreshFileServerTable() {
		fileServerTableModel.setRowCount(0);
		List<Mr_FileServer> fileServers = FileServerPreferences.queryUploadFiles();
		for (int i = 0; i < fileServers.size(); i++) {
			Mr_FileServer fileServer = fileServers.get(i);
			final String path = fileServer.getLocation();
			if (!new File(path).exists())
				continue;
			// KEY_ID, KEY_FILENAME, KEY_FILEPATH, KEY_LENGTH, KEY_URL, "个 数"
			final int index = fileServers.size() - i;
			final String id = fileServer.getFileMD5();
			final String title = fileServer.getTitle();
			final String url = String.format("http://%s:%s/files/%s", getComboText(ipCombo), portTxt.getText(), id);
			final String size = FileUtils.formatFileSize(fileServer.getFolderSize());
			final int fileList = 0;
			fileServerTableModel.insertRow(0, new Object[]{index, title, path, size, url, fileList});
		}
	}

	// 获取本机非回环、非虚拟网卡的 IP 地址
	private static String getHostIP() {
		try {
			Enumeration<NetworkInterface> allNetInterfaces = NetworkInterface.getNetworkInterfaces();
			while (allNetInterfaces.hasMoreElements()) {
				NetworkInterface netInterface = allNetInterfaces.nextElement();

				// 跳过虚拟网卡和未启用的网卡
				if (netInterface.isLoopback() || netInterface.isVirtual() || !netInterface.isUp())
					continue;

				Enumeration<InetAddress> addresses = netInterface.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress addr = addresses.nextElement();
					if (addr instanceof java.net.Inet4Address) { // 优先获取 IPv4
						String ip = addr.getHostAddress();
						// 排除 docker 或其他内网保留地址（根据需要调整）
						if (ip.startsWith("192.168") || ip.startsWith("10.") || ip.startsWith("172.")) {
							return ip;
						}
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return "127.0.0.1"; // 默认回退
	}

	Set<String> itemSet = new HashSet<String>();

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		super.propertyChange(event);
	}

	private Mr_FileServer newFileServer(File srcFile) throws IOException {
		Mr_FileServer fileServer = new Mr_FileServer();
		fileServer.setTitle(srcFile.getName());
		fileServer.setLocation(srcFile.getPath());
		// fileServer.setFileUri(stream);
		fileServer.setWebSrc("本机");
		fileServer.setContentType(Files.probeContentType(srcFile.toPath()));
		fileServer.setFolderSize(srcFile.length());
		fileServer.setFileMD5(MD5Utils.encryptFileFast(srcFile));
		fileServer.setUpdateTime(DateUtils.getCurrentTime());
		fileServer.setReqType(0);
		fileServer.setState(0);
		return fileServer;
	}
}
