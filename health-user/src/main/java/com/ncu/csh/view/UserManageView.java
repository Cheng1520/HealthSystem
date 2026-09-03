package com.ncu.csh.view;

import com.ncu.csh.dao.UserDAO;
import com.ncu.csh.entity.User;
import com.ncu.csh.util.AvatarUtil;
import com.ncu.csh.util.MD5Util;
import com.ncu.csh.util.ReportUtil;
import com.ncu.csh.util.Session;
import com.ncu.csh.util.UITheme;
import com.ncu.csh.util.Validators;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户管理界面 —— 管理员对系统账号的查询 / 新增 / 修改 / 删除 / 导出
 */
public class UserManageView extends JPanel {

    private final UserDAO userDAO = new UserDAO();
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField tfKeyword;
    private int page = 1;
    private final int pageSize = 8;

    public UserManageView() {
        JPanel bg = UITheme.backgroundPanel("bg_panel.png", new Color(0xF5, 0xF9, 0xFF), new Color(0xEA, 0xF4, 0xF8));
        bg.setLayout(new BorderLayout());
        setLayout(new BorderLayout());
        add(bg, BorderLayout.CENTER);
        bg.add(UITheme.gradientHeader("用户管理", "系统账号的查询 · 新增 · 修改 · 删除"), BorderLayout.NORTH);

        // 工具栏
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createEmptyBorder(12, 14, 14, 14));
        toolbar.add(new JLabel("账号/姓名"));
        tfKeyword = UITheme.textField(12);
        toolbar.add(tfKeyword);
        JButton btnSearch = UITheme.blueButton("查询");
        JButton btnReset = UITheme.plainButton("重置");
        JButton btnAdd = UITheme.blueButton("新增用户");
        JButton btnEdit = UITheme.orangeButton("修改");
        JButton btnDelete = UITheme.redButton("删除");
        JButton btnExport = UITheme.plainButton("导出报表");
        JButton btnPrev = UITheme.plainButton("上一页");
        JButton btnNext = UITheme.plainButton("下一页");
        toolbar.add(btnSearch);
        toolbar.add(btnReset);
        toolbar.add(btnAdd);
        toolbar.add(btnEdit);
        toolbar.add(btnDelete);
        toolbar.add(btnExport);
        toolbar.add(btnPrev);
        toolbar.add(btnNext);
        bg.add(toolbar, BorderLayout.SOUTH);

        // 表格
        String[] headers = {"编号", "账号", "姓名", "性别", "年龄", "电话", "角色", "头像"};
        tableModel = new DefaultTableModel(headers, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        UITheme.styleTable(table);
        table.setRowHeight(52);
        table.getColumnModel().getColumn(7).setCellRenderer(new CenterIconRenderer());
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 14, 14, 14));
        bg.add(scrollPane, BorderLayout.CENTER);

        loadData();

        btnSearch.addActionListener(e -> { page = 1; loadData(); });
        btnReset.addActionListener(e -> { tfKeyword.setText(""); page = 1; loadData(); });
        btnAdd.addActionListener(e -> showEditDialog(null));
        btnEdit.addActionListener(e -> {
            User u = getSelectedUser();
            if (u != null) showEditDialog(u);
        });
        btnDelete.addActionListener(e -> deleteSelected());
        btnExport.addActionListener(e -> export());
        btnPrev.addActionListener(e -> { if (page > 1) { page--; loadData(); } });
        btnNext.addActionListener(e -> { page++; loadData(); });
    }

    private void loadData() {
        tableModel.setRowCount(0);
        List<User> list = userDAO.queryByPage(tfKeyword.getText().trim(), page, pageSize);
        for (User u : list) {
            tableModel.addRow(new Object[]{
                    u.getId(), u.getUsername(), u.getRealName(), u.getGender(), u.getAge(), u.getPhone(), u.getRole(),
                    AvatarUtil.loadIcon(u.getAvatar(), 40)
            });
        }
    }

    private User getSelectedUser() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选中一行", "提示", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        User u = new User();
        u.setId((int) tableModel.getValueAt(row, 0));
        u.setUsername((String) tableModel.getValueAt(row, 1));
        u.setRealName((String) tableModel.getValueAt(row, 2));
        u.setGender((String) tableModel.getValueAt(row, 3));
        u.setAge(toInt(tableModel.getValueAt(row, 4)));
        u.setPhone((String) tableModel.getValueAt(row, 5));
        u.setRole((String) tableModel.getValueAt(row, 6));
        return u;
    }

    private Integer toInt(Object o) {
        if (o == null || o.toString().isEmpty()) return null;
        return Integer.parseInt(o.toString());
    }

    private void showEditDialog(User user) {
        boolean isEdit = user != null;
        JTextField tfUsername = new JTextField(isEdit ? user.getUsername() : "", 12);
        JTextField tfRealName = new JTextField(isEdit ? user.getRealName() : "", 12);
        JComboBox<String> cbGender = new JComboBox<>(new String[]{"男", "女"});
        JTextField tfAge = new JTextField(isEdit && user.getAge() != null ? String.valueOf(user.getAge()) : "", 6);
        JTextField tfPhone = new JTextField(isEdit ? user.getPhone() : "", 12);
        JComboBox<String> cbRole = new JComboBox<>(new String[]{"普通用户", "医生", "管理员"});
        JTextField tfPwd = new JTextField("123456", 12);
        if (isEdit) {
            if (user.getGender() != null) cbGender.setSelectedItem(user.getGender());
            if (user.getRole() != null) cbRole.setSelectedItem(user.getRole());
        }

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.add(new JLabel("账号"));
        panel.add(tfUsername);
        panel.add(new JLabel("姓名"));
        panel.add(tfRealName);
        panel.add(new JLabel("性别"));
        panel.add(cbGender);
        panel.add(new JLabel("年龄"));
        panel.add(tfAge);
        panel.add(new JLabel("电话"));
        panel.add(tfPhone);
        panel.add(new JLabel("角色"));
        panel.add(cbRole);

        // 头像：选择本地图片并预览
        JLabel avatarPreview = new JLabel(isEdit ? AvatarUtil.loadIcon(user.getAvatar(), 64)
                : AvatarUtil.defaultIcon(64), SwingConstants.CENTER);
        final File[] chosenAvatar = new File[1];
        JButton btnAvatar = new JButton("选择头像");
        btnAvatar.addActionListener(e -> {
            JFileChooser fc = new JFileChooser();
            fc.setFileFilter(new FileNameExtensionFilter("图片文件 (jpg/png/gif)", "jpg", "jpeg", "png", "gif"));
            if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                File f = fc.getSelectedFile();
                chosenAvatar[0] = f;
                try {
                    Image img = ImageIO.read(f);
                    if (img != null) {
                        avatarPreview.setIcon(new ImageIcon(img.getScaledInstance(64, 64, Image.SCALE_SMOOTH)));
                    }
                } catch (Exception ex) {
                    avatarPreview.setIcon(AvatarUtil.defaultIcon(64));
                }
            }
        });
        JPanel avatarRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        avatarRow.setOpaque(false);
        avatarRow.add(avatarPreview);
        avatarRow.add(btnAvatar);
        panel.add(new JLabel("头像"));
        panel.add(avatarRow);

        if (!isEdit) {
            panel.add(new JLabel("初始密码"));
            panel.add(tfPwd);
        }

        int r = JOptionPane.showConfirmDialog(this, panel, isEdit ? "修改用户" : "新增用户",
                JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) return;

        String username = tfUsername.getText().trim();
        String phone = tfPhone.getText().trim();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "账号不能为空", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!Validators.isValidPhone(phone)) {
            JOptionPane.showMessageDialog(this, "请输入正确格式的手机号", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!isEdit && userDAO.existsByUsername(username)) {
            JOptionPane.showMessageDialog(this, "该账号已存在", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User u = new User();
        u.setId(isEdit ? user.getId() : null);
        u.setUsername(username);
        u.setRealName(tfRealName.getText().trim());
        u.setGender((String) cbGender.getSelectedItem());
        u.setAge(parseInt(tfAge.getText()));
        u.setPhone(phone);
        u.setRole((String) cbRole.getSelectedItem());

        try {
            if (isEdit) {
                userDAO.update(u);
                if (chosenAvatar[0] != null) {
                    String fn = AvatarUtil.save(chosenAvatar[0], u.getId());
                    if (fn != null) userDAO.updateAvatar(u.getId(), fn);
                }
            } else {
                u.setPassword(MD5Util.md5(tfPwd.getText().trim()));
                int newId = userDAO.add(u);
                if (chosenAvatar[0] != null) {
                    String fn = AvatarUtil.save(chosenAvatar[0], newId);
                    if (fn != null) userDAO.updateAvatar(newId, fn);
                }
            }
            loadData();
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private Integer parseInt(String s) {
        s = s.trim();
        if (s.isEmpty()) return null;
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "请先选中一行", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        String username = (String) tableModel.getValueAt(row, 1);
        Integer currentId = Session.currentUser == null ? null : Session.currentUser.getId();
        if (currentId != null && currentId == id) {
            JOptionPane.showMessageDialog(this, "不能删除当前登录账号", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int r = JOptionPane.showConfirmDialog(this, "确定删除用户「" + username + "」？", "确认",
                JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            userDAO.deleteById(id);
            loadData();
        }
    }

    private void export() {
        String[] headers = {"编号", "账号", "姓名", "性别", "年龄", "电话", "角色"};
        List<String[]> rows = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String[] row = new String[7];
            for (int j = 0; j < 7; j++) {
                Object v = tableModel.getValueAt(i, j);
                row[j] = v == null ? "" : v.toString();
            }
            rows.add(row);
        }
        ReportUtil.exportHtml("用户报表", headers, rows);
    }

    /** 头像列渲染器：居中显示，保持斑马纹与选中态 */
    private static class CenterIconRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(SwingConstants.CENTER);
            if (isSelected) {
                setBackground(UITheme.SELECTION);
            } else {
                setBackground(row % 2 == 0 ? Color.WHITE : UITheme.ZEBRA);
            }
            return c;
        }
    }
}
