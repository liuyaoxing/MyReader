import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class ComboBoxWithCheckList extends JFrame {
    private JComboBox<String> comboBox;
    private DefaultListModel<String> listModel;
    private JList<String> checkList;
    private JPopupMenu popupMenu;

    public ComboBoxWithCheckList() {
        setTitle("下拉按钮弹出可复选列表");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300, 200);
        setLayout(new FlowLayout());

        // 1. 创建下拉按钮（使用 JComboBox）
        comboBox = new JComboBox<>();
        comboBox.setEditable(false);
        comboBox.setMaximumRowCount(5);

        // 2. 初始化可复选列表
        listModel = new DefaultListModel<>();
        listModel.addElement("选项1");
        listModel.addElement("选项2");
        listModel.addElement("选项3");
        listModel.addElement("选项4");

        checkList = new JList<>(listModel);
        checkList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        checkList.setCellRenderer(new CheckListCellRenderer());

        // 3. 创建弹出菜单（Popup Menu）
        popupMenu = new JPopupMenu();
        popupMenu.add(new JScrollPane(checkList));
        popupMenu.setPreferredSize(new Dimension(150, 100));

        // 4. 绑定点击事件：点击下拉按钮时弹出菜单
        comboBox.addActionListener(e -> {
            popupMenu.show(comboBox, 0, comboBox.getHeight());
        });

        // 5. 点击确认按钮：将选中项设置为下拉框文本
        JButton confirmButton = new JButton("确定");
        confirmButton.addActionListener(e -> {
            List<String> selectedItems = getSelectedItems();
            if (selectedItems.isEmpty()) {
                comboBox.setSelectedItem("无");
            } else {
                comboBox.setSelectedItem(String.join(", ", selectedItems));
            }
            popupMenu.setVisible(false);
        });

        // 6. 添加组件
        add(comboBox);
        add(confirmButton);

        // 7. 设置初始值
        comboBox.setSelectedItem("无");
    }

    // 获取当前选中的项
    private List<String> getSelectedItems() {
        List<String> selected = new ArrayList<>();
        int[] indices = checkList.getSelectedIndices();
        for (int index : indices) {
            selected.add(listModel.getElementAt(index));
        }
        return selected;
    }

    // 自定义列表项渲染器：支持复选框
    private static class CheckListCellRenderer extends JCheckBox implements ListCellRenderer<String> {
        public CheckListCellRenderer() {
            setOpaque(true);
            setHorizontalAlignment(SwingConstants.LEFT);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends String> list, String value, int index,
                                                      boolean isSelected, boolean cellHasFocus) {
            setText(value);
            setSelected(list.isSelectedIndex(index));
            setBackground(isSelected ? Color.LIGHT_GRAY : Color.WHITE);
            setForeground(isSelected ? Color.BLACK : Color.BLACK);
            return this;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }
            new ComboBoxWithCheckList().setVisible(true);
        });
    }
}