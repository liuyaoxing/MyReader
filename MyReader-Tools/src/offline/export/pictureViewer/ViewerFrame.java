package offline.export.pictureViewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetAdapter;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileFilter;

public class ViewerFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	public static final String MENU_PLAY1_0S = "播放(1s)";

	public static final String MENU_PLAY0_8S = "播放(0.8s)";

	public static final String MENU_PLAY0_5S = "播放(0.5s)";

	public static final String MENU_PLAYCUSTOMS = "播放(自定义)";

	public static final String MENU_PLAY_PAUSE = "暂停播放";

	public static final String MENU_PLAY_STOP = "停止播放";

	public static final String MENU_SET_ALWAYS_ONTOP = "窗口置顶";

	public static final String MENU_PREVIOUS = "上一个";

	public static final String MENU_NEXT = "下一个";

	public static final String MENU_ZOOM_FIX = "自适应";

	public static final String MENU_ZOOM_OUT = "缩小";

	public static final String MENU_ZOOM_IN = "放大";

	public static final String MENU_EXIT = "退出";

	public static final String MENU_OPENFILE = "打开";

	public static final String MENUBAR_TOOLS = "查看";

	public static final String MENUBAR_FILE = "文件";

	// 设置读图区的宽和高
	private int width = 555, height = 555;

	// 用一个JLabel放置图片
	private JLabel photoLabel;

	ViewerService service = ViewerService.getInstance();

	private ViewerFileChooser fileChooser = new ViewerFileChooser();

	ActionListener menuListener = new ActionListener() {
		public void actionPerformed(ActionEvent e) {
			service.menuDo(ViewerFrame.this, e.getActionCommand());
		}
	};

	public ViewerFrame() {
		super();
		// 初始化这个JFrame
		init();
		initDnd();

		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (service != null)
					service.playPaused = true;
			}
		});
	}

	public void init() {
		// 设置标题
		this.setTitle("读乐乐图片浏览器");
		// 设置图标
//		this.setIconImage(Toolkit.getDefaultToolkit().getImage("resource" + File.separator + "image" + File.separator + "frameIcon.jpg"));
		// 设置大小
		this.setPreferredSize(new Dimension(width, height));
		// 创建菜单
		createMenuBar();
		// 创建工具栏
		JPanel toolBar = createToolPanel();
		toolBar.setVisible(false);
		photoLabel = new JLabel();
		photoLabel.setFocusable(true);
		photoLabel.setHorizontalAlignment(JLabel.CENTER);
		photoLabel.setVerticalAlignment(JLabel.CENTER);
		photoLabel.setBorder(new EmptyBorder(0, 0, 0, 0));
		photoLabel.setIconTextGap(0);
		// 把工具栏和读图区加到JFrame里面
		this.add(toolBar, BorderLayout.NORTH);
		this.add(new JScrollPane(photoLabel), BorderLayout.CENTER);
		// 设置为可见
		this.setVisible(true);
		this.pack();

		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
			@Override
			public boolean dispatchKeyEvent(KeyEvent arg0) {
				if (arg0.getID() == KeyEvent.KEY_PRESSED) {
					switch (arg0.getKeyCode()) {
					case KeyEvent.VK_LEFT:
						service.doPrevious(ViewerFrame.this);
						break;
					case KeyEvent.VK_RIGHT:
						service.doNext(ViewerFrame.this);
						break;
					case KeyEvent.VK_SPACE:
						service.menuDo(ViewerFrame.this, ViewerFrame.MENU_PLAY_PAUSE);
						break;
					case KeyEvent.VK_ESCAPE:
						service.menuDo(ViewerFrame.this, ViewerFrame.MENU_PLAY_STOP);
						break;
					}
				}
				return false;
			}
		});

		addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				if (service.getImageFile() != null)
					service.setImageFile(ViewerFrame.this, service.getImageFile());
			}
		});

		photoLabel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					if (service != null) {
						service.playPaused = false;
						service.doAutoPlay(ViewerFrame.this, 1);
					}
				} else {
					if (service != null) {
						service.playPaused = !service.playPaused;
						service.refreshTitle(ViewerFrame.this);
					}
				}
			}
		});
		photoLabel.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if (service != null) {
					if (e.getKeyChar() == '=' || e.getKeyChar() == '+') {
						service.doNext(ViewerFrame.this);
					} else if (e.getKeyChar() == '-' || e.getKeyChar() == '_') {
						service.doPrevious(ViewerFrame.this);
					}
				}
			}
		});
	}

	private void initDnd() {
		new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, new DropTargetAdapter() {

			private boolean check(Transferable transfer) {
				// 检测拖放进来的数据类型
				if (!transfer.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
					return false;
				}
				try {
					FileFilter[] fileFilters = fileChooser.getChoosableFileFilters();
					Object td = transfer.getTransferData(DataFlavor.javaFileListFlavor);
					if (td instanceof List) {
						List<File> acceptFiles = new ArrayList<File>();
						for (Object value : ((List<?>) td)) {
							if (value instanceof File) {
								File srcFile = (File) value;
								for (FileFilter filter : fileFilters) {
									// 如果是图片文件
									if (srcFile.isFile() && filter.accept(srcFile) && !acceptFiles.contains(srcFile)) {
										// 把文件加到currentFiles中
										acceptFiles.add(srcFile);
										break;
									}
								}
							}
						}
						if (!acceptFiles.isEmpty())
							return true;
					}
				} catch (Exception ex) {

				}
				return false;
			}

			@Override
			public void dragEnter(DropTargetDragEvent evt) {
				Transferable transfer = evt.getTransferable();
				if (!check(transfer)) {
					evt.rejectDrag(); // 没有需要的类型，拒绝进入
				}
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
									service.open(ViewerFrame.this, file);
									break;
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

	public File openFile() {
		return service.open(this);
	}

	public void openFile(File srcFile) {
		service.open(this, srcFile);
	}

	public void openFile(File[] allFiles, File srcFile) {
		service.open(this, allFiles, srcFile);
	}

	public void setFile(File srcFile) {
		service.setImageFile(this, srcFile);
	}

	public JLabel getLabel() {
		return this.photoLabel;
	}

	public JPanel createToolPanel() {
		// 创建一个JPanel
		JPanel panel = new JPanel();
		// 创建一个标题为"工具"的工具栏
		JToolBar toolBar = new JToolBar(MENUBAR_TOOLS);
		// 设置为不可拖动
		toolBar.setFloatable(true);
		// 设置布局方式
		panel.setLayout(new FlowLayout(FlowLayout.LEFT));
		// 工具数组
		String[] toolarr = { "action.OpenAction", "action.LastAction", "action.NextAction", "action.BigAction", "action.SmallAction", "action.FixAction" };
		for (int i = 0; i < toolarr.length; i++) {
			ViewerAction action = new ViewerAction(new ImageIcon("resource" + File.separator + "image" + File.separator + toolarr[i] + ".gif"), toolarr[i],
					this);
			JButton button = new JButton(action);
			// 把button加到工具栏中
			toolBar.add(button);
		}
		panel.add(toolBar);
		// 返回
		return panel;
	}

	public void createMenuBar() {
		// 创建一个JMenuBar放置菜单
		JMenuBar menuBar = new JMenuBar();
		// 菜单文字数组，以下面的menuItemArr一一对应
		String[] menuArr = { MENUBAR_FILE, MENUBAR_TOOLS };
		// 菜单项文字数组
		String[][] menuItemArr = { { MENU_OPENFILE, "-", MENU_SET_ALWAYS_ONTOP, "-", MENU_EXIT }, //
				{ MENU_ZOOM_IN, MENU_ZOOM_OUT, MENU_ZOOM_FIX, "-", MENU_PREVIOUS, MENU_NEXT, "-", MENU_PLAY1_0S, MENU_PLAY0_8S, MENU_PLAY0_5S,
						MENU_PLAYCUSTOMS, MENU_PLAY_PAUSE, MENU_PLAY_STOP } };
		// 遍历menuArr与menuItemArr去创建菜单
		for (int i = 0; i < menuArr.length; i++) {
			// 新建一个JMenu菜单
			JMenu menu = new JMenu(menuArr[i]);
			for (int j = 0; j < menuItemArr[i].length; j++) {
				// 如果menuItemArr[i][j]等于"-"
				if (menuItemArr[i][j].equals("-")) {
					// 设置菜单分隔
					menu.addSeparator();
				} else {
					// 新建一个JMenuItem菜单项
					JMenuItem menuItem = new JMenuItem(menuItemArr[i][j]);
					menuItem.addActionListener(menuListener);
					// 把菜单项加到JMenu菜单里面
					menu.add(menuItem);
				}
			}
			// 把菜单加到JMenuBar上
			menuBar.add(menu);
		}
		// 设置JMenubar
		this.setJMenuBar(menuBar);
	}
}