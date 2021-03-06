package offline.export;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableModel;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import offline.export.DownloadUtil.OnDownloadListener;
import offline.export.db.BackupTask;
import offline.export.db.DataBaseProxy;
import okhttp3.Request;
import okhttp3.Response;

public class OfflineExport {

	private JFrame frame;
	private JTextField urlTxt;
	private JTable table;
	private DefaultTableModel tableModel;

	private String[] columnNames = new String[] { "ID", "标题", "URL", "大小", "状态", "保存路径" };
	private int[] columnWidths = new int[] { 50, 250, 50, 50, 50, 250 };
	protected BackupTask backupTask;
	protected DataBaseProxy database;

	protected Thread startThread;

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
				} catch (Exception e) {
					e.printStackTrace();
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

		urlTxt = new JTextField();
		urlTxt.setToolTipText("请输入读乐乐服务URL");
		panel.add(urlTxt);
		urlTxt.setColumns(10);

		JButton backupBtn = new JButton("开始备份");
		backupBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				backupTask = new BackupTask();
				database = new DataBaseProxy();
				try {
					int res = database.dbCreate(backupTask);
					System.out.println(res);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				if (startThread != null) {
					startThread.interrupt();
					startThread = null;
					return;
				}
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
								ex.printStackTrace();
							}
						}
					}
				});
				startThread.start();
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

	protected boolean doBackFiles(int page) throws IOException, SQLException {
		urlTxt.setText(urlTxt.getText().replace("//", "/").replace("：", ":"));
		String backupListUrl = String.format("%s/dll/export/list", urlTxt.getText());
		Request request = new Request.Builder().addHeader("x-header", "dll")//
				.header("page", String.valueOf(page))//
				.header("size", String.valueOf(5))//
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
			JsonObject element = jsonArray.get(i).getAsJsonObject();
			System.out.println("开始执行:" + element);
			final String id = element.get("_id").getAsString();
			final String title = element.get("title").getAsString();
			final String url = element.get("url").getAsString();
			final long length = element.get("foldersize").getAsLong();

			whereMap.put(BackupTask.KEY_ID, id);

			List<Map<String, Object>> resList = database.dbQuery(backupTask.getTableName(), whereMap);
			if (resList != null && resList.size() > 0)
				continue;

			tableModel.insertRow(0, new Object[] { id, title, url, FileUtils.getFileSize(length), "0%" });
			table.scrollRectToVisible(table.getCellRect(0, 0, true));
			table.setRowSelectionInterval(0, 0);
			table.setSelectionBackground(Color.LIGHT_GRAY);// 选中行设置背景色

			final int row = 0, col = tableModel.getColumnCount() - 1;
			final String getUrl = String.format("%s/dll/export/%s", urlTxt.getText(), id);

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
		return jsonArray.size() > 0;
	}
}
