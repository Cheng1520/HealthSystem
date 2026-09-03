package com.ncu.csh.view;

import com.ncu.csh.dao.CheckItemDAO;
import com.ncu.csh.entity.CheckItem;
import com.ncu.csh.util.ReportUtil;
import com.ncu.csh.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 检查项管理界面 —— 检查项管理模块
 */
public class CheckItemView extends JPanel {

    private final CheckItemDAO checkItemDAO = new CheckItemDAO();
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField tfId;
    private JTextField tfName;
    private int page = 1;
    private final int pageSize = 8;

    public CheckItemView() {
        JPanel bg = UITheme.backgroundPanel("bg_panel.png", new Color(0xF5, 0xF9, 0xFF), new Color(0xEA, 0xF4, 0xF8));
        bg.setLayout(new BorderLayout());
        setLayout(new BorderLayout());
        add(bg, BorderLayout.CENTER);
        bg.add(UITheme.gradientHeader("检查项管理", "血红蛋白、白细胞等检查项的增删改查"), BorderLayout.NORTH);

        // 顶部工具栏
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
        JButton btnAdd = UITheme.blueButton("新增");
        JButton btnEdit = UITheme.orangeButton("修改");
        JButton btnDelete = UITheme.redButton("删除");
        JButton btnExport = UITheme.plainButton("导出报表");
        JButton btnPrev = UITheme.plainButton("上一页");
        JButton btnNext = UITheme.plainButton("下一页");
        toolbar.add(btnSearch);
        toolbar.add(btnAll);
        toolbar.add(btnAdd);
        toolbar.add(btnEdit);
        toolbar.add(btnDelete);
        toolbar.add(btnExport);
        toolbar.add(btnPrev);
        toolbar.add(btnNext);
        bg.add(toolbar, BorderLayout.SOUTH);

        // 表格
        String[] headers = {"编号", "检查项名称", "单位", "参考下限", "参考上限", "备注"};
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
        btnAdd.addActionListener(e -> showEditDialog(null));
        btnEdit.addActionListener(e -> {
            CheckItem item = getSelectedItem();
            if (item != null) showEditDialog(item);
        });
        btnDelete.addActionListener(e -> deleteSelected());
        btnExport.addActionListener(e -> export());
        btnPrev.addActionListener(e -> { if (page > 1) { page--; loadData(); } });
        btnNext.addActionListener(e -> { page++; loadData(); });
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<CheckItem> list = checkItemDAO.queryByPage(
                tfId.getText().trim(), tfName.getText().trim(), page, pageSize);
        for (CheckItem c : list) {
            tableModel.addRow(new Object[]{
                    c.getId(), c.getItemName(), c.getUnit(), c.getRefMin(), c.getRefMax(), c.getRemark()
            });
        }
    }

    private CheckItem getSelectedItem() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选中一行", "提示", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        CheckItem c = new CheckItem();
        c.setId(id);
        c.setItemName((String) tableModel.getValueAt(row, 1));
        c.setUnit((String) tableModel.getValueAt(row, 2));
        c.setRefMin(toDouble(tableModel.getValueAt(row, 3)));
        c.setRefMax(toDouble(tableModel.getValueAt(row, 4)));
        c.setRemark((String) tableModel.getValueAt(row, 5));
        return c;
    }

    private Double toDouble(Object o) {
        if (o == null || o.toString().isEmpty()) return null;
        return Double.parseDouble(o.toString());
    }

    private void showEditDialog(CheckItem item) {
        boolean isEdit = item != null;
        JPanel form = new JPanel(new GridLayout(5, 2, 8, 8));
        JTextField tfName = new JTextField(isEdit ? item.getItemName() : "");
        JTextField tfUnit = new JTextField(isEdit ? item.getUnit() : "");
        JTextField tfMin = new JTextField(isEdit ? String.valueOf(item.getRefMin() == null ? "" : item.getRefMin()) : "");
        JTextField tfMax = new JTextField(isEdit ? String.valueOf(item.getRefMax() == null ? "" : item.getRefMax()) : "");
        JTextField tfRemark = new JTextField(isEdit ? item.getRemark() : "");
        form.add(new JLabel("检查项名称"));
        form.add(tfName);
        form.add(new JLabel("单位"));
        form.add(tfUnit);
        form.add(new JLabel("参考下限"));
        form.add(tfMin);
        form.add(new JLabel("参考上限"));
        form.add(tfMax);
        form.add(new JLabel("备注"));
        form.add(tfRemark);

        int r = JOptionPane.showConfirmDialog(this, form, isEdit ? "修改检查项" : "新增检查项",
                JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) return;

        String name = tfName.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "检查项名称不能为空", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        CheckItem c = new CheckItem();
        c.setId(isEdit ? item.getId() : null);
        c.setItemName(name);
        c.setUnit(tfUnit.getText().trim());
        c.setRefMin(parseDouble(tfMin.getText()));
        c.setRefMax(parseDouble(tfMax.getText()));
        c.setRemark(tfRemark.getText().trim());

        try {
            if (isEdit) {
                checkItemDAO.update(c);
            } else {
                checkItemDAO.add(c);
            }
            loadData();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Double parseDouble(String s) {
        s = s.trim();
        return s.isEmpty() ? null : Double.parseDouble(s);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选中一行", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        int r = JOptionPane.showConfirmDialog(this, "确定删除该检查项？", "确认", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            checkItemDAO.deleteById(id);
            loadData();
        }
    }

    private void export() {
        String[] headers = {"编号", "检查项名称", "单位", "参考下限", "参考上限", "备注"};
        List<String[]> rows = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String[] row = new String[tableModel.getColumnCount()];
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                Object v = tableModel.getValueAt(i, j);
                row[j] = v == null ? "" : v.toString();
            }
            rows.add(row);
        }
        ReportUtil.exportHtml("检查项报表", headers, rows);
    }
}
