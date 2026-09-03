package com.ncu.csh.util;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * 头像工具类 —— 本地头像的上传 / 读取 / 默认占位图。
 * 头像文件保存在项目根目录下的 avatars 目录（随 git 一起提交），数据库只存文件名。
 */
public class AvatarUtil {

    private static final File AVATAR_DIR =
            new File(System.getProperty("user.dir"), "avatars");

    private AvatarUtil() {}

    /** 确保头像目录存在并返回 */
    public static File avatarDir() {
        if (!AVATAR_DIR.exists()) {
            AVATAR_DIR.mkdirs();
        }
        return AVATAR_DIR;
    }

    /**
     * 保存头像：把源图片统一转成 PNG 存为 avatar_{userId}.png，并删除该用户旧头像。
     * @return 保存后的文件名（数据库存这个）；失败返回 null
     */
    public static String save(File src, int userId) {
        if (src == null || !src.exists()) {
            return null;
        }
        BufferedImage img;
        try {
            img = ImageIO.read(src);
        } catch (IOException e) {
            return null;
        }
        if (img == null) {
            return null;
        }
        avatarDir();
        remove(userId);
        String filename = "avatar_" + userId + ".png";
        File dest = new File(AVATAR_DIR, filename);
        try {
            if (!ImageIO.write(img, "png", dest)) {
                return null;
            }
        } catch (IOException e) {
            return null;
        }
        return filename;
    }

    /** 删除某用户的头像文件 */
    public static void remove(int userId) {
        File[] old = AVATAR_DIR.listFiles((dir, name) -> name.startsWith("avatar_" + userId + "."));
        if (old != null) {
            for (File f : old) {
                f.delete();
            }
        }
    }

    /** 读取头像为指定尺寸的圆形图标；缺失则返回默认占位图 */
    public static ImageIcon loadIcon(String filename, int size) {
        Image img = loadImage(filename);
        if (img == null) {
            return defaultIcon(size);
        }
        return circle(new ImageIcon(img), size);
    }

    private static Image loadImage(String filename) {
        if (filename == null || filename.isEmpty()) {
            return null;
        }
        File f = new File(AVATAR_DIR, filename);
        if (!f.exists()) {
            return null;
        }
        try {
            return ImageIO.read(f);
        } catch (IOException e) {
            return null;
        }
    }

    /** 默认占位头像：圆形浅灰底 + 白色人形 */
    public static ImageIcon defaultIcon(int size) {
        BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(new Color(0xC9, 0xD6, 0xE8));
        g.fillOval(0, 0, size, size);
        g.setColor(Color.WHITE);
        g.fillOval(size / 4, size / 5, size / 2, size / 2);      // 头
        g.fillOval(size / 8, size * 3 / 5, size * 3 / 4, size / 2); // 肩
        g.dispose();
        return new ImageIcon(img);
    }

    /** 把图片缩放并裁剪成圆形 */
    private static ImageIcon circle(ImageIcon src, int size) {
        BufferedImage out = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setClip(new Ellipse2D.Double(0, 0, size, size));
        g2.drawImage(src.getImage(), 0, 0, size, size, null);
        g2.dispose();
        return new ImageIcon(out);
    }
}
