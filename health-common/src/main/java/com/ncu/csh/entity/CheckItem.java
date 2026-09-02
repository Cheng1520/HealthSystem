package com.ncu.csh.entity;

/**
 * 检查项实体 —— 对应检查项管理模块
 */
public class CheckItem {
    private Integer id;
    private String itemName;   // 检查项名称（血红蛋白、白细胞等）
    private String unit;       // 单位
    private Double refMin;     // 参考值下限
    private Double refMax;     // 参考值上限
    private String remark;     // 备注

    public CheckItem() {}

    public CheckItem(Integer id, String itemName, String unit) {
        this.id = id;
        this.itemName = itemName;
        this.unit = unit;
    }

    public CheckItem(Integer id, String itemName, String unit, Double refMin, Double refMax, String remark) {
        this.id = id;
        this.itemName = itemName;
        this.unit = unit;
        this.refMin = refMin;
        this.refMax = refMax;
        this.remark = remark;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public Double getRefMin() { return refMin; }
    public void setRefMin(Double refMin) { this.refMin = refMin; }
    public Double getRefMax() { return refMax; }
    public void setRefMax(Double refMax) { this.refMax = refMax; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }

    @Override
    public String toString() {
        return itemName;
    }
}
