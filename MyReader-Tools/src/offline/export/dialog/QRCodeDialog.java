package offline.export.dialog;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.Hashtable;

public class QRCodeDialog extends JDialog {

	/** 序列号 */
	private static final long serialVersionUID = 3096870915107905428L;
	private final JLabel qrLabel;
	private final JTextField textLabel;
	private final JButton saveButton;

	// 构造函数
	public QRCodeDialog(JFrame parent, String content, String title, String description) {
		super(parent, title, true); // 模态对话框

		// 设置窗口属性
		setSize(300, 400);
		setLocationRelativeTo(parent);
		setResizable(true);
		setLayout(new BorderLayout());

		// 生成二维码
		BufferedImage qrImage = createQRCode(content, 256, 256);

		// 二维码标签
		qrLabel = new JLabel(new ImageIcon(qrImage));
		qrLabel.setHorizontalAlignment(SwingConstants.CENTER);
		qrLabel.setBorder(BorderFactory.createEmptyBorder(15, 15, 10, 15));

		// 文字说明标签
		textLabel = new JTextField(description, SwingConstants.CENTER);
		textLabel.setFont(new Font("微软雅黑", Font.PLAIN, 11));
		// textLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
		textLabel.setForeground(Color.DARK_GRAY);

		// 保存按钮（可选）
		saveButton = new JButton("保存二维码");
		saveButton.addActionListener(e -> saveQRCode(qrImage));

		// 按钮面板
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
		buttonPanel.add(saveButton);

		// 添加组件
		add(qrLabel, BorderLayout.CENTER);
		add(textLabel, BorderLayout.NORTH);
		add(buttonPanel, BorderLayout.SOUTH);

		// 点击关闭时关闭对话框
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}

	/**
	 * 生成二维码图像
	 */
	private BufferedImage createQRCode(String content, int width, int height) {
		QRCodeWriter qrCodeWriter = new QRCodeWriter();
		Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
		hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H); // 高容错
		hints.put(EncodeHintType.MARGIN, 1); // 边距

		try {
			BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					image.setRGB(x, y, bitMatrix.get(x, y) ? Color.BLACK.getRGB() : Color.WHITE.getRGB());
				}
			}
			return image;
		} catch (WriterException e) {
			JOptionPane.showMessageDialog(this, "生成二维码失败: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
			return new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		}
	}

	/**
	 * 保存二维码为 PNG 文件
	 */
	private void saveQRCode(BufferedImage image) {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setSelectedFile(new File("qrcode.png"));
		fileChooser.setDialogTitle("保存二维码");
		fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PNG 图片", "png"));

		int result = fileChooser.showSaveDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			File file = fileChooser.getSelectedFile();
			if (!file.getName().toLowerCase().endsWith(".png")) {
				file = new File(file.getParentFile(), file.getName() + ".png");
			}
			try {
				if (ImageIO.write(image, "png", file)) {
					JOptionPane.showMessageDialog(this, "✅ 二维码已保存至：" + file.getAbsolutePath(), "成功", JOptionPane.INFORMATION_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(this, "❌ 保存失败", "错误", JOptionPane.ERROR_MESSAGE);
				}
			} catch (IOException e) {
				JOptionPane.showMessageDialog(this, "❌ 保存失败：" + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	/**
	 * 更新二维码内容和文字
	 */
	public void updateQRCode(String newContent, String newDescription) {
		BufferedImage newImage = createQRCode(newContent, qrLabel.getIcon().getIconWidth(), qrLabel.getIcon().getIconHeight());
		qrLabel.setIcon(new ImageIcon(newImage));
		textLabel.setText(newDescription);
		revalidate();
		repaint();
	}

	// ==================== 测试入口 ====================
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			JFrame frame = new JFrame("二维码对话框演示");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			frame.setSize(400, 300);
			frame.setLocationRelativeTo(null);

			JButton btn = new JButton("显示扫码登录");
			btn.addActionListener(e -> {
				QRCodeDialog dialog = new QRCodeDialog(frame, "https://login.example.com?token=abc123xyz", "扫码登录", "请使用手机扫码登录");
				dialog.setVisible(true);
			});

			frame.setLayout(new FlowLayout());
			frame.add(btn);
			frame.setVisible(true);
		});
	}
}