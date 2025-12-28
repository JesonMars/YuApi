package com.pingo.yuapi.dto;

import java.math.BigDecimal;

/**
 * 创建订单请求DTO
 */
public class CreateOrderRequest {
    private String tripId;
    private String pickupPoint;
    private String dropoffPoint;
    private Integer passengerCount;
    private BigDecimal totalAmount;
    private String openid; // 用户微信openid

    public CreateOrderRequest() {}

    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public String getPickupPoint() {
        return pickupPoint;
    }

    public void setPickupPoint(String pickupPoint) {
        this.pickupPoint = pickupPoint;
    }

    public String getDropoffPoint() {
        return dropoffPoint;
    }

    public void setDropoffPoint(String dropoffPoint) {
        this.dropoffPoint = dropoffPoint;
    }

    public Integer getPassengerCount() {
        return passengerCount;
    }

    public void setPassengerCount(Integer passengerCount) {
        this.passengerCount = passengerCount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getOpenid() {
        return openid;
    }

    public void setOpenid(String openid) {
        this.openid = openid;
    }
}
