package com.pingo.yuapi.entity;

import java.time.LocalDateTime;

/**
 * 行程实体（精简版）- 只存核心数据
 */
public class Trip {
    private String id;
    private String userId; // 发布者ID
    private String type; // car_seeking_people(车找人) or people_seeking_car(人找车)

    // 核心地点信息
    private String startCity; // 起点城市
    private String startLocation; // 起点
    private Double startLongitude; // 起点经度
    private Double startLatitude; // 起点纬度
    private String endCity; // 终点城市
    private String endLocation; // 终点
    private Double endLongitude; // 终点经度
    private Double endLatitude; // 终点纬度

    // 核心时间信息
    private LocalDateTime departureTime;
    private String timing; // 时段：tonight/tomorrow

    // 核心数量信息
    private Integer availableSeats; // 可用座位数（车找人）
    private Integer bookedSeats; // 已预订座位数

    // 核心价格信息（单位：分）
    private Long price; // 价格（车找人是单价，人找车是总价）

    // 状态
    private String status; // available, full, cancelled, completed, expired

    // 时间戳
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // 关联用户信息（不存储在数据库，仅用于返回给前端）
    private transient String driverName; // 司机姓名
    private transient String carInfo; // 车辆信息（品牌+颜色）
    private transient Object pickupPoints; // 上车点列表
    private transient Object dropoffPoints; // 下车点列表

    public Trip() {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStartCity() {
        return startCity;
    }

    public void setStartCity(String startCity) {
        this.startCity = startCity;
    }

    public String getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    public String getEndCity() {
        return endCity;
    }

    public void setEndCity(String endCity) {
        this.endCity = endCity;
    }

    public String getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(String endLocation) {
        this.endLocation = endLocation;
    }

    public Double getStartLongitude() {
        return startLongitude;
    }

    public void setStartLongitude(Double startLongitude) {
        this.startLongitude = startLongitude;
    }

    public Double getStartLatitude() {
        return startLatitude;
    }

    public void setStartLatitude(Double startLatitude) {
        this.startLatitude = startLatitude;
    }

    public Double getEndLongitude() {
        return endLongitude;
    }

    public void setEndLongitude(Double endLongitude) {
        this.endLongitude = endLongitude;
    }

    public Double getEndLatitude() {
        return endLatitude;
    }

    public void setEndLatitude(Double endLatitude) {
        this.endLatitude = endLatitude;
    }

    public LocalDateTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(LocalDateTime departureTime) {
        this.departureTime = departureTime;
    }

    public Integer getAvailableSeats() {
        return availableSeats;
    }

    public void setAvailableSeats(Integer availableSeats) {
        this.availableSeats = availableSeats;
    }

    public Integer getBookedSeats() {
        return bookedSeats;
    }

    public void setBookedSeats(Integer bookedSeats) {
        this.bookedSeats = bookedSeats;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public String getTiming() {
        return timing;
    }

    public void setTiming(String timing) {
        this.timing = timing;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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

    public String getDriverName() {
        return driverName;
    }

    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }

    public String getCarInfo() {
        return carInfo;
    }

    public void setCarInfo(String carInfo) {
        this.carInfo = carInfo;
    }

    public Object getPickupPoints() {
        return pickupPoints;
    }

    public void setPickupPoints(Object pickupPoints) {
        this.pickupPoints = pickupPoints;
    }

    public Object getDropoffPoints() {
        return dropoffPoints;
    }

    public void setDropoffPoints(Object dropoffPoints) {
        this.dropoffPoints = dropoffPoints;
    }
}