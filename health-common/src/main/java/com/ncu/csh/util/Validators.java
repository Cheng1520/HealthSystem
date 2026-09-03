package com.ncu.csh.util;

/**
 * 通用校验工具类
 */
public final class Validators {

    /** 中国大陆手机号：1 开头，第二位 3-9，共 11 位 */
    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";

    private Validators() {}

    public static boolean isValidPhone(String phone) {
        if (phone == null) {
            return false;
        }
        return phone.trim().matches(PHONE_REGEX);
    }
}
