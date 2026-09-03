package com.ncu.csh.view;

import com.ncu.csh.dao.UserDAO;
import com.ncu.csh.entity.User;
import com.ncu.csh.util.MD5Util;
import com.ncu.csh.util.Session;
import com.ncu.csh.util.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * 登录界面 —— 登录注册模块
 */
public class LoginView extends JFrame {

    private final UserDAO userDAO = new UserDAO();
    private JTextField tfUsername;
    private JPasswordField tfPassword;
    private JComboBox<String> cbRole;

    /** 登录成功后的跳转动作（由入口 App 注入，用于解耦与主界面的循环依赖） */
    private final Runnable onLoginSuccess;

    public LoginView(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
        setTitle("健康管理系统 - 登录");
        setSize(420, 570);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        JPanel bg = UITheme.backgroundPanel("bg_login.png", new Color(0xF0, 0xF6, 0xFF), new Color(0xDF, 0xEE, 0xFB));
        bg.setLayout(new BorderLayout());
        setContentPane(bg);

        // 顶部渐变标题
        bg.add(UITheme.gradientHeader("健康管理系统", "Health Management System"), BorderLayout.NORTH);

        // 中间登录卡片
        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(new Color(255, 255, 255, 245));
        card.setBorder(BorderFactory.createCompoundBorder(UITheme.cardBorder(),
                BorderFactory.createEmptyBorder(28, 36, 28, 36)));

        JPanel inner = new JPanel(new GridBagLayout());
        inner.setBackground(new Color(255, 255, 255, 245));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 0, 8, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel loginTitle = new JLabel("用户登录", SwingConstants.CENTER);
        loginTitle.setFont(new Font("Microsoft YaHei", Font.BOLD, 20));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        inner.add(loginTitle, gbc);
        gbc.gridwidth = 1;

        gbc.gridy = 1;
        gbc.gridx = 0;
        inner.add(new JLabel("账号/电话"), gbc);
        gbc.gridx = 1;
        tfUsername = new JTextField(16);
        inner.add(tfUsername, gbc);

        gbc.gridy = 2;
        gbc.gridx = 0;
        inner.add(new JLabel("密码"), gbc);
        gbc.gridx = 1;
        tfPassword = new JPasswordField(16);
        inner.add(tfPassword, gbc);

        gbc.gridy = 3;
        gbc.gridx = 0;
        inner.add(new JLabel("身份"), gbc);
        gbc.gridx = 1;
        cbRole = new JComboBox<>(new String[]{"全部", "普通用户", "医生", "管理员"});
        inner.add(cbRole, gbc);

        JButton btnLogin = UITheme.primaryButton("登 录");
        btnLogin.setPreferredSize(new Dimension(0, 42));
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        inner.add(btnLogin, gbc);

        JPanel linkPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        linkPanel.setBackground(new Color(255, 255, 255, 245));
        JButton btnRegister = new JButton("注册账号");
        JButton btnForget = new JButton("忘记密码");
        btnRegister.setBorderPainted(false);
        btnRegister.setContentAreaFilled(false);
        btnRegister.setForeground(UITheme.PRIMARY);
        btnRegister.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnForget.setBorderPainted(false);
        btnForget.setContentAreaFilled(false);
        btnForget.setForeground(UITheme.TEXT_GRAY);
        btnForget.setCursor(new Cursor(Cursor.HAND_CURSOR));
        linkPanel.add(btnRegister);
        linkPanel.add(btnForget);
        gbc.gridy = 5;
        inner.add(linkPanel, gbc);

        card.add(inner);

        // 卡片外边距
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createEmptyBorder(20, 40, 40, 40));
        wrap.add(card);
        bg.add(wrap, BorderLayout.CENTER);

        // 事件
        btnLogin.addActionListener(e -> doLogin());
        tfPassword.addActionListener(e -> doLogin());
        btnRegister.addActionListener(e -> {
            dispose();
            new RegisterView(onLoginSuccess).setVisible(true);
        });
        btnForget.addActionListener(e -> forgetPassword());
    }

    private void doLogin() {
        String account = tfUsername.getText().trim();
        String password = new String(tfPassword.getPassword());
        String role = (String) cbRole.getSelectedItem();
        if ("全部".equals(role)) {
            role = null;
        }
        if (account.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "账号和密码不能为空", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            User user = userDAO.loginByAccount(account, MD5Util.md5(password), role);
            if (user == null) {
                JOptionPane.showMessageDialog(this, "账号或密码错误，或身份不匹配", "登录失败", JOptionPane.ERROR_MESSAGE);
                return;
            }
            Session.currentUser = user;
            JOptionPane.showMessageDialog(this, "欢迎，" + (user.getRealName() == null ? user.getUsername() : user.getRealName()));
            dispose();
            if (onLoginSuccess != null) {
                onLoginSuccess.run();
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void forgetPassword() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        JTextField tfUser = new JTextField();
        JTextField tfPhone = new JTextField();
        panel.add(new JLabel("用户名"));
        panel.add(tfUser);
        panel.add(new JLabel("注册电话"));
        panel.add(tfPhone);
        panel.add(new JLabel("新密码"));
        JPasswordField tfNewPwd = new JPasswordField();
        panel.add(tfNewPwd);

        int r = JOptionPane.showConfirmDialog(this, panel, "忘记密码", JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) {
            return;
        }
        String username = tfUser.getText().trim();
        String phone = tfPhone.getText().trim();
        String newPwd = new String(tfNewPwd.getPassword());
        if (username.isEmpty() || phone.isEmpty() || newPwd.isEmpty()) {
            JOptionPane.showMessageDialog(this, "请填写完整信息", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            User user = userDAO.findByUsernameAndPhone(username, phone);
            if (user == null) {
                JOptionPane.showMessageDialog(this, "用户名与电话不匹配", "找回失败", JOptionPane.ERROR_MESSAGE);
                return;
            }
            userDAO.updatePassword(user.getId(), MD5Util.md5(newPwd));
            JOptionPane.showMessageDialog(this, "密码已重置，请用新密码登录");
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
