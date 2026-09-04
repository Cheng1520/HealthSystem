package com.ncu.csh.view;

import com.ncu.csh.dao.CheckGroupDAO;
import com.ncu.csh.dao.CheckItemDAO;
import com.ncu.csh.entity.CheckGroup;
import com.ncu.csh.entity.CheckItem;
import com.ncu.csh.util.ReportUtil;
import com.ncu.csh.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 检查组管理界面 —— 检查组管理模块
 */
public class CheckGroupView extends JPanel {
//噶哟噶哟 gayou
    private final CheckGroupDAO checkGroupDAO = new CheckGroupDAO();
    private final CheckItemDAO checkItemDAO = new CheckItemDAO();
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField tfId;
    private JTextField tfName;
    private JLabel lblPage;
    private JComboBox<Integer> cbPageSize;
    private int page = 1;
    private int totalPages = 1;
    private int pageSize = 20;

    public CheckGroupView() {
        JPanel bg = UITheme.backgroundPanel("bg_panel.png", new Color(0xF5, 0xF9, 0xFF), new Color(0xEA, 0xF4, 0xF8));
        bg.setLayout(new BorderLayout());
        setLayout(new BorderLayout());
        add(bg, BorderLayout.CENTER);
        bg.add(UITheme.lightHeader("检查组管理", "勾选多个检查项组成检查组"), BorderLayout.NORTH);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(12, 14, 14, 14));
        toolbar.add(new JLabel("编号"));
        tfId = UITheme.textField(6);
        toolbar.add(tfId);
        toolbar.add(new JLabel("名称"));
        tfName = UITheme.textField(10);
        toolbar.add(tfName);
        JButton btnSearch = UITheme.blueButton("查询");
        JButton btnAll = UITheme.plainButton("查询全部");
        JButton btnDetail = UITheme.plainButton("查看组内检查项");
        JButton btnAdd = UITheme.blueButton("新增");
        JButton btnEdit = UITheme.orangeButton("修改");
        JButton btnDelete = UITheme.redButton("删除");
        JButton btnExport = UITheme.plainButton("导出报表");
        JButton btnPrev = UITheme.plainButton("上一页");
        JButton btnNext = UITheme.plainButton("下一页");
        toolbar.add(btnSearch);
        toolbar.add(btnAll);
        toolbar.add(btnDetail);
        toolbar.add(btnAdd);
        toolbar.add(btnEdit);
        toolbar.add(btnDelete);
        toolbar.add(btnExport);
        toolbar.add(btnPrev);
        toolbar.add(btnNext);
        JLabel lbPageSize = new JLabel("  每页");
        lbPageSize.setForeground(UITheme.GRAY_BTN_FG);
        toolbar.add(lbPageSize);
        cbPageSize = new JComboBox<>(new Integer[]{5, 10, 15, 20});
        cbPageSize.setSelectedItem(20);
        toolbar.add(cbPageSize);
        JLabel lbPageSize2 = new JLabel("条");
        lbPageSize2.setForeground(UITheme.GRAY_BTN_FG);
        toolbar.add(lbPageSize2);
        lblPage = new JLabel("  第 1 / 1 页");
        lblPage.setForeground(UITheme.GRAY_BTN_FG);
        toolbar.add(lblPage);
        bg.add(toolbar, BorderLayout.SOUTH);

        String[] headers = {"编号", "检查组名称", "备注"};
        tableModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 14, 14, 14));
        bg.add(scrollPane, BorderLayout.CENTER);

        loadData();

        btnSearch.addActionListener(e -> { page = 1; loadData(); });
        btnAll.addActionListener(e -> { tfId.setText(""); tfName.setText(""); page = 1; loadData(); });
        btnDetail.addActionListener(e -> viewGroupItems());
        btnAdd.addActionListener(e -> showEditDialog(null));
        btnEdit.addActionListener(e -> {
            CheckGroup g = getSelectedGroup();
            if (g != null) showEditDialog(g);
        });
        btnDelete.addActionListener(e -> deleteSelected());
        btnExport.addActionListener(e -> export());
        btnPrev.addActionListener(e -> { if (page > 1) { page--; loadData(); } });
        // 下一页要有上界保护，避免翻出空白页
        btnNext.addActionListener(e -> { if (page < totalPages) { page++; loadData(); } });
        // 每页条数改变后回到第一页再刷新
        cbPageSize.addActionListener(e -> {
            pageSize = (Integer) cbPageSize.getSelectedItem();
            page = 1;
            loadData();
        });
    }

    private void loadData() {
        tableModel.setRowCount(0);
        String idKw = tfId.getText().trim();
        String nameKw = tfName.getText().trim();
        // 先按当前条件算出总页数并校正页码，再查询，避免翻出空白页
        int total = checkGroupDAO.countByKeyword(idKw, nameKw);
        totalPages = Math.max(1, (int) Math.ceil((double) total / pageSize));
        if (page > totalPages) page = totalPages;
        if (page < 1) page = 1;
        List<CheckGroup> list = checkGroupDAO.queryByPage(idKw, nameKw, page, pageSize);
        for (CheckGroup g : list) {
            tableModel.addRow(new Object[]{g.getId(), g.getGroupName(), g.getRemark()});
        }
        lblPage.setText("  第 " + page + " / " + totalPages + " 页（共 " + total + " 条）");
    }

    /** 查看组内检查项（关联查询机制：检查组 → 组内所有检查项） */
    private void viewGroupItems() {
        CheckGroup g = getSelectedGroup();
        if (g == null) return;
        CheckGroup full = checkGroupDAO.getByIdWithItems(g.getId());
        if (full == null) {
            JOptionPane.showMessageDialog(this, "未找到该检查组", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<CheckItem> items = full.getItemList();
        String[] headers = {"检查项编号", "检查项名称", "单位"};
        List<String[]> rows = new ArrayList<>();
        if (items.isEmpty()) {
            rows.add(new String[]{"-", "该检查组暂无检查项", "-"});
        } else {
            for (CheckItem item : items) {
                rows.add(new String[]{String.valueOf(item.getId()), item.getItemName(),
                        item.getUnit() == null ? "" : item.getUnit()});
            }
        }
        DefaultTableModel m = new DefaultTableModel(headers, 0);
        for (String[] row : rows) m.addRow(row);
        JTable t = new JTable(m);
        UITheme.styleTable(t);
        JScrollPane sp = new JScrollPane(t);
        sp.setPreferredSize(new Dimension(400, 220));

        JButton btnExport = UITheme.plainButton("导出该组检查项报表");
        btnExport.addActionListener(e -> ReportUtil.exportHtml(
                "检查组[" + full.getGroupName() + "]包含的检查项", headers, rows));

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.add(new JLabel("检查组「" + full.getGroupName() + "」包含以下检查项：",
                SwingConstants.CENTER), BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);
        panel.add(btnExport, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, panel, "查看组内检查项", JOptionPane.PLAIN_MESSAGE);
    }

    private CheckGroup getSelectedGroup() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选中一行", "提示", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        CheckGroup g = new CheckGroup();
        g.setId(id);
        g.setGroupName((String) tableModel.getValueAt(row, 1));
        g.setRemark((String) tableModel.getValueAt(row, 2));
        return g;
    }

    private void showEditDialog(CheckGroup group) {
        boolean isEdit = group != null;
        List<CheckItem> allItems = checkItemDAO.listAll();

        JTextField tfName = new JTextField(isEdit ? group.getGroupName() : "", 20);
        JTextField tfRemark = new JTextField(isEdit ? group.getRemark() : "", 20);

        JPanel panel = new JPanel(new BorderLayout(0, 10));
        JPanel top = new JPanel(new GridLayout(2, 2, 8, 8));
        top.add(new JLabel("检查组名称"));
        top.add(tfName);
        top.add(new JLabel("备注"));
        top.add(tfRemark);
        panel.add(top, BorderLayout.NORTH);

        JPanel itemPanel = new JPanel(new GridLayout(0, 3, 8, 6));
        itemPanel.setBorder(BorderFactory.createTitledBorder("勾选检查项"));
        List<JCheckBox> checkBoxes = new ArrayList<>();
        for (CheckItem item : allItems) {
            JCheckBox box = new JCheckBox(item.getItemName());
            box.putClientProperty("itemId", item.getId());
            checkBoxes.add(box);
            itemPanel.add(box);
        }
        JScrollPane itemScroll = new JScrollPane(itemPanel);
        itemScroll.setPreferredSize(new Dimension(0, 180));
        panel.add(itemScroll, BorderLayout.CENTER);

        // 编辑时回显勾选
        if (isEdit) {
            CheckGroup full = checkGroupDAO.getByIdWithItems(group.getId());
            for (JCheckBox box : checkBoxes) {
                int boxId = (int) box.getClientProperty("itemId");
                for (CheckItem ci : full.getItemList()) {
                    if (ci.getId() == boxId) {
                        box.setSelected(true);
                    }
                }
            }
        }

        int r = JOptionPane.showConfirmDialog(this, panel, isEdit ? "修改检查组" : "新增检查组",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (r != JOptionPane.OK_OPTION) return;

        String name = tfName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "检查组名称不能为空", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        List<Integer> itemIds = new ArrayList<>();
        for (JCheckBox box : checkBoxes) {
            if (box.isSelected()) {
                itemIds.add((int) box.getClientProperty("itemId"));
            }
        }

        CheckGroup g = new CheckGroup();
        g.setId(isEdit ? group.getId() : null);
        g.setGroupName(name);
        g.setRemark(tfRemark.getText().trim());

        try {
            if (isEdit) {
                checkGroupDAO.update(g, itemIds);
            } else {
                checkGroupDAO.add(g, itemIds);
            }
            loadData();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选中一行", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        int r = JOptionPane.showConfirmDialog(this, "确定删除该检查组？", "确认", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            checkGroupDAO.deleteById(id);
            loadData();
        }
    }

    private void export() {
        String[] headers = {"编号", "检查组名称", "备注"};
        List<String[]> rows = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String[] row = new String[tableModel.getColumnCount()];
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                Object v = tableModel.getValueAt(i, j);
                row[j] = v == null ? "" : v.toString();
            }
            rows.add(row);
        }
        ReportUtil.exportHtml("检查组报表", headers, rows);
    }
}
