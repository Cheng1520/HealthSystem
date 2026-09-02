package com.ncu.csh.entity;

/**
 * 检查结果实体 —— 对应预约与跟踪模块中的结果分析与病史对比
 */
public class CheckResult {
    private Integer id;
    private Integer appointmentId; // 所属预约 id
    private Integer itemId;        // 检查项 id
    private Double value;          // 检测数值
    private String analysis;       // 结果分析（正常/偏高/偏低）
    private String checkDate;      // 检测日期（来自预约日期）

    // 展示用（关联查询带出）
    private String itemName;
    private String unit;

    public CheckResult() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Integer appointmentId) { this.appointmentId = appointmentId; }
    public Integer getItemId() { return itemId; }
    public void setItemId(Integer itemId) { this.itemId = itemId; }
    public Double getValue() { return value; }
    public void setValue(Double value) { this.value = value; }
    public String getAnalysis() { return analysis; }
    public void setAnalysis(String analysis) { this.analysis = analysis; }
    public String getCheckDate() { return checkDate; }
    public void setCheckDate(String checkDate) { this.checkDate = checkDate; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
}
