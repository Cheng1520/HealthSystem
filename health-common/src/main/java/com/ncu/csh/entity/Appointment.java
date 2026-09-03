package com.ncu.csh.entity;

/**
 * 预约实体 —— 对应预约与跟踪模块
 */
public class Appointment {
    private Integer id;
    private Integer userId;       // 预约用户 id
    private String method;        // 体检方式：单项 / 套餐
    private Integer checkGroupId; // 套餐时选择的检查组 id
    private Integer checkItemId;  // 单项时选择的检查项 id
    private String appointDate;   // 预约日期
    private String status;        // 状态：已预约 / 已完成
    private String remark;        // 备注
    private String suggestion;    // 医生诊断建议

    // 展示用（关联查询带出）
    private String userName;      // 用户名/姓名
    private String groupName;     // 检查组名称
    private String itemName;      // 检查项名称

    public Appointment() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public Integer getCheckGroupId() { return checkGroupId; }
    public void setCheckGroupId(Integer checkGroupId) { this.checkGroupId = checkGroupId; }
    public Integer getCheckItemId() { return checkItemId; }
    public void setCheckItemId(Integer checkItemId) { this.checkItemId = checkItemId; }
    public String getAppointDate() { return appointDate; }
    public void setAppointDate(String appointDate) { this.appointDate = appointDate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public String getSuggestion() { return suggestion; }
    public void setSuggestion(String suggestion) { this.suggestion = suggestion; }
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
}
