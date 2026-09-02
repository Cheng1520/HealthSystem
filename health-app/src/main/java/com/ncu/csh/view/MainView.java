package com.ncu.csh.view;

import com.ncu.csh.dao.AppointmentDAO;
import com.ncu.csh.dao.CheckGroupDAO;
import com.ncu.csh.dao.CheckItemDAO;
import com.ncu.csh.dao.UserDAO;
import com.ncu.csh.util.MD5Util;
import com.ncu.csh.util.ReportUtil;
import com.ncu.csh.util.Session;
import com.ncu.csh.util.UITheme;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
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

    public MainView() {
        setTitle("健康管理系统");
        setSize(1200, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        String displayName = Session.currentUser.getRealName() == null
                ? Session.currentUser.getUsername() : Session.currentUser.getRealName();
        String role = Session.currentUser.getRole();
        boolean isAdmin = "管理员".equals(role);
        String roleText = (role == null || role.isEmpty()) ? "" : "（" + role + "）";

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
        body.add(buildNav(isAdmin), BorderLayout.WEST);
        body.add(buildContent(isAdmin), BorderLayout.CENTER);
        bg.add(body, BorderLayout.CENTER);

        cardLayout.show(content, isAdmin ? "检查项管理" : "预约与跟踪");
    }

    /** 顶部标题栏：左侧标题，右上角 修改密码 / 退出登录 */
    private JPanel buildHeader(String displayName, String roleText) {
        JPanel header = UITheme.gradientHeader("健康管理系统", "欢迎，" + displayName + roleText);
        header.setLayout(new BorderLayout());

        JLabel titleArea = new JLabel();
        titleArea.setLayout(new BoxLayout(titleArea, BoxLayout.Y_AXIS));
        titleArea.setOpaque(false);
        JLabel t1 = new JLabel("健康管理系统");
        t1.setFont(new Font("Microsoft YaHei", Font.BOLD, 24));
        t1.setForeground(Color.WHITE);
        JLabel t2 = new JLabel("欢迎，" + displayName + roleText + "   |   祝您健康每一天");
        t2.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        t2.setForeground(new Color(255, 255, 255, 210));
        titleArea.add(t1);
        titleArea.add(t2);

        JButton btnChangePwd = headerLinkButton("修改密码");
        JButton btnLogout = headerLinkButton("退出登录");
        JPanel topRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
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

    /** 左侧导航菜单：按角色显示模块入口 */
    private JPanel buildNav(boolean isAdmin) {
        JPanel nav = new JPanel();
        nav.setLayout(new BoxLayout(nav, BoxLayout.Y_AXIS));
        nav.setOpaque(false);
        nav.setPreferredSize(new Dimension(200, 0));

        if (isAdmin) {
            nav.add(navButton("检查项管理", UITheme.TEAL, "检查项管理"));
            nav.add(Box.createVerticalStrut(12));
            nav.add(navButton("检查组管理", UITheme.PRIMARY, "检查组管理"));
            nav.add(Box.createVerticalStrut(12));
        }
        nav.add(navButton("预约与跟踪", UITheme.ORANGE, "预约与跟踪"));
        nav.add(Box.createVerticalStrut(12));
        nav.add(navButton("报表统计", UITheme.GREEN, "报表统计"));
        if (isAdmin) {
            nav.add(Box.createVerticalStrut(12));
            nav.add(navButton("用户管理", UITheme.PURPLE, "用户管理"));
        }
        nav.add(Box.createVerticalGlue());
        return nav;
    }

    /** 彩色导航按钮 */
    private JButton navButton(String text, Color color, String cardName) {
        JButton b = new JButton(text);
        b.setBackground(color);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Microsoft YaHei", Font.BOLD, 15));
        b.setFocusPainted(false);
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        b.setPreferredSize(new Dimension(200, 40));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.addActionListener(e -> showModule(cardName));
        return b;
    }

    /** 切换模块：切到报表统计时刷新数字 */
    private void showModule(String cardName) {
        if ("报表统计".equals(cardName)) {
            refreshStats();
        }
        cardLayout.show(content, cardName);
    }

    /** 右侧内容区（CardLayout 承载各模块面板） */
    private JPanel buildContent(boolean isAdmin) {
        cardLayout = new CardLayout();
        content = new JPanel(cardLayout);
        content.setOpaque(false);

        if (isAdmin) {
            content.add(new CheckItemView(), "检查项管理");
            content.add(new CheckGroupView(), "检查组管理");
        }
        content.add(new AppointmentView(), "预约与跟踪");
        content.add(buildReportPanel(), "报表统计");
        if (isAdmin) {
            content.add(new UserManageView(), "用户管理");
        }
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
        statPanel.add(statCard("用户总数", lbUser, UITheme.PRIMARY));
        statPanel.add(statCard("检查项数", lbItem, UITheme.TEAL));
        statPanel.add(statCard("检查组数", lbGroup, UITheme.ORANGE));
        statPanel.add(statCard("今日预约", lbToday, UITheme.PURPLE));
        statPanel.add(statCard("预约总数", lbAppoint, UITheme.GREEN));

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

    /** 右上角白色文字按钮 */
    private JButton headerLinkButton(String text) {
        JButton b = new JButton(text);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Microsoft YaHei", Font.PLAIN, 14));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    /** 彩色统计卡片 */
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
