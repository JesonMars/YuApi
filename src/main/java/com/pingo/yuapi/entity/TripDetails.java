package com.pingo.yuapi.entity;

import java.time.LocalDateTime;

/**
 * 行程详情扩展实体
 */
public class TripDetails {
    private String id;
    private String tripId; // 外键关联trips表
    private String vehicleId; // 外键关联vehicles表

    // 途经点
    private String pickupPoints; // 上车点列表（JSON数组）
    private String dropoffPoints; // 下车点列表（JSON数组）

    // 司机信息
    private String driverName;
    private String driverAvatar;
    private String driverPhone;
    private String vehicleInfo;
    private String plateNumber;

    // 行程详细信息
    private String notes; // 行程备注
    private Long pricePerSeat; // 单价/位（分）
    private Integer seatCount; // 座位数
    private Long totalIncome; // 预计总收入（分）
    private Long basePrice; // 基础价格（分）
    private Long extraFee; // 附加费用（分）

    // 乘客信息
    private Integer passengerCount; // 乘车人数

    // 时间戳
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public TripDetails() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getVehicleId() {
        return vehicleId;
    }

    public void setVehicleId(String vehicleId) {
        this.vehicleId = vehicleId;
    }

    public String getPickupPoints() {
        return pickupPoints;
    }

    public void setPickupPoints(String pickupPoints) {
        this.pickupPoints = pickupPoints;
    }

    public String getDropoffPoints() {
        return dropoffPoints;
    }

    public void setDropoffPoints(String dropoffPoints) {
        this.dropoffPoints = dropoffPoints;
    }

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getDriverAvatar() {
        return driverAvatar;
    }

    public void setDriverAvatar(String driverAvatar) {
        this.driverAvatar = driverAvatar;
    }

    public String getDriverPhone() {
        return driverPhone;
    }

    public void setDriverPhone(String driverPhone) {
        this.driverPhone = driverPhone;
    }

    public String getVehicleInfo() {
        return vehicleInfo;
    }

    public void setVehicleInfo(String vehicleInfo) {
        this.vehicleInfo = vehicleInfo;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Long getPricePerSeat() {
        return pricePerSeat;
    }

    public void setPricePerSeat(Long pricePerSeat) {
        this.pricePerSeat = pricePerSeat;
    }

    public Integer getSeatCount() {
        return seatCount;
    }

    public void setSeatCount(Integer seatCount) {
        this.seatCount = seatCount;
    }

    public Long getTotalIncome() {
        return totalIncome;
    }

    public void setTotalIncome(Long totalIncome) {
        this.totalIncome = totalIncome;
    }

    public Long getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(Long basePrice) {
        this.basePrice = basePrice;
    }

    public Long getExtraFee() {
        return extraFee;
    }

    public void setExtraFee(Long extraFee) {
        this.extraFee = extraFee;
    }

    public Integer getPassengerCount() {
        return passengerCount;
    }

    public void setPassengerCount(Integer passengerCount) {
        this.passengerCount = passengerCount;
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
