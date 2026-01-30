import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

public class ComboBoxWithCheckList_Fixed extends JFrame {
    private JComboBox<String> comboBox;
    private DefaultListModel<String> listModel;
    private JList<String> checkList;
    private JPopupMenu popupMenu;

    public ComboBoxWithCheckList_Fixed() {
        setTitle("下拉按钮弹出可复选列表（已修复无内容问题）");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(350, 220);
        setLayout(new FlowLayout());

        // 1. 创建下拉按钮
        comboBox = new JComboBox<>();
        comboBox.setEditable(false);
        comboBox.setMaximumRowCount(5);

        // ✅ 2. 确保列表有内容（关键！）
        listModel = new DefaultListModel<>();
        listModel.addElement("苹果");
        listModel.addElement("香蕉");
        listModel.addElement("橙子");
        listModel.addElement("葡萄");
        listModel.addElement("草莓");

        // 3. 创建可复选的列表（支持多选）
        checkList = new JList<>(listModel);
        checkList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        checkList.setCellRenderer(new CheckListCellRenderer());

        // 4. 创建弹出菜单（带滚动条）
        popupMenu = new JPopupMenu();
        popupMenu.add(new JScrollPane(checkList));
        popupMenu.setPreferredSize(new Dimension(180, 120));

        // 5. 点击下拉按钮时弹出菜单
        comboBox.addActionListener(e -> {
            // ✅ 重置选中状态（避免旧状态残留）
            checkList.clearSelection();
            // ✅ 获取当前选中项并恢复选中
            String currentText = (String) comboBox.getSelectedItem();
            if (currentText != null && !currentText.equals("无")) {
                String[] selectedArray = currentText.split(", ");
                for (String item : selectedArray) {
                    for (int i = 0; i < listModel.size(); i++) {
                        if (listModel.getElementAt(i).equals(item)) {
                            checkList.addSelectionInterval(i, i);
                        }
                    }
                }
            }
            popupMenu.show(comboBox, 0, comboBox.getHeight());
        });

        // 6. 确定按钮：保存选中项并关闭菜单
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

        // 7. 取消按钮：关闭菜单但不保存
        JButton cancelButton = new JButton("取消");
        cancelButton.addActionListener(e -> popupMenu.setVisible(false));

        // 添加组件
        add(comboBox);
        add(confirmButton);
        add(cancelButton);

        // 8. 设置初始值
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

    // 自定义渲染器：显示复选框
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
            setForeground(Color.BLACK);
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
            new ComboBoxWithCheckList_Fixed().setVisible(true);
        });
    }
}