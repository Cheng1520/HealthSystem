package com.ncu.csh.entity;

/**
 * 用户实体 —— 对应登录注册模块
 */
public class User {
    private Integer id;
    private String username;   // 用户名
    private String password;   // 密码（MD5 存储）
    private String realName;   // 真实姓名
    private String gender;     // 性别
    private Integer age;       // 年龄
    private String phone;      // 电话
    private String role;       // 角色：管理员 / 医生 / 普通用户
    private String avatar;     // 头像文件名（本地 avatars 目录）

    public User() {}

    public User(Integer id, String username, String password, String realName,
                String gender, Integer age, String phone) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.realName = realName;
        this.gender = gender;
        this.age = age;
        this.phone = phone;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getRealName() { return realName; }
    public void setRealName(String realName) { this.realName = realName; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
}
