package com.ncu.csh.entity;

public class CheckItem {
    private Integer id;
    private String itemName;
    private String unit;

    public CheckItem(){}
    public CheckItem(Integer id, String itemName,String unit) {
        this.id = id;
        this.itemName = itemName;
        this.unit = unit;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    @Override
    public String toString() {
        return itemName;
    }
}