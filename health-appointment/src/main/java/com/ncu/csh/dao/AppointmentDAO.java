package com.ncu.csh.dao;

import com.ncu.csh.entity.Appointment;
import com.ncu.csh.entity.CheckItem;
import com.ncu.csh.entity.CheckResult;
import com.ncu.csh.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 预约数据访问对象 —— 预约与跟踪模块的数据操作
 */
public class AppointmentDAO {

    /** 查询全部预约（关联带出用户名/检查组名/检查项名），按用户关键词搜索 */
    public List<Appointment> queryList(String keyword) {
        String sql = "SELECT a.id,a.user_id,a.method,a.check_group_id,a.check_item_id,"
                + "a.appoint_date,a.status,a.remark,a.suggestion,"
                + "u.username,u.real_name,g.group_name,i.item_name "
                + "FROM appointment a "
                + "LEFT JOIN user u ON a.user_id=u.id "
                + "LEFT JOIN check_group g ON a.check_group_id=g.id "
                + "LEFT JOIN check_item i ON a.check_item_id=i.id "
                + "WHERE u.username LIKE ? OR u.real_name LIKE ? "
                + "ORDER BY a.id DESC";
        List<Appointment> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            String like = "%" + keyword + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("查询预约失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    /** 按 id 查询单条预约 */
    public Appointment getById(int id) {
        String sql = "SELECT a.id,a.user_id,a.method,a.check_group_id,a.check_item_id,"
                + "a.appoint_date,a.status,a.remark,a.suggestion,"
                + "u.username,u.real_name,g.group_name,i.item_name "
                + "FROM appointment a "
                + "LEFT JOIN user u ON a.user_id=u.id "
                + "LEFT JOIN check_group g ON a.check_group_id=g.id "
                + "LEFT JOIN check_item i ON a.check_item_id=i.id "
                + "WHERE a.id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("查询预约失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    /** 新增预约 */
    public int add(Appointment a) {
        String sql = "INSERT INTO appointment(user_id,method,check_group_id,check_item_id,appoint_date,status,remark) "
                + "VALUES(?,?,?,?,?,?,?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, a.getUserId());
            ps.setString(2, a.getMethod());
            setInt(ps, 3, a.getCheckGroupId());
            setInt(ps, 4, a.getCheckItemId());
            ps.setString(5, a.getAppointDate());
            ps.setString(6, a.getStatus() == null ? "已预约" : a.getStatus());
            ps.setString(7, a.getRemark());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("新增预约失败", e);
        } finally {
            DBUtil.close(ps, conn);
        }
    }

    /** 修改预约 */
    public int update(Appointment a) {
        String sql = "UPDATE appointment SET user_id=?,method=?,check_group_id=?,check_item_id=?,"
                + "appoint_date=?,status=?,remark=? WHERE id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, a.getUserId());
            ps.setString(2, a.getMethod());
            setInt(ps, 3, a.getCheckGroupId());
            setInt(ps, 4, a.getCheckItemId());
            ps.setString(5, a.getAppointDate());
            ps.setString(6, a.getStatus());
            ps.setString(7, a.getRemark());
            ps.setInt(8, a.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("修改预约失败", e);
        } finally {
            DBUtil.close(ps, conn);
        }
    }

    /** 更新预约状态（如标记已完成） */
    public int updateStatus(int id, String status) {
        String sql = "UPDATE appointment SET status=? WHERE id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setInt(2, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("更新预约状态失败", e);
        } finally {
            DBUtil.close(ps, conn);
        }
    }

    /** 删除预约 */
    public int deleteById(int id) {
        String sql = "DELETE FROM appointment WHERE id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("删除预约失败", e);
        } finally {
            DBUtil.close(ps, conn);
        }
    }

    /** 今日预约数（主页统计看板用） */
    public int countToday() {
        String sql = "SELECT COUNT(*) FROM appointment WHERE appoint_date=CURDATE()";
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
            throw new RuntimeException("统计今日预约失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    /** 预约总数（统计看板用） */
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM appointment";
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
            throw new RuntimeException("统计预约失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    /** 根据预约查出应检测的检查项（单项→该检查项；套餐→检查组内全部检查项） */
    public List<CheckItem> listItemsByAppointment(int appointmentId) {
        String sql = "SELECT i.id,i.item_name,i.unit,i.ref_min,i.ref_max,i.remark "
                + "FROM appointment a "
                + "LEFT JOIN check_item i ON a.check_item_id=i.id "
                + "WHERE a.id=? AND a.method='单项' "
                + "UNION "
                + "SELECT i.id,i.item_name,i.unit,i.ref_min,i.ref_max,i.remark "
                + "FROM appointment a "
                + "JOIN check_group_item gi ON a.check_group_id=gi.group_id "
                + "JOIN check_item i ON gi.item_id=i.id "
                + "WHERE a.id=? AND a.method='套餐'";
        List<CheckItem> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, appointmentId);
            ps.setInt(2, appointmentId);
            rs = ps.executeQuery();
            while (rs.next()) {
                CheckItem c = new CheckItem();
                c.setId(rs.getInt("id"));
                c.setItemName(rs.getString("item_name"));
                c.setUnit(rs.getString("unit"));
                double min = rs.getDouble("ref_min");
                c.setRefMin(rs.wasNull() ? null : min);
                double max = rs.getDouble("ref_max");
                c.setRefMax(rs.wasNull() ? null : max);
                c.setRemark(rs.getString("remark"));
                list.add(c);
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("查询预约检查项失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    /** 录入检查结果（含分析结论） */
    public int saveResult(CheckResult r) {
        String sql = "INSERT INTO check_result(appointment_id,item_id,value,analysis,check_date) "
                + "VALUES(?,?,?,?,?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, r.getAppointmentId());
            ps.setInt(2, r.getItemId());
            ps.setDouble(3, r.getValue());
            ps.setString(4, r.getAnalysis());
            ps.setString(5, r.getCheckDate());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("保存检查结果失败", e);
        } finally {
            DBUtil.close(ps, conn);
        }
    }

    /** 某用户的某检查项历史结果（病史对比与跟踪用） */
    public List<CheckResult> listHistory(int userId, int itemId) {
        String sql = "SELECT r.id,r.value,r.analysis,r.check_date,i.item_name,i.unit "
                + "FROM check_result r "
                + "JOIN check_item i ON r.item_id=i.id "
                + "JOIN appointment a ON r.appointment_id=a.id "
                + "WHERE a.user_id=? AND r.item_id=? "
                + "ORDER BY r.check_date ASC";
        List<CheckResult> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            ps.setInt(2, itemId);
            rs = ps.executeQuery();
            while (rs.next()) {
                CheckResult r = new CheckResult();
                r.setId(rs.getInt("id"));
                r.setValue(rs.getDouble("value"));
                r.setAnalysis(rs.getString("analysis"));
                r.setCheckDate(rs.getString("check_date"));
                r.setItemName(rs.getString("item_name"));
                r.setUnit(rs.getString("unit"));
                list.add(r);
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("查询病史失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    /** 查询用户列表（预约选择用户用） */
    public List<Object[]> listUsers() {
        String sql = "SELECT id,username,real_name FROM user ORDER BY id";
        List<Object[]> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                String name = rs.getString("real_name");
                if (name == null || name.isEmpty()) {
                    name = rs.getString("username");
                }
                list.add(new Object[]{rs.getInt("id"), name});
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("查询用户列表失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    /** 查询某用户的全部预约（普通用户只能看自己的预约） */
    public List<Appointment> queryListByUser(int userId) {
        String sql = "SELECT a.id,a.user_id,a.method,a.check_group_id,a.check_item_id,"
                + "a.appoint_date,a.status,a.remark,a.suggestion,"
                + "u.username,u.real_name,g.group_name,i.item_name "
                + "FROM appointment a "
                + "LEFT JOIN user u ON a.user_id=u.id "
                + "LEFT JOIN check_group g ON a.check_group_id=g.id "
                + "LEFT JOIN check_item i ON a.check_item_id=i.id "
                + "WHERE a.user_id=? "
                + "ORDER BY a.id DESC";
        List<Appointment> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("查询预约失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    /** 查询某用户的检查报告（仅含已有检查结果的预约，用于历史检查报告） */
    public List<Appointment> listReportsByUser(int userId) {
        String sql = "SELECT a.id,a.user_id,a.method,a.check_group_id,a.check_item_id,"
                + "a.appoint_date,a.status,a.remark,a.suggestion,"
                + "u.username,u.real_name,g.group_name,i.item_name "
                + "FROM appointment a "
                + "LEFT JOIN user u ON a.user_id=u.id "
                + "LEFT JOIN check_group g ON a.check_group_id=g.id "
                + "LEFT JOIN check_item i ON a.check_item_id=i.id "
                + "WHERE a.user_id=? AND EXISTS (SELECT 1 FROM check_result r WHERE r.appointment_id=a.id) "
                + "ORDER BY a.id DESC";
        List<Appointment> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("查询检查报告失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    /** 查询全部检查报告（医生/管理员），可按用户名/姓名关键词搜索 */
    public List<Appointment> listReportsAll(String keyword) {
        String sql = "SELECT a.id,a.user_id,a.method,a.check_group_id,a.check_item_id,"
                + "a.appoint_date,a.status,a.remark,a.suggestion,"
                + "u.username,u.real_name,g.group_name,i.item_name "
                + "FROM appointment a "
                + "LEFT JOIN user u ON a.user_id=u.id "
                + "LEFT JOIN check_group g ON a.check_group_id=g.id "
                + "LEFT JOIN check_item i ON a.check_item_id=i.id "
                + "WHERE (u.username LIKE ? OR u.real_name LIKE ?) "
                + "AND EXISTS (SELECT 1 FROM check_result r WHERE r.appointment_id=a.id) "
                + "ORDER BY a.id DESC";
        List<Appointment> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            String like = "%" + (keyword == null ? "" : keyword.trim()) + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("查询检查报告失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    /** 查询某预约下的全部检查结果（含检查项名称与单位） */
    public List<CheckResult> listResultsByAppointment(int appointmentId) {
        String sql = "SELECT r.id,r.appointment_id,r.item_id,r.value,r.analysis,r.check_date,"
                + "i.item_name,i.unit "
                + "FROM check_result r "
                + "JOIN check_item i ON r.item_id=i.id "
                + "WHERE r.appointment_id=? ORDER BY r.id";
        List<CheckResult> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, appointmentId);
            rs = ps.executeQuery();
            while (rs.next()) {
                CheckResult r = new CheckResult();
                r.setId(rs.getInt("id"));
                r.setAppointmentId(rs.getInt("appointment_id"));
                r.setItemId(rs.getInt("item_id"));
                r.setValue(rs.getDouble("value"));
                r.setAnalysis(rs.getString("analysis"));
                r.setCheckDate(rs.getString("check_date"));
                r.setItemName(rs.getString("item_name"));
                r.setUnit(rs.getString("unit"));
                list.add(r);
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("查询检查结果失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    /** 医生为某次检查报告填写/更新诊断建议 */
    public int updateSuggestion(int appointmentId, String suggestion) {
        String sql = "UPDATE appointment SET suggestion=? WHERE id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, suggestion);
            ps.setInt(2, appointmentId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("保存诊断建议失败", e);
        } finally {
            DBUtil.close(ps, conn);
        }
    }

    private void setInt(PreparedStatement ps, int idx, Integer v) throws SQLException {
        if (v == null) {
            ps.setNull(idx, java.sql.Types.INTEGER);
        } else {
            ps.setInt(idx, v);
        }
    }

    private Appointment mapRow(ResultSet rs) throws SQLException {
        Appointment a = new Appointment();
        a.setId(rs.getInt("id"));
        a.setUserId(rs.getInt("user_id"));
        a.setMethod(rs.getString("method"));
        int gid = rs.getInt("check_group_id");
        a.setCheckGroupId(rs.wasNull() ? null : gid);
        int iid = rs.getInt("check_item_id");
        a.setCheckItemId(rs.wasNull() ? null : iid);
        a.setAppointDate(rs.getString("appoint_date"));
        a.setStatus(rs.getString("status"));
        a.setRemark(rs.getString("remark"));
        a.setSuggestion(rs.getString("suggestion"));
        String realName = rs.getString("real_name");
        String username = rs.getString("username");
        a.setUserName((realName == null || realName.isEmpty()) ? username : realName);
        a.setGroupName(rs.getString("group_name"));
        a.setItemName(rs.getString("item_name"));
        return a;
    }
}
