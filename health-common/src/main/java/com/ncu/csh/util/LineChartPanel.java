package com.ncu.csh.util;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * 简易折线图面板 —— 用 Java2D 绘制折线图（无第三方依赖）。
 */
public class LineChartPanel extends JPanel {

    private final String title;
    private final String yUnit;
    private final List<String> xLabels;
    private final List<Double> values;

    public LineChartPanel(String title, String yUnit, List<String> xLabels, List<Double> values) {
        this.title = title;
        this.yUnit = yUnit == null ? "" : yUnit;
        this.xLabels = xLabels;
        this.values = values;
        setPreferredSize(new Dimension(440, 260));
        setBackground(Color.WHITE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (values == null || values.isEmpty()) {
            g.setColor(UITheme.TEXT_GRAY);
            g.setFont(new Font("Microsoft YaHei", Font.PLAIN, 13));
            g.drawString("暂无数据", getWidth() / 2 - 30, getHeight() / 2);
            return;
        }
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int padL = 56, padR = 24, padT = 40, padB = 42;
        int w = getWidth(), h = getHeight();
        int plotW = w - padL - padR;
        int plotH = h - padT - padB;

        // 标题
        g2.setColor(UITheme.TEXT_MAIN);
        g2.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        g2.drawString(title, padL, 22);

        // 数值范围
        double min = Double.MAX_VALUE, max = -Double.MAX_VALUE;
        for (double v : values) {
            min = Math.min(min, v);
            max = Math.max(max, v);
        }
        if (max == min) {
            max += 1;
            min -= 1;
        }
        double pad = (max - min) * 0.15;
        min -= pad;
        max += pad;

        // 网格线 + y 刻度
        g2.setFont(new Font("Microsoft YaHei", Font.PLAIN, 11));
        int ySteps = 5;
        for (int i = 0; i <= ySteps; i++) {
            double val = max - (max - min) * i / ySteps;
            int y = padT + (int) (plotH * i / (double) ySteps);
            g2.setColor(new Color(0xED, 0xF1, 0xF5));
            g2.drawLine(padL, y, padL + plotW, y);
            g2.setColor(UITheme.TEXT_GRAY);
            g2.drawString(String.format("%.1f", val), 6, y + 4);
        }

        // 坐标轴
        g2.setColor(UITheme.BORDER_LIGHT);
        g2.drawLine(padL, padT, padL, padT + plotH);
        g2.drawLine(padL, padT + plotH, padL + plotW, padT + plotH);

        int n = values.size();
        int[] xs = new int[n];
        int[] ys = new int[n];
        for (int i = 0; i < n; i++) {
            xs[i] = padL + (n == 1 ? plotW / 2 : (int) (plotW * i / (double) (n - 1)));
            ys[i] = padT + (int) (plotH * (max - values.get(i)) / (max - min));
        }

        // x 刻度
        g2.setColor(UITheme.TEXT_GRAY);
        for (int i = 0; i < n; i++) {
            String label = xLabels.get(i);
            int tw = g2.getFontMetrics().stringWidth(label);
            int tx = Math.max(padL, Math.min(xs[i] - tw / 2, padL + plotW - tw));
            g2.drawString(label, tx, padT + plotH + 18);
        }

        // 折线
        g2.setColor(UITheme.PRIMARY);
        g2.setStroke(new BasicStroke(2f));
        for (int i = 0; i < n - 1; i++) {
            g2.drawLine(xs[i], ys[i], xs[i + 1], ys[i + 1]);
        }

        // 数据点 + 数值标签
        for (int i = 0; i < n; i++) {
            g2.setColor(UITheme.PRIMARY);
            g2.fillOval(xs[i] - 4, ys[i] - 4, 8, 8);
            g2.setColor(UITheme.TEXT_MAIN);
            g2.setFont(new Font("Microsoft YaHei", Font.BOLD, 11));
            g2.drawString(String.valueOf(values.get(i)), xs[i] - 12, ys[i] - 8);
        }

        // y 轴单位
        if (!yUnit.isEmpty()) {
            g2.setColor(UITheme.TEXT_GRAY);
            g2.drawString("(" + yUnit + ")", 8, padT + 14);
        }
        g2.dispose();
    }
}
