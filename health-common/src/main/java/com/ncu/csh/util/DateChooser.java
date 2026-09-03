package com.ncu.csh.util;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;

/**
 * 日期选择器 —— 弹出日历对话框，返回选中的日期（取消返回 null）。
 */
public final class DateChooser {

    private DateChooser() {}

    public static LocalDate choose(Component parent, LocalDate initial) {
        LocalDate start = initial == null ? LocalDate.now() : initial;
        final LocalDate[] result = new LocalDate[1];

        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(parent), "选择日期",
                Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setLayout(new BorderLayout(0, 10));

        final YearMonth[] ym = {YearMonth.from(start)};
        final LocalDate[] selected = {start};

        // 顶部：上月 / 年月 / 下月
        JLabel monthLabel = new JLabel("", SwingConstants.CENTER);
        monthLabel.setFont(new Font("Microsoft YaHei", Font.BOLD, 14));
        JButton prev = new JButton("◀");
        JButton next = new JButton("▶");
        prev.setFocusPainted(false);
        next.setFocusPainted(false);
        JPanel top = new JPanel(new BorderLayout());
        top.add(prev, BorderLayout.WEST);
        top.add(monthLabel, BorderLayout.CENTER);
        top.add(next, BorderLayout.EAST);
        dialog.add(top, BorderLayout.NORTH);

        // 日期网格
        JPanel grid = new JPanel(new GridLayout(0, 7, 2, 2));
        dialog.add(grid, BorderLayout.CENTER);

        Runnable refresh = () -> {
            monthLabel.setText(ym[0].getYear() + "年" + ym[0].getMonthValue() + "月");
            grid.removeAll();
            String[] week = {"一", "二", "三", "四", "五", "六", "日"};
            for (String w : week) {
                JLabel l = new JLabel(w, SwingConstants.CENTER);
                l.setForeground(UITheme.TEXT_GRAY);
                grid.add(l);
            }
            LocalDate first = ym[0].atDay(1);
            int offset = first.getDayOfWeek().getValue() - 1; // 周一=0
            int days = ym[0].lengthOfMonth();
            for (int i = 0; i < offset; i++) {
                grid.add(new JLabel());
            }
            for (int d = 1; d <= days; d++) {
                final LocalDate date = ym[0].atDay(d);
                JButton b = new JButton(String.valueOf(d));
                b.setFocusPainted(false);
                b.setMargin(new Insets(2, 2, 2, 2));
                if (date.equals(LocalDate.now())) {
                    b.setForeground(UITheme.PRIMARY);
                    b.setFont(b.getFont().deriveFont(Font.BOLD));
                }
                if (date.equals(selected[0])) {
                    b.setBackground(UITheme.PRIMARY);
                    b.setForeground(Color.WHITE);
                }
                b.addActionListener(e -> {
                    result[0] = date;
                    dialog.dispose();
                });
                grid.add(b);
            }
            grid.revalidate();
            grid.repaint();
        };

        prev.addActionListener(e -> { ym[0] = ym[0].minusMonths(1); refresh.run(); });
        next.addActionListener(e -> { ym[0] = ym[0].plusMonths(1); refresh.run(); });

        // 底部：今天 / 取消
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton today = new JButton("今天");
        JButton cancel = new JButton("取消");
        today.addActionListener(e -> { result[0] = LocalDate.now(); dialog.dispose(); });
        cancel.addActionListener(e -> dialog.dispose());
        bottom.add(today);
        bottom.add(cancel);
        dialog.add(bottom, BorderLayout.SOUTH);

        refresh.run();
        dialog.pack();
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true); // 模态阻塞，直到选择/取消
        return result[0];
    }
}
