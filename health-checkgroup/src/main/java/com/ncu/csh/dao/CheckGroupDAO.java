package com.ncu.csh.dao;

import com.ncu.csh.entity.CheckGroup;
import com.ncu.csh.entity.CheckItem;
import com.ncu.csh.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * 检查组数据访问对象 —— 检查组管理模块的数据操作（含组-项关联）
 */
public class CheckGroupDAO {

    /**
     * 分页 + 编号搜索 + 名称搜索（三种查询机制）
     * 编号、名称都为空时即“查询所有检查组”。
     */
    public List<CheckGroup> queryByPage(String idKeyword, String nameKeyword, int page, int pageSize) {
        String sql = "SELECT id,group_name,remark FROM check_group "
                + "WHERE CAST(id AS CHAR) LIKE ? AND group_name LIKE ? ORDER BY id LIMIT ?,?";
        List<CheckGroup> list = new ArrayList<>();
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
                CheckGroup g = new CheckGroup();
                g.setId(rs.getInt("id"));
                g.setGroupName(rs.getString("group_name"));
                g.setRemark(rs.getString("remark"));
                list.add(g);
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("查询检查组失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    /** 查询全部检查组（预约下拉用） */
    public List<CheckGroup> listAll() {
        String sql = "SELECT id,group_name,remark FROM check_group ORDER BY id";
        List<CheckGroup> list = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                CheckGroup g = new CheckGroup();
                g.setId(rs.getInt("id"));
                g.setGroupName(rs.getString("group_name"));
                g.setRemark(rs.getString("remark"));
                list.add(g);
            }
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("查询全部检查组失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    /** 按 id 查询检查组及其全部检查项 */
    public CheckGroup getByIdWithItems(int groupId) {
        String sql = "SELECT g.id,g.group_name,g.remark, i.id item_id,i.item_name,i.unit "
                + "FROM check_group g "
                + "LEFT JOIN check_group_item gi ON g.id=gi.group_id "
                + "LEFT JOIN check_item i ON gi.item_id=i.id "
                + "WHERE g.id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        CheckGroup group = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, groupId);
            rs = ps.executeQuery();
            while (rs.next()) {
                if (group == null) {
                    group = new CheckGroup();
                    group.setId(rs.getInt("id"));
                    group.setGroupName(rs.getString("group_name"));
                    group.setRemark(rs.getString("remark"));
                }
                int itemId = rs.getInt("item_id");
                if (!rs.wasNull()) {
                    group.getItemList().add(new CheckItem(itemId, rs.getString("item_name"), rs.getString("unit")));
                }
            }
            return group;
        } catch (SQLException e) {
            throw new RuntimeException("查询检查组详情失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    /** 总数（统计看板用） */
    public int countAll() {
        String sql = "SELECT COUNT(*) FROM check_group";
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
            throw new RuntimeException("统计检查组失败", e);
        } finally {
            DBUtil.close(rs, ps, conn);
        }
    }

    /** 新增检查组（含勾选的检查项），事务保证一致性 */
    public int add(CheckGroup group, List<Integer> itemIds) {
        Connection conn = null;
        PreparedStatement psGroup = null;
        PreparedStatement psItem = null;
        ResultSet rsKey = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            psGroup = conn.prepareStatement(
                    "INSERT INTO check_group(group_name,remark) VALUES(?,?)",
                    Statement.RETURN_GENERATED_KEYS);
            psGroup.setString(1, group.getGroupName());
            psGroup.setString(2, group.getRemark());
            psGroup.executeUpdate();

            rsKey = psGroup.getGeneratedKeys();
            int groupId = 0;
            if (rsKey.next()) {
                groupId = rsKey.getInt(1);
            }

            if (itemIds != null && !itemIds.isEmpty()) {
                psItem = conn.prepareStatement("INSERT INTO check_group_item(group_id,item_id) VALUES(?,?)");
                for (Integer itemId : itemIds) {
                    psItem.setInt(1, groupId);
                    psItem.setInt(2, itemId);
                    psItem.addBatch();
                }
                psItem.executeBatch();
            }
            conn.commit();
            return 1;
        } catch (SQLException e) {
            rollback(conn);
            throw new RuntimeException("新增检查组失败", e);
        } finally {
            DBUtil.close(rsKey, psItem, psGroup, conn);
        }
    }

    /** 修改检查组：先更新基本信息，再重建关联项 */
    public int update(CheckGroup group, List<Integer> itemIds) {
        Connection conn = null;
        PreparedStatement psGroup = null;
        PreparedStatement psDel = null;
        PreparedStatement psItem = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            psGroup = conn.prepareStatement("UPDATE check_group SET group_name=?,remark=? WHERE id=?");
            psGroup.setString(1, group.getGroupName());
            psGroup.setString(2, group.getRemark());
            psGroup.setInt(3, group.getId());
            psGroup.executeUpdate();

            psDel = conn.prepareStatement("DELETE FROM check_group_item WHERE group_id=?");
            psDel.setInt(1, group.getId());
            psDel.executeUpdate();

            if (itemIds != null && !itemIds.isEmpty()) {
                psItem = conn.prepareStatement("INSERT INTO check_group_item(group_id,item_id) VALUES(?,?)");
                for (Integer itemId : itemIds) {
                    psItem.setInt(1, group.getId());
                    psItem.setInt(2, itemId);
                    psItem.addBatch();
                }
                psItem.executeBatch();
            }
            conn.commit();
            return 1;
        } catch (SQLException e) {
            rollback(conn);
            throw new RuntimeException("修改检查组失败", e);
        } finally {
            DBUtil.close(psItem, psDel, psGroup, conn);
        }
    }

    /** 删除检查组（外键级联删除关联） */
    public int deleteById(int id) {
        String sql = "DELETE FROM check_group WHERE id=?";
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = DBUtil.getConnection();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("删除检查组失败", e);
        } finally {
            DBUtil.close(ps, conn);
        }
    }

    private void rollback(Connection conn) {
        try {
            if (conn != null) {
                conn.rollback();
            }
        } catch (SQLException ignored) {
        }
    }
}
