package com.ncu.csh;

import com.ncu.csh.util.UITheme;
import com.ncu.csh.view.LoginView;
import com.ncu.csh.view.MainView;

import javax.swing.*;

/**
 * 程序入口 —— 健康管理系统
 */
public class App {
    public static void main(String[] args) {
        UITheme.apply(); // 先应用全局主题
        // 登录成功后由 App 编排跳转到主界面（避免 LoginView 反向依赖 MainView 造成模块循环依赖）
        SwingUtilities.invokeLater(() ->
                new LoginView(() -> new MainView().setVisible(true)).setVisible(true));
    }
}
