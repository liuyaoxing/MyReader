package offline.export.pictureViewer;

import java.awt.Image;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileFilter;

import offline.export.utils.ComparatorFactory;

public class ViewerService {

	private static ViewerService service = null;

	private ViewerFileChooser fileChooser = new ViewerFileChooser();

	private List<File> currentFiles = new ArrayList<File>();

	private File currentFile = null;

	/** 图片放大的比例 */
	protected final float ZOOMIN_RATE = 1.1f;

	/** 图片放大的缩小 */
	protected final float ZOOMOUT_RATE = 0.9f;

	protected double zoomScale = 1.0D;

	protected boolean zoomFit = true;

	private Timer timer;

	/** 是否暂停播放 */
	protected boolean playPaused = false;
	
	/** 是否窗口抖動 */
	protected boolean windowShake = false;

	/** 自动播放间隔时间 */
	protected double playTimerPeriod = 0;

	private ViewerService() {
	}

	public static ViewerService getInstance() {
		if (service == null) {
			service = new ViewerService();
		}
		return service;
	}

	public File open(ViewerFrame frame) {
//		try {
//			if (!System.getProperty("os.name").contains("Windows"))
//				UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
//		} catch (Throwable ex) {
//		}
//		try {
			return open0(frame);
//		}catch(Exception ex) {
//			ex.printStackTrace();
//			open0(frame);
//		} finally {
//			try {
//				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
//			} catch (Exception e) {
//			}
//		}
	}
	
	File open0(ViewerFrame frame) {
		try {
			if (fileChooser.showOpenDialog(frame) == ViewerFileChooser.APPROVE_OPTION) {
				this.currentFile = fileChooser.getSelectedFile();
				open(frame, this.currentFile);
			} else {
				return null;
			}
		}catch(Exception ex) {
			ex.printStackTrace();
		}
		return this.currentFile;
	}
	
	public void open(ViewerFrame frame, File srcFile) {
		this.currentFile = srcFile;
		currentFiles.clear();
		// 如果文件夹有改变
		// 或者fileChooser的所有FileFilter
		FileFilter[] fileFilters = fileChooser.getChoosableFileFilters();
		File[] files = srcFile.getParentFile().listFiles();
		Arrays.sort(files, new ComparatorFactory.WindowsExplorerComparator());
		for (File file : files) {
			for (FileFilter filter : fileFilters) {
				// 如果是图片文件
				if (file.isFile() && filter.accept(file) && !currentFiles.contains(file)) {
					// 把文件加到currentFiles中
					this.currentFiles.add(file);
				}
			}
		}
		setImageFile(frame, this.currentFile);
	}
	
	public void open(ViewerFrame frame, File[] allFiles, File currentFile) {
		this.currentFile = currentFile;
		this.currentFiles = Arrays.asList(allFiles);
		setImageFile(frame, currentFile);
	}

	public void doPrevious(ViewerFrame frame) {
		// 如果有打开包含图片的文件夹
		if (this.currentFiles != null && !this.currentFiles.isEmpty()) {
			int index = currentFiles.indexOf(this.currentFile);

			this.currentFile = (File) this.currentFiles.get(index == 0 ? currentFiles.size() - 1 : index - 1);

			setImageFile(frame, this.currentFile);
		}
	}

	public void doNext(ViewerFrame frame) {
		// 如果有打开包含图片的文件夹
		if (this.currentFiles != null && !this.currentFiles.isEmpty()) {
			int index = currentFiles.indexOf(this.currentFile);

			this.currentFile = this.currentFiles.get(index >= currentFiles.size() - 1 ? 0 : index + 1);

			setImageFile(frame, this.currentFile);
		}
	}

	public void menuDo(ViewerFrame frame, String cmd) {
		// 打开
		if (cmd.equals(ViewerFrame.MENU_OPENFILE)) {
			open(frame);
		}
		if (cmd.equals(ViewerFrame.MENU_SET_ALWAYS_ONTOP)) {
			frame.setAlwaysOnTop(!frame.isAlwaysOnTop());
		}
		if (cmd.equals(ViewerFrame.WINDOW_SHAKE)) {
			windowShake = !windowShake;
		}
		if (currentFile == null)
			return;
		// 放大
		if (cmd.equals(ViewerFrame.MENU_ZOOM_OUT)) {
			doZoomOut(frame);
		}
		// 缩小
		if (cmd.equals(ViewerFrame.MENU_ZOOM_IN)) {
			doZoomIn(frame);
		}
		// 自适应
		if (cmd.equals(ViewerFrame.MENU_ZOOM_FIX)) {
			doZoomFit(frame);
		}
		// 上一个
		if (cmd.equals(ViewerFrame.MENU_PREVIOUS)) {
			doPrevious(frame);
		}
		// 下一个b
		if (cmd.equals(ViewerFrame.MENU_NEXT)) {
			doNext(frame);
		}
		if (cmd.equals(ViewerFrame.MENU_PLAY1_0S)) {
			doAutoPlay(frame, 1);
		}
		if (cmd.equals(ViewerFrame.MENU_PLAY0_8S)) {
			doAutoPlay(frame, 0.8);
		}
		if (cmd.equals(ViewerFrame.MENU_PLAY0_5S)) {
			doAutoPlay(frame, 0.5);
		}
		if (cmd.equals(ViewerFrame.MENU_PLAYCUSTOMS)) {
			String m = JOptionPane.showInputDialog("请输入轮播间隔,单位秒");
			try {
				double period = Double.parseDouble(m);
				doAutoPlay(frame, period);
			} catch (Exception e) {
			}
		}
		if (cmd.equals(ViewerFrame.MENU_PLAY_PAUSE)) {
			playPaused = !playPaused;
			refreshTitle(frame);
		}

		if (cmd.equals(ViewerFrame.MENU_PLAY_STOP)) {
			playPaused = true;
			playTimerPeriod = 0;
			if (timer != null) {
				timer.cancel();
				timer = null;
			}
			refreshTitle(frame);
		}

		// 退出
		if (cmd.equals(ViewerFrame.MENU_EXIT)) {
			System.exit(0);
		}
	}

	public void setImageFile(ViewerFrame frame, File imageFile) {
		this.currentFile = imageFile;
		refreshTitle(frame);
		if (zoomFit) {
			doZoomFit(frame);
		} else {
			onZoom(frame);
		}
	}

	void refreshTitle(ViewerFrame frame) {
		StringBuffer sb = new StringBuffer();
		sb.append(currentFile.getName() + String.format(" [%s - %s]", currentFiles.size(), currentFiles.indexOf(currentFile) + 1));
		if (timer != null) {
			sb.append(playPaused ? " 播放暂停中" : String.format(" 自动播放每%s秒", playTimerPeriod));
		}
		frame.setTitle(sb.toString());
	}

	public File getImageFile() {
		return this.currentFile;
	}

	public void doZoomIn(ViewerFrame frame) {
		if (this.zoomScale < 1.0D) {
			this.zoomScale *= ZOOMIN_RATE;
		} else {
			this.zoomScale += 0.5D;
			if (this.zoomScale > 4.0D) {
				this.zoomScale = 4.0D;
			}
		}
		this.zoomFit = false;
		onZoom(frame);
	}

	public void doZoomOut(ViewerFrame frame) {
		if (this.zoomScale <= 1.0D) {
			if (this.zoomScale > 0.001D)
				this.zoomScale *= ZOOMOUT_RATE;
		} else {
			this.zoomScale -= 0.5D;
		}
		zoomScale = Math.max(zoomScale, 0.2);
		this.zoomFit = false;
		onZoom(frame);
	}

	public void doZoomOriginal(ViewerFrame frame) {
		this.zoomScale = 1.0D;
		this.zoomFit = false;
		onZoom(frame);
	}

	public void onZoom(ViewerFrame frame) {
		ImageIcon icon = new ImageIcon(currentFile.getPath());
		int width = (int) (icon.getIconWidth() * zoomScale);
		int height = (int) ((icon.getIconHeight()) * zoomScale);
		// 获取改变大小后的图片
		ImageIcon newIcon = new ImageIcon(icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH));
		// 改变显示的图片
		frame.getLabel().setIcon(newIcon);
		frame.getLabel().repaint();
	}

	/**
	 * 让图片自适应窗口显示 。
	 */
	public void doZoomFit(ViewerFrame frame) {
		ImageIcon icon = new ImageIcon(currentFile.getPath());

		double bitWidth1 = (double) frame.getLabel().getVisibleRect().getWidth() / icon.getIconWidth();
		double bitHeight1 = (double) frame.getLabel().getVisibleRect().getHeight() / icon.getIconHeight();
		if (bitWidth1 > bitHeight1) {
			zoomScale = bitHeight1;
		} else {
			zoomScale = bitWidth1;
		}
		if (zoomScale < 0.001D) {
			zoomScale = 0.001D;
		}
		onZoom(frame);
	}

	public void doAutoPlay(final ViewerFrame frame, double period) {
		this.playTimerPeriod = period;
		refreshTitle(frame);
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				if (!playPaused)
					doNext(frame);
			}
		}, 2000, (long) (period * 1000));
	}
}