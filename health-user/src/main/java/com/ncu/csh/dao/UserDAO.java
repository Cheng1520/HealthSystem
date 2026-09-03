package com.ncu.csh.dao;

import com.ncu.csh.entity.User;
import com.ncu.csh.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户数据访问对象 —— 登录注册模块的数据操作
 */
public class UserDAO {

    /** 登录：按用户名 + 密码（MD5）查询 */
    public User login(String username, String passwordMd5) {
        String sql = "SELECT id,username,password,real_name,gender,age,phone,role,avatar FROM user "
                + "WHERE username=? AND password=?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, passwordMd5);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("登录查询失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    /** 判断用户名是否已存在（注册去重） */
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM user WHERE username=?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException("用户名查重失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    /** 注册：新增用户 */
    public int register(User user) {
        String sql = "INSERT INTO user(username,password,real_name,gender,age,phone) VALUES(?,?,?,?,?,?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRealName());
            ps.setString(4, user.getGender());
            if (user.getAge() == null) {
                ps.setNull(5, java.sql.Types.INTEGER);
            } else {
                ps.setInt(5, user.getAge());
            }
            ps.setString(6, user.getPhone());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("注册失败", e);
        } finally {
            DBUtil.close(ps, conn);
        }
    }

    /** 修改密码：按 id 更新 */
    public int updatePassword(int userId, String newPasswordMd5) {
        String sql = "UPDATE user SET password=? WHERE id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, newPasswordMd5);
            ps.setInt(2, userId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("修改密码失败", e);
        } finally {
            DBUtil.close(ps, conn);
        }
    }

    /** 忘记密码校验：按用户名 + 电话查询（用于找回） */
    public User findByUsernameAndPhone(String username, String phone) {
        String sql = "SELECT id,username,password,real_name,gender,age,phone,role,avatar FROM user "
                + "WHERE username=? AND phone=?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, phone);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("找回密码校验失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    /** 用户总数（主页统计看板用） */
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM user";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new RuntimeException("统计用户数失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    /** 用户管理：按账号/姓名模糊分页查询 */
    public List<User> queryByPage(String keyword, int page, int pageSize) {
        String sql = "SELECT id,username,password,real_name,gender,age,phone,role,avatar FROM user "
                + "WHERE username LIKE ? OR real_name LIKE ? ORDER BY id LIMIT ?,?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<User> list = new ArrayList<>();
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            String kw = "%" + (keyword == null ? "" : keyword.trim()) + "%";
            ps.setString(1, kw);
            ps.setString(2, kw);
            ps.setInt(3, (page - 1) * pageSize);
            ps.setInt(4, pageSize);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("查询用户失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    /** 用户管理：新增用户（含角色），返回新用户的自增 ID */
    public int add(User user) {
        String sql = "INSERT INTO user(username,password,real_name,gender,age,phone,role) VALUES(?,?,?,?,?,?,?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ps.setString(3, user.getRealName());
            ps.setString(4, user.getGender());
            if (user.getAge() == null) {
                ps.setNull(5, java.sql.Types.INTEGER);
            } else {
                ps.setInt(5, user.getAge());
            }
            ps.setString(6, user.getPhone());
            ps.setString(7, user.getRole());
            ps.executeUpdate();
            rs = ps.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("新增用户失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    /** 用户管理：更新用户（含角色） */
    public int update(User user) {
        String sql = "UPDATE user SET username=?,real_name=?,gender=?,age=?,phone=?,role=? WHERE id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getRealName());
            ps.setString(3, user.getGender());
            if (user.getAge() == null) {
                ps.setNull(4, java.sql.Types.INTEGER);
            } else {
                ps.setInt(4, user.getAge());
            }
            ps.setString(5, user.getPhone());
            ps.setString(6, user.getRole());
            ps.setInt(7, user.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("更新用户失败", e);
        } finally {
            DBUtil.close(ps, conn);
        }
    }

    /** 用户管理：删除用户 */
    public int deleteById(int id) {
        String sql = "DELETE FROM user WHERE id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("删除用户失败", e);
        } finally {
            DBUtil.close(ps, conn);
        }
    }

    /** 用户管理：单独更新头像文件名 */
    public int updateAvatar(int id, String avatar) {
        String sql = "UPDATE user SET avatar=? WHERE id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, avatar);
            ps.setInt(2, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("更新头像失败", e);
        } finally {
            DBUtil.close(ps, conn);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setUsername(rs.getString("username"));
        u.setPassword(rs.getString("password"));
        u.setRealName(rs.getString("real_name"));
        u.setGender(rs.getString("gender"));
        int age = rs.getInt("age");
        u.setAge(rs.wasNull() ? null : age);
        u.setPhone(rs.getString("phone"));
        u.setRole(rs.getString("role"));
        u.setAvatar(rs.getString("avatar"));
        return u;
    }
}
