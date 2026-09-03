package com.ncu.csh.view;

import com.ncu.csh.util.Session;
import com.ncu.csh.util.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * 首页 —— 按角色显示不同的背景图片（图片仅作用于首页）。
 */
public class HomeView extends JPanel {

    public HomeView() {
        String role = Session.currentUser.getRole();
        String imageName;
        if ("管理员".equals(role)) {
            imageName = "home_admin.jpg";
        } else if ("医生".equals(role)) {
            imageName = "home_doctor.jpg";
        } else {
            imageName = "home_user.jpg";
        }
        setLayout(new BorderLayout());
        JPanel bg = UITheme.backgroundPanel(imageName, new Color(0xF2, 0xF7, 0xFF), new Color(0xE2, 0xF2, 0xF6));
        bg.setLayout(new BorderLayout());
        add(bg, BorderLayout.CENTER);
    }
}
