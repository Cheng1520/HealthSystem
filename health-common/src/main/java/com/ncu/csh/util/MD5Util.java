package com.ncu.csh.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5 工具类 —— 对密码进行摘要加密，避免明文存储密码。
 */
public class MD5Util {

    /** 将字符串加密为 32 位小写 MD5 摘要 */
    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                String hex = Integer.toHexString(b & 0xFF);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 算法不存在", e);
        }
    }
}
