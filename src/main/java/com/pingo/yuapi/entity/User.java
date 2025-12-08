package com.pingo.yuapi.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class User {
    private String id;
    private String name;
    private String phone;
    private String avatar;
    private String community;
    private String vehicleBrand;
    private String vehicleColor;
    private String plateNumber;
    private BigDecimal balance;
    private Integer coupons;
    private Integer historyOrders;
    private String verificationStatus; // pending, verified, rejected, none
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 认证相关字段
    private String realName;
    private String idCard;
    private String driverLicensePhoto;
    private String vehicleLicensePhoto;
    
    // 微信相关字段
    private String wechatOpenid;
    private String wechatUnionid;
    
    // 首次设置标识
    private Boolean firstSetupCompleted;

    public User() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getCommunity() {
        return community;
    }

    public void setCommunity(String community) {
        this.community = community;
    }

    public String getVehicleBrand() {
        return vehicleBrand;
    }

    public void setVehicleBrand(String vehicleBrand) {
        this.vehicleBrand = vehicleBrand;
    }

    public String getVehicleColor() {
        return vehicleColor;
    }

    public void setVehicleColor(String vehicleColor) {
        this.vehicleColor = vehicleColor;
    }

    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public Integer getCoupons() {
        return coupons;
    }

    public void setCoupons(Integer coupons) {
        this.coupons = coupons;
    }

    public Integer getHistoryOrders() {
        return historyOrders;
    }

    public void setHistoryOrders(Integer historyOrders) {
        this.historyOrders = historyOrders;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
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

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getDriverLicensePhoto() {
        return driverLicensePhoto;
    }

    public void setDriverLicensePhoto(String driverLicensePhoto) {
        this.driverLicensePhoto = driverLicensePhoto;
    }

    public String getVehicleLicensePhoto() {
        return vehicleLicensePhoto;
    }

    public void setVehicleLicensePhoto(String vehicleLicensePhoto) {
        this.vehicleLicensePhoto = vehicleLicensePhoto;
    }

    public String getWechatOpenid() {
        return wechatOpenid;
    }

    public void setWechatOpenid(String wechatOpenid) {
        this.wechatOpenid = wechatOpenid;
    }

    public String getWechatUnionid() {
        return wechatUnionid;
    }

    public void setWechatUnionid(String wechatUnionid) {
        this.wechatUnionid = wechatUnionid;
    }

    public Boolean getFirstSetupCompleted() {
        return firstSetupCompleted;
    }

    public void setFirstSetupCompleted(Boolean firstSetupCompleted) {
        this.firstSetupCompleted = firstSetupCompleted;
    }
}