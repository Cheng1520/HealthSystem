package com.ncu.csh.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * 检查组实体 —— 对应检查组管理模块（一个检查组包含多个检查项）
 */
public class CheckGroup {
    private Integer id;
    private String groupName;               // 检查组名称
    private String remark;                  // 备注
    private List<CheckItem> itemList = new ArrayList<>(); // 组内检查项

    public CheckGroup() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public List<CheckItem> getItemList() { return itemList; }
    public void setItemList(List<CheckItem> itemList) { this.itemList = itemList; }
}
