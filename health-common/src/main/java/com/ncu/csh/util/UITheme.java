package com.ncu.csh.util;

import com.formdev.flatlaf.FlatLightLaf;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

/**
 * 界面主题工具类 —— 统一配色、圆角、字体、渐变面板，让界面更美观。
 */
public class UITheme {

    // 主题色板
    public static final Color PRIMARY   = new Color(0x2F, 0x80, 0xED); // 主蓝
    public static final Color PRIMARY_DARK = new Color(0x1E, 0x5F, 0xC0);
    public static final Color TEAL      = new Color(0x0F, 0xB5, 0x9A); // 青
    public static final Color GREEN     = new Color(0x22, 0xC5, 0x5E); // 绿
    public static final Color ORANGE    = new Color(0xF5, 0x9E, 0x0B); // 橙
    public static final Color PURPLE    = new Color(0x8B, 0x5C, 0xF6); // 紫
    public static final Color RED       = new Color(0xEF, 0x44, 0x44); // 红
    public static final Color BG        = new Color(0xF4, 0xF6, 0xFB); // 页面背景
    public static final Color TEXT_GRAY = new Color(0x6B, 0x72, 0x80); // 次要文字

    /** 应用全局主题，须在创建任何界面之前调用 */
    public static void apply() {
        FlatLightLaf.setup();
        // 统一字体（微软雅黑，含中文）
        Font base = new Font("Microsoft YaHei", Font.PLAIN, 14);
        UIManager.put("defaultFont", base);
        UIManager.put("Label.font", base);
        UIManager.put("Button.font", base);
        UIManager.put("Table.font", base);
        UIManager.put("TableHeader.font", base);
        UIManager.put("TextField.font", base);
        // 强调色与圆角
        UIManager.put("Component.focusColor", PRIMARY);
        UIManager.put("Button.arc", 10);
        UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 8);
    }

    /** 主色按钮 */
    public static JButton primaryButton(String text) {
        JButton b = new JButton(text);
        b.setBackground(PRIMARY);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        b.setFocusPainted(false);
        return b;
    }

    /** 普通描边按钮 */
    public static JButton plainButton(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        return b;
    }

    /** 顶部渐变标题栏 */
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
        p.setPreferredSize(new Dimension(0, 86));
        p.setBorder(BorderFactory.createEmptyBorder(14, 24, 14, 24));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        JLabel subLabel = new JLabel(subtitle);
        subLabel.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
        subLabel.setForeground(new Color(255, 255, 255, 210));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 4));
        textPanel.setOpaque(false);
        textPanel.add(titleLabel);
        textPanel.add(subLabel);

        p.add(textPanel, BorderLayout.WEST);
        return p;
    }

    /** 卡片式圆角边框 */
    public static Border cardBorder() {
        return new RoundedBorder(new Color(0xE2, 0xE8, 0xF0), 14);
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
        // 回退：渐变面板
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
