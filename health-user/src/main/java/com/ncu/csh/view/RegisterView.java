package com.ncu.csh.view;

import com.ncu.csh.dao.UserDAO;
import com.ncu.csh.entity.User;
import com.ncu.csh.util.MD5Util;
import com.ncu.csh.util.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * 注册界面 —— 登录注册模块
 */
public class RegisterView extends JFrame {

    private final UserDAO userDAO = new UserDAO();
    private JTextField tfUsername;
    private JPasswordField tfPassword;
    private JPasswordField tfPassword2;
    private JTextField tfRealName;
    private JComboBox<String> cbGender;
    private JTextField tfAge;
    private JTextField tfPhone;

    /** 登录成功后的跳转动作（透传给登录界面） */
    private final Runnable onLoginSuccess;

    public RegisterView(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
        setTitle("健康管理系统 - 注册");
        setSize(460, 620);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel bg = UITheme.backgroundPanel("bg_login.png", new Color(0xF0, 0xF6, 0xFF), new Color(0xDF, 0xEE, 0xFB));
        bg.setLayout(new BorderLayout());
        setContentPane(bg);
        bg.add(UITheme.gradientHeader("用户注册", "创建新账号"), BorderLayout.NORTH);

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(new Color(255, 255, 255, 245));
        card.setBorder(BorderFactory.createCompoundBorder(UITheme.cardBorder(),
                BorderFactory.createEmptyBorder(20, 28, 20, 28)));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(new Color(255, 255, 255, 245));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 4, 7, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        tfUsername = new JTextField(16);
        tfPassword = new JPasswordField(16);
        tfPassword2 = new JPasswordField(16);
        tfRealName = new JTextField(16);
        cbGender = new JComboBox<>(new String[]{"男", "女"});
        tfAge = new JTextField(16);
        tfPhone = new JTextField(16);

        String[] labels = {"用户名*", "密码*", "确认密码*", "真实姓名", "性别", "年龄", "电话*"};
        Component[] comps = {tfUsername, tfPassword, tfPassword2, tfRealName, cbGender, tfAge, tfPhone};
        for (int i = 0; i < labels.length; i++) {
            gbc.gridx = 0;
            gbc.gridy = i;
            gbc.anchor = GridBagConstraints.EAST;
            form.add(new JLabel(labels[i]), gbc);
            gbc.gridx = 1;
            gbc.anchor = GridBagConstraints.WEST;
            form.add(comps[i], gbc);
        }

        JButton btnSubmit = UITheme.primaryButton("注 册");
        btnSubmit.setPreferredSize(new Dimension(0, 40));
        gbc.gridx = 0;
        gbc.gridy = labels.length;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        form.add(btnSubmit, gbc);
        gbc.gridwidth = 1;

        JButton btnBack = new JButton("返回登录");
        btnBack.setBorderPainted(false);
        btnBack.setContentAreaFilled(false);
        btnBack.setForeground(UITheme.PRIMARY);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = labels.length + 1;
        form.add(btnBack, gbc);

        card.add(form);
        JPanel wrap = new JPanel(new GridBagLayout());
        wrap.setOpaque(false);
        wrap.setBorder(BorderFactory.createEmptyBorder(16, 40, 30, 40));
        wrap.add(card);
        bg.add(wrap, BorderLayout.CENTER);

        btnSubmit.addActionListener(e -> doRegister());
        btnBack.addActionListener(e -> {
            dispose();
            new LoginView(onLoginSuccess).setVisible(true);
        });
    }

    private void doRegister() {
        String username = tfUsername.getText().trim();
        String pwd = new String(tfPassword.getPassword());
        String pwd2 = new String(tfPassword2.getPassword());
        String phone = tfPhone.getText().trim();

        if (username.isEmpty() || pwd.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "用户名、密码、电话为必填项", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!pwd.equals(pwd2)) {
            JOptionPane.showMessageDialog(this, "两次输入的密码不一致", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            if (userDAO.existsByUsername(username)) {
                JOptionPane.showMessageDialog(this, "该用户名已被注册", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            User u = new User();
            u.setUsername(username);
            u.setPassword(MD5Util.md5(pwd));
            u.setRealName(tfRealName.getText().trim());
            u.setGender((String) cbGender.getSelectedItem());
            String ageStr = tfAge.getText().trim();
            u.setAge(ageStr.isEmpty() ? null : Integer.parseInt(ageStr));
            u.setPhone(phone);

            userDAO.register(u);
            JOptionPane.showMessageDialog(this, "注册成功，请登录");
            dispose();
            new LoginView(onLoginSuccess).setVisible(true);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "年龄必须是数字", "提示", JOptionPane.WARNING_MESSAGE);
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}
