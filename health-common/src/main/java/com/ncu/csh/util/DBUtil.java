package com.ncu.csh.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * 数据库工具类 —— 负责加载驱动、建立连接、关闭资源。
 * 数据库连接信息集中在此处，方便修改。
 */
public class DBUtil {

    private static final String URL =
            "jdbc:mysql://127.0.0.1:3306/healthsystem"
            + "?characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "qazplm";

    // 静态代码块：只加载一次驱动
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL 驱动加载失败", e);
        }
    }

    /** 建立数据库连接（每次返回新连接，避免共享连接导致的并发/关闭问题） */
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USERNAME, PASSWORD);
        } catch (SQLException e) {
            throw new RuntimeException("数据库连接失败，请确认 MySQL 已启动且 healthsystem 库已创建", e);
        }
    }

    /** 统一关闭资源，可传任意多个 AutoCloseable（Connection/Statement/ResultSet） */
    public static void close(AutoCloseable... closeables) {
        if (closeables == null) {
            return;
        }
        for (AutoCloseable c : closeables) {
            if (c != null) {
                try {
                    c.close();
                } catch (Exception ignored) {
                    // 关闭失败不影响主流程
                }
            }
        }
    }
}
