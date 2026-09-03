package com.ncu.csh.view;

import com.ncu.csh.dao.UserDAO;
import com.ncu.csh.entity.User;
import com.ncu.csh.util.AvatarUtil;
import com.ncu.csh.util.MD5Util;
import com.ncu.csh.util.Session;
import com.ncu.csh.util.UITheme;
import com.ncu.csh.util.Validators;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * 主界面 —— 左侧导航菜单 + 右侧内容区，点击导航切换模块
 */
public class MainView extends JFrame {

    private final UserDAO userDAO = new UserDAO();

    private JPanel content;
    private CardLayout cardLayout;
    private JButton activeNavButton;

    public MainView() {
        setTitle("健康管理系统");
        setSize(1200, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        String displayName = Session.currentUser.getRealName() == null
                ? Session.currentUser.getUsername() : Session.currentUser.getRealName();
        String role = Session.currentUser.getRole();
        String roleText = (role == null || role.isEmpty()) ? "" : "（" + role + "）";
        List<String> modules = roleModules();
        String defaultCard = modules.get(0);

        // 背景面板
        JPanel bg = UITheme.backgroundPanel("bg_main.png",
                new Color(0xF2, 0xF7, 0xFF), new Color(0xE2, 0xF2, 0xF6));
        bg.setLayout(new BorderLayout());
        setContentPane(bg);

        // 顶部标题栏
        bg.add(buildHeader(displayName, roleText), BorderLayout.NORTH);

        // 主体：左侧导航 + 右侧内容
        JPanel body = new JPanel(new BorderLayout(12, 0));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        body.add(buildNav(modules, defaultCard), BorderLayout.WEST);
        body.add(buildContent(modules), BorderLayout.CENTER);
        bg.add(body, BorderLayout.CENTER);

        cardLayout.show(content, defaultCard);
    }

    /** 顶部标题栏：左侧标题，右上角 修改个人信息 / 退出登录 */
    private JPanel buildHeader(String displayName, String roleText) {
        JPanel header = UITheme.gradientHeader("健康管理系统", "欢迎，" + displayName + roleText);
        header.setLayout(new BorderLayout());

        JLabel titleArea = new JLabel();
        titleArea.setLayout(new BoxLayout(titleArea, BoxLayout.Y_AXIS));
        titleArea.setOpaque(false);
        JLabel t1 = new JLabel("健康管理系统");
        t1.setFont(UITheme.FONT_BANNER);
        t1.setForeground(Color.WHITE);
        JLabel t2 = new JLabel("欢迎，" + displayName + roleText + "   |   祝您健康每一天");
        t2.setFont(UITheme.FONT_SUBTITLE);
        t2.setForeground(new Color(255, 255, 255, 200));
        titleArea.add(t1);
        titleArea.add(t2);

        JButton btnEditProfile = UITheme.headerButton("修改个人信息");
        JButton btnLogout = UITheme.headerButton("退出登录");
        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        topRight.setOpaque(false);
        topRight.add(btnEditProfile);
        topRight.add(btnLogout);

        header.add(titleArea, BorderLayout.WEST);
        header.add(topRight, BorderLayout.EAST);

        btnLogout.addActionListener(e -> {
            Session.currentUser = null;
            dispose();
            new LoginView(() -> new MainView().setVisible(true)).setVisible(true);
        });
        btnEditProfile.addActionListener(e -> editProfile());
        return header;
    }

    /** 角色 → 可见模块（三角色权限：管理员 > 医生 > 普通用户） */
    private List<String> roleModules() {
        String role = Session.currentUser.getRole();
        if ("管理员".equals(role)) {
            return Arrays.asList("首页", "检查项管理", "检查组管理", "预约与跟踪", "历史检查报告", "用户管理");
        }
        if ("医生".equals(role)) {
            return Arrays.asList("首页", "检查项管理", "检查组管理", "预约与跟踪", "历史检查报告");
        }
        return Arrays.asList("首页", "预约与跟踪", "历史检查报告");
    }

    /** 模块名 → 导航图标 */
    private String iconFor(String module) {
        switch (module) {
            case "首页": return "🏠";
            case "检查项管理": return "📋";
            case "检查组管理": return "📑";
            case "预约与跟踪": return "📅";
            case "历史检查报告": return "🩺";
            case "用户管理": return "👤";
            default: return "•";
        }
    }

    /** 左侧导航菜单：按角色显示模块入口 */
    private JPanel buildNav(List<String> modules, String defaultCard) {
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setOpaque(false);
        nav.setPreferredSize(new Dimension(210, 0));

        nav.add(buildUserCard());
        nav.add(Box.createVerticalStrut(14));

        boolean first = true;
        for (String module : modules) {
            if (!first) {
                nav.add(Box.createVerticalStrut(10));
            }
            nav.add(navButton(iconFor(module), module, module, defaultCard));
            first = false;
        }
        nav.add(Box.createVerticalGlue());
        return nav;
    }

    /** 侧边导航按钮：统一主题色，图标 + 文字，当前项高亮 */
    private JButton navButton(String icon, String text, String cardName, String defaultCard) {
        JButton b = new JButton(icon + "  " + text);
        b.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        b.setFocusPainted(false);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        b.setPreferredSize(new Dimension(210, 40));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> {
            setActiveNav(b);
            showModule(cardName);
        });
        if (cardName.equals(defaultCard)) {
            setActiveNav(b);
        } else {
            setInactiveNav(b);
        }
        return b;
    }

    private void setActiveNav(JButton b) {
        if (activeNavButton != null && activeNavButton != b) {
            setInactiveNav(activeNavButton);
        }
        activeNavButton = b;
        b.setBackground(UITheme.PRIMARY);
        b.setForeground(Color.WHITE);
    }

    private void setInactiveNav(JButton b) {
        b.setBackground(UITheme.BLUE_SOFT);
        b.setForeground(UITheme.PRIMARY);
    }

    /** 侧边栏顶部的欢迎卡片 */
    private JPanel buildUserCard() {
        String name = Session.currentUser.getRealName() == null
                ? Session.currentUser.getUsername() : Session.currentUser.getRealName();
        String role = Session.currentUser.getRole();
        String roleText = (role == null || role.isEmpty()) ? "普通用户" : role;

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(255, 255, 255, 235));
        card.setBorder(BorderFactory.createCompoundBorder(
                new UITheme.RoundedBorder(new Color(0xE2, 0xE8, 0xF0), 14),
                BorderFactory.createEmptyBorder(16, 12, 16, 12)));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 170));

        JLabel avatar = new JLabel(AvatarUtil.loadIcon(Session.currentUser.getAvatar(), 60),
                SwingConstants.CENTER);
        avatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        avatar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        avatar.setToolTipText("点击查看个人信息");
        avatar.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                showPersonalInfo();
            }
        });

        JLabel welcome = new JLabel("欢迎登录", SwingConstants.CENTER);
        welcome.setFont(new Font("Microsoft YaHei", Font.BOLD, 15));
        welcome.setForeground(UITheme.PRIMARY);
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel info = new JLabel(name + " · " + roleText, SwingConstants.CENTER);
        info.setFont(new Font("Microsoft YaHei", Font.PLAIN, 12));
        info.setForeground(UITheme.TEXT_GRAY);
        info.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(avatar);
        card.add(Box.createVerticalStrut(8));
        card.add(welcome);
        card.add(Box.createVerticalStrut(6));
        card.add(info);
        return card;
    }

    /** 切换模块 */
    private void showModule(String cardName) {
        cardLayout.show(content, cardName);
    }

    /** 右侧内容区（CardLayout 承载各模块面板） */
    private JPanel buildContent(List<String> modules) {
        cardLayout = new CardLayout();
        content = new JPanel(cardLayout);
        content.setOpaque(false);

        if (modules.contains("首页")) content.add(new HomeView(), "首页");
        if (modules.contains("检查项管理")) content.add(new CheckItemView(), "检查项管理");
        if (modules.contains("检查组管理")) content.add(new CheckGroupView(), "检查组管理");
        if (modules.contains("预约与跟踪")) content.add(new AppointmentView(), "预约与跟踪");
        if (modules.contains("历史检查报告")) content.add(new HistoryReportView(), "历史检查报告");
        if (modules.contains("用户管理")) content.add(new UserManageView(), "用户管理");
        return content;
    }

    /** 点击头像查看个人信息 */
    private void showPersonalInfo() {
        User u = Session.currentUser;
        String info = "<html><b>账号：</b>" + u.getUsername()
                + "<br><b>姓名：</b>" + (u.getRealName() == null ? "未填写" : u.getRealName())
                + "<br><b>性别：</b>" + (u.getGender() == null ? "未填写" : u.getGender())
                + "<br><b>年龄：</b>" + (u.getAge() == null ? "未填写" : u.getAge())
                + "<br><b>电话：</b>" + (u.getPhone() == null ? "未填写" : u.getPhone())
                + "<br><b>角色：</b>" + (u.getRole() == null ? "" : u.getRole())
                + "</html>";
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.add(new JLabel(AvatarUtil.loadIcon(u.getAvatar(), 72), SwingConstants.CENTER), BorderLayout.WEST);
        panel.add(new JLabel(info), BorderLayout.CENTER);
        JOptionPane.showMessageDialog(this, panel, "个人信息", JOptionPane.PLAIN_MESSAGE);
    }

    /** 修改个人信息：编辑账号、姓名、性别、年龄、电话，并可修改密码 */
    private void editProfile() {
        User u = Session.currentUser;
        JTextField tfUsername = new JTextField(u.getUsername(), 12);
        JTextField tfRealName = new JTextField(u.getRealName(), 12);
        JComboBox<String> cbGender = new JComboBox<>(new String[]{"男", "女"});
        if (u.getGender() != null) {
            cbGender.setSelectedItem(u.getGender());
        }
        JTextField tfAge = new JTextField(u.getAge() == null ? "" : String.valueOf(u.getAge()), 6);
        JTextField tfPhone = new JTextField(u.getPhone() == null ? "" : u.getPhone(), 12);
        JPasswordField tfPwd = new JPasswordField(12);

        // 头像：选择本地图片并预览
        JLabel avatarPreview = new JLabel(AvatarUtil.loadIcon(u.getAvatar(), 48), SwingConstants.CENTER);
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
                        avatarPreview.setIcon(new ImageIcon(img.getScaledInstance(48, 48, Image.SCALE_SMOOTH)));
                    }
                } catch (Exception ex) {
                    avatarPreview.setIcon(AvatarUtil.defaultIcon(48));
                }
            }
        });
        JPanel avatarRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        avatarRow.setOpaque(false);
        avatarRow.add(avatarPreview);
        avatarRow.add(btnAvatar);

        JPanel panel = new JPanel(new GridLayout(0, 2, 8, 8));
        panel.add(new JLabel("用户名"));
        panel.add(tfUsername);
        panel.add(new JLabel("真实姓名"));
        panel.add(tfRealName);
        panel.add(new JLabel("性别"));
        panel.add(cbGender);
        panel.add(new JLabel("年龄"));
        panel.add(tfAge);
        panel.add(new JLabel("电话"));
        panel.add(tfPhone);
        panel.add(new JLabel("新密码(留空不修改)"));
        panel.add(tfPwd);
        panel.add(new JLabel("头像"));
        panel.add(avatarRow);

        int r = JOptionPane.showConfirmDialog(this, panel, "修改个人信息", JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) {
            return;
        }

        String username = tfUsername.getText().trim();
        if (username.isEmpty()) {
            JOptionPane.showMessageDialog(this, "用户名不能为空", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (!username.equals(u.getUsername()) && userDAO.existsByUsername(username)) {
            JOptionPane.showMessageDialog(this, "该用户名已被使用", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Integer age = null;
        String ageStr = tfAge.getText().trim();
        if (!ageStr.isEmpty()) {
            try {
                age = Integer.parseInt(ageStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "年龄必须是数字", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
        }

        String realName = tfRealName.getText().trim();
        String gender = (String) cbGender.getSelectedItem();
        String phone = tfPhone.getText().trim();
        String newPwd = new String(tfPwd.getPassword());

        if (!Validators.isValidPhone(phone)) {
            JOptionPane.showMessageDialog(this, "请输入正确格式的手机号", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        User updated = new User();
        updated.setId(u.getId());
        updated.setUsername(username);
        updated.setRealName(realName);
        updated.setGender(gender);
        updated.setAge(age);
        updated.setPhone(phone);

        try {
            userDAO.updateProfile(updated);
            if (chosenAvatar[0] != null) {
                String fn = AvatarUtil.save(chosenAvatar[0], u.getId());
                if (fn != null) {
                    userDAO.updateAvatar(u.getId(), fn);
                    updated.setAvatar(fn);
                }
            }
            if (!newPwd.isEmpty()) {
                userDAO.updatePassword(u.getId(), MD5Util.md5(newPwd));
            }
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        u.setUsername(username);
        u.setRealName(realName);
        u.setGender(gender);
        u.setAge(age);
        u.setPhone(phone);
        if (updated.getAvatar() != null) {
            u.setAvatar(updated.getAvatar());
        }
        if (!newPwd.isEmpty()) {
            u.setPassword(MD5Util.md5(newPwd));
        }
        JOptionPane.showMessageDialog(this, "个人信息已更新");
        dispose();
        new MainView().setVisible(true);
    }
}
