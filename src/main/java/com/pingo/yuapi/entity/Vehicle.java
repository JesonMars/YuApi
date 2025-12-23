package com.pingo.yuapi.entity;

import java.time.LocalDateTime;

/**
 * 车辆实体
 */
public class Vehicle {
    private String id;
    private String userId; // 车主用户ID

    // 车辆基本信息
    private String vin; // 车架号（Vehicle Identification Number）
    private String plateNumber; // 车牌号
    private String brand; // 车辆品牌
    private String color; // 车辆颜色
    private String model; // 车型

    // 默认车辆标识
    private Boolean isDefault; // 是否为用户的默认车辆

    // 时间戳
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public Vehicle() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getVin() {
        return vin;
    }

    public void setVin(String vin) {
        this.vin = vin;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Boolean getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}
