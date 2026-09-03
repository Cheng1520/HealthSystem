package com.ncu.csh.util;

import com.formdev.flatlaf.FlatLightLaf;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

/**
 * 界面主题工具类 —— 统一深蓝配色、微软雅黑字体、圆角、表格样式，让界面美观统一。
 */
public class UITheme {

    // 主题色板（深蓝主题）
    public static final Color PRIMARY       = new Color(0x2B, 0x57, 0x97); // 主色 深蓝 #2b5797
    public static final Color PRIMARY_DARK  = new Color(0x23, 0x4A, 0x82); // 深蓝（渐变尾）
    public static final Color SECONDARY     = new Color(0x4A, 0x7A, 0xBC); // 次要 浅蓝 #4a7abc
    public static final Color BLUE_SOFT     = new Color(0xE8, 0xF0, 0xFA); // 极浅蓝（导航未选中）
    public static final Color ORANGE        = new Color(0xE8, 0x94, 0x3A); // 浅橙（修改）
    public static final Color RED           = new Color(0xD9, 0x6C, 0x66); // 浅红（删除）
    public static final Color GRAY_BTN_BG   = new Color(0xED, 0xF1, 0xF6); // 普通按钮浅灰
    public static final Color GRAY_BTN_FG   = new Color(0x3A, 0x4A, 0x5A);
    public static final Color BG            = new Color(0xF4, 0xF6, 0xFB); // 页面背景
    public static final Color TEXT_GRAY     = new Color(0x6B, 0x72, 0x80); // 次要文字
    public static final Color ZEBRA         = new Color(0xF4, 0xF7, 0xFB); // 表格斑马纹
    public static final Color SELECTION     = new Color(0xD6, 0xE4, 0xF6); // 表格选中浅蓝
    public static final Color TEXT_MAIN     = new Color(0x2F, 0x3A, 0x4A); // 正文深灰
    public static final Color BORDER_LIGHT  = new Color(0xE2, 0xE8, 0xF0);

    // 统一字体（微软雅黑）
    public static final Font FONT_BANNER   = new Font("Microsoft YaHei", Font.BOLD, 18);
    public static final Font FONT_TITLE    = new Font("Microsoft YaHei", Font.BOLD, 16);
    public static final Font FONT_SUBTITLE = new Font("Microsoft YaHei", Font.PLAIN, 14);
    public static final Font FONT_BODY     = new Font("Microsoft YaHei", Font.PLAIN, 13);
    public static final Font FONT_HEADER   = new Font("Microsoft YaHei", Font.BOLD, 13);

    /** 应用全局主题，须在创建任何界面之前调用 */
    public static void apply() {
        FlatLightLaf.setup();
        UIManager.put("defaultFont", FONT_BODY);
        UIManager.put("Label.font", FONT_BODY);
        UIManager.put("Button.font", FONT_BODY);
        UIManager.put("Table.font", FONT_BODY);
        UIManager.put("TableHeader.font", FONT_HEADER);
        UIManager.put("TextField.font", FONT_BODY);
        UIManager.put("ComboBox.font", FONT_BODY);
        UIManager.put("PasswordField.font", FONT_BODY);

        // 强调色与圆角
        UIManager.put("Component.focusColor", SECONDARY);
        UIManager.put("Button.arc", 8);
        UIManager.put("Component.arc", 8);
        UIManager.put("TextComponent.arc", 8);

        // 表格默认样式（具体表格会经 styleTable 二次强化）
        UIManager.put("Table.background", Color.WHITE);
        UIManager.put("Table.foreground", TEXT_MAIN);
        UIManager.put("Table.selectionBackground", SELECTION);
        UIManager.put("Table.selectionForeground", TEXT_MAIN);
        UIManager.put("TableHeader.background", PRIMARY);
        UIManager.put("TableHeader.foreground", Color.WHITE);
        UIManager.put("Table.gridColor", new Color(0xED, 0xF1, 0xF5));
        UIManager.put("TableHeader.separatorColor", PRIMARY_DARK);
        UIManager.put("Table.rowHeight", 28);
    }

    /** 主色按钮（深蓝，用于登录/注册/一键导出等主操作） */
    public static JButton primaryButton(String text) {
        return filledButton(text, PRIMARY);
    }

    /** 柔和蓝按钮（查询 / 新增） */
    public static JButton blueButton(String text) {
        return filledButton(text, SECONDARY);
    }

    /** 浅橙按钮（修改） */
    public static JButton orangeButton(String text) {
        return filledButton(text, ORANGE);
    }

    /** 浅红按钮（删除） */
    public static JButton redButton(String text) {
        return filledButton(text, RED);
    }

    /** 普通浅灰按钮（导出、分页、重置等中性操作） */
    public static JButton plainButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(GRAY_BTN_BG);
        b.setForeground(GRAY_BTN_FG);
        b.setFont(FONT_BODY);
        b.setFocusPainted(false);
        styleButton(b);
        return b;
    }

    /** 顶部标题栏上的小按钮（白底深蓝字） */
    public static JButton headerButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(Color.WHITE);
        b.setForeground(PRIMARY);
        b.setFont(new Font("Microsoft YaHei", Font.BOLD, 12));
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setMargin(new Insets(4, 14, 4, 14));
        return b;
    }

    private static JButton filledButton(String text, Color bg) {
        JButton b = new JButton(text);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
        b.setFont(FONT_BODY);
        b.setFocusPainted(false);
        styleButton(b);
        return b;
    }

    private static void styleButton(JButton b) {
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setMargin(new Insets(6, 14, 6, 14));
    }

    /** 统一高度的输入框 */
    public static JTextField textField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(FONT_BODY);
        tf.setPreferredSize(new Dimension(tf.getPreferredSize().width, 30));
        return tf;
    }

    /** 表格统一美化：表头深蓝、斑马纹、浅蓝选中、无网格、行高 28 */
    public static void styleTable(JTable table) {
        table.setRowHeight(28);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setFont(FONT_BODY);
        table.setSelectionBackground(SELECTION);
        table.setSelectionForeground(TEXT_MAIN);
        table.setDefaultRenderer(Object.class, new ZebraRenderer());

        JTableHeader header = table.getTableHeader();
        if (header != null) {
            header.setReorderingAllowed(false);
            header.setFont(FONT_HEADER);
            header.setBackground(PRIMARY);
            header.setForeground(Color.WHITE);
            header.setPreferredSize(new Dimension(header.getPreferredSize().width, 36));
        }
    }

    /** 斑马纹渲染器：偶数行白、奇数行极浅灰，选中行浅蓝 */
    private static class ZebraRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : ZEBRA);
            }
            return c;
        }
    }

    /** 顶部渐变标题栏（深蓝渐变，白字） */
    public static JPanel gradientHeader(String title, String subtitle) {
        JPanel p = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, PRIMARY, getWidth(), getHeight(), PRIMARY_DARK);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 0, 0);
                g2.dispose();
            }
        };
        p.setLayout(new BorderLayout());
        p.setPreferredSize(new Dimension(0, 82));
        p.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(Color.WHITE);

        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(FONT_SUBTITLE);
        subLabel.setForeground(new Color(255, 255, 255, 200));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(subLabel);

        p.add(textPanel, BorderLayout.WEST);
        return p;
    }

    /** 卡片式圆角边框 */
    public static Border cardBorder() {
        return new RoundedBorder(BORDER_LIGHT, 14);
    }

    /**
     * 从 resources/images 下加载背景图并铺满面板。
     * 加载失败时回退为纯色渐变面板，保证界面不空白。
     */
    public static JPanel backgroundPanel(String imageName, Color fallback1, Color fallback2) {
        Image bg = loadImage(imageName);
        if (bg != null) {
            return new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2.drawImage(bg, 0, 0, getWidth(), getHeight(), this);
                    g2.dispose();
                }
            };
        }
        return new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, fallback1, getWidth(), getHeight(), fallback2));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
    }

    private static Image loadImage(String name) {
        try (InputStream in = UITheme.class.getClassLoader().getResourceAsStream("images/" + name)) {
            if (in == null) {
                return null;
            }
            return ImageIO.read(in);
        } catch (IOException e) {
            return null;
        }
    }

    /** 圆角边框实现 */
    public static class RoundedBorder implements Border {
        private final Color color;
        private final int radius;
        public RoundedBorder(Color color, int radius) {
            this.color = color;
            this.radius = radius;
        }
        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(radius + 1, radius + 1, radius + 1, radius + 1);
        }
        @Override
        public boolean isBorderOpaque() {
            return false;
        }
        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.drawRoundRect(x, y, width - 1, height - 1, radius, radius);
            g2.dispose();
        }
    }
}
