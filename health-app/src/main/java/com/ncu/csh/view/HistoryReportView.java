package com.ncu.csh.view;

import com.ncu.csh.dao.AppointmentDAO;
import com.ncu.csh.entity.Appointment;
import com.ncu.csh.entity.CheckResult;
import com.ncu.csh.util.ReportUtil;
import com.ncu.csh.util.Session;
import com.ncu.csh.util.UITheme;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 历史检查报告界面 —— 查看历史检查报告与医生诊断建议。
 * 普通用户只能看自己的报告；医生/管理员可查看全部并填写诊断建议。
 */
public class HistoryReportView extends JPanel {

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField tfSearch;

    public HistoryReportView() {
        boolean normal = isNormalUser();

        JPanel bg = UITheme.backgroundPanel("bg_panel.png", new Color(0xF5, 0xF9, 0xFF), new Color(0xEA, 0xF4, 0xF8));
        bg.setLayout(new BorderLayout());
        setLayout(new BorderLayout());
        add(bg, BorderLayout.CENTER);
        bg.add(UITheme.gradientHeader("历史检查报告", "查看检查报告 · 医生诊断建议"), BorderLayout.NORTH);

        // 工具栏：医生/管理员可搜索，普通用户仅查看自己的报告
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(12, 14, 14, 14));

        JButton btnSearch = null;
        JButton btnSuggest = null;
        if (!normal) {
            toolbar.add(new JLabel("搜索用户"));
            tfSearch = UITheme.textField(12);
            toolbar.add(tfSearch);
            btnSearch = UITheme.blueButton("查询");
            toolbar.add(btnSearch);
        }
        JButton btnDetail = UITheme.blueButton("查看报告明细");
        toolbar.add(btnDetail);
        if (!normal) {
            btnSuggest = UITheme.orangeButton("填写诊断建议");
            toolbar.add(btnSuggest);
        }
        JButton btnExport = UITheme.plainButton("导出报表");
        toolbar.add(btnExport);
        bg.add(toolbar, BorderLayout.NORTH);

        // 表格
        String[] headers = {"编号", "用户", "体检方式", "检查组", "检查项", "预约日期", "状态", "诊断建议"};
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

        if (btnSearch != null) btnSearch.addActionListener(e -> loadData());
        btnDetail.addActionListener(e -> showDetail());
        if (btnSuggest != null) btnSuggest.addActionListener(e -> editSuggestion());
        btnExport.addActionListener(e -> export());
    }

    private boolean isNormalUser() {
        return Session.currentUser != null && "普通用户".equals(Session.currentUser.getRole());
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<Appointment> list;
        if (isNormalUser()) {
            list = appointmentDAO.listReportsByUser(Session.currentUser.getId());
        } else {
            String keyword = tfSearch == null ? "" : tfSearch.getText().trim();
            list = appointmentDAO.listReportsAll(keyword);
        }
        for (Appointment a : list) {
            tableModel.addRow(new Object[]{
                    a.getId(), a.getUserName(), a.getMethod(), a.getGroupName(), a.getItemName(),
                    a.getAppointDate(), a.getStatus(),
                    a.getSuggestion() == null ? "" : a.getSuggestion()
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

    /** 查看报告明细：该预约下的全部检查结果 */
    private void showDetail() {
        Appointment a = getSelected();
        if (a == null) return;
        List<CheckResult> results = appointmentDAO.listResultsByAppointment(a.getId());
        if (results.isEmpty()) {
            JOptionPane.showMessageDialog(this, "该报告暂无检查结果", "提示", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        String[] headers = {"检查项", "数值", "单位", "分析", "检测日期"};
        DefaultTableModel m = new DefaultTableModel(headers, 0);
        for (CheckResult r : results) {
            m.addRow(new Object[]{r.getItemName(), r.getValue(), r.getUnit(), r.getAnalysis(), r.getCheckDate()});
        }
        JTable t = new JTable(m);
        UITheme.styleTable(t);
        JScrollPane sp = new JScrollPane(t);
        sp.setPreferredSize(new Dimension(520, 220));

        JPanel panel = new JPanel(new BorderLayout(0, 8));
        JLabel title = new JLabel("「" + a.getUserName() + "」的检查报告明细（" + a.getAppointDate() + "）",
                SwingConstants.CENTER);
        title.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        panel.add(title, BorderLayout.NORTH);
        panel.add(sp, BorderLayout.CENTER);
        if (a.getSuggestion() != null && !a.getSuggestion().isEmpty()) {
            JTextArea ta = new JTextArea(a.getSuggestion());
            ta.setEditable(false);
            ta.setLineWrap(true);
            ta.setWrapStyleWord(true);
            ta.setBackground(new Color(0xF4, 0xF7, 0xFB));
            JScrollPane sug = new JScrollPane(ta);
            sug.setPreferredSize(new Dimension(520, 70));
            sug.setBorder(BorderFactory.createTitledBorder("医生诊断建议"));
            panel.add(sug, BorderLayout.SOUTH);
        }
        JOptionPane.showMessageDialog(this, panel, "报告明细", JOptionPane.PLAIN_MESSAGE);
    }

    /** 医生/管理员填写诊断建议 */
    private void editSuggestion() {
        Appointment a = getSelected();
        if (a == null) return;
        JTextArea ta = new JTextArea(a.getSuggestion() == null ? "" : a.getSuggestion(), 5, 40);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(ta);
        int r = JOptionPane.showConfirmDialog(this, sp, "填写诊断建议", JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) return;
        appointmentDAO.updateSuggestion(a.getId(), ta.getText().trim());
        loadData();
        JOptionPane.showMessageDialog(this, "诊断建议已保存");
    }

    private void export() {
        String[] headers = {"编号", "用户", "体检方式", "检查组", "检查项", "预约日期", "状态", "诊断建议"};
        List<String[]> rows = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String[] row = new String[tableModel.getColumnCount()];
            for (int j = 0; j < tableModel.getColumnCount(); j++) {
                Object v = tableModel.getValueAt(i, j);
                row[j] = v == null ? "" : v.toString();
            }
            rows.add(row);
        }
        ReportUtil.exportHtml("历史检查报告", headers, rows);
    }
}
