package com.ncu.csh.dao;

import com.ncu.csh.entity.CheckItem;
import com.ncu.csh.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * 检查项数据访问对象 —— 检查项管理模块的数据操作（分页/搜索/增删改）
 */
public class CheckItemDAO {

    /**
     * 分页 + 编号搜索 + 名称搜索（三种查询机制）
     * 编号、名称都为空时即“查询所有检查项”。
     */
    public List<CheckItem> queryByPage(String idKeyword, String nameKeyword, int page, int pageSize) {
        String sql = "SELECT id,item_name,unit,ref_min,ref_max,remark FROM check_item "
                + "WHERE CAST(id AS CHAR) LIKE ? AND item_name LIKE ? ORDER BY id LIMIT ?,?";
        List<CheckItem> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, "%" + idKeyword + "%");
            ps.setString(2, "%" + nameKeyword + "%");
            ps.setInt(3, (page - 1) * pageSize);
            ps.setInt(4, pageSize);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("查询检查项失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    /** 查询全部检查项（勾选组、预约下拉用） */
    public List<CheckItem> listAll() {
        String sql = "SELECT id,item_name,unit,ref_min,ref_max,remark FROM check_item ORDER BY id";
        List<CheckItem> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("查询全部检查项失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    /** 总数（统计看板用） */
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM check_item";
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
            throw new RuntimeException("统计检查项失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    /** 新增检查项 */
    public int add(CheckItem item) {
        String sql = "INSERT INTO check_item(item_name,unit,ref_min,ref_max,remark) VALUES(?,?,?,?,?)";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, item.getItemName());
            ps.setString(2, item.getUnit());
            setDouble(ps, 3, item.getRefMin());
            setDouble(ps, 4, item.getRefMax());
            ps.setString(5, item.getRemark());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("新增检查项失败", e);
        } finally {
            DBUtil.close(ps, conn);
        }
    }

    /** 修改检查项 */
    public int update(CheckItem item) {
        String sql = "UPDATE check_item SET item_name=?,unit=?,ref_min=?,ref_max=?,remark=? WHERE id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setString(1, item.getItemName());
            ps.setString(2, item.getUnit());
            setDouble(ps, 3, item.getRefMin());
            setDouble(ps, 4, item.getRefMax());
            ps.setString(5, item.getRemark());
            ps.setInt(6, item.getId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("修改检查项失败", e);
        } finally {
            DBUtil.close(ps, conn);
        }
    }

    /** 删除检查项 */
    public int deleteById(int id) {
        String sql = "DELETE FROM check_item WHERE id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("删除检查项失败", e);
        } finally {
            DBUtil.close(ps, conn);
        }
    }

    private void setDouble(PreparedStatement ps, int idx, Double v) throws SQLException {
        if (v == null) {
            ps.setNull(idx, java.sql.Types.DOUBLE);
        } else {
            ps.setDouble(idx, v);
        }
    }

    private CheckItem mapRow(ResultSet rs) throws SQLException {
        CheckItem c = new CheckItem();
        c.setId(rs.getInt("id"));
        c.setItemName(rs.getString("item_name"));
        c.setUnit(rs.getString("unit"));
        double min = rs.getDouble("ref_min");
        c.setRefMin(rs.wasNull() ? null : min);
        double max = rs.getDouble("ref_max");
        c.setRefMax(rs.wasNull() ? null : max);
        c.setRemark(rs.getString("remark"));
        return c;
    }
}
