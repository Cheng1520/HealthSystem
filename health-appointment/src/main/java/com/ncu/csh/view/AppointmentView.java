package com.ncu.csh.view;

import com.ncu.csh.dao.AppointmentDAO;
import com.ncu.csh.dao.CheckGroupDAO;
import com.ncu.csh.dao.CheckItemDAO;
import com.ncu.csh.entity.Appointment;
import com.ncu.csh.entity.CheckGroup;
import com.ncu.csh.entity.CheckItem;
import com.ncu.csh.entity.CheckResult;
import com.ncu.csh.util.ReportUtil;
import com.ncu.csh.util.ResultAnalyzer;
import com.ncu.csh.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * 预约与跟踪界面 —— 预约体检、体检方式选择、结果分析、病史对比与跟踪
 */
public class AppointmentView extends JPanel {

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final CheckGroupDAO checkGroupDAO = new CheckGroupDAO();
    private final CheckItemDAO checkItemDAO = new CheckItemDAO();

    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField tfSearch;

    public AppointmentView() {
        JPanel bg = UITheme.backgroundPanel("bg_panel.png", new Color(0xF5, 0xF9, 0xFF), new Color(0xEA, 0xF4, 0xF8));
        bg.setLayout(new BorderLayout());
        setLayout(new BorderLayout());
        add(bg, BorderLayout.CENTER);
        bg.add(UITheme.gradientHeader("预约与跟踪", "预约体检 · 结果分析 · 病史对比"), BorderLayout.NORTH);

        // 顶部工具栏
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(12, 14, 14, 14));
        toolbar.add(new JLabel("搜索用户"));
        tfSearch = UITheme.textField(12);
        toolbar.add(tfSearch);
        JButton btnSearch = UITheme.blueButton("查询");
        JButton btnAdd = UITheme.blueButton("新增预约");
        JButton btnEdit = UITheme.orangeButton("修改");
        JButton btnDelete = UITheme.redButton("删除");
        JButton btnFinish = UITheme.plainButton("标记完成");
        JButton btnResult = UITheme.plainButton("录入结果");
        JButton btnHistory = UITheme.plainButton("病史对比");
        JButton btnExport = UITheme.plainButton("导出报表");
        toolbar.add(btnSearch);
        toolbar.add(btnAdd);
        toolbar.add(btnEdit);
        toolbar.add(btnDelete);
        toolbar.add(btnFinish);
        toolbar.add(btnResult);
        toolbar.add(btnHistory);
        toolbar.add(btnExport);
        bg.add(toolbar, BorderLayout.NORTH);

        // 表格
        String[] headers = {"编号", "用户", "体检方式", "检查组", "检查项", "预约日期", "状态", "备注"};
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

        btnSearch.addActionListener(e -> loadData());
        btnAdd.addActionListener(e -> showEditDialog(null));
        btnEdit.addActionListener(e -> {
            Appointment a = getSelected();
            if (a != null) showEditDialog(a);
        });
        btnDelete.addActionListener(e -> deleteSelected());
        btnFinish.addActionListener(e -> finishSelected());
        btnResult.addActionListener(e -> inputResult());
        btnHistory.addActionListener(e -> historyCompare());
        btnExport.addActionListener(e -> export());
    }

    private void loadData() {
        tableModel.setRowCount(0);
        String keyword = tfSearch.getText().trim();
        List<Appointment> list = appointmentDAO.queryList(keyword);
        for (Appointment a : list) {
            tableModel.addRow(new Object[]{
                    a.getId(), a.getUserName(), a.getMethod(), a.getGroupName(), a.getItemName(),
                    a.getAppointDate(), a.getStatus(), a.getRemark()
            });
        }
    }

    private Appointment getSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选中一行", "提示", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        return appointmentDAO.getById((int) tableModel.getValueAt(row, 0));
    }

    private void showEditDialog(Appointment appt) {
        boolean isEdit = appt != null;
        List<Object[]> users = appointmentDAO.listUsers();
        List<CheckGroup> groups = checkGroupDAO.listAll();
        List<CheckItem> items = checkItemDAO.listAll();

        JComboBox<String> cbUser = new JComboBox<>();
        for (Object[] u : users) {
            cbUser.addItem(u[0] + "-" + u[1]);
        }
        JComboBox<String> cbMethod = new JComboBox<>(new String[]{"套餐", "单项"});
        JComboBox<String> cbGroup = new JComboBox<>();
        for (CheckGroup g : groups) {
            cbGroup.addItem(g.getGroupName());
        }
        JComboBox<String> cbItem = new JComboBox<>();
        for (CheckItem i : items) {
            cbItem.addItem(i.getItemName());
        }
        JTextField tfDate = new JTextField(LocalDate.now().toString(), 12);
        JTextField tfRemark = new JTextField(16);

        if (isEdit) {
            cbMethod.setSelectedItem(appt.getMethod());
            tfDate.setText(appt.getAppointDate());
            tfRemark.setText(appt.getRemark());
            for (int i = 0; i < cbUser.getItemCount(); i++) {
                if (cbUser.getItemAt(i).startsWith(appt.getUserId() + "-")) {
                    cbUser.setSelectedIndex(i);
                }
            }
            if (appt.getCheckGroupId() != null) {
                for (int i = 0; i < cbGroup.getItemCount(); i++) {
                    if (groups.get(i).getId().equals(appt.getCheckGroupId())) {
                        cbGroup.setSelectedIndex(i);
                    }
                }
            }
            if (appt.getCheckItemId() != null) {
                for (int i = 0; i < cbItem.getItemCount(); i++) {
                    if (items.get(i).getId().equals(appt.getCheckItemId())) {
                        cbItem.setSelectedIndex(i);
                    }
                }
            }
        }

        // 体检方式切换联动：套餐→检查组可选，单项→检查项可选
        cbMethod.addActionListener(e -> {
            String method = (String) cbMethod.getSelectedItem();
            cbGroup.setEnabled("套餐".equals(method));
            cbItem.setEnabled("单项".equals(method));
        });
        cbGroup.setEnabled("套餐".equals(cbMethod.getSelectedItem()));
        cbItem.setEnabled("单项".equals(cbMethod.getSelectedItem()));

        JPanel panel = new JPanel(new GridLayout(6, 2, 8, 8));
        panel.add(new JLabel("预约用户"));
        panel.add(cbUser);
        panel.add(new JLabel("体检方式"));
        panel.add(cbMethod);
        panel.add(new JLabel("检查组"));
        panel.add(cbGroup);
        panel.add(new JLabel("检查项"));
        panel.add(cbItem);
        panel.add(new JLabel("预约日期(yyyy-MM-dd)"));
        panel.add(tfDate);
        panel.add(new JLabel("备注"));
        panel.add(tfRemark);

        int r = JOptionPane.showConfirmDialog(this, panel, isEdit ? "修改预约" : "新增预约",
                JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) return;

        String method = (String) cbMethod.getSelectedItem();
        Appointment a = new Appointment();
        a.setId(isEdit ? appt.getId() : null);
        a.setUserId(Integer.parseInt(String.valueOf(users.get(cbUser.getSelectedIndex())[0])));
        a.setMethod(method);
        a.setCheckGroupId("套餐".equals(method) ? groups.get(cbGroup.getSelectedIndex()).getId() : null);
        a.setCheckItemId("单项".equals(method) ? items.get(cbItem.getSelectedIndex()).getId() : null);
        String dateStr = tfDate.getText().trim();
        if (!isValidDate(dateStr)) {
            JOptionPane.showMessageDialog(this, "预约日期格式应为 yyyy-MM-dd，例如 2026-09-03", "提示",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        a.setAppointDate(dateStr);
        a.setStatus(isEdit ? appt.getStatus() : "已预约");
        a.setRemark(tfRemark.getText().trim());

        try {
            if (isEdit) {
                appointmentDAO.update(a);
            } else {
                appointmentDAO.add(a);
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
        int r = JOptionPane.showConfirmDialog(this, "确定删除该预约？", "确认", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            appointmentDAO.deleteById(id);
            loadData();
        }
    }

    private void finishSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选中一行", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        appointmentDAO.updateStatus(id, "已完成");
        loadData();
    }

    /** 录入检查结果：根据预约取出应检项，逐项输入数值并自动分析 */
    private void inputResult() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选中一条预约", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int appointmentId = (int) tableModel.getValueAt(row, 0);
        String appointDate = (String) tableModel.getValueAt(row, 5);
        List<CheckItem> items = appointmentDAO.listItemsByAppointment(appointmentId);

        if (items.isEmpty()) {
            JOptionPane.showMessageDialog(this, "该预约没有可检测的检查项", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        List<JTextField> fields = new ArrayList<>();
        for (CheckItem item : items) {
            String label = item.getItemName() + (item.getUnit() == null ? "" : "(" + item.getUnit() + ")");
            panel.add(new JLabel(label));
            JTextField tf = new JTextField();
            fields.add(tf);
            panel.add(tf);
        }
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setPreferredSize(new Dimension(360, 220));

        int r = JOptionPane.showConfirmDialog(this, scroll, "录入检查结果", JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) return;

        try {
            int saved = 0;
            for (int i = 0; i < items.size(); i++) {
                CheckItem item = items.get(i);
                String valStr = fields.get(i).getText().trim();
                if (valStr.isEmpty()) continue;
                double value = Double.parseDouble(valStr);
                CheckResult result = new CheckResult();
                result.setAppointmentId(appointmentId);
                result.setItemId(item.getId());
                result.setValue(value);
                result.setAnalysis(ResultAnalyzer.analyze(value, item.getRefMin(), item.getRefMax()));
                result.setCheckDate(appointDate);
                appointmentDAO.saveResult(result);
                saved++;
            }
            if (saved == 0) {
                JOptionPane.showMessageDialog(this, "未录入任何检查结果，预约状态未改变", "提示",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }
            appointmentDAO.updateStatus(appointmentId, "已完成");
            JOptionPane.showMessageDialog(this, "已保存 " + saved + " 条检查结果");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "数值格式不正确", "提示", JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** 校验日期格式 yyyy-MM-dd（严格校验，非法日期如 2026-13-99 会被拒绝） */
    private boolean isValidDate(String s) {
        if (s == null || s.isEmpty()) {
            return false;
        }
        try {
            LocalDate.parse(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /** 病史对比与跟踪：选择用户和检查项，展示历史结果 */
    private void historyCompare() {
        List<Object[]> users = appointmentDAO.listUsers();
        List<CheckItem> items = checkItemDAO.listAll();
        if (users.isEmpty() || items.isEmpty()) {
            JOptionPane.showMessageDialog(this, "暂无用户或检查项数据", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JComboBox<String> cbUser = new JComboBox<>();
        for (Object[] u : users) {
            cbUser.addItem(u[0] + "-" + u[1]);
        }
        JComboBox<String> cbItem = new JComboBox<>();
        for (CheckItem i : items) {
            cbItem.addItem(i.getItemName());
        }

        JPanel panel = new JPanel(new GridLayout(2, 2, 8, 8));
        panel.add(new JLabel("选择用户"));
        panel.add(cbUser);
        panel.add(new JLabel("选择检查项"));
        panel.add(cbItem);

        int r = JOptionPane.showConfirmDialog(this, panel, "病史对比与跟踪", JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) return;

        int userId = Integer.parseInt(String.valueOf(users.get(cbUser.getSelectedIndex())[0]));
        int itemId = items.get(cbItem.getSelectedIndex()).getId();
        List<CheckResult> history = appointmentDAO.listHistory(userId, itemId);

        if (history.isEmpty()) {
            JOptionPane.showMessageDialog(this, "该用户没有该项的历史检查记录", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] headers = {"日期", "数值", "单位", "分析"};
        List<String[]> rows = new ArrayList<>();
        for (CheckResult h : history) {
            rows.add(new String[]{h.getCheckDate(), String.valueOf(h.getValue()), h.getUnit(), h.getAnalysis()});
        }

        DefaultTableModel m = new DefaultTableModel(headers, 0);
        for (String[] row : rows) {
            m.addRow(row);
        }
        JTable t = new JTable(m);
        UITheme.styleTable(t);
        JScrollPane sp = new JScrollPane(t);
        sp.setPreferredSize(new Dimension(420, 220));

        JButton btnExport = UITheme.plainButton("导出该病史报表");
        btnExport.addActionListener(e -> ReportUtil.exportHtml(
                "病史对比：" + items.get(cbItem.getSelectedIndex()).getItemName(), headers, rows));

        JPanel resultPanel = new JPanel(new BorderLayout(0, 8));
        resultPanel.add(sp, BorderLayout.CENTER);
        resultPanel.add(btnExport, BorderLayout.SOUTH);

        JOptionPane.showMessageDialog(this, resultPanel, "病史对比结果", JOptionPane.PLAIN_MESSAGE);
    }

    private void export() {
        String[] headers = {"编号", "用户", "体检方式", "检查组", "检查项", "预约日期", "状态", "备注"};
        List<String[]> rows = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String[] row = new String[tableModel.getColumnCount()];
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                Object v = tableModel.getValueAt(i, j);
                row[j] = v == null ? "" : v.toString();
            }
            rows.add(row);
        }
        ReportUtil.exportHtml("预约与跟踪报表", headers, rows);
    }
}
