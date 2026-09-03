package com.ncu.csh.view;

import com.ncu.csh.dao.AppointmentDAO;
import com.ncu.csh.dao.CheckGroupDAO;
import com.ncu.csh.dao.CheckItemDAO;
import com.ncu.csh.dao.UserDAO;
import com.ncu.csh.util.AvatarUtil;
import com.ncu.csh.util.MD5Util;
import com.ncu.csh.util.ReportUtil;
import com.ncu.csh.util.Session;
import com.ncu.csh.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 主界面 —— 左侧导航菜单 + 右侧内容区，点击导航切换模块
 */
public class MainView extends JFrame {

    private final UserDAO userDAO = new UserDAO();
    private final CheckItemDAO checkItemDAO = new CheckItemDAO();
    private final CheckGroupDAO checkGroupDAO = new CheckGroupDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    // 报表统计卡片上的数值标签（用于刷新）
    private JLabel lbUser;
    private JLabel lbItem;
    private JLabel lbGroup;
    private JLabel lbToday;
    private JLabel lbAppoint;

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

    /** 顶部标题栏：左侧标题，右上角 修改密码 / 退出登录 */
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

        JButton btnChangePwd = UITheme.headerButton("修改密码");
        JButton btnLogout = UITheme.headerButton("退出登录");
        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        topRight.setOpaque(false);
        topRight.add(btnChangePwd);
        topRight.add(btnLogout);

        header.add(titleArea, BorderLayout.WEST);
        header.add(topRight, BorderLayout.EAST);

        btnLogout.addActionListener(e -> {
            Session.currentUser = null;
            dispose();
            new LoginView(() -> new MainView().setVisible(true)).setVisible(true);
        });
        btnChangePwd.addActionListener(e -> changePassword());
        return header;
    }

    /** 角色 → 可见模块（三角色权限：管理员 > 医生 > 普通用户） */
    private List<String> roleModules() {
        String role = Session.currentUser.getRole();
        if ("管理员".equals(role)) {
            return Arrays.asList("检查项管理", "检查组管理", "预约与跟踪", "报表统计", "用户管理");
        }
        if ("医生".equals(role)) {
            return Arrays.asList("检查项管理", "检查组管理", "预约与跟踪", "报表统计");
        }
        return Arrays.asList("预约与跟踪", "报表统计");
    }

    /** 模块名 → 导航图标 */
    private String iconFor(String module) {
        switch (module) {
            case "检查项管理": return "📋";
            case "检查组管理": return "📑";
            case "预约与跟踪": return "📅";
            case "报表统计": return "📊";
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

    /** 切换模块：切到报表统计时刷新数字 */
    private void showModule(String cardName) {
        if ("报表统计".equals(cardName)) {
            refreshStats();
        }
        cardLayout.show(content, cardName);
    }

    /** 右侧内容区（CardLayout 承载各模块面板） */
    private JPanel buildContent(List<String> modules) {
        cardLayout = new CardLayout();
        content = new JPanel(cardLayout);
        content.setOpaque(false);

        if (modules.contains("检查项管理")) content.add(new CheckItemView(), "检查项管理");
        if (modules.contains("检查组管理")) content.add(new CheckGroupView(), "检查组管理");
        if (modules.contains("预约与跟踪")) content.add(new AppointmentView(), "预约与跟踪");
        if (modules.contains("报表统计")) content.add(buildReportPanel(), "报表统计");
        if (modules.contains("用户管理")) content.add(new UserManageView(), "用户管理");
        return content;
    }

    /** 报表统计面板：统计卡片 + 一键导出 */
    private JPanel buildReportPanel() {
        JPanel panel = UITheme.backgroundPanel("bg_panel.png",
                new Color(0xF5, 0xF9, 0xFF), new Color(0xEA, 0xF4, 0xF8));
        panel.setLayout(new BorderLayout());
        panel.add(UITheme.gradientHeader("报表统计", "系统各模块数据总览与一键导出"), BorderLayout.NORTH);

        JPanel statPanel = new JPanel(new GridLayout(2, 3, 16, 16));
        statPanel.setOpaque(false);
        statPanel.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));
        lbUser = statValueLabel();
        lbItem = statValueLabel();
        lbGroup = statValueLabel();
        lbToday = statValueLabel();
        lbAppoint = statValueLabel();
        Color c1 = UITheme.PRIMARY;
        Color c2 = new Color(0x3A, 0x68, 0xA8);
        Color c3 = UITheme.SECONDARY;
        Color c4 = new Color(0x5A, 0x8A, 0xC0);
        Color c5 = new Color(0x6B, 0x9A, 0xC8);
        statPanel.add(statCard("用户总数", lbUser, c1));
        statPanel.add(statCard("检查项数", lbItem, c2));
        statPanel.add(statCard("检查组数", lbGroup, c3));
        statPanel.add(statCard("今日预约", lbToday, c4));
        statPanel.add(statCard("预约总数", lbAppoint, c5));

        JButton btnExport = UITheme.primaryButton("一键导出系统报表");
        btnExport.setFont(new Font("Microsoft YaHei", Font.BOLD, 15));
        btnExport.addActionListener(e -> exportReport());
        statPanel.add(btnExport);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(statPanel, BorderLayout.NORTH);
        panel.add(center, BorderLayout.CENTER);
        return panel;
    }

    /** 统计卡片 */
    private JPanel statCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(new Color(255, 255, 255, 235));
        card.setBorder(BorderFactory.createCompoundBorder(
                new UITheme.RoundedBorder(color, 16),
                BorderFactory.createEmptyBorder(20, 22, 20, 22)));

        valueLabel.setForeground(color);

        JLabel titleLabel = new JLabel(title, SwingConstants.LEFT);
        titleLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        titleLabel.setForeground(UITheme.TEXT_GRAY);

        card.add(valueLabel, BorderLayout.CENTER);
        card.add(titleLabel, BorderLayout.SOUTH);
        return card;
    }

    /** 统计数值标签 */
    private JLabel statValueLabel() {
        JLabel l = new JLabel("0", SwingConstants.LEFT);
        l.setFont(new Font("Microsoft YaHei", Font.BOLD, 34));
        return l;
    }

    /** 重新查询并刷新统计数字 */
    private void refreshStats() {
        if (lbUser == null) {
            return;
        }
        lbUser.setText(String.valueOf(userDAO.countAll()));
        lbItem.setText(String.valueOf(checkItemDAO.countAll()));
        lbGroup.setText(String.valueOf(checkGroupDAO.countAll()));
        lbToday.setText(String.valueOf(appointmentDAO.countToday()));
        lbAppoint.setText(String.valueOf(appointmentDAO.countAll()));
    }

    /** 每次显示主界面时刷新统计 */
    @Override
    public void setVisible(boolean b) {
        if (b) {
            refreshStats();
        }
        super.setVisible(b);
    }

    /** 导出系统数据报表：汇总各模块数据 */
    private void exportReport() {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"用户总数", String.valueOf(userDAO.countAll()), "用户管理模块", ""});
        rows.add(new String[]{"检查项数", String.valueOf(checkItemDAO.countAll()), "检查项管理模块", ""});
        rows.add(new String[]{"检查组数", String.valueOf(checkGroupDAO.countAll()), "检查组管理模块", ""});
        rows.add(new String[]{"预约总数", String.valueOf(appointmentDAO.countAll()), "预约与跟踪模块", ""});
        rows.add(new String[]{"今日预约", String.valueOf(appointmentDAO.countToday()), "预约与跟踪模块", ""});
        ReportUtil.exportHtml("健康管理系统总览报表",
                new String[]{"统计项", "数值", "所属模块", "备注"}, rows);
    }

    private void changePassword() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 8, 8));
        JPasswordField tfOld = new JPasswordField();
        JPasswordField tfNew = new JPasswordField();
        JPasswordField tfNew2 = new JPasswordField();
        panel.add(new JLabel("原密码"));
        panel.add(tfOld);
        panel.add(new JLabel("新密码"));
        panel.add(tfNew);
        panel.add(new JLabel("确认新密码"));
        panel.add(tfNew2);

        int r = JOptionPane.showConfirmDialog(this, panel, "修改密码", JOptionPane.OK_CANCEL_OPTION);
        if (r != JOptionPane.OK_OPTION) {
            return;
        }
        String oldPwd = new String(tfOld.getPassword());
        String newPwd = new String(tfNew.getPassword());
        String newPwd2 = new String(tfNew2.getPassword());

        if (!Session.currentUser.getPassword().equals(MD5Util.md5(oldPwd))) {
            JOptionPane.showMessageDialog(this, "原密码错误", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (newPwd.isEmpty() || !newPwd.equals(newPwd2)) {
            JOptionPane.showMessageDialog(this, "新密码为空或不一致", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }
        userDAO.updatePassword(Session.currentUser.getId(), MD5Util.md5(newPwd));
        Session.currentUser.setPassword(MD5Util.md5(newPwd));
        JOptionPane.showMessageDialog(this, "密码修改成功");
    }
}
