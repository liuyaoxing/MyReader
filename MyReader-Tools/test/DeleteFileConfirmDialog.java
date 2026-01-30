import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;

public class DeleteFileConfirmDialog {

    /**
     * 显示删除文件确认对话框，列出所有待删除文件
     *
     * @param parent   父窗口（可为 null）
     * @param files    要删除的文件列表
     * @return true 表示用户确认删除，false 表示取消
     */
    public static boolean showConfirmDialog(Component parent, List<File> files) {
        if (files == null || files.isEmpty()) {
            return true; // 无文件则直接返回 true
        }

        // 构建文件列表字符串
        StringBuilder sb = new StringBuilder();
        sb.append("即将删除以下 ").append(files.size()).append(" 个文件：");
        for (File file : files) {
            sb.append(file.getAbsolutePath());
        }

        // 创建自定义面板显示文件列表
        JPanel panel = new JPanel(new BorderLayout());
        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        textArea.setFont(new Font("宋体", Font.PLAIN, 12));
        textArea.setBackground(UIManager.getColor("Panel.background"));
        textArea.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 200));

        panel.add(scrollPane, BorderLayout.CENTER);

        // 设置图标（可选）
        Icon warningIcon = UIManager.getIcon("OptionPane.warningIcon");

        // 显示对话框
        int result = JOptionPane.showConfirmDialog(
                parent,
                panel,
                "确认删除文件",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.WARNING_MESSAGE,
                warningIcon
        );

        return result == JOptionPane.OK_OPTION;
    }

    // === 测试用主方法 ===
    public static void main(String[] args) {
        // 模拟要删除的文件列表
        List<File> filesToDelete = Arrays.asList(
                new File("/adhome/ex-liuraoxing135/Documents/afa-pdfwithpdffox/test1.pdf"),
                new File("/adhome/ex-liuraoxing135/Documents/afa-pdfwithpdffox/test2.jpg"),
                new File("/adhome/ex-liuraoxing135/Documents/afa-pdfwithpdffox/temp.log")
        );

        // 显示确认对话框
        boolean confirmed = showConfirmDialog(null, filesToDelete);

        if (confirmed) {
            System.out.println("✅ 用户确认删除文件");
            // 这里可以调用删除逻辑：filesToDelete.forEach(f -> f.delete());
        } else {
            System.out.println("❌ 用户取消删除");
        }
    }
}