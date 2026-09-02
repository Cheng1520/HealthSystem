package com.ncu.csh.util;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 报表导出工具类 —— 将表格数据导出为彩色 HTML 报表并在浏览器打开。
 */
public class ReportUtil {

    /**
     * 导出一张数据表为 HTML 报表
     * @param title   报表标题
     * @param headers 列名
     * @param rows    数据行（每行一个 String[]）
     */
    public static void exportHtml(String title, String[] headers, List<String[]> rows) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html><html><head><meta charset='utf-8'>");
        html.append("<title>").append(title).append("</title>");
        html.append("<style>");
        html.append("body{font-family:'Microsoft YaHei',sans-serif;background:#f4f6fb;margin:0;padding:32px;}");
        html.append(".card{background:#fff;border-radius:14px;box-shadow:0 6px 20px rgba(0,0,0,.08);overflow:hidden;max-width:1000px;margin:0 auto;}");
        html.append(".head{background:linear-gradient(90deg,#2f80ed,#1e5fc0);color:#fff;padding:22px 28px;}");
        html.append(".head h1{margin:0;font-size:22px;}");
        html.append(".head p{margin:6px 0 0;font-size:13px;opacity:.85;}");
        html.append("table{border-collapse:collapse;width:100%;}");
        html.append("th{background:#eef2f9;color:#333;font-weight:bold;padding:12px 14px;text-align:left;font-size:14px;border-bottom:2px solid #dde5ef;}");
        html.append("td{padding:11px 14px;border-bottom:1px solid #eef2f9;font-size:13px;color:#444;}");
        html.append("tr:hover td{background:#f7faff;}");
        html.append(".foot{padding:14px 28px;color:#888;font-size:12px;text-align:right;}");
        html.append("</style></head><body><div class='card'>");
        html.append("<div class='head'><h1>").append(title).append("</h1>");
        html.append("<p>导出时间：").append(java.time.LocalDateTime.now().withNano(0)).append("</p></div>");
        html.append("<table><thead><tr>");
        for (String h : headers) {
            html.append("<th>").append(escape(h)).append("</th>");
        }
        html.append("</tr></thead><tbody>");
        for (String[] row : rows) {
            html.append("<tr>");
            for (String cell : row) {
                html.append("<td>").append(escape(cell == null ? "" : cell)).append("</td>");
            }
            html.append("</tr>");
        }
        html.append("</tbody></table>");
        html.append("<div class='foot'>共 ").append(rows.size()).append(" 条记录 · 健康管理系统</div>");
        html.append("</div></body></html>");

        // 写临时文件并用浏览器打开
        try {
            File file = File.createTempFile("report_", ".html");
            try (Writer w = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8)) {
                w.write(html.toString());
            }
            Desktop.getDesktop().browse(file.toURI());
        } catch (Exception e) {
            throw new RuntimeException("报表导出失败：" + e.getMessage(), e);
        }
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
