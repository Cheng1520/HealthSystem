package com.ncu.csh.util;

import com.ncu.csh.entity.User;

/**
 * 会话工具类 —— 保存当前登录用户，供各模块使用。
 */
public class Session {

    /** 当前登录用户 */
    public static User currentUser;

    private Session() {}
}
